package me.cth451.paperframe.util.tileviewer;

import me.cth451.paperframe.PaperFramePlugin;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;

public class TileSetViewerClient {
	private static final String uninitializedError = "API endpoint base URL is not set in plugin config.";
	/* Local Fields */
	private PaperFramePlugin plugin = null;
	private boolean borked = true;
	private final ReentrantLock cacheLock = new ReentrantLock();
	private final HashMap<String, GroupMetadata> localMetadataCache = new HashMap<>();
	private final HashMap<String, Instant> localMetadataMtime = new HashMap<>();

	/**
	 * Client constructor - mostly sets up SSL
	 */
	public TileSetViewerClient(@NotNull PaperFramePlugin plugin) {
		SSLContext sslContext = null;
		this.plugin = plugin;
		try {
			sslContext = SSLContext.getInstance("TLS");
		} catch (NoSuchAlgorithmException e) {
			return;
		}

		SSLContext.setDefault(sslContext);
		try {
			sslContext.init(null, null, null);
		} catch (KeyManagementException e) {
			return;
		}

		borked = false;
	}

	/**
	 * @return current API endpoint base url string via config
	 */
	private @Nullable String getEndpointBase() {
		String base = this.plugin.getConfig().getString("util.tileviewer.endpoint_base", null);
		return base == null || base.isEmpty() ? null : base;
	}

	/**
	 * Normalize path - essentially removing first and last slashes
	 *
	 * @param groupPath tile set path
	 * @return normalized path
	 */
	static @NotNull String normalizePath(@NotNull String groupPath) {
		return StringUtils.strip(groupPath, "/");
	}

	/**
	 * Verify whether a certain tile set is available locally
	 * <br>
	 * Side effect: Metadata cache more than 1 day old is presumed invalid.
	 *
	 * @param groupPath tile set path
	 * @return group meta if locally cached and valid
	 */
	public GroupMetadata checkCache(@NotNull String groupPath) {
		if (getEndpointBase() == null) {
			throw new IllegalStateException(uninitializedError);
		}

		String normalizedPath = normalizePath(groupPath);
		cacheLock.lock();
		GroupMetadata metadata = localMetadataCache.getOrDefault(normalizedPath, null);
		Instant mtime = localMetadataMtime.getOrDefault(normalizedPath, Instant.ofEpochMilli(0));
		cacheLock.unlock();

		if (mtime.isBefore(Instant.now().minus(Duration.ofDays(1)))) {
			return null;
		}
		return metadata;
	}

	/**
	 * Schedule a background refresh of specified tileset
	 *
	 * @param plugin         reference to local plugin
	 * @param groupPath      tileset path
	 * @param notifyCallback notification callback - boolean = success or not, String = message
	 */
	public void scheduleBackgroundFetch(@NotNull PaperFramePlugin plugin,
	                                    @NotNull String groupPath,
	                                    final BiConsumer<Boolean, String> notifyCallback) {
		new Thread(() -> {
			if (getEndpointBase() == null) {
				plugin.getComponentLogger().error(uninitializedError);
				Bukkit.getScheduler()
				      .scheduleSyncDelayedTask(plugin, () -> notifyCallback.accept(false, uninitializedError));
			}
			try {
				getMetadata(groupPath);
			} catch (IllegalStateException e) {
				plugin.getComponentLogger().error("API is not ready", e);
				Bukkit.getScheduler()
				      .scheduleSyncDelayedTask(plugin, () -> notifyCallback.accept(false, e.getMessage()));
				return;
			} catch (IOException e) {
				plugin.getComponentLogger().error("Network Error", e);
				Bukkit.getScheduler()
				      .scheduleSyncDelayedTask(plugin, () -> notifyCallback.accept(false, e.getMessage()));
				return;
			} catch (RuntimeException e) {
				plugin.getComponentLogger().error("Internal Error", e);
				Bukkit.getScheduler()
				      .scheduleSyncDelayedTask(plugin,
				                               () -> notifyCallback.accept(false,
				                                                           "Internal error occurred - please check server logs"));
				return;
			}
			String message = String.format("Metadata for tileset %s downloaded. Please try again.", groupPath);
			plugin.getComponentLogger().info("Metadata for tileset {} downloaded and cached", groupPath);
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> notifyCallback.accept(true, message));
		}).start();
	}

	public void getMetadata(@NotNull String groupPath) throws IOException {
		if (getEndpointBase() == null) {
			throw new IllegalStateException(uninitializedError);
		}

		if (borked) {
			throw new IllegalStateException("Client initialization was unsuccessful");
		}

		String normalizedPath = normalizePath(groupPath);

		/* May throw MalformedURLException */
		URL url = new URL(getEndpointBase() + "/v1/group/" + normalizedPath);

		/* May throw IOException */
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");
		/* No need to refresh if everything is still update to date */
		this.cacheLock.lock();
		connection.setIfModifiedSince(localMetadataMtime.getOrDefault(normalizedPath, Instant.ofEpochMilli(0))
		                                                .toEpochMilli());
		this.cacheLock.unlock();
		connection.setDoInput(true);
		connection.connect();

		if (connection.getResponseCode() == 314) {
			/* No need to set update time. Update local records only. */
			this.cacheLock.lock();
			this.localMetadataMtime.put(normalizedPath, Instant.now());
			this.cacheLock.unlock();
			connection.disconnect();
			return;
		}
		if (connection.getResponseCode() != 200) {
			throw new IOException(String.format("Could not retrieve metadata for %s: %d %s",
			                                    normalizedPath,
			                                    connection.getResponseCode(),
			                                    connection.getResponseMessage()));
		}
		InputStream inputStream = connection.getInputStream();
		String response = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
		connection.disconnect();

		GroupMetadata metadata = GroupMetadata.fromJson(response);

		this.cacheLock.lock();
		localMetadataCache.put(normalizedPath, metadata);
		localMetadataMtime.put(normalizedPath, Instant.ofEpochMilli(connection.getLastModified()));
		this.cacheLock.unlock();
	}

	public DirectoryListing listPrefix(@NotNull String prefix) throws IOException {
		if (getEndpointBase() == null) {
			throw new IllegalStateException(uninitializedError);
		}

		if (borked) {
			throw new IllegalStateException("Client initialization was unsuccessful");
		}

		/* May throw MalformedURLException */
		String normalized = StringUtils.stripStart(prefix, "/");
		URL url = new URL(getEndpointBase() + "/v1/list-group/" + normalized);

		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");
		connection.setDoInput(true);
		connection.connect();
		if (connection.getResponseCode() != 200) {
			throw new IOException(String.format("Could not retrieve directory listing for %s: %d %s",
			                                    prefix,
			                                    connection.getResponseCode(),
			                                    connection.getResponseMessage()));
		}
		InputStream inputStream = connection.getInputStream();
		String response = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
		connection.disconnect();

		return DirectoryListing.fromJson(response);
	}
}

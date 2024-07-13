package me.cth451.paperframe.util.tileviewer;

import me.cth451.paperframe.PaperFramePlugin;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
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
	private final HashMap<String, GroupMetadata> localCache = new HashMap<>();

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
	 * Side effect: Metadata cache more than 1 day old is presumed invalid and removed.
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
		GroupMetadata metadata = localCache.getOrDefault(normalizedPath, null);
		if (metadata != null && metadata.created.isBefore(Instant.now().minus(Duration.ofDays(1)))) {
			localCache.remove(normalizedPath);
		}
		cacheLock.unlock();
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
			} catch (RuntimeException e) {
				plugin.getComponentLogger().trace("Error downloading group meta", e);
				Bukkit.getScheduler()
				      .scheduleSyncDelayedTask(plugin, () -> notifyCallback.accept(false, e.getMessage()));
				return;
			}
			String message = String.format("Metadata for tileset %s downloaded. Please try again.", groupPath);
			plugin.getComponentLogger().info("Metadata for tileset {} downloaded and cached", groupPath);
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> notifyCallback.accept(true, message));
		}).start();
	}

	public GroupMetadata getMetadata(@NotNull String groupPath) {
		if (getEndpointBase() == null) {
			throw new IllegalStateException(uninitializedError);
		}

		if (borked) {
			throw new IllegalStateException("Client initialization was unsuccessful");
		}

		String normalizedPath = normalizePath(groupPath);
		GroupMetadata metadata = checkCache(normalizedPath);
		if (metadata != null) {
			return metadata;
		}

		URL url;
		try {
			url = new URL(getEndpointBase() + "/group/" + normalizedPath);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}

		String response;
		try {
			HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setDoInput(true);
			connection.connect();
			InputStream inputStream = connection.getInputStream();
			response = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
			connection.disconnect();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		metadata = GroupMetadata.fromJson(response);

		localCache.put(normalizedPath, metadata);
		return metadata;
	}
}

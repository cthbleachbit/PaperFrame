package me.cth451.paperframe.util.tileviewer;

import me.cth451.paperframe.PaperFramePlugin;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.net.ssl.SSLContext;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

public class TileSetViewerClient {
	private static final String uninitializedError = "API endpoint base URL is not set in plugin config.";
	private static final String initFailedError = "Client unavailable as initialization was unsuccessful.";
	/* Local Fields */
	private PaperFramePlugin plugin = null;
	private boolean borked = true;
	private final ConcurrentHashMap<String, GroupMetadata> metadataCache = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<String, DirectoryListing> listingCache = new ConcurrentHashMap<>();

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
	public GroupMetadata checkMetadataCache(@NotNull String groupPath) {
		if (getEndpointBase() == null) {
			throw new IllegalStateException(uninitializedError);
		}

		String normalizedPath = normalizePath(groupPath);
		GroupMetadata metadata = metadataCache.getOrDefault(normalizedPath, null);

		if (metadata == null || metadata.created.isBefore(Instant.now().minus(Duration.ofDays(1)))) {
			return null;
		}
		return metadata;
	}

	/**
	 * Schedule a background refresh of specified tileset. Note that network operation is intentionally forked
	 * to background as minecraft command execution is synchronous.
	 *
	 * @param plugin         reference to local plugin
	 * @param groupPath      tileset path
	 * @param notifyCallback notification callback - boolean = success or not, String = message
	 */
	public void scheduleMetadataFetch(@NotNull PaperFramePlugin plugin,
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
			} catch (FileNotFoundException e) {
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

	/**
	 * Call /api/v1/group/[path] - result will be returned stored in cache
	 *
	 * @param groupPath path prefix
	 * @return group metadata
	 * @throws IOException           for network errors
	 * @throws FileNotFoundException for a 404 response
	 */
	public GroupMetadata getMetadata(@NotNull String groupPath) throws IOException {
		if (getEndpointBase() == null) {
			throw new IllegalStateException(uninitializedError);
		}

		if (borked) {
			throw new IllegalStateException(initFailedError);
		}

		String normalizedPath = normalizePath(groupPath);

		/* May throw MalformedURLException */
		URL url = new URL(getEndpointBase() + "/v1/group/" + normalizedPath);

		/* May throw IOException */
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");

		/* Inform server of local timestamps */
		GroupMetadata metadata = metadataCache.getOrDefault(normalizedPath, null);

		if (metadata != null) {
			connection.setIfModifiedSince(metadata.created.toEpochMilli());
		}

		connection.setDoInput(true);
		connection.connect();

		if (connection.getResponseCode() == 314) {
			/* No need to update information - content up to date */
			assert metadata != null;
			metadata.created = Instant.now();
			connection.disconnect();
			return metadata;
		} else if (connection.getResponseCode() == 404) {
			metadataCache.remove(normalizedPath);
			throw new FileNotFoundException(String.format("Tile set %s does not exist in the database!",
			                                              normalizedPath));
		} else if (connection.getResponseCode() != 200) {
			throw new IOException(String.format("Could not retrieve metadata for %s: %d %s",
			                                    normalizedPath,
			                                    connection.getResponseCode(),
			                                    connection.getResponseMessage()));
		}
		InputStream inputStream = connection.getInputStream();
		String response = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
		connection.disconnect();

		metadata = GroupMetadata.fromJson(response);
		metadataCache.put(normalizedPath, metadata);
		return metadata;
	}

	/**
	 * Call /api/v1/list-groups/[path] - result will be returned and stored in cache
	 *
	 * @param prefix path prefix
	 * @return directory listing of that path
	 * @throws IOException for network errors
	 */
	public DirectoryListing listPrefix(@NotNull String prefix) throws IOException {
		if (getEndpointBase() == null) {
			throw new IllegalStateException(uninitializedError);
		}

		if (borked) {
			throw new IllegalStateException(initFailedError);
		}

		/* Check cache */
		String normalized = StringUtils.stripStart(prefix, "/");
		DirectoryListing listing = listingCache.getOrDefault(normalized, null);
		if (listing != null && listing.created.isAfter(Instant.now().minus(Duration.ofMinutes(10)))) {
			this.plugin.getComponentLogger().info("Returning cached directory listing for '{}'", normalized);
			return listing;
		}

		/* May throw MalformedURLException */
		URL url = new URL(getEndpointBase() + "/v1/list-group/" + normalized);
		this.plugin.getComponentLogger().info("Retrieving directory listing for '{}'", normalized);

		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");
		connection.setDoInput(true);
		connection.connect();
		if (connection.getResponseCode() != 200) {
			throw new IOException(String.format("Could not retrieve directory listing for '%s': %d %s",
			                                    prefix,
			                                    connection.getResponseCode(),
			                                    connection.getResponseMessage()));
		}
		InputStream inputStream = connection.getInputStream();
		String response = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
		connection.disconnect();

		listing = DirectoryListing.fromJson(response);
		listingCache.put(normalized, listing);
		return listing;
	}
}

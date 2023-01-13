package me.cth451.paperframe.dependency;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.session.SessionManager;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import me.cth451.paperframe.PaperFramePlugin;
import org.bukkit.entity.ItemFrame;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public class WorldEditAPI {
	private final PaperFramePlugin plugin;

	/**
	 * Constructor
	 *
	 * @param plugin initializing plugin
	 */
	public WorldEditAPI(@NotNull PaperFramePlugin plugin) {
		this.plugin = plugin;
		this.plugin.getLogger().info("WorldEdit integration initialized.");
	}

	/**
	 * Get frames within a cuboid
	 *
	 * @param player requesting player
	 * @return list of item frames contained in the selection, or empty list if the selection is not a cube
	 */
	public List<ItemFrame> getCuboidSelection(@NotNull org.bukkit.entity.Player player) {

		com.sk89q.worldedit.entity.Player actor = BukkitAdapter.adapt(player);
		SessionManager manager = com.sk89q.worldedit.WorldEdit.getInstance().getSessionManager();
		LocalSession localSession = manager.get(actor);
		com.sk89q.worldedit.regions.Region region;
		com.sk89q.worldedit.world.World selectionWorld = localSession.getSelectionWorld();
		try {
			if (selectionWorld == null) throw new IncompleteRegionException();
			region = localSession.getSelection(selectionWorld);
			if (!(region instanceof CuboidRegion)) throw new IncompleteRegionException();
		} catch (IncompleteRegionException ex) {
			actor.printError(TextComponent.of("Please make a cuboid region selection first."));
			return new LinkedList<>();
		}

		return BukkitAdapter.adapt(selectionWorld).getEntitiesByClass(ItemFrame.class)
		                    .stream()
		                    .filter(e -> region.getBoundingBox().contains(BukkitAdapter.asBlockVector(e.getLocation())))
		                    .toList();
	}
}

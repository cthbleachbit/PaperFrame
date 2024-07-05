package me.cth451.paperframe.command;

import me.cth451.paperframe.PaperFramePlugin;
import me.cth451.paperframe.util.Targeting;
import me.cth451.paperframe.util.getopt.ArgvParser;
import me.cth451.paperframe.util.getopt.UnixFlagSpec;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;

import static me.cth451.paperframe.util.Targeting.byTargetedStackedEntity;

/**
 * Removes ALL item frames attached on the block surface under the cursor or with -w all frames within selection.
 */
public class FrameUnmap implements CommandExecutor {
	/**
	 * Ref to plugin instance itself
	 */
	private final PaperFramePlugin plugin;

	protected final static UnixFlagSpec[] arguments = {
			new UnixFlagSpec("use-we", 'w', UnixFlagSpec.FlagType.EXIST, "use-we"),
	};

	protected final static ArgvParser argvParser = new ArgvParser(List.of(arguments));

	public FrameUnmap(PaperFramePlugin plugin) {
		this.plugin = plugin;
	}

	/**
	 * Remove all item frames hanging on the specific block and on the specific face.
	 *
	 * @param player the player to check
	 * @return number of item frames removed
	 */
	public static long removeFramesTargetedBlockFace(@NotNull Player player) {
		List<ItemFrame> filtered = Targeting.byAttachedBlockFace(player);
		filtered.forEach(Entity::remove);
		return filtered.size();
	}

	/**
	 * Remove all item frames by targeted entity - the frames might be in the air and not attached to a block.
	 *
	 * @param player the player to check
	 * @return number of item frames removed
	 */
	public static long removeFramesTargetedEntity(@NotNull Player player) {
		List<ItemFrame> filtered = byTargetedStackedEntity(player);
		filtered.forEach(Entity::remove);
		return filtered.size();
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String argv0, @NotNull String[] argv1p) {
		if (!(sender instanceof Player player)) {
			sender.sendMessage("This command can only be used by a player");
			return false;
		}

		HashMap<String, Object> parsed;
		try {
			parsed = argvParser.parse(List.of(argv1p));
		} catch (IllegalArgumentException e) {
			player.sendMessage(ChatColor.RED + e.getMessage());
			return false;
		}

		long removed = 0;
		if ((boolean) parsed.get("use-we")) {
			List<ItemFrame> targets = Targeting.byWorldEditCuboid(player);
			if (targets != null) {
				removed = targets.size();
				targets.forEach(Entity::remove);
			}
		} else {
			removed = removeFramesTargetedEntity(player);
		}

		player.sendMessage(String.format("Removed %d item frames", removed));
		return true;
	}
}

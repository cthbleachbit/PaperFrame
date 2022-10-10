package me.cth451.paperframe.util;

import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Targeting {
	/**
	 * Range to find any frames to show/hide
	 */
	public static final int SELECTION_RANGE = 6;

	public static ItemFrame findFrameByTargetedEntity(@NotNull Player player) {
		Entity entity = player.getTargetEntity(SELECTION_RANGE);
		if (entity == null) {
			return null;
		}

		if (!(entity instanceof ItemFrame frame)) {
			return null;
		}

		return frame;
	}
}

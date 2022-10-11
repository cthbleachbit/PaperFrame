package me.cth451.paperframe.util;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Date;

public class FrameProperties {
	public static PersistentDataType<Long, Date> TIMESTAMP_DATA_TYPE = new PersistentDataType<>() {
		@Override
		public @NotNull Class<Long> getPrimitiveType() {
			return Long.class;
		}

		@Override
		public @NotNull Class<Date> getComplexType() {
			return Date.class;
		}

		@Override
		public @NotNull Long toPrimitive(@NotNull Date complex, @NotNull PersistentDataAdapterContext context) {
			return complex.getTime();
		}

		@Override
		public @NotNull Date fromPrimitive(@NotNull Long primitive, @NotNull PersistentDataAdapterContext context) {
			return new Date(primitive);
		}
	};

	public static String NAMESPACE = "paperframe";
	public static NamespacedKey PROTECTED_BY = new NamespacedKey(NAMESPACE, "protected-by");
	public static NamespacedKey PROTECTED_AT = new NamespacedKey(NAMESPACE, "protected-at");

	/**
	 * Query the player that applied protection to the frame
	 *
	 * @param frame frame to query
	 * @return player name
	 */
	public static String getProtectedBy(ItemFrame frame) {
		if (frame.getPersistentDataContainer().has(PROTECTED_BY, PersistentDataType.STRING)) {
			return frame.getPersistentDataContainer().get(PROTECTED_BY, PersistentDataType.STRING);
		} else {
			return null;
		}
	}

	/**
	 * Record the player that applied protection to the frame, or if the player is null, remove recorded protector
	 *
	 * @param frame  frame to modify
	 * @param player acting player
	 */
	public static void setProtectedBy(@NotNull ItemFrame frame, Player player) {
		setProtectedBy(frame, player == null ? null : player.getName());
	}

	/**
	 * Record the player that applied protection to the frame, or if the name is null, remove recorded protector
	 *
	 * @param frame frame to modify
	 * @param name  name of the acting player
	 */
	public static void setProtectedBy(@NotNull ItemFrame frame, String name) {
		if (name != null) {
			frame.getPersistentDataContainer().set(PROTECTED_BY, PersistentDataType.STRING, name);
			;
		} else {
			frame.getPersistentDataContainer().remove(PROTECTED_BY);
		}
	}

	/**
	 * Query the timestamp of protect enable
	 *
	 * @param frame frame to query
	 * @return time when the frame is protected, or null if it is currently unprotected
	 */
	public static Date getProtectedAt(ItemFrame frame) {
		if (frame.getPersistentDataContainer().has(PROTECTED_AT, TIMESTAMP_DATA_TYPE)) {
			return frame.getPersistentDataContainer().get(PROTECTED_AT, TIMESTAMP_DATA_TYPE);
		} else {
			return null;
		}
	}

	/**
	 * Set the timestamp of protect enable
	 *
	 * @param frame frame to modify
	 * @param date  name
	 */
	public static void setProtectedAt(@NotNull ItemFrame frame, Date date) {
		if (date != null) {
			frame.getPersistentDataContainer().set(PROTECTED_AT, TIMESTAMP_DATA_TYPE, date);
			;
		} else {
			frame.getPersistentDataContainer().remove(PROTECTED_AT);
		}
	}
}

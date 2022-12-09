package me.cth451.paperframe.util;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.ItemFrame;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Date;

/**
 * Helper class for setting static properties (i.e. properties that players can set / get at will). All access to the
 * following properties should go through this class:
 * <ul>
 *     <li>protected status + acting player + timestamp</li>
 *     <li>hidden status + acting player + timestamp</li>
 * </ul>
 * Properties listed here are also checked from {@link FrameFilter} when specified from /framehighlight command.
 */
public class FrameProperties {
	/**
	 * An NBT data type adaptor that stores java.util.Date as a 64b signed integer
	 */
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
	public static NamespacedKey HIDDEN_BY = new NamespacedKey(NAMESPACE, "hidden-by");
	public static NamespacedKey HIDDEN_AT = new NamespacedKey(NAMESPACE, "hidden-at");

	/**
	 * Get value from namespace key by type
	 *
	 * @param frame item frame to query
	 * @param key   key to query
	 * @param type  nbt data type for this key
	 * @param <Z>   Java native type for the value - translated from NBT primitives as specified in the nbt data type
	 * @return stored value for this key or null if it doesn't exist
	 */
	private static <Z> Z get(@NotNull ItemFrame frame,
	                         @NotNull NamespacedKey key,
	                         @NotNull PersistentDataType<?, Z> type) {
		if (frame.getPersistentDataContainer().has(key, type)) {
			return frame.getPersistentDataContainer().get(key, type);
		} else {
			return null;
		}
	}

	/**
	 * Wrapper function to get protected status with minecraft native Fixed tag
	 *
	 * @param frame frame to check
	 * @return whether frame is protected
	 */
	public static boolean getProtected(@NotNull ItemFrame frame) {
		return frame.isFixed();
	}

	/**
	 * Wrapper function to set protected status with minecraft native Fixed tag
	 *
	 * @param frame   frame to check
	 * @param protect whether the frame should be protected
	 */
	public static void setProtected(@NotNull ItemFrame frame, boolean protect) {
		frame.setFixed(protect);
	}

	/**
	 * Wrapper function to get viability/hidden status with minecraft native Visible tag
	 *
	 * @param frame frame to check
	 * @return whether frame is hidden
	 */
	public static boolean getHidden(@NotNull ItemFrame frame) {
		return !frame.isVisible();
	}

	/**
	 * Wrapper function to set viability/hidden status with minecraft native Visible tag
	 *
	 * @param frame  frame to check
	 * @param hidden whether the frame should be visible
	 */
	public static void setHidden(@NotNull ItemFrame frame, boolean hidden) {
		frame.setVisible(!hidden);
	}

	/**
	 * Set/Remove value from namespace key by type. This is only used for custom keys.
	 *
	 * @param frame item frame to modify
	 * @param key   key to modify
	 * @param type  nbt data type for this key
	 * @param value value to write or null if the key is to be removed
	 * @param <Z>   Java native type for the value - translated into NBT primitives as specified in the nbt data type
	 */
	private static <Z> void set(@NotNull ItemFrame frame,
	                            @NotNull NamespacedKey key,
	                            @NotNull PersistentDataType<?, Z> type,
	                            Z value) {
		if (value == null) {
			frame.getPersistentDataContainer().remove(key);
		} else {
			frame.getPersistentDataContainer().set(key, type, value);
		}
	}

	private static String getString(@NotNull ItemFrame frame, @NotNull NamespacedKey key) {
		return get(frame, key, PersistentDataType.STRING);
	}

	private static void setString(@NotNull ItemFrame frame, @NotNull NamespacedKey key, String value) {
		set(frame, key, PersistentDataType.STRING, value);
	}

	private static Date getDate(@NotNull ItemFrame frame, @NotNull NamespacedKey key) {
		return get(frame, key, TIMESTAMP_DATA_TYPE);
	}

	private static void setDate(@NotNull ItemFrame frame, @NotNull NamespacedKey key, Date date) {
		set(frame, key, TIMESTAMP_DATA_TYPE, date);
	}

	public static String getProtectedBy(@NotNull ItemFrame frame) {
		return getString(frame, PROTECTED_BY);
	}

	public static void setProtectedBy(@NotNull ItemFrame frame, String name) {
		setString(frame, PROTECTED_BY, name);
	}

	public static Date getProtectedAt(ItemFrame frame) {
		return getDate(frame, PROTECTED_AT);
	}

	public static void setProtectedAt(@NotNull ItemFrame frame, Date date) {
		setDate(frame, PROTECTED_AT, date);
	}

	public static String getHiddenBy(@NotNull ItemFrame frame) {
		return getString(frame, HIDDEN_BY);
	}

	public static void setHiddenBy(@NotNull ItemFrame frame, String name) {
		setString(frame, HIDDEN_BY, name);
	}

	public static Date getHiddenAt(@NotNull ItemFrame frame) {
		return getDate(frame, HIDDEN_AT);
	}

	public static void setHiddenAt(@NotNull ItemFrame frame, Date date) {
		setDate(frame, HIDDEN_AT, date);
	}
}

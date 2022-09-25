package me.cth451.paperframe.util;

import me.cth451.paperframe.PaperFramePlugin;
import org.bukkit.*;
import org.bukkit.entity.ItemFrame;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.bukkit.Particle.DustOptions;

public class Drawing {

	/**
	 * Schedule repeating drawing routines for finite times
	 *
	 * @param plugin        instance of this plugin
	 * @param frame         item frame to draw
	 * @param options       redstone dust particle options
	 * @param repeatFor     number of occurences to draw
	 * @param intervalTicks interval between two draw events in ticks
	 */
	public static void scheduleStickyDraw(PaperFramePlugin plugin, ItemFrame frame, DustOptions options, int repeatFor, long intervalTicks) {
		Runnable draw = () -> {
			Drawing.drawBoundingBox(frame, options);
		};
		for (int i = 0; i < repeatFor; i++) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, draw, i * intervalTicks);
		}
	}

	/**
	 * Spawn particle along a straight line between two points
	 *
	 * @param p1      the starting point
	 * @param p2      the end point
	 * @param spacing spacing between two particles
	 * @param world   world to spawn particles in
	 * @param options redstone dust particle options
	 */
	public static void drawLine(Vector p1, Vector p2, double spacing, World world, DustOptions options) {
		double distance = p1.distance(p2);
		Vector vector = p2.clone().subtract(p1).normalize().multiply(spacing);
		double length = 0;
		for (; length < distance; p1.add(vector)) {
			world.spawnParticle(Particle.REDSTONE, p1.getX(), p1.getY(), p1.getZ(), 1, 0, 0, 0, 0, options);
			length += spacing;
		}
	}

	/**
	 * Draw bounding box of an item frame
	 *
	 * @param frame   item frame to draw
	 * @param options redstone dust particle options
	 */
	public static void drawBoundingBox(ItemFrame frame, DustOptions options) {
		BoundingBox box = frame.getBoundingBox().clone();
		World world = frame.getWorld();
		double mx = box.getMinX();
		double my = box.getMinY();
		double mz = box.getMinZ();
		double MX = box.getMaxX();
		double MY = box.getMaxY();
		double MZ = box.getMaxZ();

		double spacing = 0.5;
		drawLine(new Vector(mx, my, mz), new Vector(MX, my, mz), spacing, world, options);
		drawLine(new Vector(MX, my, mz), new Vector(MX, my, MZ), spacing, world, options);
		drawLine(new Vector(MX, my, MZ), new Vector(mx, my, MZ), spacing, world, options);
		drawLine(new Vector(mx, my, MZ), new Vector(mx, my, mz), spacing, world, options);
		drawLine(new Vector(mx, MY, mz), new Vector(MX, MY, mz), spacing, world, options);
		drawLine(new Vector(MX, MY, mz), new Vector(MX, MY, MZ), spacing, world, options);
		drawLine(new Vector(MX, MY, MZ), new Vector(mx, MY, MZ), spacing, world, options);
		drawLine(new Vector(mx, MY, MZ), new Vector(mx, MY, mz), spacing, world, options);
		drawLine(new Vector(mx, my, mz), new Vector(mx, MY, mz), spacing, world, options);
		drawLine(new Vector(MX, my, mz), new Vector(MX, MY, mz), spacing, world, options);
		drawLine(new Vector(MX, my, MZ), new Vector(MX, MY, MZ), spacing, world, options);
		drawLine(new Vector(mx, my, MZ), new Vector(mx, MY, MZ), spacing, world, options);
	}
}

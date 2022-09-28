package me.cth451.paperframe.util;

import me.cth451.paperframe.PaperFramePlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

public class Drawing {

	/**
	 * Schedule repeating drawing routines for finite times
	 *
	 * @param plugin        instance of this plugin
	 * @param drawCall      drawing call
	 * @param repeatFor     number of occurences to draw
	 * @param intervalTicks interval between two draw events in ticks
	 */
	public static void scheduleStickyDraw(PaperFramePlugin plugin, Runnable drawCall, int repeatFor, long intervalTicks) {
		for (int i = 0; i < repeatFor; i++) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, drawCall, i * intervalTicks);
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
	 * Draw bounding box of a entity
	 *
	 * @param entity  item frame to draw
	 * @param options redstone dust particle options
	 */
	public static void drawBoundingBox(Entity entity, DustOptions options) {
		drawBoundingBox(entity.getBoundingBox(), entity.getWorld(), options);
	}

	/**
	 * Draw wireframe outline of a block
	 *
	 * @param block   the block to draw
	 * @param options redstone dust particle options
	 */
	public static void drawBoundingBox(Block block, DustOptions options) {
		Location loc = block.getLocation();
		drawBoundingBox(
				new BoundingBox(loc.getX(), loc.getY(), loc.getZ(), loc.getX() + 1, loc.getY() + 1, loc.getZ() + 1),
				block.getWorld(), options);
	}

	/**
	 * Draw a given bounding box in the given world
	 *
	 * @param box     the bounding box to draw
	 * @param world   the world to draw in - note that {@link BoundingBox} does not have ref to World
	 * @param options redstone dust options
	 */
	public static void drawBoundingBox(BoundingBox box, World world, DustOptions options) {
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

package fr.lumin0u.survivor.utils;

import net.minecraft.world.level.block.DoubleBlockFinder.BlockType;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TransparentUtils
{
	private static List<TransparentUtils.BlockType> blockTypes;
	private static List<Material> emptyBlocks;
	
	public TransparentUtils()
	{
	}
	
	public static void setConfiguration(FileConfiguration t)
	{
		blockTypes = new ArrayList<>();
		emptyBlocks = new ArrayList<>();
		
		for(String type : t.getKeys(false))
		{
			if(t.contains(type + ".full") && !t.getBoolean(type + ".full"))
				for(Material mat : Material.values())
					if(mat.name().matches(type.replaceAll("\\+", ".*")) && mat.isBlock())
						emptyBlocks.add(mat);
			
			for(String data : t.getConfigurationSection(type).getKeys(false))
			{
				if(!data.startsWith("d"))
					continue;
				
				List<AABB> solid = cubsFrom(t.getString(type + "." + data));
				
				byte theData;
				if(data.replaceFirst("d", "").equals("*"))
					theData = -1;
				else
					theData = Byte.parseByte(data.replaceFirst("d", ""));
				
				for(Material mat : Material.values())
				{
					if(mat.name().matches(type.replaceAll("\\+", ".*")) && mat.isBlock())
					{
						if(theData == -1)
							for(byte i = Byte.MIN_VALUE; i < Byte.MAX_VALUE; i++)
								blockTypes.add(new BlockType(solid, mat, i));
						else
							blockTypes.add(new BlockType(solid, mat, theData));
					}
				}
			}
		}
	}
	
	private static Vector getLocation(String container, int firstIndex, boolean first)
	{
		return new Vector(getRealValue(getDoubleAt(container, firstIndex), !first), getRealValue(getDoubleAt(container, firstIndex + 1), !first), getRealValue(getDoubleAt(container, firstIndex + 2), !first));
	}
	
	private static double getRealValue(double d, boolean zeroEqualsTen)
	{
		return zeroEqualsTen && d == 0.0D ? 1.0D : d / 10.0D;
	}
	
	private static double getDoubleAt(String container, int index)
	{
		return (double) Integer.parseInt(String.valueOf(container.charAt(index)));
	}
	
	private static List<AABB> cubsFrom(String s)
	{
		List<AABB> list = new ArrayList<>();
		if(!s.isEmpty())
		{
			for(String st : s.split("-"))
			{
				list.add(new AABB(getLocation(st, 0, true), getLocation(st, 3, false)));
			}
		}
		
		return list;
	}
	
	@Deprecated
	public static List<AABB> solidAABBs(Block block)
	{
		return solidAABBs(block, false);
	}
	
	@Deprecated
	public static List<AABB> solidAABBs(Block block, boolean ignoreConfig)
	{
		TransparentUtils.BlockType blockType = (TransparentUtils.BlockType) blockTypes.stream().filter((bT) -> bT.matches(block)).findFirst().orElse(null);
		
		if(blockType != null && !ignoreConfig)
		{
			return blockType.solid.stream().map(aabb -> new AABB(block.getLocation().add(aabb.getLoc1()), block.getLocation().add(aabb.getLoc2()))).collect(Collectors.toList());
		}
		
		return block.getCollisionShape().getBoundingBoxes().stream().map(AABB::new).collect(Collectors.toList());
	}
	
	public static boolean isFullBlock(Material mat)
	{
		if(emptyBlocks == null)
		{
			System.err.println("Please set the config before using this method");
		}
		
		return !emptyBlocks.contains(mat) && !mat.isEmpty() && mat.isSolid();
	}
	
	public static RayTraceResult collisionBetween(Location l1, Location l2)
	{
		return collisionBetween(l1, l2, false);
	}
	
	public static RayTraceResult collisionBetween(Location l1, Location l2, boolean ignoreConfig)
	{
		if(!l1.getWorld().equals(l2.getWorld()))
			throw new IllegalArgumentException("l1 and l2 are not in the same world");
		
		l1 = l1.clone();
		l2 = l2.clone();
		Vector line = MCUtils.vectorFrom(l1, l2);
		
		RayTraceResult rayTrace = l1.getWorld().rayTraceBlocks(l1, line, l1.distance(l2), FluidCollisionMode.NEVER, true);
		
		while(rayTrace != null)
		{
			if(!emptyBlocks.contains(rayTrace.getHitBlock().getType()))
				return rayTrace;
			else
				l1 = rayTrace.getHitPosition().toLocation(l1.getWorld());
			
			rayTrace = l1.getWorld().rayTraceBlocks(l1, line, l1.distance(l2), FluidCollisionMode.NEVER, true);
		}
		
		return null;
		
		/*
		BlockIterator itr = new BlockIterator(l1.setDirection(line), 0.0D, (int) Math.ceil(line.length()));
		
		while(itr.hasNext())
		{
			Block block = (Block) itr.next();
			TransparentUtils.BlockType blockType = (TransparentUtils.BlockType) blockTypes.stream().filter((bT) -> bT.matches(block)).findFirst().orElse(null);
			
			if(blockType != null)
			{
				for(AABB c : blockType.solid.stream().map(aabb -> new AABB(block.getLocation().add(aabb.getLoc1()), block.getLocation().add(aabb.getLoc2()))).toList())
				{
					CollisionResult collisionResult = collision(c, l1.toVector(), l2.toVector());
					if(collisionResult.hasCollision())
					{
						return new CollisionResult(true, collisionResult.collisionPoint.toVector().toLocation(l1.getWorld()), block, collisionResult.collidedFace);
					}
				}
			}
		}
		
		return new TransparentUtils.CollisionResult(false, l2, (Block) null, (BlockFace) null);
		}*/
	}
	
	public static Location hitPointOrL2(Location l1, Location l2)
	{
		return hitPointOrL2(l1, l2, false);
	}
	
	public static Location hitPointOrL2(Location l1, Location l2, boolean ignoreConfig)
	{
		RayTraceResult result = collisionBetween(l1, l2, ignoreConfig);
		
		return result == null ? l2 : result.getHitPosition().toLocation(l1.getWorld());
	}
	
	public static boolean anySolidBetween(Location l1, Location l2)
	{
		return collisionBetween(l1, l2) != null;
	}
	
	public static double solidBetween(Location l1, Location l2)
	{
		RayTraceResult collisionResult = collisionBetween(l1, l2);
		return collisionResult == null ? 0.0D : collisionBetween(l1, l2).getHitPosition().distance(collisionBetween(l2, l1).getHitPosition());
	}
	
	public static TransparentUtils.CollisionResult collision(AABB c, Vector l1, Vector l2)
	{
		Vector line = MCUtils.vectorFrom(l1, l2);
		double planX = line.getX() > 0.0D ? c.xMin : c.xMax;
		double planY = line.getY() > 0.0D ? c.yMin : c.yMax;
		double planZ = line.getZ() > 0.0D ? c.zMin : c.zMax;
		Vector vx = new Vector(planX, line.getY() / line.getX() * (planX - l1.getX()) + l1.getY(), line.getZ() / line.getX() * (planX - l1.getX()) + l1.getZ());
		Vector vy = new Vector(line.getX() / line.getY() * (planY - l1.getY()) + l1.getX(), planY, line.getZ() / line.getY() * (planY - l1.getY()) + l1.getZ());
		Vector vz = new Vector(line.getX() / line.getZ() * (planZ - l1.getZ()) + l1.getX(), line.getY() / line.getZ() * (planZ - l1.getZ()) + l1.getY(), planZ);
		
		boolean containsVx = c.boundsContains(vx);
		boolean containsVy = c.boundsContains(vy);
		boolean containsVz = c.boundsContains(vz);
		
		if(!containsVx || containsVy && !(vx.distance(l1) < vy.distance(l1)) || containsVz && !(vx.distance(l1) < vz.distance(l1)))
		{
			if(!containsVy || containsVz && !(vy.distance(l1) < vz.distance(l1)))
			{
				return containsVz ? new TransparentUtils.CollisionResult(true, vz.toLocation(null), null, line.getZ() > 0.0D ? BlockFace.NORTH : BlockFace.SOUTH) : new TransparentUtils.CollisionResult(false, null, null, null);
			}
			else
			{
				return new TransparentUtils.CollisionResult(true, vy.toLocation(null), null, line.getY() > 0.0D ? BlockFace.DOWN : BlockFace.UP);
			}
		}
		else
		{
			return new TransparentUtils.CollisionResult(true, vx.toLocation(null), null, line.getX() > 0.0D ? BlockFace.WEST : BlockFace.EAST);
		}
	}
	
	private static class BlockType
	{
		private List<AABB> solid;
		private Material mat;
		private byte data;
		
		public BlockType(List<AABB> solid, Material mat, byte data)
		{
			this.solid = solid;
			this.mat = mat;
			this.data = data;
		}
		
		public boolean matches(Block b)
		{
			return b.getType().equals(this.mat) && b.getData() == this.data;
		}
		
		public String toString()
		{
			return "BlockType [solid=" + this.solid + ", mat=" + this.mat + ", data=" + this.data + "]";
		}
	}
	
	public static class CollisionResult
	{
		private final boolean hasCollision;
		private final Location collisionPoint;
		private final Block collidedBlock;
		private final BlockFace collidedFace;
		
		CollisionResult(boolean hasCollision, Location collisionPoint, Block collidedBlock, BlockFace collidedFace)
		{
			this.hasCollision = hasCollision;
			this.collisionPoint = collisionPoint;
			this.collidedBlock = collidedBlock;
			this.collidedFace = collidedFace;
		}
		
		public boolean hasCollision()
		{
			return this.hasCollision;
		}
		
		public Location getHitPosition()
		{
			return this.collisionPoint;
		}
		
		public Block getHitBlock()
		{
			return this.collidedBlock;
		}
		
		public BlockFace getHitBlockFace()
		{
			return this.collidedFace;
		}
	}
}

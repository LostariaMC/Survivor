package fr.lumin0u.survivor.utils;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AABB implements Cloneable
{
	private Vector loc1;
	private Vector loc2;
	private List<Block> inside;
	private boolean insideOK;
	
	public double xMin;
	public double xMax;
	
	public double yMin;
	public double yMax;
	
	public double zMin;
	public double zMax;
	
	public AABB(Entity ent)
	{
		this(ent.getBoundingBox());
	}
	
	public AABB(BoundingBox bb)
	{
		loc1 = bb.getMin();
		loc2 = bb.getMax();
		
		inside = new ArrayList<>();
		
		update();
	}
	
	public AABB(Location loc1, Location loc2)
	{
		this(loc1.toVector(), loc2.toVector());
	}
	
	public AABB(Vector loc1, Vector loc2)
	{
		this.loc1 = loc1.clone();
		this.loc2 = loc2.clone();
		
		inside = new ArrayList<>();
		
		update();
		
		this.loc1 = new Vector(xMin, yMin, zMin);
		this.loc2 = new Vector(xMax, yMax, zMax);
	}
	
	public Vector getLoc1()
	{
		return loc1;
	}
	
	public boolean isZero()
	{
		return loc1.equals(new Vector(0, 0, 0)) && loc2.equals(new Vector(0, 0, 0));
	}
	
	public static AABB zero()
	{
		return new AABB(new Vector(0, 0, 0), new Vector(0, 0, 0));
	}
	
	public AABB setLoc1(Vector loc1)
	{
		this.loc1 = loc1;
		
		update();
		
		return this;
	}
	
	public Vector getLoc2()
	{
		return loc2;
	}
	
	public AABB setLoc2(Vector loc2)
	{
		this.loc2 = loc2;
		
		update();
		
		return this;
	}
	
	public boolean contains(Location loc)
	{
		return contains(loc.toVector());
	}
	
	public boolean contains(Vector loc)
	{
		return (loc.getX() > xMin && loc.getX() < xMax && loc.getY() > yMin && loc.getY() < yMax && loc.getZ() > zMin && loc.getZ() < zMax);
	}
	
	public boolean boundsContains(Location loc)
	{
		return boundsContains(loc.toVector());
	}
	
	public boolean boundsContains(Vector loc)
	{
		return (loc.getX() >= xMin && loc.getX() <= xMax && loc.getY() >= yMin && loc.getY() <= yMax && loc.getZ() >= zMin && loc.getZ() <= zMax);
	}
	
	public boolean hasInside(Player p)
	{
		Vector loc = p.getLocation().toVector().clone().add(new Vector(0, 0.6, 0));
		
		return (loc.getX() > xMin && loc.getX() < xMax && loc.getY() > yMin && loc.getY() < yMax && loc.getZ() > zMin && loc.getZ() < zMax);
	}
	
	public List<Block> blocksInside(World w)
	{
		if(xMax - xMin < 50 && yMax - yMin < 50 && zMax - zMin < 50 && !insideOK)
		{
			for(int x = (int)xMin; x < xMax; x++)
			{
				for(int y = (int)yMin; y < yMax; y++)
				{
					for(int z = (int)zMin; z < zMax; z++)
					{
						inside.add(w.getBlockAt(x, y, z));
					}
				}
			}
		}
		
		insideOK = true;
		
		return inside;
	}
	
	private void update()
	{
		inside = new ArrayList<>();
		
		xMin = Math.min(loc1.getX(), loc2.getX());
		xMax = Math.max(loc1.getX(), loc2.getX());
		
		yMin = Math.min(loc1.getY(), loc2.getY());
		yMax = Math.max(loc1.getY(), loc2.getY());
		
		zMin = Math.min(loc1.getZ(), loc2.getZ());
		zMax = Math.max(loc1.getZ(), loc2.getZ());
		
		insideOK = false;
	}
	
	private void recalcLocs()
	{
		this.loc1 = new Vector(xMin, yMin, zMin);
		this.loc2 = new Vector(xMax, yMax, zMax);
	}
	
	public Vector midpoint()
	{
		return loc1.clone().add(MCUtils.vectorFrom(loc1, loc2).multiply(0.5));
	}
	
	public AABB multiply(double m)
	{
		loc1 = midpoint().add(MCUtils.vectorFrom(midpoint(), loc1).multiply(m));
		loc2 = midpoint().add(MCUtils.vectorFrom(midpoint(), loc2).multiply(m));
		
		update();
		
		return this;
	}
	
	/**
	 * @return la distance entre loc1 et loc2
	 */
	public double size()
	{
		return loc1.distance(loc2);
	}
	
	@Override
	public String toString()
	{
		return "Cuboid [loc1=" + loc1 + ", loc2=" + loc2 + "]";
	}
	
	public Vector rdLoc()
	{
		Random r = new Random();
		
		return new Vector(r.nextDouble()*(xMax-xMin)+xMin, r.nextDouble()*(yMax-yMin)+yMin, r.nextDouble()*(zMax-zMin)+zMin);
	}
	
	public Vector rdContourLoc()
	{
		Random r = new Random();
		
		int face = r.nextInt(3);
		boolean opposite = r.nextBoolean();
		
		if(face == 0)
			return new Vector(r.nextDouble()*(xMax-xMin)+xMin, r.nextDouble()*(yMax-yMin)+yMin, (opposite ? zMin : zMax));
		
		else if(face == 1)
			return new Vector(r.nextDouble()*(xMax-xMin)+xMin, (opposite ? yMin : yMax), r.nextDouble()*(zMax-zMin)+zMin);
		
		else
			return new Vector((opposite ? xMin : xMax), r.nextDouble()*(yMax-yMin)+yMin, r.nextDouble()*(zMax-zMin)+zMin);
	}
	
	public double distance(Vector loc)
	{
		return nearestPoint(loc).distance(loc);
	}
	
	public Vector nearestPoint(Vector loc)
	{
		if(contains(loc))
			return loc.clone();
		else
		{
			double x = Math.max(xMin, Math.min(xMax, loc.getX()));
			double y = Math.max(yMin, Math.min(yMax, loc.getY()));
			double z = Math.max(zMin, Math.min(zMax, loc.getZ()));
			
			return new Vector(x, y, z);
		}
	}
	
	public Vector nearestBoundsPoint(Vector loc)
	{
		if(contains(loc))
		{
			double dx = Math.min(loc.getX() - xMin, xMax - loc.getX());
			double dy = Math.min(loc.getY() - yMin, yMax - loc.getY());
			double dz = Math.min(loc.getZ() - zMin, zMax - loc.getZ());
			if(dx < dy && dx < dz)
			{
				return new Vector(loc.getX() - xMin < xMax - loc.getX() ? xMin : xMax, loc.getY(), loc.getZ());
			}
			else if(dy < dz)
			{
				return new Vector(loc.getX(), loc.getY() - yMin < yMax - loc.getY() ? yMin : yMax, loc.getZ());
			}
			return new Vector(loc.getX(), loc.getY(), loc.getZ() - zMin < zMax - loc.getZ() ? zMin : zMax);
		}
		return nearestPoint(loc);
	}
	
	public AABB add(Location l)
	{
		return add(l.toVector());
	}
	
	public AABB add(Vector l)
	{
		loc1.add(l);
		loc2.add(l);
		update();
		return this;
	}
	
	public AABB grow(double x, double y, double z)
	{
		xMin -= x;
		yMin -= y;
		zMin -= z;
		xMax += x;
		yMax += y;
		zMax += z;
		
		recalcLocs();
		return this;
	}
	
	public boolean collides(AABB other)
	{
		return collides(other.toBukkit());
	}
	
	public boolean collides(BoundingBox other)
	{
		return toBukkit().overlaps(other);
	}
	
	public BoundingBox toBukkit()
	{
		return new BoundingBox(xMin, yMin, zMin, xMax, yMax, zMax);
	}
	
	public static AABB cube(Location position)
	{
		return cube(position.toVector());
	}
	
	public static AABB cube(Vector position)
	{
		return new AABB(position, position.clone().add(new Vector(1, 1, 1)));
	}
	
	@Override
	public AABB clone()
	{
		return new AABB(loc1.clone(), loc2.clone());
	}
}

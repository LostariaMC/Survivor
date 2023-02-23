package fr.lumin0u.survivor.utils;

import fr.lumin0u.survivor.utils.TransparentUtils.CollisionResult;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.Random;

public class GravityRay extends Ray
{
	private double gravity;
	private boolean bounce;
	private double frictionLoss;
	
	public GravityRay(Location start, Vector increase, double length, double accuracy, double gravity)
	{
		super(start, increase, length, accuracy);
		this.gravity = gravity;
	}
	
	public GravityRay(Location start, Vector increase, double length, double accuracy, double gravity, boolean bounce, double frictionLoss)
	{
		this(start, increase, length, accuracy, gravity);
		this.setBounce(bounce, frictionLoss);
	}
	
	@Override
	public void calculate()
	{
		Location point = this.start.clone();
		
		World world = point.getWorld();
		
		Vector increase = this.increase;
		double m = increase.length() / increase.clone().normalize().length();
		Random r = new Random();
		increase.setX(increase.getX() + (double) (r.nextBoolean() ? -1 : 1) * r.nextDouble() * this.accuracy / 50.0D);
		increase.setY(increase.getY() + (double) (r.nextBoolean() ? -1 : 1) * r.nextDouble() * this.accuracy / 50.0D);
		increase.setZ(increase.getZ() + (double) (r.nextBoolean() ? -1 : 1) * r.nextDouble() * this.accuracy / 50.0D);
		increase.normalize().multiply(m);
		if(increase.length() > 0.0D)
		{
			int loops = (int) (this.length / increase.length());
			
			for(int i = 0; i < loops; ++i)
			{
				Location wantedEndPoint = point.clone().add(increase);
				increase.setY(increase.getY() - this.gravity / 1000.0D);
				this.points.add(point.clone());
				RayTraceResult collisionResult = TransparentUtils.collisionBetween(point, wantedEndPoint);
				if(collisionResult != null)
				{
					BlockFace face = collisionResult.getHitBlockFace();
					if(!this.bounce)
					{
						point = collisionResult.getHitPosition().toLocation(world);
						this.points.add(point);
						break;
					}
					
					if(!face.equals(BlockFace.DOWN) && !face.equals(BlockFace.UP))
					{
						if(!face.equals(BlockFace.EAST) && !face.equals(BlockFace.WEST))
						{
							if(face.equals(BlockFace.NORTH) || face.equals(BlockFace.SOUTH))
							{
								increase.setZ(-increase.getZ());
							}
						}
						else
						{
							increase.setX(-increase.getX());
						}
					}
					else
					{
						increase.setY(-increase.getY());
					}
					
					increase.multiply(this.frictionLoss);
					
					point = collisionResult.getHitPosition().toLocation(world);
					
					point.add((double) face.getModX() / 100.0D, (double) face.getModY() / 100.0D, (double) face.getModZ() / 100.0D);
				}
				else
				{
					point = wantedEndPoint.clone();
				}
			}
		}
		
		this.pointsOk = true;
	}
	
	public boolean isBounce()
	{
		return this.bounce;
	}
	
	public void setBounce(boolean bounce, double frictionLoss)
	{
		this.bounce = bounce;
		this.frictionLoss = 1.0D - frictionLoss;
		this.pointsOk = false;
	}
	
	public double getGravity()
	{
		return this.gravity;
	}
	
	public void setGravity(double gravity)
	{
		this.gravity = gravity;
		this.pointsOk = false;
	}
}

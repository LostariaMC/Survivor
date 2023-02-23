package fr.lumin0u.survivor.weapons;

import fr.lumin0u.survivor.GameManager;
import fr.lumin0u.survivor.Survivor;
import fr.lumin0u.survivor.mobs.mob.Enemy;
import fr.lumin0u.survivor.player.SvPlayer;
import fr.lumin0u.survivor.utils.MCUtils;
import fr.lumin0u.survivor.utils.Ray;
import fr.lumin0u.survivor.utils.TransparentUtils;
import fr.lumin0u.survivor.weapons.superweapons.Turret;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TurretRunnable extends BukkitRunnable
{
	private Block turret;
	private SvPlayer owner;
	private long time;
	private ArmorStand infoTime;
	private ArmorStand info;
	private boolean ready;
	private boolean isShooting;
	private long lifeTime;
	private Enemy target;
	public static List<TurretRunnable> runningInstances = new ArrayList<>();
	private Turret weapon;
	private boolean hasLivedWave;
	private boolean hasLivedEndWave;
	
	public TurretRunnable(Block turret, SvPlayer owner, Turret weapon)
	{
		this.turret = turret;
		this.owner = owner;
		this.weapon = weapon;
		this.lifeTime = 600L + 50L * (long) GameManager.getInstance().getWave();
		this.time = 0L;
		this.isShooting = false;
		this.info = MCUtils.oneConsistentFlyingText(turret.getLocation().add(0.5D, 1.4D, 0.5D), "§6Construction ...");
		this.ready = false;
		turret.setType(Material.BEACON);
	}
	
	@Override
	public void run()
	{
		if(!hasLivedWave && GameManager.getInstance().isInWave())
		{
			hasLivedWave = true;
		}
		if(hasLivedWave && !hasLivedEndWave && !GameManager.getInstance().isInWave())
		{
			hasLivedEndWave = true;
			time = Math.max(time, lifeTime - 500);
		}
		
		if(this.time > 100L && !this.ready)
		{
			this.ready = true;
			this.infoTime = MCUtils.oneConsistentFlyingText(this.turret.getLocation().add(0.5D, 1.2D, 0.5D), "§e" + (this.lifeTime - this.time) / 20L);
		}
		
		if(this.lifeTime - this.time <= 0L)
		{
			this.info.setCustomName("§c*tuuûut*");
			this.cancel();
		}
		else
		{
			if(this.ready)
			{
				this.infoTime.setCustomName("§e" + (this.lifeTime - this.time) / 20L);
				if(this.time % 6 == 0)
				{
					Location tur = this.turret.getLocation().add(0.5D, 1.0D, 0.5D);
					if(this.target == null || this.target.isDead() || TransparentUtils.solidBetween(this.target.getBodyHitbox().midpoint().toLocation(tur.getWorld()), tur) >= 0.1D)
					{
						this.target = null;
						Iterator var2 = GameManager.getInstance().getMobs().iterator();
						
						label55:
						while(true)
						{
							Enemy m;
							Location l;
							do
							{
								do
								{
									do
									{
										if(!var2.hasNext())
										{
											break label55;
										}
										
										m = (Enemy) var2.next();
									} while(m.isDead());
									
									l = m.getBodyHitbox().midpoint().toLocation(tur.getWorld());
								} while(!(l.distance(tur) < 20.0D));
							} while(this.target != null && !(l.distance(tur) < this.target.getBodyHitbox().midpoint().distance(tur.toVector())));
							
							if(!TransparentUtils.anySolidBetween(l, tur))
							{
								this.target = m;
							}
						}
					}
					
					if(this.target != null)
					{
						this.isShooting = true;
						Ray shoot = new Ray(tur, MCUtils.vectorFrom(tur.toVector(), this.target.getBodyHitbox().midpoint()).normalize().multiply(0.2D), 40.0D, 0.7D);
						MCUtils.playSound(this.turret.getLocation(), "guns.m240l", 20.0F);
						IGun.rawShoot(this.owner, weapon, shoot, 1.5D + 0.3D * (double) GameManager.getInstance().getWave());
					}
					else
					{
						this.isShooting = false;
					}
				}
				
				if(this.isShooting)
				{
					this.info.setCustomName("§c§k!!!!!");
				}
				else
				{
					this.info.setCustomName("§a-----");
				}
			}
			
			++this.time;
		}
	}
	
	@Override
	public synchronized void cancel()
	{
		runningInstances.remove(this);
		this.turret.setType(Material.AIR);
		super.cancel();
		this.info.remove();
		this.infoTime.remove();
	}
	
	public void start()
	{
		runningInstances.add(this);
		runTaskTimer(Survivor.getInstance(), 1, 1);
	}
}

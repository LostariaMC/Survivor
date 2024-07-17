package fr.lumin0u.survivor.weapons;

import fr.lumin0u.survivor.GameManager;
import fr.lumin0u.survivor.Survivor;
import fr.lumin0u.survivor.mobs.mob.Enemy;
import fr.lumin0u.survivor.player.SvPlayer;
import fr.lumin0u.survivor.utils.MCUtils;
import fr.lumin0u.survivor.utils.Ray;
import fr.lumin0u.survivor.utils.TFSound;
import fr.lumin0u.survivor.utils.TransparentUtils;
import fr.lumin0u.survivor.weapons.superweapons.Turret;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.TextDisplay;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static java.lang.Math.PI;

public class TurretRunnable extends BukkitRunnable
{
	private Block turret;
	private SvPlayer owner;
	private long time;
	private TextDisplay infoTime;
	private TextDisplay info;
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
		
		if(time > 100L && !this.ready)
		{
			ready = true;
			infoTime = MCUtils.oneConsistentFlyingText(turret.getLocation().add(0.5D, 1.2D, 0.5D), "§e" + (this.lifeTime - this.time) / 20L);
		}
		
		if(lifeTime - time <= 0L)
		{
			info.setText("§c*tuuûut*");
			cancel();
		}
		else
		{
			if(ready)
			{
				infoTime.setText("§e" + (lifeTime - time) / 20L);
				if(this.time % 6 == 0)
				{
					Location tur = turret.getLocation().add(0.5D, 1.0D, 0.5D);
					if(target == null || target.isDead() || TransparentUtils.solidBetween(target.getBodyHitbox().midpoint().toLocation(tur.getWorld()), tur) >= 0.1D)
					{
						target = null;
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
							} while(target != null && !(l.distance(tur) < target.getBodyHitbox().midpoint().distance(tur.toVector())));
							
							if(!TransparentUtils.anySolidBetween(l, tur))
							{
								target = m;
							}
						}
					}
					
					if(target != null)
					{
						isShooting = true;
						Ray shoot = new Ray(tur, MCUtils.vectorFrom(tur.toVector(), target.getBodyHitbox().midpoint()).normalize().multiply(0.2D), 40.0D, PI / 50);
						TFSound.GUN_SHOT.play(turret.getLocation());
						IGun.rawShoot(this.owner, weapon, shoot, 1.5D + 0.3D * (double) GameManager.getInstance().getWave());
					}
					else
					{
						this.isShooting = false;
					}
				}
				
				if(isShooting) {
					info.setText("§c§k!!!!!");
				}
				else {
					info.setText("§a-----");
				}
			}
			
			time++;
		}
	}
	
	@Override
	public synchronized void cancel()
	{
		runningInstances.remove(this);
		turret.setType(Material.AIR);
		super.cancel();
		info.remove();
		infoTime.remove();
	}
	
	public void start()
	{
		runningInstances.add(this);
		runTaskTimer(Survivor.getInstance(), 1, 1);
	}
}

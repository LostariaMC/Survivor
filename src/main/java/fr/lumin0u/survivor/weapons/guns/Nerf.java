package fr.lumin0u.survivor.weapons.guns;

import fr.lumin0u.survivor.GameManager;
import fr.lumin0u.survivor.StatsManager;
import fr.lumin0u.survivor.Survivor;
import fr.lumin0u.survivor.player.SvDamageable;
import fr.lumin0u.survivor.player.SvPlayer;
import fr.lumin0u.survivor.player.WeaponOwner;
import fr.lumin0u.survivor.utils.GravityRay;
import fr.lumin0u.survivor.weapons.Upgradeable;
import fr.lumin0u.survivor.weapons.WeaponType;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.scheduler.BukkitRunnable;

public class Nerf extends Gun implements Upgradeable
{
	public Nerf(final WeaponOwner owner)
	{
		super(owner, WeaponType.NERF);
	}
	
	@Override
	public int getNextLevelPrice()
	{
		return super.getNextLevelPrice();
	}
	
	@Override
	public void upgrade()
	{
		super.upgrade();
	}
	
	@Override
	public void shoot() {
		final GravityRay r = new GravityRay(owner.getShootLocation(), owner.getShootLocation().getDirection().multiply(0.2D), range, accuracy, 1.0D);
		r.setBounce(true, 0.75D);
		new BukkitRunnable()
		{
			int i = 0;
			int lastStop = 0;
			
			@Override
			public void run()
			{
				for(; (double) this.i < Math.min((double) r.getPoints().size(), 8.333333333333334D + (double) this.lastStop); i++)
				{
					Location point = (Location) r.getPoints().get(this.i);
					Location effectLoc = point.clone();
					if(this.i % 3 == 0)
					{
						point.getWorld().spawnParticle(Particle.REDSTONE, effectLoc, 0, new DustOptions(Color.fromRGB(75, 75, 200), 1));
					}
					
					for(SvDamageable ent : owner.getTargetType().getDamageables(GameManager.getInstance()))
					{
						if(ent.getBodyHitbox().contains(point) || ent.getHeadHitbox().contains(point))
						{
							if(owner instanceof SvPlayer)
							{
								StatsManager.increaseWeaponHits(Nerf.this);
							}
							
							ent.damage(dmg, owner, Nerf.this, ent.getHeadHitbox().contains(point), r.getIncrease().normalize().multiply(0.15D));
							this.cancel();
							return;
						}
					}
				}
				
				this.lastStop = this.i;
			}
		}.runTaskTimer(Survivor.getInstance(), 0L, 1L);
	}
}

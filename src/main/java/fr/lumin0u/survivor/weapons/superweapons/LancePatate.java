package fr.lumin0u.survivor.weapons.superweapons;

import fr.lumin0u.survivor.Survivor;
import fr.lumin0u.survivor.player.WeaponOwner;
import fr.lumin0u.survivor.utils.GravityRay;
import fr.lumin0u.survivor.utils.MCUtils;
import fr.lumin0u.survivor.utils.Ray;
import fr.lumin0u.survivor.weapons.WeaponType;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.scheduler.BukkitRunnable;

public class LancePatate extends SuperWeapon
{
	public LancePatate(WeaponOwner owner)
	{
		super(owner, WeaponType.LANCEPATATE);
	}
	
	@Override
	public ClickType getMainClickAction() {
		return ClickType.RIGHT;
	}
	
	@Override
	public void rightClick()
	{
		this.useAmmo();
		
		double range = 100.0D;
		double accuracy = 0.1D;
		final Ray r = new GravityRay(owner.getShootLocation(), owner.getShootLocation().getDirection().multiply(0.2D), range, accuracy, 50D);
		double ballSpeed = 20.0D;
		new BukkitRunnable()
		{
			int i = 0;
			int lastStop = 0;
			
			@Override
			public void run()
			{
				while((double) this.i < Math.min((double) r.getPoints().size(), 5.0D + (double) this.lastStop))
				{
					Location point = (Location) r.getPoints().get(this.i);
					point.getWorld().spawnParticle(Particle.REDSTONE, point, 0, new DustOptions(Color.fromRGB(227, 252, 1), 1));
					++this.i;
				}
				
				if(this.i >= r.getPoints().size() - 1)
				{
					MCUtils.explosion(owner, LancePatate.this, 20.0D, (Location) r.getPoints().get(r.getPoints().size() - 2), 20.0D, "guns.grenade", 0.5D, owner.getTargetType());
					this.cancel();
				}
				
				this.lastStop = this.i;
			}
		}.runTaskTimer(Survivor.getInstance(), 0L, 1L);
	}
	
	@Override
	public void leftClick()
	{
	}
}

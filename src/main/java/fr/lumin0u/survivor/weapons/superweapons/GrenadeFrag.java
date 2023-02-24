package fr.lumin0u.survivor.weapons.superweapons;

import fr.lumin0u.survivor.GameManager;
import fr.lumin0u.survivor.Survivor;
import fr.lumin0u.survivor.mobs.Waves;
import fr.lumin0u.survivor.player.WeaponOwner;
import fr.lumin0u.survivor.utils.MCUtils;
import fr.lumin0u.survivor.weapons.SupplyWeapon;
import fr.lumin0u.survivor.weapons.WeaponType;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Random;

public class GrenadeFrag extends AbstractGrenade implements SupplyWeapon
{
	public GrenadeFrag(final WeaponOwner owner)
	{
		super(owner, WeaponType.GRENADEFRAG, Material.SLIME_BALL, 50);
	}
	
	@Override
	public void explode(Location loc)
	{
		MCUtils.explosion(owner, this, 10.0D, loc, 2.0D, "guns.grenade", 1.0D, owner.getTargetType());
		
		for(int i = 0; i < 10; ++i)
		{
			double angle = (new Random()).nextDouble() * 2.0D * Math.PI;
			
			Vector vector = MCUtils.explosionVector(loc.clone().add(Math.sin(angle) * 0.5D, 0.3D, Math.cos(angle) * 0.5D), loc, 10.0D)
					.multiply(new Vector(1, 0.5, 1))
					.multiply(Math.random() * 7.0D * (Math.random() * 0.6 + 0.4));
			
			int explosionDelay = new Random().nextInt(10) + 50;
			
			new BukkitRunnable()
			{
				long time = 0L;
				Item projectile;
				
				@Override
				public void run()
				{
					++this.time;
					
					if(this.time == 1L)
					{
						ItemStack itemStack = new ItemStack(Material.CLAY_BALL);
						ItemMeta im = itemStack.getItemMeta();
						im.displayName(Component.text(Long.toHexString(System.nanoTime()) + "" + new Random().nextInt(100)));
						itemStack.setItemMeta(im);
						
						this.projectile = loc.getWorld().dropItem(loc.clone().add(0, 0.2, 0), itemStack);
						this.projectile.setPickupDelay(Integer.MAX_VALUE);
						this.projectile.setVelocity(vector);
					}
					
					if(this.time > explosionDelay)
					{
						GameManager gm = GameManager.getInstance();
						MCUtils.explosion(owner, GrenadeFrag.this, Waves.getEnnemiesLife(gm.getWave(), gm.getDifficulty()) * 0.35D, projectile.getLocation(), 5.0D, "guns.grenade", 1.0D, owner.getTargetType());
						this.projectile.remove();
						this.cancel();
					}
				}
			}.runTaskTimer(Survivor.getInstance(), 1L, 1L);
		}
	}
}

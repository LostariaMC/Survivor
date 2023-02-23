package fr.lumin0u.survivor.weapons.superweapons;

import fr.lumin0u.survivor.Survivor;
import fr.lumin0u.survivor.player.WeaponOwner;
import fr.lumin0u.survivor.weapons.WeaponType;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Random;

public abstract class AbstractGrenade extends SuperWeapon
{
	protected final Material launchedItem;
	protected final int explosionDelay;
	
	public AbstractGrenade(WeaponOwner owner, WeaponType wt, Material launchedItem, int explosionDelay) {
		super(owner, wt);
		this.launchedItem = launchedItem;
		this.explosionDelay = explosionDelay;
	}
	
	@Override
	public ClickType getMainClickAction() {
		return ClickType.RIGHT;
	}
	
	@Override
	public void rightClick() {
		useAmmo();
		launch(owner.getShootLocation(), owner.getShootLocation().getDirection());
	}
	
	@Override
	public void leftClick() {
	
	}
	
	public void launch(final Location loc, final Vector v)
	{
		new BukkitRunnable()
		{
			long time = 0L;
			Item projectile;
			
			@Override
			public void run()
			{
				++this.time;
				ItemStack itemStack;
				ItemMeta im;
				if(this.time == 1L)
				{
					itemStack = new ItemStack(launchedItem);
					im = itemStack.getItemMeta();
					im.displayName(Component.text(Long.toHexString(System.nanoTime()) + "" + new Random().nextInt(100)));
					itemStack.setItemMeta(im);
					
					this.projectile = loc.getWorld().dropItem(loc, itemStack);
					this.projectile.setPickupDelay(Integer.MAX_VALUE);
					this.projectile.setVelocity(v);
				}
				
				if(this.time > explosionDelay)
				{
					explode(this.projectile.getLocation());
					this.projectile.remove();
					this.cancel();
				}
			}
		}.runTaskTimer(Survivor.getInstance(), 1L, 1L);
	}
	
	public abstract void explode(Location location);
}

package fr.lumin0u.survivor.objects;

import fr.lumin0u.survivor.Difficulty;
import fr.lumin0u.survivor.GameManager;
import fr.lumin0u.survivor.Survivor;
import fr.lumin0u.survivor.mobs.mob.Enemy;
import fr.lumin0u.survivor.mobs.mob.boss.Boss;
import fr.lumin0u.survivor.player.SvPlayer;
import fr.lumin0u.survivor.utils.MCUtils;
import fr.lumin0u.survivor.weapons.Weapon;
import fr.lumin0u.survivor.weapons.WeaponType;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.type.Gate;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public enum Bonus
{
	NUKE("§4Nuke", Material.TNT),
	INSTANT_KILL("§cMort Instantanée", Material.DEAD_BUSH),
	MUNMAX("§9Munitions Max", Material.GLOWSTONE_DUST),
	CARPENTER("§6Charpentier", Material.OAK_FENCE),
	AIRSTRIKE("§dAirstrike", Material.REDSTONE);
	
	private final String name;
	private final Material mat;
	
	public static double probability(Difficulty difficulty)
	{
		return 0.008D;
	}
	
	private Bonus(String name, Material mat)
	{
		this.name = name;
		this.mat = mat;
	}
	
	public Material getMat()
	{
		return this.mat;
	}
	
	public Item spawn(Location loc)
	{
		final ArmorStand as = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
		final Item it = loc.getWorld().dropItem(loc, new ItemStack(this.mat));
		as.setVisible(false);
		as.setGravity(false);
		as.setCustomName(this.name);
		as.setCustomNameVisible(true);
		as.setPassenger(it);
		it.getItemStack().setAmount(64);
		it.setPickupDelay(2147483647);
		(new BukkitRunnable()
		{
			int i = 0;
			static final int imax = 1200;
			
			@Override
			public void run()
			{
				++this.i;
				
				for(SvPlayer p : GameManager.getInstance().getOnlinePlayers())
				{
					if(p.toBukkit().getGameMode().equals(GameMode.ADVENTURE) && p.toBukkit().getLocation().distance(it.getLocation()) < 2.5D)
					{
						Bonus.this.onPickup(p);
						this.i = 1200;
					}
				}
				
				if(this.i >= 1200)
				{
					it.remove();
					as.remove();
					this.cancel();
				}
				
			}
		}).runTaskTimer(Survivor.getInstance(), 1L, 1L);
		return it;
	}
	
	public void onPickup(final SvPlayer picker)
	{
		GameManager gm = GameManager.getInstance();
		if(!this.equals(AIRSTRIKE))
		{
			for(SvPlayer pl : gm.getOnlinePlayers())
			{
				MCUtils.sendTitle(pl.toBukkit(), 5, 40, 10, this.name);
			}
		}
		
		Iterator var6;
		Iterator var11;
		switch(this)
		{
			case NUKE -> {
				final List<Enemy> damagee = new ArrayList<>(gm.getMobs());
				new BukkitRunnable()
				{
					int distance = 2;
					
					@Override
					public void run()
					{
						if(damagee.isEmpty())
						{
							this.cancel();
						}
						
						for(Enemy enemy : new ArrayList<>(damagee))
						{
							if(!(enemy.getEntity().getLocation().distance(picker.toBukkit().getLocation()) > (double) this.distance))
							{
								if(!(enemy instanceof Boss))
								{
									enemy.kill((SvPlayer) null);
								}
								else
								{
									enemy.damage(enemy.getHealth() / 4.0D, (SvPlayer) null, null, false, new Vector(0, 0, 0));
								}
								
								damagee.remove(enemy);
							}
						}
						
						this.distance += 2;
					}
				}.runTaskTimer(Survivor.getInstance(), 0L, 1L);
				
				for(SvPlayer sp : gm.getPlayers()) {
					sp.addMoney(damagee.stream().mapToDouble(Enemy::getReward).sum() / 3);
				}
			}
			case INSTANT_KILL -> {
				for(SvPlayer sp : gm.getOnlinePlayers())
				{
					sp.startInstantKill();
				}
			}
			case CARPENTER -> {
				int count = 0;
				for(Room r : gm.getRooms()) {
					for(Location fence : r.getFences()) {
						if(fence.getBlock().getBlockData() instanceof Gate) {
							r.placeFence(fence);
							count++;
						}
					}
				}
				
				picker.addMoney((double) count * 0.2 * gm.getWave());
			}
			case MUNMAX -> {
				for(SvPlayer sp : gm.getPlayers())
				{
					for(Weapon w : sp.getWeapons())
					{
						w.setAmmo(w.getMaxAmmo());
					}
				}
			}
			case AIRSTRIKE -> WeaponType.AIRSTRIKE.giveNewWeapon(picker).giveItem();
		}
		
	}
	
	public static Bonus byItem(ItemStack it)
	{
		if(it != null)
		{
			Bonus[] var1 = values();
			int var2 = var1.length;
			
			for(Bonus b : var1)
			{
				if(b.getMat().equals(it.getType()))
				{
					return b;
				}
			}
			
		}
		return null;
	}
}

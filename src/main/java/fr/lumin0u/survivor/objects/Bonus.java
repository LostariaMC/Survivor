package fr.lumin0u.survivor.objects;

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
import java.util.List;
import java.util.Optional;

public enum Bonus
{
	NUKE("§4Nuke", Material.TNT, 0.001),
	INSTANT_KILL("§cMort Instantanée", Material.DEAD_BUSH, 0.002),
	MUNMAX("§9Munitions Max", Material.GLOWSTONE_DUST, 0.004),
	CARPENTER("§6Charpentier", Material.OAK_FENCE, 0.004),
	AIRSTRIKE("§dAirstrike", Material.REDSTONE, 0.003);
	
	private final String name;
	private final Material mat;
	private final double probaWeight;
	
	Bonus(String name, Material mat, double probaWeight) {
		this.name = name;
		this.mat = mat;
		this.probaWeight = probaWeight;
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
		as.addPassenger(it);
		it.getItemStack().setAmount(64);
		it.setPickupDelay(2147483647);
		(new BukkitRunnable()
		{
			int i = 0;
			static final int imax = 60 * 20;
			
			@Override
			public void run()
			{
				++this.i;
				
				for(SvPlayer p : GameManager.getInstance().getOnlinePlayers())
				{
					if(p.toBukkit().getGameMode().equals(GameMode.ADVENTURE) && p.toBukkit().getLocation().distance(it.getLocation()) < 2.5D)
					{
						Bonus.this.onPickup(p);
						i = imax;
					}
				}
				
				if(i >= imax)
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
		
		switch(this)
		{
			case NUKE -> {
				final List<Enemy> damagee = new ArrayList<>(gm.getMobs());
				
				double reward = damagee.stream().mapToDouble(Enemy::getReward).sum() / 2;
				reward /= gm.getOnlinePlayers().size();
				for(SvPlayer sp : gm.getOnlinePlayers()) {
					sp.addMoney(reward);
				}
				
				new BukkitRunnable()
				{
					int distance = 2;
					
					@Override
					public void run()
					{
						if(damagee.isEmpty() || distance > 100)
						{
							this.cancel();
							new ArrayList<>(damagee).forEach(enemy -> enemy.kill(null));
						}
						
						for(Enemy enemy : new ArrayList<>(damagee))
						{
							if(!(enemy.getEntity().getLocation().distanceSquared(picker.toBukkit().getLocation()) > distance*distance))
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
	
	public static Optional<Bonus> findWeightedRandom() {
		double random = Math.random();
		
		for(Bonus b : values()) {
			random -= b.probaWeight;
			if(random < 0) {
				return Optional.of(b);
			}
		}
		
		return Optional.empty();
	}
}

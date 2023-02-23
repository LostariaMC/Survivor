package fr.lumin0u.survivor.mobs.mob.boss;

import fr.lumin0u.survivor.GameManager;
import fr.lumin0u.survivor.Survivor;
import fr.lumin0u.survivor.player.SvPlayer;
import fr.lumin0u.survivor.mobs.mob.ZombieWithHead;
import fr.lumin0u.survivor.weapons.Weapon;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Random;

public abstract class Boss extends ZombieWithHead
{
	protected Runnable lifeTask;
	
	public Boss(Location spawnLoc, double maxHealth, double walkSpeed, String skullOwner)
	{
		super(spawnLoc, maxHealth, walkSpeed, skullOwner);
		
		(new BukkitRunnable()
		{
			public void run()
			{
				Zombie zomb = (Zombie) Boss.this.ent;
				if(!zomb.isDead() && !Boss.this.dead)
				{
					GameManager gm = GameManager.getInstance();
					if(zomb.isVillager())
					{
						zomb.getEquipment().setHelmet((ItemStack) null);
					}
					
					if(lifeTask != null)
						lifeTask.run();
				}
				else
				{
					this.cancel();
				}
			}
		}).runTaskTimer(Survivor.getInstance(), 2L, 1L);
	}
	
	public void damage(double dmg, SvPlayer damager, Weapon weapon, boolean headshot, Vector kb, double coinsMultiplier)
	{
		kb = kb.clone().multiply(0.1D);
		super.damage(dmg, damager, weapon, headshot, kb, coinsMultiplier);
	}
	
	public static Boss createRandom(Location spawnLoc, double maxHealth, double walkSpeed)
	{
		switch(new Random().nextInt(3))
		{
			case 0:
				return new CloneBoss(spawnLoc, maxHealth, walkSpeed);
			case 1:
				return new PoisonousBoss(spawnLoc, maxHealth, walkSpeed);
			case 2:
				return new CopyCatBoss(spawnLoc, maxHealth, walkSpeed);
			default:
				throw new RuntimeException("Incomplete switch case");
		}
	}
	
	@Override
	public void setFrozenArmor()
	{
		setArmor();
		this.ent.getEquipment().setChestplate(new ItemStack(Material.CHAINMAIL_CHESTPLATE));
	}
}

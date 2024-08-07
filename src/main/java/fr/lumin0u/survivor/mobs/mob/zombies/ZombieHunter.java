package fr.lumin0u.survivor.mobs.mob.zombies;

import fr.lumin0u.survivor.Survivor;
import fr.lumin0u.survivor.mobs.mob.EnemyWeaponAI;
import fr.lumin0u.survivor.weapons.guns.ZombieHuntingGun;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class ZombieHunter extends Zombie
{
	public ZombieHunter(Location spawnLoc, double maxHealth, double walkSpeed)
	{
		super(ZombieType.HUNTER, spawnLoc, maxHealth, walkSpeed);
		
		giveWeapon(new ZombieHuntingGun(this));
		ai = new EnemyWeaponAI(weapon, true);
		
		(new BukkitRunnable()
		{
			Vector increase;
			
			@Override
			public void run()
			{
				org.bukkit.entity.Zombie zomb = (org.bukkit.entity.Zombie) ZombieHunter.this.ent;
				if(ZombieHunter.this.dead)
				{
					this.cancel();
				}
				
				if(!zomb.isDead() && ZombieHunter.this.target != null)
				{
					ai.run();
				}
			}
		}).runTaskTimer(Survivor.getInstance(), 2L, 1L);
	}
	
	@Override
	public void setArmor()
	{
		super.setArmor();
		this.ent.getEquipment().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
		this.ent.getEquipment().setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
		this.ent.getEquipment().setBoots(new ItemStack(Material.LEATHER_BOOTS));
	}
}

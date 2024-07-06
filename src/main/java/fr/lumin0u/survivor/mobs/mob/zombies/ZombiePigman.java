package fr.lumin0u.survivor.mobs.mob.zombies;

import fr.lumin0u.survivor.utils.TFSound;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;

public class ZombiePigman extends Zombie
{
	public ZombiePigman(Location spawnLoc, double maxHealth, double walkSpeed) {
		super(ZombieType.ZOMBIE_PIGMAN, spawnLoc, maxHealth, walkSpeed, TFSound.simple(Sound.ENTITY_ZOMBIFIED_PIGLIN_HURT), TFSound.simple(Sound.ENTITY_ZOMBIFIED_PIGLIN_DEATH));
	}
	
	@Override
	public int getReward() {
		return super.getReward() * 3;
	}
	
	@Override
	public void setArmor() {
		super.setArmor();
		ent.getEquipment().setItemInMainHand(new ItemStack(Material.GOLDEN_SWORD));
	}
}

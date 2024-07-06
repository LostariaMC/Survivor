package fr.lumin0u.survivor.mobs.mob.boss;

import fr.lumin0u.survivor.mobs.mob.zombies.ZombieType;
import fr.lumin0u.survivor.mobs.mob.zombies.ZombieWithHead;
import fr.lumin0u.survivor.utils.ItemBuilder;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;

public class PoisonousBoss extends ZombieWithHead implements Boss
{
	public PoisonousBoss(Location spawnLoc, double maxHealth, double walkSpeed)
	{
		super(ZombieType.NORMAL, spawnLoc, maxHealth, walkSpeed * 1.2, "Poisonous");
	}
	
	@Override
	public void setArmor()
	{
		super.setArmor();
		this.ent.getEquipment().setChestplate(new ItemBuilder(Material.LEATHER_CHESTPLATE).setLeatherColor(Color.OLIVE).build());
		this.ent.getEquipment().setLeggings(new ItemBuilder(Material.LEATHER_LEGGINGS).setLeatherColor(Color.OLIVE).build());
		this.ent.getEquipment().setBoots(new ItemBuilder(Material.LEATHER_BOOTS).setLeatherColor(Color.OLIVE).build());
	}
}

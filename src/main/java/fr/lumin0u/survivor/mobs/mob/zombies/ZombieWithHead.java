package fr.lumin0u.survivor.mobs.mob.zombies;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class ZombieWithHead extends Zombie
{
	private String skullOwner;
	
	public ZombieWithHead(ZombieType type, Location spawnLoc, double maxHealth, double walkSpeed, String skullOwner)
	{
		super(type, spawnLoc, maxHealth, walkSpeed);
		this.skullOwner = skullOwner;
	}
	
	@Override
	public void setArmor()
	{
		super.setArmor();
		putHead(skullOwner);
	}
	
	public void putHead(String skullOwner)
    {
        org.bukkit.entity.Zombie zomb = (org.bukkit.entity.Zombie) this.ent;
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if(skullOwner != null)
        	meta.setOwner(skullOwner);
        head.setItemMeta(meta);
        zomb.getEquipment().setHelmet(head);
    }
}

package fr.lumin0u.survivor.mobs.mob.boss;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class CloneBoss extends Boss
{
	private long reload = System.currentTimeMillis();
	
	public CloneBoss(Location spawnLoc, double maxHealth, double walkSpeed)
	{
		super(spawnLoc, maxHealth, walkSpeed * 0.9, "Bobely");
		lifeTask = this::tick;
	}
	
	@Override
	public void setArmor()
	{
		super.setArmor();
		this.ent.getEquipment().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
		this.ent.getEquipment().setLeggings(new ItemStack(Material.IRON_LEGGINGS));
		this.ent.getEquipment().setBoots(new ItemStack(Material.IRON_BOOTS));
		this.ent.getEquipment().setItemInHand(new ItemStack(Material.BONE));
	}
	
	private void tick()
	{
		if((double) (System.currentTimeMillis() - this.reload) > (double) 8000)
		{
			this.reload = System.currentTimeMillis();
			
			double wave = (double) gm.getWave();
			
			for(int i = 0; i < wave / 2; ++i)
			{
				getGroup().update();
				if(getGroup().canAddErpriexZombie())
				{
					CloneZombie clone = new CloneZombie(getEntity().getLocation(), wave / 2.0D, getWalkSpeed() * 1.2);
					clone.setReward(0);
					getGroup().getZombies().add(clone);
				}
			}
		}
	}
}

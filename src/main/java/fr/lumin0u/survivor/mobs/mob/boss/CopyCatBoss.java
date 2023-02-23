package fr.lumin0u.survivor.mobs.mob.boss;

import fr.lumin0u.survivor.player.SvPlayer;
import fr.lumin0u.survivor.mobs.mob.ZombieWeaponAI;
import fr.lumin0u.survivor.weapons.Weapon;
import fr.lumin0u.survivor.weapons.guns.Gun;
import fr.lumin0u.survivor.weapons.knives.Knife;
import org.bukkit.Location;

import java.util.Objects;
import java.util.Optional;

public class CopyCatBoss extends Boss
{
	private long reload = System.currentTimeMillis();
	private SvPlayer lastTarget;
	
	public CopyCatBoss(Location spawnLoc, double maxHealth, double walkSpeed)
	{
		super(spawnLoc, maxHealth, walkSpeed, "");
		
		lifeTask = this::tick;
	}
	
	public void tick()
	{
		if(target == lastTarget && target != null)
		{
			ai.run();
		}
		else
		{
			if(target != null)
			{
				Optional<Weapon> weapon;
				if(target.getWeaponInHand() instanceof Gun)
					weapon = Optional.of(target.getWeaponInHand());
				else
				{
					weapon = target.getWeapons().stream()
							.filter(Objects::nonNull)
							.filter(w -> !(w instanceof Knife)).findAny();
				}
				if(weapon.isPresent())
				{
					Weapon w = weapon.get().getType().getNewWeapon(this);
					w.giveItem();
					ai = new ZombieWeaponAI(w, true);
				}
			}
			setArmor();
		}
		lastTarget = target;
	}
	
	@Override
	public void setArmor()
	{
		super.setArmor();
		//		this.ent.getEquipment().setChestplate(new ItemBuilder(Material.LEATHER_CHESTPLATE).leatherColor(Color.OLIVE).build());
		//		this.ent.getEquipment().setLeggings(new ItemBuilder(Material.LEATHER_LEGGINGS).leatherColor(Color.OLIVE).build());
		//		this.ent.getEquipment().setBoots(new ItemBuilder(Material.LEATHER_BOOTS).leatherColor(Color.OLIVE).build());
	}
	
	@Override
	public void putHead(String ignore)
	{
		super.putHead(target == null ? null : target.getName());
	}
	
	@Override
	public boolean hasHead()//so can't lose it
	{
		return false;
	}
}

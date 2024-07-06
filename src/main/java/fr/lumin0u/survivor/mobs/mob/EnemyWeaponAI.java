package fr.lumin0u.survivor.mobs.mob;

import fr.lumin0u.survivor.utils.MCUtils;
import fr.lumin0u.survivor.utils.TransparentUtils;
import fr.lumin0u.survivor.weapons.RepeatingType;
import fr.lumin0u.survivor.weapons.Weapon;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EnemyWeaponAI implements Runnable
{
	private Weapon weapon;
	private int reload;
	private int delay;
	private boolean infiniteAmmo;
	private List<Location> locationsBuffer = new ArrayList<>();
	private Enemy mob;
	
	public EnemyWeaponAI(Weapon weapon, boolean infiniteAmmo)
	{
		this.weapon = weapon;
		this.infiniteAmmo = infiniteAmmo;
		reload = (int) (weapon.getType().getRepeatingType() == RepeatingType.AUTOMATIC ? weapon.getType().getRpm() : weapon.getType().getRpm() + new Random().nextInt(30));
		this.mob = (Enemy) weapon.getOwner();
	}
	
	@Override
	public void run()
	{
		boolean targetFound = mob.getTarget() != null
				&& isInSight(mob.getEntity(), mob.getTarget().toBukkit().getEyeLocation())
				&& isInFOV(mob.getEntity(), mob.getTarget().toBukkit().getEyeLocation());
		
		if(targetFound)
			locationsBuffer.add(mob.getTarget().getBodyHitbox().midpoint().toLocation(mob.getEntity().getWorld()));
		
		if(reload <= 0 && weapon.isUseable() && weapon.aiHelp_MayShoot(mob, mob.getTarget()))
		{
			if(targetFound)
			{
				if(delay == -1)
				{
					delay = new Random().nextInt(20);
				}
				else if(delay == 0)
				{
					if(infiniteAmmo)
						weapon.setAmmo(weapon.getClipSize());
					
					weapon.click(weapon.getMainClickAction());
					
					reload = weapon.getType().getRpm() + new Random().nextInt(weapon.getType().getRpm() * (weapon.getType().getRepeatingType() == RepeatingType.AUTOMATIC ? 2 : 5));
				}
				else
				{
					delay--;
				}
			}
			else
				delay = -1;
		}
		reload--;
		
		if(!locationsBuffer.isEmpty() && (!targetFound || locationsBuffer.size() >= 25))
			locationsBuffer.remove(0);
	}
	
	public Vector getEntDirection()
	{
		return locationsBuffer.isEmpty() ? mob.getEntity().getLocation().getDirection() : MCUtils.vectorFrom(mob.getEntity().getEyeLocation(), locationsBuffer.get(Math.max(0, new Random().nextInt(locationsBuffer.size()))));
	}
	
	private static boolean isInSight(LivingEntity ent, Location targetEyes)
	{
		return !TransparentUtils.anySolidBetween(ent.getEyeLocation(), targetEyes);
	}
	
	private static boolean isInFOV(LivingEntity ent, Location targetEyes)
	{
		return ent.getLocation().getDirection().angle(MCUtils.vectorFrom(ent.getEyeLocation(), targetEyes)) < Math.PI / 2;
	}
}

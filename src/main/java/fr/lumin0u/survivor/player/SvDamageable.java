package fr.lumin0u.survivor.player;

import fr.lumin0u.survivor.utils.AABB;
import fr.lumin0u.survivor.weapons.Weapon;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public interface SvDamageable
{
	public AABB getHeadHitbox();
	
	public AABB getBodyHitbox();
	
	public Location getFeets();
	
	public void damage(double dmg, WeaponOwner damager, Weapon weapon, boolean headshot, Vector kb);
	
	public void setFireTime(long fireTime, WeaponOwner fireMan, Weapon weapon);
	
	public void setFrozenTime(long frozenTime);
	
	public boolean isAlive();
}

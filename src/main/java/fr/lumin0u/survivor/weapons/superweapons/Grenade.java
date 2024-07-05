package fr.lumin0u.survivor.weapons.superweapons;

import fr.lumin0u.survivor.GameManager;
import fr.lumin0u.survivor.player.WeaponOwner;
import fr.lumin0u.survivor.utils.MCUtils;
import fr.lumin0u.survivor.weapons.SupplyWeapon;
import fr.lumin0u.survivor.weapons.WeaponType;
import org.bukkit.Location;
import org.bukkit.Material;

public class Grenade extends AbstractGrenade implements SupplyWeapon
{
	public Grenade(WeaponOwner owner)
	{
		super(owner, WeaponType.GRENADE, Material.CLAY_BALL, 70);
	}
	
	@Override
	public void explode(Location loc)
	{
		GameManager gm = GameManager.getInstance();
		MCUtils.explosion(owner, this, gm.getApproxEnnemyHealth() * 0.85D, loc, 11.0D, 5.0D, owner.getTargetType());
	}
}

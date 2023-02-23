package fr.lumin0u.survivor.weapons.superweapons;

import fr.lumin0u.survivor.GameManager;
import fr.lumin0u.survivor.mobs.Waves;
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
		MCUtils.explosion(owner, this, Waves.getEnnemiesLife(gm.getWave(), gm.getDifficulty()) * 1.05D, loc, 10.0D, "guns.grenade", 1.0D, owner.getTargetType());
	}
}

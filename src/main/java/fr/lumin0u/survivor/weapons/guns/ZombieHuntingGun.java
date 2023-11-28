package fr.lumin0u.survivor.weapons.guns;

import fr.lumin0u.survivor.player.SvDamageable;
import fr.lumin0u.survivor.player.SvPlayer;
import fr.lumin0u.survivor.player.WeaponOwner;
import fr.lumin0u.survivor.weapons.WeaponType;
import fr.lumin0u.survivor.weapons.guns.Gun;

public class ZombieHuntingGun extends Gun
{
	public ZombieHuntingGun(WeaponOwner owner)
	{
		super(owner, WeaponType.HUNTING_GUN);
	}
	
	@Override
	public double getDamageMultiplier(SvDamageable victim)
	{
		if(victim instanceof SvPlayer)
			return 0.7 / getDmg();
		else
			return super.getDamageMultiplier(victim);
	}
}

package fr.lumin0u.survivor.weapons.guns.rifles;

import fr.lumin0u.survivor.player.WeaponOwner;
import fr.lumin0u.survivor.weapons.WeaponType;
import fr.lumin0u.survivor.weapons.guns.Gun;

public class MPLLF extends Gun
{
	public MPLLF(WeaponOwner owner)
	{
		super(owner, WeaponType.MPLLF);
	}
	
	@Override
	public int getAmmoBoxRecovery()
	{
		return 2 * clipSize;
	}
}

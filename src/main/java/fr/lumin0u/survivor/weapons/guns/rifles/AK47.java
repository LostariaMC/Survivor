package fr.lumin0u.survivor.weapons.guns.rifles;

import fr.lumin0u.survivor.player.WeaponOwner;
import fr.lumin0u.survivor.weapons.Upgradeable;
import fr.lumin0u.survivor.weapons.WeaponType;
import fr.lumin0u.survivor.weapons.guns.Gun;

public class AK47 extends Gun implements Upgradeable
{
	public AK47(WeaponOwner owner)
	{
		super(owner, WeaponType.AK47);
	}
	
	@Override
	public int getNextLevelPrice()
	{
		return super.getNextLevelPrice();
	}
	
	@Override
	public void upgrade()
	{
		super.upgrade();
	}
}

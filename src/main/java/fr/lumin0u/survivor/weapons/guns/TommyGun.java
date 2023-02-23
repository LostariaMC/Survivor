package fr.lumin0u.survivor.weapons.guns;

import fr.lumin0u.survivor.player.WeaponOwner;
import fr.lumin0u.survivor.weapons.Upgradeable;
import fr.lumin0u.survivor.weapons.WeaponType;

public class TommyGun extends Gun implements Upgradeable
{
	public TommyGun(WeaponOwner owner)
	{
		super(owner, WeaponType.TOMMY_GUN);
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

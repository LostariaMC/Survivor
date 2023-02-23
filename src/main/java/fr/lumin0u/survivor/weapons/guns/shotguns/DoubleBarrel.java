package fr.lumin0u.survivor.weapons.guns.shotguns;

import fr.lumin0u.survivor.player.WeaponOwner;
import fr.lumin0u.survivor.weapons.Upgradeable;
import fr.lumin0u.survivor.weapons.WeaponType;

public class DoubleBarrel extends Shotgun implements Upgradeable
{
	public DoubleBarrel(WeaponOwner owner)
	{
		super(owner, WeaponType.DOUBLE_BARREL);
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

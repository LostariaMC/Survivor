package fr.lumin0u.survivor.weapons.guns.shotguns;

import fr.lumin0u.survivor.player.WeaponOwner;
import fr.lumin0u.survivor.weapons.Upgradeable;
import fr.lumin0u.survivor.weapons.WeaponType;

public class MCS extends Shotgun implements Upgradeable
{
	public MCS(WeaponOwner owner)
	{
		super(owner, WeaponType.MCS);
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

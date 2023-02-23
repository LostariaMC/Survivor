package fr.lumin0u.survivor.weapons.knives;

import fr.lumin0u.survivor.player.WeaponOwner;
import fr.lumin0u.survivor.weapons.Upgradeable;
import fr.lumin0u.survivor.weapons.WeaponType;

public class BucherKnife extends Knife implements Upgradeable
{
	public BucherKnife(WeaponOwner owner)
	{
		super(owner, WeaponType.BUCHER_KNIFE);
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

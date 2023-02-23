package fr.lumin0u.survivor.weapons.guns;

import fr.lumin0u.survivor.player.WeaponOwner;
import fr.lumin0u.survivor.weapons.Upgradeable;
import fr.lumin0u.survivor.weapons.WeaponType;

public class Revolver extends Gun implements Upgradeable
{
	public Revolver(WeaponOwner owner)
	{
		super(owner, WeaponType.REVOLVER);
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

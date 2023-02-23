package fr.lumin0u.survivor.weapons.guns.rifles;

import fr.lumin0u.survivor.player.WeaponOwner;
import fr.lumin0u.survivor.weapons.Upgradeable;
import fr.lumin0u.survivor.weapons.WeaponType;
import fr.lumin0u.survivor.weapons.guns.Gun;

public class Famas extends Gun implements Upgradeable
{
	public Famas(WeaponOwner owner)
	{
		super(owner, WeaponType.FAMAS);
	}
	
	@Override
	public void upgrade()
	{
		super.upgrade();
	}
	
	@Override
	public int getNextLevelPrice()
	{
		return super.getNextLevelPrice();
	}
}

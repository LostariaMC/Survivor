package fr.lumin0u.survivor.weapons.guns.shotguns;

import fr.lumin0u.survivor.player.WeaponOwner;
import fr.lumin0u.survivor.weapons.Upgradeable;
import fr.lumin0u.survivor.weapons.WeaponType;

public class SPAS12 extends Shotgun implements Upgradeable
{
	public SPAS12(WeaponOwner owner) {
		super(owner, WeaponType.SPAS12);
	}
	
	public int getNextLevelPrice() {
		return super.getNextLevelPrice();
	}

	public void upgrade() {
		super.upgrade();
	}
}

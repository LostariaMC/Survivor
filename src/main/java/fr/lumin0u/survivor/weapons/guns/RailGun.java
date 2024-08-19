package fr.lumin0u.survivor.weapons.guns;

import fr.lumin0u.survivor.player.WeaponOwner;
import fr.lumin0u.survivor.utils.TFSound;
import fr.lumin0u.survivor.weapons.IRailGun;
import fr.lumin0u.survivor.weapons.Upgradeable;
import fr.lumin0u.survivor.weapons.WeaponType;
import org.bukkit.Color;

public class RailGun extends Gun implements Upgradeable, IRailGun
{
	public RailGun(WeaponOwner owner)
	{
		super(owner, WeaponType.RAILGUN, TFSound.RAILGUN_SHOT);
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
	
	@Override
	public int getAmmoBoxRecovery()
	{
		return 2 * getClipSize();
	}
	
	@Override
	public Color getRailColor() {
		return Color.fromRGB(1, 200, 0);
	}
	
	@Override
	public void shoot() {
		IRailGun.super.shoot();
	}
}

package fr.lumin0u.survivor.weapons.guns.shotguns;

import fr.lumin0u.survivor.player.WeaponOwner;
import fr.lumin0u.survivor.utils.TFSound;
import fr.lumin0u.survivor.weapons.WeaponType;
import fr.lumin0u.survivor.weapons.guns.Gun;

public abstract class Shotgun extends Gun
{
	private int shots;
	
	public Shotgun(WeaponOwner owner, WeaponType wt)
	{
		super(owner, wt, TFSound.SHOTGUN_SHOT);
		this.shots = wt.get("shots");
	}
	
	@Override
	public void upgrade()
	{
		super.upgrade();
		this.accuracy = this.wt.get("accuracy");
		this.shots = (int) ((double) (Integer) this.wt.get("shots") + Math.pow(1.07D, (double) this.level));
	}
	
	public int getShots()
	{
		return this.shots;
	}
	
	@Override
	public double dmgPerTick()
	{
		return this.dmg * (double) this.clipSize * (double) this.shots / (double) (26 * this.clipSize + this.reloadTime);
	}
	
	@Override
	public int getAmmoBoxRecovery()
	{
		return Math.max(2 * getClipSize(), 4);
	}
	
	@Override
	public void shoot() {
		for(int i = 0; i < shots; ++i) {
			super.shoot();
		}
	}
}

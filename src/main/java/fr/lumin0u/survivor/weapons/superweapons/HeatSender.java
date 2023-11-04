package fr.lumin0u.survivor.weapons.superweapons;

import fr.lumin0u.survivor.player.WeaponOwner;
import fr.lumin0u.survivor.utils.TFSound;
import fr.lumin0u.survivor.weapons.WeaponType;

public abstract class HeatSender extends SuperGun
{
	protected long rpm;
	
	public HeatSender(WeaponOwner owner, WeaponType wt, TFSound sound)
	{
		super(owner, wt, sound);
		this.rpm = wt.getRpm();
	}
	
	@Override
	public abstract void shoot();
}

package fr.lumin0u.survivor.weapons.superweapons;

import fr.lumin0u.survivor.player.WeaponOwner;
import fr.lumin0u.survivor.weapons.WeaponType;

public abstract class HeatSender extends Gun
{
	protected long rpm;
	
	public HeatSender(WeaponOwner owner, WeaponType wt)
	{
		super(owner, wt);
		this.rpm = wt.getRpm();
	}
	
	@Override
	public abstract void shoot();
}

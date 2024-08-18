package fr.lumin0u.survivor.weapons.guns.snipers;

import fr.lumin0u.survivor.player.SvDamageable;
import fr.lumin0u.survivor.player.WeaponOwner;
import fr.lumin0u.survivor.weapons.Upgradeable;
import fr.lumin0u.survivor.weapons.WeaponType;

public class MosinSniper extends Sniper implements Upgradeable
{
	public MosinSniper(WeaponOwner owner)
	{
		super(owner, WeaponType.MOSIN);
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
        return 4 + getClipSize();
    }
	
	@Override
	public double getDamageMultiplier(SvDamageable victim)
	{
		return super.getDamageMultiplier(victim) * 0.3;
	}
}

package fr.lumin0u.survivor.weapons.guns.snipers;

import fr.lumin0u.survivor.player.SvDamageable;
import fr.lumin0u.survivor.player.WeaponOwner;
import fr.lumin0u.survivor.utils.Ray;
import fr.lumin0u.survivor.weapons.IGun;
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
        return 5;
    }
	
	@Override
	public double getDamageMultiplier(SvDamageable victim)
	{
		return super.getDamageMultiplier(victim) * 0.3;
	}
	
	@Override
	public void shoot() {
		
		WeaponOwner shooter = getOwner();
		Ray ray = new Ray(shooter.getShootLocation(), shooter.getShootLocation().getDirection().multiply(0.2D), getRange(), getAccuracy());
		
		IGun.rawShoot(shooter, this, ray, getDmg());
	}
}

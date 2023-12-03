package fr.lumin0u.survivor.weapons.guns.snipers;

import fr.lumin0u.survivor.player.WeaponOwner;
import fr.lumin0u.survivor.weapons.Upgradeable;
import fr.lumin0u.survivor.weapons.WeaponType;

public class Dragunov extends Sniper implements Upgradeable
{
	public Dragunov(WeaponOwner owner)
	{
		super(owner, WeaponType.DRAGUNOV);
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
}

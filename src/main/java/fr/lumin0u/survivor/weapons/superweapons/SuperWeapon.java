package fr.lumin0u.survivor.weapons.superweapons;

import fr.lumin0u.survivor.player.WeaponOwner;
import fr.lumin0u.survivor.weapons.Weapon;
import fr.lumin0u.survivor.weapons.WeaponType;
import org.bukkit.enchantments.Enchantment;

public abstract class SuperWeapon extends Weapon
{
	public SuperWeapon(WeaponOwner owner, WeaponType wt)
	{
		super(owner, wt);
	}
	
	@Override
	public String getActionBar()
	{
		return "§d" + this.wt.getName() + " §6" + this.clip;
	}
	
	@Override
	public void useAmmo()
	{
		super.useAmmo();
		if(ammo + clip <= 0)
			owner.removeWeapon(this);
	}
	
	@Override
	protected void upgrade()
	{
		super.upgrade();
		this.item.removeEnchantment(Enchantment.DAMAGE_ALL);
	}
}

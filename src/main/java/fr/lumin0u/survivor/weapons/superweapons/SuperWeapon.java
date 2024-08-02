package fr.lumin0u.survivor.weapons.superweapons;

import fr.lumin0u.survivor.player.WeaponOwner;
import fr.lumin0u.survivor.weapons.Weapon;
import fr.lumin0u.survivor.weapons.WeaponType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

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
		
		owner.refreshWeaponItem(this);
		
		if(ammo + clip <= 0)
			owner.removeWeapon(this);
	}
	
	@Override
	public ItemStack buildItem() {
		item.setAmount(ammo + clip);
		
		return super.buildItem();
	}
	
	@Override
	protected void upgrade()
	{
		super.upgrade();
		this.item.removeEnchantment(Enchantment.DAMAGE_ALL);
	}
}

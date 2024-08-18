package fr.lumin0u.survivor.weapons.guns.snipers;

import fr.lumin0u.survivor.player.SvPlayer;
import fr.lumin0u.survivor.player.WeaponOwner;
import fr.lumin0u.survivor.utils.Ray;
import fr.lumin0u.survivor.weapons.IGun;
import fr.lumin0u.survivor.weapons.WeaponType;
import fr.lumin0u.survivor.weapons.guns.Gun;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public abstract class Sniper extends Gun
{
	protected boolean scopeActive = false;
	
	public Sniper(WeaponOwner owner, WeaponType wt)
	{
		super(owner, wt);
	}
	
	@Override
	public void leftClick()
	{
		if(!this.scopeActive && owner instanceof SvPlayer)
			scope();
		else if(owner instanceof SvPlayer)
			unScope();
	}
	
	private void scope()
	{
		this.scopeActive = true;
		((SvPlayer) owner).toBukkit().getInventory().setHelmet(new ItemStack(Material.PUMPKIN));
		((SvPlayer) owner).toBukkit().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 99999, 10, false, false));
	}
	
	public void unScope()
	{
		if(scopeActive && owner instanceof SvPlayer sp) {
			scopeActive = false;
			sp.toBukkit().getInventory().setHelmet(new ItemStack(Material.AIR));
			sp.toBukkit().removePotionEffect(PotionEffectType.SLOW);
		}
	}
	
	@Override
	public double getAccuracy()
	{
		return super.getAccuracy() * (double) (this.scopeActive ? 1 : 2);
	}
	
	@Override
	public void shoot() {
		Ray ray = new Ray(owner.getShootLocation(), owner.getShootLocation().getDirection().multiply(0.2D), getRange(), getAccuracy());
		
		IGun.rawShoot(owner, this, ray, getDmg(), true);
	}
}

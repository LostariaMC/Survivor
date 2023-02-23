package fr.lumin0u.survivor.weapons.superweapons;

import fr.lumin0u.survivor.Survivor;
import fr.lumin0u.survivor.player.WeaponOwner;
import fr.lumin0u.survivor.utils.MCUtils;
import fr.lumin0u.survivor.weapons.IGun;
import fr.lumin0u.survivor.weapons.WeaponType;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.ClickType;

import java.util.List;

public abstract class Gun extends SuperWeapon implements IGun
{
	protected double dmg;
	protected double range;
	protected double accuracy;
	
	public Gun(WeaponOwner owner, WeaponType wt)
	{
		super(owner, wt);
		this.dmg = wt.get("dmg");
		this.range = wt.get("range");
		this.accuracy = wt.get("accuracy");
	}
	
	@Override
	public void rightClick()
	{
		if(!isUseable())
			return;
		
		MCUtils.playSound(this.owner.getShootLocation(), this.wt.getSound());
		shoot();
		this.useAmmo();
		
		if(this.owner.hasDoubleCoup())
			Bukkit.getScheduler().runTaskLater(Survivor.getInstance(), this::shoot, 1L);
	}
	
	@Override
	public ClickType getMainClickAction() {
		return ClickType.RIGHT;
	}
	
	@Override
	public void leftClick()
	{
	}
	
	@Override
	public List<String> getLore()
	{
		List<String> lore = super.getLore();
		lore.add("§6Dégats par balle : §a" + this.dmg);
		return lore;
	}
	
	@Override
	public double getDmg()
	{
		return this.dmg;
	}
	
	@Override
	public double getRange()
	{
		return this.range;
	}
	
	@Override
	public double getAccuracy()
	{
		return this.accuracy;
	}
}

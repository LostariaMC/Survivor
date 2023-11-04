package fr.lumin0u.survivor.weapons.superweapons;

import fr.lumin0u.survivor.Survivor;
import fr.lumin0u.survivor.player.WeaponOwner;
import fr.lumin0u.survivor.utils.TFSound;
import fr.lumin0u.survivor.weapons.IGun;
import fr.lumin0u.survivor.weapons.WeaponType;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.ClickType;

import java.util.List;

public abstract class SuperGun extends SuperWeapon implements IGun
{
	protected final TFSound sound;
	protected double dmg;
	protected double range;
	protected double accuracy;
	
	public SuperGun(WeaponOwner owner, WeaponType wt) {
		this(owner, wt, TFSound.GUN_SHOT);
	}
	
	public SuperGun(WeaponOwner owner, WeaponType wt, TFSound sound) {
		super(owner, wt);
		this.dmg = wt.get("dmg");
		this.range = wt.get("range");
		this.accuracy = wt.get("accuracy");
		this.sound = sound;
	}
	
	@Override
	public void rightClick()
	{
		if(!isUseable())
			return;
		
		sound.play(owner.getShootLocation());
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

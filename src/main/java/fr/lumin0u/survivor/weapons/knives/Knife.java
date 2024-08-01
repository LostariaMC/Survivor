package fr.lumin0u.survivor.weapons.knives;

import fr.lumin0u.survivor.Survivor;
import fr.lumin0u.survivor.player.SvDamageable;
import fr.lumin0u.survivor.player.SvPlayer;
import fr.lumin0u.survivor.player.WeaponOwner;
import fr.lumin0u.survivor.utils.TFSound;
import fr.lumin0u.survivor.weapons.Weapon;
import fr.lumin0u.survivor.weapons.WeaponType;
import org.bukkit.event.inventory.ClickType;

import java.util.ArrayList;
import java.util.List;

public abstract class Knife extends Weapon
{
	private int lastLClick;
	protected int rpm;
	protected double dmg;
	
	public Knife(WeaponOwner owner, WeaponType wt)
	{
		super(owner, wt);
		this.rpm = wt.getRpm();
		this.dmg = wt.get("dmg");
		this.lastLClick = 0;
	}
	
	@Override
	public ClickType getMainClickAction() {
		return ClickType.UNKNOWN;
	}
	
	@Override
	public void rightClick()
	{
	}
	
	@Override
	public void leftClick()
	{
	
	}
	
	public void onHit(SvDamageable mob) {
		if(owner instanceof SvPlayer && Survivor.getCurrentTick() - this.lastLClick > this.rpm)
		{
			TFSound.MELEE_MISS.playTo((SvPlayer) owner);
			showCooldown(rpm);
			mob.damage(dmg, owner, this, false, ((SvPlayer) owner).toBukkit().getLocation().getDirection().multiply(0.1));
			
			lastLClick = Survivor.getCurrentTick();
		}
	}
	
	public double dmgPerTick()
	{
		return this.dmg * ((double) this.rpm / 60.0D) / 20.0D;
	}
	
	@Override
	public List<String> getLore()
	{
		List<String> lore = new ArrayList<>();
		lore.add("§6Dégats : §a" + String.format("%.2f", dmg) + (!isUpgradeable() ? "" : " §8➝ " + String.format("%.2f", getDamageAtLevel(level + 1))));
		if(isUpgradeable())
			lore.add("§6Niveau : §a" + level + " §8➝ " + (level + 1));
		return lore;
	}
	
	@Override
	public String getActionBar()
	{
		return "§9" + this.wt.getName();
	}
	
	public double getDmg()
	{
		return this.dmg;
	}
	
	@Override
	public void upgrade()
	{
		super.upgrade();
		this.dmg = (double) this.wt.get("dmg") * Math.pow(1.2D, (double) this.level);
	}
	
	protected double getDamageAtLevel(int level) {
		return (double) wt.get("dmg") * Math.pow(1.14D, level);
	}
}

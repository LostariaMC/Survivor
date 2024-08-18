package fr.lumin0u.survivor.weapons.guns;

import fr.lumin0u.survivor.GameManager;
import fr.lumin0u.survivor.Survivor;
import fr.lumin0u.survivor.SurvivorGame;
import fr.lumin0u.survivor.player.SvPlayer;
import fr.lumin0u.survivor.player.WeaponOwner;
import fr.lumin0u.survivor.utils.TFSound;
import fr.lumin0u.survivor.weapons.IGun;
import fr.lumin0u.survivor.weapons.RepeatingType;
import fr.lumin0u.survivor.weapons.Weapon;
import fr.lumin0u.survivor.weapons.WeaponType;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.event.inventory.ClickType;

import java.util.List;

public abstract class Gun extends Weapon implements IGun
{
	protected final TFSound sound;
	protected double dmg;
	protected double range;
	protected double accuracy;
	
	public Gun(WeaponOwner owner, WeaponType wt) {
		this(owner, wt, TFSound.GUN_SHOT);
	}
	
	public Gun(WeaponOwner owner, WeaponType wt, TFSound sound) {
		super(owner, wt);
		this.dmg = wt.get("dmg");
		this.range = wt.get("range");
		this.accuracy = wt.get("accuracy");
		this.sound = sound;
	}
	
	@Override
	public ClickType getMainClickAction() {
		return ClickType.RIGHT;
	}
	
	@Override
	public void rightClick() {
		if(!isUseable())
			return;
		
		handleTrigger(false);
		
		if(this.owner.hasDoubleCoup())
			Bukkit.getScheduler().runTaskLater(Survivor.getInstance(), () -> handleTrigger(true), 1L);
	}
	
	private void handleTrigger(boolean freeAmmo) {
		shoot();
		
		if(!freeAmmo) {
			sound.play(owner.getShootLocation());
			this.useAmmo();
		}
		
		if(wt.getRepeatingType() == RepeatingType.BURSTS)
		{
			for(int i = 1; i < (int) wt.get("shots"); i++)
			{
				Bukkit.getScheduler().runTaskLater(Survivor.getInstance(), () -> {
					shoot();
					if(!freeAmmo) {
						sound.play(owner.getShootLocation());
						this.useAmmo();
					}
				}, i * (long) wt.get("shotsDelay"));
			}
		}
	}
	
	@Override
    public void shoot() {
		int nbZombies = GameManager.getInstance().getRemainingEnnemies();
		IGun.super.shoot();
		if(owner instanceof SvPlayer player && nbZombies - GameManager.getInstance().getRemainingEnnemies() >= 3) {
			player.toCosmox().addAchievementEarned(SurvivorGame.TRIPLE_KILL_ACHIEVEMENT);
		}
	}
	
	@Override
	public void leftClick() {
	}
	
	public double dmgPerTick() {
		return this.dmg * (double) this.clipSize / (double) (5 * this.clipSize + this.reloadTime);
	}
	
	@Override
	public List<String> getLore() {
		List<String> lore = super.getLore();
		lore.add("§6Dégats par balle : §a" + String.format("%.2f", this.dmg) + (!isUpgradeable() ? "" : " §8➝ " + String.format("%.2f", getDamageAtLevel(level + 1))));
		lore.add("§6Range : §a" + String.format("%.2f", this.range) + (!isUpgradeable() ? "" : " §8➝ " + String.format("%.2f", getRangeAtLevel(level + 1))));
		
		lore.add("§6Délai entre 2 tirs : §a" + String.format("%.2f", (float) this.wt.getRpm() / 20) + "s");
		return lore;
	}
	
	protected double getDamageAtLevel(int level) {
		return (double) wt.get("dmg") * Math.pow(1.14D, level);
	}
	
	protected double getRangeAtLevel(int level) {
		return (double) this.wt.get("range") * Math.pow(1.003D, level);
	}
	
	protected double getAccuracyAtLevel(int level) {
		return (double) this.wt.get("accuracy") * Math.pow(0.92D, level);
	}
	
	@Override
	protected void upgrade() {
		super.upgrade();
		this.dmg = getDamageAtLevel(level);
		this.range = getRangeAtLevel(level);
		this.accuracy = getAccuracyAtLevel(level);
	}
	
	@Override
	public double getDmg() {
		return this.dmg;
	}
	
	@Override
	public double getRange() {
		return this.range;
	}
	
	@Override
	public double getAccuracy() {
		return this.accuracy;
	}
	
	public Color getRayColor() {
		return Color.fromRGB(75, 75, 75);
	}
}

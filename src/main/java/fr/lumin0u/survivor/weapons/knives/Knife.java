package fr.lumin0u.survivor.weapons.knives;

import fr.lumin0u.survivor.GameManager;
import fr.lumin0u.survivor.StatsManager;
import fr.lumin0u.survivor.Survivor;
import fr.lumin0u.survivor.player.SvDamageable;
import fr.lumin0u.survivor.player.SvPlayer;
import fr.lumin0u.survivor.player.WeaponOwner;
import fr.lumin0u.survivor.utils.MCUtils;
import fr.lumin0u.survivor.utils.Ray;
import fr.lumin0u.survivor.weapons.Weapon;
import fr.lumin0u.survivor.weapons.WeaponType;
import org.bukkit.Location;
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
		if(owner instanceof SvPlayer)
			((SvPlayer) owner).setKnife(this);
	}
	
	@Override
	public ClickType getMainClickAction() {
		return ClickType.LEFT;
	}
	
	@Override
	public void rightClick()
	{
	}
	
	@Override
	public void leftClick()
	{
		if(Survivor.getCurrentTick() - this.lastLClick > this.rpm)
		{
			if(owner instanceof SvPlayer)
			{
				MCUtils.playSound(((SvPlayer) owner).getPlayer().getLocation(), this.wt.getSound());
				((SvPlayer) owner).getPlayer().setCooldown(getType().getMaterial(), rpm);
			}
			this.lastLClick = Survivor.getCurrentTick();
			
			Ray r = new Ray(owner.getShootLocation(), owner.getShootLocation().getDirection().multiply(0.5D), 3.0D, 0.0D);
			
			for(Location point : r.getPoints())
			{
				for(SvDamageable m : owner.getTargetType().getDamageables(GameManager.getInstance()))
				{
					if(m.getBodyHitbox().contains(point) || m.getHeadHitbox().multiply(1.3D).contains(point))
					{
						if(owner instanceof SvPlayer)
							StatsManager.increaseWeaponHits(this);
						m.damage(dmg, owner, this, m.getHeadHitbox().contains(point), r.getIncrease().normalize().multiply(0.1D));
						return;
					}
				}
			}
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
		lore.add("§6Dégats : §a" + this.dmg);
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
	
	@Override
	public void giveItem()
	{
		super.giveItem();
	}
}

package fr.lumin0u.survivor.mobs.mob;

import fr.lumin0u.survivor.GameManager;
import fr.lumin0u.survivor.player.SvDamageable;
import fr.lumin0u.survivor.player.SvPlayer;
import fr.lumin0u.survivor.player.WeaponOwner;
import fr.lumin0u.survivor.utils.MCUtils;
import fr.lumin0u.survivor.utils.Ray;
import fr.lumin0u.survivor.weapons.WeaponType;
import fr.lumin0u.survivor.weapons.guns.Gun;
import org.bukkit.Location;

public class GrapplingHook extends Gun
{
	private static final double DISTANCE = 5.0D;
	
	public GrapplingHook(WeaponOwner owner)
	{
		super(owner, WeaponType.GRAPPLING_HOOK);
	}
	
	@Override
	public void useAmmo()
	{
	
	}
	
	@Override
	public boolean aiHelp_MayShot(Enemy mob, SvPlayer target)
	{
		return mob.getEntity().getEyeLocation().distance(target.getShootLocation()) < DISTANCE;
	}
	
	@Override
	public double getDamageMultiplier(SvDamageable victim)
	{
		if(victim instanceof SvPlayer)
			return 0;
		else
			return super.getDamageMultiplier(victim);
	}
	
	@Override
	public void shoot() {
		Ray r = new Ray(owner.getShootLocation(), owner.getShootLocation().getDirection().multiply(0.5D), DISTANCE, 0.0D);
		
		for(Location point : r.getPoints())
		{
			for(SvDamageable m : owner.getTargetType().getDamageables(GameManager.getInstance()))
			{
				if(m.getBodyHitbox().contains(point) || m.getHeadHitbox().contains(point))
				{
					m.damage(dmg, owner, this, false, MCUtils.vectorFrom(m.getFeets(), owner.getFeets()).normalize().multiply(0.4));
					return;
				}
			}
		}
	}
}

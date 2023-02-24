package fr.lumin0u.survivor.weapons.superweapons;

import fr.lumin0u.survivor.GameManager;
import fr.lumin0u.survivor.player.SvDamageable;
import fr.lumin0u.survivor.player.WeaponOwner;
import fr.lumin0u.survivor.utils.MCUtils;
import fr.lumin0u.survivor.utils.TransparentUtils;
import fr.lumin0u.survivor.weapons.SupplyWeapon;
import fr.lumin0u.survivor.weapons.WeaponType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;

public class GrenadeFlame extends AbstractGrenade implements SupplyWeapon
{
	public GrenadeFlame(WeaponOwner owner)
	{
		super(owner, WeaponType.GRENADEFLAME, Material.MAGMA_CREAM, 70);
	}
	
	@Override
	public void explode(Location loc)
	{
		MCUtils.explosionParticles(loc, 10.0F, 500, Particle.FLAME);
		MCUtils.explosion(owner, this, 4.0D, loc, 3.0D, "guns.grenade", 0.0D, owner.getTargetType());
		
		GameManager gm = GameManager.getInstance();
		for(SvDamageable m : owner.getTargetType().getDamageables(gm))
		{
			if(m.getFeets().distance(loc) < 20.0D && TransparentUtils.solidBetween(m.getFeets(), loc) < 3)
			{
				m.setFireTime((long) (450 - (int) (17.0D * m.getFeets().distance(loc))), owner, this);
			}
		}
		
	}
}

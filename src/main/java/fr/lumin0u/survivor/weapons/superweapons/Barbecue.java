package fr.lumin0u.survivor.weapons.superweapons;

import fr.lumin0u.survivor.GameManager;
import fr.lumin0u.survivor.player.SvDamageable;
import fr.lumin0u.survivor.player.WeaponOwner;
import fr.lumin0u.survivor.utils.Ray;
import fr.lumin0u.survivor.utils.TFSound;
import fr.lumin0u.survivor.weapons.WeaponType;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Barbecue extends HeatSender
{
	public Barbecue(WeaponOwner owner)
	{
		super(owner, WeaponType.BARBECUE, TFSound.FLAME_THROWER_SHOT);
	}
	
	@Override
	public void shoot() {
		
		Particle particle = (Particle) getType().get("particle");
		Location loc = owner.getShootLocation();
		Random ra = new Random();
		
		for(int i = 0; i < 100; ++i)
		{
			Vector v = loc.getDirection().multiply(range * ra.nextDouble() * 0.1D);
			
			loc.getWorld().spawnParticle(particle, loc.clone().add(loc.getDirection()), 0, (float) v.getX(), (float) v.getY(), (float) v.getZ());
		}
		
		Ray r = new Ray(loc.clone(), loc.getDirection().multiply(0.5D), range / 1.5D, 0.0D);
		List<SvDamageable> hit = new ArrayList<>();
		
		for(int i = 0; i < r.getPoints().size(); ++i)
		{
			Location point = r.getPoints().get(i);
			
			for(SvDamageable ent : owner.getTargetType().getDamageables(GameManager.getInstance()))
			{
				if((ent.getBodyHitbox().multiply((double) i / 2.5).contains(point) || ent.getHeadHitbox().multiply((double) i / 2.5).contains(point)) && !hit.contains(ent))
				{
					ent.setFireTime(160L, owner, this);
					hit.add(ent);
				}
			}
		}
	}
}

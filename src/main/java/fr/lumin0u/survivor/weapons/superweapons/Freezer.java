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

public class Freezer extends HeatSender
{
	public Freezer(WeaponOwner owner)
	{
		super(owner, WeaponType.FREEZER, TFSound.FREEZER_SHOT);
	}
	
	@Override
	public void shoot() {
		Particle particle = (Particle) getType().get("particle");
		Location loc = owner.getShootLocation();
		Random ra = new Random();
		
		for(int i = 0; i < 50; ++i)
		{
			Vector v = loc.getDirection().multiply(range * ra.nextDouble() * 0.1D);
			
			loc.getWorld().spawnParticle(particle, loc.clone().add(loc.getDirection()), 0, (float) v.getX(), (float) v.getY(), (float) v.getZ());
		}
		
		Ray r = new Ray(loc.clone(), loc.getDirection().multiply(0.5D), range / 1.5D, 0.0D);
		List<SvDamageable> hit = new ArrayList<>();
		
		for(int i = 0; i < r.getPoints().size(); ++i)
		{
			Location point = r.getPoints().get(i);
			
			GameManager gm = GameManager.getInstance();
			
			for(SvDamageable ent : owner.getTargetType().getDamageables(gm))
			{
				if((ent.getBodyHitbox().multiply((double) i / 1.5).contains(point) || ent.getHeadHitbox().multiply((double) i / 1.5).contains(point)) && !hit.contains(ent))
				{
					ent.damage(gm.getBaseEnnemyHealth() / 10, owner, this, false, null);
					ent.setFrozenTime(200L);
					hit.add(ent);
				}
			}
		}
	}
}

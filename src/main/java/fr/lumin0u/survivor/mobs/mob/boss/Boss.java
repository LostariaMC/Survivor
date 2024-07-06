package fr.lumin0u.survivor.mobs.mob.boss;

import fr.lumin0u.survivor.mobs.mob.Enemy;
import fr.lumin0u.survivor.player.SvDamageable;
import org.bukkit.Location;

import java.util.Random;

public interface Boss extends SvDamageable
{
	public static Enemy createRandom(Location spawnLoc, double maxHealth, double walkSpeed)
	{
		return switch(new Random().nextInt(3)) {
			case 0 ->
					new IllusionerBoss(spawnLoc, maxHealth, walkSpeed);
			case 1 ->
					new BlazeBoss(spawnLoc, maxHealth, walkSpeed);
			case 2 ->
					new PoisonousBoss(spawnLoc, maxHealth, walkSpeed);
			default ->
					throw new RuntimeException("Incomplete switch case");
		};
	}
}

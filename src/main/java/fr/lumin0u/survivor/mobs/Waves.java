package fr.lumin0u.survivor.mobs;

import fr.lumin0u.survivor.Difficulty;
import fr.lumin0u.survivor.utils.TransparentUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;

public class Waves
{
	private static HashMap<Difficulty, List<Integer>> nbZombies;
	private static List<Integer> dogWaves;
	private static Random random;
	
	static
	{
		init();
	}
	
	public static void init() {
		random = new Random();
		dogWaves = new ArrayList<>();
		nbZombies = new HashMap<>();
		
		for(Difficulty diff : Difficulty.values())
		{
			nbZombies.put(diff, new ArrayList<>());
			nbZombies.get(diff).add(0);
		}
		
		dogWaves.add(random.nextInt(3) + 4);
	}
	
	public static boolean isDogWave(int wave)
	{
		for(int i = dogWaves.get(dogWaves.size() - 1); i < wave + 5; ++i)
		{
			if(i - dogWaves.get(dogWaves.size() - 1) > random.nextInt(4) + 5 && !isBossWave(i))
			{
				dogWaves.add(i);
			}
		}
		
		return dogWaves.contains(wave);
	}
	
	public static int getNbEnnemies(int wave, Difficulty diff)
	{
		for(int i = nbZombies.get(diff).size(); i < wave + 5; ++i)
		{
			nbZombies.get(diff).add((int) ((double) wave * Math.sqrt(diff.getNB()) * 1.3 + 1.5 + Math.random() * (double) diff.getNB()));
		}
		
		return nbZombies.get(diff).get(wave);
	}
	
	public static double getEnnemiesLife(int wave, Difficulty diff)
	{
		return 2.3D * diff.getEnnemyHealthModifier() * (double) wave;
	}
	
	private static BiFunction<Double, Double, Double> speedF =
		(w, d) -> 0.03 * w / (20 - 2 * d + w);
	
	public static double getEnnemiesSpeed(int wave, Difficulty diff)
	{
		double constant = 0.13 + 0.005 * (double) diff.getNB();
		
		return constant + speedF.apply((double) wave, (double) diff.getNB());
	}
	
	public static boolean isBossWave(int wave)
	{
		return wave % 10 == 0;
	}
	
	public static Location aWolfSpawnLocationAround(Player player)
	{
		List<Location> possible = new ArrayList<>();
		Location playerLoc = player.getEyeLocation();
		
		for(int x = -10; x < 10; ++x)
		{
			for(int y = -3; y < 3; ++y)
			{
				for(int z = -10; z < 10; ++z)
				{
					Location l = new Location(player.getWorld(), (double) x + playerLoc.getX() + 0.5D, (double) y + playerLoc.getY() + 0.5D, (double) z + playerLoc.getZ() + 0.5D);
					boolean transparent = TransparentUtils.isFullBlock(l.clone().add(0.0D, -1.0D, 0.0D).getBlock().getType());
					transparent |= l.clone().add(0.0D, -1.0D, 0.0D).getBlock().getType().name().matches(".*SLAB2|.*STEP|.*STAIRS");
					if(transparent && l.getBlock().getType().equals(Material.AIR) && TransparentUtils.collisionBetween(playerLoc, l, true) == null)
					{
						possible.add(l);
					}
				}
			}
		}
		
		if(!possible.isEmpty())
		{
			return possible.get(random.nextInt(possible.size()));
		}
		else
		{
			return playerLoc;
		}
	}
}

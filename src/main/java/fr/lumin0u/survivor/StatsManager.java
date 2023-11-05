package fr.lumin0u.survivor;

import fr.lumin0u.survivor.player.SvPlayer;
import fr.lumin0u.survivor.weapons.Weapon;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StatsManager
{
	private static Map<UUID, Map<String, Double>> stats = new HashMap<>();
	
	public StatsManager()
	{
	}
	
	public static double getStatDouble(UUID playerUid, String name)
	{
		stats.putIfAbsent(playerUid, new HashMap<>());
		stats.get(playerUid).putIfAbsent(name, 0.0D);
		
		return stats.get(playerUid).get(name);
	}
	
	public static int getStatInt(UUID playerUid, String name)
	{
		return (int) getStatDouble(playerUid, name);
	}
	
	public static boolean getStatBoolean(UUID playerUid, String name)
	{
		return getStatDouble(playerUid, name) != 0.0D;
	}
	
	public static void setStat(UUID playerUid, String name, double value)
	{
		stats.putIfAbsent(playerUid, new HashMap<>());
		
		stats.get(playerUid).put(name, value);
	}
	
	public static void increaseStat(UUID playerUid, String name, double value, boolean store)
	{
		setStat(playerUid, name, getStatDouble(playerUid, name) + value);
		//if(store)
		//	RedisUtil.incrementStat("zombie", playerUid, name, value);
	}
	
	public static void increaseStat(UUID playerUid, String name, int value, boolean store)
	{
		setStat(playerUid, name, getStatInt(playerUid, name) + value);
		//if(store)
		//	RedisUtil.incrementStat("zombie", playerUid, name, value);
	}
	
	public static void increaseWeaponShots(Weapon weapon)
	{
		increaseStat(((SvPlayer)weapon.getOwner()).getPlayerUid(), weapon.getType().name().toLowerCase() + ":shots", 1, true);
	}
	
	public static void increaseWeaponHits(Weapon weapon)
	{
		increaseStat(((SvPlayer)weapon.getOwner()).getPlayerUid(), weapon.getType().name().toLowerCase() + ":hits", 1, true);
	}
	
	public static void increaseWeaponDamage(Weapon weapon, double damage)
	{
		increaseStat(((SvPlayer)weapon.getOwner()).getPlayerUid(), weapon.getType().name().toLowerCase() + ":dmg", damage, true);
	}
	
	public static void increaseWeaponKills(Weapon weapon)
	{
		increaseStat(((SvPlayer)weapon.getOwner()).getPlayerUid(), weapon.getType().name().toLowerCase() + ":kills", 1, true);
	}
}

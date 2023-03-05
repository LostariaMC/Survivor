package fr.lumin0u.survivor;

import fr.lumin0u.survivor.player.SvPlayer;
import fr.lumin0u.survivor.utils.MCUtils;
import fr.worsewarn.cosmox.api.players.WrappedPlayer;
import fr.worsewarn.cosmox.api.scoreboard.CosmoxScoreboard;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;

import java.awt.*;
import java.util.Comparator;
import java.util.Objects;
import java.util.Random;

public class Surviboard
{
	private static final int SEED = new Random().nextInt();
	private static final Comparator<Object> HASH_COMPARATOR = (o1, o2) -> Integer.compare(Objects.hashCode(o1) * SEED, Objects.hashCode(o2) * SEED);
	
	private static int lerp(double a, double b, double m)
	{
		return (int) ((1 - m) * a + m * b);
	}
	
	private static String getPlayerLine(SvPlayer sp, Location from) {
		
		String arrow = sp.isOnline() ? MCUtils.pointingArrow(from, sp.toBukkit().getLocation()) : "\u2022";
		
		String lifePrefix;
		
		double normalizedHealth = sp.isOnline() ? 1 - sp.getPlayer().getHealth() / sp.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() : 0;
		
		if(!sp.isOnline())
			lifePrefix = "§7§m";
		else if(sp.isDead())
			lifePrefix = "§8✞ ";
		else if(sp.isOnGround())
			lifePrefix = "§5§l";
		else
			lifePrefix = ChatColor.of(new Color(lerp(75, 0xaa, normalizedHealth), lerp(255, 0, normalizedHealth), lerp(75, 0, normalizedHealth))).toString();
		
		return "§7" + arrow + " " + lifePrefix + sp.getName() + " §e" + sp.getMoney() + "$";
	}
	
	private static void updateScoreboardScore(SvPlayer player) {
		
		player.toCosmox().setScoreboardScore(player.getMoney());
	}
	
	private static String getWaveLine(GameManager gm) {
		return "§7Vague§8: " + (gm.isInWave() ? "§c" + gm.getWave() : "§a" + gm.getWave() + " \u279D " + (gm.getWave() + 1));
	}
	
	public static void reInitScoreboard(WrappedPlayer player)
	{
		GameManager gm = GameManager.getInstance();
		
		CosmoxScoreboard scoreboard = new CosmoxScoreboard(player.toBukkit());
		scoreboard.updateTitle("§f§lSURVIVOR");
		
		int i = 0;
		
		scoreboard.updateLine(i++, "§0");
		scoreboard.updateLine(i++, getWaveLine(gm));
		scoreboard.updateLine(i++, "§7» Difficulté : " + gm.getDifficulty().getColoredDisplayName());
		scoreboard.updateLine(i++, "§1");
		
		for(SvPlayer other : gm.getPlayers().stream().sorted(HASH_COMPARATOR).toList())
		{
			if(i > 10)
				break;
			scoreboard.updateLine(i++, getPlayerLine(other, player.toBukkit().getLocation()));
			updateScoreboardScore(other);
		}
		
		scoreboard.updateLine(i++, "§d");
		scoreboard.updateLine(i++, "§8» Don§8: §7/money");
		scoreboard.updateLine(i, "§f");
		
		player.toCosmox().setScoreboard(scoreboard);
	}
	
	public static void updatePlayerLine(SvPlayer sp) {
		
		GameManager gm = GameManager.getInstance();
		
		int line = 5 + gm.getPlayers().stream().sorted(Surviboard.HASH_COMPARATOR).toList().indexOf(sp);
		updateScoreboardScore(sp);
		
		for(WrappedPlayer player : WrappedPlayer.of(Bukkit.getOnlinePlayers()))
		{
			player.toCosmox().getScoreboard().updateLine(line, getPlayerLine(sp, player.toBukkit().getLocation()));
		}
	}
	
	public static void updateWave() {
		
		GameManager gm = GameManager.getInstance();
		
		for(WrappedPlayer player : WrappedPlayer.of(Bukkit.getOnlinePlayers()))
		{
			player.toCosmox().getScoreboard().updateLine(1, getWaveLine(gm));
		}
	}
	
	@Deprecated
	private static void refreshScoreboard(SvPlayer player)
	{
		GameManager gm = GameManager.getInstance();
		
		CosmoxScoreboard scoreboard = player.toCosmox().getScoreboard();
		
		int i = 0;
		
		scoreboard.updateLine(i++, "§6Vague§8: §e" + gm.getWave() + (gm.isInWave() ? "" : " \u279D " + (gm.getWave() + 1)));
		
		for(SvPlayer other : gm.getPlayers())
		{
			scoreboard.updateLine(i++, (!other.isAlive() ? (other.isOnGround() ? "§c" : "§8✞ ") : "§6") + Bukkit.getOfflinePlayer(other.getPlayerUid()).getName() + "§8: §e" + other.getMoney() + "$");
		}
		
		scoreboard.updateLine(i, "");
		
		/*
		ScoreboardManager manager = Bukkit.getScoreboardManager();
		Scoreboard board = manager.getNewScoreboard();
		Objective rightObjective = board.registerNewObjective("Sv scoreboard", "dummy");
		
		int score = 10;
		rightObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
		rightObjective.setDisplayName("§6-=§8Zombie§6=-");
		
		Score var10000 = rightObjective.getScore("§6Timer: §7" + (gm.getTimer() / 1000L / 60L < 10L ? "0" : "") + gm.getTimer() / 1000L / 60L + ":" + (gm.getTimer() / 1000L % 60L < 10L ? "0" : "") + gm.getTimer() / 1000L % 60L);
		
		int var10 = score - 1;
		var10000.setScore(score);
		
		if(gm.isStarted())
		{
			rightObjective.getScore("§8§l> §6Mode§8: " + gm.getDifficulty().getColoredDisplayName()).setScore(var10--);
			rightObjective.getScore("§0").setScore(var10--);
			rightObjective.getScore("§6Vague§8: §e" + gm.getWave()).setScore(var10--);
		}
		
		int remainingEnnemies = gm.isDogWave() ? gm.getRemainingWolves() : gm.getMobs().size();
		
		if(remainingEnnemies > 0)
		{
			rightObjective.getScore("§6Ennemis restants§8: §e" + remainingEnnemies).setScore(var10--);
		}
		else if(gm.isStarted() && gm.getTimeUntilNextWave() > 0)
		{
			rightObjective.getScore("§6Prochaine vague dans §e" + gm.getTimeUntilNextWave() / 20 + "s").setScore(var10--);
		}
		else if(gm.isStarted())
		{
			rightObjective.getScore("§6Plus aucun ennemis").setScore(var10--);
		}
		
		rightObjective.getScore("§1").setScore(var10--);
		
		for(SvPlayer pl : gm.getPlayers())
		{
			rightObjective.getScore((!pl.isAlive() ? (pl.isOnGround() ? "§c" : "§8✞ ") : "§6") + Bukkit.getOfflinePlayer(pl.getPlayerUid()).getName() + "§8: §e" + pl.getMoney() + "$").setScore(var10--);
		}
		
		
		final int blinkDelay = 100;
		String baseHellariaFr = "play.hellaria.fr";
		StringBuilder hellariafr = new StringBuilder(baseHellariaFr);
		Function<Integer, Integer> niceIndex = index1 -> Utils.clamp(index1, 0, hellariafr.length());
		int index = niceIndex.apply(Survivor.getCurrentTick() % (blinkDelay + baseHellariaFr.length()));
		
		hellariafr.insert(niceIndex.apply(index), "§6");
		hellariafr.insert(niceIndex.apply(index + 3), "§c");
		hellariafr.insert(niceIndex.apply(index + 6), "§6");
		hellariafr.insert(niceIndex.apply(index + 9), "§e");
		
		rightObjective.getScore("§e").setScore(var10--);
		rightObjective.getScore("§e" + hellariafr.toString()).setScore(var10--);
		
		Objective tabObjective = board.registerNewObjective("sv tab", "dummy");
		tabObjective.setDisplaySlot(DisplaySlot.PLAYER_LIST);
		
		for(SvPlayer pl : gm.getPlayers())
		{
			tabObjective.getScore(pl.getName()).setScore(StatsManager.getStatInt(pl.getPlayerUid(), "totalKills"));
		}
		
		player.toBukkit().setScoreboard(board);
		String listName = "§7" + player.getName();
		player.toBukkit().setPlayerListName(listName);*/
	}
}

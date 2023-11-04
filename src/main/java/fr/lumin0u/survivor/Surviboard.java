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

public class Surviboard
{
	private static final Comparator<Object> HASH_COMPARATOR = (o1, o2) -> Integer.compare(Objects.hashCode(o1), Objects.hashCode(o2));
	
	private static int lerp(double a, double b, double m)
	{
		return (int) ((1 - m) * a + m * b);
	}
	
	private static String getPlayerLine(SvPlayer sp, Location from) {
		
		String arrow = sp.isOnline() ? MCUtils.pointingArrow(from, sp.toBukkit().getLocation()) : "•";
		
		String lifePrefix;
		
		double normalizedHealth = sp.isOnline() ? 1 - sp.toBukkit().getHealth() / sp.toBukkit().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() : 0;
		
		if(!sp.isOnline())
			lifePrefix = "§7§m";
		else if(sp.isDead())
			lifePrefix = "§8✞ ";
		else if(sp.isOnGround())
			lifePrefix = "§5§l";
		else
			lifePrefix = ChatColor.of(new Color(lerp(75, 0xaa, normalizedHealth), lerp(255, 0, normalizedHealth), lerp(75, 0, normalizedHealth))).toString();
		
		return "§7" + arrow + " " + lifePrefix + sp.getName() + " §e" + (int) sp.getMoney() + "$";
	}
	
	public static void updateScoreboardScore(SvPlayer player) {
		player.toCosmox().setScoreboardScore((int) player.getMoney());
	}
	
	private static String getWaveLine(GameManager gm) {
		return "§7Vague§8: " + (gm.isInWave() ? "§c" + gm.getWave() : "§a" + gm.getWave() + " ➝ " + (gm.getWave() + 1));
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
		scoreboard.updateLine(i++, "§f");
		
		player.toCosmox().setScoreboard(scoreboard);
	}
	
	public static void updatePlayerLine(SvPlayer sp) {
		
		GameManager gm = GameManager.getInstance();
		
		int line = 4 + gm.getPlayers().stream().sorted(Surviboard.HASH_COMPARATOR).toList().indexOf(sp);
		updateScoreboardScore(sp);
		
		//Bukkit.broadcastMessage("\nfrom " + sp.getName() + ": line is " + line);
		
		for(WrappedPlayer player : WrappedPlayer.of(Bukkit.getOnlinePlayers()))
		{
			String playerLine = getPlayerLine(sp, player.toBukkit().getLocation());
			//Bukkit.broadcastMessage("from " + sp.getName() + " to " + player.getName() + ": " + playerLine);
			
			player.toCosmox().getScoreboard().updateLine(line, playerLine);
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
		
		scoreboard.updateLine(i++, "§6Vague§8: §e" + gm.getWave() + (gm.isInWave() ? "" : " ➝ " + (gm.getWave() + 1)));
		
		for(SvPlayer other : gm.getPlayers())
		{
			scoreboard.updateLine(i++, (!other.isAlive() ? (other.isOnGround() ? "§c" : "§8✞ ") : "§6") + other.getName() + "§8: §e" + other.getMoney() + "$");
		}
		
		scoreboard.updateLine(i, "");
	}
}

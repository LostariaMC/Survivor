package fr.lumin0u.survivor;

import fr.lumin0u.survivor.utils.ItemBuilder;
import fr.lumin0u.survivor.utils.SurvivorParameters;
import fr.worsewarn.cosmox.api.statistics.Statistic;
import fr.worsewarn.cosmox.game.Game;
import fr.worsewarn.cosmox.game.GameVariables;
import fr.worsewarn.cosmox.game.configuration.Parameter;
import fr.worsewarn.cosmox.game.teams.Team;
import fr.worsewarn.cosmox.tools.map.MapLocation;
import fr.worsewarn.cosmox.tools.map.MapLocationType;
import fr.worsewarn.cosmox.tools.map.MapTemplate;
import fr.worsewarn.cosmox.tools.map.MapType;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SurvivorGame
{
	public static String prefix = "§2§lSurvivor §f▪ ";
	
	/*
	 * %y = sum of integers of a category
	 * %b = value of parameter (boolean)
	 * %v = value of parameter (integer)
	 * %f = value of parameter (float)
	 * %t = value of the integer to add (just for integers)
	 * %ls = current str in the options list
	 *
	 *
	 * %rbname; = value of name parameter (boolean)
	 * %rvname; = value of name parameter (int)
	 * %rfname; = value of name parameter (float)
	 * %rtname; = value to add of name parameter (int)
	 * %rlsname; = current str in the options list (str)
	 */
	
	private SurvivorGame() {
	}
	
	public static Game get() {
		
		// statistics
		List<Statistic> statistics = new ArrayList<>(Arrays.asList(
				new Statistic("Temps de jeu", GameVariables.TIME_PLAYED, true),
				new Statistic("Parties jouées", GameVariables.GAMES_PLAYED),
				new Statistic("Kills", GameVariables.KILLS, true, true),
				new Statistic("Victoires", GameVariables.WIN)
		));
		
		/*statistics.addAll(Arrays.stream(WeaponType.values())
				.map(weaponType -> new Statistic("Dégats avec " + weaponType.getName(), "survivor:%sdmg".formatted(weaponType.getName()), false, false, false))
				.toList());*/
		
		// parameters
		List<Parameter> parameters = new ArrayList<>(Arrays.asList(
				new Parameter(SurvivorParameters.DIFFICULTY, "",
						Arrays.stream(Difficulty.values()).map(Difficulty::getColoredDisplayName).toList(),
						new ItemBuilder(Material.SKELETON_SKULL).setDisplayName("§6Difficulté").addLore(List.of(" ", "§7Définir la difficulté de", "§7la partie", " ", "§e Valeur actuelle : %ls")).build(),
						false, false)
		));
		
		
		Game game = new Game("survivor", "Survivor", ChatColor.DARK_GREEN, Material.ZOMBIE_HEAD, Arrays.asList(Team.RANDOM), 1, true, true,
				statistics,
				/*Achievements*/
				List.of(),
				/*Description*/
				List.of(
						" ",
						"§7Pan ! Dans la tete du zombie !",
						"§7Ratatata !"
				),
				/*MapTemplate*/
				List.of(new MapTemplate(MapType.NONE,
								List.of(
										new MapLocation("authors", MapLocationType.STRING),
										new MapLocation("name", MapLocationType.STRING)
								))
				));
		
		game.setDefaultFriendlyFire(false)
				.setCanSeeFriendlyInvisible(true)
				.setScoreboardOption(org.bukkit.scoreboard.Team.Option.COLLISION_RULE, org.bukkit.scoreboard.Team.OptionStatus.ALWAYS)
				.setShowScoreTablist(true)
				
				.setGameAuthor("lumin0u")
				.activeJoinInGame();
		
		for(Parameter parameter : parameters)
			game.addParameter(parameter);
		
		prefix = game.getPrefix();
		
		return game;
	}
}

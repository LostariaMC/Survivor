package fr.lumin0u.survivor;

import fr.lumin0u.survivor.utils.ImmutableItemStack;
import fr.lumin0u.survivor.utils.ItemBuilder;
import fr.worsewarn.cosmox.api.achievements.Achievement;
import fr.worsewarn.cosmox.api.statistics.AggregationFunction;
import fr.worsewarn.cosmox.api.statistics.Statistic;
import fr.worsewarn.cosmox.game.Game;
import fr.worsewarn.cosmox.game.GameVariables;
import fr.worsewarn.cosmox.game.configuration.Parameter;
import fr.worsewarn.cosmox.game.teams.Team;
import fr.worsewarn.cosmox.tools.items.DefaultItemSlot;
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
	public static String prefix = "§fSurvivor (!) ";
	public static final ImmutableItemStack DIFF_VOTE_ITEM = new ItemBuilder(Material.SKELETON_SKULL).setDisplayName("§eChoix de la difficulté").setLore("§7Si l'host choisit une difficulté,", "§7votre vote ne sera pas pris", "§7en compte").buildImmutable();
	
	public static final Achievement SURVIVOR_ACHIEVEMENT = new Achievement(3600, "Survivor", Material.ZOMBIE_HEAD, "Terminer tous les succès en Survivor", 0);
	public static final Achievement UPGRADE_ACHIEVEMENT = new Achievement(3601, "Amélioration", Material.HONEY_BOTTLE, "Améliorer une arme", 3600);
	public static final Achievement UPGRADE5_ACHIEVEMENT = new Achievement(3602, "Eradication", Material.EXPERIENCE_BOTTLE, "Améliorer une arme niveau 5", 3601);
	public static final Achievement TRIPLE_KILL_ACHIEVEMENT = new Achievement(3603, "Une balle, 3 zombies", Material.SPECTRAL_ARROW, "Tuez 3 zombies en une balle", 3600);
	
	public static final String DOWNFALLS_STAT = "downfalls";
	public static final String REANIMATIONS_STAT = "reanimations";
	public static final String WAVE_REACHED_EASY_STAT = "wavereachedeasy";
	public static final String WAVE_REACHED_NORMAL_STAT = "wavereachednormal";
	public static final String WAVE_REACHED_HARD_STAT = "wavereachedhard";
	public static final String WAVE_REACHED_EXPERT_STAT = "wavereachedexpert";
	
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
				new Statistic("Zombies tués", GameVariables.KILLS, true, true),
				new Statistic("Chutes au sol", DOWNFALLS_STAT, true, true),
				new Statistic("Morts", GameVariables.DEATHS, true, true),
				new Statistic("Réanimations", REANIMATIONS_STAT, true, true),
				new Statistic("Meilleure vague (easy)", WAVE_REACHED_EASY_STAT, false, false, true, AggregationFunction.MAX),
				new Statistic("Meilleure vague (normal)", WAVE_REACHED_NORMAL_STAT, false, false, true, AggregationFunction.MAX),
				new Statistic("Meilleure vague (hard)", WAVE_REACHED_HARD_STAT, false, false, true, AggregationFunction.MAX),
				new Statistic("Meilleure vague (expert)", WAVE_REACHED_EXPERT_STAT, false, false, true, AggregationFunction.MAX)
		));
		
		/*statistics.addAll(Arrays.stream(WeaponType.values())
				.map(weaponType -> new Statistic("Dégats avec " + weaponType.getName(), "survivor:%sdmg".formatted(weaponType.getName()), false, false, false))
				.toList());*/
		
		// parameters
		Parameter difficultyParameter = new Parameter(SurvivorParameters.DIFFICULTY, "",
				Arrays.stream(Difficulty.values()).map(Difficulty::getColoredDisplayName).toList(),
				new ItemBuilder(Difficulty.NOT_SET.getItemRep()).setDisplayName("§6Difficulté").setLore(List.of(" ", "§7Définir la difficulté de", "§7la partie", " ", "§e Valeur actuelle : %ls")).build(),
				false, false);
		difficultyParameter.setCurrentInt(Difficulty.NOT_SET.ordinal());
		
		List<Parameter> parameters = new ArrayList<>(List.of(difficultyParameter));
		
		
		Game game = new Game("survivor", "Survivor", ChatColor.DARK_GREEN, Material.ZOMBIE_HEAD, Arrays.asList(Team.RANDOM), 1, true, true,
				statistics,
				/*Achievements*/
				List.of(
						SURVIVOR_ACHIEVEMENT,
						UPGRADE_ACHIEVEMENT,
						UPGRADE5_ACHIEVEMENT,
						TRIPLE_KILL_ACHIEVEMENT
				),
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
		
		game.addDefaultItemWaitingRoom(new DefaultItemSlot("diffVoteItem", DIFF_VOTE_ITEM), 7);
		
		for(Parameter parameter : parameters)
			game.addParameter(parameter);
		
		prefix = game.getPrefix();
		
		return game;
	}
	
	public static class SurvivorParameters
	{
		public static final String DIFFICULTY = "DIFFICULTY";
	}
}

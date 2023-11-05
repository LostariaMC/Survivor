package fr.lumin0u.survivor.config.commands;

import fr.lumin0u.survivor.Survivor;
import fr.lumin0u.survivor.commands.SvArgCommand;
import fr.lumin0u.survivor.config.MapConfig;
import fr.lumin0u.survivor.config.MapConfigCreation;
import fr.lumin0u.survivor.config.MapConfigRenderer;
import fr.lumin0u.survivor.utils.MCUtils;
import fr.worsewarn.cosmox.api.players.WrappedPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.ClickEvent.Action;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class StartConfigCommand extends SvArgCommand
{
	private Map<Player, String> confirmQueue = new HashMap<>();
	
	public StartConfigCommand() {
		super("startConfig", "Commence la création de la configuration d'une map", "<map>", false, 1, true);
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		if(args[0].equalsIgnoreCase("confirm"))
		{
			MapConfigCreation configCreation = Survivor.getInstance().getInCreationMapConfigs().get(WrappedPlayer.of(sender));
			
			if(args.length > 1 && args[1].equalsIgnoreCase("save") && configCreation != null) {
				configCreation.config().save(Survivor.getInstance().getMapConfigName(WrappedPlayer.of(sender)));
				
				sender.sendMessage("§aConfiguration sauvegardée");
			}
			
			if(confirmQueue.containsKey((Player) sender))
			{
				startConfig((Player) sender, confirmQueue.remove((Player) sender));
			}
			else
			{
				sender.sendMessage("§cCe nom n'est pas valide");
			}
		}
		else if(Survivor.getInstance().getMapConfig(WrappedPlayer.of(sender)) != null)
		{
			confirmQueue.put((Player) sender, args[0]);
			sender.sendMessage("§cVous étiez déjà en train de configurer une map !");
			
			sender.sendMessage(MCUtils.buildTextComponent(" ", "§7Pour §cécraser §7l'ancienne configuration et commencer la nouvelle", Component.text("§6[cliquez ici]")
					.clickEvent(ClickEvent.clickEvent(Action.RUN_COMMAND, "/sv startConfig confirm"))
					.hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text("§cCommencer la configuration")))));
			
			sender.sendMessage(MCUtils.buildTextComponent(" ", "§7Pour §asauvegarder l'ancienne configuration et commencer la nouvelle", Component.text("§6[cliquez ici]")
					.clickEvent(ClickEvent.clickEvent(Action.RUN_COMMAND, "/sv startConfig confirm save"))
					.hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text("§cCommencer la configuration")))));
		}
		else
		{
			startConfig((Player) sender, args[0]);
		}
	}
	
	public static void startConfig(Player player, String mapName)
	{
		if(Survivor.getInstance().getInCreationMapConfigs().containsKey(WrappedPlayer.of(player)))
		{
			Survivor.getInstance().getMapConfigRenderer(WrappedPlayer.of(player)).stop();
		}
		
		if(MapConfig.doesConfigExists(mapName))
			player.sendMessage("§7Vous continuez la configuration de la map §f" + mapName);
		else
			player.sendMessage("§7Vous commencez la configuration de la map §f" + mapName);
		
		MapConfig config = MapConfig.loadConfig(mapName);
		
		Survivor.getInstance().getInCreationMapConfigs().put(WrappedPlayer.of(player), new MapConfigCreation(config, mapName, new MapConfigRenderer(WrappedPlayer.of(player), config)));
	}
}

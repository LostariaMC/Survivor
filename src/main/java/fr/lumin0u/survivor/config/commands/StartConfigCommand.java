package fr.lumin0u.survivor.config.commands;

import fr.lumin0u.survivor.Survivor;
import fr.lumin0u.survivor.commands.SvArgCommand;
import fr.lumin0u.survivor.config.MapConfig;
import fr.lumin0u.survivor.config.MapConfigCreation;
import fr.lumin0u.survivor.config.MapConfigRenderer;
import fr.lumin0u.survivor.utils.MCUtils;
import fr.worsewarn.cosmox.api.players.WrappedPlayer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
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
			if(confirmQueue.containsKey((Player) sender))
			{
				startConfig((Player) sender, confirmQueue.remove((Player) sender));
			}
			else
			{
				sender.sendMessage("§cCe nom n'est pas valide");
			}
		}
		else if(Survivor.getInstance().getMapConfig(((Player) sender).getUniqueId()) != null)
		{
			confirmQueue.put((Player) sender, args[0]);
			sender.sendMessage("§cVous étiez déjà en train de configurer une map !");
			
			TextComponent confirm = new TextComponent("§6[cliquez ici]");
			confirm.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/sv startConfig confirm"));
			confirm.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[] {new TextComponent("/sv startConfig confirm")}));
			
			sender.sendMessage(MCUtils.buildTextComponent(" ", "§7Pour écraser l'ancienne configuration et commencer la nouvelle", confirm));
		}
		else
		{
			startConfig((Player) sender, args[0]);
		}
	}
	
	public static void startConfig(Player player, String mapName)
	{
		if(Survivor.getInstance().getInCreationMapConfigs().containsKey(player.getUniqueId()))
		{
			Survivor.getInstance().getMapConfigRenderer(player.getUniqueId()).stop();
		}
		
		if(MapConfig.doesConfigExists(mapName))
			player.sendMessage("§7Vous continuez la configuration de la map §f" + mapName);
		else
			player.sendMessage("§7Vous commencez la configuration de la map §f" + mapName);
		
		MapConfig config = MapConfig.loadConfig(mapName);
		
		Survivor.getInstance().getInCreationMapConfigs().put(player.getUniqueId(), new MapConfigCreation(config, mapName, new MapConfigRenderer(WrappedPlayer.of(player), config)));
	}
}

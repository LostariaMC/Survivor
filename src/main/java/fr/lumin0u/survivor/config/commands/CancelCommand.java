package fr.lumin0u.survivor.config.commands;

import fr.lumin0u.survivor.Survivor;
import fr.lumin0u.survivor.commands.SvArgCommand;
import fr.lumin0u.survivor.config.Action;
import fr.lumin0u.survivor.config.MapConfig;
import fr.lumin0u.survivor.utils.MCUtils;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CancelCommand extends SvArgCommand
{
	public CancelCommand() {
		super("cancel", "", "<id>", true, 1, true);
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		
		Player player = (Player) sender;
		MapConfig config = Survivor.getInstance().getMapConfig(player.getUniqueId());
		if(config == null)
		{
			player.sendMessage("§cVous n'avez pas commencé de configuration (voir /sv startConfig)");
			return;
		}
		
		int id = Integer.parseInt(args[0]);
		
		boolean isRedo = args.length != 1 && args[0].equalsIgnoreCase("redo");
		
		if(config.getAction(id) != null)
		{
			Action action = config.removeAction(id);
			if(isRedo)
			{
				action.redo();
				player.sendMessage("§7Opération réitérée");
			}
			else
			{
				action.undo();
				
				int newId = config.addAction(action);
				
				TextComponent redoText = new TextComponent("§8[refaire]");
				redoText.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/sv cancel %d redo".formatted(newId)));
				redoText.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[] {new TextComponent("Annuler l'annulation")}));
				
				player.sendMessage(MCUtils.buildTextComponent(" ", "§7Opération annulée", redoText));
			}
		}
		else
		{
			player.sendMessage("§7Cette opération a déjà été " + (isRedo ? "réitérée" : "annulée"));
		}
	}
}

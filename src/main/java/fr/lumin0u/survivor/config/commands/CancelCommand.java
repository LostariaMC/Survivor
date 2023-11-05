package fr.lumin0u.survivor.config.commands;

import fr.lumin0u.survivor.Survivor;
import fr.lumin0u.survivor.commands.SvArgCommand;
import fr.lumin0u.survivor.config.Action;
import fr.lumin0u.survivor.config.MapConfig;
import fr.lumin0u.survivor.utils.MCUtils;
import fr.worsewarn.cosmox.api.players.WrappedPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
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
		MapConfig config = Survivor.getInstance().getMapConfig(WrappedPlayer.of(player));
		if(config == null) {
			player.sendMessage("§cVous n'avez pas commencé de configuration (voir /sv startConfig)");
			return;
		}
		
		int id = Integer.parseInt(args[0]);
		
		boolean isRedo = args.length != 1 && args[0].equalsIgnoreCase("redo");
		
		if(config.getAction(id) != null) {
			Action action = config.removeAction(id);
			if(isRedo) {
				action.redo();
				player.sendMessage("§7Opération réitérée");
			}
			else {
				action.undo();
				
				int newId = config.addAction(action);
				
				player.sendMessage(MCUtils.buildTextComponent(" ", "§7Opération annulée", Component.text("§8[refaire]")
						.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/sv cancel %d redo".formatted(newId)))
						.hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text("Annuler l'annulation")))));
			}
		}
		else {
			player.sendMessage("§7Cette opération n'existe pas ou a déjà été " + (isRedo ? "réitérée" : "annulée"));
		}
	}
}

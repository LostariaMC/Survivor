package fr.lumin0u.survivor.config.commands;

import fr.lumin0u.survivor.Survivor;
import fr.lumin0u.survivor.commands.SvArgCommand;
import fr.lumin0u.survivor.config.Action;
import fr.lumin0u.survivor.config.ConfigUtil;
import fr.lumin0u.survivor.config.MapConfig;
import fr.lumin0u.survivor.utils.MCUtils;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class AddRoomCommand extends SvArgCommand
{
	public AddRoomCommand() {
		super("addRoom", "Créer une nouvelle salle", "<nom>", false, 1, true);
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
		
		if(config.getRoom(args[0]) != null)
		{
			player.sendMessage("§cCette salle existe déjà");
			return;
		}
		
		String name = String.join(" ", args).replaceAll("_", " ");
		
		TextComponent extra = ConfigUtil.getAdditionAndDo(config, new Action()
		{
			@Override
			public void redo() {
                config.addRoom(name);
				Survivor.getInstance().getMapConfigRenderer(player.getUniqueId()).update();
            }
			
			@Override
			public void undo() {
                config.getRooms().removeIf(room -> room.getName().equals(name));
				Survivor.getInstance().getMapConfigRenderer(player.getUniqueId()).update();
            }
		}, "annuler");
		player.sendMessage(MCUtils.buildTextComponent(" ", "§aSalle ajoutée", extra));
	}
	
	@Override
	public List<String> getPossibleArgs(CommandSender executer, String[] args) {
		return new ArrayList<>();
	}
}

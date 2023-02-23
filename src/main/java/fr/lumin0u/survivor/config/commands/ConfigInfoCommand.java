package fr.lumin0u.survivor.config.commands;

import fr.lumin0u.survivor.Survivor;
import fr.lumin0u.survivor.commands.SvArgCommand;
import fr.lumin0u.survivor.config.ConfigUtil;
import fr.lumin0u.survivor.config.MapConfig;
import fr.lumin0u.survivor.objects.Room;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class ConfigInfoCommand extends SvArgCommand
{
	public ConfigInfoCommand() {
		super("configInfo", "Affiche la configuration en cours", "[ammoBoxes|magicBoxes|mobSpawns]", false, 0, true, "info");
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
		
		if(args.length == 0)
		{
			player.sendMessage(ConfigUtil.toPlayerExplanation(config, player.getWorld()));
		}
		else if(args[0].equalsIgnoreCase("ammoBoxes"))
		{
			player.sendMessage(ConfigUtil.toPlayerExplanation(config, config.ammoBoxes, player.getWorld(), "§7Boites de munitions:"));
		}
		else if(args[0].equalsIgnoreCase("magicBoxes"))
		{
			player.sendMessage(ConfigUtil.toPlayerExplanation(config, config.magicBoxes, player.getWorld(), "§7Boites magiques:"));
		}
		else if(args[0].equalsIgnoreCase("mobSpawns"))
		{
			if(args.length == 1)
				player.sendMessage("§cVeuillez indiquer la salle");
			
			Room room = config.getRoom(String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
			
			if(room == null)
				player.sendMessage("§cCette salle n'existe pas");
			else
				player.sendMessage(ConfigUtil.toPlayerExplanation(config, room.getMobSpawnsUnsafe(), player.getWorld(), "§7Points d'apparition de §f%s§7:".formatted(room.getName())));
		}
	}
	
	@Override
	public List<String> getPossibleArgs(CommandSender executer, String[] args) {
		return Arrays.asList("ammoBoxes", "magicBoxes", "mobSpawns");
	}
}

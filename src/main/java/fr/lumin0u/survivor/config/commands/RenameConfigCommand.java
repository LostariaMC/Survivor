package fr.lumin0u.survivor.config.commands;

import fr.lumin0u.survivor.commands.SvArgCommand;
import fr.lumin0u.survivor.config.MapConfig;
import org.bukkit.command.CommandSender;

public class RenameConfigCommand extends SvArgCommand
{
	public RenameConfigCommand() {
		super("renameConfig", "Renomme une configuration", "<ancien nom> <nouveau nom>", false, 2, true);
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		
		if(args[1].equalsIgnoreCase("confirm"))
		{
			sender.sendMessage("§cLe nouveau nom n'est pas valide");
			return;
		}
		
		MapConfig config = MapConfig.loadConfig(args[0]);
		config.save(args[1]);
		MapConfig.deleteConfig(args[0]);
		
		sender.sendMessage("§aLa configuration a bien été renommée !");
	}
}

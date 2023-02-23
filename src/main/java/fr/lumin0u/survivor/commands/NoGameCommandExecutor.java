package fr.lumin0u.survivor.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class NoGameCommandExecutor implements CommandExecutor
{
	public static final NoGameCommandExecutor INSTANCE = new NoGameCommandExecutor();
	
	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
	{
		sender.sendMessage("§cUne partie de survivor doit être lancée pour pouvoir éxecuter cette commande");
		return true;
	}
}

package fr.lumin0u.survivor.commands;

import fr.lumin0u.survivor.GameManager;
import fr.lumin0u.survivor.player.SvPlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class VoteSkipCommand implements CommandExecutor
{
	public VoteSkipCommand()
	{
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		SvPlayer sp = SvPlayer.of(sender);
		
		if(!sp.isSpectator())
			GameManager.getInstance().addVoteSkipper(sp);
		
		return true;
	}
}

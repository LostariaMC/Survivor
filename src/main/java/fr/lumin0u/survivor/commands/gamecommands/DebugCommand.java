package fr.lumin0u.survivor.commands.gamecommands;

import fr.lumin0u.survivor.commands.AbstractGameCommand;
import fr.lumin0u.survivor.mobs.Waves;
import org.bukkit.command.CommandSender;

public class DebugCommand extends AbstractGameCommand
{
	public DebugCommand()
	{
		super("debug", "debug", "", true, 0, false);
	}
	
	@Override
	public void execute(CommandSender sender, String[] args)
	{
		for(int i = 0; i < 30; i++)
		{
			if(Waves.isDogWave(i))
				sender.sendMessage(i + "");
		}
	}
}

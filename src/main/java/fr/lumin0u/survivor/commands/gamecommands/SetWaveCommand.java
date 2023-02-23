package fr.lumin0u.survivor.commands.gamecommands;

import fr.lumin0u.survivor.GameManager;
import fr.lumin0u.survivor.commands.AbstractGameCommand;
import org.bukkit.command.CommandSender;

public class SetWaveCommand extends AbstractGameCommand
{
    public SetWaveCommand() {
        super("setWave", "changer la vague", "<wave>", true, 1, false);
    }
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        GameManager.getInstance().setWave(Integer.parseInt(args[0]));
    }
}

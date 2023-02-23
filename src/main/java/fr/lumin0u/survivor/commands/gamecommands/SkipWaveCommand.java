package fr.lumin0u.survivor.commands.gamecommands;

import fr.lumin0u.survivor.GameManager;
import fr.lumin0u.survivor.commands.AbstractGameCommand;
import org.bukkit.command.CommandSender;

public class SkipWaveCommand extends AbstractGameCommand
{
    public SkipWaveCommand() {
        super("skipWave", "annule la vague courante", "", false, 0, false);
    }
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        GameManager.getInstance().skipWave();
    }
}

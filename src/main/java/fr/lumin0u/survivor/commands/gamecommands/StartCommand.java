package fr.lumin0u.survivor.commands.gamecommands;

import fr.lumin0u.survivor.GameManager;
import fr.lumin0u.survivor.commands.AbstractGameCommand;
import org.bukkit.command.CommandSender;

public class StartCommand extends AbstractGameCommand
{
    public StartCommand() {
        super("start", "lancer la partie", "", false, 0, false);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        GameManager.getInstance().startGame();
    }
}

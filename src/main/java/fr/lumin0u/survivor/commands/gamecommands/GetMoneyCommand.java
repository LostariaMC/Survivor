package fr.lumin0u.survivor.commands.gamecommands;

import fr.lumin0u.survivor.GameManager;
import fr.lumin0u.survivor.commands.AbstractGameCommand;
import fr.lumin0u.survivor.commands.SvArgCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GetMoneyCommand extends AbstractGameCommand
{
    public GetMoneyCommand() {
        super("getMoney", "DONNE DES SOUS", "<MONEY AMOUNT>", false, 1, true);
    }
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        GameManager.getInstance().getSvPlayer((Player)sender).addMoney(Integer.valueOf(args[0]));
    }
}

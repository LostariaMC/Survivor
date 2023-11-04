package fr.lumin0u.survivor.commands.gamecommands;

import fr.lumin0u.survivor.commands.AbstractGameCommand;
import fr.lumin0u.survivor.player.SvPlayer;
import org.bukkit.command.CommandSender;

public class GetMoneyCommand extends AbstractGameCommand
{
    public GetMoneyCommand() {
        super("getMoney", "DONNE DES SOUS", "<MONEY AMOUNT>", false, 1, true);
    }
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        SvPlayer.of(sender).addMoney(Integer.valueOf(args[0]));
    }
}

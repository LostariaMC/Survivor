package fr.lumin0u.survivor.commands.gamecommands;

import fr.lumin0u.survivor.commands.AbstractGameCommand;
import fr.lumin0u.survivor.player.SvPlayer;
import org.bukkit.command.CommandSender;

public class CleanInventoryCommand extends AbstractGameCommand
{
    public CleanInventoryCommand() {
        super("cleanInventory", "arrange l'inventaire, et permet quelques fois de regler des bugs", "", false, 0, true);
        this.minRankPower = 0;
    }
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        SvPlayer.of(sender).cleanInventory();
    }
}

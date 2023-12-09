package fr.lumin0u.survivor.config.commands;

import fr.lumin0u.survivor.SvAsset;
import fr.lumin0u.survivor.commands.SvArgCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class GetAssetItemCommand extends SvArgCommand
{
    public GetAssetItemCommand() {
        super("getAssetItem", "Donne l'item correspondant à un atout à placer dans une item frame", "<atout>", false, 1, true);
    }
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        Player p = (Player)sender;
        p.getInventory().addItem(SvAsset.byName(args[0].replaceAll("_", " ")).getItem());
    }
    
    @Override
    public List<String> getPossibleArgs(CommandSender executer, String[] args) {
        return Arrays.stream(SvAsset.values())
                .map(SvAsset::getName)
                .map(str -> str.replaceAll(" ", "_")).toList();
    }
}

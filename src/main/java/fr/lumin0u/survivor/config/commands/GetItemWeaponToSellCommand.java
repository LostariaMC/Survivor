package fr.lumin0u.survivor.config.commands;

import fr.lumin0u.survivor.commands.SvArgCommand;
import fr.lumin0u.survivor.weapons.WeaponType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class GetItemWeaponToSellCommand extends SvArgCommand
{
    public GetItemWeaponToSellCommand() {
        super("getItemWeaponToSell", "Donne l'item à placer dans une item frame", "<arme>", false, 1, true);
    }
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        Player p = (Player)sender;
        p.getInventory().addItem(WeaponType.byName(args[0].replaceAll("_", " ")).getItemToSell());

    }
    
    @Override
    public List<String> getPossibleArgs(CommandSender executer, String[] args) {
        return WeaponType.getNames().stream().map(s -> s.replace(" ", "_")).toList();
    }
}

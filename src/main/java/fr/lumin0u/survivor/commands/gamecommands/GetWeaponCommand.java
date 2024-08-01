package fr.lumin0u.survivor.commands.gamecommands;

import fr.lumin0u.survivor.GameManager;
import fr.lumin0u.survivor.commands.AbstractGameCommand;
import fr.lumin0u.survivor.player.SvPlayer;
import fr.lumin0u.survivor.weapons.WeaponType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class GetWeaponCommand extends AbstractGameCommand
{
    public GetWeaponCommand() {
        super("getWeapon", "Donne l'arme spécifiée", "<nom de l'arme>", false, 1, true);
    }
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        Player p = (Player)sender;
        if (GameManager.getInstance() != null) {
            SvPlayer sp = SvPlayer.of(p);
            
            sp.giveBuyableWeapon(WeaponType.byName(args[0].replaceAll("_", " ")).getNewWeapon(sp));
        }
    }
    
    @Override
    public List<String> getPossibleArgs(CommandSender executer, String[] args) {
        return WeaponType.getNames().stream().map(s -> s.replace(" ", "_")).toList();
    }
}

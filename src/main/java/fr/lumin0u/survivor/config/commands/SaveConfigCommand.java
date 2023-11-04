package fr.lumin0u.survivor.config.commands;

import fr.lumin0u.survivor.Survivor;
import fr.lumin0u.survivor.commands.SvArgCommand;
import fr.lumin0u.survivor.config.MapConfigCreation;
import fr.worsewarn.cosmox.api.players.WrappedPlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SaveConfigCommand extends SvArgCommand
{
    public SaveConfigCommand() {
        super("saveConfig", "Sauvegarde la configuration en cours", "", false, 0, true);
    }
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        
        MapConfigCreation configCreation = Survivor.getInstance().getInCreationMapConfigs().get(WrappedPlayer.of(player));
        
        if(configCreation == null)
        {
            player.sendMessage("§cVous n'avez pas commencé de configuration (voir /sv startConfig)");
            return;
        }
        configCreation.config().save(Survivor.getInstance().getMapConfigName(WrappedPlayer.of(player)));
        player.sendMessage("§aConfiguration sauvegardée");
    
        LeaveConfigCommand.leaveConfig(player);
    }
}

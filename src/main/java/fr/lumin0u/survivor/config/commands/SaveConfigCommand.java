package fr.lumin0u.survivor.config.commands;

import fr.lumin0u.survivor.GameManager;
import fr.lumin0u.survivor.Survivor;
import fr.lumin0u.survivor.commands.SvArgCommand;
import fr.lumin0u.survivor.config.MapConfig;
import fr.lumin0u.survivor.config.MapConfigCreation;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
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
        
        MapConfigCreation configCreation = Survivor.getInstance().getInCreationMapConfigs().get(player.getUniqueId());
        
        if(configCreation == null)
        {
            player.sendMessage("§cVous n'avez pas commencé de configuration (voir /sv startConfig)");
            return;
        }
        configCreation.config().save(Survivor.getInstance().getMapConfigName(player.getUniqueId()));
        player.sendMessage("§aConfiguration sauvegardée");
    
        LeaveConfigCommand.leaveConfig(player);
    }
}

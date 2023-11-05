package fr.lumin0u.survivor.config.commands;

import fr.lumin0u.survivor.Survivor;
import fr.lumin0u.survivor.commands.SvArgCommand;
import fr.lumin0u.survivor.config.MapConfigCreation;
import fr.worsewarn.cosmox.api.players.WrappedPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.ClickEvent.Action;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LeaveConfigCommand extends SvArgCommand
{
    public LeaveConfigCommand() {
        super("leaveConfig", "Quitte la configuration en cours", "", false, 0, true);
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
    
        leaveConfig(player);
    }
    
    public static void leaveConfig(Player player) {
        MapConfigCreation configCreation = Survivor.getInstance().getInCreationMapConfigs().get(WrappedPlayer.of(player));
        
        configCreation.renderer().stop();
    
        player.sendMessage(Component.text()
                .append(Component.text("§7Vous quittez la configuration actuelle "))
                .append(Component.text("§f[cliquez ici] §7pour la modifier")
                        .clickEvent(ClickEvent.clickEvent(Action.RUN_COMMAND, "/sv startconfig " + configCreation.name()))
                        .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text(" ")))));
    
        Survivor.getInstance().getInCreationMapConfigs().remove(WrappedPlayer.of(player));
    }
}

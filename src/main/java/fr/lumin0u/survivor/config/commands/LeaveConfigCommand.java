package fr.lumin0u.survivor.config.commands;

import fr.lumin0u.survivor.Survivor;
import fr.lumin0u.survivor.commands.SvArgCommand;
import fr.lumin0u.survivor.config.MapConfigCreation;
import fr.worsewarn.cosmox.game.WrappedPlayer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
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
        
        MapConfigCreation configCreation = Survivor.getInstance().getInCreationMapConfigs().get(player.getUniqueId());
        
        if(configCreation == null)
        {
            player.sendMessage("§cVous n'avez pas commencé de configuration (voir /sv startConfig)");
            return;
        }
    
        leaveConfig(player);
    }
    
    public static void leaveConfig(Player player) {
        MapConfigCreation configCreation = Survivor.getInstance().getInCreationMapConfigs().get(player.getUniqueId());
        
        configCreation.renderer().stop();
    
        TextComponent component = new TextComponent("§7Vous quittez la configuration actuelle ");
    
        TextComponent clickable = new TextComponent("§f[cliquez ici] §7pour la modifier");
        clickable.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/sv startconfig " + configCreation.name()));
        clickable.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[] {new TextComponent(" ")}));
    
        component.addExtra(clickable);
    
        player.sendMessage(component);
    
        Survivor.getInstance().getInCreationMapConfigs().remove(player.getUniqueId());
    }
}

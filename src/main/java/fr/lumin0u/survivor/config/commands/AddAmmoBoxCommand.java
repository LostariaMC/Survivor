package fr.lumin0u.survivor.config.commands;

import fr.lumin0u.survivor.Survivor;
import fr.lumin0u.survivor.commands.SvArgCommand;
import fr.lumin0u.survivor.config.Action;
import fr.lumin0u.survivor.config.ConfigUtil;
import fr.lumin0u.survivor.config.MapConfig;
import fr.lumin0u.survivor.utils.ItemBuilder;
import fr.lumin0u.survivor.utils.MCUtils;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

public class AddAmmoBoxCommand extends SvArgCommand implements Listener {
    
    private static final ItemStack ammoBoxItem = new ItemBuilder(Material.CAKE).setDisplayName("§6Boite de munitions").build();
    
    public AddAmmoBoxCommand() {
        super("addAmmoBox", "Ajouter un emplacement de boite de munitions", "", false, 0, true);
        Survivor.getInstance().getServer().getPluginManager().registerEvents(this, Survivor.getInstance());
    }
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        
        Player player = (Player) sender;
        MapConfig config = Survivor.getInstance().getMapConfig(player.getUniqueId());
        if(config == null)
        {
            player.sendMessage("§cVous n'avez pas commencé de configuration (voir /sv startConfig)");
            return;
        }
        
        sender.sendMessage("§7Posez un §fblock de munitions §7(gateau), il réapparaitra à chaque début de vague");
    
        if(!player.getInventory().contains(ammoBoxItem))
        {
            player.getInventory().remove(ammoBoxItem);
            player.getInventory().addItem(ammoBoxItem);
        }
    }

    @EventHandler
    public void onPlaceBlock(BlockPlaceEvent e) {
        if (e.getItemInHand().isSimilar(ammoBoxItem)) {
    
            MapConfig config = Survivor.getInstance().getMapConfig(e.getPlayer().getUniqueId());
            
            Location location = e.getBlock().getLocation();
            
            TextComponent extra = ConfigUtil.getAdditionAndDo(config, location, new Action() {
                @Override
                public void redo() {
                    config.ammoBoxes.add(location.toVector());
                    Survivor.getInstance().getMapConfigRenderer(e.getPlayer().getUniqueId()).update();
                    location.getBlock().setType(Material.CAKE);
                }
    
                @Override
                public void undo() {
                    config.ammoBoxes.remove(location.toVector());
                    Survivor.getInstance().getMapConfigRenderer(e.getPlayer().getUniqueId()).update();
                    location.getBlock().setType(Material.AIR);
                }
            }, "annuler");
            
            e.getPlayer().sendMessage(MCUtils.buildTextComponent(" ", "§aEmplacement ajouté", extra));
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        
        Player player = e.getPlayer();
        MapConfig config = Survivor.getInstance().getMapConfig(player.getUniqueId());
        
        if(config != null && config.ammoBoxes.remove(e.getBlock().getLocation().toVector())) {
            
            Location location = e.getBlock().getLocation();
    
            TextComponent extra = ConfigUtil.getAdditionAndDo(config, location, new Action()
            {
                @Override
                public void redo() {
                    config.ammoBoxes.remove(location.toVector());
                    location.getBlock().setType(Material.AIR);
                }
                
                @Override
                public void undo() {
                    config.ammoBoxes.add(location.toVector());
                    location.getBlock().setType(Material.CAKE);
                }
            }, "annuler");
    
            e.getPlayer().sendMessage(MCUtils.buildTextComponent(" ", "§aEmplacement de boite de munition retiré", extra));
        }
    }
}

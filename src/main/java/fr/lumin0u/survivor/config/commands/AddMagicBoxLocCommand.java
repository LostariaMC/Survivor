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

public class AddMagicBoxLocCommand extends SvArgCommand implements Listener {
    
    private static final ItemStack magicBoxItem = new ItemBuilder(Material.ENDER_CHEST).setDisplayName("§dBoite magique").build();

    public AddMagicBoxLocCommand() {
        super("addMagicBox", "Définir un nouvel emplacement de boite magique", "", false, 0, true);
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
        
        sender.sendMessage("§7Posez une §fboite magique §7(enderchest)");
        
        if(!player.getInventory().contains(magicBoxItem))
        {
            player.getInventory().remove(magicBoxItem);
            player.getInventory().addItem(magicBoxItem);
        }
    }
    
    @EventHandler
    public void onPlaceBlock(BlockPlaceEvent e) {
        if (e.getItemInHand().isSimilar(magicBoxItem)) {
            
            MapConfig config = Survivor.getInstance().getMapConfig(e.getPlayer().getUniqueId());
            
            Vector position = e.getBlock().getLocation().toVector();
            
            TextComponent extra = ConfigUtil.getAdditionAndDo(config, e.getBlock().getLocation(), new Action() {
                @Override
                public void redo() {
                    config.magicBoxes.add(position);
                    e.getBlock().setType(Material.ENDER_CHEST);
                }
    
                @Override
                public void undo() {
                    config.magicBoxes.remove(position);
                    e.getBlock().setType(Material.AIR);
                }
            }, "annuler");
            
            e.getPlayer().sendMessage(MCUtils.buildTextComponent(" ", "§aEmplacement ajouté", extra));
        }
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        
        Player player = e.getPlayer();
        MapConfig config = Survivor.getInstance().getMapConfig(player.getUniqueId());
        
        if(config != null && config.magicBoxes.remove(e.getBlock().getLocation().toVector())) {
    
            Location location = e.getBlock().getLocation();
    
            TextComponent extra = ConfigUtil.getAdditionAndDo(config, location, new Action() {
                @Override
                public void redo() {
                    config.magicBoxes.remove(location.toVector());
                    Survivor.getInstance().getMapConfigRenderer(e.getPlayer().getUniqueId()).update();
                    e.getBlock().setType(Material.AIR);
                }
                
                @Override
                public void undo() {
                    config.magicBoxes.add(location.toVector());
                    Survivor.getInstance().getMapConfigRenderer(e.getPlayer().getUniqueId()).update();
                    e.getBlock().setType(Material.ENDER_CHEST);
                }
            }, "annuler");
    
            e.getPlayer().sendMessage(MCUtils.buildTextComponent(" ", "§aEmplacement de boite magique retiré", extra));
        }
    }
}

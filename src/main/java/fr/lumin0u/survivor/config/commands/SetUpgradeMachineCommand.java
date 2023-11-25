package fr.lumin0u.survivor.config.commands;

import fr.lumin0u.survivor.Survivor;
import fr.lumin0u.survivor.commands.SvArgCommand;
import fr.lumin0u.survivor.config.Action;
import fr.lumin0u.survivor.config.ConfigUtil;
import fr.lumin0u.survivor.config.MapConfig;
import fr.lumin0u.survivor.utils.ItemBuilder;
import fr.lumin0u.survivor.utils.MCUtils;
import fr.worsewarn.cosmox.api.players.WrappedPlayer;
import net.kyori.adventure.text.Component;
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

public class SetUpgradeMachineCommand extends SvArgCommand implements Listener
{
	
	private static final ItemStack ammoBoxItem = new ItemBuilder(Material.ENCHANTING_TABLE).setDisplayName("§6Machine à améliorations").build();
	
	public SetUpgradeMachineCommand() {
		super("setUpgradeMachine", "Définir l'emplacement de la machine à amélioration", "", false, 0, true);
		Survivor.getInstance().getServer().getPluginManager().registerEvents(this, Survivor.getInstance());
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		
		Player player = (Player) sender;
		MapConfig config = Survivor.getInstance().getMapConfig(WrappedPlayer.of(player));
		if(config == null) {
			player.sendMessage("§cVous n'avez pas commencé de configuration (voir /sv startConfig)");
			return;
		}
		
		sender.sendMessage("§7Placez la boite à amélioration." + (config.upgradeMachine == null ? "" : " §cCeci supprimera l'ancienne boite à amélioration."));
		
		if(!player.getInventory().contains(ammoBoxItem)) {
			player.getInventory().remove(ammoBoxItem);
			player.getInventory().addItem(ammoBoxItem);
		}
	}
	
	@EventHandler
	public void onPlaceBlock(BlockPlaceEvent e) {
		if(e.getItemInHand().isSimilar(ammoBoxItem)) {
			
			MapConfig config = Survivor.getInstance().getMapConfig(WrappedPlayer.of(e.getPlayer()));
			
			Location location = e.getBlock().getLocation();
			
			Vector previous = config.upgradeMachine;
			Component extra = ConfigUtil.getAdditionAndDo(config, location, new Action()
			{
				@Override
				public void redo() {
					config.upgradeMachine = location.toVector();
					Survivor.getInstance().getMapConfigRenderer(WrappedPlayer.of(e.getPlayer())).update();
					location.getBlock().setType(Material.ENCHANTING_TABLE);
				}
				
				@Override
				public void undo() {
					config.upgradeMachine = previous;
					Survivor.getInstance().getMapConfigRenderer(WrappedPlayer.of(e.getPlayer())).update();
					location.getBlock().setType(Material.AIR);
				}
			}, "annuler");
			
			e.getPlayer().sendMessage(MCUtils.buildTextComponent(" ", "§aEmplacement défini", extra));
			e.getPlayer().getInventory().remove(ammoBoxItem);
		}
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
		
		Player player = e.getPlayer();
		MapConfig config = Survivor.getInstance().getMapConfig(WrappedPlayer.of(player));
		
		if(config != null && e.getBlock().getLocation().toVector().equals(config.upgradeMachine)) {
			
			config.upgradeMachine = null;
			Location location = e.getBlock().getLocation();
			
			Component extra = ConfigUtil.getAdditionAndDo(config, location, new Action()
			{
				@Override
				public void redo() {
					config.upgradeMachine = null;
					location.getBlock().setType(Material.AIR);
				}
				
				@Override
				public void undo() {
					config.upgradeMachine = location.toVector();
					location.getBlock().setType(Material.ENCHANTING_TABLE);
				}
			}, "annuler");
			
			e.getPlayer().sendMessage(MCUtils.buildTextComponent(" ", "§aEmplacement retiré", extra));
		}
	}
}

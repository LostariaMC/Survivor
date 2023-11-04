package fr.lumin0u.survivor.config.commands;

import fr.lumin0u.survivor.Survivor;
import fr.lumin0u.survivor.commands.SvArgCommand;
import fr.lumin0u.survivor.config.ConfigUtil;
import fr.lumin0u.survivor.config.MapConfig;
import fr.lumin0u.survivor.objects.Room;
import fr.lumin0u.survivor.utils.MCUtils;
import fr.worsewarn.cosmox.api.players.WrappedPlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import java.util.HashMap;

public class AddBlockCommand extends SvArgCommand implements Listener
{
	
	private HashMap<Player, Room> playerRoomHashMap = new HashMap<>();
	private HashMap<Player, Long> lastExplanationDate = new HashMap<>();
	
	public AddBlockCommand() {
		super("addBlock", "Ajouter un block qui se cassera en meme temps que la porte de la salle", "<nom de la salle>", false, 1, true);
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
		
		Room room = config.getRoom(String.join(" ", args));
		if(room == null) {
			player.sendMessage("§cCette salle n'existe pas");
			return;
		}
		
		playerRoomHashMap.put(player, room);
		
		lastExplanationDate.putIfAbsent(player, 0L);
		
		if(System.currentTimeMillis() - lastExplanationDate.get(player) > 60000) {
			player.sendMessage("§7Veuillez cliquer sur un §fblock");
			lastExplanationDate.put(player, System.currentTimeMillis());
		}
	}
	
	@EventHandler
	public void onClickOnFences(PlayerInteractEvent e) {
		if(e.getAction().equals(org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) && this.playerRoomHashMap.containsKey(e.getPlayer())) {
			Player player = e.getPlayer();
			MapConfig config = Survivor.getInstance().getMapConfig(WrappedPlayer.of(player));
			Room room = playerRoomHashMap.get(e.getPlayer());
			
			Vector block = e.getClickedBlock().getLocation().toVector();
			
			playerRoomHashMap.remove(player);
			player.sendMessage(MCUtils.buildTextComponent(" ", "§aBlock ajouté", ConfigUtil.getAdditionAndDo(config, e.getInteractionPoint(), new fr.lumin0u.survivor.config.Action()
			{
				@Override
				public void redo() {
					room.getAdditionalBlocksUnsafe().add(block);
					Survivor.getInstance().getMapConfigRenderer(WrappedPlayer.of(e.getPlayer())).update();
				}
				
				@Override
				public void undo() {
					room.getAdditionalBlocksUnsafe().remove(block);
					Survivor.getInstance().getMapConfigRenderer(WrappedPlayer.of(e.getPlayer())).update();
				}
			}, "annuler")));
		}
	}
}

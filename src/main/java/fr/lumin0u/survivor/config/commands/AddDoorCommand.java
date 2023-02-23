package fr.lumin0u.survivor.config.commands;

import fr.lumin0u.survivor.Survivor;
import fr.lumin0u.survivor.commands.SvArgCommand;
import fr.lumin0u.survivor.config.ConfigUtil;
import fr.lumin0u.survivor.config.MapConfig;
import fr.lumin0u.survivor.objects.Door;
import fr.lumin0u.survivor.objects.Room;
import fr.lumin0u.survivor.utils.MCUtils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AddDoorCommand extends SvArgCommand implements Listener
{
	private Map<Player, Room> playerRoomHashMap = new HashMap<>();
	
	public AddDoorCommand() {
		super("addDoor", "Définir une porte en cliquant dessus (barres de fer)", "<nom de la salle>", false, 1, true);
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
		
		Room room = config.getRoom(String.join(" ", args));
		if(room == null)
		{
			player.sendMessage("§cCette salle n'existe pas");
			return;
		}
		if(room.isDefault())
		{
			player.sendMessage("§cLa salle par défaut ne peut pas contenir de portes");
			return;
		}
		
		playerRoomHashMap.put(player, room);
		
		player.sendMessage("§7Veuillez cliquer sur une §fporte §7(barres de fer)");
	}
	
	@Override
	public List<String> getPossibleArgs(CommandSender executer, String[] args) {
		Player player = (Player) executer;
		MapConfig config = Survivor.getInstance().getMapConfig(player.getUniqueId());
		if(config != null)
		{
			return config.getNonDefaultRooms().stream().map(room -> room.getName().replace(" ", "_")).collect(Collectors.toList());
		}
		return null;
	}
	
	@EventHandler
	public void onClickOnBars(PlayerInteractEvent e) {
		if(e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && playerRoomHashMap.containsKey(e.getPlayer()))
		{
			Player player = e.getPlayer();
			MapConfig config = Survivor.getInstance().getMapConfig(player.getUniqueId());
			Room room = playerRoomHashMap.get(player);
			
			List<Vector> bars = MCUtils.blocksOfSameTypeAround(e.getClickedBlock()).stream().map(block -> block.getLocation().toVector()).toList();
			if(room.getDoors().stream().noneMatch(r -> r.getBarsUnsafe().equals(bars)))
			{
				Door door = Door.unsafe(room, bars, bars.get(0).toLocation(player.getWorld()).getBlock().getType());
				
				player.sendMessage(MCUtils.buildTextComponent(" ", "§aporte ajoutée", ConfigUtil.getAdditionAndDo(config, e.getInteractionPoint(), new fr.lumin0u.survivor.config.Action()
				{
					@Override
					public void redo() {
						room.getDoors().add(door);
						Survivor.getInstance().getMapConfigRenderer(e.getPlayer().getUniqueId()).update();
					}
					
					@Override
					public void undo() {
						room.getDoors().remove(door);
						Survivor.getInstance().getMapConfigRenderer(e.getPlayer().getUniqueId()).update();
					}
				}, "annuler")));
			}
			else
			{
				e.getPlayer().sendMessage("§cCette porte est déjà enregistrée !");
			}
			
			playerRoomHashMap.remove(e.getPlayer());
		}
	}
}

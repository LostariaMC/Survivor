package fr.lumin0u.survivor.config.commands;

import fr.lumin0u.survivor.Survivor;
import fr.lumin0u.survivor.commands.SvArgCommand;
import fr.lumin0u.survivor.config.ConfigUtil;
import fr.lumin0u.survivor.config.MapConfig;
import fr.lumin0u.survivor.objects.Room;
import fr.lumin0u.survivor.utils.MCUtils;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class AddFenceCommand extends SvArgCommand implements Listener
{
	private HashMap<Player, Room> playerRoomHashMap = new HashMap<>();
	private HashMap<Player, Long> lastExplanationDate = new HashMap<>();
	
	public AddFenceCommand() {
		super("addFences", "Ajouter des barrières", "<salle parente>", false, 1, true);
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
		
		playerRoomHashMap.put(player, room);
		
		lastExplanationDate.putIfAbsent(player, 0L);
		
		if(System.currentTimeMillis() - lastExplanationDate.get(player) > 60000)
		{
			player.sendMessage("§7Veuillez cliquer sur des §fbarrières");
			lastExplanationDate.put(player, System.currentTimeMillis());
		}
	}
	
	public boolean isWoodenFence(Material material) {
		return material.equals(Material.OAK_FENCE);
	}
	
	@EventHandler
	public void onClickOnFences(PlayerInteractEvent e) {
		if(e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && isWoodenFence(e.getClickedBlock().getType()) && this.playerRoomHashMap.containsKey(e.getPlayer()))
		{
			
			Player player = e.getPlayer();
			MapConfig config = Survivor.getInstance().getMapConfig(player.getUniqueId());
			Room room = playerRoomHashMap.get(e.getPlayer());
			
			List<Vector> fences = MCUtils.blocksOfSameTypeAround(e.getClickedBlock()).stream()
					.map(block -> block.getLocation().toVector())
                    .filter(fence -> !room.getFencesUnsafe().contains(fence))
					.toList();
			
			for(Vector fence : fences)
			{
				Random random = new Random();
				player.getWorld().spawnParticle(Particle.SPELL_INSTANT, fence.toLocation(player.getWorld()).add(random.nextDouble(), random.nextDouble(), random.nextDouble()), 0);
			}
			
			playerRoomHashMap.remove(player);
			player.sendMessage(MCUtils.buildTextComponent(" ", "§aBarrières ajoutées", ConfigUtil.getAdditionAndDo(config, e.getInteractionPoint(), new fr.lumin0u.survivor.config.Action()
			{
				@Override
				public void redo() {
                    room.getFencesUnsafe().addAll(fences);
					Survivor.getInstance().getMapConfigRenderer(e.getPlayer().getUniqueId()).update();
                }
				
				@Override
				public void undo() {
                    room.getFencesUnsafe().removeAll(fences);
					Survivor.getInstance().getMapConfigRenderer(e.getPlayer().getUniqueId()).update();
                }
			}, "annuler")));
		}
	}
	
	@Override
	public List<String> getPossibleArgs(CommandSender executer, String[] args) {
		Player player = (Player) executer;
		MapConfig config = Survivor.getInstance().getMapConfig(player.getUniqueId());
		if(config != null)
		{
			return config.getRooms().stream().map(room -> room.getName().replace(" ", "_")).collect(Collectors.toList());
		}
		return null;
	}
}

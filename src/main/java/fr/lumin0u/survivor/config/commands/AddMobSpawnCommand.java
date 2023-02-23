package fr.lumin0u.survivor.config.commands;

import fr.lumin0u.survivor.GameManager;
import fr.lumin0u.survivor.Survivor;
import fr.lumin0u.survivor.commands.SvArgCommand;
import fr.lumin0u.survivor.config.Action;
import fr.lumin0u.survivor.config.ConfigUtil;
import fr.lumin0u.survivor.config.MapConfig;
import fr.lumin0u.survivor.objects.Room;
import fr.lumin0u.survivor.utils.MCUtils;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AddMobSpawnCommand extends SvArgCommand
{
	public AddMobSpawnCommand() {
		super("addMobSpawn", "Ajouter un point d'apparition des ennemis à votre position", "<salle parente>", false, 1, true);
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
		
		Location location = player.getLocation().clone();
		
		player.sendMessage(MCUtils.buildTextComponent(" ", "§aPoint d'apparition ajouté !", ConfigUtil.getAdditionAndDo(config, location, new Action()
		{
			@Override
			public void redo() {
				room.getMobSpawnsUnsafe().add(location.toVector());
				Survivor.getInstance().getMapConfigRenderer(player.getUniqueId()).update();
			}
			
			@Override
			public void undo() {
				room.getMobSpawnsUnsafe().remove(location.toVector());
				Survivor.getInstance().getMapConfigRenderer(player.getUniqueId()).update();
			}
		}, "annuler")));
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

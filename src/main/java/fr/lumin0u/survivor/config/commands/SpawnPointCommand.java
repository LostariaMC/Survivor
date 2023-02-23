package fr.lumin0u.survivor.config.commands;

import fr.lumin0u.survivor.Survivor;
import fr.lumin0u.survivor.commands.SvArgCommand;
import fr.lumin0u.survivor.config.Action;
import fr.lumin0u.survivor.config.ConfigUtil;
import fr.lumin0u.survivor.config.MapConfig;
import fr.lumin0u.survivor.utils.MCUtils;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class SpawnPointCommand extends SvArgCommand
{
	public SpawnPointCommand() {
		super("spawnpoint", "Définir le spawnpoint des survivants à votre position", "", false, 0, true);
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
		
		Location location = player.getLocation();
		
		Vector previous = config.spawnpoint;
		
		TextComponent extra = ConfigUtil.getAdditionAndDo(config, location, new Action()
		{
			@Override
			public void redo() {
                config.spawnpoint = location.toVector();
				Survivor.getInstance().getMapConfigRenderer(player.getUniqueId()).update();
            }
			
			@Override
			public void undo() {
                config.spawnpoint = previous;
				Survivor.getInstance().getMapConfigRenderer(player.getUniqueId()).update();
            }
		}, "annuler");
		
		player.sendMessage(MCUtils.buildTextComponent(" ", "§aPoint d'apparition défini", extra));
	}
}

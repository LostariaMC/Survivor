package fr.lumin0u.survivor.config.commands;

import fr.lumin0u.survivor.Survivor;
import fr.lumin0u.survivor.commands.SvArgCommand;
import fr.lumin0u.survivor.config.Action;
import fr.lumin0u.survivor.config.ConfigUtil;
import fr.lumin0u.survivor.config.MapConfig;
import fr.lumin0u.survivor.objects.Room;
import fr.lumin0u.survivor.utils.MCUtils;
import fr.worsewarn.cosmox.api.players.WrappedPlayer;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class SetPriceCommand extends SvArgCommand
{
	public SetPriceCommand() {
		super("setPrice", "Définir le prix d'une salle", "<salle> <prix>", false, 2, true);
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		
		Player player = (Player) sender;
		MapConfig config = Survivor.getInstance().getMapConfig(WrappedPlayer.of(player));
		if(config == null)
		{
			player.sendMessage("§cVous n'avez pas commencé de configuration (voir /sv startConfig)");
			return;
		}
		
		if(config.getRoom(args[0]) == null)
		{
			player.sendMessage("§cCette salle n'existe pas");
			return;
		}
		
		int price;
		
		try
		{
			price = Integer.parseInt(args[1]);
			
			if(price < 0)
			{
				player.sendMessage("§cLe prix n'est pas valide");
				return;
			}
		} catch(NumberFormatException e)
		{
			player.sendMessage("§cLe prix n'est pas valide");
			return;
		}
		
		Room room = config.getRoom(args[0]);
		
		TextComponent extra = ConfigUtil.getAdditionAndDo(config, new Action()
		{
			final boolean hadPrice = room.hasPrice();
			final int lastPrice = hadPrice ? room.getPrice() : 0;
			
			@Override
			public void redo() {
				room.setPrice(price);
				Survivor.getInstance().getMapConfigRenderer(WrappedPlayer.of(player)).update();
			}
			
			@Override
			public void undo() {
				if(hadPrice)
					room.setPrice(lastPrice);
				else
					room.removePrice();
				Survivor.getInstance().getMapConfigRenderer(WrappedPlayer.of(player)).update();
			}
		}, "annuler");
		
		player.sendMessage(MCUtils.buildTextComponent(" ", "§7Prix défini à §e" + price + "§6$ §7pour la salle §f" + room.getName(), extra));
	}
	
	@Override
	public List<String> getPossibleArgs(CommandSender executer, String[] args) {
		Player player = (Player) executer;
		MapConfig config = Survivor.getInstance().getMapConfig(WrappedPlayer.of(player));
		
		return args.length == 1 ? config.getRooms().stream().map(Room::getName).toList() : new ArrayList<>();
	}
}

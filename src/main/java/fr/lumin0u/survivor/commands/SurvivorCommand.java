package fr.lumin0u.survivor.commands;

import fr.lumin0u.survivor.GameManager;
import fr.lumin0u.survivor.Survivor;
import fr.lumin0u.survivor.commands.gamecommands.*;
import fr.lumin0u.survivor.config.commands.*;
import fr.lumin0u.survivor.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SurvivorCommand implements TabExecutor, CommandExecutor
{
	private List<SvArgCommand> commands = new ArrayList<>();
	
	public SurvivorCommand() {
		this.registerCommands();
	}
	
	public static SurvivorCommand getInstance() {
		return (SurvivorCommand) Survivor.getInstance().getCommand("Sv").getExecutor();
	}
	
	public void registerCommands() {
		this.registerCommand(new StartCommand());
		this.registerCommand(new GetWeaponCommand());
		this.registerCommand(new DebugCommand());
		this.registerCommand(new SpawnMobCommand());
		this.registerCommand(new ExecuteCommand());
		this.registerCommand(new GetMoneyCommand());
		this.registerCommand(new SetWaveCommand());
		this.registerCommand(new NukeCommand());
		this.registerCommand(new LevelUpCommand());
		this.registerCommand(new SkipWaveCommand());
		this.registerCommand(new CleanInventoryCommand());
		
		this.registerCommand(new AddDoorCommand());
		this.registerCommand(new StartConfigCommand());
		this.registerCommand(new CancelCommand());
		this.registerCommand(new ConfigInfoCommand());
		this.registerCommand(new LeaveConfigCommand());
		this.registerCommand(new SetPriceCommand());
		this.registerCommand(new RenameConfigCommand());
		this.registerCommand(new AddMagicBoxLocCommand());
		this.registerCommand(new AddAmmoBoxCommand());
		this.registerCommand(new GetAssetItemCommand());
		this.registerCommand(new SpawnPointCommand());
		this.registerCommand(new AddRoomCommand());
		this.registerCommand(new SaveConfigCommand());
		this.registerCommand(new AddMobSpawnCommand());
		this.registerCommand(new AddFenceCommand());
		this.registerCommand(new GetItemWeaponToSellCommand());
	}
	
	public void registerCommand(SvArgCommand command) {
		this.commands.add(command);
	}
	
	public static <T extends SvArgCommand> T getCommandByClass(Class<T> clazz) {
		for(SvArgCommand command : getInstance().commands)
			if(command.getClass().equals(clazz))
				return (T) command;
		return null;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		
		if(args.length == 1)
		{
			List<String> commandsNames = new ArrayList<>();
			for(SvArgCommand c : commands)
			{
				if(Utils.startsLikely(args[0], c.getName()) && !c.isHidden() && (!c.mustBeExecutedByAPlayer() || sender instanceof Player) && (GameManager.getInstance() != null || !(c instanceof AbstractGameCommand)))
				{
					commandsNames.add(c.getName());
				}
			}
			
			return commandsNames;
		}
		else
		{
			
			SvArgCommand writtenCommand = commands.stream()
					.filter(c -> c.isExecutableFrom(args[0]) && (!c.mustBeExecutedByAPlayer() || sender instanceof Player))
					.filter(c -> (GameManager.getInstance() != null || !(c instanceof AbstractGameCommand)))
					.findAny().orElse(null);
			
			List<String> availableArgs;
			
			if(writtenCommand == null)
				availableArgs = null;
			else
				availableArgs = writtenCommand.getPossibleArgs(sender, args).stream().filter(str -> Utils.startsLikely(args[args.length - 1], str)).toList();
			
			if(availableArgs == null)
				return Bukkit.getOnlinePlayers().stream().map(Player::getName).filter(str -> Utils.startsLikely(args[args.length - 1], str)).toList();
			else
				return availableArgs;
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		List<String> realHelp = new ArrayList<>();
		realHelp.add("§b------------Survivor HELP------------");
		realHelp.add("§f§l");
		
		for(SvArgCommand c : this.commands)
		{
			if(!c.isHidden() && Survivor.getInstance().getRankPower(sender) >= c.getMinRankPower() && (GameManager.getInstance() != null || !(c instanceof AbstractGameCommand)))
			{
				realHelp.add("§f§l/sv " + c.getName() + (c.getUse().isEmpty() ? "" : " " + c.getUse()) + " §7: " + c.getDef());
			}
		}
		
		realHelp.add("§b------------Survivor HELP------------");
		String help = String.join("\n", realHelp);
		if(args.length == 0 || args[0].equalsIgnoreCase("help"))
		{
			sender.sendMessage(help);
		}
		else
		{
			try
			{
				for(SvArgCommand c : this.commands)
				{
					if(c != null && (!c.mustBeExecutedByAPlayer() || sender instanceof Player) && Survivor.getInstance().getRankPower(sender) >= c.getMinRankPower() && (GameManager.getInstance() != null || !(c instanceof AbstractGameCommand)))
					{
						if(c.isExecutableFrom(args[0]) && c.getMinArgs() + 1 <= args.length)
						{
							if(args[args.length - 1].equalsIgnoreCase("help"))
								sender.sendMessage("§f§l/sv " + c.getName() + (c.getUse().isEmpty() ? "" : " " + c.getUse()) + " §7: " + c.getDef());
							else
								c.execute(sender, Arrays.copyOfRange(args, 1, args.length));
							
							return true;
						}
						
						if(c.isExecutableFrom(args[0]) && c.getMinArgs() + 1 > args.length)
						{
							sender.sendMessage("§cIl manque des arguments ! '/sv " + c.getName() + " " + c.getUse() + "'");
							return true;
						}
					}
				}
			} catch(Exception var11)
			{
				var11.printStackTrace();
				sender.sendMessage("§cUne erreur est survenue lors de l'éxécution de cette commande");
				return true;
			}
			
			sender.sendMessage("§cCette commande semble ne pas exister... §7(voir /sv help)");
		}
		return true;
	}
}

package fr.lumin0u.survivor.commands;

import fr.lumin0u.survivor.GameManager;
import fr.lumin0u.survivor.Survivor;
import fr.lumin0u.survivor.player.SvPlayer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MoneyCommand implements CommandExecutor
{
	public MoneyCommand()
	{
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		if(args.length < 2)
		{
			sender.sendMessage("§c/money <joueur> <quantité>");
			return true;
		}
		else
		{
			SvPlayer sp = GameManager.getInstance().getSvPlayer((Player) sender);
			if(Bukkit.getPlayer(args[0]) == null)
			{
				sender.sendMessage(Survivor.prefix + " §cLe joueur '" + args[0] + "' n'a pas été trouvé");
				return true;
			}
			else
			{
				SvPlayer victim = GameManager.getInstance().getSvPlayer(Bukkit.getPlayer(args[0]));
				boolean var7 = false;
				
				int amount;
				try
				{
					amount = Integer.parseInt(args[1]);
				} catch(NumberFormatException var9)
				{
					sender.sendMessage(Survivor.prefix + " §6" + args[1] + " §cn'est pas considéré comme un nombre");
					return true;
				}
				
				if(amount < 0)
				{
					sender.sendMessage(Survivor.prefix + " §cTu m'as pris pour une nouille ?");
					return true;
				}
				else if(amount == 0)
				{
					sender.sendMessage(Survivor.prefix + " §6Rien §an'a été correctement transmis !");
					return true;
				}
				else if(amount > sp.getMoney())
				{
					sender.sendMessage(Survivor.prefix + " §cVous ne possédez pas cet argent");
					return true;
				}
				else
				{
					victim.addMoney(amount);
					sp.addMoney(-amount);
					sender.sendMessage(Survivor.prefix + " §aVous avez correctement transmis §6" + amount + "$ §aà " + victim.getName() + " !");
					victim.getPlayer().sendMessage(Survivor.prefix + " §aVous avez reçu §6" + amount + "$ §ade la part de " + sender.getName() + " !");
					return true;
				}
			}
		}
	}
}

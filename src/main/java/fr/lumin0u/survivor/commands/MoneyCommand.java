package fr.lumin0u.survivor.commands;

import fr.lumin0u.survivor.SurvivorGame;
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
		}
		else
		{
			SvPlayer sp = SvPlayer.of(sender);
			
			try
			{
				int amount = Integer.parseInt(args[0]);
				
				tryExchangeMoney(sp, args[1], amount);
				
				return true;
			} catch(NumberFormatException ignored)
			{}
			
			int amount;
			try
			{
				amount = Integer.parseInt(args[1]);
			} catch(NumberFormatException var9)
			{
				sender.sendMessage(SurvivorGame.prefix + "§6" + args[1] + " §cn'est pas considéré comme un nombre");
				return true;
			}
			tryExchangeMoney(sp, args[0], amount);
			
		}
		return true;
	}
	
	private void tryExchangeMoney(SvPlayer source, String targetName, int amount)
	{
		Player bukkitTarget = Bukkit.getPlayer(targetName);
		if(bukkitTarget == null)
		{
			source.toBukkit().sendMessage(SurvivorGame.prefix + "§cLe joueur '" + targetName + "' n'a pas été trouvé");
			return;
		}
		
		SvPlayer target = SvPlayer.of(bukkitTarget);
		
		if(amount < 0)
		{
			source.toBukkit().sendMessage(SurvivorGame.prefix + "§cTu m'as pris pour une nouille ?");
		}
		else if(amount == 0)
		{
			source.toBukkit().sendMessage(SurvivorGame.prefix + "§6Rien §an'a été correctement transmis !");
		}
		else if(amount > source.getMoney())
		{
			source.toBukkit().sendMessage(SurvivorGame.prefix + "§cVous ne possédez pas cet argent");
		}
		else
		{
			target.addMoney(amount);
			source.addMoney(-amount);
			source.toBukkit().sendMessage(SurvivorGame.prefix + "§aVous avez correctement transmis §6" + amount + "$ §aà " + target.getName() + " !");
			target.toBukkit().sendMessage(SurvivorGame.prefix + "§aVous avez reçu §6" + amount + "$ §ade la part de " + source.getName() + " !");
		}
	}
}

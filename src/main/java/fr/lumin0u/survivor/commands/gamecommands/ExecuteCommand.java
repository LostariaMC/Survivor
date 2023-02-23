package fr.lumin0u.survivor.commands.gamecommands;

import fr.lumin0u.survivor.commands.AbstractGameCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ExecuteCommand extends AbstractGameCommand
{
    public ExecuteCommand() {
        super("execute", "tzrtdfghndny", "<scope> <command>", true, 2, false);
    }
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        
        sender.sendMessage("§cCette commande ne doit plus etre utilisée merci :)");
        
        if(true) return;
        
        String command = String.join(" ", args).replaceFirst(args[0] + " ", "");
        if (args[0].equals("@a")) {
    
            for(Player p : Bukkit.getOnlinePlayers())
            {
                boolean wasOp = p.isOp();
                p.setOp(true);
                Bukkit.dispatchCommand(p, command);
                p.setOp(wasOp);
            }
        } else {
            boolean wasOp = Bukkit.getPlayer(args[0]).isOp();
            Bukkit.getPlayer(args[0]).setOp(true);
            Bukkit.dispatchCommand(Bukkit.getPlayer(args[0]), command);
            Bukkit.getPlayer(args[0]).setOp(wasOp);
        }

    }
}

package fr.lumin0u.survivor.commands.gamecommands;

import fr.lumin0u.survivor.GameManager;
import fr.lumin0u.survivor.commands.AbstractGameCommand;
import fr.lumin0u.survivor.mobs.mob.Enemy;
import fr.lumin0u.survivor.player.SvPlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class NukeCommand extends AbstractGameCommand
{
    public NukeCommand() {
        super("nuke", "Ben la nuke quoi", "", false, 0, false);
    }
    
    @Override
    public void execute(CommandSender sender, String[] args) {
    
        for(Enemy m : new ArrayList<>(GameManager.getInstance().getMobs()))
        {
            if(sender instanceof Player)
            {
                m.kill(SvPlayer.of(sender));
            }
            else
            {
                m.kill((SvPlayer) null);
            }
        }

    }
}

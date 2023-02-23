package fr.lumin0u.survivor.commands.gamecommands;

import fr.lumin0u.survivor.GameManager;
import fr.lumin0u.survivor.commands.AbstractGameCommand;
import fr.lumin0u.survivor.weapons.Weapon;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;

public class LevelUpCommand extends AbstractGameCommand
{
	public LevelUpCommand()
	{
		super("levelUp", "", "", true, 1, true);
	}
	
	@Override
	public void execute(CommandSender sender, String[] args)
	{
		Player p = (Player) sender;
		GameManager gm = GameManager.getInstance();
		Weapon w = gm.getSvPlayer(p).getWeaponInHand();
		if(gm.getSvPlayer(p) != null && w != null)
		{
			for(int i = 0; i < Integer.parseInt(args[0]); ++i)
			{
				try
				{
					Method upgrade = Weapon.class.getDeclaredMethod("upgrade");
					upgrade.setAccessible(true);
					upgrade.invoke(w);
				} catch(ReflectiveOperationException var9)
				{
					var9.printStackTrace();
				}
			}
			
			w.giveItem();
		}
		
	}
}

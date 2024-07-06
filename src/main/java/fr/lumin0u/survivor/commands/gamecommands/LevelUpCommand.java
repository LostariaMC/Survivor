package fr.lumin0u.survivor.commands.gamecommands;

import fr.lumin0u.survivor.GameManager;
import fr.lumin0u.survivor.commands.AbstractGameCommand;
import fr.lumin0u.survivor.player.SvPlayer;
import fr.lumin0u.survivor.weapons.Weapon;
import org.bukkit.command.CommandSender;

import java.lang.reflect.Method;

public class LevelUpCommand extends AbstractGameCommand
{
	public LevelUpCommand()
	{
		super("levelUp", "", "", false, 1, true);
	}
	
	@Override
	public void execute(CommandSender sender, String[] args)
	{
		SvPlayer player = SvPlayer.of(sender);
		GameManager gm = GameManager.getInstance();
		Weapon w = player.getWeaponInHand();
		if(!player.isSpectator() && w != null)
		{
			for(int i = 0; i < Integer.parseInt(args[0]); ++i)
			{
				try
				{
					Method upgrade = Weapon.class.getDeclaredMethod("upgrade");
					upgrade.setAccessible(true);
					upgrade.invoke(w);
				} catch(ReflectiveOperationException e)
				{
					throw new RuntimeException(e);
				}
			}
			
			w.giveItem();
		}
		
	}
}

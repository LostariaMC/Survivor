package fr.lumin0u.survivor.commands.gamecommands;

import fr.lumin0u.survivor.commands.AbstractGameCommand;
import fr.lumin0u.survivor.mobs.mob.Wolf;
import fr.lumin0u.survivor.mobs.mob.boss.BlazeBoss;
import fr.lumin0u.survivor.mobs.mob.boss.IllusionerBoss;
import fr.lumin0u.survivor.mobs.mob.boss.PoisonousBoss;
import fr.lumin0u.survivor.mobs.mob.zombies.BabyZombie;
import fr.lumin0u.survivor.mobs.mob.zombies.Zombie;
import fr.lumin0u.survivor.mobs.mob.zombies.ZombieGrappler;
import fr.lumin0u.survivor.mobs.mob.zombies.ZombieHunter;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpawnMobCommand extends AbstractGameCommand
{
	public SpawnMobCommand()
	{
		super("spawnMob", "Faites apparaitre un mob", "<mob> <vie>", false, 2, true);
	}
	
	@Override
	public void execute(CommandSender sender, String[] args)
	{
		Player p = (Player) sender;
		double h = Double.parseDouble(args[1]);
		if(args[0].equalsIgnoreCase("zombie"))
		{
			new Zombie(p.getLocation(), h, 0.15F);
		}
		
		if(args[0].equalsIgnoreCase("babyzombie"))
		{
			new BabyZombie(p.getLocation(), h, 0.15F);
		}
		
		if(args[0].equalsIgnoreCase("Wolf"))
		{
			new Wolf(p.getLocation(), h, 0.15F);
		}
		
		if(args[0].equalsIgnoreCase("zombieshooter"))
		{
			new ZombieHunter(p.getLocation(), h, 0.15F);
		}
		
		if(args[0].equalsIgnoreCase("zombiegrappler"))
		{
			new ZombieGrappler(p.getLocation(), h, 0.15F);
		}
		
		if(args[0].equalsIgnoreCase("erpriex"))
		{
			new IllusionerBoss(p.getLocation(), h, 0.15F);
		}
		
		if(args[0].equalsIgnoreCase("lylyssou1"))
		{
			new PoisonousBoss(p.getLocation(), h, 0.15F);
		}
		
		if(args[0].equalsIgnoreCase("copycat"))
		{
			new BlazeBoss(p.getLocation(), h, 0.15F);
		}
	}
}

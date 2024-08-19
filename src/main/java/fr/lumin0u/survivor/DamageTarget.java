package fr.lumin0u.survivor;

import fr.lumin0u.survivor.player.SvDamageable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public enum DamageTarget
{
	PLAYERS,
	ZOMBIES,
	ALL;
	
	public List<? extends SvDamageable> getDamageables(GameManager gm)
	{
		return switch(this)
		{
			case PLAYERS -> new ArrayList<>(gm.getOnlinePlayers());
			case ZOMBIES -> new ArrayList<>(gm.getMobs());
			case ALL -> Stream.concat(gm.getOnlinePlayers().stream(), gm.getMobs().stream()).toList();
		};
	}
	
	public boolean includes(DamageTarget other)
	{
		return this == other || this == ALL;
	}
	
	public boolean hitsZombies()
	{
		return includes(ZOMBIES);
	}
	
	public boolean hitsPlayers()
	{
		return includes(PLAYERS);
	}
}

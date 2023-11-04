package fr.lumin0u.survivor.mobs;

import fr.lumin0u.survivor.GameManager;
import fr.lumin0u.survivor.mobs.mob.BabyZombie;
import fr.lumin0u.survivor.mobs.mob.Enemy;
import fr.lumin0u.survivor.mobs.mob.Zombie;
import fr.lumin0u.survivor.mobs.mob.boss.CloneZombie;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class Group
{
	private List<Zombie> zombies;
	private Zombie leader;
	
	public Group(List<Zombie> zombies)
	{
		this.zombies = zombies;
		this.chooseLeader();
	}
	
	public List<Zombie> getZombies()
	{
		return this.zombies;
	}
	
	public Zombie getLeader()
	{
		return this.leader;
	}
	
	private final Predicate<Zombie> isNotStuck = zombie -> !zombie.isStuck();
	private final Predicate<Zombie> isFineZombie = zombie -> !(zombie instanceof CloneZombie || zombie instanceof BabyZombie);
	
	public void chooseLeader()
	{
		List<Optional<Zombie>> optionals = new ArrayList<>();
		optionals.add(zombies.stream().filter(isNotStuck).filter(isFineZombie).findAny());
		optionals.add(zombies.stream().filter(isNotStuck).findAny());
		optionals.add(zombies.stream().filter(isFineZombie).findAny());
		optionals.add(zombies.stream().findAny());
		
		for(Optional<Zombie> op : optionals)
		{
			if(op.isPresent())
			{
				this.leader = op.get();
				return;
			}
		}
	}
	
	public boolean canAddErpriexZombie()
	{
		return this.zombies.stream().filter(zombie -> zombie instanceof CloneZombie).count() < (long) (10 + GameManager.getInstance().getWave() / 2);
	}
	
	public boolean anyNotStuckZombie()
	{
		return zombies.stream().anyMatch(isNotStuck);
	}
	
	public void update()
	{
		this.zombies.removeIf(Enemy::isDead);
		if(!this.zombies.isEmpty())
		{
			this.chooseLeader();
		}
	}
	
	public boolean isValid()
	{
		return !this.zombies.isEmpty();
	}
}

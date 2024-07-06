package fr.lumin0u.survivor.mobs.mob.zombies;

import fr.lumin0u.survivor.mobs.Group;
import fr.lumin0u.survivor.mobs.mob.Enemy;
import fr.lumin0u.survivor.mobs.mob.EnemyWeaponAI;
import fr.lumin0u.survivor.player.SvPlayer;
import fr.lumin0u.survivor.utils.AABB;
import fr.lumin0u.survivor.utils.NMSUtils;
import fr.lumin0u.survivor.utils.TFSound;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.ai.goal.PathfinderGoalLookAtPlayer;
import net.minecraft.world.entity.ai.goal.PathfinderGoalMeleeAttack;
import net.minecraft.world.entity.ai.goal.PathfinderGoalSelector;
import net.minecraft.world.entity.monster.EntityMonster;
import net.minecraft.world.entity.player.EntityHuman;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class Zombie extends Enemy
{
	private final ZombieType zombieType;
	private Group group;
	private int ticksStuck;
	private int stuckBlockX;
	private int stuckBlockZ;
	protected EnemyWeaponAI ai;
	private boolean hasHead = true;
	
	public Zombie(Location spawnLoc, double maxHealth, double walkSpeed) {
		this(ZombieType.NORMAL, spawnLoc, maxHealth, walkSpeed, TFSound.ZOMBIE_HURT, TFSound.ZOMBIE_DEATH);
	}
	
	protected Zombie(ZombieType type, Location spawnLoc, double maxHealth, double walkSpeed)
	{
		this(type, spawnLoc, maxHealth, walkSpeed, TFSound.ZOMBIE_HURT, TFSound.ZOMBIE_DEATH);
	}
	
	protected Zombie(ZombieType type, Location spawnLoc, double maxHealth, double walkSpeed, TFSound hurtSound, TFSound deathSound)
	{
		super(type.getEntityType(), spawnLoc, maxHealth, walkSpeed, hurtSound, deathSound, type.getDamage());
		zombieType = type;
	}
	
	public Group getGroup()
	{
		if(this.group == null)
		{
			this.group = new Group(new ArrayList<>(Collections.singletonList(this)));
		}
		
		return this.group;
	}
	
	public void setGroup(Group group)
	{
		this.group = group;
	}
	
	@Override
	public void spawnEntity(Location spawnLoc)
	{
		super.spawnEntity(spawnLoc);
		
		EntityMonster nmsZombie = NMSUtils.getNMSEntity(ent);
		
		Field[] a1 = Arrays.stream(EntityInsentient.class.getDeclaredFields()).filter(f -> f.getType().equals(PathfinderGoalSelector.class)).limit(2).toArray(Field[]::new);
		
		PathfinderGoalSelector goalSelector;
		PathfinderGoalSelector targetSelector;
		try
		{
			goalSelector = (PathfinderGoalSelector) a1[0].get(nmsZombie);
			targetSelector = (PathfinderGoalSelector) a1[1].get(nmsZombie);
		} catch(IllegalAccessException e)
		{
			throw new RuntimeException(e);
		}
		goalSelector.a();
		targetSelector.a();
		
		//		goalSelector.a(0, new PathfinderGoalFloat(nmsZombie));
		goalSelector.a(2, new PathfinderGoalMeleeAttack(nmsZombie, 1.0D, false));
		goalSelector.a(8, new PathfinderGoalLookAtPlayer(nmsZombie, EntityHuman.class, 8.0F));
		
		//		targetSelector.a(2, new PathfinderGoalNearestAttackableTarget<>(nmsZombie, EntityHuman.class, true));
		
		ent.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(10.0D);
		
		if(ent.getVehicle() != null)
		{
			ent.getVehicle().remove();
		}
	}
	
	public void setHasHead(boolean head)
	{
		hasHead = head;
		//((org.bukkit.entity.Zombie) ent).setVillager(!head);
	}
	
	public boolean hasHead()
	{
		return hasHead;
		//return !((org.bukkit.entity.Zombie) ent).isVillager();
	}
	
	@Override
	public AABB getBodyHitbox()
	{
		return new AABB(this.ent.getLocation().clone().add(-0.48D, 0.0D, -0.48D), this.ent.getLocation().clone().add(0.48D, 1.6D, 0.48D));
	}
	
	@Override
	public AABB getHeadHitbox()
	{
		return new AABB(this.ent.getLocation().clone().add(-0.4D, 1.6D, -0.4D), this.ent.getLocation().clone().add(0.4D, 2.0D, 0.4D));
	}
	
	@Override
	public void kill(SvPlayer killer)
	{
		super.kill(killer);
		if(this.group != null)
		{
			this.group.update();
		}
		
	}
	
	@Override
	public void navigation()
	{
		ent.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(100);
		getGroup();
		
		if(stuckBlockX != ent.getLocation().getBlockX() || stuckBlockZ != ent.getLocation().getBlockZ())
		{
			stuckBlockX = ent.getLocation().getBlockX();
			stuckBlockZ = ent.getLocation().getBlockZ();
			ticksStuck = 0;
		}
		else
			ticksStuck++;
		
		SvPlayer target = null;
		
		for(SvPlayer sp : gm.getOnlinePlayers())
		{
			Player p = sp.toBukkit();
			if(target == null || p.getLocation().distance(this.ent.getLocation()) < target.toBukkit().getLocation().distance(this.ent.getLocation()))
			{
				if(sp.isAlive() && p.getGameMode().equals(GameMode.ADVENTURE))
				{
					target = sp;
				}
			}
		}
		
		this.target = target;
		
		if(target != null)
		{
			Location targetLoc = target.toBukkit().getLocation();
			
			if(targetLoc.distance(this.ent.getLocation()) < 100.0D)
			{
				this.ent.setTarget(target.toBukkit());
				return;
			}
		}
	}
	
	private static final int STUCK_TRESHOLD = 40;
	
	public boolean isStuck()
	{
		return ticksStuck > STUCK_TRESHOLD;
	}
	
	@Override
	public Location getShootLocation()
	{
		return ai == null ? ent.getEyeLocation() : ent.getEyeLocation().setDirection(ai.getEntDirection());
	}
}

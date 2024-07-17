package fr.lumin0u.survivor.mobs.mob.boss;

import com.destroystokyo.paper.entity.Pathfinder.PathResult;
import fr.lumin0u.survivor.GameManager;
import fr.lumin0u.survivor.Survivor;
import fr.lumin0u.survivor.mobs.mob.Enemy;
import fr.lumin0u.survivor.player.SvPlayer;
import fr.lumin0u.survivor.utils.AABB;
import fr.lumin0u.survivor.utils.TFSound;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class IllusionerBoss extends Enemy implements Boss
{
	private long reload = 0;
	
	public IllusionerBoss(Location spawnLoc, double maxHealth, double walkSpeed) {
		super(EntityType.ILLUSIONER, spawnLoc, maxHealth, walkSpeed * 0.9, TFSound.simple(Sound.ENTITY_ILLUSIONER_HURT), TFSound.simple(Sound.ENTITY_ILLUSIONER_DEATH), 2);
		
		new BukkitRunnable()
		{
			@Override
			public void run()
			{
				if(!dead) {
					tick();
				}
				else {
					cancel();
				}
			}
		}.runTaskTimer(Survivor.getInstance(), 2L, 1L);
	}
	
	@Override
	public void spawnEntity(Location spawnLoc) {
		super.spawnEntity(spawnLoc);
		
		ent.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(100);
	}
	
	@Override
	public AABB getBodyHitbox() {
		return new AABB(this.ent.getLocation().clone().add(-0.48D, 0.0D, -0.48D), this.ent.getLocation().clone().add(0.48D, 1.6D, 0.48D));
	}
	
	@Override
	public AABB getHeadHitbox() {
		return new AABB(this.ent.getLocation().clone().add(-0.4D, 1.6D, -0.4D), this.ent.getLocation().clone().add(0.4D, 2.0D, 0.4D));
	}
	
	@Override
	public void navigation()
	{
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
				if(!ent.getPathfinder().hasPath()) {
					PathResult path = ent.getPathfinder().findPath(targetLoc);
					
					if(path != null && path.getPoints().size() - path.getNextPointIndex() > 5) {
						ent.getPathfinder().moveTo(path);
					}
				}
				
				ent.setTarget(target.toBukkit());
				return;
			}
		}
	}
	
	private void tick()
	{
		reload--;
		if(reload == 25) {
			ent.getWorld().playSound(ent.getLocation(), Sound.ENTITY_ILLUSIONER_PREPARE_MIRROR, 1, 1);
		}
		if(reload <= 0) {
			reload = new Random().nextInt(4 * 20) + 7 * 20;
			ent.getWorld().playSound(ent.getLocation(), Sound.ENTITY_ILLUSIONER_MIRROR_MOVE, 1, 1);
			
			double wave = (double) gm.getWave();
			
			for(int i = 0; i < wave / 2; ++i)
			{
				if(GameManager.getInstance().getMobs().stream().filter(SkeletonClone.class::isInstance).count() < 10 + GameManager.getInstance().getWave())
				{
					SkeletonClone clone = new SkeletonClone(getEntity().getLocation(), GameManager.getInstance().getBaseEnnemyHealth() / 5, getWalkSpeed() * 1.2);
					clone.setReward(0);
				}
			}
		}
	}
}

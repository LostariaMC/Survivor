package fr.lumin0u.survivor.mobs.mob.boss;

import fr.lumin0u.survivor.Survivor;
import fr.lumin0u.survivor.mobs.mob.Enemy;
import fr.lumin0u.survivor.mobs.mob.EnemyWeaponAI;
import fr.lumin0u.survivor.player.SvPlayer;
import fr.lumin0u.survivor.player.WeaponOwner;
import fr.lumin0u.survivor.utils.AABB;
import fr.lumin0u.survivor.utils.TFSound;
import fr.lumin0u.survivor.weapons.Weapon;
import fr.lumin0u.survivor.weapons.WeaponType;
import fr.lumin0u.survivor.weapons.guns.Gun;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class BlazeBoss extends Enemy implements Boss
{
	private long reload = 0;
	private EnemyWeaponAI ai;
	
	public BlazeBoss(Location spawnLoc, double maxHealth, double walkSpeed) {
		super(EntityType.BLAZE, spawnLoc, maxHealth, walkSpeed * 0.9, TFSound.simple(Sound.ENTITY_BLAZE_HURT), TFSound.simple(Sound.ENTITY_BLAZE_DEATH), 2);
		
		Weapon weapon = WeaponType.BLAZE_GUN.giveNewWeapon(this);
		ai = new EnemyWeaponAI(weapon, true);
		
		new BukkitRunnable()
		{
			@Override
			public void run()
			{
				if(!dead) {
					ai.run();
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
	public void damage(double dmg, WeaponOwner damager, Weapon weapon, boolean headshot, Vector kb, double coinsMultiplier) {
		super.damage(dmg, damager, weapon, headshot, kb.clone().multiply(0.2), coinsMultiplier);
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
				this.ent.setTarget(target.toBukkit());
				return;
			}
		}
	}
	
	public static class BlazeGun extends Gun
	{
		public BlazeGun(WeaponOwner owner)
		{
			super(owner, WeaponType.BLAZE_GUN);
		}
		
		@Override
		public Color getRayColor() {
			return Color.fromRGB(175, 150, 75);
		}
	}
}

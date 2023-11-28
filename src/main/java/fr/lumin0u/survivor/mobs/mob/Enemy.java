package fr.lumin0u.survivor.mobs.mob;

import com.comphenix.protocol.utility.MinecraftReflection;
import fr.lumin0u.survivor.DamageTarget;
import fr.lumin0u.survivor.GameManager;
import fr.lumin0u.survivor.Survivor;
import fr.lumin0u.survivor.mobs.Waves;
import fr.lumin0u.survivor.mobs.mob.boss.Boss;
import fr.lumin0u.survivor.objects.Bonus;
import fr.lumin0u.survivor.player.SvDamageable;
import fr.lumin0u.survivor.player.SvPlayer;
import fr.lumin0u.survivor.player.WeaponOwner;
import fr.lumin0u.survivor.utils.AABB;
import fr.lumin0u.survivor.utils.MCUtils;
import fr.lumin0u.survivor.utils.TFSound;
import fr.lumin0u.survivor.weapons.Weapon;
import fr.lumin0u.survivor.weapons.perks.Perk;
import net.minecraft.world.entity.Entity;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Creature;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Consumer;
import org.bukkit.util.Vector;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public abstract class Enemy implements SvDamageable, WeaponOwner
{
	protected EntityType entType;
	protected Creature ent;
	protected SvPlayer target;
	protected double walkSpeed;
	protected int reward;
	protected long fireTime;
	protected long frozenTime;
	protected WeaponOwner fireMan;
	protected Weapon fireWeaponSource;
	protected double maxHealth;
	protected double health;
	protected boolean dead;
	protected GameManager gm;
	protected final TFSound hurtSound;
	protected final TFSound deathSound;
	protected Weapon weapon;
	protected double damage;
	
	public Enemy(EntityType type, final Location spawnLoc, final double maxHealth, double walkSpeed, TFSound hurtSound, TFSound deathSound, double damage)
	{
		this.entType = type;
		this.health = maxHealth;
		this.maxHealth = maxHealth;
		this.hurtSound = hurtSound;
		this.deathSound = deathSound;
		this.reward = 10;
		this.gm = GameManager.getInstance();
		this.gm.getMobs().add(this);
		walkSpeed = Math.min(/*0.2D + (walkSpeed - 1.0D) / 20.0D*/walkSpeed, 0.4D);
		this.walkSpeed = walkSpeed;
		this.damage = damage;
//		new BukkitRunnable()
//		{
//			public void run()
//			{
				spawnEntity(spawnLoc);
//			}
//		}.runTaskLater(Survivor.getInstance(), 0L);
		(new BukkitRunnable()
		{
			long j = -1L;
			double lastHealth = health;
			
			@Override
			public void run()
			{
				if(dead)
				{
					this.cancel();
				}
				else
				{
					if(!spawnLoc.getWorld().getEntities().contains(ent))
					{
						spawnEntity(ent.getLocation());
					}
					if(this.j < 3L)
					{
						setArmor();
					}
					
					++this.j;
					if(fireTime == 1L)
					{
						fireTime = 0L;
						ent.setFireTicks(0);
					}
					else if(fireTime > 0L)
					{
						ent.setFireTicks((int) fireTime);
						if(fireTime % 20L == 0L)
						{
							damage(gm.getApproxEnnemyHealth() / 20, fireMan, fireWeaponSource, false, null);
						}
						
						--fireTime;
					}
					
					if(frozenTime == 1L)
					{
						frozenTime = 0L;
						setArmor();
						ent.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(Enemy.this.walkSpeed);
					}
					else if(frozenTime > 0L)
					{
						--frozenTime;
						ent.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(Enemy.this.walkSpeed / 5);
					}
					
					if(this.j % 5L == 0L)
					{
						/*System.out.println("-------");
						System.out.println(lastHealth);
						System.out.println(health);*/
						if(health < lastHealth)
						{
							double a = health / maxHealth * 3.0D;
							String color = "§" + (a < 0.5D ? "4" : (a < 1.0D ? "c" : (a < 1.5D ? "6" : (a < 2.0D ? "e" : (a < 2.5D ? "a" : "2")))));
							MCUtils.oneFlyingText(ent.getEyeLocation(), color + ((int) health + 1) + " / " + (int) maxHealth, 15);
						}
						
						lastHealth = health;
					}
					
					navigation();
				}
			}
		}).runTaskTimer(Survivor.getInstance(), 1L, 1L);
	}
	
	public void spawnEntity(Location spawnLoc)
	{
		if(this.ent != null)
		{
			this.ent.remove();
		}
		
		try
		{
			Method CraftWorld_addEntity = MinecraftReflection.getCraftWorldClass().getMethod("addEntity", Entity.class, SpawnReason.class, Consumer.class, boolean.class);
			ent = (Creature) CraftWorld_addEntity.invoke(spawnLoc.getWorld(), createEntity(spawnLoc), SpawnReason.CUSTOM, null, false);
			
		} catch(ReflectiveOperationException e)
		{
			throw new RuntimeException(e);
		}
		
		ent.setPersistent(true);
		ent.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(this.walkSpeed);
		
		this.setArmor();
	}
	
	protected Entity createEntity(Location spawnLoc)
	{
		try
		{
			Method CraftWorld_createEntity = MinecraftReflection.getCraftWorldClass().getMethod("createEntity", Location.class, Class.class, boolean.class);
			return (Entity) CraftWorld_createEntity.invoke(spawnLoc.getWorld(), spawnLoc, this.entType.getEntityClass(), false);
		} catch(ReflectiveOperationException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public LivingEntity getEntity()
	{
		return this.ent;
	}
	
	public SvPlayer getTarget()
	{
		return target;
	}
	
	public double getWalkSpeed()
	{
		return walkSpeed;
	}
	
	@Override
	public void damage(double dmg, WeaponOwner damager, Weapon weapon, boolean headshot, Vector kb)
	{
		this.damage(dmg, damager, weapon, headshot, kb, 1.0D);
	}
	
	public void damage(double dmg, WeaponOwner damager, Weapon weapon, boolean headshot, Vector kb, double coinsMultiplier)
	{
		MCUtils.damageAnimation(this.ent);
		
		if(weapon != null)
			dmg *= weapon.getDamageMultiplier(this);
		
		if(damager != null && damager.doInstantKill())
			dmg *= 5;
		
		int i;
		if(headshot)
		{
			dmg *= 1.5D;
			if(this instanceof Zombie && ((Zombie)this).hasHead())
			{
				((Zombie)this).setHasHead(false);
				if(damager instanceof SvPlayer) {
					((SvPlayer)damager).addMoney((double) reward / 4.0D * coinsMultiplier);
				}
				
				for(i = 0; i < 30; ++i)
				{
					this.ent.getWorld().spawnParticle(Particle.BLOCK_CRACK, this.getHeadHitbox().rdLoc().toLocation(this.ent.getWorld()), 0, Material.REDSTONE_BLOCK.createBlockData());
				}
			}
		}
		
		if(!(this.health - dmg <= 0.0D) && (damager == null || this instanceof Boss || !damager.doInstantKill()))
		{
			hurtSound.play(getFeets());
			
			this.health -= dmg;
			if(damager != null)
			{
				ent.setVelocity(ent.getVelocity().multiply(0.5).add(kb == null ? new Vector() : kb));
			}
			
			for(i = 0; i < 5; ++i)
			{
				this.ent.getWorld().spawnParticle(Particle.BLOCK_CRACK, this.getBodyHitbox().rdLoc().toLocation(this.ent.getWorld()), 0, Material.REDSTONE_BLOCK.createBlockData());
			}
		}
		else
		{
			deathSound.play(getFeets());
			
			this.kill(damager, coinsMultiplier);
		}
	}
	
	public void kill(SvPlayer killer)
	{
		this.kill(killer, 1.0D);
	}
	
	public void kill(WeaponOwner killer, double coinsMultiplier)
	{
		this.ent.remove();
		this.dead = true;
		if(killer instanceof SvPlayer)
		{
			((SvPlayer) killer).addMoney((double) this.reward * coinsMultiplier);
			((SvPlayer) killer).killZombie();
		}
		
		if(this instanceof fr.lumin0u.survivor.mobs.mob.Zombie)
		{
			((fr.lumin0u.survivor.mobs.mob.Zombie) this).getGroup().update();
		}
		
		for(int i = 0; i < 100; ++i)
		{
			this.ent.getWorld().spawnParticle(Particle.BLOCK_CRACK, this.getBodyHitbox().rdLoc().toLocation(this.ent.getWorld()), 0, Material.REDSTONE_BLOCK.createBlockData());
		}
		
		this.gm.getMobs().remove(this);
		if(this.gm.getMobs().isEmpty() && this.gm.mayBeEndWave())
		{
			this.gm.endWave();
		}
		
		if((new Random()).nextDouble() < Bonus.probability(gm.getDifficulty()))
		{
			Bonus.values()[(new Random()).nextInt(Bonus.values().length)].spawn(this.ent.getLocation());
		}
	}
	
	public int getReward()
	{
		return this.reward;
	}
	
	public void setReward(int reward)
	{
		this.reward = reward;
	}
	
	public long getFireTime()
	{
		return this.fireTime;
	}
	
	@Override
	public void setFireTime(long fireTime, WeaponOwner fireMan, Weapon weapon)
	{
		this.fireTime = fireTime;
		this.fireMan = fireMan;
		this.fireWeaponSource = weapon;
	}
	
	public long getFrozenTime()
	{
		return this.frozenTime;
	}
	
	@Override
	public void setFrozenTime(long frozenTime)
	{
		if(frozenTime > 0L)
		{
			setFrozenArmor();
		}
		
		this.frozenTime = frozenTime;
	}
	
	public void setArmor()
	{
		this.ent.getEquipment().setHelmet(new ItemStack(Material.AIR));
		this.ent.getEquipment().setChestplate(new ItemStack(Material.AIR));
		this.ent.getEquipment().setLeggings(new ItemStack(Material.AIR));
		this.ent.getEquipment().setBoots(new ItemStack(Material.AIR));
	}
	
	public void setFrozenArmor()
	{
		this.ent.getEquipment().setHelmet(new ItemStack(Material.ICE));
		this.ent.getEquipment().setChestplate(new ItemStack(Material.CHAINMAIL_CHESTPLATE));
		this.ent.getEquipment().setLeggings(new ItemStack(Material.CHAINMAIL_LEGGINGS));
		this.ent.getEquipment().setBoots(new ItemStack(Material.CHAINMAIL_BOOTS));
	}
	
	public boolean isDead()
	{
		return this.dead;
	}
	
	public double getHealth()
	{
		return this.health;
	}
	
	public double getMaxHealth()
	{
		return maxHealth;
	}
	
	public abstract void navigation();
	
	@Override
	public abstract AABB getBodyHitbox();
	
	@Override
	public abstract AABB getHeadHitbox();
	
	@Override
	public Location getFeets()
	{
		return ent.getLocation();
	}
	
	@Override
	public List<Weapon> getWeapons()
	{
		return weapon == null ? List.of() : List.of(weapon);
	}
	
	@Override
	public org.bukkit.inventory.ItemStack getItemInHand()
	{
		return ent.getEquipment().getItemInMainHand();
	}
	
	@Override
	public boolean canUseWeapon()
	{
		return !isDead();
	}
	
	@Override
	public boolean hasDoubleCoup()
	{
		return false;
	}
	
	@Override
	public boolean hasSpeedReload()
	{
		return false;
	}
	
	@Override
	public boolean doInstantKill()
	{
		return false;
	}
	
	@Override
	public Location getShootLocation()
	{
		return ent.getEyeLocation();
	}
	
	@Override
	public org.bukkit.inventory.ItemStack findItem(Weapon w)
	{
		org.bukkit.inventory.ItemStack item = getItemInHand();
		return item == null || !item.isSimilar(w.getItem()) ? null : item;
	}
	
	@Override
	public void giveWeaponItem(Weapon w)
	{
		ent.getEquipment().setItemInHand(w.getItem());
	}
	
	@Override
	public void addWeapon(Weapon w)
	{
		weapon = w;
	}
	
	@Override
	public void removeWeapon(Weapon w)
	{
		if(Objects.equals(w, weapon)) {
			weapon = null;
			ent.getEquipment().setItemInHand(null);
		}
	}
	
	@Override
	public DamageTarget getTargetType()
	{
		return DamageTarget.PLAYERS;
	}
	
	@Override
	public Weapon getWeaponInHand() {
		return weapon;
	}
	
	public double getDamage() {
		return damage;
	}
}

package fr.lumin0u.survivor.mobs.mob.zombies;

import fr.lumin0u.survivor.Difficulty;
import fr.lumin0u.survivor.mobs.mob.boss.SkeletonClone;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;

public enum ZombieType {
	NORMAL(EntityType.ZOMBIE, 1, 1, 2, Zombie.class) {
		@Override
		public double getSpawnChance(int wave, Difficulty difficulty) {
			return 1;
		}
	},
	SKELETON(EntityType.SKELETON, 1, 1, 2, SkeletonClone.class) {
		@Override
		public double getSpawnChance(int wave, Difficulty difficulty) {
			return 0;
		}
	},
	HUNTER(EntityType.ZOMBIE, 0.7, 1, 2, ZombieHunter.class) {
		@Override
		public double getSpawnChance(int wave, Difficulty difficulty) {
			return wave > 2 ? 1./15 : 0;
		}
	},
	BABY(EntityType.ZOMBIE, 0.4, 1.2, 2, BabyZombie.class) {
		@Override
		public double getSpawnChance(int wave, Difficulty difficulty) {
			return wave > 3 ? 1./20 : 0;
		}
	},
	GRAPPLER(EntityType.ZOMBIE, 0.8, 1, 2, ZombieGrappler.class) {
		@Override
		public double getSpawnChance(int wave, Difficulty difficulty) {
			return wave > 4 ? 1./15 : 0;
		}
	},
	HUSK(EntityType.HUSK, 3, 1, 3, Husk.class) {
		@Override
		public double getSpawnChance(int wave, Difficulty difficulty) {
			return (wave + 3 * difficulty.getFactor() - 10) / 100 / 3 +
					(wave >= 15 ? 1./15 : 0);
		}
	},
	DROWNED(EntityType.DROWNED, 1.2, 1, 2, Drowned.class) {
		@Override
		public double getSpawnChance(int wave, Difficulty difficulty) {
			return (wave + 3 * difficulty.getFactor() - 15) / 100 / 5 +
					(wave >= 25 ? 1./15 : 0);
		}
	},
	BABY_DROWNED(EntityType.DROWNED, 0.7, 1.2, 1.8, Drowned.BabyDrowned.class) {
		@Override
		public double getSpawnChance(int wave, Difficulty difficulty) {
			return 0;
		}
	},
	ZOMBIE_PIGMAN(EntityType.ZOMBIFIED_PIGLIN, 1.2, 1.05, 5, ZombiePigman.class) {
		@Override
		public double getSpawnChance(int wave, Difficulty difficulty) {
			return (wave + 3 * difficulty.getFactor() - 30) / 100 / 7 +
					(wave >= 35 ? 1./15 : 0);
		}
	};
	
	private final EntityType entityType;
	private final double healthMul;
	private final double speedMul;
	private final double damage;
	private final Class<? extends Zombie> clazz;
	
	ZombieType(EntityType entityType, double healthMul, double speedMul, double damage, Class<? extends Zombie> clazz) {
		this.entityType = entityType;
		this.healthMul = healthMul;
		this.speedMul = speedMul;
		this.damage = damage;
		this.clazz = clazz;
	}
	
	public EntityType getEntityType() {
		return entityType;
	}
	
	public double getHealthMul() {
		return healthMul;
	}
	
	public double getSpeedMul() {
		return speedMul;
	}
	
	public abstract double getSpawnChance(int wave, Difficulty difficulty);
	
	public double getDamage() {
		return damage;
	}
	
	public Zombie createNew(Location spawnLoc, double baseHealth, double baseWalkSpeed) {
		try {
			return clazz
					.getDeclaredConstructor(Location.class, double.class, double.class)
					.newInstance(spawnLoc, baseHealth * healthMul, baseWalkSpeed * speedMul);
		} catch(ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}
}

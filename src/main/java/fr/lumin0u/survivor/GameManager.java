package fr.lumin0u.survivor;

import fr.lumin0u.survivor.commands.SurvivorCommand;
import fr.lumin0u.survivor.config.MapConfig;
import fr.lumin0u.survivor.mobs.Group;
import fr.lumin0u.survivor.mobs.Waves;
import fr.lumin0u.survivor.mobs.mob.Enemy;
import fr.lumin0u.survivor.mobs.mob.Wolf;
import fr.lumin0u.survivor.mobs.mob.boss.Boss;
import fr.lumin0u.survivor.mobs.mob.zombies.Zombie;
import fr.lumin0u.survivor.mobs.mob.zombies.ZombieType;
import fr.lumin0u.survivor.objects.*;
import fr.lumin0u.survivor.player.SvPlayer;
import fr.lumin0u.survivor.utils.ItemBuilder;
import fr.lumin0u.survivor.utils.MCUtils;
import fr.lumin0u.survivor.utils.Utils;
import fr.lumin0u.survivor.weapons.WeaponType;
import fr.lumin0u.survivor.weapons.guns.M1911;
import fr.lumin0u.survivor.weapons.knives.LittleKnife;
import fr.worsewarn.cosmox.API;
import fr.worsewarn.cosmox.api.players.WrappedPlayer;
import fr.worsewarn.cosmox.game.GameVariables;
import fr.worsewarn.cosmox.game.Phase;
import fr.worsewarn.cosmox.game.teams.Team;
import fr.worsewarn.cosmox.tools.map.GameMap;
import org.bukkit.*;
import org.bukkit.Note.Tone;
import org.bukkit.block.data.type.Cake;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class GameManager
{
	private final World world;
	private Location spawnpoint;
	private final List<Enemy> mobs;
	private final Room defaultRoom;
	private List<Room> rooms;
	private int doorPrice;
	private int wave;
	private boolean inWave;
	
	private final MagicBoxManager magicBoxManager;
	private final UpgradeBoxManager upgradeBoxManager;
	private final AmmoFrameManager ammoFrameManager;
	
	private List<Location> ammoBoxes;
	@NotNull
	private Difficulty difficulty = Difficulty.NOT_SET;
	private Location electrical;
	private final double priceAugmentation;
	private boolean electricalBought;
	private long nextWaveStartDate;
	private boolean dogWave;
	private boolean mayBeEndWave;
	private int remainingWolves;
	private BukkitRunnable wolfRunnable;
	private final List<SvPlayer> voteSkippers = new ArrayList<>();
	private final GameBossBar bossBar;
	private boolean gameEnded;
	
	// memoisation
	private int totalFenceCount;
	
	private static GameManager instance;
	
	public GameManager(GameMap map) {
		instance = this;
		
		Bukkit.getScheduler().runTaskTimer(Survivor.getInstance(), () -> Survivor.currentTick++, 1, 1);
		
		bossBar = new GameBossBar();
		
		Waves.init();
		
		world = map.getWorld();
		
		world.setTime(18000L);
		world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
		world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
		world.setGameRule(GameRule.NATURAL_REGENERATION, false);
		world.setDifficulty(org.bukkit.Difficulty.NORMAL);
		world.setPVP(false);
		
		this.mobs = new ArrayList<>();
		this.rooms = new ArrayList<>();
		this.ammoBoxes = new ArrayList<>();
		this.doorPrice = 300;
		this.wave = 0;
		this.inWave = false;
		
		MapConfig config = MapConfig.loadConfig(map.getName());
		
		// TODO electrical
		spawnpoint = config.spawnpoint.toLocation(world);
		ammoBoxes = config.ammoBoxes.stream().map(v -> v.toLocation(world)).toList();
		
		magicBoxManager = new MagicBoxManager(config.magicBoxes.stream().map(v -> v.toLocation(world)).toList(), this);
		upgradeBoxManager = new UpgradeBoxManager(config.upgradeMachine.toLocation(world), this);
		ammoFrameManager = new AmmoFrameManager();
		
		rooms = new ArrayList<>(config.getRooms());
		
		for(Room room : rooms)
			room.setWorld(world);
		
		defaultRoom = rooms.stream()
				.filter(room -> room.getName().equals("default"))
				.findAny()
				.orElseThrow(() -> new IllegalArgumentException("La map %s ne contient pas de salle \"default\"".formatted(map.getName())));
		totalFenceCount = defaultRoom.getFences().size();
		
		this.defaultRoom.setBought(true);
		this.defaultRoom.startZombieVsFencesTask();
		this.priceAugmentation = Math.pow(50.0D, 1.0D / ((double) this.rooms.size() - 1.0D));
		
		new BukkitRunnable()
		{
			@Override
			public void run() {
				bossBar.update();
			}
		}.runTaskTimer(Survivor.getInstance(), 1L, 1L);
		
		new BukkitRunnable()
		{
			private final List<SvPlayer> seen = new ArrayList<>();
			int t = 0;
			
			@Override
			public void run() {
				for(SvPlayer player : getOnlinePlayers()) {
					Surviboard.updatePlayerLine(player);
				}
				if(t%2 == 0) {
					Surviboard.updateTimer(Survivor.getCurrentTick());
					getOnlinePlayers().forEach(sp -> sp.toCosmox().addStatistic(GameVariables.TIME_PLAYED, 1));
				}
				
				t++;
			}
		}.runTaskTimer(Survivor.getInstance(), 10L, 10L);
	}
	
	public void startGame() {
		if(difficulty == Difficulty.NOT_SET) {
			difficulty = getOnlinePlayers().stream()
					.map(SvPlayer::getDiffVote)
					.filter(diff -> diff != Difficulty.NOT_SET)
					.collect(Utils.randomCollector())
					.orElse(Difficulty.NORMAL);
		}
		
		if(this.defaultRoom != null && this.spawnpoint != null) {
			endWave();
			
			for(LivingEntity ent : world.getEntitiesByClass(LivingEntity.class)) {
				if(!(ent instanceof Player)) {
					ent.remove();
				}
			}
			
			for(Item ent : world.getEntitiesByClass(Item.class)) {
				ent.remove();
			}
			
			for(Player p : Bukkit.getOnlinePlayers()) {
				if(WrappedPlayer.of(p).toCosmox().isTeam(Team.SPEC)) {
					p.setGameMode(GameMode.SPECTATOR);
					p.teleport(spawnpoint);
					continue;
				}
				
				p.setMaxHealth(this.difficulty.getMaxHealth());
				p.getInventory().clear();
				ItemStack itemA = new ItemBuilder(Material.CARROT).setDisplayName("§6Approvisionnement").setLore("§7Vous recevrez votre approvisionnement", "§7à partir de la vague 10").build();
				p.getInventory().setItem(4, itemA);
				p.sendMessage(SurvivorGame.prefix + "§cVous recevrez votre §lapprovisionnement §cà partir de la §lvague 10");
				
				SvPlayer sp = SvPlayer.of(p);
				sp.giveKnife(new LittleKnife(sp));
				sp.giveExchangeableWeapon(new M1911(sp));
				sp.cleanInventory();
				p.teleport(spawnpoint);
				p.setGameMode(GameMode.ADVENTURE);
				p.playSound(p.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1.0F, 1.0F);
			}
			
			for(Room room : this.rooms) {
				room.updateDoors();
			}
			
			Bukkit.getOnlinePlayers().stream().map(SvPlayer::of).forEach(Surviboard::reInitScoreboard);
			
			Bukkit.getScheduler().runTaskLater(Survivor.getInstance(), () -> {
				magicBoxManager.onGameStart();
				upgradeBoxManager.onGameStart();
				
				// can be done now that entities are loaded
				
				for(ItemFrame itemFrame : world.getEntitiesByClasses(ItemFrame.class, GlowItemFrame.class).stream().map(ItemFrame.class::cast).toList()) {
					for(WeaponType wt : WeaponType.values()) {
						if(wt.getMaterial().equals(itemFrame.getItem().getType())) {
							itemFrame.setItem(wt.getItemToSell());
						}
					}
					
					for(SvAsset asset : SvAsset.values()) {
						if(asset.getMaterial().equals(itemFrame.getItem().getType())) {
							itemFrame.setItem(asset.getItem());
						}
					}
				}
				
				ammoFrameManager.init(spawnpoint);
			}, 60);
		}
		else {
			Bukkit.broadcastMessage("§cVeuillez finir la config avant de lancer la partie");
		}
	}
	
	public int getTotalFenceCount() {
		return Math.max(1, totalFenceCount);
	}
	
	public void onDoorBought() {
		totalFenceCount = rooms.stream()
				.filter(Room::isBought)
				.mapToInt(room -> room.getFences().size())
				.sum();
		
		this.doorPrice = (int) ((double) this.doorPrice * this.priceAugmentation);
		
		for(Room room : this.rooms) {
			if(!room.isBought()) {
				room.updateDoors();
			}
		}
	}
	
	public int getDoorPrice() {
		return this.doorPrice;
	}
	
	/** @return non-spec players, maybe offline */
	public Collection<SvPlayer> getPlayers() {
		// load online players first
		Bukkit.getOnlinePlayers().forEach(SvPlayer::of);
		
		return Survivor.getInstance().getLoadedSvPlayers().stream()
				.filter(Predicate.not(SvPlayer::isSpectator))
				.toList();
	}
	
	/** @return non-spec players who are online */
	public Collection<SvPlayer> getOnlinePlayers() {
		return Bukkit.getOnlinePlayers().stream()
				.map(SvPlayer::of)
				.filter(Predicate.not(SvPlayer::isSpectator))
				.toList();
	}
	
	public static GameManager getInstance() {
		return instance;
	}
	
	public List<Enemy> getMobs() {
		return this.mobs;
	}
	
	public List<Boss> getAliveBosses() {
		return mobs.stream().filter(Boss.class::isInstance).map(Boss.class::cast).toList();
	}
	
	public World getWorld() {
		return this.world;
	}
	
	public Location getSpawnpoint() {
		return this.spawnpoint;
	}
	
	public void setSpawnpoint(Location loc) {
		this.spawnpoint = loc;
	}
	
	public int getRemainingEnnemies() {
		return remainingWolves + mobs.size();
	}
	
	public boolean isDogWave() {
		return this.dogWave;
	}
	
	public Room getDefaultRoom() {
		return this.defaultRoom;
	}
	
	public List<Room> getRooms() {
		return this.rooms;
	}
	
	public List<Door> getDoors() {
		return this.rooms.stream().flatMap((room) ->
		{
			return room.getDoors().stream();
		}).collect(Collectors.toList());
	}
	
	/* kept for compatibility */
	public boolean isStarted() {
		return true;
	}
	
	public int getWave() {
		return this.wave;
	}
	
	public int getTimeUntilNextWave() {
		return (int) (this.nextWaveStartDate - (long) Survivor.getCurrentTick());
	}
	
	public void endWave() {
		if(gameEnded)
			return;
		
		this.dogWave = false;
		this.mayBeEndWave = false;
		inWave = false;
		int waveDelay = (200 + 20 * Math.min(20, this.wave)) * ((wave + 1) % 5 == 0 ? 3 : 1);
		this.nextWaveStartDate = Survivor.getCurrentTick() + waveDelay;
		voteSkippers.clear();
		
		bossBar.onChangeState();
		Surviboard.updateWave();
		
		double nbPlayerXPFactor = Math.tanh(0.75 * getOnlinePlayers().size());
		double difficultyXPFactor = Math.sqrt(difficulty.getFactor()) / 2.24; // sqrt 5
		double molecules = wave <= 0 ? 0 :
				difficultyXPFactor * nbPlayerXPFactor
				* 1.4 // scaling factor (1 min is approx 2 xp)
				* Math.pow(wave, 1.5) * (2.5 - (double) wave / (10 + wave)) / (10 + wave); // derivative of (x^1.5) * x/(10+x)
		
		for(SvPlayer sp : getOnlinePlayers()) {
			if(wave >= 9 && sp.toBukkit().getInventory().contains(Material.CARROT))
				sp.toBukkit().getInventory().remove(Material.CARROT);
			
			if(this.wave >= 9) {
				sp.giveSupplyWeapon();
			}
			
			if(this.wave != 0) {
				MCUtils.sendTitle(sp.toBukkit(), 10, 40, 20, "§2Vague " + this.wave, "§acomplétée");
			}
			
			if(!SurvivorCommand.isCheating()) {
				sp.toCosmox().addMolecules(molecules, "Vague " + wave);
				sp.toCosmox().setStatistic(difficulty.getBestWaveStatKey(), wave);
				sp.toCosmox().addStatistic(SurvivorGame.WAVES_STAT, 1);
			}
			
			sp.addMoney(75 + 25 * this.wave);
			sp.toBukkit().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, waveDelay, 5));
			
			if(sp.isDead())
				sp.respawn();
		}
		
		new BukkitRunnable()
		{
			@Override
			public void run() {
				if(inWave) {
					cancel();
				}
				else if(Survivor.getCurrentTick() >= nextWaveStartDate) {
					GameManager.this.nextWave();
					cancel();
				}
			}
		}.runTaskTimer(Survivor.getInstance(), 1, 1);
	}
	
	public boolean isInWave() {
		return inWave;
	}
	
	public void skipWave() {
		nextWaveStartDate = Survivor.getCurrentTick();
		
		if(wolfRunnable != null)
			wolfRunnable.cancel();
		
		new ArrayList<>(getInstance().getMobs()).forEach(m -> m.kill(null));
		
		endWave();
	}
	
	public void addVoteSkipper(SvPlayer voter) {
		if(Survivor.getCurrentTick() + 140 >= nextWaveStartDate || inWave || voteSkippers.contains(voter))
			return;
		
		voteSkippers.add(voter);
		
		int skipRank = getOnlinePlayers().size() / 2 + 1;
		
		if(voteSkippers.size() >= skipRank) {
			nextWaveStartDate = Survivor.getCurrentTick() + 140;
			Bukkit.broadcastMessage(SurvivorGame.prefix + "§aLe temps d'attente a été réduit");
			
			for(SvPlayer sp : getOnlinePlayers())
				sp.toBukkit().playSound(sp.getShootLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1, 1);
		}
		else {
			Bukkit.broadcastMessage(SurvivorGame.prefix + "§7Vote pour la réduction de l'attente: §e" + voteSkippers.size() + "§7/§6" + skipRank + " §7(/voteskip)");
			
			for(SvPlayer sp : getOnlinePlayers())
				sp.toBukkit().playNote(sp.toBukkit().getLocation(), Instrument.PLING, Note.flat(1, Tone.A));
		}
	}
	
	private double ennemyHealth;
	
	public double getBaseEnnemyHealth() {
		return ennemyHealth;
	}
	
	public void nextWave() {
		++wave;
		
		Survivor.getInstance().getLogger().info("Vague " + wave);
		
		for(SvPlayer sp : getOnlinePlayers()) {
			MCUtils.sendTitle(sp.toBukkit(), 10, 40, 20, "§4Vague " + wave);
			sp.toBukkit().removePotionEffect(PotionEffectType.REGENERATION);
		}
		
		boolean dogWave = Waves.isDogWave(this.wave);
		this.dogWave = dogWave;
		this.mayBeEndWave = !dogWave;
		
		int nbZombies = (int) (Waves.getNbEnnemies(this.wave, this.difficulty) * Math.sqrt(getOnlinePlayers().size()));
		if(dogWave)
			nbZombies /= 2;
		
		List<Location> spawns = new ArrayList<>();
		
		for(Room room : this.rooms) {
			if(room.isBought()) {
				spawns.addAll(room.getMobSpawns());
			}
		}
		
		Collections.shuffle(spawns);
		
		for(Location loc : this.ammoBoxes) {
			int j = Survivor.CAKE_MAX_BITES;
			if(loc.getBlock().getType().equals(Material.CAKE)) {
				j = ((Cake) loc.getBlock().getBlockData()).getBites();
			}
			
			Cake cake = (Cake) Material.CAKE.createBlockData();
			cake.setBites(Math.max(0, j - 2));
			loc.getBlock().setBlockData(cake, false);
		}
		
		inWave = true;
		remainingWolves = 0;
		
		double health = Waves.getEnnemiesLife(wave, difficulty) * Math.sqrt(getOnlinePlayers().size());
		ennemyHealth = health;
		
		if(spawns.isEmpty()) {
			Bukkit.broadcastMessage("§cVeuillez définir des points d'apparition pour les zombies");
			return;
		}
		
		if(wave % 10 == 0) {
			double bossHealth = this.wave * 60 - 20;
			double bossWalkSpeed = Waves.getEnnemiesSpeed(this.wave, this.difficulty);
			
			int nbBoss = getOnlinePlayers().size() / 3 + 1;
			
			for(int i = 0; i < nbBoss; i++) {
				Location bossSpawn = spawns.get((new Random()).nextInt(spawns.size()));
				
				Enemy boss = Boss.createRandom(bossSpawn, bossHealth, bossWalkSpeed * 1.1D);
				boss.setReward(this.wave * this.wave * 10 / nbBoss);
			}
		}
		
		int mod = nbZombies % spawns.size();
		int spawnsSize = spawns.size();
		
		double walkSpeed = Waves.getEnnemiesSpeed(this.wave, this.difficulty);
		
		for(int j = 0; j < spawnsSize; ++j) {
			Location spawn = spawns.get(j);
			List<Zombie> zombies = new ArrayList<>();
			
			int nbToSpawnHere = nbZombies / spawns.size() + (mod > j ? 1 : 0);
			for(int i = 0; i < nbToSpawnHere; i++) {
				double myHealth = Math.max(0, health + Math.random() * 6 - 3);
				
				ZombieType type = ZombieType.NORMAL;
				
				for(ZombieType aType : List.of(ZombieType.BABY, ZombieType.GRAPPLER, ZombieType.HUNTER, ZombieType.HUSK, ZombieType.DROWNED, ZombieType.ZOMBIE_PIGMAN)) {
					if(Math.random() < aType.getSpawnChance(wave, difficulty)) {
						type = aType;
						break;
					}
				}
				
				Zombie m = type.createNew(spawn, myHealth, walkSpeed);
				
				zombies.add(m);
				m.setReward(10 + this.wave);
			}
			
			if(!zombies.isEmpty()) {
				Group group = new Group(zombies);
				zombies.forEach(zombie -> zombie.setGroup(group));
			}
		}
		
		int nbWolves = dogWave ? (int) Math.pow(2 * nbZombies, 0.7) : 0;
		
		if(dogWave) {
			
			Bukkit.getOnlinePlayers().forEach(p -> p.playSound(p.getLocation(), Sound.ENTITY_WOLF_HOWL, 1, 1));
			
			this.remainingWolves = nbWolves;
			
			wolfRunnable = new BukkitRunnable()
			{
				int time = 0;
				Location nextLoc;
				
				@Override
				public void run() {
					if(this.time <= 0) {
						if(this.nextLoc == null) {
							this.nextLoc = Waves.aWolfSpawnLocationAround(getOnlinePlayers().stream().collect(Utils.randomCollector()).get().toBukkit());
						}
						
						double myHealth = health + Math.random() * 6 - 3;
						float walkSpeed = (float) Waves.getEnnemiesSpeed(wave, difficulty);
						world.strikeLightningEffect(nextLoc);
						
						int spawnCount = Math.min(new Random().nextInt((int) Math.sqrt(wave)) + 1, remainingWolves);
						
						for(int i = 0; i < spawnCount; i++) {
							Wolf wo = new Wolf(this.nextLoc, myHealth * 0.4D, walkSpeed * 2.0F);
							wo.setReward(10 + GameManager.this.wave);
						}
						
						this.nextLoc = null;
						
						remainingWolves -= spawnCount;
						
						this.time = (new Random()).nextInt(130) + 40;
						
						if(remainingWolves <= 0) {
							mayBeEndWave = true;
							this.cancel();
							return;
						}
						
						Bukkit.getScheduler().runTaskAsynchronously(Survivor.getInstance(), new Runnable()
						{
							@Override
							public void run() {
								nextLoc = Waves.aWolfSpawnLocationAround(getOnlinePlayers().stream().collect(Utils.randomCollector()).get().toBukkit());
							}
						});
					}
					
					--this.time;
				}
			};
			wolfRunnable.runTaskTimer(Survivor.getInstance(), 1L, 1L);
		}
		
		if(wave % 10 != 0) {
			final int totalEnnemies = nbZombies + nbWolves;
			
			new BukkitRunnable()
			{
				int lastCount = totalEnnemies;
				int secNoChange = 0;
				int secSinceStart = 0;
				
				@Override
				public void run() {
					if(!inWave) {
						cancel();
					}
					else if(mayBeEndWave) {
						if(getRemainingEnnemies() != lastCount) {
							lastCount = getRemainingEnnemies();
							secNoChange = 0;
						}
						else {
							secNoChange++;
						}
						secSinceStart++;
						
						// calculs savant
						double x = (double) secNoChange / 60;
						double x2 = x*x;
						double n = totalEnnemies;
						double treshold = (n * x2) / (n + x2);
						
						if(secNoChange > 5 && getRemainingEnnemies() < treshold) {
							for(Enemy m : new ArrayList<>(mobs)) {
								m.kill(null);
							}
						}
					}
				}
			}.runTaskTimer(Survivor.getInstance(), 20L, 20L);
		}
		
		bossBar.onChangeState();
		Surviboard.updateWave();
	}
	
	public boolean mayBeEndWave() {
		return mayBeEndWave;
	}
	
	public void setWave(int wave) {
		this.wave = wave;
	}
	
	public void endGame() {
		Bukkit.broadcastMessage(SurvivorGame.prefix + "§cVous avez perdu, tout le monde est mort ...");
		
		gameEnded = true;
		
		for(SvPlayer sp : getOnlinePlayers()) {
			bossBar.bossBar.removeAll();
			
			sp.toCosmox().addStatistic(GameVariables.GAMES_PLAYED, 1);
		}
		
		API.instance().getManager().setPhase(Phase.END);
		API.instance().getManager().getGame().addToResume("§7Vague atteinte : §a" + wave);
		API.instance().getManager().getGame().addToResume("§7Durée de la partie : §e" + timeDisplay(Survivor.currentTick / 20));
		API.instance().getManager().getGame().addToResume("§7Difficulté : " + difficulty.getColoredDisplayName());
	}
	
	private String timeDisplay(int seconds) {
		StringBuilder s = new StringBuilder();
		if(seconds >= 3600)
			s.append(seconds / 3600).append("h ");
		if(seconds >= 60)
			s.append((seconds % 3600) / 60).append("m ");
		
		s.append(seconds % 60).append("s");
		
		return s.toString();
	}
	
	public MagicBoxManager getMagicBoxManager() {
		return this.magicBoxManager;
	}
	
	public UpgradeBoxManager getUpgradeBoxManager() {
		return upgradeBoxManager;
	}
	
	public AmmoFrameManager getAmmoFrameManager() {
		return ammoFrameManager;
	}
	
	public List<Location> getAmmoBoxes() {
		return this.ammoBoxes;
	}
	
	public void buyElectrical(SvPlayer p) {
		this.electricalBought = true;
		Bukkit.broadcastMessage(SurvivorGame.prefix + "§6" + p.getName() + " §aa activé l'électricité !");
	}
	
	public Location getElectrical() {
		return this.electrical;
	}
	
	public boolean canPlayerBuyAsset() {
		return this.electrical == null || this.electricalBought;
	}
	
	public Enemy getMob(Entity ent) {
		return mobs.stream().filter(mob -> mob.getEntity().equals(ent)).findFirst().orElse(null);
	}
	
	public Difficulty getDifficulty() {
		return this.difficulty;
	}
	
	public void setDifficulty(Difficulty difficulty) {
		this.difficulty = difficulty;
	}
	
	public void destroy() {
		if(instance == this)
			instance = null;
	}
	
	public class GameBossBar
	{
		private final BossBar bossBar;
		private double max;
		
		public GameBossBar() {
			bossBar = Bukkit.createBossBar("En attente ...", BarColor.WHITE, BarStyle.SOLID);
		}
		
		public void onChangeState() {
			if(isInWave()) {
				this.max = getRemainingEnnemies();
				bossBar.setColor(BarColor.WHITE);
			}
			else {
				this.max = getTimeUntilNextWave();
				bossBar.setColor(BarColor.GREEN);
			}
		}
		
		public void update() {
			for(Player player : Bukkit.getOnlinePlayers()) {
				if(!bossBar.getPlayers().contains(player))
					bossBar.addPlayer(player);
			}
			
			if(isInWave()) {
				bossBar.setTitle("§6Ennemis restant: §c" + getRemainingEnnemies() + (!getAliveBosses().isEmpty() ? " §7(" + getAliveBosses().size() + " boss)" : ""));
				bossBar.setProgress(Math.min(1, (double) getRemainingEnnemies() / max));
			}
			else {
				bossBar.setTitle("§eProchaine vague: §6" + (getTimeUntilNextWave() / 20) + "s §8(/voteskip)");
				bossBar.setProgress(1 - (double) getTimeUntilNextWave() / max);
			}
		}
	}
	
	public boolean isGameEnded() {
		return gameEnded;
	}
}

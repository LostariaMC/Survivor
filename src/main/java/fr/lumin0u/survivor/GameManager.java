package fr.lumin0u.survivor;

import fr.lumin0u.survivor.config.MapConfig;
import fr.lumin0u.survivor.mobs.Group;
import fr.lumin0u.survivor.mobs.Waves;
import fr.lumin0u.survivor.mobs.mob.Enemy;
import fr.lumin0u.survivor.mobs.mob.Wolf;
import fr.lumin0u.survivor.mobs.mob.Zombie;
import fr.lumin0u.survivor.mobs.mob.*;
import fr.lumin0u.survivor.mobs.mob.boss.Boss;
import fr.lumin0u.survivor.objects.Door;
import fr.lumin0u.survivor.objects.MagicBoxManager;
import fr.lumin0u.survivor.objects.Room;
import fr.lumin0u.survivor.player.SvPlayer;
import fr.lumin0u.survivor.utils.ItemBuilder;
import fr.lumin0u.survivor.utils.MCUtils;
import fr.lumin0u.survivor.utils.Utils;
import fr.lumin0u.survivor.weapons.WeaponType;
import fr.worsewarn.cosmox.API;
import fr.worsewarn.cosmox.api.players.WrappedPlayer;
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
	private List<Location> ammoBoxes;
	@NotNull
	private Difficulty difficulty = Difficulty.NOT_SET;
	private Location electrical;
	private final /*final*/ double priceAugmentation;
	private boolean electricalBought;
	private long nextWaveStartDate;
	private boolean dogWave;
	private boolean mayBeEndWave;
	private int spawnedWolves;
	private int totalWolves;
	private BukkitRunnable wolfRunnable;
	private final List<SvPlayer> voteSkippers = new ArrayList<>();
	private final GameBossBar bossBar;
	
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
		
		rooms = new ArrayList<>(config.getRooms());
		totalFenceCount = rooms.stream().mapToInt(room -> room.getFences().size()).sum();
		
		for(Room room : rooms)
			room.setWorld(world);
		
		defaultRoom = rooms.stream()
				.filter(room -> room.getName().equals("default"))
				.findAny()
				.orElseThrow(() -> new IllegalArgumentException("La map %s ne contient pas de salle \"default\"".formatted(map.getName())));
		
		this.defaultRoom.setBought(true);
		this.priceAugmentation = Math.pow(50.0D, 1.0D / ((double) this.rooms.size() - 1.0D));
		
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
			
			@Override
			public void run() {
				for(SvPlayer player : getPlayers()) {
					Surviboard.updatePlayerLine(player);
				}
			}
		}.runTaskTimer(Survivor.getInstance(), 10L, 10L);
	}
	
	public void startGame() {
		if(difficulty == Difficulty.NOT_SET) {
			difficulty = getPlayers().stream().map(SvPlayer::getDiffVote).filter(diff -> diff != Difficulty.NOT_SET).collect(Utils.randomCollector()).orElse(Difficulty.NORMAL);
		}
		
		if(this.defaultRoom != null && this.spawnpoint != null) {
			Bukkit.broadcastMessage(SurvivorGame.prefix + "§6La partie commence !");
			endWave();
			magicBoxManager.onGameStart();
			
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
				ItemStack itemA = new ItemBuilder(Material.CARROT).setDisplayName("§6Approvisionnement").build();
				p.getInventory().setItem(4, itemA);
				p.updateInventory();
				p.sendMessage(SurvivorGame.prefix + "§cVous recevrez votre approvisionnement à partir de la vague 10");
				
				SvPlayer sp = SvPlayer.of(p);
				WeaponType.LITTLE_KNIFE.getNewWeapon(sp).giveItem();
				WeaponType.M1911.getNewWeapon(sp).giveItem();
				p.teleport(spawnpoint);
				p.setGameMode(GameMode.ADVENTURE);
				p.playSound(p.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1.0F, 1.0F);
			}
			
			for(Room room : this.rooms) {
				room.startZombieVsFencesTask();
				room.updateDoors();
			}
			
			Bukkit.getOnlinePlayers().stream().map(SvPlayer::of).forEach(Surviboard::reInitScoreboard);
		}
		else {
			Bukkit.broadcastMessage("§cVeuillez finir la config avant de lancer la partie");
		}
	}
	
	public void augmentPrice() {
		this.doorPrice = (int) ((double) this.doorPrice * this.priceAugmentation);
		
		for(Room room : this.rooms) {
			if(!room.isBought()) {
				room.updateDoors();
			}
		}
		
	}
	
	/*public long getTimer()
	{
		return this.startDate == 0L ? 0L : System.currentTimeMillis() - this.startDate;
	}*/
	
	public int getDoorPrice() {
		return this.doorPrice;
	}
	
	public int getTotalFenceCount() {
		return totalFenceCount;
	}
	
	/** @return non-spec players, maybe offline */
	public Collection<SvPlayer> getPlayers() {
		// load online players first
		Bukkit.getOnlinePlayers().forEach(SvPlayer::of);
		
		return Survivor.getInstance().getLoadedSvPlayers().stream()
				.filter(Predicate.not(SvPlayer::isSpectator))
				.toList();
	}
	
	/** @return non-spec players */
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
		return dogWave ? totalWolves - spawnedWolves + mobs.size() : mobs.size();
	}
	
	public boolean isDogWave() {
		return this.dogWave;
	}
	
	/*@Deprecated
	public void saveInConfig()
	{
		try
		{
			if(this.spawnpoint != null)
			{
				this.config.set(this.world.getName() + ".spawnpoint", MCUtils.locToString(this.spawnpoint));
			}
			
			if(this.lobby != null)
			{
				this.config.set("lobby", MCUtils.locToString(this.lobby));
			}
			
			Writer writer = new FileWriter(this.mapsFile);
			this.gson.toJson(this.worldRooms, writer);
			writer.flush();
			writer.close();
			List<String> ammoBoxes = new ArrayList<>();
			
			for(Location b : this.ammoBoxes)
			{
				ammoBoxes.add(MCUtils.locToString(b));
			}
			
			this.config.set(this.world.getName() + ".ammoBoxes", ammoBoxes);
			Survivor.getInstance().saveConfig();
		} catch(Exception var6)
		{
			var6.printStackTrace();
		}
		
		try
		{
			this.mbm.saveInConfig();
		} catch(Exception var5)
		{
			var5.printStackTrace();
		}
	}*/
	
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
		this.dogWave = false;
		this.mayBeEndWave = false;
		inWave = false;
		int waveDelay = (200 + 20 * Math.min(20, this.wave)) * ((wave + 1) % 5 == 0 ? 3 : 1);
		this.nextWaveStartDate = Survivor.getCurrentTick() + waveDelay;
		voteSkippers.clear();
		
		bossBar.onChangeState();
		Surviboard.updateWave();
		
		for(SvPlayer sp : getOnlinePlayers()) {
			if(wave >= 9 && sp.toBukkit().getInventory().contains(Material.CARROT))
				sp.toBukkit().getInventory().remove(Material.CARROT);
			
			if(this.wave >= 9) {
				sp.getSupply().getNewWeapon(sp).giveItem();
			}
			
			if(this.wave != 0) {
				MCUtils.sendTitle(sp.toBukkit(), 10, 40, 20, "§2Vague " + this.wave, "§acomplétée");
			}
			
			sp.toCosmox().addMolecules(this.wave * Math.sqrt(difficulty.getNB()) / 5, "Vague " + wave);
			
			
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
	
	public void nextWave() {
		++wave;
		
		Survivor.getInstance().getLogger().info("Vague " + wave);
		
		for(SvPlayer sp : getOnlinePlayers()) {
			MCUtils.sendTitle(sp.toBukkit(), 10, 40, 20, "§4Vague " + wave);
			sp.toBukkit().removePotionEffect(PotionEffectType.REGENERATION);
		}
		
		boolean dogWave = Waves.isDogWave(this.wave);
		this.dogWave = dogWave;
		int nbZombies = (int) (Waves.getNbEnnemies(this.wave, this.difficulty) * Math.sqrt(getOnlinePlayers().size()));
		int ennemies = dogWave ? (int) Math.pow(nbZombies, 0.7) : nbZombies;
		
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
		this.mayBeEndWave = false;
		spawnedWolves = 0;
		
		if(!dogWave) {
			if(spawns.isEmpty()) {
				Bukkit.broadcastMessage("§cVeuillez définir des points d'apparition pour les zombies");
				return;
			}
			
			if(wave % 10 == 0) {
				for(int i = 0; i < getOnlinePlayers().size() / 4 + 1; i++) {
					Location bossSpawn = spawns.get((new Random()).nextInt(spawns.size()));
					
					double bossHealth = this.wave * 60 - 20;
					double bossWalkSpeed = Waves.getEnnemiesSpeed(this.wave, this.difficulty);
					Boss boss = Boss.createRandom(bossSpawn, bossHealth, bossWalkSpeed * 1.1D);
					boss.setReward(this.wave * this.wave * 10);
				}
			}
			
			int mod = ennemies % spawns.size();
			int spawnsSize = spawns.size();
			
			for(int j = 0; j < spawnsSize; ++j) {
				Location spawn = spawns.get(j);
				List<Zombie> zombies = new ArrayList<>();
				
				for(int i = mod > j ? -1 : 0; i < ennemies / spawns.size(); ++i) {
					double health = Math.max(0, Waves.getEnnemiesLife(this.wave, this.difficulty) + Math.random() * 6 - 3);
					double walkSpeed = Waves.getEnnemiesSpeed(this.wave, this.difficulty);
					Zombie m;
					if((new Random()).nextInt(20) == 1 && this.wave > 3) {
						m = new BabyZombie(spawn, health / 2.0D, walkSpeed * 1.3D);
					}
					else if((new Random()).nextInt(15) == 1 && this.wave > 2) {
						m = new ZombieShooter(spawn, health / 2.0D, walkSpeed);
					}
					else if((new Random()).nextInt(15) == 1 && this.wave > 4) {
						m = new ZombieGrappler(spawn, health, walkSpeed);
					}
					else {
						m = new Zombie(spawn, health, walkSpeed);
					}
					
					zombies.add(m);
					m.setReward(10 + this.wave);
				}
				
				this.mayBeEndWave = true;
				if(!zombies.isEmpty()) {
					Group group = new Group(zombies);
					zombies.forEach(zombie -> zombie.setGroup(group));
				}
			}
		}
		else {
			this.totalWolves = ennemies;
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
						
						double health = Math.max(0, Waves.getEnnemiesLife(wave, difficulty) + Math.random() * 6 - 3);
						float walkSpeed = (float) Waves.getEnnemiesSpeed(wave, difficulty);
						world.strikeLightningEffect(nextLoc);
						
						int spawnCount = Math.min(new Random().nextInt((int) Math.sqrt(wave)) + 1, totalWolves - spawnedWolves);
						
						for(int i = 0; i < spawnCount; i++) {
							Wolf wo = new Wolf(this.nextLoc, health * 0.4D, walkSpeed * 2.0F);
							wo.setReward(10 + GameManager.this.wave);
						}
						
						this.nextLoc = null;
						
						spawnedWolves += spawnCount;
						
						this.time = (new Random()).nextInt(130) + 40;
						
						if(spawnedWolves >= totalWolves) {
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
			new BukkitRunnable()
			{
				int lastCount = ennemies;
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
						
						double treshold = (double) (secSinceStart + secNoChange - 5) / 60;//(double) ticksSinceStart / 500 * (double) Math.min(ticksNoChange - 100, 400) / 400;
						
						if(getRemainingEnnemies() < ennemies / 5 && secNoChange > 5 && treshold > 0 && getRemainingEnnemies() < treshold * treshold) {
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
		return this.mayBeEndWave;
	}
	
	public void setWave(int wave) {
		this.wave = wave;
	}
	
	public void endGame() {
		Bukkit.broadcastMessage(SurvivorGame.prefix + "§cVous avez perdu, tout le monde est mort ...");
		
		for(SvPlayer sp : getOnlinePlayers()) {
			sp.toBukkit().sendMessage("§6Ennemis tués : §e" + StatsManager.getStatInt(sp.getPlayerUid(), "totalKills"));
			sp.toBukkit().sendMessage("§6Dégats infligés : §e" + StatsManager.getStatInt(sp.getPlayerUid(), "totalDamage"));
			
			bossBar.bossBar.removeAll();
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
			s.append((seconds / 60) % 3600).append("m ");
		
		s.append(seconds % 60).append("s");
		
		return s.toString();
	}
	
	public MagicBoxManager getMagicBoxManager() {
		return this.magicBoxManager;
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
		/*if(this.difficulty == null)
		{
			this.calculateDifficulty();
		}*/
		
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
				bossBar.setTitle("§6Ennemis restant: §c" + getRemainingEnnemies() + (getAliveBosses().size() > 0 ? " §7(" + getAliveBosses().size() + " boss" + (getAliveBosses().size() > 1 ? "es" : "") + ")" : ""));
				bossBar.setProgress(Math.min(1, (double) getRemainingEnnemies() / max));
			}
			else {
				bossBar.setTitle("§eProchaine vague: §6" + (getTimeUntilNextWave() / 20) + "s §8(/voteskip)");
				bossBar.setProgress(1 - (double) getTimeUntilNextWave() / max);
			}
		}
	}
}

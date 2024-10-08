package fr.lumin0u.survivor.player;

import fr.lumin0u.survivor.Difficulty;
import fr.lumin0u.survivor.*;
import fr.lumin0u.survivor.utils.AABB;
import fr.lumin0u.survivor.utils.ItemBuilder;
import fr.lumin0u.survivor.utils.MCUtils;
import fr.lumin0u.survivor.utils.TFSound;
import fr.lumin0u.survivor.weapons.Weapon;
import fr.lumin0u.survivor.weapons.WeaponType;
import fr.lumin0u.survivor.weapons.knives.Knife;
import fr.lumin0u.survivor.weapons.superweapons.AirStrike;
import fr.lumin0u.survivor.weapons.superweapons.SuperWeapon;
import fr.worsewarn.cosmox.api.players.WrappedPlayer;
import fr.worsewarn.cosmox.game.GameVariables;
import fr.worsewarn.cosmox.game.teams.Team;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.stream.Streams;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Stream;

public class SvPlayer extends WrappedPlayer implements WeaponOwner, SvDamageable
{
	public static final int SUPPLY_SLOT = 4;
	public static final int AIRSTRIKE_SLOT = 7;
	public static final int SPECIAL_SLOT = 8;
	
	
	private Knife knife;
	private final List<Weapon> exchangeableWeapons;
	private SuperWeapon supplyWeapon;
	private AirStrike airstrike;
	private SuperWeapon specialWeapon;
	
	private final Set<SvAsset> assets;
	private Object actionBar;
	private LifeState lifeState;
	private double money;
	private double reviveTime;
	private long deathDate;
	private long instantKillStartDate;
	private WeaponType supplyType;
	private Difficulty diffVote = Difficulty.NOT_SET;
	private int fireTime;
	private WeaponOwner fireSource;
	private Weapon fireSourceWeapon;
	
	private static final long INSTANT_KILL_DURATION = 400;
	private static final long ON_GROUND_TIME = 750L;
	
	public SvPlayer(UUID uid)
	{
		super(uid);
		
		this.exchangeableWeapons = new LinkedList<>();
		this.assets = new HashSet<>();
		this.actionBar = null;
		this.lifeState = LifeState.ALIVE;
		this.supplyType = WeaponType.GRENADE;
		
		startLifeRunnable();
	}
	
	public void setDeadLifeStateCauseHeJustJoined()
	{
		lifeState = LifeState.DEAD;
	}
	
	public boolean isSpectator() {
		return toCosmox().getTeam() == Team.SPEC;
	}
	
	private void startLifeRunnable() {
		new BukkitRunnable()
		{
			int turns = 10;
			int regen = 0;
			double lastHealth;
			int lastLookElec;
			
			@Override
			public void run()
			{
				if(GameManager.getInstance() == null)
					return;
				
				if(isOnline())
				{
					Player bukkitPlayer = toBukkit();
					
					this.regen = Math.max(0, this.regen - 1);
					if(this.lastHealth > bukkitPlayer.getHealth())
					{
						this.regen = 100;
					}
					
					double maxHealth = bukkitPlayer.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
					
					if(this.regen == 0 && bukkitPlayer.getHealth() < maxHealth)
					{
						double lastHealth = bukkitPlayer.getHealth();
						bukkitPlayer.setHealth(Math.min(maxHealth, bukkitPlayer.getHealth() + GameManager.getInstance().getDifficulty().regenHpPerSecond()));
						if(Math.floor(lastHealth) < Math.floor(bukkitPlayer.getHealth())) {
							bukkitPlayer.sendHealthUpdate();
						}
						
						this.regen = 20;
					}
					
					if(this.lastHealth != bukkitPlayer.getHealth()) {
						WorldBorder worldBorder = Bukkit.createWorldBorder();
						worldBorder.setCenter(bukkitPlayer.getLocation());
						worldBorder.setSize(200000);
						worldBorder.setWarningTime(15);
						worldBorder.setWarningDistance((int) (-10000 * bukkitPlayer.getHealth() / maxHealth + 1300000));
						
						bukkitPlayer.setWorldBorder(worldBorder);
					}
					
					if(fireTime == 1)
					{
						fireTime = 0;
						bukkitPlayer.setFireTicks(0);
					}
					else if(fireTime > 0)
					{
						bukkitPlayer.setFireTicks(19);
						if(fireTime % 20 == 0)
						{
							damage(0.5, fireSource, fireSourceWeapon, false, null);
						}
						
						--fireTime;
					}
					
					if(isOnGround() && deathDate + ON_GROUND_TIME - Survivor.getCurrentTick() > 0)
					{
						MCUtils.sendActionBar(bukkitPlayer, "§cMort dans §6" + (deathDate + ON_GROUND_TIME - Survivor.getCurrentTick()) / 20 + " §csecondes");
					}
					
					else if(actionBar instanceof String && isAlive())
					{
						MCUtils.sendActionBar(bukkitPlayer, (String) actionBar);
						--this.turns;
						if(this.turns == 0)
						{
							this.turns = 10;
							setActionBar(null);
						}
					}
					else if(isAlive())
					{
						Weapon weaponInHand = SvPlayer.this.getWeaponInHand();
						SvPlayer.this.actionBar = weaponInHand;
						if(weaponInHand != null && weaponInHand.getClip() <= 0 && weaponInHand.getAmmo() > 0 && !weaponInHand.isReloading())
						{
							weaponInHand.reload();
						}
						
						if(GameManager.getInstance().isStarted())
						{
							if(actionBar instanceof Weapon) {
								MCUtils.sendActionBar(bukkitPlayer, ((Weapon) actionBar).getActionBar());
							}
							else if(actionBar != null) {
								MCUtils.sendActionBar(bukkitPlayer, actionBar.toString());
							}
							
							if(actionBar == null)
							{
								MCUtils.sendActionBar(bukkitPlayer, "");
							}
						}
						
						if(!GameManager.getInstance().canPlayerBuyAsset())
						{
							Location l1 = bukkitPlayer.getEyeLocation();
							Location l2 = bukkitPlayer.getEyeLocation().add(bukkitPlayer.getEyeLocation().getDirection().multiply(5));
							Vector line = MCUtils.vectorFrom(l1, l2);
							BlockIterator itr = new BlockIterator(l1.setDirection(line), 0.0D, (int) Math.ceil(line.length()));
							
							boolean inSight = false;
							
							while(itr.hasNext())
							{
								Block blockx = itr.next();
								if(GameManager.getInstance().getElectrical().getBlock().equals(blockx))
								{
									if(lastLookElec == 0)
									{
										MCUtils.sendTitle(bukkitPlayer, 5, 10, 5, "§aActiver l'électricité", "§e1000$");
									}
									else if(lastLookElec > 5)
									{
										MCUtils.sendTitle(bukkitPlayer, 0, 10, 5, "§aActiver l'électricité", "§e1000$");
									}
									inSight = true;
									lastLookElec++;
									break;
								}
							}
							
							if(!inSight)
								lastLookElec = 0;
						}
						
						this.lastHealth = bukkitPlayer.getHealth();
					}
				}
			}
		}.runTaskTimer(Survivor.getInstance(), 1L, 1L);
	}
	
	public void onRightClick() {
		
		Weapon weapon = getWeaponInHand();
		
		if(weapon != null && (canUseWeapon() || (weapon.getType() == WeaponType.AIRSTRIKE && !isDead()))) {
			int rpm = weapon.getType().getRpm();
			
			weapon.impulseRightClick();
		}
	}
	
	public void onLeftClick() {
		Weapon weapon = getWeaponInHand();
		
		if(weapon != null && canUseWeapon())
		{
			weapon.leftClick();
		}
	}
	
	public UUID getPlayerUid()
	{
		return this.uid;
	}
	
	public List<Weapon> getExchangeableWeapons() {
		return new ArrayList<>(this.exchangeableWeapons);
	}
	
	@Override
	public Collection<Weapon> getWeapons() {
		return Stream.concat(Streams.of(exchangeableWeapons), Streams.nonNull(knife, supplyWeapon, airstrike, specialWeapon)).toList();
	}
	
	@Override
	public ItemStack findItem(Weapon w)
	{
		int place = getInventory().first(w.getType().getMaterial());
		return place == -1 ? null : getInventory().getItem(place);
	}
	
	public void setActionBar(Weapon actionBar)
	{
		this.actionBar = actionBar;
		if(this.toBukkit() != null && actionBar != null)
		{
			MCUtils.sendActionBar(this.toBukkit(), actionBar.getActionBar());
		}
		else if(this.toBukkit() != null)
		{
			MCUtils.sendActionBar(this.toBukkit(), "");
		}
		
	}
	
	public Knife getKnife()
	{
		return this.knife;
	}
	
	public boolean isAlive() {
		return lifeState == LifeState.ALIVE && !isSpectator();
	}
	
	public boolean isOnGround() {
		return lifeState == LifeState.ON_GROUND && !isSpectator();
	}
	
	public boolean isDead() {
		return lifeState == LifeState.DEAD || isSpectator();
	}
	
	public void respawn()
	{
		this.lifeState = LifeState.ALIVE;
		if(isOnline())
		{
			toBukkit().teleport(GameManager.getInstance().getSpawnpoint());
			toBukkit().setGameMode(GameMode.ADVENTURE);
			toBukkit().setGlowing(false);
			cleanInventory();
		}
		
		LainBodies.wakeUp(uid);
	}
	
	public void fallOnGround()
	{
		Bukkit.broadcastMessage(SurvivorGame.prefix + "§6" + this.getName() + "§c est à terre !");
		
		TFSound.PLAYER_FALL.play(getFeets());
		this.lifeState = LifeState.ON_GROUND;
		
		Player player = this.toBukkit();
		player.setGlowing(true);
		MCUtils.sendTitle(player, 10, 40, 20, "§cVous êtes à terre");
		
		player.setHealth(player.getMaxHealth());
		final Location deathLoc = player.getLocation();
		this.deathDate = Survivor.getCurrentTick();
		
		Location as1Location = player.getEyeLocation().add(0, 1.2, 0);
		final Entity as2;
		
		if(Calendar.getInstance().get(Calendar.DAY_OF_MONTH) == 1 && Calendar.getInstance().get(Calendar.MONTH) == Calendar.APRIL)
		{
			as2 = MCUtils.oneConsistentFlyingText(as1Location, "§aSAUVE MOI EN FAISANT ALT+F4");
		}
		else
		{
			as2 = MCUtils.oneConsistentFlyingText(as1Location, "§a§lSNEAK §2POUR ME SAUVER");
		}
		
		final TextDisplay as3 = MCUtils.oneConsistentFlyingText(as1Location.clone().add(0, -0.3, 0), "§4· · · · · ·");
		player.setVelocity(new Vector(0, 0, 0));
		LainBodies.lie(player);
		
		toCosmox().addStatistic(SurvivorGame.DOWNFALLS_STAT, 1);
		
		reviveTime = 1000;
		boolean everyoneIsDead = true;
		
		for(SvPlayer sp : GameManager.getInstance().getPlayers()) {
			if(sp.isAlive() && sp.toBukkit() != null) {
				everyoneIsDead = false;
			}
		}
		
		if(everyoneIsDead) {
			GameManager.getInstance().endGame();
		}
		else
		{
			// not safe to use in the runnable
			player = null;
			
			(new BukkitRunnable()
			{
				private void playDead()
				{
					toCosmox().addStatistic(GameVariables.DEATHS, 1);
					Bukkit.broadcastMessage(SurvivorGame.prefix + "§6" + getName() + "§c est mort !");
					TFSound.PLAYER_DEATH.play(getFeets());
					lifeState = LifeState.DEAD;
					as2.remove();
					as3.remove();
					
					if(isOnline()) {
						toBukkit().setGlowing(false);
						
						MCUtils.sendTitle(toBukkit(), 10, 40, 20, "§cVous êtes mort");
						toBukkit().setGameMode(GameMode.SPECTATOR);
					}
					
					if(!assets.contains(SvAsset.PIERRE_TOMBALE))
					{
						assets.clear();
						
						if(isOnline())
						{
							toBukkit().setMaxHealth(GameManager.getInstance().getDifficulty().getMaxHealth());
							toBukkit().setWalkSpeed(0.2F);
							
							toBukkit().sendMessage(SurvivorGame.prefix + "§cVous n'aviez pas l'atout §7Pierre tombale§c, vous avez donc perdu vos atouts");
						}
					}
					else
					{
						assets.remove(SvAsset.PIERRE_TOMBALE);
						if(isOnline()) {
							toBukkit().sendMessage(SurvivorGame.prefix + "§cVous aviez l'atout §7Pierre tombale§c, vous n'avez donc pas perdu vos autres atouts");
						}
					}
					
					LainBodies.wakeUp(SvPlayer.this.uid);
				}
				
				private void revive() {
					lifeState = LifeState.ALIVE;
					toBukkit().setGameMode(GameMode.ADVENTURE);
					as2.remove();
					as3.remove();
					cleanInventory();
					LainBodies.wakeUp(uid);
					toBukkit().setGlowing(false);
					deathDate = 0L;
					
					MCUtils.sendTitle(toBukkit(), 10, 40, 20, "§aDebout !", "§6vous êtes vivant !");
				}
				
				boolean hardlyDead = false;
				
				@Override
				public void run()
				{
					if(isAlive())
					{
						cancel();
						return;
					}
					
					if(!isOnline())
					{
						playDead();
						lifeState = LifeState.DEAD;
						LainBodies.wakeUp(uid);
					}
					else if(GameManager.getInstance().isGameEnded()) {
						playDead();
						cancel();
					}
					else
					{
						if(!GameManager.getInstance().isInWave()) {
							revive();
							cancel();
							return;
						}
						
						reviveTime += 4;
						
						List<SvPlayer> savers = new ArrayList<>();
						
						for(SvPlayer sp : GameManager.getInstance().getOnlinePlayers()) {
							if(sp.toBukkit().isSneaking() && sp.toBukkit().getLocation().distanceSquared(toBukkit().getLocation()) < 25 && sp.isAlive()) {
								savers.add(sp);
								int difficultyV = GameManager.getInstance().getDifficulty().ordinal();
								reviveTime -= (sp.getAssets().contains(SvAsset.QUICK_REVIVE) ? 28 : 15) - difficultyV;
							}
						}
						
						boolean dyingAlone = savers.isEmpty();
						
						if(Survivor.getCurrentTick() - deathDate > ON_GROUND_TIME && dyingAlone) {
							if(!isDead()) {
								playDead();
								cancel();
							}
						}
						else
						{
							if(Survivor.getCurrentTick() - SvPlayer.this.deathDate > ON_GROUND_TIME && !hardlyDead)
							{
								hardlyDead = true;
								toBukkit().addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 200, 0));
								toBukkit().sendMessage(SurvivorGame.prefix + "§aEn voyant vos alliés vous sauver, vous vous raccrochez à la vie");
							}
							
							toBukkit().setHealth(toBukkit().getMaxHealth());
							
							if(reviveTime > 1000.0D)
							{
								reviveTime = 1000.0D;
							}
							
							if(reviveTime == 1000.0D)
								as3.setText("§4· · · · · ·");
							else if(reviveTime > 800.0D)
								as3.setText("§c■ · · · · ·");
							else if(reviveTime > 600.0D)
								as3.setText("§6■ ■ · · · ·");
							else if(reviveTime > 400.0D)
								as3.setText("§e■ ■ ■ · · ·");
							else if(reviveTime > 200.0D)
								as3.setText("§a■ ■ ■ ■ · ·");
							else if(reviveTime > 0.0D)
								as3.setText("§2■ ■ ■ ■ ■ ·");
							else if(reviveTime <= 0.0D)
							{
								Bukkit.broadcastMessage(SurvivorGame.prefix + "§6" + getName() + "§a a été réanimé !");
								revive();
								savers.forEach(sp -> sp.toCosmox().addStatistic(SurvivorGame.REANIMATIONS_STAT, 1));
								cancel();
							}
						}
					}
				}
			}).runTaskTimer(Survivor.getInstance(), 1L, 1L);
		}
	}
	
	public double getMoney()
	{
		return this.money;
	}
	
	public void setMoney(double money)
	{
		if(money != this.money) {
			String moneyChange = Math.abs(money - this.money) < 1 ? "%.2f".formatted(Math.abs(money - this.money)) : (int) Math.ceil(Math.abs(money - this.money)) + "";
			this.actionBar = "§a" + (money - this.money > 0 ? "+" : "-") + "§6" + moneyChange + "$";
		}
		this.money = money;
	}
	
	public void addMoney(double money)
	{
		setMoney(this.money + money);
	}
	
	public Collection<SvAsset> getAssets()
	{
		return this.assets;
	}
	
	public int getMaxWeaponCount() {
		return getAssets().contains(SvAsset.TROIS_ARME) ? 3 : 2;
	}
	
	public AABB bodyCub()
	{
		return new AABB(this.toBukkit().getLocation().clone().add(-0.48D, 0.0D, -0.48D), this.toBukkit().getLocation().clone().add(0.48D, 1.5D, 0.48D));
	}
	
	public AABB headCub()
	{
		return new AABB(this.toBukkit().getLocation().clone().add(-0.4D, 1.5D, -0.4D), this.toBukkit().getLocation().clone().add(0.4D, 1.9D, 0.4D));
	}
	
	public void startInstantKill()
	{
		this.instantKillStartDate = Survivor.getCurrentTick();
		
		BossBar bossBar = Bukkit.createBossBar("§c§lInstant Kill", BarColor.RED, BarStyle.SOLID);
		
		bossBar.addPlayer(toBukkit());
		
		final Player player = this.toBukkit();
		
		new BukkitRunnable()
		{
			@Override
			public void run()
			{
				if(player.isOnline() && doInstantKill()) {
					bossBar.setProgress(1 - (double) (Survivor.getCurrentTick() - instantKillStartDate) / INSTANT_KILL_DURATION);
				}
				else {
					bossBar.removeAll();
					cancel();
				}
			}
		}.runTaskTimer(Survivor.getInstance(), 5L, 5L);
	}
	
	@Override
	public boolean doInstantKill() {
		return Survivor.getCurrentTick() - this.instantKillStartDate < INSTANT_KILL_DURATION;
	}
	
	@Override
	public Location getShootLocation()
	{
		return toBukkit().getEyeLocation();
	}
	
	public WeaponType getSupplyType() {
		return this.supplyType;
	}
	
	public void setSupplyType(WeaponType supplyType)
	{
		this.supplyType = supplyType;
	}
	
	public void openSupplyInventory()
	{
		Inventory inv = Bukkit.createInventory(null, 18, Component.text("Approvisionnement"));
		inv.setItem(1, MCUtils.newItem(Material.SNOWBALL, "§9Grenade", Arrays.asList("§6Recevez §e3 §6grenades à", "§6chaque fin de vague")));
		inv.setItem(2, MCUtils.newItem(Material.SLIME_BALL, "§9Grenade Frag", Arrays.asList("§6Recevez §e2 §6grenades frag", "§6à chaque fin de vague")));
		inv.setItem(3, MCUtils.newItem(Material.PAPER, "§9Medic Kit", Arrays.asList("§6Recevez §e1 §6medic kit", "§6à chaque fin de vague")));
		inv.setItem(5, MCUtils.newItem(Material.MAGMA_CREAM, "§9Grenade Incendiaire", Arrays.asList("§6Recevez §e3 §6grenades", "§6incendiaires à chaque fin", "§6de vague")));
		inv.setItem(6, MCUtils.newItem(Material.GOLD_NUGGET, "§9Tourelle", Arrays.asList("§6Recevez §e1 §6tourelle à", "§6chaque fin de vague")));
		inv.setItem(7, MCUtils.newItem(Material.CAKE, "§9Boite de munitions", Arrays.asList("§6Recevez §e1 §6boite de munitions", "§6chaque fin de vague")));
		inv.setItem(10, new ItemStack(supplyType.equals(WeaponType.GRENADE) ? Material.GREEN_STAINED_GLASS_PANE :  Material.RED_STAINED_GLASS_PANE));
		inv.setItem(11, new ItemStack(supplyType.equals(WeaponType.GRENADEFRAG) ? Material.GREEN_STAINED_GLASS_PANE :  Material.RED_STAINED_GLASS_PANE));
		inv.setItem(12, new ItemStack(supplyType.equals(WeaponType.MEDIC_KIT) ? Material.GREEN_STAINED_GLASS_PANE :  Material.RED_STAINED_GLASS_PANE));
		inv.setItem(14, new ItemStack(supplyType.equals(WeaponType.GRENADEFLAME) ? Material.GREEN_STAINED_GLASS_PANE :  Material.RED_STAINED_GLASS_PANE));
		inv.setItem(15, new ItemStack(supplyType.equals(WeaponType.TURRET) ? Material.GREEN_STAINED_GLASS_PANE :  Material.RED_STAINED_GLASS_PANE));
		inv.setItem(16, new ItemStack(supplyType.equals(WeaponType.AMMO_BOX) ? Material.GREEN_STAINED_GLASS_PANE :  Material.RED_STAINED_GLASS_PANE));
		toBukkit().openInventory(inv);
	}
	
	@Override
	public Weapon getWeaponInHand()
	{
		return getWeapon(toBukkit().getInventory().getItemInMainHand());
	}
	
	public Weapon getWeapon(ItemStack item)
	{
		return getWeapons().stream().filter(w -> w.getItem().isSimilar(item)).findAny().orElse(null);
	}
	
	public void killZombie()
	{
		toCosmox().addStatistic(GameVariables.KILLS, 1);
	}
	
	public @NotNull Difficulty getDiffVote()
	{
		return this.diffVote;
	}
	
	public void setDiffVote(@NotNull Difficulty diffVote)
	{
		this.diffVote = diffVote;
	}
	
	public void cleanInventory()
	{
		Map<Weapon, Integer> exchangeableSlots = new HashMap<>();
		
		PlayerInventory inv = this.toBukkit().getInventory();
		
		for(Weapon w : getExchangeableWeapons()) {
			exchangeableSlots.put(w, inv.first(w.getItem().getType()));
		}
		
		inv.clear();
		
		regiveAllWeapons();
		exchangeableSlots.forEach(this::giveWeaponItem);
		
		if(this.getAssets().contains(SvAsset.MASTODONTE))
		{
			LeatherArmorMeta meta = (LeatherArmorMeta) (new ItemStack(Material.LEATHER_BOOTS)).getItemMeta();
			meta.setUnbreakable(true);
			ItemStack boots = SvAsset.getMastoArmorPiece(Material.LEATHER_BOOTS);
			ItemStack leggings = SvAsset.getMastoArmorPiece(Material.LEATHER_LEGGINGS);
			ItemStack chestplate = SvAsset.getMastoArmorPiece(Material.LEATHER_CHESTPLATE);
			inv.setArmorContents(new ItemStack[] {boots, leggings, chestplate, null});
		}
		else
		{
			inv.setArmorContents(null);
		}
		
		int c = 0;
		
		ItemStack noAssetItem = new ItemBuilder(Material.BARRIER).setDisplayName("§cAchetez un atout").build();
		for(int i = 0; i < 4; i++) {
			inv.setItem(9 + i, noAssetItem);
		}
		for(SvAsset asset : getAssets())
		{
			inv.setItem(9 + (c++), asset.getItem());
		}
		
		if(GameManager.getInstance().getWave() < 9) {
			ItemStack itemA = new ItemBuilder(Material.CARROT).setDisplayName("§6Approvisionnement").build();
			toBukkit().getInventory().setItem(SUPPLY_SLOT, itemA);
		}
	}
	
	public Location checkNotOnBeacon()
	{
		if(toBukkit().getLocation().getY() % 1 != 0)
			return null;
		
		BoundingBox bb = toBukkit().getBoundingBox();
		bb.shift(0, -0.4, 0);
		
		double tpY = toBukkit().getLocation().getY() - 0.4;
		boolean any = false;
		
		World world = toBukkit().getWorld();
		
		Block[] blocks = new Block[] {
				new Location(world, bb.getMinX(), bb.getMinY(), bb.getMinZ()).getBlock(),
				new Location(world, bb.getMaxX(), bb.getMinY(), bb.getMinZ()).getBlock(),
				new Location(world, bb.getMaxX(), bb.getMinY(), bb.getMaxZ()).getBlock(),
				new Location(world, bb.getMinX(), bb.getMinY(), bb.getMaxZ()).getBlock()
		};
		
		for(Block block : blocks)
		{
			if(block.getType() != Material.BEACON)
				tpY = Math.max(tpY, block.getBoundingBox().getMaxY());
			else
				any = true;
		}
		
		Location tp = toBukkit().getLocation();
		if(any && tp.getY() != tpY)
		{
			tp.setY(tpY);
			return tp;
		}
		
		return null;
	}
	
	public Inventory getInventory()
	{
		return toBukkit().getInventory();
	}
	
	@Override
	public boolean canUseWeapon()
	{
		return isAlive();
	}
	
	@Override
	public ItemStack getItemInHand()
	{
		return toBukkit().getInventory().getItemInMainHand();
	}
	
	@Override
	public boolean hasDoubleCoup()
	{
		return getAssets().contains(SvAsset.DOUBLE_COUP);
	}
	
	@Override
	public boolean hasSpeedReload()
	{
		return getAssets().contains(SvAsset.SPEED_RELOAD);
	}
	
	@Override
	public void removeWeapon(Weapon w)
	{
		exchangeableWeapons.remove(w);
		
		if(w.equals(supplyWeapon))
			supplyWeapon = null;
		if(w.equals(airstrike))
			airstrike = null;
		if(w.equals(specialWeapon))
			specialWeapon = null;
		if(w.equals(knife))
			knife = null;
		
		getInventory().remove(w.getItem().getType());
	}
	
	public void regiveAllWeapons() {
		giveWeaponItem(knife, 0);
		exchangeableWeapons.forEach(w -> giveWeaponItem(w, -1));
		if(supplyWeapon != null)
			giveWeaponItem(supplyWeapon, SUPPLY_SLOT);
		if(airstrike != null)
			giveWeaponItem(airstrike, AIRSTRIKE_SLOT);
		if(specialWeapon != null)
			giveWeaponItem(specialWeapon, SPECIAL_SLOT);
	}
	
	/**
	 * Will infer from weapon's type whether it should be given as an exchangeable weapon, a special weapon or a knife.
	 * */
	public void giveBuyableWeapon(Weapon weapon) {
		if(weapon instanceof Knife k) {
			giveKnife(k);
		}
		else if(weapon instanceof SuperWeapon s) {
			giveSpecialWeapon(s);
		}
		else {
			giveExchangeableWeapon(weapon);
		}
	}
	
	public void giveKnife(Knife knife) {
		this.knife = knife;
		giveWeaponItem(knife, 0);
	}
	
	public void giveExchangeableWeapon(Weapon weapon) {
		exchangeableWeapons.add(weapon);
		giveWeaponItem(weapon, -1);
	}
	
	public void giveSpecialWeapon(SuperWeapon weapon) {
		// removing the last is not necessary
		specialWeapon = weapon;
		giveWeaponItem(weapon, SPECIAL_SLOT);
	}
	
	public void giveSupplyWeapon() {
		// removing the last is not necessary
		supplyWeapon = supplyType.getNewWeapon(this);
		giveWeaponItem(supplyWeapon, SUPPLY_SLOT);
	}
	
	public void giveAirstrike() {
		// removing the last is not necessary
		airstrike = new AirStrike(this);
		giveWeaponItem(airstrike, AIRSTRIKE_SLOT);
	}
	
	@Override
	public void refreshWeaponItem(Weapon weapon) {
		giveWeaponItem(weapon, -1);
	}
	
	public void giveWeaponItem(Weapon w, int defaultPlace)
	{
		if(!isOnline())
			return;
		
		Inventory inv = getInventory();
		int place = inv.first(w.getItem().getType());
		inv.remove(w.getItem().getType());
		
		if(defaultPlace != -1)
		{
			inv.setItem(defaultPlace, w.buildItem());
		}
		else if(place != -1)
		{
			inv.setItem(place, w.buildItem());
		}
		else
		{
			inv.addItem(w.buildItem());
		}
	}
	
	@Override
	public AABB getHeadHitbox()
	{
		return new AABB(toBukkit().getLocation().add(-0.25, 1.4, -0.25), toBukkit().getLocation().add(0.25, 1.8, 0.25));
	}
	
	@Override
	public AABB getBodyHitbox()
	{
		return new AABB(toBukkit().getLocation().add(-0.3, 0, -0.3), toBukkit().getLocation().add(0.3, 1.4, 0.3));
	}
	
	@Override
	public Location getFeets()
	{
		return toBukkit().getLocation();
	}
	
	@Override
	public DamageTarget getTargetType()
	{
		return DamageTarget.ZOMBIES;
	}
	
	@Override
	public void setFireTime(long fireTime, WeaponOwner fireMan, Weapon weapon)
	{
		this.fireTime = (int) fireTime;
		this.fireSource = fireMan;
		this.fireSourceWeapon = weapon;
	}
	
	@Override
	public void setFrozenTime(long frozenTime)
	{
		toBukkit().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) frozenTime, 4));
	}
	
	@Override
	public void damage(double dmg, WeaponOwner damager, Weapon weapon, boolean headshot, Vector kb)
	{
		damage(dmg, damager, weapon, headshot, kb, false);
	}
	
	public boolean damage(double dmg, WeaponOwner damager, Weapon weapon, boolean headshot, Vector kb, boolean vanilla)
	{
		if(weapon != null)
			dmg *= weapon.getDamageMultiplier(this);
		
		if(headshot)
			dmg *= 1.5;
		
		if(isAlive())
		{
			Player p = toBukkit();
			if(p.getHealth() <= dmg)
			{
				fallOnGround();
				p.setVelocity(new Vector(0, 0, 0));
				
				return true;
			}
			
			else
			{
				if(!vanilla)
				{
					MCUtils.damageAnimation(p);
					
					toBukkit().getWorld().playSound(toBukkit().getLocation(), Sound.ENTITY_PLAYER_HURT, 1, 1);
					p.setHealth(p.getHealth() - dmg);
					p.setVelocity(p.getVelocity().multiply(0.5).add(kb == null ? new Vector() : kb));
					p.sendHealthUpdate();
				}
				
				return false;
			}
		}
		
		return true;
	}
	
	public static SvPlayer of(Object player) {
		return WrappedPlayer.of(player).to(SvPlayer.class);
	}
	
	public enum LifeState
	{
		ALIVE,
		ON_GROUND,
		DEAD
	}
}

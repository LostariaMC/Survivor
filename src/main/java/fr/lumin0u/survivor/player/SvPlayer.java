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
import fr.lumin0u.survivor.weapons.superweapons.SuperWeapon;
import fr.worsewarn.cosmox.api.players.WrappedPlayer;
import fr.worsewarn.cosmox.game.teams.Team;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
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

public class SvPlayer extends WrappedPlayer implements WeaponOwner, SvDamageable
{
	private Knife knife;
	private final List<Weapon> weapons;
	private final List<SvAsset> assets;
	private Object actionBar;
	private LifeState lifeState;
	private double money;
	private double reviveTime;
	private long deathDate;
	private long instantKillStartDate;
	private WeaponType supply;
	private Difficulty diffVote = Difficulty.NOT_SET;
	private final BadgesData badgesData;
	private int fireTime;
	private WeaponOwner fireSource;
	private Weapon fireSourceWeapon;
	
	private long lastClickDate;
	private long lastShotDate;
	
	private static final long INSTANT_KILL_TIME = 20000L;
	private static final long ON_GROUND_TIME = 750L;
	
	public SvPlayer(UUID uid)
	{
		super(uid);
		
		this.weapons = new ArrayList<>();
		this.assets = new ArrayList<>();
		this.actionBar = null;
		this.lifeState = LifeState.ALIVE;
		this.supply = WeaponType.GRENADE;
		
		badgesData = new BadgesData();
		
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
					
					if(this.regen == 0 && bukkitPlayer.getHealth() < bukkitPlayer.getMaxHealth())
					{
						double lastHealth = bukkitPlayer.getHealth();
						bukkitPlayer.setHealth(Math.min(bukkitPlayer.getMaxHealth(), bukkitPlayer.getHealth() + GameManager.getInstance().getDifficulty().regenHpPerSecond()));
						if(Math.floor(lastHealth) < Math.floor(bukkitPlayer.getHealth()))
						{
							/*PacketContainer packet = new PacketContainer(Play.Server.UPDATE_HEALTH);
							packet.getFloat()
									.write(0, (float) ((int) bukkitPlayer.getHealth()))
									.write(1, 0f);
							packet.getIntegers().write(0, (int) bukkitPlayer.getMaxHealth());
							
							
							sendPacket(packet);*/
							bukkitPlayer.sendHealthUpdate();
						}
						
						this.regen = 20;
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
								Block blockx = (Block) itr.next();
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
		
		if(weapon != null && canUseWeapon())
		{
			int rpm = weapon.getType().getRpm();
			
			switch(weapon.getType().getRepeatingType())
			{
				case NONE -> {
					weapon.rightClick();
				}
				case SEMIAUTOMATIC, BURSTS -> {
					if(System.currentTimeMillis() - lastClickDate > 215 && Survivor.getCurrentTick() - lastShotDate >= rpm)
					{
						weapon.rightClick();
						if(!weapon.isReloading())
							weapon.showCooldown(Math.max(4, rpm));
						lastShotDate = Survivor.getCurrentTick();
					}
				}
				case AUTOMATIC -> {
					if(weapon.rClickingTask == null)
					{
						Runnable doShot = weapon::rightClick;
						
						doShot.run();
						
						weapon.rClickingTask = new BukkitRunnable()
						{
							@Override
							public void run()
							{
								if(weapon.equals(getWeaponInHand()) && weapon.getClip() > 0 && weapon.isUseable() && System.currentTimeMillis() - lastClickDate < 150)
								{
									doShot.run();
								}
								else
								{
									weapon.rClickingTask = null;
									this.cancel();
								}
							}
						};
						
						weapon.rClickingTask.runTaskTimer(Survivor.getInstance(), rpm, rpm);
					}
				}
			}
		}
		
		lastClickDate = System.currentTimeMillis();
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
	
	@Override
	public List<Weapon> getWeapons()
	{
		return new ArrayList<>(this.weapons);
	}
	
	@Override
	public ItemStack findItem(Weapon w)
	{
		int place = getInventory().first(w.getType().getMaterial());
		return place == -1 ? null : getInventory().getItem(place);
	}
	
	public List<Weapon> getSimpleWeapons()
	{
		List<Weapon> list = new ArrayList<>();
		
		for(Weapon w : this.weapons)
		{
			if(!(w instanceof SuperWeapon) && !w.equals(this.getKnife()))
			{
				list.add(w);
			}
		}
		
		return list;
	}
	
	public List<SuperWeapon> getSuperWeapons()
	{
		return getWeaponsByType(SuperWeapon.class);
	}
	
	public Object getActionBar()
	{
		return this.actionBar;
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
	
	public void setKnife(Knife knife)
	{
		this.knife = knife;
	}
	
	public LifeState getLifeState() {
		return lifeState;
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
		
		badgesData.hadBeenOnGround = true;
		Player player = this.toBukkit();
		player.setGlowing(true);
		MCUtils.sendTitle(player, 10, 40, 20, "§cVous êtes à terre");
		
		player.setHealth(player.getMaxHealth());
		final Location deathLoc = player.getLocation();
		this.deathDate = Survivor.getCurrentTick();
		final ArmorStand as1 = MCUtils.oneConsistentFlyingText(player.getEyeLocation(), "§4JE SUIS EN TRAIN DE MOURIR");
		final ArmorStand as2;
		
		if(Calendar.getInstance().get(Calendar.DATE) == 1 && Calendar.getInstance().get(Calendar.MONTH) == Calendar.APRIL)
		{
			as2 = MCUtils.oneConsistentFlyingText(player.getEyeLocation().add(0.0D, -0.3D, 0.0D), "§aSAUVE MOI EN FAISANT ALT+F4");
		}
		else
		{
			as2 = MCUtils.oneConsistentFlyingText(player.getEyeLocation().add(0.0D, -0.3D, 0.0D), "§aSAUVE MOI EN SNEAKANT");
		}
		
		final ArmorStand as3 = MCUtils.oneConsistentFlyingText(player.getEyeLocation().add(0.0D, -0.6D, 0.0D), "§4· · · · · ·");
		player.setVelocity(new Vector(0, 0, 0));
		LainBodies.lie(player);
		StatsManager.increaseStat(this.uid, "fallDowns", 1, true);
		this.reviveTime = 1000.0D;
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
					StatsManager.increaseStat(uid, "deaths", 1, true);
					Bukkit.broadcastMessage(SurvivorGame.prefix + "§6" + SvPlayer.this.getName() + "§c est mort !");
					TFSound.PLAYER_DEATH.play(getFeets());
					
					toBukkit().setGlowing(false);
					lifeState = LifeState.DEAD;
					as1.remove();
					as2.remove();
					as3.remove();
					if(!SvPlayer.this.toBukkitOffline().isOnline())
					{
						return;
					}
					
					MCUtils.sendTitle(toBukkit(), 10, 40, 20, "§cVous êtes mort");
					SvPlayer.this.toBukkit().setGameMode(GameMode.SPECTATOR);
					
					if(!assets.contains(SvAsset.PIERRE_TOMBALE))
					{
						assets.clear();
						weapons.clear();
						WeaponType.LITTLE_KNIFE.getNewWeapon(SvPlayer.this).giveItem();
						WeaponType.M1911.getNewWeapon(SvPlayer.this).giveItem();
						money = (int) ((double) money * 0.75);
						
						if(isOnline())
						{
							toBukkit().setMaxHealth(GameManager.getInstance().getDifficulty().getMaxHealth());
							toBukkit().setWalkSpeed(0.2F);
							
							toBukkit().sendMessage(SurvivorGame.prefix + "§cVous n'aviez pas l'atout §7Pierre tombale§c, vous avez donc perdu vos armes et vos atouts");
						}
					}
					else
					{
						assets.remove(SvAsset.PIERRE_TOMBALE);
						toBukkit().sendMessage(SurvivorGame.prefix + "§cVous aviez l'atout §7Pierre tombale§c, vous n'avez donc pas perdu vos armes ni vos atouts, excepté l'atout pierre tombale");
					}
					
					LainBodies.wakeUp(SvPlayer.this.uid);
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
						lifeState = LifeState.DEAD;
						LainBodies.wakeUp(uid);
					}
					else
					{
						if(!GameManager.getInstance().isInWave())
						{
							lifeState = LifeState.ALIVE;
							toBukkit().setGameMode(GameMode.ADVENTURE);
							toBukkit().teleport(GameManager.getInstance().getSpawnpoint());
							as1.remove();
							as2.remove();
							as3.remove();
							this.cancel();
							cleanInventory();
							LainBodies.wakeUp(SvPlayer.this.uid);
							toBukkit().setGlowing(false);
						}
						
						reviveTime += 4;
						
						List<SvPlayer> savers = new ArrayList<>();
						
						for(SvPlayer sp : GameManager.getInstance().getOnlinePlayers()) {
							if(sp.toBukkit().isSneaking() && sp.toBukkit().getLocation().distance(deathLoc) < 5 && sp.isAlive()) {
								savers.add(sp);
								reviveTime -= (sp.getAtouts().contains(SvAsset.QUICK_REVIVE) ? 25 : 10);
							}
						}
						
						boolean dyingAlone = savers.isEmpty();
						
						if(Survivor.getCurrentTick() - deathDate > ON_GROUND_TIME && dyingAlone)
						{
							if(!isDead())
							{
								this.playDead();
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
								as3.setCustomName("§4· · · · · ·");
							else if(reviveTime > 800.0D)
								as3.setCustomName("§c■ · · · · ·");
							else if(reviveTime > 600.0D)
								as3.setCustomName("§6■ ■ · · · ·");
							else if(reviveTime > 400.0D)
								as3.setCustomName("§e■ ■ ■ · · ·");
							else if(reviveTime > 200.0D)
								as3.setCustomName("§a■ ■ ■ ■ · ·");
							else if(reviveTime > 0.0D)
								as3.setCustomName("§2■ ■ ■ ■ ■ ·");
							else if(reviveTime <= 0.0D)
							{
								Bukkit.broadcastMessage(SurvivorGame.prefix + "§6" + getName() + "§a a été réanimé !");
								lifeState = LifeState.ALIVE;
								toBukkit().teleport(deathLoc);
								toBukkit().setGameMode(GameMode.ADVENTURE);
								toBukkit().setGlowing(false);
								deathDate = 0L;
								cancel();
								LainBodies.wakeUp(uid);
								as1.remove();
								as2.remove();
								as3.remove();
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
	
	public List<SvAsset> getAtouts()
	{
		return this.assets;
	}
	
	public AABB bodyCub()
	{
		return new AABB(this.toBukkit().getLocation().clone().add(-0.48D, 0.0D, -0.48D), this.toBukkit().getLocation().clone().add(0.48D, 1.5D, 0.48D));
	}
	
	public AABB headCub()
	{
		return new AABB(this.toBukkit().getLocation().clone().add(-0.4D, 1.5D, -0.4D), this.toBukkit().getLocation().clone().add(0.4D, 1.9D, 0.4D));
	}
	
	public double getReviveTime()
	{
		return this.reviveTime;
	}
	
	public void setReviveTime(double reviveTime)
	{
		this.reviveTime = reviveTime;
	}
	
	public void startInstantKill()
	{
		this.instantKillStartDate = Survivor.getCurrentTick();
		
		BossBar bossBar = Bukkit.createBossBar("§c§lInstant Kill", BarColor.RED, BarStyle.SOLID);
		
		bossBar.addPlayer(toBukkit());
		
		final Player player = this.toBukkit();
		
		new BukkitRunnable()
		{
			public void run()
			{
				if(player.isOnline() && doInstantKill())
				{
					bossBar.setProgress(1 - (double) (Survivor.getCurrentTick() - instantKillStartDate) / 400);
				}
				else
				{
					bossBar.removeAll();
					cancel();
				}
			}
		}.runTaskTimer(Survivor.getInstance(), 5L, 5L);
	}
	
	@Override
	public boolean doInstantKill()
	{
		return Survivor.getCurrentTick() - this.instantKillStartDate < 400L;
	}
	
	@Override
	public Location getShootLocation()
	{
		return toBukkit().getEyeLocation();
	}
	
	public WeaponType getSupply()
	{
		return this.supply;
	}
	
	public void setSupply(WeaponType supply)
	{
		this.supply = supply;
	}
	
	public Inventory openSupplyInventory()
	{
		Inventory inv = Bukkit.createInventory((InventoryHolder) null, 18, "Approvisionnement");
		inv.setItem(1, MCUtils.newItem(Material.SNOWBALL, "§9Grenade", Arrays.asList("§6Recevez §e3 §6grenades à", "§6chaque fin de vague")));
		inv.setItem(2, MCUtils.newItem(Material.SLIME_BALL, "§9Grenade Frag", Arrays.asList("§6Recevez §e2 §6grenades frag", "§6à chaque fin de vague")));
		inv.setItem(3, MCUtils.newItem(Material.PAPER, "§9Medic Kit", Arrays.asList("§6Recevez §e1 §6medic kit", "§6à chaque fin de vague")));
		inv.setItem(5, MCUtils.newItem(Material.MAGMA_CREAM, "§9Grenade Incendiaire", Arrays.asList("§6Recevez §e3 §6grenades", "§6incendiaires à chaque fin", "§6de vague")));
		inv.setItem(6, MCUtils.newItem(Material.GOLD_NUGGET, "§9Tourelle", Arrays.asList("§6Recevez §e1 §6tourelle à", "§6chaque fin de vague")));
		inv.setItem(7, MCUtils.newItem(Material.CAKE, "§9Boite de munitions", Arrays.asList("§6Recevez §e1 §6boite de munitions", "§6chaque fin de vague")));
		inv.setItem(10, new ItemStack(this.supply.equals(WeaponType.GRENADE) ? Material.GREEN_STAINED_GLASS_PANE :  Material.RED_STAINED_GLASS_PANE));
		inv.setItem(11, new ItemStack(this.supply.equals(WeaponType.GRENADEFRAG) ? Material.GREEN_STAINED_GLASS_PANE :  Material.RED_STAINED_GLASS_PANE));
		inv.setItem(12, new ItemStack(this.supply.equals(WeaponType.MEDIC_KIT) ? Material.GREEN_STAINED_GLASS_PANE :  Material.RED_STAINED_GLASS_PANE));
		inv.setItem(14, new ItemStack(this.supply.equals(WeaponType.GRENADEFLAME) ? Material.GREEN_STAINED_GLASS_PANE :  Material.RED_STAINED_GLASS_PANE));
		inv.setItem(15, new ItemStack(this.supply.equals(WeaponType.TURRET) ? Material.GREEN_STAINED_GLASS_PANE :  Material.RED_STAINED_GLASS_PANE));
		inv.setItem(16, new ItemStack(this.supply.equals(WeaponType.AMMO_BOX) ? Material.GREEN_STAINED_GLASS_PANE :  Material.RED_STAINED_GLASS_PANE));
		Bukkit.getPlayer(this.uid).openInventory(inv);
		return inv;
	}
	
	public Weapon getWeaponInHand()
	{
		return getWeapon(toBukkit().getInventory().getItemInMainHand());
	}
	
	public Weapon getWeapon(ItemStack item)
	{
		return weapons.stream().filter(w -> w.getItem().isSimilar(item)).findAny().orElse(null);
	}
	
	public void killZombie()
	{
		StatsManager.increaseStat(this.uid, "totalKills", 1, false);
		badgesData.killInLast2Seconds++;
		Bukkit.getScheduler().runTaskLater(Survivor.getInstance(), () -> badgesData.killInLast2Seconds--, 40);
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
		List<Weapon> orderedWeapons = new ArrayList<>();
		
		PlayerInventory inv = this.toBukkit().getInventory();
		
		for(int i = 0; i < inv.getSize(); i++)
		{
			if(inv.getItem(i) != null && getWeapon(inv.getItem(i)) != null)
			{
				orderedWeapons.add(getWeapon(inv.getItem(i)));
			}
		}
		
		inv.clear();
		
		for(Weapon weapon : orderedWeapons)
		{
			weapon.giveItem();
		}
		
		if(this.getAtouts().contains(SvAsset.MASTODONTE))
		{
			LeatherArmorMeta meta = (LeatherArmorMeta) (new ItemStack(Material.LEATHER_BOOTS)).getItemMeta();
			meta.setUnbreakable(true);
			ItemStack boots = SvAsset.getMastoArmorPiece(Material.LEATHER_BOOTS);
			ItemStack leggings = SvAsset.getMastoArmorPiece(Material.LEATHER_LEGGINGS);
			ItemStack chestplate = SvAsset.getMastoArmorPiece(Material.LEATHER_CHESTPLATE);
			inv.setArmorContents(new ItemStack[]{boots, leggings, chestplate, null});
		}
		else
		{
			inv.setArmorContents((ItemStack[]) null);
		}
		
		int c = 0;
		
		for(SvAsset asset : this.getAtouts())
		{
			inv.setItem(9 + c, asset.getItem());
			++c;
		}
		
		if(GameManager.getInstance().getWave() < 10) {
			ItemStack itemA = new ItemBuilder(Material.CARROT).setDisplayName("§6Approvisionnement").build();
			toBukkit().getInventory().setItem(4, itemA);
		}
	}
	
	public BadgesData getBadgesData()
	{
		return badgesData;
	}
	
	public Location checkNotOnBeacon()
	{
		if(toBukkit().getLocation().getY() % 1 != 0)
			return null;
		
		BoundingBox bb = toBukkit().getBoundingBox();
		bb.shift(0, -0.01, 0);
		
		double tpY = toBukkit().getLocation().getY() - 0.01;
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
		return getAtouts().contains(SvAsset.DOUBLE_COUP);
	}
	
	@Override
	public boolean hasSpeedReload()
	{
		return getAtouts().contains(SvAsset.SPEED_RELOAD);
	}
	
	@Override
	public void removeWeapon(Weapon w)
	{
		weapons.remove(w);
		getInventory().remove(w.getItem());
	}
	
	@Override
	public void addWeapon(Weapon weapon) {
		weapons.add(weapon);
	}
	
	@Override
	public void giveWeaponItem(Weapon w)
	{
		Inventory inv = getInventory();
		int place = inv.first(w.getItem().getType());
		inv.remove(w.getItem().getType());
		
		if(w.getType().getPlace() != -1)
		{
			inv.setItem(w.getType().getPlace(), w.getItem());
		}
		else if(place != -1)
		{
			inv.setItem(place, w.getItem());
		}
		else
		{
			inv.addItem(w.getItem());
		}
		
		toBukkit().updateInventory();
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
					p.setHealth(p.getHealth() - dmg * (headshot ? 1.5 : 1));
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
	
	
	public class BadgesData
	{
		public int doorsBought;
		public int reviveCount;
		public boolean hadBeenOnGround;
		public int killInLast2Seconds;
		
		private BadgesData()
		{
		
		}
	}
	
	public enum LifeState
	{
		ALIVE,
		ON_GROUND,
		DEAD;
	}
}

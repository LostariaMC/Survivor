package fr.lumin0u.survivor.listeners;

import com.comphenix.protocol.PacketType.Play.Client;
import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.wrappers.EnumWrappers.EntityUseAction;
import fr.lumin0u.survivor.*;
import fr.lumin0u.survivor.config.commands.LeaveConfigCommand;
import fr.lumin0u.survivor.mobs.mob.Enemy;
import fr.lumin0u.survivor.mobs.mob.boss.PoisonousBoss;
import fr.lumin0u.survivor.objects.Door;
import fr.lumin0u.survivor.objects.MagicBoxManager;
import fr.lumin0u.survivor.objects.Room;
import fr.lumin0u.survivor.player.LainBodies;
import fr.lumin0u.survivor.player.SvPlayer;
import fr.lumin0u.survivor.utils.MCUtils;
import fr.lumin0u.survivor.utils.TFSound;
import fr.lumin0u.survivor.weapons.IPlaceable;
import fr.lumin0u.survivor.weapons.Weapon;
import fr.lumin0u.survivor.weapons.WeaponType;
import fr.lumin0u.survivor.weapons.guns.snipers.Sniper;
import fr.lumin0u.survivor.weapons.knives.Knife;
import fr.lumin0u.survivor.weapons.superweapons.SuperWeapon;
import fr.worsewarn.cosmox.api.players.WrappedPlayer;
import fr.worsewarn.cosmox.game.events.PlayerJoinGameEvent;
import fr.worsewarn.cosmox.game.teams.Team;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Cake;
import org.bukkit.block.data.type.Gate;
import org.bukkit.block.data.type.Switch;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockIterator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class PlayerEvents implements PacketListener, Listener
{
	@EventHandler
	public void onAnimation(PlayerAnimationEvent e) {
		if(GameManager.getInstance() != null)
		{
			SvPlayer sp = SvPlayer.of(e.getPlayer());
			GameManager gm = GameManager.getInstance();
			if(e.getAnimationType().equals(PlayerAnimationType.ARM_SWING) && !sp.isSpectator())
			{
				sp.onLeftClick();
			}
		}
	}
	
	@EventHandler
	public void onScroll(PlayerItemHeldEvent e) {
		if(GameManager.getInstance() != null)
		{
			ItemStack item = e.getPlayer().getInventory().getItemInMainHand();
			SvPlayer sp = SvPlayer.of(e.getPlayer());
			GameManager gm = GameManager.getInstance();
			
			if(!sp.isSpectator()) {
				for(Sniper w : sp.getWeaponsByType(Sniper.class)) {
					if(item.equals(w.getItem())) {
						w.unScope();
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onSwitchHands(PlayerSwapHandItemsEvent event) {
		if(GameManager.getInstance() != null) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onInteract(final PlayerInteractEvent e) {
		if(GameManager.getInstance() != null)
		{
			ItemStack item = e.getItem();
			SvPlayer player = SvPlayer.of(e.getPlayer());
			
			GameManager gm = GameManager.getInstance();
			
			if(!player.toBukkit().getGameMode().equals(GameMode.CREATIVE)) {
				e.setCancelled(true);
			}
			
			if(e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && e.getClickedBlock().getType().equals(Material.ENDER_CHEST) && gm.isStarted()) {
				gm.getMagicBoxManager().onClickOnBox(player);
				e.setCancelled(true);
			}
			
			if(e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && e.getItem() != null && e.getClickedBlock().equals(gm.getUpgradeBoxManager().getBlock()) && gm.isStarted() && player.isAlive())
			{
				gm.getUpgradeBoxManager().openGui(player);
				e.setCancelled(true);
				return;
			}
			
			if(e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && e.getClickedBlock().getBlockData() instanceof Gate && gm.isStarted() && player.isAlive())
			{
				boolean zombieNear = gm.getMobs().stream().anyMatch(m -> m.getEntity().getLocation().distance(e.getClickedBlock().getLocation()) < 4.0D);
				
				Room room = gm.getRooms().stream().filter(r -> r.getFences().contains(e.getClickedBlock().getLocation())).findFirst().get();
				
				/*new BukkitRunnable()
				{
					@Override
					public void run() {*/
						if(!zombieNear)
						{
							room.placeFence(e.getClickedBlock().getLocation());
							player.addMoney(100 * Math.sqrt(gm.getWave()) / gm.getTotalFenceCount());
						}
						else
						{
							room.placeGate(e.getClickedBlock().getLocation());
						}
				/*	}
				}.runTaskLater(Survivor.getInstance(), 2L);*/
			}
			
			if(e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
				if(gm.isStarted()) {
					for(Door d : gm.getDoors()) {
						if(d.getBars().contains(e.getClickedBlock()) && !d.getRoom().isBought()) {
							d.tryBuy(player);
							return;
						}
					}
				}
			}
			
			if(e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && e.getClickedBlock().getLocation().equals(gm.getElectrical()) && !gm.canPlayerBuyAsset() && player.getMoney() >= 1000)
			{
				gm.buyElectrical(player);
				player.addMoney(-1000);
				
				if(e.getClickedBlock().getType().equals(Material.LEVER))
				{
					Switch lever = (Switch) e.getClickedBlock().getBlockData();
					lever.setPowered(!lever.isPowered());
					e.getClickedBlock().setBlockData(lever);
				}
			}
			
			if(item != null && item.getType().equals(Material.CARROT))
			{
				player.openSupplyInventory();
			}
			
			if((e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) && !player.isSpectator())
			{
				List<Weapon> needsAmmo = new ArrayList<>();
				
				for(Weapon w : player.getSimpleWeapons())
				{
					if(w.getAmmo() < w.getMaxAmmo())
					{
						needsAmmo.add(w);
					}
				}
				
				if(!needsAmmo.isEmpty())
				{
					BlockIterator itr = new BlockIterator(player.toBukkit().getEyeLocation().setDirection(player.toBukkit().getEyeLocation().getDirection()), 0.0D, 5);
					
					while(itr.hasNext())
					{
						Block block = (Block) itr.next();
						
						if(block.getType().equals(Material.CAKE))
						{
							MCUtils.armSwingAnimation(player.toBukkit(), false);
							Cake cake = (Cake) block.getBlockData();
							
							for(int i = 0; i < Math.min(needsAmmo.size(), Survivor.CAKE_MAX_BITES - cake.getBites() + 1); ++i)
							{
								TFSound.AMMO_TAKE.play(player.toBukkit().getLocation());
								if(needsAmmo.get(i).getAmmo() <= 0) {
									needsAmmo.get(i).reload();
								}
								
								needsAmmo.get(i).setAmmo(Math.min(needsAmmo.get(i).getAmmo() + needsAmmo.get(i).getAmmoBoxRecovery(), needsAmmo.get(i).getMaxAmmo()));
								if(cake.getBites() == Survivor.CAKE_MAX_BITES - 1)
								{
									block.setType(Material.AIR);
									cake = null;
									break;
								}
								
								cake.setBites(cake.getBites() + 1);
							}
							
							if(cake != null)
								block.setBlockData(cake);
							
							return;
						}
					}
				}
				
				Weapon weapon = player.getWeaponInHand();
				if(weapon != null) {
					if(weapon instanceof IPlaceable placeable
							&& e.getAction() == Action.RIGHT_CLICK_BLOCK
							&& placeable.canPlace(e.getClickedBlock().getRelative(e.getBlockFace()), e.getBlockFace())) {
						placeable.place(e.getClickedBlock().getRelative(e.getBlockFace()), e.getBlockFace());
					}
					else {
						player.onRightClick();
					}
				}
			}
			
		}
	}
	
	@EventHandler
	public void onFoodLevelChange(FoodLevelChangeEvent e) {
		e.setCancelled(true);
		e.getEntity().setFoodLevel(20);
	}
	
	@EventHandler
	public void onDamage(EntityDamageEvent e) {
		if(GameManager.getInstance() != null && e.getEntity() instanceof Player)
		{
			SvPlayer player = SvPlayer.of(e.getEntity());
			player.toBukkit().setFireTicks(0);
			Enemy damagerMob = null;
			if(e.getCause().equals(DamageCause.ENTITY_ATTACK) && player.isAlive()) {
				Entity damager = ((EntityDamageByEntityEvent) e).getDamager();
				for(DamageModifier mod : DamageModifier.values())
					if(e.isApplicable(mod))
						e.setDamage(mod, 0);
				
				damagerMob = GameManager.getInstance().getMob(damager);
				
				if(damagerMob != null) {
					e.setDamage(DamageModifier.BASE, damagerMob.getDamage());
				}
				
				if(damagerMob instanceof PoisonousBoss) {
					player.toBukkit().addPotionEffect(new PotionEffect(PotionEffectType.POISON, 200, 0));
				}
			}
			else if(e.getCause() != DamageCause.POISON) {
				e.setCancelled(true);
			}
			
			if(!e.isCancelled() && player.damage(e.getFinalDamage(), damagerMob, null, false, null, true)) {
				e.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onInteractAtEntity(PlayerInteractEntityEvent e) {
		if(GameManager.getInstance() != null)
		{
			GameManager gm = GameManager.getInstance();
			SvPlayer player = SvPlayer.of(e.getPlayer());
			if((e.getRightClicked().getType().equals(EntityType.ITEM_FRAME) || e.getRightClicked().getType().equals(EntityType.GLOW_ITEM_FRAME)) && !((ItemFrame) e.getRightClicked()).getItem().getType().equals(Material.AIR) && gm.isStarted() && player.isAlive())
			{
				e.setCancelled(true);
				
				Consumer<WeaponType> tryBuyWeapon = wt -> {
					
					((ItemFrame) e.getRightClicked()).setItem(wt.getItemToSell());
					
					if(!player.getWeaponTypes().contains(wt)) {
						if(wt.getPrice() <= player.getMoney()) {
							if(player.getSimpleWeapons().size() >= (player.getAssets().contains(SvAsset.TROIS_ARME) ? 3 : 2)) {
								Weapon replaced = player.getWeaponInHand();
								
								if(replaced != null && !(replaced instanceof SuperWeapon || replaced instanceof Knife)) {
									player.removeWeapon(replaced);
									wt.giveNewWeapon(player).giveItem();
									player.addMoney(-wt.getPrice());
								} else {
									TFSound.CANT_AFFORD.playTo(player);
									player.toBukkit().sendMessage(SurvivorGame.prefix + "§cVous avez atteint la limite d'armes. Prenez une arme dans la main pour la remplacer.");
								}
							}
							else {
								wt.giveNewWeapon(player).giveItem();
								player.addMoney(-wt.getPrice());
							}
						}
						else {
							TFSound.CANT_AFFORD.playTo(player);
							player.toBukkit().sendMessage(SurvivorGame.prefix + "§cVous ne possédez pas assez d'argent pour acheter cette arme !");
						}
					}
					else if((double) wt.getPrice() / 2 <= player.getMoney())
					{
						player.getWeapon(wt).ifPresent(weapon -> {
							if(weapon.getAmmo() < weapon.getMaxAmmo())
							{
								weapon.setAmmo(weapon.getMaxAmmo());
								player.addMoney((double) -wt.getPrice() / 2);
							}
						});
					}
					else {
						TFSound.CANT_AFFORD.playTo(player);
						player.toBukkit().sendMessage(SurvivorGame.prefix + "§cVous ne possédez pas assez d'argent pour acheter ces munitions !");
					}
				};
				
				Arrays.stream(WeaponType.values())
						.filter(wt -> wt.getMaterial().equals(((ItemFrame) e.getRightClicked()).getItem().getType()))
						.findFirst()
						.ifPresent(tryBuyWeapon);
				
				for(SvAsset asset : SvAsset.values())
				{
					if(asset.getMaterial().equals(((ItemFrame) e.getRightClicked()).getItem().getType()))
					{
						((ItemFrame) e.getRightClicked()).setItem(asset.getItem());
						if(!gm.canPlayerBuyAsset())
						{
							ItemStack item = asset.getItem().clone();
							ItemMeta meta = item.getItemMeta();
							meta.setDisplayName("§cActivez d'abord l'electricité");
							item.setItemMeta(meta);
							((ItemFrame) e.getRightClicked()).setItem(item);
							(new BukkitRunnable()
							{
								@Override
								public void run() {
									((ItemFrame) e.getRightClicked()).setItem(asset.getItem());
								}
							}).runTaskLater(Survivor.getInstance(), 40L);
							break;
						}
						
						if(asset.getPrice() > player.getMoney()) {
							TFSound.CANT_AFFORD.playTo(player);
							player.toBukkit().sendMessage(SurvivorGame.prefix + "§cVous n'avez pas assez d'argent pour acheter ca !");
						}
						else if(player.getAssets().size() >= 4) {
							TFSound.CANT_AFFORD.playTo(player);
							player.toBukkit().sendMessage(SurvivorGame.prefix + "§cVous ne pouvez pas acheter plus de 4 atouts !");
						}
						else if(!player.getAssets().contains(asset))
						{
							player.getAssets().add(asset);
							
							player.toBukkit().sendMessage(SurvivorGame.prefix + "§6Vous avez acheté l'atout §a" + asset.getName() + " §7(il apparait dans votre inventaire, appuyer sur votre touche de drop pour vous en débarrasser)");
							
							player.addMoney(-asset.getPrice());
							if(asset.equals(SvAsset.MASTODONTE))
							{
								player.toBukkit().setMaxHealth(GameManager.getInstance().getDifficulty().getMaxHealth() * 2.0D);
								player.toBukkit().setHealth(GameManager.getInstance().getDifficulty().getMaxHealth() * 2.0D);
							}
							
							if(asset.equals(SvAsset.MARATHON))
							{
								player.toBukkit().setWalkSpeed(0.3F);
							}
							
							player.cleanInventory();
						}
					}
				}
			}
			else
			{
				this.onInteract(new PlayerInteractEvent(e.getPlayer(), Action.RIGHT_CLICK_AIR, e.getPlayer().getInventory().getItemInMainHand(), null, BlockFace.DOWN));
			}
		}
	}
	
	@Override
	public void onPacketReceiving(PacketEvent packetEvent) {
		if(packetEvent.getPacket().getType().equals(Client.USE_ENTITY) && packetEvent.getPlayer() != null)
		{
			PacketContainer packet = packetEvent.getPacket();
			Player player = packetEvent.getPlayer();
			
			if(packet.getEntityUseActions().read(0) != EntityUseAction.ATTACK && Survivor.getInstance().getProtocolManager().getEntityFromID(player.getWorld(), packet.getIntegers().read(0)) == null)
			{
				Bukkit.getScheduler().runTask(Survivor.getInstance(), () -> onInteract(new PlayerInteractEvent(player, Action.RIGHT_CLICK_AIR, player.getInventory().getItemInMainHand(), null, BlockFace.DOWN)));
			}
		}
	}
	
	@EventHandler
	public void onMove(PlayerMoveEvent e) {
		if(GameManager.getInstance() != null)
		{
			SvPlayer sp = SvPlayer.of(e.getPlayer());
			
			
			if(sp.isOnGround()) {
				e.getTo().setX(e.getFrom().getX());
				e.getTo().setZ(e.getFrom().getZ());
			}
			
			if(!sp.isDead()) {
				Location tp = sp.checkNotOnBeacon();
				if(tp != null)
					e.setTo(tp);
				
				if(GameManager.getInstance().getRooms().stream().anyMatch(room -> room.getFences().contains(e.getTo().getBlock().getLocation()) || room.getFences().contains(e.getTo().getBlock().getLocation().add(0.0D, 1.0D, 0.0D)))) {
					e.setCancelled(true);
				}
			}
			
			if(sp.isSpectator() && !e.getPlayer().getWorld().equals(GameManager.getInstance().getWorld()) || Bukkit.getOnlinePlayers().stream().filter(player -> !player.getGameMode().equals(GameMode.SPECTATOR)).mapToDouble(player -> player.getLocation().distance(e.getTo())).min().orElse(0) > 100)
				e.getPlayer().teleport(Bukkit.getOnlinePlayers().stream().filter(player -> !player.getGameMode().equals(GameMode.SPECTATOR)).findFirst().get());
		}
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageByEntityEvent e) {
		if(e.getEntity() instanceof ItemFrame && e.getDamager() instanceof Player)
		{
			if(!((Player) e.getDamager()).getGameMode().equals(GameMode.CREATIVE))
			{
				e.setCancelled(true);
			}
		}
		else if(e.getEntity() instanceof ItemFrame)
		{
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onJoin(PlayerJoinGameEvent e) {
		if(GameManager.getInstance() != null)
		{
			Player player = e.getPlayer();
			player.getInventory().clear();
			
			GameManager gm = GameManager.getInstance();
			
			SvPlayer sp = SvPlayer.of(player);
			
			Bukkit.broadcastMessage(SurvivorGame.prefix + "§a+ §7" + e.getPlayer().getName() + " a rejoint la partie");
			player.getInventory().clear();
			
			boolean inWave = gm.isInWave();
			
			if(sp.getWeapons().isEmpty()) {
				WeaponType.LITTLE_KNIFE.giveNewWeapon(sp);
				WeaponType.M1911.giveNewWeapon(sp);
				
				double n = gm.getWave();
				sp.addMoney(12.5 * n * n + 87.5 * n); // = \sum_{k=0}^{n} 75 + 25*k
			}
			
			sp.getWeapons().forEach(sp::giveWeaponItem);
			
			if(inWave)
			{
				player.sendMessage(SurvivorGame.prefix + "§fUne vague est en cours, vous apparaitrez au début de la prochaine");
				sp.setDeadLifeStateCauseHeJustJoined();
			}
			
			sp.toCosmox().setTeam(Team.RANDOM);
			
			player.setWalkSpeed(sp.getAssets().contains(SvAsset.MARATHON) ? 0.3F : 0.2F);
			player.setMaxHealth(gm.getDifficulty().getMaxHealth() * (sp.getAssets().contains(SvAsset.MASTODONTE) ? 2 : 1));
			
			player.teleport(gm.getSpawnpoint());
			player.setGameMode(inWave ? GameMode.SPECTATOR : GameMode.ADVENTURE);
			
			sp.cleanInventory();
			Bukkit.getOnlinePlayers().stream().map(SvPlayer::of).forEach(Surviboard::reInitScoreboard);
		}
	}
	
	@EventHandler
	public void onDisconnect(PlayerQuitEvent e) {
		
		if(Survivor.getInstance().getInCreationMapConfigs().containsKey(WrappedPlayer.of(e.getPlayer()))) {
			LeaveConfigCommand.leaveConfig(e.getPlayer());
		}
		
		if(GameManager.getInstance() != null)
		{
			GameManager gm = GameManager.getInstance();
			SvPlayer sp = SvPlayer.of(e.getPlayer());
			
			if(gm.isStarted())
				e.setQuitMessage(SurvivorGame.prefix + "§c- §7" + e.getPlayer().getName() + " a quitté la partie");
			else
				e.setQuitMessage(SurvivorGame.prefix + "§c- §7" + e.getPlayer().getName() + " a quitté le lobby");
			
			//sp.onDisconnect();
			
			if(gm.getOnlinePlayers().stream().filter(sp::isNot).findAny().isEmpty() && gm.isStarted()) {
				gm.endGame();
			}
		}
		
		LainBodies.onDisconnect(WrappedPlayer.of(e.getPlayer()));
	}
	
	@EventHandler
	public void onInteractAtArmorStand(PlayerInteractAtEntityEvent e) {
		if(e.getRightClicked() instanceof ArmorStand)
		{
			e.setCancelled(true);
			MagicBoxManager mbm = GameManager.getInstance().getMagicBoxManager();
			ArmorStand magicBox = mbm.getMbTask().getClickableHologram();
			
			Optional<Door> clickedDoor = GameManager.getInstance().getRooms().stream()
					.flatMap(room -> room.getDoors().stream())
					.filter(door -> !door.getRoom().isBought())
					.filter(door -> door.getClickableArmorStands().contains(e.getRightClicked()))
					.findFirst();
			
			if(magicBox != null && !magicBox.isDead() && magicBox.equals(e.getRightClicked()))
			{
				MCUtils.armSwingAnimation(e.getPlayer(), false);
				this.onInteract(new PlayerInteractEvent(e.getPlayer(), Action.RIGHT_CLICK_BLOCK, e.getPlayer().getInventory().getItemInMainHand(), mbm.getBox(), BlockFace.EAST));
			}
			else if(clickedDoor.isPresent())
			{
				clickedDoor.get().tryBuy(SvPlayer.of(e.getPlayer()));
			}
			else
			{
				this.onInteract(new PlayerInteractEvent(e.getPlayer(), Action.RIGHT_CLICK_AIR, e.getPlayer().getInventory().getItemInMainHand(), null, BlockFace.DOWN));
			}
		}
		
	}
	
	@EventHandler
	public void onChat(AsyncChatEvent e) {
		Component message = e.originalMessage();
		e.message(Component.text(e.getPlayer().displayName() + "§7 » §f").append(message));
	}
	
	@Override
	public void onPacketSending(PacketEvent event) {
		if(event.getPacket().getType().equals(Server.ENTITY_METADATA)
				&& event.getPlayer().getEntityId() != event.getPacket().getIntegers().read(0)
				&& LainBodies.isLain(event.getPacket().getIntegers().read(0)))
		{
			event.setCancelled(true);
		}
	}
	
	@Override
	public ListeningWhitelist getSendingWhitelist() {
		return ListeningWhitelist.EMPTY_WHITELIST;
	}
	
	@Override
	public ListeningWhitelist getReceivingWhitelist() {
		return ListeningWhitelist.EMPTY_WHITELIST;
	}
	
	@Override
	public Plugin getPlugin() {
		return Survivor.getInstance();
	}
}

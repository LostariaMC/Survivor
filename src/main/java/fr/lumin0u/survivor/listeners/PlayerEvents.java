package fr.lumin0u.survivor.listeners;

import com.comphenix.protocol.PacketType.Play.Client;
import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.wrappers.EnumWrappers.EntityUseAction;
import fr.lumin0u.survivor.GameManager;
import fr.lumin0u.survivor.Survivor;
import fr.lumin0u.survivor.SvAsset;
import fr.lumin0u.survivor.mobs.mob.Enemy;
import fr.lumin0u.survivor.mobs.mob.boss.PoisonousBoss;
import fr.lumin0u.survivor.objects.Door;
import fr.lumin0u.survivor.objects.MagicBoxManager;
import fr.lumin0u.survivor.objects.Room;
import fr.lumin0u.survivor.objects.UpgradeBoxManager;
import fr.lumin0u.survivor.player.SvPlayer;
import fr.lumin0u.survivor.utils.MCUtils;
import fr.lumin0u.survivor.weapons.Weapon;
import fr.lumin0u.survivor.weapons.WeaponType;
import fr.lumin0u.survivor.weapons.guns.snipers.Sniper;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Cake;
import org.bukkit.block.data.type.Gate;
import org.bukkit.block.data.type.Hopper;
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
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class PlayerEvents implements PacketListener, Listener
{
	private boolean cancelAnimation = false;
	public static final Predicate<Block> IS_PACK_A_PUNCH = (block) -> block.getType().equals(Material.HOPPER) && ((Hopper) block.getBlockData()).getFacing() != BlockFace.DOWN;
	
	@EventHandler
	public void onAnimation(PlayerAnimationEvent e) {
		if(GameManager.getInstance() != null)
		{
			if(!this.cancelAnimation)
			{
				Player p = e.getPlayer();
				GameManager gm = GameManager.getInstance();
				if(e.getAnimationType().equals(PlayerAnimationType.ARM_SWING) && gm.getSvPlayer(p) != null)
				{
					gm.getSvPlayer(p).onLeftClick();
				}
			}
		}
	}
	
	@EventHandler
	public void onScroll(PlayerItemHeldEvent e) {
		if(GameManager.getInstance() != null)
		{
			ItemStack item = e.getPlayer().getInventory().getItemInMainHand();
			Player player = e.getPlayer();
			GameManager gm = GameManager.getInstance();
			if(gm.getSvPlayer(player) != null)
			{
				for(Sniper w : gm.getSvPlayer(player).getWeaponsByType(Sniper.class))
				{
					if(item.equals(w.getItem()))
					{
						w.onScroll();
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onSwitchHands(PlayerSwapHandItemsEvent event) {
		if(GameManager.getInstance() != null)
		{
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onInteract(final PlayerInteractEvent e) {
		if(GameManager.getInstance() != null)
		{
			ItemStack item = e.getItem();
			final Player player = e.getPlayer();
			final GameManager gm = GameManager.getInstance();
			if(e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && !e.getPlayer().getGameMode().equals(GameMode.CREATIVE))
			{
				e.setCancelled(true);
			}
			
			if(gm.isStarted() && player.getGameMode().equals(GameMode.ADVENTURE))
			{
				e.setCancelled(true);
			}
			
			if(e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && e.getClickedBlock().getType().equals(Material.ENDER_CHEST) && gm.isStarted())
			{
				gm.getMagicBoxManager().onClickOnBox(e.getPlayer());
				e.setCancelled(true);
			}
			
			if(e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && e.getItem() != null && (e.getClickedBlock().getType().equals(Material.HOPPER) || e.getClickedBlock().getType().equals(Material.BARRIER)) && gm.isStarted() && gm.getSvPlayer(player).isAlive())
			{
				boolean hopper = IS_PACK_A_PUNCH.test(e.getClickedBlock());
				if(!hopper)
				{
					hopper = IS_PACK_A_PUNCH.test(e.getClickedBlock().getRelative(1, 0, 0));
				}
				
				if(!hopper)
				{
					hopper = IS_PACK_A_PUNCH.test(e.getClickedBlock().getRelative(-1, 0, 0));
				}
				
				if(!hopper)
				{
					hopper = IS_PACK_A_PUNCH.test(e.getClickedBlock().getRelative(0, 0, 1));
				}
				
				if(!hopper)
				{
					hopper = IS_PACK_A_PUNCH.test(e.getClickedBlock().getRelative(0, 0, -1));
				}
				
				if(hopper)
				{
					if(e.getClickedBlock().getType().equals(Material.BARRIER))
					{
						MCUtils.armSwingAnimation(player, false);
					}
					
					UpgradeBoxManager.onClickHopper(e.getClickedBlock(), e.getPlayer());
					e.setCancelled(true);
					return;
				}
			}
			
			if(e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && e.getClickedBlock().getBlockData() instanceof Gate && gm.isStarted() && gm.getSvPlayer(player).isAlive())
			{
				boolean zombieNear = gm.getMobs().stream().anyMatch(m -> m.getEntity().getLocation().distance(e.getClickedBlock().getLocation()) < 4.0D);
				
				new BukkitRunnable()
				{
					@Override
					public void run() {
						if(!zombieNear)
						{
							Room.placeFence(e.getClickedBlock().getLocation());
							gm.getSvPlayer(player).addMoney(gm.getWave());
						}
						else
						{
							Room.placeGate(e.getClickedBlock().getLocation());
						}
						
					}
				}.runTaskLater(Survivor.getInstance(), 2L);
			}
			
			if(e.getAction().equals(Action.RIGHT_CLICK_BLOCK))
			{
				if(gm.isStarted())
				{
					for(Door d : gm.getDoors())
					{
						if(d.getBars().contains(e.getClickedBlock()) && !d.getRoom().isBought())
						{
							d.buy(gm.getSvPlayer(player));
							return;
						}
					}
				}
			}
			
			if(e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && e.getClickedBlock().getLocation().equals(gm.getElectrical()) && !gm.canPlayerBuyAsset() && gm.getSvPlayer(player).getMoney() >= 1000)
			{
				gm.buyElectrical(player);
				gm.getSvPlayer(player).addMoney(-1000);
				
				if(e.getClickedBlock().getType().equals(Material.LEVER))
				{
					Switch lever = (Switch) e.getClickedBlock().getBlockData();
					lever.setPowered(!lever.isPowered());
					e.getClickedBlock().setBlockData(lever);
				}
			}
			
			if(item != null && item.getType().equals(Material.CARROT))
			{
				gm.getSvPlayer(player).openSupplyInventory();
			}
			
			if(!gm.isStarted() && item != null)
			{
				if(item.getType().equals(Material.SKELETON_SKULL))
				{
					gm.getSvPlayer(player).openDiffInventory();
				}
			}
			
			if((e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) && gm.getSvPlayer(player) != null)
			{
				List<Weapon> needsAmmo = new ArrayList<>();
				
				for(Weapon w : gm.getSvPlayer(player).getSimpleWeapons())
				{
					if(w.getAmmo() < w.getMaxAmmo())
					{
						needsAmmo.add(w);
					}
				}
				
				if(!needsAmmo.isEmpty())
				{
					BlockIterator itr = new BlockIterator(player.getEyeLocation().setDirection(player.getEyeLocation().getDirection()), 0.0D, 5);
					
					while(itr.hasNext())
					{
						Block block = (Block) itr.next();
						
						if(block.getType().equals(Material.CAKE))
						{
							MCUtils.armSwingAnimation(player, false);
							Cake cake = (Cake) block.getBlockData();
							
							for(int i = 0; i < Math.min(needsAmmo.size(), Survivor.CAKE_MAX_BITES - cake.getBites() + 1); ++i)
							{
								MCUtils.playSound(player.getLocation(), "guns.ammotake", 10.0F);
								if(needsAmmo.get(i).getAmmo() <= 0)
								{
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
				
				if(item != null && gm.getSvPlayer(player).getWeaponInHand() != null)
				{
					gm.getSvPlayer(player).onRightClick();
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
		if(GameManager.getInstance() != null && e.getEntity() instanceof Player p)
		{
			p.setFireTicks(0);
			Enemy damagerMob = null;
			if(e.getCause().equals(DamageCause.ENTITY_ATTACK) && GameManager.getInstance().getSvPlayer(p).isAlive())
			{
				Entity damager = ((EntityDamageByEntityEvent) e).getDamager();
				
				for(DamageModifier mod : DamageModifier.values())
					if(e.isApplicable(mod))
						e.setDamage(mod, 0);
				
				e.setDamage(DamageModifier.BASE, damager.getType().equals(EntityType.ZOMBIE) ? 2.0D : 1.0D);
				
				damagerMob = GameManager.getInstance().getMob(damager);
				
				if(damager instanceof Zombie && damagerMob instanceof PoisonousBoss)
				{
					p.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 200, 0));
				}
			}
			else if(e.getCause() != DamageCause.POISON)
			{
				e.setCancelled(true);
			}
			
			if(!e.isCancelled() && GameManager.getInstance().getSvPlayer(p).damage(e.getFinalDamage(), damagerMob, null, false, null, true))
			{
				e.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onInteractAtEntity(final PlayerInteractEntityEvent e) {
		if(GameManager.getInstance() != null)
		{
			GameManager gm = GameManager.getInstance();
			SvPlayer sp = gm.getSvPlayer(e.getPlayer());
			if(e.getRightClicked().getType().equals(EntityType.ITEM_FRAME) && (!((ItemFrame) e.getRightClicked()).getItem().getType().equals(Material.AIR) || !e.getPlayer().getGameMode().equals(GameMode.CREATIVE)) && gm.isStarted() && sp.isAlive())
			{
				e.setCancelled(true);
				
				for(WeaponType wt : WeaponType.values())
				{
					if(wt.getMaterial().equals(((ItemFrame) e.getRightClicked()).getItem().getType()))
					{
						((ItemFrame) e.getRightClicked()).setItem(wt.getItemToSell());
						if(wt.getPrice() <= sp.getMoney() && !sp.getWeaponTypes().contains(wt))
						{
							if(sp.getSimpleWeapons().size() >= (sp.getAtouts().contains(SvAsset.TROIS_ARME) ? 3 : 2))
							{
								for(Weapon w : new ArrayList<>(sp.getSimpleWeapons()))
								{
									if(e.getPlayer().getInventory().getItemInMainHand().equals(w.getItem()))
									{
										sp.removeWeapon(w);
										sp.getPlayer().getInventory().remove(w.getType().getMaterial());
										wt.getNewWeapon(sp).giveItem();
										sp.addMoney(-wt.getPrice());
									}
								}
							}
							else
							{
								wt.getNewWeapon(sp).giveItem();
								sp.addMoney(-wt.getPrice());
							}
						}
						else if(wt.getPrice() / 2 <= sp.getMoney() && sp.getWeaponTypes().contains(wt))
						{
							for(Weapon w : new ArrayList<>(sp.getSimpleWeapons()))
							{
								if(w.getType().equals(wt) && w.getAmmo() < w.getMaxAmmo())
								{
									w.setAmmo(w.getMaxAmmo());
									sp.addMoney(-wt.getPrice() / 2);
								}
							}
						}
					}
				}
				
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
						
						if(asset.getPrice() <= sp.getMoney() && !sp.getAtouts().contains(asset) && sp.getAtouts().size() < 4)
						{
							sp.getAtouts().add(asset);
							
							sp.getPlayer().sendMessage(Survivor.prefix + " §6Vous avez acheté l'atout §a" + asset.getName() + " §7(il apparait dans votre inventaire, appuyer sur votre touche de drop pour vous en débarrasser)");
							
							sp.addMoney(-asset.getPrice());
							if(asset.equals(SvAsset.MASTODONTE))
							{
								sp.getPlayer().setMaxHealth(GameManager.getInstance().getDifficulty().getMaxHealth() * 2.0D);
								sp.getPlayer().setHealth(GameManager.getInstance().getDifficulty().getMaxHealth() * 2.0D);
							}
							
							if(asset.equals(SvAsset.MARATHON))
							{
								sp.getPlayer().setWalkSpeed(0.3F);
							}
							
							sp.cleanInventory();
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
			SvPlayer sp = GameManager.getInstance().getSvPlayer(e.getPlayer());
			
			if(/*((LivingEntity) e.getPlayer()).isOnGround() && */sp != null)
			{
				Location tp = sp.checkNotOnBeacon();
				if(tp != null)
					e.setTo(tp);
			}
			
			if(sp != null && sp.isOnGround())
			{
				e.getTo().setX(e.getFrom().getX());
				e.getTo().setZ(e.getFrom().getZ());
				e.setTo(e.getTo());
			}
			
			if(GameManager.getInstance().getRooms().stream().anyMatch(room -> room.getFences().contains(e.getTo().getBlock().getLocation()) || room.getFences().contains(e.getTo().getBlock().getLocation().add(0.0D, 1.0D, 0.0D))))
				e.setCancelled(true);
			
			if(e.getPlayer().getGameMode().equals(GameMode.SPECTATOR))
			{
				if(!e.getPlayer().getWorld().equals(GameManager.getInstance().getWorld()) || Bukkit.getOnlinePlayers().stream().filter(player -> !player.getGameMode().equals(GameMode.SPECTATOR)).mapToDouble(player -> player.getLocation().distance(e.getTo())).min().orElse(0) > 100)
					e.getPlayer().teleport(Bukkit.getOnlinePlayers().stream().filter(player -> !player.getGameMode().equals(GameMode.SPECTATOR)).findFirst().get());
			}
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
	public void onJoin(PlayerJoinEvent e) {
		if(GameManager.getInstance() != null)
		{
			e.getPlayer().getInventory().clear();
			e.getPlayer().updateInventory();
			e.getPlayer().setWalkSpeed(0.2F);
			
			GameManager gm = GameManager.getInstance();
			
			SvPlayer svPlayer = gm.getOfflinePlayer(e.getPlayer().getUniqueId());
			gm.getOfflinePlayers().remove(svPlayer);
			boolean contains = svPlayer != null;
			
			if(contains)
				gm.getPlayers().add(svPlayer);
			
			e.setJoinMessage(Survivor.prefix + " §a+ §7" + e.getPlayer().getName() + " a rejoint la partie");
			e.getPlayer().getInventory().clear();
			
			if(!contains)
			{
				svPlayer = new SvPlayer(e.getPlayer());
				gm.getPlayers().add(svPlayer);
				svPlayer.setMoney((int) (Math.pow(gm.getWave(), 1.5D) * 50.0D) / 50 * 50);
				WeaponType.LITTLE_KNIFE.getNewWeapon(svPlayer).giveItem();
				WeaponType.M1911.getNewWeapon(svPlayer).giveItem();
			}
			
			e.getPlayer().setMaxHealth(gm.getDifficulty().getMaxHealth());
			
			Bukkit.getScheduler().runTaskLater(getPlugin(), () ->
			{
				e.getPlayer().teleport(gm.getSpawnpoint());
				e.getPlayer().setGameMode(GameMode.ADVENTURE);
			}, 1);
			
			svPlayer.cleanInventory();
		}
	}
	
	@EventHandler
	public void onDisconnect(PlayerQuitEvent e) {
		if(GameManager.getInstance() != null)
		{
			GameManager gm = GameManager.getInstance();
			SvPlayer sp = gm.getSvPlayer(e.getPlayer());
			
			if(gm.isStarted())
				e.setQuitMessage(Survivor.prefix + " §c- §7" + e.getPlayer().getName() + " a quitté la partie");
			else
				e.setQuitMessage(Survivor.prefix + " §c- §7" + e.getPlayer().getName() + " a quitté le lobby");
			
			if(sp == null)
				return;
			
			gm.getOfflinePlayers().add(sp);
			gm.getPlayers().remove(sp);
			if(gm.getPlayers().isEmpty() && gm.isStarted())
			{
				gm.endGame();
			}
			
		}
	}
	
	@EventHandler
	public void onInteractAtArmorStand(PlayerInteractAtEntityEvent e) {
		if(e.getRightClicked() instanceof ArmorStand)
		{
			e.setCancelled(true);
			MagicBoxManager mbm = GameManager.getInstance().getMagicBoxManager();
			ArmorStand magicBox = mbm.getMbTask().getClickableArmorStandWhenLaBoxEstOuverte();
			if(magicBox == null)
				magicBox = mbm.getMbTask().getClickableArmorStandWhenLaBoxEstPasOuverte();
			
			Optional<Door> clickedDoor = GameManager.getInstance().getRooms().stream().flatMap(room -> room.getDoors().stream()).filter(door -> !door.getRoom().isBought() && door.getClickableArmorStands().contains(e.getRightClicked())).findFirst();
			
			if(magicBox != null && !magicBox.isDead() && magicBox.equals(e.getRightClicked()))
			{
				MCUtils.armSwingAnimation(e.getPlayer(), false);
				this.onInteract(new PlayerInteractEvent(e.getPlayer(), Action.RIGHT_CLICK_BLOCK, e.getPlayer().getInventory().getItemInMainHand(), mbm.getBox(), BlockFace.EAST));
			}
			else if(clickedDoor.isPresent())
			{
				clickedDoor.get().buy(GameManager.getInstance().getSvPlayer(e.getPlayer()));
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
	public void onPacketSending(PacketEvent packetEvent) {
	
	}
	
	@Override
	public ListeningWhitelist getSendingWhitelist() {
		return null;
	}
	
	@Override
	public ListeningWhitelist getReceivingWhitelist() {
		return null;
	}
	
	@Override
	public Plugin getPlugin() {
		return Survivor.getInstance();
	}
}

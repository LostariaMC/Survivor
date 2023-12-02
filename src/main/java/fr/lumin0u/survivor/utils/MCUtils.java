package fr.lumin0u.survivor.utils;

import com.comphenix.protocol.PacketType.Play;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.EnumWrappers.ChatType;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import fr.lumin0u.survivor.DamageTarget;
import fr.lumin0u.survivor.GameManager;
import fr.lumin0u.survivor.Survivor;
import fr.lumin0u.survivor.player.SvDamageable;
import fr.lumin0u.survivor.player.WeaponOwner;
import fr.lumin0u.survivor.weapons.Weapon;
import fr.worsewarn.cosmox.api.players.WrappedPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TextComponent.Builder;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.Title.Times;
import net.kyori.adventure.util.Ticks;
import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class MCUtils
{
	/*public static void playSound(Location l, Sound sound, float maxDistance)
	{
		playSound(l, sound, maxDistance, 1);
	}
	
	public static void playSound(Location l, Sound sound, float maxDistance, float pitch)
	{
		playSound(l, sound.getKey().getKey(), maxDistance, pitch);
	}
	
	public static void playSound(Location l, String sound, float maxDistance)
	{
		playSound(l, sound, maxDistance, 1);
	}
	
	public static void playSound(Location l, String sound, float maxDistance, float pitch)
	{
		for(Player p : l.getWorld().getPlayers())
			playSound(p, l, sound, maxDistance, pitch);
	}
	
	public static void playSound(Player p, Location l, String sound, float maxDistance, float pitch)
	{
		if(!p.getWorld().equals(l.getWorld()) || p.getEyeLocation().distance(l) >= maxDistance)
			return;
		
		p.playSound(l, sound, maxDistance, pitch);
	}*/
	
	public static String vecToString(Vector loc)
	{
		return loc.getX() + "!" + loc.getY() + "!" + loc.getZ();
	}
	
	public static Vector stringToVec(String loc)
	{
		return loc.split("!").length < 4 ? new Vector(Double.parseDouble(loc.split("!")[0]), Double.parseDouble(loc.split("!")[1]), Double.parseDouble(loc.split("!")[2])) : new Vector(Double.parseDouble(loc.split("!")[1]), Double.parseDouble(loc.split("!")[2]), Double.parseDouble(loc.split("!")[3]));
	}
	
	public static String locToString(Location loc)
	{
		return loc.getWorld().getName().replace("_save", "") + "!" + loc.getX() + "!" + loc.getY() + "!" + loc.getZ();
	}
	
	public static String locDirToString(Location loc)
	{
		return loc.getWorld().getName().replace("_save", "") + "!" + loc.getX() + "!" + loc.getY() + "!" + loc.getZ() + "!" + loc.getDirection().getX() + "!" + loc.getDirection().getY() + "!" + loc.getDirection().getZ();
	}
	
	public static String locYawPitchToString(Location loc)
	{
		return loc.getWorld().getName().replace("_save", "") + "!" + loc.getX() + "!" + loc.getY() + "!" + loc.getZ() + "!" + loc.getYaw() + "!" + loc.getPitch();
	}
	
	public static Location stringToLoc(String loc)
	{
		try
		{
			if(loc.split("!").length < 6)
			{
				return new Location(Bukkit.getWorld(loc.split("!")[0]), Double.parseDouble(loc.split("!")[1]), Double.parseDouble(loc.split("!")[2]), Double.parseDouble(loc.split("!")[3]));
			}
			else
			{
				return loc.split("!").length < 7 ? new Location(Bukkit.getWorld(loc.split("!")[0]), Double.parseDouble(loc.split("!")[1]), Double.parseDouble(loc.split("!")[2]), Double.parseDouble(loc.split("!")[3]), Float.parseFloat(loc.split("!")[4]), Float.parseFloat(loc.split("!")[5])) : (new Location(Bukkit.getWorld(loc.split("!")[0]), Double.parseDouble(loc.split("!")[1]), Double.parseDouble(loc.split("!")[2]), Double.parseDouble(loc.split("!")[3]))).setDirection(new Vector(Double.parseDouble(loc.split("!")[4]), Double.parseDouble(loc.split("!")[5]), Double.parseDouble(loc.split("!")[6])));
			}
		} catch(Exception e)
		{
			debug("§cErreur lors de la transformation en Location : §r" + loc);
			throw new RuntimeException(e);
		}
	}
	
	public static Vector explosionVector(Location ent, Location l, double radius)
	{
		Location entLoc = ent.clone();
		double x = (entLoc.getX() - l.getX()) / entLoc.distance(l) * (radius - entLoc.distance(l));
		double y = (entLoc.getY() - l.getY()) / entLoc.distance(l) * (radius - entLoc.distance(l));
		double z = (entLoc.getZ() - l.getZ()) / entLoc.distance(l) * (radius - entLoc.distance(l));
		return (new Vector(x, y, z)).multiply(0.03D);
	}
	
	public static void explosionParticles(Location l, float rayon, int nbParticlesOfEach, Particle... particles)
	{
		Random r = new Random();
		double angle1 = (new Random()).nextDouble() * 2.0D * Math.PI;
		double angle2 = (new Random()).nextDouble() * 2.0D * Math.PI - 1.5707963267948966D;
		double x = Math.cos(angle1) * Math.cos(angle2);
		double z = Math.sin(angle1) * Math.cos(angle2);
		double y = Math.sin(angle2);
		float m = 0.0F;
		
		for(Particle particle : particles)
		{
			for(int i = 0; i < (particle.equals(Particle.EXPLOSION_LARGE) ? nbParticlesOfEach / 10 : nbParticlesOfEach); ++i)
			{
				angle1 = r.nextDouble() * 2.0D * Math.PI;
				angle2 = r.nextDouble() * 2.0D * Math.PI - 1.5707963267948966D;
				x = Math.cos(angle1) * Math.cos(angle2);
				z = Math.sin(angle1) * Math.cos(angle2);
				y = Math.sin(angle2);
				m = r.nextFloat();
				if(particle.equals(Particle.EXPLOSION_LARGE))
				{
					l.getWorld().spawnParticle(particle, l.clone().add(x * (double) rayon * 0.3D, y * (double) rayon * 0.3D, z * (double) rayon * 0.3D), 1);
				}
				else
				{
					l.getWorld().spawnParticle(particle, l, 0, (float) (x * (double) rayon / 10.0D * (double) m), (float) (y * (double) rayon / 10.0D * (double) m), (float) (z * (double) rayon / 10.0D * (double) m));
				}
			}
		}
		
	}
	
	public static Vector vectorFrom(Vector l1, Vector l2)
	{
		return l2.clone().subtract(l1);
	}
	
	public static Vector vectorFrom(Location l1, Location l2)
	{
		return l2.toVector().subtract(l1.toVector());
	}
	
	public static Vector rdVector(double magnitude)
	{
		double angle1 = new Random().nextDouble() * 2.0D * Math.PI;
		double angle2 = new Random().nextDouble() * 2.0D * Math.PI - 1.5707963267948966D;
		double x = Math.cos(angle1) * Math.cos(angle2);
		double z = Math.sin(angle1) * Math.cos(angle2);
		double y = Math.sin(angle2);
		return (new Vector(x, y, z)).multiply(magnitude);
	}
	
	public static boolean areSimilar(ItemStack i1, ItemStack i2)
	{
		return i1 == null && i2 == null
				|| i1 != null && i2 != null && i1.getType().equals(i2.getType()) && (i1.getItemMeta() == null && i2.getItemMeta() == null || ChatColor.stripColor(i1.getItemMeta().getDisplayName()).equalsIgnoreCase(ChatColor.stripColor(i2.getItemMeta().getDisplayName())));
	}
	
	public static void damageAnimation(Entity p)
	{
		playAnimation(p, 1, null);
	}
	
	public static void armSwingAnimation(Player p, boolean hideForHim)
	{
		playAnimation(p, 0, hideForHim ? Arrays.asList(p) : null);
	}
	
	public static void playAnimation(Entity entity, int anim, List<Player> exceptForThem)
	{
		try
		{
			PacketContainer packetAnimation = new PacketContainer(Play.Server.ANIMATION);
			packetAnimation.getIntegers()
					.write(0, entity.getEntityId())
					.write(1, anim);
			
			for(Player pl : entity.getWorld().getPlayers())
			{
				if(exceptForThem == null || !exceptForThem.contains(pl))
				{
					WrappedPlayer.of(pl).sendPacket(packetAnimation);
				}
			}
		} catch(Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public static void playBlockBreak(Location blockLoc, BlockData data)
	{
		playBlockBreak(blockLoc, data, blockLoc.getNearbyPlayers(100));
	}
	
	public static void playBlockBreak(Location blockLoc, BlockData data, Collection<? extends Player> players)
	{
		BlockPosition position = new BlockPosition(blockLoc.toVector());
		
		PacketContainer packet = new PacketContainer(Play.Server.WORLD_EVENT);
		packet.getIntegers().write(0, 2001);
		packet.getBlockPositionModifier().write(0, position);
		packet.getIntegers().write(1, NMSUtils.getFieldValue("id", data.getMaterial()));
		
		for(Player p : players)
		{
			WrappedPlayer.of(p).sendPacket(packet);
		}
	}
	
	public static void explosion(WeaponOwner damager, Weapon weapon, double centerDamage, Location l, double radius, double kb, DamageTarget damageTarget)
	{
		GameManager gm = GameManager.getInstance();
		
		for(SvDamageable mo : damageTarget.getDamageables(gm))
		{
			if(mo.getFeets().distance(l) <= radius)
			{
				double damage = (1 - Utils.square(mo.getFeets().distance(l) / radius)) * centerDamage;
				double m = 1;//Math.min(1, Math.max(0, 1 - TransparentUtils.solidBetween(l, mo.getFeets()) / 4));
				mo.damage(damage * m, damager, weapon, false, explosionVector(mo.getFeets(), l, radius).multiply(m * kb));
			}
		}
		
		TFSound.EXPLOSION.withVolume((float) radius / 16 * 3).play(l);
		l.getWorld().spawnParticle(radius > 5 ? Particle.EXPLOSION_HUGE : Particle.EXPLOSION_LARGE, l, 1, 0, 0, 0, 0, null, true);
		explosionParticles(l, (float) radius, (int) (centerDamage * radius / 2), Particle.FLAME, Particle.SMOKE_LARGE, Particle.CLOUD);
		Random r = new Random();
		
		for(int i = 0; (double) i < 3.0D * radius; ++i)
		{
			Location lavaLoc = l.clone().add(r.nextDouble() * 5 - 2.5D, r.nextDouble() * 5 - 2.5D, r.nextDouble() * 5 - 2.5D);
			l.getWorld().spawnParticle(Particle.LAVA, lavaLoc, 0);
		}
	}
	
	public static YamlConfiguration configFromFileName(String name)
	{
		YamlConfiguration file = null;
		File check = new File(Survivor.getInstance().getDataFolder(), name + ".yml");
		if(!Survivor.getInstance().getDataFolder().exists())
		{
			Survivor.getInstance().getDataFolder().mkdir();
		}
		
		if(!check.exists())
		{
			try
			{
				check.createNewFile();
			} catch(IOException e)
			{
				throw new RuntimeException(e);
			}
		}
		
		try
		{
			file = YamlConfiguration.loadConfiguration(check);
		} catch(NullPointerException e)
		{
			throw new RuntimeException(e);
		}
		
		return file;
	}
	
	public static void oneFlyingText(Location l, String text, long time)
	{
		oneFlyingText(l, text, time, 0.5);
	}
	
	public static void oneFlyingText(Location l, String text, long time, double minDistance)
	{
		oneConsistentFlyingText(l, text, time, minDistance);
	}
	
	public static ArmorStand oneConsistentFlyingText(Location l, String text)
	{
		ArmorStand as = (ArmorStand) l.getWorld().spawnEntity(l.clone().add(0.0D, -2.125D, 0.0D), EntityType.ARMOR_STAND);
		as.setVisible(false);
		as.setGravity(false);
		as.setCustomName(text);
		as.setCustomNameVisible(true);
		return as;
	}
	
	public static ArmorStand oneConsistentFlyingText(Location l, String text, long time, double minDistance)
	{
		ArmorStand as = (ArmorStand) l.getWorld().spawnEntity(l.clone().add(0.0D, -2.125D, 0.0D), EntityType.ARMOR_STAND);
		as.setVisible(false);
		as.setGravity(false);
		as.setCustomName(text);
		as.setCustomNameVisible(true);
		
		for(Player player : l.getWorld().getPlayers())
		{
			if(player.getEyeLocation().distance(l) < minDistance)
			{
				PacketContainer packet = new PacketContainer(Play.Server.ENTITY_DESTROY, new PacketPlayOutEntityDestroy(as.getEntityId()));
				WrappedPlayer.of(player).sendPacket(packet);
			}
		}
		
		new BukkitRunnable()
		{
			@Override
			public void run()
			{
				as.remove();
			}
		}.runTaskLater(Survivor.getInstance(), time);
		
		return as;
	}
	
	public static ItemStack addEnchantEffect(ItemStack item)
	{
		item.addUnsafeEnchantment(Enchantment.DURABILITY, 2);
		ItemMeta meta = item.getItemMeta();
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		item.setItemMeta(meta);
		return item;
	}
	
	public static Location middleLoc(List<Block> blocks)
	{
		List<Vector> locs = new ArrayList<>();
		
		for(Block b : blocks)
		{
			locs.add(b.getLocation().add(0.5D, 0.5D, 0.5D).toVector());
		}
		
		return middle(locs).toLocation(blocks.get(0).getWorld());
	}
	
	public static Vector middle(List<Vector> locs)
	{
		return new Vector(locs.stream().mapToDouble(Vector::getX).average().getAsDouble(), locs.stream().mapToDouble(Vector::getY).average().getAsDouble(), locs.stream().mapToDouble(Vector::getZ).average().getAsDouble());
	}
	
	public static String configToString(MemorySection yc)
	{
		if(yc == null)
		{
			return "";
		}
		else
		{
			StringBuilder txt = new StringBuilder();
			
			for(String key : yc.getKeys(true))
			{
				if(yc.isConfigurationSection(key))
				{
					txt.append("\n");
					
					txt.append("  ".repeat(Math.max(0, Utils.occurencesOf(".", key))));
					
					txt.append(key.split("\\.")[key.split("\\.").length - 1]).append(": ");
				}
				else
				{
					txt.append("\n");
					
					txt.append("  ".repeat(Math.max(0, Utils.occurencesOf(".", key))));
					
					txt.append(key.split("\\.")[key.split("\\.").length - 1]).append(": ").append(yc.get(key));
				}
			}
			
			return txt.toString();
		}
	}
	/*
	public static ItemStack newItem(Material mat, String displayName, List<String> lore)
	{
		ItemStack item = new ItemStack(mat);
		ItemMeta meta = item.getItemMeta();
		if(displayName != null)
		{
			meta.setDisplayName(displayName);
		}
		
		if(lore != null)
		{
			meta.setLore(lore);
		}
		
		item.setItemMeta(meta);
		return item;
	}*/
	
	public static void debug(Object debug)
	{
		Bukkit.broadcastMessage("§6[DEBUG] §r" + debug + " " + System.currentTimeMillis());
	}
	
	public static Set<Block> blocksOfSameTypeAround(Block aroundIt)
	{
		return blocksOfSameTypeAround(aroundIt, new ArrayList<>());
	}
	
	private static Set<Block> blocksOfSameTypeAround(Block aroundIt, List<Block> found)
	{
		Set<Block> blocks = new HashSet<>();
		blocks.add(aroundIt);
		
		for(int x = -1; x < 2; ++x)
		{
			for(int y = -1; y < 2; ++y)
			{
				for(int z = -1; z < 2; ++z)
				{
					if(Math.abs(x) + Math.abs(y) + Math.abs(z) == 1 && !found.contains(aroundIt.getLocation().clone().add((double) x, (double) y, (double) z).getBlock()) && aroundIt.getLocation().clone().add((double) x, (double) y, (double) z).getBlock().getType().equals(aroundIt.getType()))
					{
						blocks.add(aroundIt.getLocation().clone().add((double) x, (double) y, (double) z).getBlock());
						found.add(aroundIt.getLocation().clone().add((double) x, (double) y, (double) z).getBlock());
						blocks.addAll(blocksOfSameTypeAround(aroundIt.getLocation().clone().add((double) x, (double) y, (double) z).getBlock(), found));
					}
				}
			}
		}
		
		return blocks;
	}
	
	public static void sendJsonMessage(Player player, String json)
	{
		PacketContainer packetChat = new PacketContainer(Play.Server.CHAT);
		packetChat.getChatComponents().write(0, WrappedChatComponent.fromJson(json));
		packetChat.getChatTypes().write(0, ChatType.SYSTEM);
		
		WrappedPlayer.of(player).sendPacket(packetChat);
	}
	
	public static String getTitle(InventoryView view)
	{
		return getRawText(view.title());
	}
	
	public static String getRawText(Component component)
	{
		StringBuilder txt = new StringBuilder();
		component = component.compact();
		if(component instanceof TextComponent)
			txt.append(((TextComponent) component).content());
		for(Component child : component.children())
			if(child instanceof TextComponent)
				txt.append(((TextComponent) child).content());
		return txt.toString();
	}
	
	public static void sendTitle(Player player, int fadeIn, int stay, int fadeOut, String title)
	{
		player.showTitle(Title.title(Component.text(title), Component.empty(), Times.times(Ticks.duration(fadeIn), Ticks.duration(stay), Ticks.duration(fadeOut))));
	}
	
	public static void sendTitle(Player player, int fadeIn, int stay, int fadeOut, String title, String subTitle)
	{
		player.showTitle(Title.title(Component.text(title), Component.text(subTitle), Times.times(Ticks.duration(fadeIn), Ticks.duration(stay), Ticks.duration(fadeOut))));
	}
	
	public static void sendActionBar(Player player, String actionBar)
	{
		player.sendActionBar(Component.text(actionBar));
	}
	
	public static ItemStack newItem(Material material, String displayName, List<String> lore)
	{
		return new ItemBuilder(material).setDisplayName(displayName).setLore(lore).build();
	}
	
	private static Component componentOf(Object o)
	{
		if(o instanceof Component)
			return (Component) o;
		else if(o instanceof String s)
			return Component.text(s);
		else
			throw new IllegalArgumentException("Must be either Component or String");
	}
	
	public static TextComponent buildTextComponent(Object delimiter, Object... components)
	{
		Builder builder = Component.text();
		
		boolean first = true;
		
		for(Object component : components)
		{
			if(!first)
				builder.append(componentOf(delimiter));
			builder.append(componentOf(component));
			first = false;
		}
		
		return builder.build();
	}
	
	public static String pointingArrow(Location from, Location to)
	{
		if(from.getWorld() != to.getWorld() || from.distance(to) < 0.1)
			return "\u2022";
		
		Location nulLoc = new Location(null, 0, 0, 0);
		nulLoc.setDirection(vectorFrom(from, to));
		nulLoc.setYaw(from.getYaw() - nulLoc.getYaw());
		nulLoc.setDirection(nulLoc.getDirection());//pour remettre un yaw correct
		
		return arrowByYaw(nulLoc.getYaw());
	}
	
	public static String arrowByYaw(double yaw)
	{
		if(Double.isNaN(yaw))
			return "\u2022";
		
		final double cut = 45.0D;
		double split = 360;
		
		if(yaw < cut / 2 || yaw > 360 - cut / 2)
			return Utils.ARROW_N;
		
		String[] arrows = new String[] {Utils.ARROW_NE, Utils.ARROW_E, Utils.ARROW_SE, Utils.ARROW_S, Utils.ARROW_SW, Utils.ARROW_W, Utils.ARROW_NW};
		for(String arrow : arrows)
		{
			if(yaw > (split -= cut) - cut / 2 && yaw < split + cut / 2)
				return arrow;
		}
		return "\u2022";
	}
}

package fr.lumin0u.survivor.config;

import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.MinecraftKey;
import fr.lumin0u.survivor.Survivor;
import fr.lumin0u.survivor.utils.AABB;
import fr.lumin0u.survivor.utils.MCUtils;
import fr.lumin0u.survivor.utils.NMSUtils;
import fr.worsewarn.cosmox.api.players.WrappedPlayer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.EntityZombie;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Zombie;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class MapConfigRenderer
{
	private WrappedPlayer player;
	private MapConfig config;
	private World lastWorld;
	private List<ArmorStand> doorHolograms = new ArrayList<>();
	private List<Zombie> zombies = new ArrayList<>();
	private final BukkitRunnable particlesRunnable = new BukkitRunnable()
	{
		@Override
		public void run() {
			if(!player.isOnline())
			{
				stop();
				return;
			}
			
			if(!player.toBukkit().getWorld().equals(lastWorld))
			{
				update();
				lastWorld = player.toBukkit().getWorld();
			}
			
			for(Vector boxLocation : config.magicBoxes)
			{
				AABB box = new AABB(boxLocation, boxLocation.clone().add(new Vector(1, 1, 1)));
				MCUtils.explosionParticles(box.clone().multiply(Math.random() * 0.3D + 1.0D).rdContourLoc().toLocation(player.toBukkit().getWorld()), 0.0F, 3, Particle.SPELL_WITCH, Particle.ENCHANTMENT_TABLE);
			}
		}
	};
	
	private static Set<MapConfigRenderer> instances = new HashSet<>();
	
	public MapConfigRenderer(WrappedPlayer player, MapConfig config) {
		this.player = player;
		this.config = config;
		
		this.lastWorld = player.toBukkit().getWorld();
		
		showAll();
		
		instances.add(this);
		
		particlesRunnable.runTaskTimer(Survivor.getInstance(), 1, 1);
	}
	
	private void hideAll() {
		doorHolograms.forEach(org.bukkit.entity.Entity::remove);
		doorHolograms.clear();
		
		zombies.forEach(zombie ->
		{
			PacketContainer packetDestroy = new PacketContainer(Server.ENTITY_DESTROY, new PacketPlayOutEntityDestroy(zombie.getEntityId()));
			player.sendPacket(packetDestroy);
		});
		zombies.clear();
		
		PacketContainer custom = new PacketContainer(Server.CUSTOM_PAYLOAD);
		custom.getMinecraftKeys().write(0, new MinecraftKey("debug/game_test_clear"));
		player.sendPacket(custom);
	}
	
	private void showAll() {
		MCDebugUtil.sendHideBehindBlocksAlways(player);
		
		showDoorHolograms();
		showFences();
		showSpawners();
		showMagicBoxes();
	}
	
	private void showSpawners() {
		config.getRooms().stream().flatMap(room -> room.getMobSpawnsUnsafe().stream()).forEach(vector ->
		{
			Location location = vector.toLocation(player.toBukkit().getWorld());
			
			net.minecraft.world.level.World nmsWorld = NMSUtils.getHandle(location.getWorld());
			
			Zombie zombie = (Zombie) new EntityZombie(nmsWorld).getBukkitEntity();
			zombie.teleport(location);
			zombie.setGravity(false);
			zombie.setCustomNameVisible(true);
			zombie.setAI(false);
			
			PacketContainer zombiePacketSpawn = new PacketContainer(Server.SPAWN_ENTITY, new PacketPlayOutSpawnEntity((Entity) NMSUtils.getHandle(zombie)));
			
			player.sendPacket(zombiePacketSpawn);
			
			zombies.add(zombie);
		});
	}
	
	private void showDoorHolograms() {
		
		AtomicInteger i = new AtomicInteger();
		config.getRooms().stream().flatMap(room -> room.getDoors().stream()).forEach(door ->
		{
			Location middle = door.getMidLoc().toLocation(player.toBukkit().getWorld());
			
			World world = middle.getWorld();
			net.minecraft.world.level.World nmsWorld = NMSUtils.getHandle(middle.getWorld());
			
			ArmorStand asName = (ArmorStand) world.spawnEntity(middle.clone().add(0, -2, 0), EntityType.ARMOR_STAND);
			asName.setVisible(false);
			asName.setGravity(false);
			asName.setCustomName("§a" + door.getRoom().getName());
			asName.setCustomNameVisible(true);
			asName.setVisibleByDefault(false);
			
			ArmorStand asPrice = (ArmorStand) world.spawnEntity(middle.clone().add(0, -2.2, 0), EntityType.ARMOR_STAND);
			asPrice.setVisible(false);
			asPrice.setGravity(false);
			asPrice.setCustomName("§6" + (door.getRoom().hasPrice() ? door.getRoom().getPrice() : "?") + "$");
			asPrice.setCustomNameVisible(true);
			asPrice.setVisibleByDefault(false);
			
			player.toBukkit().showEntity(Survivor.getInstance(), asName);
			player.toBukkit().showEntity(Survivor.getInstance(), asPrice);
			
			/*PacketContainer asNamePacketSpawn = new PacketContainer(Server.SPAWN_ENTITY, new PacketPlayOutSpawnEntity(nmsName));
			PacketContainer asPricePacketSpawn = new PacketContainer(Server.SPAWN_ENTITY, new PacketPlayOutSpawnEntity(nmsPrice));
			PacketContainer asNamePacketMetadata = new PacketContainer(Server.ENTITY_METADATA);
			asNamePacketMetadata.getIntegers().write(0, asName.getEntityId());
			asNamePacketMetadata.getDataValueCollectionModifier().write(0, List.of(
					new WrappedDataValue(0, Registry.get(Byte.class), 0x20),
					new WrappedDataValue(2, Registry.getChatComponentSerializer(true), WrappedChatComponent.fromText("§a" + door.getRoom().getName())),
					new WrappedDataValue(3, Registry.get(Boolean.class), true),
					new WrappedDataValue(5, Registry.get(Boolean.class), true)
					));
			
			PacketContainer asPricePacketMetadata = new PacketContainer(Server.ENTITY_METADATA);
			asPricePacketMetadata.getIntegers().write(0, asPrice.getEntityId());
			asPricePacketMetadata.getDataValueCollectionModifier().write(0, List.of(
					new WrappedDataValue(0, Registry.get(Byte.class), 0x20),
					new WrappedDataValue(2, Registry.getChatComponentSerializer(true), WrappedChatComponent.fromText("§6" + (door.getRoom().hasPrice() ? door.getRoom().getPrice() : "?") + "$")),
					new WrappedDataValue(3, Registry.get(Boolean.class), true),
					new WrappedDataValue(5, Registry.get(Boolean.class), true)
			));
			
			player.sendPacket(asNamePacketSpawn);
			player.sendPacket(asPricePacketSpawn);
			new BukkitRunnable()
			{
				@Override
				public void run() {
					
					player.sendPacket(asNamePacketMetadata);
					player.sendPacket(asPricePacketMetadata);
				}
			}.runTaskLater(Survivor.getInstance(), 1);*/
			
			doorHolograms.add(asName);
			doorHolograms.add(asPrice);
			
			for(Block doorBlock : door.getBarsUnsafe().stream().map(vector -> vector.toLocation(player.toBukkit().getWorld()).getBlock()).toList())
			{
				if(doorBlock.getLocation().add(0.5, 0.5, 0.5).toVector().distance(door.getMidLoc()) < 1.5)
					continue;
				
				MCDebugUtil.sendBlockHighlight(player, new BlockHighlight(doorBlock, new Color(0, 255, 0, 127), "door_" + i.getAndIncrement(), 100000000));
			}
		});
	}
	
	private void showMagicBoxes() {
		int i = 0;
		for(Block magicBox : config.magicBoxes.stream().map(vector -> vector.toLocation(player.toBukkit().getWorld()).getBlock()).toList())
		{
			MCDebugUtil.sendBlockHighlight(player, new BlockHighlight(magicBox, new Color(255, 0, 255, 127), "magicbox_" + i++, 100000000));
		}
	}
	
	private void showFences() {
		int i = 0;
		for(Block fence : config.getRooms().stream().flatMap(room -> room.getFencesUnsafe().stream()).map(vector -> vector.toLocation(player.toBukkit().getWorld()).getBlock()).toList())
		{
			MCDebugUtil.sendBlockHighlight(player, new BlockHighlight(fence, new Color(0, 127, 0, 127), "fence_" + i++, 100000000));
		}
	}
	
	public void update() {
		hideAll();
		showAll();
		if(player.isOnline())
			player.toBukkit().sendMessage(ConfigUtil.toPlayerExplanation(config, player.toBukkit().getWorld()));
	}
	
	public static void stopAll() {
		new HashSet<>(instances).forEach(MapConfigRenderer::stop);
	}
	
	public void stop() {
		particlesRunnable.cancel();
		
		instances.remove(this);
		
		hideAll();
	}
	
	/**
	 * @author <a href="https://www.spigotmc.org/members/artfect.286670/">ArtFect</a>
	 */
	public static class MCDebugUtil
	{
		public static void sendHideBehindBlocks(WrappedPlayer player, int time) {
			sendBlockHighlight(player, BlockHighlight.getHideBehindBlocks(time));
		}
		
		public static void sendHideBehindBlocksAlways(WrappedPlayer player) {
			sendHideBehindBlocks(player, 1000000000);
		}
		
		private static void writeVarInt(ByteBuf packet, int i) {
			while((i & 0xFFFFFF80) != 0)
			{
				packet.writeByte(i & 0x7F | 0x80);
				i >>>= 7;
			}
			packet.writeByte(i);
		}
		
		private static void writeString(ByteBuf packet, String s) {
			byte[] abyte = s.getBytes(StandardCharsets.UTF_8);
			writeVarInt(packet, abyte.length);
			packet.writeBytes(abyte);
		}
		
		public static void sendBlockHighlight(WrappedPlayer player, BlockHighlight highlight) {
			ByteBuf packet = Unpooled.buffer();
			packet.writeLong((((long) highlight.x() & 0x3FFFFFF) << 38) | (((long) highlight.z() & 0x3FFFFFF) << 12) | (((long) highlight.y() & 0xFFF)));
			packet.writeInt(highlight.color());
			String text = highlight.text();
			writeString(packet, text);
			packet.writeInt(highlight.time());
			
			PacketContainer custom = new PacketContainer(Server.CUSTOM_PAYLOAD);
			custom.getMinecraftKeys().write(0, new MinecraftKey("debug/game_test_clear"));
			
			player.sendPacket(custom);
		}
		
		public static void sendStop(WrappedPlayer player) {
			PacketContainer custom = new PacketContainer(Server.CUSTOM_PAYLOAD);
			custom.getMinecraftKeys().write(0, new MinecraftKey("debug/game_test_clear"));
			
			player.sendPacket(custom);
		}
	}
	
	public record BlockHighlight(int x, int y, int z, int color, String text, int time)
	{
		public BlockHighlight(int x, int y, int z, Color color, String text, int time) {
			this(x, y, z, color.getRGB(), text, time);
		}
		
		public BlockHighlight(Block block, Color color, String text, int time) {
			this(block.getX(), block.getY(), block.getZ(), color.getRGB(), text, time);
		}
		
		public static BlockHighlight getHideBehindBlocks(int time) {
			return new BlockHighlight(0, 0, 0, new Color(0, 0, 0, 0), " ", time);
		}
	}
}

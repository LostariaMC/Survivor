package fr.lumin0u.survivor.player;

import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.EnumWrappers.EntityPose;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Registry;
import fr.lumin0u.survivor.Survivor;
import fr.worsewarn.cosmox.api.players.WrappedPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class LainBodies
{
	private static final Set<Integer> lainIds = new HashSet<>();
	
	public static void lie(Player p)
	{
		if(!p.isOnline())
			return;
		
		PacketContainer packetSwim = new PacketContainer(Server.ENTITY_METADATA);
		packetSwim.getIntegers().write(0, p.getEntityId());
		packetSwim.getDataValueCollectionModifier().write(0, List.of(new WrappedDataValue(6, Registry.get(EnumWrappers.getEntityPoseClass()), EntityPose.SWIMMING)));
		
		for(WrappedPlayer other : WrappedPlayer.of(p.getWorld().getPlayers()))
		{
			if(other.is(p))
				continue;
			
			other.sendPacket(packetSwim);
		}
		
		lainIds.add(p.getEntityId());
		
		/*
		try
		{
			playFakeBed(p);
		} catch(Exception var2)
		{
			var2.printStackTrace();
		}
		
	*/}
	
	public static void wakeUp(UUID uid)
	{
		if(Bukkit.getOfflinePlayer(uid).isOnline())
		{
			Player p = Bukkit.getPlayer(uid);
			
			for(WrappedPlayer other : WrappedPlayer.of(p.getWorld().getPlayers()))
			{
				if(other.is(p))
					continue;
				other.toBukkit().hidePlayer(Survivor.getInstance(), p);
				other.toBukkit().showPlayer(Survivor.getInstance(), p);
				
				/*//https://wiki.vg/Entity_metadata
				PacketContainer packetSwim = new PacketContainer(Server.ENTITY_METADATA);
				packetSwim.getIntegers().write(0, p.getEntityId());
				packetSwim.getDataValueCollectionModifier().write(0, List.of(new WrappedDataValue(6, WrappedDataWatcher.Registry.get(EntityPose.class), EntityPose.STANDING)));
				
				other.sendPacket(packetSwim);*/
			}
			
			lainIds.remove(p.getEntityId());
		}
		
		/*PacketContainer packetDestroy = new PacketContainer(Server.ENTITY_DESTROY);
		packetDestroy.getIntLists().write(0, Collections.singletonList(lainFakeId.get(uid)));
		Bukkit.getOnlinePlayers().forEach(pl -> MCUtils.WrappedPlayer.of(pl).sendPacket(packetDestroy));
		lainFakeId.remove(uid);*/
	}
	
	public static boolean isLain(int id)
	{
		return lainIds.contains(id);
	}
	
	public static void onDisconnect(int entityId)
	{
		lainIds.remove(entityId);
	}
	
	/*private static void playFakeBed(Player player) throws Exception
	{
		EntityPlayer entityPlayer = NMSUtils.getNMSEntity(player);
		
		int entityId = (new Random()).nextInt();
		
		double locY = player.getLocation().getY();
		
		WrappedGameProfile prof = WrappedGameProfile.fromPlayer(player);
		
		WrappedDataWatcher dw = new WrappedDataWatcher(player).deepClone();
		
		// player info
		//PacketContainer packetInfo = new PacketContainer(Server.PLAYER_INFO,
		//		new PacketPlayOutPlayerInfo((EnumPlayerInfoAction) EnumWrappers.getPlayerInfoActionConverter().getGeneric(PlayerInfoAction.ADD_PLAYER), List.of(entityPlayer)));
		
		// datawatcher
		PacketContainer packetMetadata = new PacketContainer(Server.ENTITY_METADATA);
		packetMetadata.getIntegers().write(0, entityId);
		packetMetadata.getWatchableCollectionModifier().write(0, dw.getWatchableObjects());
		
		// entity spawn
		PacketContainer packetEntitySpawn = new PacketContainer(Server.NAMED_ENTITY_SPAWN);
		packetEntitySpawn.getIntegers().write(0, entityId);
		packetEntitySpawn.getUUIDs().write(0, prof.getUUID());
		packetEntitySpawn.getDoubles()
				.write(0, player.getLocation().getX())
				.write(1, player.getLocation().getY())
				.write(2, player.getLocation().getZ());
		packetEntitySpawn.getBytes()
				.write(0, (byte) ((int) (player.getLocation().getYaw() * 256.0F / 360.0F)))
				.write(1, (byte) ((int) (player.getLocation().getPitch() * 256.0F / 360.0F)));
		
		// bed
		PacketContainer packetBed = new PacketContainer(Server.BED);
		packetBed.getIntegers().write(0, entityId);
		packetBed.getBlockPositionModifier().write(0, new BlockPosition(player.getLocation().getBlockX(), 0, player.getLocation().getBlockZ()));
		
		// teleport up
		PacketContainer packetTeleportUp = new PacketContainer(Server.ENTITY_TELEPORT);
		packetTeleportUp.getIntegers().write(0, entityId);
		packetTeleportUp.getDoubles()
				.write(0, player.getLocation().getX())
				.write(1, player.getLocation().getY())
				.write(2, player.getLocation().getZ());
		packetTeleportUp.getBytes()
				.write(0, (byte) ((int) (player.getLocation().getYaw() * 256.0F / 360.0F)))
				.write(1, (byte) ((int) (player.getLocation().getPitch() * 256.0F / 360.0F)));
		packetTeleportUp.getBooleans().write(0, true);
		
		// teleport down
		PacketContainer packetTeleportDown = new PacketContainer(Server.ENTITY_TELEPORT);
		packetTeleportDown.getIntegers().write(0, entityId);
		packetTeleportDown.getDoubles()
				.write(0, player.getLocation().getX())
				.write(1, 0.0)
				.write(2, player.getLocation().getZ());
		packetTeleportDown.getBytes()
				.write(0, (byte) ((int) (player.getLocation().getYaw() * 256.0F / 360.0F)))
				.write(1, (byte) ((int) (player.getLocation().getPitch() * 256.0F / 360.0F)));
		packetTeleportDown.getBooleans().write(0, true);
		
		Location bedLoc = player.getLocation().clone();
		bedLoc.setY(0);
		
		for(Player other : Bukkit.getOnlinePlayers())
		{
			other.sendBlockChange(bedLoc, Material.RED_BED.createBlockData());
			if(!other.equals(player))
			{
				other.hidePlayer(Survivor.getInstance(), player);
				//MCUtils.WrappedPlayer.of(other).sendPacket(packetInfo);
				MCUtils.WrappedPlayer.of(other).sendPacket(packetEntitySpawn);
				
				MCUtils.WrappedPlayer.of(other).sendPacket(packetMetadata);
				MCUtils.WrappedPlayer.of(other).sendPacket(packetTeleportDown);
				MCUtils.WrappedPlayer.of(other).sendPacket(packetBed);
				MCUtils.WrappedPlayer.of(other).sendPacket(packetTeleportUp);
			}
		}
	}*/
}

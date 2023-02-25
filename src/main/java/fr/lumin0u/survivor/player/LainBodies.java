package fr.lumin0u.survivor.player;

import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import fr.lumin0u.survivor.Survivor;
import fr.lumin0u.survivor.utils.MCUtils;
import fr.lumin0u.survivor.utils.NMSUtils;
import net.minecraft.server.level.EntityPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Random;
import java.util.UUID;

public class LainBodies
{
	//private static HashMap<UUID, Integer> lainFakeId = new HashMap<>();
	
	public LainBodies()
	{
	}
	
	public static void lie(Player p)
	{
		try
		{
			playFakeBed(p);
		} catch(Exception var2)
		{
			var2.printStackTrace();
		}
		
	}
	
	public static void wakeUp(UUID uid)
	{
		OfflinePlayer p = Bukkit.getOfflinePlayer(uid);
		if(p.isOnline())
		{
			Bukkit.getOnlinePlayers().forEach(pl -> pl.showPlayer(Survivor.getInstance(), p.getPlayer()));
		}
		
		/*PacketContainer packetDestroy = new PacketContainer(Server.ENTITY_DESTROY);
		packetDestroy.getIntLists().write(0, Collections.singletonList(lainFakeId.get(uid)));
		Bukkit.getOnlinePlayers().forEach(pl -> MCUtils.sendPacket(pl, packetDestroy));
		lainFakeId.remove(uid);*/
	}
	
	static void playFakeBed(Player player) throws Exception
	{
		EntityPlayer entityPlayer = NMSUtils.getNMSEntity(player);
		
		int entityId = (new Random()).nextInt();
		
		double locY = player.getLocation().getY();
		
		WrappedGameProfile prof = WrappedGameProfile.fromPlayer(player);
		
		WrappedDataWatcher dw = new WrappedDataWatcher(player).deepClone();
		
		// player info
		/*PacketContainer packetInfo = new PacketContainer(Server.PLAYER_INFO,
				new PacketPlayOutPlayerInfo((EnumPlayerInfoAction) EnumWrappers.getPlayerInfoActionConverter().getGeneric(PlayerInfoAction.ADD_PLAYER), List.of(entityPlayer)));*/
		
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
				//MCUtils.sendPacket(other, packetInfo);
				MCUtils.sendPacket(other, packetEntitySpawn);
				
				MCUtils.sendPacket(other, packetMetadata);
				MCUtils.sendPacket(other, packetTeleportDown);
				MCUtils.sendPacket(other, packetBed);
				MCUtils.sendPacket(other, packetTeleportUp);
			}
		}
	}
}

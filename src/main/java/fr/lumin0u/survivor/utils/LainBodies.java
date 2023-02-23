package fr.lumin0u.survivor.utils;

import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.*;
import com.comphenix.protocol.wrappers.EnumWrappers.NativeGameMode;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerInfoAction;
import fr.lumin0u.survivor.Survivor;
import net.minecraft.network.protocol.game.PacketPlayOutEntityStatus;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

public class LainBodies
{
	private static HashMap<UUID, Integer> lainFakeId = new HashMap<>();
	
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
	
	static void playFakeBed(Player p) throws Exception
	{
		int entityId = (new Random()).nextInt();
		lainFakeId.put(p.getUniqueId(), entityId);
		
		double locY = p.getLocation().getY();
		
		WrappedGameProfile prof = WrappedGameProfile.fromPlayer(p);
		
		WrappedDataWatcher dw = new WrappedDataWatcher(p).deepClone();
		
		// player info
		PacketContainer packetInfo = new PacketContainer(Server.PLAYER_INFO);
		packetInfo.getPlayerInfoAction().write(0, PlayerInfoAction.ADD_PLAYER);
		
		PlayerInfoData playerInfoData = new PlayerInfoData(prof, 0, NativeGameMode.ADVENTURE, WrappedChatComponent.fromText(""));
		packetInfo.getPlayerInfoDataLists().write(0, Collections.singletonList(playerInfoData));
		
		// datawatcher
		PacketContainer packetMetadata = new PacketContainer(Server.ENTITY_METADATA);
		packetMetadata.getIntegers().write(0, entityId);
		packetMetadata.getWatchableCollectionModifier().write(0, dw.getWatchableObjects());
		
		// entity spawn
		PacketContainer packetEntitySpawn = new PacketContainer(Server.NAMED_ENTITY_SPAWN);
		packetEntitySpawn.getIntegers().write(0, entityId);
		packetEntitySpawn.getUUIDs().write(0, prof.getUUID());
		packetEntitySpawn.getDoubles()
				.write(0, p.getLocation().getX())
				.write(1, p.getLocation().getY())
				.write(2, p.getLocation().getZ());
		packetEntitySpawn.getBytes()
				.write(0, (byte) ((int) (p.getLocation().getYaw() * 256.0F / 360.0F)))
				.write(1, (byte) ((int) (p.getLocation().getPitch() * 256.0F / 360.0F)));
		
		// bed
		PacketContainer packetBed = new PacketContainer(Server.BED);
		packetBed.getIntegers().write(0, entityId);
		packetBed.getBlockPositionModifier().write(0, new BlockPosition(p.getLocation().getBlockX(), 0, p.getLocation().getBlockZ()));
		
		// teleport up
		PacketContainer packetTeleportUp = new PacketContainer(Server.ENTITY_TELEPORT);
		packetTeleportUp.getIntegers().write(0, entityId);
		packetTeleportUp.getDoubles()
				.write(0, p.getLocation().getX())
				.write(1, p.getLocation().getY())
				.write(2, p.getLocation().getZ());
		packetTeleportUp.getBytes()
				.write(0, (byte) ((int) (p.getLocation().getYaw() * 256.0F / 360.0F)))
				.write(1, (byte) ((int) (p.getLocation().getPitch() * 256.0F / 360.0F)));
		packetTeleportUp.getBooleans().write(0, true);
		
		// teleport down
		PacketContainer packetTeleportDown = new PacketContainer(Server.ENTITY_TELEPORT);
		packetTeleportDown.getIntegers().write(0, entityId);
		packetTeleportDown.getDoubles()
				.write(0, p.getLocation().getX())
				.write(1, 0.0)
				.write(2, p.getLocation().getZ());
		packetTeleportDown.getBytes()
				.write(0, (byte) ((int) (p.getLocation().getYaw() * 256.0F / 360.0F)))
				.write(1, (byte) ((int) (p.getLocation().getPitch() * 256.0F / 360.0F)));
		packetTeleportDown.getBooleans().write(0, true);
		
		Location bedLoc = p.getLocation().clone();
		bedLoc.setY(0);
		
		for(Player player : Bukkit.getOnlinePlayers())
		{
			player.sendBlockChange(bedLoc, Material.RED_BED.createBlockData());
			if(!player.equals(p))
			{
				player.hidePlayer(Survivor.getInstance(), p);
				/*MCUtils.sendPacket(player, packetInfo);
				MCUtils.sendPacket(player, packetEntitySpawn);
				
				MCUtils.sendPacket(player, packetMetadata);
				MCUtils.sendPacket(player, packetTeleportDown);
				MCUtils.sendPacket(player, packetBed);
				MCUtils.sendPacket(player, packetTeleportUp);*/
			}
		}
	}
}

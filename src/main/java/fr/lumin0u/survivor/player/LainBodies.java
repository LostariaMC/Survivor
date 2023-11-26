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
	}
	
	public static void wakeUp(UUID uid) {
		if(Bukkit.getOfflinePlayer(uid).isOnline())
		{
			Player p = Bukkit.getPlayer(uid);
			
			for(WrappedPlayer other : WrappedPlayer.of(p.getWorld().getPlayers()))
			{
				if(other.is(p))
					continue;
				other.toBukkit().hidePlayer(Survivor.getInstance(), p);
				other.toBukkit().showPlayer(Survivor.getInstance(), p);
			}
			lainIds.remove(p.getEntityId());
		}
	}
	
	public static boolean isLain(int eid) {
		return lainIds.contains(eid);
	}
	
	public static void onDisconnect(WrappedPlayer player) {
		lainIds.remove(player.toBukkit().getEntityId());
	}
}

package fr.lumin0u.survivor.listeners;

import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.events.PacketContainer;
import fr.lumin0u.survivor.Difficulty;
import fr.lumin0u.survivor.GameManager;
import fr.lumin0u.survivor.Survivor;
import fr.lumin0u.survivor.commands.MoneyCommand;
import fr.lumin0u.survivor.commands.NoGameCommandExecutor;
import fr.lumin0u.survivor.commands.VoteSkipCommand;
import fr.lumin0u.survivor.utils.SurvivorParameters;
import fr.worsewarn.cosmox.API;
import fr.worsewarn.cosmox.api.players.WrappedPlayer;
import fr.worsewarn.cosmox.game.Phase;
import fr.worsewarn.cosmox.game.events.GameStartEvent;
import fr.worsewarn.cosmox.game.events.GameStopEvent;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.protocol.game.PacketPlayOutCustomPayload;
import net.minecraft.resources.MinecraftKey;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.nio.charset.StandardCharsets;

public class CosmoxEvents implements Listener
{
	@EventHandler
	public void onGameStart(GameStartEvent event)
	{
		Survivor plugin = Survivor.getInstance();
		
		if(!event.getGame().equals(plugin.getGame()))
			return;
		
		GameManager gm = new GameManager(event.getMap());
		gm.setDifficulty(Difficulty.values()[API.instance().getGameParameter(SurvivorParameters.DIFFICULTY)]);
		gm.startGame();
		plugin.getCosmoxAPI().getManager().setPhase(Phase.GAME);
		
		plugin.getCommand("money").setExecutor(new MoneyCommand());
		plugin.getCommand("voteSkip").setExecutor(new VoteSkipCommand());
		
		plugin.registerListeners();
	}
	
	@EventHandler
	public void onGameStop(GameStopEvent event)
	{
		Survivor plugin = Survivor.getInstance();
		
		if(plugin.getCosmoxAPI().getManager().getGame().getEritating().equals(plugin.getGame()))
		{
			plugin.unregisterListeners();
			Bukkit.getScheduler().cancelTasks(plugin);
			if(GameManager.getInstance() != null)
				GameManager.getInstance().destroy();
			
			plugin.getCommand("money").setExecutor(NoGameCommandExecutor.INSTANCE);
			plugin.getCommand("voteSkip").setExecutor(NoGameCommandExecutor.INSTANCE);
		}
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		WrappedPlayer.of(event.getPlayer()).sendPacket(new PacketContainer(Server.CUSTOM_PAYLOAD, new PacketPlayOutCustomPayload(new MinecraftKey("register"), new PacketDataSerializer(Unpooled.copiedBuffer("debug/game_test_clear\0debug/game_test_add_marker\0".getBytes(StandardCharsets.UTF_8))))));
	}
}

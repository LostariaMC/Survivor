package fr.lumin0u.survivor.listeners;

import fr.lumin0u.survivor.Difficulty;
import fr.lumin0u.survivor.GameManager;
import fr.lumin0u.survivor.Survivor;
import fr.lumin0u.survivor.SurvivorGame;
import fr.lumin0u.survivor.SurvivorGame.SurvivorParameters;
import fr.lumin0u.survivor.commands.MoneyCommand;
import fr.lumin0u.survivor.commands.NoGameCommandExecutor;
import fr.lumin0u.survivor.commands.VoteSkipCommand;
import fr.lumin0u.survivor.player.SvPlayer;
import fr.worsewarn.cosmox.API;
import fr.worsewarn.cosmox.game.Phase;
import fr.worsewarn.cosmox.game.events.GameDefaultItemUseEvent;
import fr.worsewarn.cosmox.game.events.GameStartEvent;
import fr.worsewarn.cosmox.game.events.GameStopEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CosmoxEvents implements Listener
{
	private Map<UUID, SvPlayer> waitingPlayers = new HashMap<>(10);
	
	@EventHandler
	public void onGameStart(GameStartEvent event)
	{
		Survivor plugin = Survivor.getInstance();
		
		if(!event.getGame().equals(plugin.getGame()))
			return;
		
		GameManager gm = new GameManager(event.getMap(), waitingPlayers.values());
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
			
			waitingPlayers.clear();
		}
	}
	
	private SvPlayer getSvPlayer(Player player) {
		if(!waitingPlayers.containsKey(player.getUniqueId()))
			waitingPlayers.put(player.getUniqueId(), new SvPlayer(player));
		
		return waitingPlayers.get(player.getUniqueId());
	}
	
	private void changeVote(Player player) {
		SvPlayer sp = getSvPlayer(player);
		
		do sp.setDiffVote(Difficulty.values()[(sp.getDiffVote().ordinal() + 1) % Difficulty.values().length]);
		while(sp.getDiffVote() == Difficulty.NOT_SET);
		
		player.sendMessage(SurvivorGame.prefix + "§7Vous changez votre vote pour : " + sp.getDiffVote().getColoredDisplayName());
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		if(event.getAction().isRightClick() && SurvivorGame.DIFF_VOTE_ITEM.equals(event.getItem())) {
			changeVote(event.getPlayer());
		}
		if(event.getAction().isLeftClick() && SurvivorGame.DIFF_VOTE_ITEM.equals(event.getItem())) {
			event.getPlayer().sendMessage(SurvivorGame.prefix + "§7Votre vote actuel est : " + getSvPlayer(event.getPlayer()).getDiffVote().getColoredDisplayName());
		}
	}
	
	@EventHandler
	public void onUseLobbyItem(GameDefaultItemUseEvent event) {
		if(event.getIdentifier().equals("diffVoteItem"))
			changeVote(event.getPlayer());
	}
}

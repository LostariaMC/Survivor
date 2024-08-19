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
import fr.lumin0u.survivor.utils.ItemBuilder;
import fr.lumin0u.survivor.utils.MCUtils;
import fr.worsewarn.cosmox.API;
import fr.worsewarn.cosmox.game.Phase;
import fr.worsewarn.cosmox.game.events.GameDefaultItemUseEvent;
import fr.worsewarn.cosmox.game.events.GameStartEvent;
import fr.worsewarn.cosmox.game.events.GameStopEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Optional;

public class CosmoxEvents implements Listener
{
	private Inventory difficultyPanel;
	
	public CosmoxEvents()
	{
		difficultyPanel = Bukkit.createInventory(null, 9, "Choisissez une difficulté");
		
		ItemStack grayGlass = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setDisplayName(" ").build();
		for(int i = 0; i < 9; i++) {
			difficultyPanel.setItem(i, grayGlass);
		}
		
		difficultyPanel.setItem(2, Difficulty.EASY.getItemRep(0));
		difficultyPanel.setItem(3, Difficulty.NORMAL.getItemRep(0));
		difficultyPanel.setItem(5, Difficulty.CLASSIC.getItemRep(0));
		difficultyPanel.setItem(6, Difficulty.HARDCORE.getItemRep(0));
	}
	
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
	public void onInventoryClick(InventoryClickEvent event) {
		if(difficultyPanel.equals(event.getClickedInventory())) {
			event.setCancelled(true);
			
			Optional<Difficulty> clicked = Arrays.stream(Difficulty.values()).filter(d -> event.getCurrentItem() != null && event.getCurrentItem().getType() == d.getSkullType()).findAny();
			clicked.ifPresent(diff ->
			{
				SvPlayer player = SvPlayer.of(event.getWhoClicked());
				event.getWhoClicked().sendMessage(SurvivorGame.prefix + "§7Vous votez pour la difficulté " + diff.getColoredDisplayName());
				updateVoteCount(player.getDiffVote(), (int) Survivor.getInstance().getLoadedSvPlayers().stream().filter(sp -> sp.getDiffVote() == player.getDiffVote()).count() - 1);
				player.setDiffVote(diff);
				updateVoteCount(diff, (int) Survivor.getInstance().getLoadedSvPlayers().stream().filter(sp -> sp.getDiffVote() == diff).count());
			});
		}
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		if(event.getAction().isRightClick() && MCUtils.areSimilar(SurvivorGame.DIFF_VOTE_ITEM, event.getItem())) {
			event.getPlayer().openInventory(difficultyPanel);
		}
		if(event.getAction().isLeftClick() && SurvivorGame.DIFF_VOTE_ITEM.equals(event.getItem())) {
			event.getPlayer().sendMessage(SurvivorGame.prefix + "§7Votre vote actuel est : " + SvPlayer.of(event.getPlayer()).getDiffVote().getColoredDisplayName());
		}
	}
	
	@EventHandler
	public void onUseLobbyItem(GameDefaultItemUseEvent event) {
		if(event.getIdentifier().equals("diffVoteItem"))
			event.getPlayer().openInventory(difficultyPanel);
	}
	
	public void updateVoteCount(Difficulty diff, int votes) {
		int place = switch(diff) {
            case NOT_SET -> 0;
            case EASY -> 2;
            case NORMAL -> 3;
            case CLASSIC -> 5;
            case HARDCORE -> 6;
        };
		
		difficultyPanel.setItem(place, diff.getItemRep(votes));
	}
}

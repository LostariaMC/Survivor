package fr.lumin0u.survivor;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import fr.lumin0u.survivor.commands.NoGameCommandExecutor;
import fr.lumin0u.survivor.commands.SurvivorCommand;
import fr.lumin0u.survivor.config.MapConfig;
import fr.lumin0u.survivor.config.MapConfigCreation;
import fr.lumin0u.survivor.config.MapConfigRenderer;
import fr.lumin0u.survivor.listeners.CosmoxEvents;
import fr.lumin0u.survivor.listeners.EntityEvents;
import fr.lumin0u.survivor.listeners.InventoryEvents;
import fr.lumin0u.survivor.listeners.PlayerEvents;
import fr.lumin0u.survivor.player.SvPlayer;
import fr.lumin0u.survivor.utils.MCUtils;
import fr.lumin0u.survivor.utils.TransparentUtils;
import fr.lumin0u.survivor.utils.Utils;
import fr.worsewarn.cosmox.API;
import fr.worsewarn.cosmox.api.players.WrappedPlayer;
import fr.worsewarn.cosmox.api.players.WrappedPlayer.PlayerWrapper;
import fr.worsewarn.cosmox.game.Game;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Survivor extends JavaPlugin
{
	private static Survivor instance;
	private PlayerEvents playerListener;
	private EntityEvents entityListener;
	private InventoryEvents inventoryListener;
	public static int currentTick;
	private ProtocolManager protocolManager;
	private API cosmoxAPI;
	private final Map<WrappedPlayer, MapConfigCreation> mapConfigs = new HashMap<>();
	private final List<SvPlayer> svPlayers = new ArrayList<>();
	
	public static int CAKE_MAX_BITES = 7;
	
	private Game survivorGame;
	
	public Survivor()
	{
		WrappedPlayer.registerType(new PlayerWrapper<SvPlayer>(SvPlayer.class) {
			@Override
			public SvPlayer unWrap(java.util.UUID uuid) {
				return getSvPlayer(uuid);
			}
			
			@Override
			public java.util.UUID wrap(SvPlayer svPlayer) {
				return svPlayer == null ? null : svPlayer.getUniqueId();
			}
		});
	}
	
	public Game getGame() {
		return survivorGame;
	}
	
	@Override
	public void onEnable()
	{
		/*
		*/
		
		protocolManager = ProtocolLibrary.getProtocolManager();
		
		instance = this;
		cosmoxAPI = API.instance();
		
		if(!cosmoxAPI.isBuildingServer()) {
			survivorGame = SurvivorGame.get();
			cosmoxAPI.registerNewGame(survivorGame);
		}
		
		getServer().getPluginManager().registerEvents(new CosmoxEvents(), this);
		
		FileConfiguration transparentConfig = MCUtils.configFromFileName("transparents");
		transparentConfig.set("CAKE_BLOCK.d*", "000040");
		transparentConfig.set("CAKE_BLOCK.full", false);
		
		transparentConfig.set("BEACON.d*", "");
		transparentConfig.set("BEACON.full", false);
		
		transparentConfig.set("BARRIER.d*", "");
		transparentConfig.set("BARRIER.full", false);
		TransparentUtils.setConfiguration(transparentConfig);
		
		SurvivorCommand svcommand = new SurvivorCommand();
		
		getCommand("Sv").setExecutor(svcommand);
		getCommand("Sv").setTabCompleter(svcommand);
		getCommand("money").setExecutor(NoGameCommandExecutor.INSTANCE);
		getCommand("voteSkip").setExecutor(NoGameCommandExecutor.INSTANCE);
		
		if(!getPluginConfigDir().exists())
			getPluginConfigDir().mkdir();
		
		File mapConfigFile = MapConfig.getSaveFile();
		if(!mapConfigFile.exists())
		{
			try
			{
				mapConfigFile.createNewFile();
				FileWriter fw = new FileWriter(mapConfigFile);
				fw.write("{}");
				fw.flush();
				fw.close();
			} catch(IOException e)
			{
				throw new RuntimeException(e);
			}
		}
	}
	
	public void registerListeners()
	{
		playerListener = new PlayerEvents();
		entityListener = new EntityEvents();
		inventoryListener = new InventoryEvents();
		
		protocolManager.addPacketListener(playerListener);
		
		getServer().getPluginManager().registerEvents(this.entityListener, this);
		getServer().getPluginManager().registerEvents(this.playerListener, this);
		getServer().getPluginManager().registerEvents(this.inventoryListener, this);
	}
	
	public void unregisterListeners()
	{
		protocolManager.removePacketListener(playerListener);
		
		HandlerList.unregisterAll(entityListener);
		HandlerList.unregisterAll(playerListener);
		HandlerList.unregisterAll(inventoryListener);
	}
	
	@Override
	public void onDisable()
	{
		/*
		if(this.gameManager != null)
		{
			this.gameManager.saveInConfig();
			
			for(TurretRunnable tur : TurretRunnable.runningInstances)
			{
				tur.cancel();
			}
		}
		*/
		this.saveConfig();
		
		MapConfigRenderer.stopAll();
	}
	
	public static Survivor getInstance()
	{
		return instance;
	}
	
	public List<File> getPossibleWorlds()
	{
		List<File> worlds = new ArrayList<>();
		
		for(File f : this.getServer().getWorldContainer().listFiles())
		{
			if(f.isDirectory() && (new File(f.getAbsolutePath() + "/level.dat")).exists())
			{
				worlds.add(f);
			}
		}
		
		return worlds;
	}
	
	public PlayerEvents getPlayerEvents()
	{
		return this.playerListener;
	}
	
	public int getRankPower(CommandSender commandSender)
	{
		return commandSender.isOp() ? 100 : 0;
	}
	
	public static int getCurrentTick()
	{
		return currentTick;
	}
	
	public ProtocolManager getProtocolManager()
	{
		return protocolManager;
	}
	
	public API getCosmoxAPI() {
		return cosmoxAPI;
	}
	
	public Map<WrappedPlayer, MapConfigCreation> getInCreationMapConfigs() {
		return mapConfigs;
	}
	
	public MapConfig getMapConfig(WrappedPlayer player) {
		return Utils.ifNotNullApply(mapConfigs.get(player), MapConfigCreation::config);
	}
	
	public String getMapConfigName(WrappedPlayer player) {
		return Utils.ifNotNullApply(mapConfigs.get(player), MapConfigCreation::name);
	}
	
	public MapConfigRenderer getMapConfigRenderer(WrappedPlayer player) {
		return Utils.ifNotNullApply(mapConfigs.get(player), MapConfigCreation::renderer);
	}
	
	public static File getPluginConfigDir() {
		return new File("/srv/MinecraftServer/dev/config/plugins/release/Survivor");
	}
	
	public Collection<SvPlayer> getLoadedSvPlayers() {
		return new ArrayList<>(svPlayers);
	}
	
	public SvPlayer getSvPlayer(Player p) {
		return getSvPlayer(p.getUniqueId());
	}
	
	public SvPlayer getSvPlayer(UUID uid) {
		for(SvPlayer sp : svPlayers)
			if(sp.getUniqueId().equals(uid))
				return sp;
		
		SvPlayer sp = new SvPlayer(uid);
		svPlayers.add(sp);
		return sp;
	}
}

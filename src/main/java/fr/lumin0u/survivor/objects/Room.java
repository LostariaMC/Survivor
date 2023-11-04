package fr.lumin0u.survivor.objects;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import fr.lumin0u.survivor.GameManager;
import fr.lumin0u.survivor.Survivor;
import fr.lumin0u.survivor.SurvivorGame;
import fr.lumin0u.survivor.mobs.mob.Enemy;
import fr.lumin0u.survivor.player.SvPlayer;
import fr.lumin0u.survivor.utils.MCUtils;
import fr.lumin0u.survivor.utils.TFSound;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Fence;
import org.bukkit.block.data.type.Gate;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

public class Room
{
	private String name;
	private List<Door> doors;
	private List<Vector> mobSpawns;
	private List<Vector> fences;
	private List<Vector> additionalBlocks;
	private boolean hasPrice;
	private int price;
	
	private World world;
	private boolean bought;
	private final Map<Block, BlockData> fenceDirections;
	
	public Room(String name, World world)
	{
		this(name);
		this.world = world;
	}
	
	private Room(String name)
	{
		this.name = name;
		this.doors = new ArrayList<>();
		this.mobSpawns = new ArrayList<>();
		this.fences = new ArrayList<>();
		this.fenceDirections = new HashMap<>();
	}
	
	public static Room unsafe(String name)
	{
		return new Room(name);
	}
	
	public void startZombieVsFencesTask()
	{
		final Random r = new Random();
		fenceDirections.putAll(getFences().stream().collect(Collectors.toMap(Location::getBlock, l -> l.getBlock().getBlockData())));
		
		new BukkitRunnable() {
			@Override
			public void run() {
				List<Location> fencesDone = new ArrayList<>();
				
				for(Enemy m : GameManager.getInstance().getMobs()) {
					for(Location fence : getFences()) {
						if(fence.distance(m.getEntity().getLocation()) < 4.0D && fence.getBlock().getBlockData() instanceof Fence && !fencesDone.contains(fence)) {
							fencesDone.add(fence);
							if(r.nextInt(7) == 0) {
								placeGate(fence);
							}
							
							if(r.nextInt(2) == 1) {
								for(int i = 0; i < 10; ++i) {
									fence.getWorld().spawnParticle(Particle.BLOCK_CRACK, fence.clone().add(r.nextDouble(), r.nextDouble(), r.nextDouble()), 5, fence.getBlock().getBlockData());
								}
							}
							
							TFSound.ZOMBIE_ATTACK_FENCE.play(fence);
						}
					}
				}
			}
		}.runTaskTimer(Survivor.getInstance(), 20L, 20L);
	}
	
	public boolean isDefault() {
		return name.equals("default");
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	public List<Door> getDoors()
	{
		return this.doors;
	}
	
	public void addDoor(Door door)
	{
		doors.add(door);
	}
	
	public List<Location> getMobSpawns()
	{
		return mobSpawns.stream().map(v -> v.toLocation(world)).toList();
	}
	
	public List<Vector> getMobSpawnsUnsafe()
	{
		return mobSpawns;
	}
	
	public List<Location> getFences()
	{
		return fences.stream().map(bv -> bv.toLocation(world)).toList();
	}
	
	public List<Vector> getFencesUnsafe()
	{
		return fences;
	}
	
	public boolean isBought()
	{
		return this.bought;
	}
	
	public void setBought(boolean bought)
	{
		this.bought = bought;
	}
	
	public void buy(SvPlayer sp)
	{
		if(sp.getMoney() >= getPrice() && sp.isAlive())
		{
			setBought(true);
			sp.addMoney(-getPrice());
			
			for(Door d : getDoors())
			{
				d.remove();
			}
			
			GameManager.getInstance().augmentPrice();
			Bukkit.broadcastMessage(SurvivorGame.prefix + "§5" + Bukkit.getOfflinePlayer(sp.getPlayerUid()).getName() + " §ea acheté §6" + getName());
		}
		
		for(Location block : getAdditionalBlocks())
		{
			MCUtils.playBlockBreak(block, block.getBlock().getBlockData());
			
			block.getBlock().setType(Material.AIR);
		}
	}
	
	public void setPrice(int price)
	{
		this.hasPrice = true;
		this.price = price;
	}
	
	public void removePrice()
	{
		this.hasPrice = false;
	}
	
	public List<Location> getAdditionalBlocks()
	{
		return additionalBlocks.stream().map(bv -> bv.toLocation(world)).toList();
	}
	
	public List<Vector> getAdditionalBlocksUnsafe()
	{
		return additionalBlocks;
	}
	
	public void placeFence(Location loc)
	{
		boolean isGate = loc.getBlock().getBlockData() instanceof Gate;
		
		/*if(isGate)
		{
			Material material = Material.getMaterial(loc.getBlock().getType().name().replaceAll("_GATE$", ""));
			
			loc.getBlock().setType(material);
		}
		else if(!(loc.getBlock().getBlockData() instanceof Fence))
		{
			loc.getBlock().setType(Material.OAK_FENCE);
		}*/
		
		loc.getBlock().setBlockData(fenceDirections.get(loc.getBlock()));
	}
	
	public void placeGate(Location loc)
	{
		boolean isFence = loc.getBlock().getBlockData() instanceof Fence;
		
		if(isFence)
		{
			Material material = Material.getMaterial(loc.getBlock().getType().name() + "_GATE");
			
			Gate gate = (Gate) material.createBlockData();
			gate.setOpen(true);
			
			loc.getBlock().setBlockData(gate);
		}
		else if(!(loc.getBlock().getBlockData() instanceof Gate))
		{
			loc.getBlock().setType(Material.OAK_FENCE_GATE);
		}
	}
	
	public void updateDoors()
	{
		for(Door door : this.doors)
		{
			door.showNameAndPrice();
			if(!this.bought)
			{
				door.place();
			}
		}
	}
	
	public int getPrice() {
		return this.hasPrice ? this.price : GameManager.getInstance().getDoorPrice();
	}
	
	public boolean hasPrice() {
		return this.hasPrice;
	}
	
	public void setWorld(World world) {
		this.world = world;
		for(Door door : doors)
			door.setWorld(world);
	}
	
	public static class RoomAdapter implements JsonSerializer<Room>, JsonDeserializer<Room>
	{
		@Override
		public Room deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException
		{
			JsonObject jsonObject = (JsonObject) jsonElement;
			
			Room room = Room.unsafe(jsonObject.get("name").getAsString());
			room.fences = context.<List<Vector>>deserialize(jsonObject.get("fences"), new TypeToken<List<Vector>>() {}.getType());
			room.mobSpawns = context.<List<Vector>>deserialize(jsonObject.get("mobSpawns"), new TypeToken<List<Vector>>() {}.getType());
			room.doors = context.<List<Door>>deserialize(jsonObject.get("doors"), new TypeToken<List<Door>>() {}.getType());
			room.doors.forEach(door -> door.setRoom(room));
			room.hasPrice = jsonObject.has("price");
			
			if(room.hasPrice)
				room.price = jsonObject.get("price").getAsInt();
			
			if(jsonObject.has("additionalBlocks"))
				room.additionalBlocks = context.<List<Vector>>deserialize(jsonObject.get("additionalBlocks"), new TypeToken<List<Vector>>() {}.getType());
			else
				room.additionalBlocks = new ArrayList<>();
			
			return room;
		}
		
		@Override
		public JsonElement serialize(Room room, Type type, JsonSerializationContext context)
		{
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("name", room.name);
			jsonObject.add("doors", context.serialize(room.doors));
			jsonObject.add("fences", context.serialize(room.fences));
			jsonObject.add("mobSpawns", context.serialize(room.mobSpawns));
			jsonObject.add("additionalBlocks", context.serialize(room.additionalBlocks));
			
			if(room.hasPrice) {
				jsonObject.addProperty("price", room.price);
			}
			
			return jsonObject;
		}
	}
}

package fr.lumin0u.survivor.objects;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import fr.lumin0u.survivor.GameManager;
import fr.lumin0u.survivor.SurvivorGame;
import fr.lumin0u.survivor.player.SvPlayer;
import fr.lumin0u.survivor.utils.MCUtils;
import fr.lumin0u.survivor.utils.TFSound;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Door
{
	private final List<Vector> bars;
	private final Material material;
	private Room room;
	private ArmorStand asName;
	private ArmorStand asPrice;
	private World world;
	
	public Door(Room room, List<Vector> bars, World world, Material material)
	{
		this(room, bars, material);
		this.world = world;
	}
	
	private Door(Room room, List<Vector> bars, Material material)
	{
		this.room = room;
		if(bars.isEmpty())
			throw new IllegalArgumentException("§cLa porte de la salle §6" + room.getName() + " §cn'a pas de barres");
		
		this.bars = bars;
		this.material = material;
	}
	
	public static Door unsafe(Room room, List<Vector> bars, Material material)
	{
		return new Door(room, bars, material);
	}
	
	public Room getRoom()
	{
		return this.room;
	}
	
	public List<ArmorStand> getClickableArmorStands()
	{
		if(!room.isBought())
			return Arrays.asList(asName, asPrice);
		return Collections.emptyList();
	}
	
	public List<Block> getBars()
	{
		return bars.stream().map(bv -> bv.toLocation(world).getBlock()).toList();
	}
	
	public List<Vector> getBarsUnsafe()
	{
		return bars;
	}
	
	public void tryBuy(SvPlayer sp)
	{
		if(sp.isAlive()) {
			if(sp.getMoney() >= room.getPrice()) {
				room.buy(sp);
			}
			else {
				TFSound.CANT_AFFORD.playTo(sp);
				String message = "§cVous n'avez pas assez d'argent pour acheter cette porte (requis: " + room.getPrice() + ") !";
				sp.toBukkit().sendMessage(SurvivorGame.prefix + message);
			}
		}
	}
	
	public void break_()
	{
		for(Block bar : this.getBars())
		{
			MCUtils.playBlockBreak(bar.getLocation(), bar.getBlockData());
			
			bar.setType(Material.AIR);
		}
	}
	
	public void removeArmorStands()
	{
		if(this.asName != null)
		{
			this.asName.remove();
		}
		
		if(this.asPrice != null)
		{
			this.asPrice.remove();
		}
	}
	
	public Vector getMidLoc()
	{
		return MCUtils.middle(bars);
	}
	
	@Deprecated
	public void place()
	{
		for(Block bar : getBars())
		{
			if(bar.getType() != material)
			{
				if(material.createBlockData() instanceof MultipleFacing multipleFacing)
				{
					for(BlockFace face : List.of(BlockFace.EAST, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.WEST))
						multipleFacing.setFace(face, true);
					bar.setBlockData(multipleFacing);
				}
				else {
					bar.setType(material);
				}
			}
		}
	}
	
	public void showNameAndPrice()
	{
		if(GameManager.getInstance() != null)
		{
			Location middle = MCUtils.middleLoc(this.getBars());
			if(this.asName != null)
			{
				this.asName.remove();
			}
			
			if(this.asPrice != null)
			{
				this.asPrice.remove();
			}
			
			this.asName = (ArmorStand) middle.getWorld().spawnEntity(middle.clone().add(0.0D, -2.0D, 0.0D), EntityType.ARMOR_STAND);
			this.asPrice = (ArmorStand) middle.getWorld().spawnEntity(middle.clone().add(0.0D, -2.2D, 0.0D), EntityType.ARMOR_STAND);
			
			this.asName.setVisible(false);
			this.asName.setGravity(false);
			this.asName.setCustomName("§a" + this.room.getName());
			this.asName.setCustomNameVisible(true);
			
			this.asPrice.setVisible(false);
			this.asPrice.setGravity(false);
			this.asPrice.setCustomName("§6" + this.room.getPrice() + "$");
			this.asPrice.setCustomNameVisible(true);
		}
	}
	
	@Deprecated
	public void setRoom(Room room) {
		this.room = room;
	}
	
	public void setWorld(World world) {
		this.world = world;
	}
	
	public static class DoorAdapter implements JsonSerializer<Door>, JsonDeserializer<Door>
	{
		@Override
		public Door deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException
		{
			JsonObject jObject = (JsonObject) jsonElement;
			List<Vector> bars = context.<List<Vector>>deserialize(jObject.get("bars"), new TypeToken<List<Vector>>() {}.getType());
			
			Material material = jObject.has("material") ? Material.valueOf(jObject.get("material").getAsString()) : Material.IRON_BARS;
			
			return Door.unsafe(null, bars, material);
		}
		
		@Override
		public JsonElement serialize(Door door, Type type, JsonSerializationContext jsonSerializationContext)
		{
			JsonObject jsonObject = new JsonObject();
			jsonObject.add("bars", jsonSerializationContext.serialize(door.bars));
			jsonObject.add("material", jsonSerializationContext.serialize(door.material.name()));
			return jsonObject;
		}
	}
}

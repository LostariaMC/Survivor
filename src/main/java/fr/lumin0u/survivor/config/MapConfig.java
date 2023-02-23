package fr.lumin0u.survivor.config;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import fr.lumin0u.survivor.Survivor;
import fr.lumin0u.survivor.objects.Door;
import fr.lumin0u.survivor.objects.Door.DoorAdapter;
import fr.lumin0u.survivor.objects.Room;
import fr.lumin0u.survivor.objects.Room.RoomAdapter;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.util.Vector;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class MapConfig
{
	private static final Gson gson = new GsonBuilder()
			.setPrettyPrinting()
			.disableHtmlEscaping()
			.setExclusionStrategies(new ExclusionStrategy() {
				@Override
				public boolean shouldSkipField(FieldAttributes fieldAttributes) {
					return fieldAttributes.getAnnotation(GsonIgnore.class) != null;
				}
				
				@Override
				public boolean shouldSkipClass(Class<?> aClass) {
					return false;
				}
			})
			.registerTypeHierarchyAdapter(ConfigurationSerializable.class, new ConfigurationSerializableAdapter())
			.registerTypeHierarchyAdapter(Room.class, new RoomAdapter())
			.registerTypeHierarchyAdapter(Door.class, new DoorAdapter())
			.registerTypeHierarchyAdapter(MapConfig.class, new MapConfigAdapter())
			.create();
	
	private static final Type stringConfigMapType = new TypeToken<Map<String, MapConfig>>() {}.getType();
	
	public final List<Vector> ammoBoxes = new ArrayList<>();
	public final List<Vector> magicBoxes = new ArrayList<>();
	private final List<Room> rooms = new ArrayList<>();
	public Vector spawnpoint;
	
	@GsonIgnore
	private final Map<Integer, Action> actions = new HashMap<>();
	@GsonIgnore
	private final AtomicInteger actionCounter = new AtomicInteger();
	
	private MapConfig(boolean defaultRoom)
	{
		if(defaultRoom)
			rooms.add(Room.unsafe("default"));
	}
	
	public List<Room> getRooms() {
		return rooms;
	}
	
	public List<Room> getNonDefaultRooms() {
		return rooms.stream().filter(room -> !room.isDefault()).toList();
	}
	
	public Room getDefaultRoom() {
		return rooms.stream().filter(Room::isDefault).findAny().get();
	}
	
	public void addRoom(String name) {
		rooms.add(Room.unsafe(name));
	}
	
	public Room getRoom(String name) {
		return rooms.stream().filter(room -> room.getName().equals(name.replace("_", " "))).findAny().orElse(null);
	}
	
	public Action getAction(int id) {
		return actions.get(id);
	}
	
	public Action removeAction(int id) {
		return actions.remove(id);
	}
	
	public int addAction(Action action) {
		actions.put(actionCounter.incrementAndGet(), action);
		return actionCounter.get();
	}
	
	public void save(String name) {
		Map<String, MapConfig> configs = getFullConfig();
		
		configs.put(name, this);
		
		try(FileWriter fw = new FileWriter(getSaveFile()))
		{
			gson.toJson(configs, fw);
			fw.flush();
			fw.close();
		} catch(IOException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public static File getSaveFile() {
		return new File(Survivor.getPluginConfigDir(), "maps.json");
	}
	
	public static boolean doesConfigExists(String name) {
		return getFullConfig().containsKey(name);
	}
	
	private static Map<String, MapConfig> getFullConfig() {
		try
		{
			return gson.<Map<String, MapConfig>>fromJson(new FileReader(getSaveFile()), stringConfigMapType);
		} catch(FileNotFoundException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public static MapConfig loadConfig(String name) {
		if(doesConfigExists(name))
			return getFullConfig().get(name);
		else
			return new MapConfig(true);
	}
	
	public static boolean deleteConfig(String name) {
		if(!doesConfigExists(name))
			return false;
		
		Map<String, MapConfig> configs = getFullConfig();
		configs.remove(name);
		
		try(FileWriter fw = new FileWriter(getSaveFile()))
		{
			gson.toJson(configs, fw);
			fw.flush();
			fw.close();
		} catch(IOException e)
		{
			throw new RuntimeException(e);
		}
		
		return true;
	}
	
	static class MapConfigAdapter implements JsonDeserializer<MapConfig>
	{
		static final Type vectorListType = new TypeToken<List<Vector>>(){}.getType();
		static final Type roomListType = new TypeToken<List<Room>>(){}.getType();
		
		@Override
		public MapConfig deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
			
			JsonObject object = jsonElement.getAsJsonObject();
			
			MapConfig config = new MapConfig(false);
			
			config.spawnpoint = context.deserialize(object.get("spawnpoint"), Vector.class);
			config.ammoBoxes.addAll(context.deserialize(object.get("ammoBoxes"), vectorListType));
			config.magicBoxes.addAll(context.deserialize(object.get("magicBoxes"), vectorListType));
			config.rooms.addAll(context.deserialize(object.get("rooms"), roomListType));
			
			return config;
		}
	}
}

package fr.lumin0u.survivor.config;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class ConfigurationSerializableAdapter implements JsonSerializer<ConfigurationSerializable>, JsonDeserializer<ConfigurationSerializable>
{
	final Type objectStringMapType = (new TypeToken<Map<String, Object>>() {}).getType();
	
	
	@Override
	public final ConfigurationSerializable deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		Map<String, Object> map = new LinkedHashMap<>();
		
		for(Entry<String, JsonElement> stringJsonElementEntry : json.getAsJsonObject().entrySet())
		{
			JsonElement value = (JsonElement) stringJsonElementEntry.getValue();
			String name = (String) stringJsonElementEntry.getKey();
			if(value.isJsonObject() && value.getAsJsonObject().has("=="))
			{
				map.put(name, this.deserialize(value, value.getClass(), context));
			}
			else
			{
				map.put(name, context.deserialize(value, Object.class));
			}
		}
		
		
		return ConfigurationSerialization.deserializeObject(map);
	}
	
	@Override
	public final JsonElement serialize(ConfigurationSerializable src, Type typeOfSrc, JsonSerializationContext context) {
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("==", ConfigurationSerialization.getAlias(src.getClass()));
		map.putAll(src.serialize());
		return context.serialize(map, this.objectStringMapType);
	}
}

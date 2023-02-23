package fr.lumin0u.survivor.config;

import fr.lumin0u.survivor.commands.SurvivorCommand;
import fr.lumin0u.survivor.config.commands.CancelCommand;
import fr.lumin0u.survivor.objects.Door;
import fr.lumin0u.survivor.objects.Room;
import fr.lumin0u.survivor.utils.MCUtils;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public final class ConfigUtil
{
	public static TextComponent getAdditionAndDo(MapConfig config, Action action, String actionText)
	{
		action.redo();
		return getAddition(config, action, actionText);
	}
	
	public static TextComponent getAddition(MapConfig config, Action action, String actionText)
	{
		int id = config.addAction(action);
		
		TextComponent addition = new TextComponent("");
		
		TextComponent cancel = new TextComponent("§c[%s]".formatted(actionText));
		cancel.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/sv %s %d".formatted(SurvivorCommand.getCommandByClass(CancelCommand.class).getName(), id)));
		cancel.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[] {new TextComponent("§6" + actionText)}));
		
		addition.addExtra(cancel);
		
		return addition;
	}
	
	public static TextComponent getAdditionAndDo(MapConfig config, Location location, Action action, String actionText)
	{
		action.redo();
		return getAddition(config, location, action, actionText);
	}
	
	public static TextComponent getAddition(MapConfig config, Location location, Action action, String actionText)
	{
		TextComponent addition = getAddition(config, action, actionText);
		
		TextComponent tp = new TextComponent("§6[tp]");
		tp.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tp %f %f %f".formatted(location.getX(), location.getY(), location.getZ())));
		tp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[] {new TextComponent("§aSe téléporter à la position")}));
		
		addition.addExtra(" ");
		addition.addExtra(tp);
		
		return addition;
	}
	
	public static BaseComponent toPlayerExplanation(MapConfig config, World world)
	{
		List<BaseComponent> components = new ArrayList<>();
		
		TextComponent intro = new TextComponent("\n§7§m    §d Configuration en cours §m    ");
		components.add(intro);
		intro.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/sv configInfo"));
		intro.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[] {new TextComponent("§aActualiser")}));
		
		TextComponent spawnpoint = new TextComponent("§7Point d'apparition: ");
		components.add(spawnpoint);
		
		components.add(new TextComponent(""));
		
		TextComponent spawnpointClickable = new TextComponent(config.spawnpoint == null ?
				"§cnon défini" :
				"§6[%d %d %d]".formatted((int) config.spawnpoint.getX(), (int) config.spawnpoint.getY(), (int) config.spawnpoint.getZ()));
		if(config.spawnpoint != null)
		{
			spawnpointClickable.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tp %f %f %f".formatted(config.spawnpoint.getX(), config.spawnpoint.getY(), config.spawnpoint.getZ())));
			spawnpointClickable.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent("§aSe téléporter à la position")}));
		}
		
		spawnpoint.addExtra(spawnpointClickable);
		
		for(Room room : config.getRooms())
		{
			BaseComponent roomExplanation = toPlayerExplanation(config, room, world);
			components.add(roomExplanation);
			if(roomExplanation.toPlainText().chars().filter(ch -> ch == '\n').count() > 2)
				components.add(new TextComponent(""));
		}
		
		components.add(new TextComponent(""));
		
		TextComponent magicBoxes = new TextComponent("§d[boites magiques] ");
		components.add(magicBoxes);
		magicBoxes.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/sv configInfo magicBoxes"));
		magicBoxes.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[] {new TextComponent("§aAfficher")}));
		
		TextComponent ammoBoxes = new TextComponent("§6[boites de munitions]");
		magicBoxes.addExtra(ammoBoxes);
		ammoBoxes.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/sv configInfo ammoBoxes"));
		ammoBoxes.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[] {new TextComponent("§aAfficher")}));
		
		components.add(new TextComponent(""));
		
		return MCUtils.buildTextComponent("\n", components.toArray());
	}
	
	public static BaseComponent toPlayerExplanation(MapConfig config, List<Vector> positions, World world, String text)
	{
		return toPlayerExplanation(config, positions, world, text, " ");
	}
	
	public static BaseComponent toPlayerExplanation(MapConfig config, List<Vector> positions, World world, String text, String indent)
	{
		TextComponent component = new TextComponent(text);
		
		for(Vector vector : positions)
		{
			component.addExtra("\n" + indent + "§7> §f%d %d %d ".formatted((int) vector.getX(), (int) vector.getY(), (int) vector.getZ()));
			component.addExtra(getAddition(config, vector.toLocation(world), new Action() {
				@Override
				public void redo() {
					positions.add(vector);
				}
				
				@Override
				public void undo() {
					positions.remove(vector);
				}
			}, "retirer"));
		}
		
		return component;
	}
	
	public static BaseComponent toPlayerExplanation(MapConfig config, Room room, World world)
	{
		TextComponent component = new TextComponent("§2Salle §a" + room.getName() + "§7: ");
		
		if(!room.isDefault())
		{
			component.addExtra(getAddition(config, new Action() {
				@Override
				public void redo() {
					config.getRooms().add(room);
				}
				
				@Override
				public void undo() {
					config.getRooms().remove(room);
				}
			}, "retirer"));
			
			for(Door door : room.getDoors())
			{
				component.addExtra("\n §8> Porte ");
				component.addExtra(getAddition(config, door.getMidLoc().toLocation(world), new Action() {
					@Override
					public void redo() {
						room.getDoors().add(door);
					}
					
					@Override
					public void undo() {
						room.getDoors().remove(door);
					}
				}, "retirer"));
			}
		}
		
		component.addExtra("\n");
		component.addExtra(toPlayerExplanation(config, room.getMobSpawnsUnsafe(), world, " §7Points d'apparitions des zombies: ", "  "));
		
		// TODO fences
		
		return component;
	}
}

package fr.lumin0u.survivor.config;

import fr.lumin0u.survivor.commands.SurvivorCommand;
import fr.lumin0u.survivor.config.commands.CancelCommand;
import fr.lumin0u.survivor.objects.Room;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TextComponent.Builder;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.util.List;

public final class ConfigUtil
{
	public static Component getAdditionAndDo(MapConfig config, Action action, String cancelText) {
		action.redo();
		return getAddition(config, action, cancelText);
	}
	
	public static Component getAddition(MapConfig config, Action action, String cancelText) {
		int id = config.addAction(action);
		
		return Component.text("§c[%s]".formatted(cancelText))
				.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/sv %s %d".formatted(SurvivorCommand.getCommandByClass(CancelCommand.class).getName(), id)))
				.hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text("§6" + cancelText)));
	}
	
	public static Component getAdditionAndDo(MapConfig config, Location location, Action action, String cancelText) {
		action.redo();
		return getAddition(config, location, action, cancelText);
	}
	
	public static Component getAddition(MapConfig config, Location location, Action action, String cancelText) {
		return getAddition(config, action, cancelText)
				.appendSpace()
				.append(
						Component.text("§6[tp]")
								.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/tp %f %f %f".formatted(location.getX(), location.getY(), location.getZ())))
								.hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text("§aSe téléporter à la position"))));
	}
	
	public static Component toPlayerExplanation(MapConfig config, World world) {
		TextComponent intro = Component.text("\n§7§m    §d Configuration en cours §m    ")
				.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/sv configInfo"))
				.hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text("§aActualiser")));
		
		TextComponent spawnpointClickable = config.spawnpoint == null ? Component.text("§cnon défini")
				: Component.text("§6[%d %d %d]".formatted((int) config.spawnpoint.getX(), (int) config.spawnpoint.getY(), (int) config.spawnpoint.getZ()))
				.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/tp %f %f %f".formatted(config.spawnpoint.getX(), config.spawnpoint.getY(), config.spawnpoint.getZ())))
				.hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text("§aSe téléporter à la position")));
		
		TextComponent rooms = config.getRooms().stream()
				.map(r -> toPlayerExplanation(config, r, world).appendNewline())
				.collect(Component::text, Builder::append, Builder::append).build();
		//roomExplanation.toPlainText().chars().filter(Character.valueOf('\n')::equals).count() > 2 ???
		
		TextComponent magicBoxes = Component.text("§d[boites magiques] ")
				.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/sv configInfo magicBoxes"))
				.hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text("§aAfficher")));
		
		TextComponent ammoBoxes = Component.text("§d[boites de munitions] ")
				.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/sv configInfo ammoBoxes"))
				.hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text("§aAfficher")));
		
		return Component.text()
				.append(intro)
				.appendNewline()
				.append(Component.text("§7Point d'apparition: "))
				.append(spawnpointClickable)
				.appendNewline()
				.append(rooms)
				//.appendNewline()
				.append(magicBoxes)
				.appendNewline()
				.append(ammoBoxes)
				.appendNewline()
				.build();
	}
	
	public static Component toPlayerExplanation(MapConfig config, List<Vector> positions, World world, String text) {
		return toPlayerExplanation(config, positions, world, text, " ");
	}
	
	public static Component toPlayerExplanation(MapConfig config, List<Vector> positions, World world, String text, String indent) {
		TextComponent positionsComponent = positions.stream()
				.map(vector -> Component.newline()
						.append(Component.text(indent + "§7> §f%d %d %d ".formatted((int) vector.getX(), (int) vector.getY(), (int) vector.getZ()))
								.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/tp %f %f %f".formatted(vector.getX(), vector.getY(), vector.getZ())))
								.hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text("§aSe téléporter à la position"))))
						.append(getAddition(config, vector.toLocation(world), Action.ofAdd(positions, vector), "retirer")))
				.collect(Component::text, Builder::append, Builder::append).build();
		
		return Component.text(text)
				.append(positionsComponent);
	}
	
	public static Component toPlayerExplanation(MapConfig config, Room room, World world) {
		
		TextComponent doorsComponent = room.isDefault() ? Component.empty() : room.getDoors().stream()
				.map(door -> Component.newline()
						.append(Component.text(" §8> Porte "))
						.append(getAddition(config, door.getMidLoc().toLocation(world), Action.ofAdd(room.getDoors(), door), "retirer")))
				.collect(Component::text, Builder::append, Builder::append).build();
		
		// TODO fences
		
		return Component.text("§2Salle §a" + room.getName() + "§7: ")
				.append(room.isDefault() ? Component.empty() : getAddition(config, Action.ofAdd(config.getRooms(), room), "retirer"))
				.append(doorsComponent)
				.appendNewline()
				.append(toPlayerExplanation(config, room.getMobSpawnsUnsafe(), world, " §7Points d'apparitions des zombies: ", "  "));
	}
}

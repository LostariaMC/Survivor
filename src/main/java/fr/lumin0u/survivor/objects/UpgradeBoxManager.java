package fr.lumin0u.survivor.objects;

import fr.lumin0u.survivor.GameManager;
import fr.lumin0u.survivor.SurvivorGame;
import fr.lumin0u.survivor.player.SvPlayer;
import fr.lumin0u.survivor.utils.ItemBuilder;
import fr.lumin0u.survivor.utils.TFSound;
import fr.lumin0u.survivor.weapons.Perk;
import fr.lumin0u.survivor.weapons.Upgradeable;
import fr.lumin0u.survivor.weapons.Weapon;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentBuilder;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Display;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class UpgradeBoxManager
{
	private Block machine;
	private GameManager gm;
	private TextDisplay nameDisplay;
	
	public UpgradeBoxManager(Location boxLoc, GameManager gm) {
		this.machine = boxLoc.getBlock();
		this.gm = gm;
	}
	
	public Block getBlock() {
		return machine;
	}
	
	private final Map<SvPlayer, UpgradeGui> onScreen = new HashMap<>();
	
	public void openGui(SvPlayer clicker)
	{
		Weapon weapon = clicker.getWeaponInHand();
		
		UpgradeGui gui = new UpgradeGui(weapon);
		gui.createAndShow();
		onScreen.put(clicker, gui);
	}
	
	public void onGameStart()
	{
		nameDisplay = (TextDisplay) machine.getWorld().spawnEntity(machine.getLocation().add(0.5, 1.3, 0.5), EntityType.TEXT_DISPLAY);
		nameDisplay.text(Component.text("Boite à améliorations")
				.decorate(TextDecoration.BOLD)
				.color(TextColor.color(new Color(0xB66028).getRGB())));
		nameDisplay.setBillboard(Display.Billboard.CENTER);
		//nameDisplay.setSeeThrough(true);
		nameDisplay.setDefaultBackground(false);
		
		machine.setType(Material.ENCHANTING_TABLE);
	}
	
	public UpgradeGui getUpgradeGui(SvPlayer sp)
	{
		return onScreen.get(sp);
	}
	
	public static class UpgradeGui
	{
		private final Weapon weapon;
		private final SvPlayer sp;
		private Inventory inv;
		
		public UpgradeGui(Weapon weapon)
		{
			this.weapon = weapon;
			sp = (SvPlayer) weapon.getOwner();
		}
		
		public Inventory getInventory()
		{
			return inv;
		}
		
		public Weapon getWeapon()
		{
			return weapon;
		}
		
		public void clickOn(ItemStack item) {
			if(item.getType().equals(Material.COOKED_RABBIT) || item.getType().equals(Material.RABBIT))
				upgradeWeapon();
			if(item.getType().equals(Material.COOKIE) || item.getType().equals(Material.COOKIE))
				buyPerk();
		}
		
		public void upgradeWeapon()
		{
			if(!(weapon instanceof Upgradeable))
			{
				sp.toBukkit().closeInventory();
				TFSound.CANT_AFFORD.playTo(sp);
				sp.toBukkit().sendMessage(SurvivorGame.prefix + "§cCette arme n'est pas améliorable");
				return;
			}
			if(sp.getMoney() <= ((Upgradeable) weapon).getNextLevelPrice())
			{
				sp.toBukkit().closeInventory();
				TFSound.CANT_AFFORD.playTo(sp);
				sp.toBukkit().sendMessage(SurvivorGame.prefix + "§cVous ne possédez pas assez d'argent");
				return;
			}
			
			sp.toCosmox().grantAdvancement(SurvivorGame.UPGRADE_ACHIEVEMENT.getId());
			if(weapon.getLevel() == 4) {
				sp.toCosmox().grantAdvancement(SurvivorGame.UPGRADE5_ACHIEVEMENT.getId());
			}
			sp.toBukkit().playSound(sp.toBukkit().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
			sp.addMoney(-((Upgradeable) weapon).getNextLevelPrice());
			((Upgradeable) weapon).upgrade();
			weapon.setAmmo(Math.min(weapon.getMaxAmmo(), weapon.getAmmo() + weapon.getMaxAmmo() / 4));
			weapon.setClip(weapon.getClipSize());
			sp.refreshWeaponItem(weapon);
			
			createAndShow();
		}
		
		public void buyPerk()
		{
			if(!weapon.acceptsPerks())
			{
				sp.toBukkit().closeInventory();
				TFSound.CANT_AFFORD.playTo(sp);
				sp.toBukkit().sendMessage(SurvivorGame.prefix + "§cCette arme ne peut pas recevoir de perk");
				return;
			}
			if(sp.getMoney() <= Perk.PRICE)
			{
				sp.toBukkit().closeInventory();
				TFSound.CANT_AFFORD.playTo(sp);
				sp.toBukkit().sendMessage(SurvivorGame.prefix + "§cVous ne possédez pas assez d'argent");
				return;
			}
			
			Perk perk = Perk.getRandomPerk();
			
			sp.toBukkit().playSound(sp.toBukkit().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
			sp.addMoney(-Perk.PRICE);
			weapon.setPerk(perk);
			sp.refreshWeaponItem(weapon);
			
			TextComponent message = Component.text(SurvivorGame.prefix + "§eVous obtenez :");
			message.append(Component.text(" " + perk.getDisplayName()).hoverEvent(HoverEvent.showText(perk.getDescription().stream().reduce(Component.text(), (c, s) -> c.append(Component.text(s)).appendNewline(), ComponentBuilder::append))));
			sp.toBukkit().sendMessage(SurvivorGame.prefix + "§eVous obtenez : " + perk.getDisplayName());
			
			sp.toBukkit().closeInventory();
		}
		
		public void createAndShow()
		{
			inv = Bukkit.createInventory(null, 3 * 9, "Améliorations");
			
			inv.setItem(11, new ItemBuilder(Material.COOKIE)
					.setDisplayName("§" + (sp.getMoney() >= Perk.PRICE ? "a" : "c") + "Acheter un perk")
					.addLore("§7Les perks confèrent des améliorations")
					.addLore("§7particulières aux armes")
					.addLore("§7Un perk coûte §6§l"+Perk.PRICE+"§a$")
					.addLore("§7Vous ne pouvez ajouter qu'§cun")
					.addLore("§cseul perk §7par arme, en racheter")
					.addLore("§7un retirera l'ancien !")
					.build());
			
			inv.setItem(13, new ItemBuilder(weapon.getItem()).setAmount(1).build());
			boolean isUpgradeable = weapon instanceof Upgradeable;
			boolean canUpgrade = isUpgradeable && ((Upgradeable) weapon).getNextLevelPrice() <= sp.getMoney();
			
			inv.setItem(15, new ItemBuilder(canUpgrade ? Material.COOKED_RABBIT : Material.RABBIT)
					.setDisplayName(
							isUpgradeable ?
							"§" + (canUpgrade ? "a" : "c") + "Améliorer §8- §6" + ((Upgradeable) weapon).getNextLevelPrice() + "$"
							: "§cNon améliorable")
					.build());
			
			sp.toBukkit().openInventory(inv);
		}
	}
}

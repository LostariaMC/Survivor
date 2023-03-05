package fr.lumin0u.survivor.objects;

import fr.lumin0u.survivor.GameManager;
import fr.lumin0u.survivor.SurvivorGame;
import fr.lumin0u.survivor.player.SvPlayer;
import fr.lumin0u.survivor.utils.ItemBuilder;
import fr.lumin0u.survivor.weapons.Upgradeable;
import fr.lumin0u.survivor.weapons.Weapon;
import fr.lumin0u.survivor.weapons.perks.Perk;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;

public class UpgradeBoxManager
{
	public UpgradeBoxManager()
	{
	
	}
	
	private static Map<SvPlayer, UpgradeGui> onScreen = new HashMap<>();
	
	public static void onClickHopper(Block b, Player clicker)
	{
		SvPlayer sp = GameManager.getInstance().getSvPlayer(clicker);
		Weapon weapon = sp.getWeaponInHand();
		
		UpgradeGui gui = new UpgradeGui(weapon);
		gui.createAndShow();
		onScreen.put(sp, gui);
	}
	
	public static UpgradeGui getUpgradeGui(SvPlayer sp)
	{
		return onScreen.get(sp);
	}
	
	public static class UpgradeGui
	{
		private Weapon weapon;
		private Inventory inv;
		private SvPlayer sp;
		
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
		
		public void upgradeWeapon()
		{
			if(!(weapon instanceof Upgradeable))
			{
				sp.getPlayer().closeInventory();
				sp.getPlayer().sendMessage(SurvivorGame.prefix + "§cCette arme n'est pas améliorable");
				return;
			}
			if(sp.getMoney() <= ((Upgradeable) weapon).getNextLevelPrice())
			{
				sp.getPlayer().closeInventory();
				sp.getPlayer().playSound(sp.getPlayer().getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
				sp.getPlayer().sendMessage(SurvivorGame.prefix + "§cVous ne possédez pas assez d'argent");
				return;
			}
			
			sp.addMoney(-((Upgradeable) weapon).getNextLevelPrice());
			((Upgradeable) weapon).upgrade();
			weapon.setAmmo(Math.min(weapon.getMaxAmmo(), weapon.getAmmo() + weapon.getMaxAmmo() / 4));
			weapon.setClip(weapon.getClipSize());
			weapon.giveItem();
			
			createAndShow();
		}
		
		public void buyPerk()
		{
			if(sp.getMoney() <= Perk.PRICE)
			{
				sp.getPlayer().closeInventory();
				sp.getPlayer().sendMessage(SurvivorGame.prefix + "§cVous ne possédez pas assez d'argent");
				return;
			}
			
			Perk perk = Perk.FIRE_BULLET;
			
			sp.addMoney(-Perk.PRICE);
			weapon.setPerk(perk);
			weapon.giveItem();
			
			sp.getPlayer().sendMessage(SurvivorGame.prefix + "§eVous obtenez : " + perk.getDisplayName());
			
			sp.getPlayer().closeInventory();
		}
		
		public void createAndShow()
		{
			inv = Bukkit.createInventory(null, 3 * 9, "Améliorations");
			
			inv.setItem(11, new ItemBuilder(Material.COOKIE).setDisplayName("§" + (sp.getMoney() >= Perk.PRICE ? "a" : "c") + "Acheter un perk").addLore("§7Les perks confèrent des améliorations").addLore("§7particulières aux armes").addLore("§7Un perk coûte §6§l"+Perk.PRICE+"§a$").addLore("§7Vous ne pouvez mettre qu'un").addLore("§7perk par arme").build());
			inv.setItem(13, new ItemBuilder(weapon.getItem()).setAmount(1).build());
			boolean isUpgradeable = weapon instanceof Upgradeable;
			boolean canUpgrade = isUpgradeable && ((Upgradeable) weapon).getNextLevelPrice() <= sp.getMoney();
			inv.setItem(15, new ItemBuilder(canUpgrade ? Material.COOKED_RABBIT : Material.RABBIT).setDisplayName((isUpgradeable ? "§" + (canUpgrade ? "a" : "c") + "Améliorer §8- §6" + ((Upgradeable) weapon).getNextLevelPrice() + "$" : "§cNon améliorable")).build());
			
			sp.getPlayer().openInventory(inv);
		}
	}
}

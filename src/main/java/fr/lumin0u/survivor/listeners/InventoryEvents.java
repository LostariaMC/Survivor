package fr.lumin0u.survivor.listeners;

import fr.lumin0u.survivor.GameManager;
import fr.lumin0u.survivor.Survivor;
import fr.lumin0u.survivor.SurvivorGame;
import fr.lumin0u.survivor.SvAsset;
import fr.lumin0u.survivor.objects.UpgradeBoxManager;
import fr.lumin0u.survivor.player.SvPlayer;
import fr.lumin0u.survivor.utils.MCUtils;
import fr.lumin0u.survivor.weapons.Weapon;
import fr.lumin0u.survivor.weapons.WeaponType;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class InventoryEvents implements Listener
{
	@EventHandler
	public void onClickInventory(InventoryClickEvent e)
	{
		if(GameManager.getInstance() == null)
			return;
		
		if(!e.getWhoClicked().getGameMode().equals(GameMode.CREATIVE) && !e.getClick().equals(ClickType.DROP))
		{
			e.setCancelled(true);
		}
		
		SvPlayer sp = SvPlayer.of(e.getWhoClicked());
		
		if(MCUtils.getTitle(e.getView()).equals("Approvisionnement") && e.getCurrentItem() != null)
		{
			switch(e.getCurrentItem().getType())
			{
				case SNOWBALL -> sp.setSupply(WeaponType.GRENADE);
				case SLIME_BALL -> sp.setSupply(WeaponType.GRENADEFRAG);
				case PAPER -> sp.setSupply(WeaponType.MEDIC_KIT);
				case MAGMA_CREAM -> sp.setSupply(WeaponType.GRENADEFLAME);
				case GOLD_NUGGET -> sp.setSupply(WeaponType.TURRET);
				case CAKE -> sp.setSupply(WeaponType.AMMO_BOX);
			}
			
			sp.openSupplyInventory();
		}
		
		UpgradeBoxManager.UpgradeGui upgradeGui = GameManager.getInstance().getUpgradeBoxManager().getUpgradeGui(sp);
		if(upgradeGui != null && e.getCurrentItem() != null) {
			upgradeGui.clickOn(e.getCurrentItem());
		}
	}
	
	@EventHandler
	public void onDrop(PlayerDropItemEvent e)
	{
		if(GameManager.getInstance() != null)
		{
			e.setCancelled(true);
			ItemStack item = e.getItemDrop().getItemStack();
			SvPlayer player = SvPlayer.of(e.getPlayer());
			GameManager gm = GameManager.getInstance();
			
			final Weapon inHand = player.getWeapon(e.getItemDrop().getItemStack());
			
			if(gm.isStarted() && inHand != null && inHand.isUseable() && inHand.getClip() < inHand.getClipSize())
			{
				new BukkitRunnable()
				{
					@Override
					public void run()
					{
						inHand.reload();
					}
				}.runTaskLater(Survivor.getInstance(), 1L);
			}
			
			SvAsset asset = SvAsset.byMat(item.getType());
			if(asset != null)
			{
				e.setCancelled(false);
				e.getItemDrop().remove();
				player.getAssets().remove(asset);
				
				player.toBukkit().sendMessage(SurvivorGame.prefix + "§6Vous avez retiré l'atout §a" + asset.getName());
				player.addMoney((double) asset.getPrice() / 2);
				
				if(asset.equals(SvAsset.MASTODONTE)) {
					player.toBukkit().setHealth(GameManager.getInstance().getDifficulty().getMaxHealth());
					player.toBukkit().setMaxHealth(GameManager.getInstance().getDifficulty().getMaxHealth());
				}
				if(asset.equals(SvAsset.MARATHON)) {
					player.toBukkit().setWalkSpeed(0.2F);
				}
				player.cleanInventory();
			}
			
		}
	}
}

package fr.lumin0u.survivor.listeners;

import fr.lumin0u.survivor.*;
import fr.lumin0u.survivor.objects.UpgradeBoxManager;
import fr.lumin0u.survivor.player.SvPlayer;
import fr.lumin0u.survivor.utils.MCUtils;
import fr.lumin0u.survivor.weapons.Weapon;
import fr.lumin0u.survivor.weapons.WeaponType;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
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
		
		SvPlayer sp = GameManager.getInstance().getSvPlayer((Player) e.getWhoClicked());
		
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
		
		/*if(MCUtils.getTitle(e.getView()).equals("Difficulté") && e.getCurrentItem() != null)
		{
			for(Difficulty diff : Difficulty.values())
			{
				ItemStack item = e.getCurrentItem().clone();
				ItemStack item1 = diff.getNewGlass().clone();
				if(item.getType().equals(item1.getType()))
				{
					sp.setDiffVote(diff);
				}
			}
			
			GameManager.getInstance().calculateDifficulty();
			sp.openDiffInventory();
		}*/
		
		if(UpgradeBoxManager.getUpgradeGui(sp) != null && e.getCurrentItem() != null)
		{
			if(e.getCurrentItem().getType().equals(Material.COOKED_RABBIT) || e.getCurrentItem().getType().equals(Material.RABBIT))
				UpgradeBoxManager.getUpgradeGui(sp).upgradeWeapon();
		}
	}
	
	@EventHandler
	public void onDrop(PlayerDropItemEvent e)
	{
		if(GameManager.getInstance() != null)
		{
			e.setCancelled(true);
			ItemStack item = e.getItemDrop().getItemStack();
			Player p = e.getPlayer();
			GameManager gm = GameManager.getInstance();
			final Weapon inHand = gm.getSvPlayer(p).getWeapon(e.getItemDrop().getItemStack());
			if(gm.isStarted() && inHand != null && inHand.isUseable() && inHand.getClip() < inHand.getClipSize())
			{
				(new BukkitRunnable()
				{
					public void run()
					{
						inHand.reload();
					}
				}).runTaskLater(Survivor.getInstance(), 1L);
			}
			
			SvAsset asset = SvAsset.byMat(item.getType());
			if(asset != null)
			{
				e.setCancelled(false);
				e.getItemDrop().remove();
				gm.getSvPlayer(p).getAtouts().remove(asset);
				
				p.sendMessage(Survivor.prefix + " §6Vous avez retiré l'atout §a" + asset.getName());
				gm.getSvPlayer(p).addMoney(asset.getPrice() / 2);
				
				if(asset.equals(SvAsset.MASTODONTE))
				{
					p.setHealth(GameManager.getInstance().getDifficulty().getMaxHealth());
					p.setMaxHealth(GameManager.getInstance().getDifficulty().getMaxHealth());
				}
				
				if(asset.equals(SvAsset.MARATHON))
				{
					p.setWalkSpeed(0.2F);
				}
				gm.getSvPlayer(p).cleanInventory();
			}
			
		}
	}
}

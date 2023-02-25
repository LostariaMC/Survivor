package fr.lumin0u.survivor.weapons;

import fr.lumin0u.survivor.Survivor;
import fr.lumin0u.survivor.mobs.mob.Enemy;
import fr.lumin0u.survivor.player.SvDamageable;
import fr.lumin0u.survivor.player.SvPlayer;
import fr.lumin0u.survivor.player.WeaponOwner;
import fr.lumin0u.survivor.weapons.perks.Perk;
import fr.lumin0u.survivor.weapons.superweapons.SuperWeapon;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public abstract class Weapon implements IWeapon
{
	protected int ammo;
	protected int clip;
	protected WeaponOwner owner;
	protected ItemStack item;
	protected boolean isReloading;
	protected int level;
	protected int maxAmmo;
	protected int clipSize;
	protected int reloadTime;
	protected WeaponType wt;
	private long timeLastClick;
	private long lastShotDate;
	private long shootingTime;
	
	private Perk perk;
	
	public BukkitRunnable rClickingTask;
	
	public Weapon(final WeaponOwner owner, final WeaponType wt)
	{
		this.owner = owner;
		this.wt = wt;
		this.isReloading = false;
		this.ammo = wt.getMaxAmmo();
		this.maxAmmo = wt.getMaxAmmo();
		this.clip = wt.getClipSize();
		this.clipSize = wt.getClipSize();
		this.reloadTime = wt.getReloadTime();
		this.level = 0;
		this.item = new ItemStack(wt.getMaterial());
		ItemMeta meta = this.item.getItemMeta();
		meta.setDisplayName((this instanceof SuperWeapon ? "§d" : "§9") + wt.getName());
		meta.setLore(this.getLore());
		meta.setUnbreakable(true);
		this.item.setItemMeta(meta);
		if(owner.getWeaponTypes().contains(wt))
		{
			owner.getWeaponsByType(this.getClass()).forEach(owner::removeWeapon);
		}
		
		owner.addWeapon(this);
		if(owner instanceof SvPlayer)
		{
			new BukkitRunnable()
			{
				@Override
				public void run()
				{
					if(owner.canUseWeapon() && !owner.hasItem(Weapon.this))
					{
						if(!((SvPlayer) owner).getPlayer().getItemOnCursor().getType().equals(wt.getMaterial()))
						{
							owner.removeWeapon(Weapon.this);
							this.cancel();
						}
					}
				}
			}.runTaskTimer(Survivor.getInstance(), 1L, 1L);
		}
	}
	
	public ItemStack getItem()
	{
		return this.item.clone();
	}
	
	public int getAmmo()
	{
		return this.ammo;
	}
	
	public void setAmmo(int ammo)
	{
		this.ammo = ammo;
	}
	
	public int getClip()
	{
		return this.clip;
	}
	
	/**
	 * @deprecated
	 */
	@Deprecated
	public void setClip(int clip)
	{
		this.clip = clip;
	}
	
	public void useAmmo()
	{
		--this.clip;
		if(this.clip <= 0)
		{
			this.reload();
		}
	}
	
	public void reload()
	{
		if(ammo > 0 && owner.hasItem(this))
		{
			isReloading = true;
			
			final int modifiedReloadTime = owner.hasSpeedReload() ? (int) ((double) this.reloadTime / 1.6) : this.reloadTime;
			if(owner instanceof SvPlayer)
			{
				showCooldown(modifiedReloadTime);
			}
			
			new BukkitRunnable()
			{
				int time = 0;
				
				@Override
				public void run()
				{
					isReloading = true;
					
					if(this.time >= modifiedReloadTime)
					{
						isReloading = false;
						this.cancel();
						int clip = getClip();
						setClip(Math.min(getAmmo() + clip, clipSize));
						setAmmo(Math.max(0, getAmmo() - (clipSize - clip)));
						item.setAmount(1);
						return;
					}
					
					if(time == 0)
					{
						showCooldown(modifiedReloadTime - time);
					}
					
					if(owner.getItemInHand().isSimilar(item))
					{
						time++;
					}
					else
					{
						showCooldown(modifiedReloadTime - time);
					}
				}
			}.runTaskTimer(Survivor.getInstance(), 1L, 1L);
		}
	}
	
	public void showCooldown(int ticks)
	{
		((SvPlayer) owner).getPlayer().setCooldown(wt.getMaterial(), ticks);
	}
	
	public WeaponOwner getOwner()
	{
		return this.owner;
	}
	
	public boolean isReloading()
	{
		return this.isReloading;
	}
	
	public WeaponType getType()
	{
		return this.wt;
	}
	
	public List<String> getLore()
	{
		List<String> lore = new ArrayList<>();
		lore.add("§6Munitions max : §a" + this.maxAmmo + (!isUpgradeable() ? "" : " §8\u279D " + getMaxAmmoAtLevel(level + 1)));
		lore.add("§6Taille d'un chargeur : §a" + this.clipSize + (!isUpgradeable() ? "" : " §8\u279D " + getClipSizeAtLevel(level + 1)));
		lore.add("§6Temps de reload : §a" + String.format("%.2f", (double) this.reloadTime / 20.0D) + (!isUpgradeable() ? "" : " §8\u279D " + String.format("%.2f", (double) getReloadTimeAtLevel(level + 1) / 20)));
		lore.add("§6Niveau : §a" + this.level + (!isUpgradeable() ? "" : " §8\u279D " + (this.level + 1)));
		if(perk != null)
			lore.add("§6Perk : " + perk.getDisplayName());
		return lore;
	}
	
	public String getActionBar()
	{
		return "§9" + this.wt.getName() + (this.level > 0 ? " §5" + this.level + "§9" : "") + " §6" + this.clip + "§7/§6" + this.ammo;
	}
	
	/*public void action()
	{
		if(owner instanceof SvPlayer)
			StatsManager.increaseWeaponShots(this);
		
		this.wa.action(this);
		
		if(this.owner.hasDoubleCoup() && !(this instanceof SupplyWeapon))
		{
			Bukkit.getScheduler().runTaskLater(Survivor.getInstance(), () ->
			{
				wa.action(this);
			}, 1L);
		}
	}*/
	
	protected int getMaxAmmoAtLevel(int level)
	{
		return (int) ((double) wt.getMaxAmmo() * Math.pow(1.05D, level));
	}
	
	protected int getClipSizeAtLevel(int level)
	{
		return (int) ((double) wt.getClipSize() * Math.pow(1.05D, level));
	}
	
	protected int getReloadTimeAtLevel(int level)
	{
		return (int) ((double) wt.getReloadTime() * Math.pow(0.97D, level));
	}
	
	protected void upgrade()
	{
		++this.level;
		this.maxAmmo = getMaxAmmoAtLevel(this.level);
		this.clipSize = getClipSizeAtLevel(this.level);
		this.reloadTime = getReloadTimeAtLevel(this.level);
		ItemMeta meta = this.item.getItemMeta();
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		meta.setLore(this.getLore());
		this.item.setItemMeta(meta);
		this.item.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 1);
	}
	
	protected int getNextLevelPrice()
	{
		return (int) ((double) (this.level + 1) * Math.pow(1.13D, (double) this.level)) * 750;
	}
	
	public int getLevel()
	{
		return this.level;
	}
	
	public void giveItem()
	{
		ItemMeta meta = this.item.getItemMeta();
		meta.setLore(this.getLore());
		this.item.setItemMeta(meta);
		
		owner.giveWeaponItem(this);
		
//		if(owner instanceof SvPlayer)
//		{
//			Inventory inv = ((SvPlayer) owner).getInventory();
//			int place = inv.first(this.item.getType());
//			inv.remove(this.item.getType());
//
//			if(this.wt.getPlace() != -1)
//			{
//				inv.setItem(this.wt.getPlace(), this.item.clone());
//			}
//			else if(place != -1)
//			{
//				inv.setItem(place, this.item.clone());
//			}
//			else
//			{
//				inv.addItem(this.item.clone());
//			}
//
//			((SvPlayer) owner).getPlayer().updateInventory();
//		}
//
//		else
//			owner
	}
	
	public int getMaxAmmo()
	{
		return this.maxAmmo;
	}
	
	public int getClipSize()
	{
		return this.clipSize;
	}
	
	/*public final void impulseRightClick()
	{
		if(this.owner.canUseWeapon())
		{
			switch(this.wt.getRepeatingType())
			{
				case NONE:
					this.rightClick();
					break;
				case SEMIAUTOMATIC:
				case BURSTS:
					int lag = owner instanceof SvPlayer ? ((SvPlayer) owner).getPlayer().getPing() : 0;
					
					boolean wasClicking = System.currentTimeMillis() - this.timeLastClick < (long) (215 + lag);
					
					this.timeLastClick = System.currentTimeMillis();
					if(!wasClicking && System.currentTimeMillis() - this.lastShotDate > this.wt.getRpm() * 50L)
					{
						if(this.clip > 0 && !this.isReloading)
						{
							if(wt.getRepeatingType() == RepeatingType.SEMIAUTOMATIC)
							{
								this.lastShotDate = System.currentTimeMillis();
								this.rightClick();
							}
							else
							{
								if(this.rClickingTask == null)
								{
									this.rClickingTask = new BukkitRunnable()
									{
										int shots = 0;
										
										@Override
										public void run()
										{
											lastShotDate = System.currentTimeMillis();
											rightClick();
											shots++;
											if(shots >= (int) wt.get("shots"))
											{
												cancel();
												rClickingTask = null;
											}
										}
									};
									rClickingTask.runTaskTimer(Survivor.getInstance(), 0L, (Integer) this.wt.get("shotsDelay"));
								}
							}
						}
						else if(owner instanceof SvPlayer)
						{
							((SvPlayer) owner).getPlayer().playSound(((SvPlayer) owner).getPlayer().getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
						}
					}
					break;
				case AUTOMATIC:
					if(this.rClickingTask == null)
					{
						this.shootingTime = 0L;
						this.timeLastClick = 0L;
						this.rClickingTask = new BukkitRunnable()
						{
							@Override
							public void run()
							{
								if(Weapon.this.clip > 0 && !Weapon.this.isReloading && Weapon.this.timeLastClick + Math.max(3L, Weapon.this.wt.getRpm()) > Weapon.this.shootingTime)
								{
									Weapon.this.rightClick();
									Weapon.this.shootingTime = Weapon.this.shootingTime + Weapon.this.wt.getRpm();
								}
								else
								{
									Weapon.this.rClickingTask = null;
									this.cancel();
								}
							}
						};
						this.rClickingTask.runTaskTimer(Survivor.getInstance(), 0L, this.wt.getRpm());
					}
					else
					{
						this.timeLastClick = this.shootingTime;
					}
					break;
			}
			
		}
	}*/
	
	/*public final void impulseLeftClick()
	{
		if(this.owner.canUseWeapon())
		{
			this.leftClick();
		}
	}*/
	
	public int getAmmoBoxRecovery()
	{
		return clipSize;
	}
	
	public boolean isUpgradeable()
	{
		return this instanceof Upgradeable;
	}
	
	public void click(ClickType clickType)
	{
		if(clickType == ClickType.RIGHT)
			rightClick();
		else if(clickType == ClickType.LEFT)
			leftClick();
		else
			throw new UnsupportedOperationException("Can't handle this click !");
	}
	
	public abstract ClickType getMainClickAction();
	
	public abstract void rightClick();
	
	public abstract void leftClick();
	
	public String toString()
	{
		return this.getClass().getSimpleName() + " [level=" + this.level + ", ammo=" + this.ammo + ", clip=" + this.clip + "]";
	}
	
	public boolean aiHelp_MayShot(Enemy mob, SvPlayer target)
	{
		return true;
	}
	
	public double getDamageMultiplier(SvDamageable victim)
	{
		if(victim instanceof SvPlayer)
			return 0.1;
		else
			return 1;
	}
	
	public boolean isUseable() {
		return !isReloading && clip > 0;
	}
	
	public boolean hasPerk(Perk perk)
	{
		return perk == null || perk.equals(this.perk);
	}
	
	public Perk getPerk()
	{
		return perk;
	}
	
	public void setPerk(Perk perk) {
		this.perk = perk;
	}
}

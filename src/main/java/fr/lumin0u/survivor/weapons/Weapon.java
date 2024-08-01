package fr.lumin0u.survivor.weapons;

import fr.lumin0u.survivor.Survivor;
import fr.lumin0u.survivor.mobs.mob.Enemy;
import fr.lumin0u.survivor.player.SvDamageable;
import fr.lumin0u.survivor.player.SvPlayer;
import fr.lumin0u.survivor.player.WeaponOwner;
import fr.lumin0u.survivor.utils.ImmutableItemStack;
import fr.lumin0u.survivor.utils.TFSound;
import fr.lumin0u.survivor.weapons.knives.Knife;
import fr.lumin0u.survivor.weapons.perks.Perk;
import fr.lumin0u.survivor.weapons.superweapons.SuperWeapon;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
	
	private Perk perk;
	
	public BukkitRunnable rClickingTask;
	
	public Weapon(final WeaponOwner owner, final WeaponType wt) {
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
		meta.displayName(Component.text(wt.getName()).color(this instanceof SuperWeapon ? NamedTextColor.LIGHT_PURPLE : NamedTextColor.BLUE));
		meta.setLore(this.getLore());
		meta.setUnbreakable(true);
		meta.addItemFlags(ItemFlag.HIDE_ITEM_SPECIFICS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
		this.item.setItemMeta(meta);
	}
	
	public ItemStack getItem() {
		return this.item.clone();
	}
	
	public int getAmmo() {
		return this.ammo;
	}
	
	public void setAmmo(int ammo) {
		this.ammo = ammo;
	}
	
	public int getClip() {
		return this.clip;
	}
	
	public void setClip(int clip) {
		this.clip = clip;
	}
	
	public boolean acceptsPerks() {
		return !(this instanceof SuperWeapon) && !(this instanceof Knife);
	}
	
	public void useAmmo() {
		if(!Perk.FREE_BULLETS.testRandomDropAndHas(this))
			--this.clip;
		if(this.clip <= 0) {
			this.reload();
		}
	}
	
	public void reload() {
		if(!owner.hasItem(this)) {
			owner.refreshWeaponItem(this);
		}
		if(ammo > 0) {
			isReloading = true;
			
			final int modifiedReloadTime = (int) ((double) reloadTime * (owner.hasSpeedReload() ? 0.6 : 1) * (Perk.FASTER_RELOAD.testRandomDropAndHas(this) ? 0.8 : 1));
			if(owner instanceof SvPlayer) {
				TFSound.RELOAD.playTo((SvPlayer) owner);
				showCooldown(modifiedReloadTime);
				owner.refreshWeaponItem(Weapon.this);
			}
			
			new BukkitRunnable()
			{
				int time = 0;
				boolean wasInHand = true;
				
				@Override
				public void run() {
					isReloading = true;
					
					boolean ownerOffline = owner instanceof SvPlayer sp && !sp.isOnline();
					
					if(this.time >= modifiedReloadTime || ownerOffline) {
						isReloading = false;
						this.cancel();
						int clip = getClip();
						setClip(Math.min(getAmmo() + clip, clipSize));
						setAmmo(Math.max(0, getAmmo() - (clipSize - clip)));
						item.setAmount(1);
						if(!ownerOffline) {
							owner.refreshWeaponItem(Weapon.this);
						}
						return;
					}
					
					if(owner.getItemInHand().isSimilar(item)) {
						time++;
						if(!wasInHand) {
							showCooldown(modifiedReloadTime - time);
							wasInHand = true;
						}
					}
					else if(wasInHand) {
						showCooldown(60000);
						wasInHand = false;
					}
				}
			}.runTaskTimer(Survivor.getInstance(), 1L, 1L);
		}
	}
	
	public void showCooldown(int ticks) {
		if(owner instanceof SvPlayer)
			((SvPlayer) owner).toBukkit().setCooldown(wt.getMaterial(), ticks);
	}
	
	@Override
	public WeaponOwner getOwner() {
		return this.owner;
	}
	
	public boolean isReloading() {
		return this.isReloading;
	}
	
	public WeaponType getType() {
		return this.wt;
	}
	
	public List<String> getLore() {
		List<String> lore = new ArrayList<>();
		lore.add("§6Munitions max : §a" + this.maxAmmo + (!isUpgradeable() ? "" : " §8➝ " + getMaxAmmoAtLevel(level + 1)));
		lore.add("§6Taille d'un chargeur : §a" + this.clipSize + (!isUpgradeable() ? "" : " §8➝ " + getClipSizeAtLevel(level + 1)));
		lore.add("§6Temps de reload : §a" + String.format("%.2f", (double) this.reloadTime / 20.0D) + (!isUpgradeable() ? "" : " §8➝ " + String.format("%.2f", (double) getReloadTimeAtLevel(level + 1) / 20)));
		lore.add("§6Niveau : §a" + this.level + (!isUpgradeable() ? "" : " §8➝ " + (this.level + 1)));
		if(perk != null)
			lore.add("§6Perk : " + perk.getDisplayName());
		return lore;
	}
	
	public String getActionBar() {
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
	
	protected int getMaxAmmoAtLevel(int level) {
		return (int) ((double) wt.getMaxAmmo() * Math.pow(1.05D, level));
	}
	
	protected int getClipSizeAtLevel(int level) {
		return (int) ((double) wt.getClipSize() * Math.pow(1.05D, level));
	}
	
	protected int getReloadTimeAtLevel(int level) {
		return (int) ((double) wt.getReloadTime() * Math.pow(0.97D, level));
	}
	
	protected void upgrade() {
		++this.level;
		this.maxAmmo = getMaxAmmoAtLevel(this.level);
		this.clipSize = getClipSizeAtLevel(this.level);
		this.reloadTime = getReloadTimeAtLevel(this.level);
		ItemMeta meta = this.item.getItemMeta();
		meta.setLore(this.getLore());
		this.item.setItemMeta(meta);
		this.item.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 1);
	}
	
	protected int getNextLevelPrice() {
		return (int) ((double) (level + 1) * Math.pow(1.13D, level)) * 750;
	}
	
	public int getLevel() {
		return this.level;
	}
	
	public ImmutableItemStack buildItem() {
		ItemMeta meta = this.item.getItemMeta();
		meta.setLore(this.getLore());
		this.item.setItemMeta(meta);
		
		return new ImmutableItemStack(item);
	}
	
	public int getMaxAmmo() {
		return this.maxAmmo;
	}
	
	public int getClipSize() {
		return this.clipSize;
	}
	
	private long lastClickDate;
	private long lastShotDate;
	
	public final void impulseRightClick() {
		int rpm = getType().getRpm();
		
		switch(getType().getRepeatingType()) {
			case NONE ->
					rightClick();
			case SEMIAUTOMATIC, BURSTS -> {
				if(System.currentTimeMillis() - lastClickDate > 215 && Survivor.getCurrentTick() - lastShotDate >= rpm) {
					rightClick();
					if(!isReloading())
						showCooldown(Math.max(4, rpm));
					lastShotDate = Survivor.getCurrentTick();
				}
			}
			case AUTOMATIC -> {
				if(rClickingTask == null) {
					rightClick();
					
					rClickingTask = new BukkitRunnable()
					{
						@Override
						public void run() {
							if(Weapon.this.equals(owner.getWeaponInHand()) && getClip() > 0 && isUseable() && System.currentTimeMillis() - lastClickDate < 150) {
								rightClick();
							}
							else {
								rClickingTask = null;
								cancel();
							}
						}
					};
					
					rClickingTask.runTaskTimer(Survivor.getInstance(), rpm, rpm);
				}
			}
		}
		
		lastClickDate = System.currentTimeMillis();
	}
	
	public int getAmmoBoxRecovery() {
		return clipSize;
	}
	
	public boolean isUpgradeable() {
		return this instanceof Upgradeable;
	}
	
	public void click(ClickType clickType) {
		if(clickType == ClickType.RIGHT)
			rightClick();
		else if(clickType == ClickType.LEFT)
			leftClick();
		/*else
			throw new UnsupportedOperationException("Can't handle this click !");*/
	}
	
	public abstract ClickType getMainClickAction();
	
	public abstract void rightClick();
	
	public abstract void leftClick();
	
	public String toString() {
		return this.getClass().getSimpleName() + " [level=" + this.level + ", ammo=" + this.ammo + ", clip=" + this.clip + "]";
	}
	
	public boolean aiHelp_MayShoot(Enemy mob, SvPlayer target) {
		return true;
	}
	
	public double getDamageMultiplier(SvDamageable victim) {
		if(victim instanceof SvPlayer)
			return 0.1;
		else
			return 1;
	}
	
	public boolean isUseable() {
		return !isReloading && clip > 0;
	}
	
	public boolean hasPerk(Perk perk) {
		return perk == null || perk.equals(this.perk);
	}
	
	public Perk getPerk() {
		return perk;
	}
	
	public void setPerk(Perk perk) {
		this.perk = perk;
	}
}

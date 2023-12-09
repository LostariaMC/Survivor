package fr.lumin0u.survivor.objects;

import fr.lumin0u.survivor.GameManager;
import fr.lumin0u.survivor.Survivor;
import fr.lumin0u.survivor.SvAsset;
import fr.lumin0u.survivor.player.SvPlayer;
import fr.lumin0u.survivor.utils.AABB;
import fr.lumin0u.survivor.utils.MCUtils;
import fr.lumin0u.survivor.utils.TFSound;
import fr.lumin0u.survivor.weapons.Weapon;
import fr.lumin0u.survivor.weapons.WeaponType;
import fr.lumin0u.survivor.weapons.knives.Knife;
import fr.lumin0u.survivor.weapons.superweapons.SuperWeapon;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class MBTask extends BukkitRunnable
{
	private MagicBoxManager mbm;
	private Random random;
	private World world;
	private static final long period = 1L;
	private SvPlayer clicker;
	private long superCounter;
	private static final long kMax = 10L;
	private long subCounter;
	private Item shownItem;
	private WeaponType shownWeapon;
	private ArmorStand magicBoxName;
	private boolean nounours;
	private ArmorStand vehicle;
	
	private int effectCount = 0;
	private int effectSubCount = 0;
	private boolean newBoxLoc = true;
	
	public MBTask(MagicBoxManager mbm)
	{
		this.mbm = mbm;
		this.random = new Random();
		this.superCounter = 0L;
		this.subCounter = 0L;
		this.world = GameManager.getInstance().getWorld();
	}
	
	public void start()
	{
		this.runTaskTimer(Survivor.getInstance(), 1L, 1L);
	}
	
	@Override
	public void run()
	{
		if(newBoxLoc)
		{
			newBoxLoc = false;
			effectCount = 0;
		}
		
		if(this.effectSubCount == this.effectCount)
		{
			this.effectSubCount = 0;
			if(this.effectCount < 100)
				++this.effectCount;
			
			for(double y = (double) mbm.getBox().getY(); y < 255.0D; y += 0.4) //Math.max(0.2D, (y - mbm.getBox().getY()) / 100)
			{
				double yMultiplier = 1 - (y - mbm.getBox().getY()) / (255 - mbm.getBox().getY());
				double x = Math.cos((y + (double) (this.effectCount * 2)) / 4.0D) * yMultiplier * 2.0D + mbm.getBox().getX() + 0.5D;
				double z = Math.sin((y + (double) (this.effectCount * 2)) / 4.0D) * yMultiplier * 2.0D + mbm.getBox().getZ() + 0.5D;
				world.spawnParticle(Particle.VILLAGER_HAPPY, new Location(world, x, y, z), 0, 0, 0, 0, 1, null, true);
			}
		}
		
		++this.effectSubCount;
		
		if(this.mbm.getBox() != null)
		{
			AABB box = new AABB(this.mbm.getBox().getLocation(), this.mbm.getBox().getLocation().add(1.0D, 1.0D, 1.0D));
			MCUtils.explosionParticles(box.clone().multiply(this.random.nextDouble() * 0.3D + 1.0D).rdContourLoc().toLocation(this.world), 0.0F, 3, Particle.SPELL_WITCH, Particle.ENCHANTMENT_TABLE);
			if(this.clicker != null)
			{
				if(this.magicBoxName != null)
				{
					this.magicBoxName.remove();
					this.magicBoxName = null;
				}
				
				if(this.superCounter < 10L)
				{
					if(this.subCounter > 0L)
					{
						--this.subCounter;
					}
					else
					{
						this.superCounter = Math.min(10L, this.superCounter + 1L);
						this.subCounter = this.superCounter;
						WeaponType last = this.shownWeapon;
						int i = 0;
						
						do
						{
							++i;
							this.shownWeapon = WeaponType.values()[this.random.nextInt(WeaponType.values().length)];
						} while(i <= 100 && (!this.shownWeapon.isInMagicBox() || this.shownWeapon.equals(last) || this.clicker.getWeaponTypes().contains(this.shownWeapon)));
						
						if(this.shownItem == null)
						{
							this.shownItem = this.world.dropItem(box.midpoint().toLocation(this.world).add(0.0D, 1.0D, 0.0D), this.shownWeapon.getItemToSell());
							this.shownItem.setPickupDelay(20000);
							this.vehicle = (ArmorStand) this.world.spawnEntity(box.midpoint().toLocation(this.world).add(0.0D, -1.0D, 0.0D), EntityType.ARMOR_STAND);
							this.vehicle.addPassenger(this.shownItem);
							this.vehicle.setVisible(false);
							this.vehicle.setGravity(false);
							this.vehicle.setCustomName((this.shownWeapon.isSuperWeaponType() ? "§d" : "§9") + this.shownWeapon.getName());
							this.vehicle.setCustomNameVisible(true);
						}
						
						this.nounours = this.random.nextInt(14) == 0;
						if(this.nounours)
						{
							this.shownWeapon = null;
						}
						
						this.shownItem.setItemStack(this.nounours ? new ItemStack(Material.COAL) : this.shownWeapon.getItemToSell());
						this.shownItem.setPickupDelay(Integer.MAX_VALUE);
						((ArmorStand) this.shownItem.getVehicle()).setCustomName(this.nounours ? "§6Nounours" : (this.shownWeapon.isSuperWeaponType() ? "§d" : "§9") + this.shownWeapon.getName());
					}
				}
				else if(this.superCounter == 10L)
				{
					this.subCounter = 0L;
					this.superCounter = System.currentTimeMillis();
				}
				else if(System.currentTimeMillis() - this.superCounter > (long) (this.nounours ? 2000 : 10000))
				{
					if(this.nounours)
					{
						this.clicker.addMoney(MagicBoxManager.boxPrice);
						this.removeAll();
						this.mbm.changeLoc();
					}
					else
					{
						this.removeAll();
					}
				}
			}
			else if(this.magicBoxName == null || this.magicBoxName.isDead())
			{
				this.magicBoxName = (ArmorStand) this.world.spawnEntity(this.mbm.getBox().getLocation().add(0.5D, -1.0D, 0.5D), EntityType.ARMOR_STAND);
				this.magicBoxName.setVisible(false);
				this.magicBoxName.setGravity(false);
				this.magicBoxName.setCustomName("§dBoite magique §6" + MagicBoxManager.boxPrice + "$");
				this.magicBoxName.setCustomNameVisible(true);
			}
		}
		
	}
	
	public void onClickOnBox(SvPlayer sp)
	{
		if(this.clicker == null && sp.isAlive())
		{
			if(sp.getMoney() >= MagicBoxManager.boxPrice)
			{
				this.clicker = sp;
				sp.addMoney(-MagicBoxManager.boxPrice);
				Block b = this.mbm.getBox();
				for(Player player : b.getWorld().getPlayers())
					player.playNote(b.getLocation(), (byte) 1, (byte) 1);
			}
			else {
				TFSound.CANT_AFFORD.playTo(sp);
			}
		}
		else if(this.superCounter > 10L && this.clicker.equals(sp) && sp.isAlive())
		{
			Location itemLoc = this.shownItem.getLocation();
			boolean isItemGiven = false;
			if(nounours)
			{
				this.clicker.addMoney(MagicBoxManager.boxPrice);
				this.removeAll();
				this.mbm.changeLoc();
			}
			else if(sp.getSimpleWeapons().size() >= (sp.getAssets().contains(SvAsset.TROIS_ARME) ? 3 : 2) && !this.shownWeapon.isSuperWeaponType())
			{
				Weapon w = sp.getWeaponInHand();
				if(w != null && !(w instanceof SuperWeapon) && (!(w instanceof Knife) || this.shownWeapon.isKnife()))
				{
					sp.removeWeapon(this.shownWeapon.isKnife() ? sp.getKnife() : w);
					sp.toBukkit().getInventory().remove(((Weapon) (this.shownWeapon.isKnife() ? sp.getKnife() : w)).getType().getMaterial());
					this.shownWeapon.giveNewWeapon(sp).giveItem();
					isItemGiven = true;
					this.removeAll();
				}
			}
			else
			{
				this.shownWeapon.giveNewWeapon(sp).giveItem();
				isItemGiven = true;
				this.removeAll();
			}
			
			if(isItemGiven)
			{
				for(int i = 0; i < 10; ++i)
				{
					itemLoc.getWorld().spawnParticle(Particle.LAVA, itemLoc, 0);
				}
			}
		}
		
	}
	
	private void removeAll()
	{
		this.shownItem.getVehicle().remove();
		this.shownItem.remove();
		this.shownItem = null;
		this.shownWeapon = null;
		this.clicker = null;
		this.superCounter = 0L;
		Block b = this.mbm.getBox();
		for(Player player : b.getWorld().getPlayers())
			player.playNote(b.getLocation(), (byte) 1, (byte) 0);
	}
	
	public void onChangeLoc(final Location newLoc)
	{
		if(this.magicBoxName != null)
		{
			this.magicBoxName.remove();
		}
	}
	
	public ArmorStand getClickableArmorStandWhenLaBoxEstOuverte()
	{
		return this.vehicle;
	}
	
	public ArmorStand getClickableArmorStandWhenLaBoxEstPasOuverte()
	{
		return this.magicBoxName;
	}
}

package fr.lumin0u.survivor.player;

import fr.lumin0u.survivor.DamageTarget;
import fr.lumin0u.survivor.weapons.Weapon;
import fr.lumin0u.survivor.weapons.WeaponType;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.stream.Collectors;

public interface WeaponOwner extends SvDamageable
{
	public default List<WeaponType> getWeaponTypes() {
		return getWeapons().stream().map(Weapon::getType).collect(Collectors.toList());
	}
	
	public List<Weapon> getWeapons();
	
	public void addWeapon(Weapon weapon);
	
	public void removeWeapon(Weapon weapon);
	
	public default <T extends Weapon> List<T> getWeaponsByType(Class<T> clazz) {
		return (List<T>) getWeapons().stream().filter(clazz::isInstance).collect(Collectors.toList());
	}
	
	public Weapon getWeaponInHand();
	
	public ItemStack findItem(Weapon weapon);
	
	public default boolean hasItem(Weapon weapon) {
		return findItem(weapon) != null;
	}
	
	public ItemStack getItemInHand();
	
	public boolean canUseWeapon();
	
	public boolean hasDoubleCoup();
	
	public boolean hasSpeedReload();
	
	public boolean doInstantKill();
	
	public Location getShootLocation();
	
	public void giveWeaponItem(Weapon weapon);
	
	public DamageTarget getTargetType();
}

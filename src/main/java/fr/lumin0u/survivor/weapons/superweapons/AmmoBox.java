package fr.lumin0u.survivor.weapons.superweapons;

import fr.lumin0u.survivor.player.WeaponOwner;
import fr.lumin0u.survivor.weapons.IPlaceable;
import fr.lumin0u.survivor.weapons.SupplyWeapon;
import fr.lumin0u.survivor.weapons.WeaponType;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.inventory.ClickType;

public class AmmoBox extends SuperWeapon implements SupplyWeapon, IPlaceable
{
	public AmmoBox(WeaponOwner owner)
	{
		super(owner, WeaponType.AMMO_BOX);
	}
	
	@Override
	public ClickType getMainClickAction() {
		return ClickType.CREATIVE;
	}
	
	@Override
	public void rightClick()
	{
	
	}
	
	@Override
	public void leftClick()
	{
	
	}
	
	@Override
	public void place(Block block, BlockFace against) {
		useAmmo();
		block.setType(Material.CAKE);
	}
	
	@Override
	public boolean canPlace(Block block, BlockFace against) {
		return block.isEmpty() && Turret.placeableOn.test(block.getRelative(BlockFace.DOWN).getType());
	}
}

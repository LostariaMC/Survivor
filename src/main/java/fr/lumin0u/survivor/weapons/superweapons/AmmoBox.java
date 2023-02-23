package fr.lumin0u.survivor.weapons.superweapons;

import fr.lumin0u.survivor.player.WeaponOwner;
import fr.lumin0u.survivor.utils.TransparentUtils;
import fr.lumin0u.survivor.weapons.SupplyWeapon;
import fr.lumin0u.survivor.weapons.WeaponType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.util.RayTraceResult;

public class AmmoBox extends SuperWeapon implements SupplyWeapon
{
	public AmmoBox(WeaponOwner owner)
	{
		super(owner, WeaponType.AMMO_BOX);
	}
	
	@Override
	public ClickType getMainClickAction() {
		return ClickType.RIGHT;
	}
	
	@Override
	public void rightClick()
	{
		Location eyeLoc = owner.getShootLocation();
		RayTraceResult collisionResult = TransparentUtils.collisionBetween(eyeLoc, eyeLoc.clone().add(eyeLoc.getDirection().multiply(3)), true);
		
		if(collisionResult != null)
		{
			Block placeSlot = collisionResult.getHitBlock().getRelative(collisionResult.getHitBlockFace());
			if(Turret.placeableOn.test(placeSlot.getRelative(0, -1, 0).getType()) && placeSlot.getType().equals(Material.AIR))
			{
				useAmmo();
				placeSlot.setType(Material.CAKE);
			}
		}
	}
	
	@Override
	public void leftClick()
	{
	
	}
}

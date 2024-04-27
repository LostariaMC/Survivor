package fr.lumin0u.survivor.weapons.superweapons;

import fr.lumin0u.survivor.player.SvPlayer;
import fr.lumin0u.survivor.player.WeaponOwner;
import fr.lumin0u.survivor.utils.TransparentUtils;
import fr.lumin0u.survivor.weapons.IPlaceable;
import fr.lumin0u.survivor.weapons.SupplyWeapon;
import fr.lumin0u.survivor.weapons.TurretRunnable;
import fr.lumin0u.survivor.weapons.WeaponType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.RayTraceResult;

import java.util.Iterator;

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
		/*if(!(owner instanceof SvPlayer))
			return;
		
		Iterator<Block> sight = new BlockIterator(((SvPlayer) owner).toBukkit(), 3);
		
		Block last = owner.getShootLocation().getBlock();
		while(sight.hasNext())
		{
			Block current = sight.next();
			if(Turret.placeableOn.test(last.getRelative(0, -1, 0).getType()) && last.getType().equals(Material.AIR))
			{
				useAmmo();
				last.setType(Material.CAKE);
				break;
			}
			last = current;
		}*/
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

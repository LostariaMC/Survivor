package fr.lumin0u.survivor.weapons.superweapons;

import fr.lumin0u.survivor.player.SvPlayer;
import fr.lumin0u.survivor.player.WeaponOwner;
import fr.lumin0u.survivor.utils.TransparentUtils;
import fr.lumin0u.survivor.weapons.SupplyWeapon;
import fr.lumin0u.survivor.weapons.TurretRunnable;
import fr.lumin0u.survivor.weapons.WeaponType;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.util.BlockIterator;

import java.util.Iterator;
import java.util.function.Predicate;

public class Turret extends SuperWeapon implements SupplyWeapon
{
	public static final Predicate<Material> placeableOn = material -> (TransparentUtils.isFullBlock(material) || material.name().matches(".*SLAB2|.*STEP|.*STAIRS")) && !material.equals(Material.BEACON) && !material.equals(Material.CAKE);
	
	public Turret(WeaponOwner owner) {
		super(owner, WeaponType.TURRET);
	}
	
	@Override
	public ClickType getMainClickAction() {
		return ClickType.RIGHT;
	}
	
	@Override
	public void rightClick() {
		if(!(owner instanceof SvPlayer))
			return;
		
		Iterator<Block> sight = new BlockIterator(((SvPlayer) owner).toBukkit(), 3);
		
		Block last = owner.getShootLocation().getBlock();
		while(sight.hasNext())
		{
			Block current = sight.next();
			if(placeableOn.test(last.getRelative(0, -1, 0).getType()) && last.getType().equals(Material.AIR))
			{
				useAmmo();
				new TurretRunnable(last, (SvPlayer) owner, this).start();
				break;
			}
			last = current;
		}
	}
	
	@Override
	public void leftClick() {
	}
}

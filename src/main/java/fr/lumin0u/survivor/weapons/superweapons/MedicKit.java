package fr.lumin0u.survivor.weapons.superweapons;

import fr.lumin0u.survivor.GameManager;
import fr.lumin0u.survivor.player.SvPlayer;
import fr.lumin0u.survivor.player.WeaponOwner;
import fr.lumin0u.survivor.weapons.SupplyWeapon;
import fr.lumin0u.survivor.weapons.WeaponType;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class MedicKit extends SuperWeapon implements SupplyWeapon
{
	public MedicKit(final WeaponOwner owner)
	{
		super(owner, WeaponType.MEDIC_KIT);
	}
	
	@Override
	public ClickType getMainClickAction() {
		return ClickType.RIGHT;
	}
	
	@Override
	public void rightClick()
	{
		this.useAmmo();
		
		for(SvPlayer sp : GameManager.getInstance().getOnlinePlayers())
		{
			if(sp.toBukkit().getLocation().distanceSquared(owner.getShootLocation()) < 20*20)
			{
				sp.toBukkit().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20 * 20, 2, true));
			}
			
			for(int i = 0; i < 100; ++i)
			{
				Location particleLoc = owner.getShootLocation().add(Math.random() * 20.0D - 10.0D, Math.random() * 4.0D - 2.0D, Math.random() * 20.0D - 10.0D);
				sp.toBukkit().spawnParticle(Particle.VILLAGER_HAPPY, particleLoc, 0);
			}
		}
	}
	
	@Override
	public void leftClick()
	{
	}
}

package fr.lumin0u.survivor.weapons.superweapons;

import fr.lumin0u.survivor.GameManager;
import fr.lumin0u.survivor.Survivor;
import fr.lumin0u.survivor.mobs.Waves;
import fr.lumin0u.survivor.player.WeaponOwner;
import fr.lumin0u.survivor.weapons.IRailGun;
import fr.lumin0u.survivor.weapons.WeaponType;
import org.bukkit.Color;
import org.bukkit.scheduler.BukkitRunnable;

public class TripleRailGun extends Gun implements IRailGun
{
	public TripleRailGun(WeaponOwner owner)
	{
		super(owner, WeaponType.TRIPLE_RAILGUN);
	}
	
	@Override
	public void rightClick() {
		useAmmo();
		
		new BukkitRunnable()
		{
			int i = 0;
			
			@Override
			public void run()
			{
				shoot();
				
				++i;
				
				if(i >= 3)
				{
					this.cancel();
				}
			}
		}.runTaskTimer(Survivor.getInstance(), 0L, 3L);
	}
	
	@Override
	public void leftClick()
	{
	}
	
	@Override
	public double getDmg()
	{
		return Waves.getEnnemiesLife(GameManager.getInstance().getWave(), GameManager.getInstance().getDifficulty()) * 0.6D;
	}
	
	@Override
	public Color getRailColor() {
		return Color.fromRGB(1, 200, 0);
	}
}

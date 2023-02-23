package fr.lumin0u.survivor.mobs.mob;

import fr.lumin0u.survivor.player.SvPlayer;
import fr.lumin0u.survivor.utils.AABB;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Iterator;

public class Wolf extends Enemy
{
	public Wolf(Location spawnLoc, double maxHealth, double walkSpeed)
	{
		super(EntityType.WOLF, spawnLoc, maxHealth, walkSpeed);
	}
	
	@Override
	public void spawnEntity(Location spawnLoc)
	{
		super.spawnEntity(spawnLoc);
		((org.bukkit.entity.Wolf) this.ent).setAdult();
		((org.bukkit.entity.Wolf) this.ent).setAngry(true);
		this.ent.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(60.0D);
	}
	
	@Override
	public AABB getBodyHitbox()
	{
		return new AABB(this.ent.getLocation().add(-0.45D, 0.0D, -0.45D), this.ent.getLocation().add(0.45D, 1.0D, 0.45D));
	}
	
	@Override
	public AABB getHeadHitbox()
	{
		Vector v = this.ent.getEyeLocation().getDirection().multiply(0.7D);
		return new AABB(this.ent.getLocation().add(v.getX() - 0.125D, 0.45D, v.getZ() - 0.125D), this.ent.getLocation().add(0.125D, 1.0D, 0.125D));
	}
    
    @Override
	public void navigation()
	{
		Iterator var1 = this.gm.getPlayers().iterator();
		
		while(true)
		{
			SvPlayer sp;
			Player p;
			do
			{
				if(!var1.hasNext())
				{
					((org.bukkit.entity.Wolf) this.ent).setAngry(true);
					if(this.target != null)
					{
						((org.bukkit.entity.Wolf) this.ent).setTarget(this.target.getPlayer());
					}
					
					return;
				}
				
				sp = (SvPlayer) var1.next();
				p = sp.getPlayer();
			} while(this.target != null && !(p.getLocation().distance(this.ent.getLocation()) < this.target.getPlayer().getLocation().distance(this.ent.getLocation())));
			
			if(sp.isAlive() && p.getGameMode().equals(GameMode.ADVENTURE))
			{
				this.target = sp;
			}
		}
	}
}

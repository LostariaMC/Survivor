package fr.lumin0u.survivor.listeners;

import fr.lumin0u.survivor.GameManager;
import fr.lumin0u.survivor.mobs.mob.Enemy;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

import java.util.ArrayList;

public class EntityEvents implements Listener
{
	public EntityEvents()
	{
	}
	
	@EventHandler
	public void onDamage(EntityDamageEvent e)
	{
		if(GameManager.getInstance() != null)
		{
			
			for(Enemy m : new ArrayList<>(GameManager.getInstance().getMobs())) {
				if(m.getEntity().equals(e.getEntity())) {
					e.setCancelled(true);
				}
				if(e.getCause() == DamageCause.VOID) {
					m.kill(null);
				}
			}
			
			if(e.getEntityType().equals(EntityType.ITEM_FRAME))
			{
				e.setCancelled(true);
			}
			
		}
	}
	
	@EventHandler
	public void onDamageByEntity(EntityDamageByEntityEvent e)
	{
		if(GameManager.getInstance() != null)
		{
			if(e.getDamager() instanceof Player && !(e.getEntity() instanceof ItemFrame))
			{
				e.setCancelled(true);
			}
			
			if(e.getDamager() instanceof Projectile)
			{
				e.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onEntityExplode(EntityExplodeEvent e)
	{
		e.setCancelled(true);
		e.getEntity().remove();
	}
	
	@EventHandler
	public void onProjectileHit(ProjectileHitEvent e)
	{
	}
	
	@EventHandler
	public void onPhysics(BlockPhysicsEvent e)
	{
		e.setCancelled(true);
	}
	
	@EventHandler
	public void onWeatherChange(WeatherChangeEvent e)
	{
		if(e.toWeatherState())
		{
			e.setCancelled(true);
		}
		
	}
}

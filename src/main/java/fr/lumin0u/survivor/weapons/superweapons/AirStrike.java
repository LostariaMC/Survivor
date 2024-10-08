package fr.lumin0u.survivor.weapons.superweapons;

import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.events.PacketContainer;
import fr.lumin0u.survivor.GameManager;
import fr.lumin0u.survivor.Survivor;
import fr.lumin0u.survivor.player.WeaponOwner;
import fr.lumin0u.survivor.utils.MCUtils;
import fr.lumin0u.survivor.utils.TransparentUtils;
import fr.lumin0u.survivor.weapons.WeaponType;
import fr.worsewarn.cosmox.api.players.WrappedPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class AirStrike extends SuperWeapon
{
	public AirStrike(WeaponOwner owner) {
		super(owner, WeaponType.AIRSTRIKE);
	}
	
	@Override
	public ClickType getMainClickAction() {
		return ClickType.RIGHT;
	}
	
	@Override
	public void rightClick() {
		for(int i = 0; i < 15; i++)
		{
			Random r = new Random();
			this.fireball(owner.getShootLocation().clone().add(r.nextDouble() * 10 - 5, 100 + r.nextDouble() * 30 - 15, r.nextDouble() * 10 - 5));
		}
		this.useAmmo();
	}
	
	@Override
	public void leftClick() {
	}
	
	@Override
	public List<String> getLore() {
		List<String> lore = super.getLore();
		lore.add("§e§lUtilisable en étant au sol");
		return lore;
	}
	
	private void fireball(Location loc) {
		final Vector direction = new Vector(0.0D, -1.5D, 0.0D);
		
		final int eid = new Random().nextInt();
		
		PacketContainer spawnPacket = new PacketContainer(Server.SPAWN_ENTITY);
		spawnPacket.getIntegers()
				.write(0, eid)
				.write(2, (int) (direction.getY() * 8000));
		spawnPacket.getUUIDs().write(0, UUID.randomUUID());
		spawnPacket.getDoubles()
				.write(0, loc.getX())
				.write(1, loc.getY())
				.write(2, loc.getZ());
		spawnPacket.getEntityTypeModifier().write(0, EntityType.FIREBALL);
		
		for(Player player : Bukkit.getOnlinePlayers())
			WrappedPlayer.of(player).sendPacket(spawnPacket);
		
		Location up3 = loc.clone();
		up3.setY(owner.getShootLocation().getY() + 1);
		double yStop = TransparentUtils.hitPointOrL2(up3, up3.clone().add(0, -6, 0), true).getY();
		
		new BukkitRunnable()
		{
			int i = 0;
			
			@Override
			public void run() {
				Location floc = loc.clone().add(direction.clone().multiply(i));
				
				PacketContainer movePacket = new PacketContainer(Server.ENTITY_TELEPORT);
				movePacket.getIntegers()
						.write(0, eid);
				movePacket.getDoubles()
						.write(0, floc.getX())
						.write(1, floc.getY())
						.write(2, floc.getZ());
				
				for(Player player : Bukkit.getOnlinePlayers())
					WrappedPlayer.of(player).sendPacket(movePacket);
				
				MCUtils.explosionParticles(floc, 2.0F, 6, Particle.FLAME, Particle.SMOKE_LARGE);
				if(floc.getY() <= yStop)
				{
					double life = GameManager.getInstance().getBaseEnnemyHealth();
					MCUtils.explosion(owner, AirStrike.this, life / 3, floc, 10.0D, 0.0D, owner.getTargetType());
					this.cancel();
					
					PacketContainer destroyPacket = new PacketContainer(Server.ENTITY_DESTROY);
					destroyPacket.getIntLists().write(0, Collections.singletonList(eid));
					
					for(Player player : Bukkit.getOnlinePlayers())
						WrappedPlayer.of(player).sendPacket(destroyPacket);
				}
				
				i++;
			}
		}.runTaskTimer(Survivor.getInstance(), 0L, 1L);
	}
}

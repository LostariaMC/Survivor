package fr.lumin0u.survivor.objects;

import fr.lumin0u.survivor.GameManager;
import fr.lumin0u.survivor.player.SvPlayer;
import fr.lumin0u.survivor.utils.AABB;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;

import java.util.List;
import java.util.Random;

public class MagicBoxManager
{
	private final List<Location> possibleLocations;
	private Block currentBox;
	private MBTask mbTask;
	private GameManager gm;
	public static final int boxPrice = 1000;
	
	public MagicBoxManager(List<Location> possibleLocations, GameManager gm)
	{
		this.gm = gm;
		this.possibleLocations = possibleLocations;
		
		if(possibleLocations.isEmpty())
		{
			Bukkit.broadcastMessage("§cIl n'y a aucun emplacement de boite magique.");
		}
		else
		{
			for(Location l : possibleLocations)
			{
				l.getBlock().setType(Material.AIR);
			}
		}
	}
	
	public void onGameStart()
	{
		this.mbTask = new MBTask(this);
		this.changeLoc();
		this.mbTask.start();
	}
	
	public List<Location> getPossibleLocations()
	{
		return this.possibleLocations;
	}
	
	public void changeLoc()
	{
		Location last = null;
		if(this.currentBox != null)
		{
			last = this.currentBox.getLocation();
			this.currentBox.setType(Material.AIR);
		}
		
		do
		{
			this.currentBox = this.possibleLocations.get((new Random()).nextInt(this.possibleLocations.size())).getBlock();
		} while(last != null && this.currentBox.getLocation().equals(last) && this.possibleLocations.size() > 1);
		
		if(last != null)
		{
			for(int i = 0; i < 50; ++i)
			{
				Location loc = AABB.cube(this.currentBox.getLocation()).multiply(0.7D).rdContourLoc().toLocation(this.gm.getWorld());
				last.getWorld().spawnParticle(Particle.BLOCK_CRACK, loc, 0, Material.ENDER_CHEST.createBlockData());
			}
		}
		
		this.currentBox.setType(Material.ENDER_CHEST);
		this.mbTask.onChangeLoc(this.currentBox.getLocation());
	}
	
	public Block getBox()
	{
		return this.currentBox;
	}
	
	public void onClickOnBox(SvPlayer p) {
		this.mbTask.onClickOnBox(p);
	}
	
	public MBTask getMbTask()
	{
		return this.mbTask;
	}
}

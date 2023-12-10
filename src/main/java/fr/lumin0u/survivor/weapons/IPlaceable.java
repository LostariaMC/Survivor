package fr.lumin0u.survivor.weapons;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public interface IPlaceable {
	public void place(Block block, BlockFace against);
	
	public boolean canPlace(Block block, BlockFace against);
}

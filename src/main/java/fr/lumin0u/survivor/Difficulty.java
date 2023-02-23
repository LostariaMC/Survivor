package fr.lumin0u.survivor;

import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Contract;

public enum Difficulty
{
	//SUPER_EASY("Très simple", 20.0D, 1.0D, ChatColor.AQUA, Material.PLAYER_HEAD, 0.5D, 1, 0.4D),
	EASY("Simple", 16.0D, 0.5D, ChatColor.GREEN, Material.CREEPER_HEAD, 1.0D, 2, 1.0D),
	NORMAL("Normal", 10.0D, 0.5D, ChatColor.YELLOW, Material.ZOMBIE_HEAD, 2.0D, 3, 1.15D),
	CLASSIC("Classique", 4.0D, 1.0D, ChatColor.RED, Material.SKELETON_SKULL, 3.0D, 4, 1.2D),
	HARDCORE("Hardcore", 4.0D, 0.2D, ChatColor.GRAY, Material.WITHER_SKELETON_SKULL, 4.0D, 5, 1.4D);
	
	private final double maxHealth;
	private final double regenTime;
	private final ChatColor color;
	private final Material skull;
	private final double monM;
	private final int id;
	private final double ennemyHealthModifier;
	private final String displayName;
	
	private Difficulty(String displayName, double maxHealth, double regenTime, ChatColor color, Material skull, double monM, int id, double ennemyHealthModifier)
	{
		this.maxHealth = maxHealth;
		this.regenTime = regenTime;
		this.color = color;
		this.skull = skull;
		this.monM = monM;
		this.id = id;
		this.ennemyHealthModifier = ennemyHealthModifier;
		this.displayName = displayName;
	}
	
	public double getMaxHealth()
	{
		return this.maxHealth;
	}
	
	public double regenHpPerSecond()
	{
		return this.regenTime;
	}
	
	@Contract("-> new")
	public ItemStack getGlass()
	{
		ItemStack head = new ItemStack(skull);
		ItemMeta meta = head.getItemMeta();
		meta.displayName(Component.text(this.color + this.displayName));
		head.setItemMeta(meta);
		return head;
	}
	
	public ChatColor getColor()
	{
		return this.color;
	}
	
	public double a()
	{
		return this.monM;
	}
	
	public int getNB()
	{
		return this.id;
	}
	
	public double getEnnemyHealthModifier()
	{
		return this.ennemyHealthModifier;
	}
	
	public String getDisplayName()
	{
		return this.displayName;
	}
	
	public String getColoredDisplayName()
	{
		return this.color + this.displayName;
	}
}

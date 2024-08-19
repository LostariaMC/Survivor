package fr.lumin0u.survivor;

import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public enum Difficulty
{
	NOT_SET("Non définie (à voter)", 20, 0.5, ChatColor.LIGHT_PURPLE, Material.PLAYER_HEAD, 0, 1),
	//SUPER_EASY("Très simple", 20.0D, 1.0D, ChatColor.AQUA, Material.PLAYER_HEAD, 0.5D, 1, 0.4D),
	EASY("Simple", 16.0D, 0.5D, ChatColor.GREEN, Material.CREEPER_HEAD, 2, 1.0D),
	NORMAL("Normal", 10.0D, 0.5D, ChatColor.YELLOW, Material.ZOMBIE_HEAD, 3, 1.15D),
	CLASSIC("Hard", 6.0D, 0.3D, ChatColor.RED, Material.SKELETON_SKULL, 4.2, 1.4D),
	HARDCORE("Expert", 4.0D, 0.2D, ChatColor.DARK_GRAY, Material.WITHER_SKELETON_SKULL, 5, 1.4D);
	
	private final double maxHealth;
	private final double regenHpPerSecond;
	private final ChatColor color;
	private final Material skull;
	private final double difficultyFactor;
	private final double ennemyHealthModifier;
	private final String displayName;
	
	private Difficulty(String displayName, double maxHealth, double regenHpPerSecond, ChatColor color, Material skull, double difficultyFactor, double ennemyHealthModifier)
	{
		this.maxHealth = maxHealth;
		this.regenHpPerSecond = regenHpPerSecond;
		this.color = color;
		this.skull = skull;
		this.difficultyFactor = difficultyFactor;
		this.ennemyHealthModifier = ennemyHealthModifier;
		this.displayName = displayName;
	}
	
	public double getMaxHealth()
	{
		return this.maxHealth;
	}
	
	public double regenHpPerSecond()
	{
		return this.regenHpPerSecond;
	}
	
	public Material getSkullType() {
		return skull;
	}
	
	public ItemStack getItemRep()
	{
		ItemStack head = new ItemStack(skull);
		ItemMeta meta = head.getItemMeta();
		meta.displayName(Component.text(this.color + this.displayName));
		head.setItemMeta(meta);
		return head;
	}
	
	public ItemStack getItemRep(int votes)
	{
		ItemStack head = new ItemStack(skull);
		ItemMeta meta = head.getItemMeta();
		meta.displayName(Component.text(this.color + this.displayName + " §7(%d)".formatted(votes)));
		head.setItemMeta(meta);
		return head;
	}
	
	public ChatColor getColor()
	{
		return this.color;
	}
	
	public double getFactor()
	{
		return this.difficultyFactor;
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
	
	public String getBestWaveStatKey() {
		return switch(this) {
            case NOT_SET ->
                    "";
            case EASY ->
                    SurvivorGame.WAVE_REACHED_EASY_STAT;
            case NORMAL ->
					SurvivorGame.WAVE_REACHED_NORMAL_STAT;
            case CLASSIC ->
					SurvivorGame.WAVE_REACHED_HARD_STAT;
            case HARDCORE ->
					SurvivorGame.WAVE_REACHED_EXPERT_STAT;
        };
	}
}

package fr.lumin0u.survivor;

import fr.lumin0u.survivor.utils.ItemBuilder;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum SvAsset
{
	MASTODONTE("Mastodonte", Material.DIAMOND_CHESTPLATE, 3000),
	DOUBLE_COUP("Double coup", Material.TRIPWIRE_HOOK, 3500),
	MARATHON("Marathon", Material.SUGAR, 2000),
	SPEED_RELOAD("Speed cola", Material.IRON_BARS, 3000),
	TROIS_ARME("3 armes", Material.COMPARATOR, 2000),
	QUICK_REVIVE("Quick revive", Material.APPLE, 2500),
	PIERRE_TOMBALE("Pierre tombale", Material.FIRE_CHARGE, 2000);
	
	private final String name;
	private final Material material;
	private final int price;
	
	private SvAsset(String name, Material material, int price)
	{
		this.name = name;
		this.material = material;
		this.price = price;
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public static List<String> getNames()
	{
		return Arrays.stream(values()).map(SvAsset::getName).toList();
	}
	
	public static SvAsset byName(String name)
	{
		return Arrays.stream(values()).filter(a -> a.getName().equals(name)).findFirst().orElse(null);
	}
	
	public ItemStack getItem()
	{
		ItemStack item = new ItemStack(this.material);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName("§b" + this.name + " §e" + this.price + "$");
		item.setItemMeta(meta);
		return item;
	}
	
	public Material getMaterial()
	{
		return this.material;
	}
	
	public int getPrice()
	{
		return this.price;
	}
	
	public static SvAsset byMat(Material mat)
	{
		return Arrays.stream(values()).filter(a -> a.getMaterial() == mat).findFirst().orElse(null);
	}
	
	public static ItemStack getMastoArmorPiece(Material part)
	{
		return new ItemBuilder(part).setColor(Color.BLACK).setUnbreakable(true).build();
	}
}

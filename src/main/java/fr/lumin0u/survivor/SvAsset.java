package fr.lumin0u.survivor;

import fr.lumin0u.survivor.utils.ItemBuilder;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public enum SvAsset
{
	MASTODONTE("Mastodonte", Material.DIAMOND_CHESTPLATE, 3000),
	DOUBLE_COUP("Double coup", Material.TRIPWIRE_HOOK, 3500),
	MARATHON("Marathon", Material.SUGAR, 2000),
	SPEED_RELOAD("Recharge rapide", Material.IRON_BARS, 3000),
	TROIS_ARME("3 armes", Material.COMPARATOR, 2000),
	QUICK_REVIVE("Réanimation rapide", Material.APPLE, 2500),
	PIERRE_TOMBALE("Pierre tombale", Material.FIREWORK_STAR, 2000);
	
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
	
	public static SvAsset byName(String name) {
		return Arrays.stream(values())
				.filter(a -> a.getName().equals(name))
				.findFirst()
				.orElse(null);
	}
	
	public ItemStack getItem() {
		return new ItemBuilder(material)
				.setDisplayName("§b" + this.name + " §e" + this.price + "$")
				.build();
	}
	
	public Material getMaterial()
	{
		return this.material;
	}
	
	public int getPrice()
	{
		return this.price;
	}
	
	public static SvAsset byMaterial(Material mat)
	{
		return Arrays.stream(values()).filter(a -> a.getMaterial() == mat).findFirst().orElse(null);
	}
	
	public static ItemStack getMastoArmorPiece(Material part)
	{
		return new ItemBuilder(part).setLeatherColor(Color.BLACK).setUnbreakable(true).build();
	}
}

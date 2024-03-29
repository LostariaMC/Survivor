package fr.lumin0u.survivor.utils;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ImmutableItemStack extends ItemStack
{
	public ImmutableItemStack(@NotNull ItemStack stack) {
		super(stack);
	}
	
	public ImmutableItemStack(@NotNull Material type) {
		super(type);
	}
	
	public ImmutableItemStack(@NotNull Material type, int amount) {
		super(type, amount);
	}
	
	@Deprecated
	public ImmutableItemStack(@NotNull Material type, int amount, short damage) {
		super(type, amount, damage);
	}
	
	@Deprecated
	public ImmutableItemStack(@NotNull Material type, int amount, short damage, @Nullable Byte data) {
		super(type, amount, damage, data);
	}
	
	public void throwImmutable() throws IllegalStateException {
		throw new IllegalStateException("This itemstack is immutable");
	}
	
	@Override
	public boolean setItemMeta(@Nullable ItemMeta itemMeta) {
		throwImmutable();
		return false;
	}
	
	@Override
	public void setAmount(int amount) {
		throwImmutable();
	}
	
	@Override
	@Deprecated
	public void setData(@Nullable MaterialData data) {
		throwImmutable();
	}
	
	@Override
	public void setType(@NotNull Material type) {
		throwImmutable();
	}
	
	@Override
	@Deprecated
	public void setDurability(short durability) {
		throwImmutable();
	}
	
	@Override
	public void addUnsafeEnchantment(@NotNull Enchantment ench, int level) {
		throwImmutable();
	}
	
	@NotNull
	@Override
	public ItemStack clone() {
		return new ItemStack(this);
	}
}

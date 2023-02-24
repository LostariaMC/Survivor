package fr.lumin0u.survivor.weapons.perks;

import java.util.List;

public abstract class Perk
{
	public static final int PRICE = 1000;
	
	private static List<Perk> perks;
	
	public static final Perk FIRE_BULLET = new Perk("balles de feu", "§6balles de feu") {};
	
	private final String name;
	private final String displayName;
	
	private Perk(String name, String displayName)
	{
		this.name = name;
		this.displayName = displayName;
	}
	
	public String getName() {
		return name;
	}
	
	public String getDisplayName() {
		return displayName;
	}
}

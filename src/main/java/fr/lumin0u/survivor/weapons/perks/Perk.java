package fr.lumin0u.survivor.weapons.perks;

import fr.lumin0u.survivor.utils.Utils;
import fr.lumin0u.survivor.weapons.Weapon;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class Perk
{
	public static final int PRICE = 1000;
	
	private static final List<Perk> perks;
	
	public static final Perk FIRE_BULLET = new Perk("balles de feu", "§6balles de feu", List.of("Une chance d'enflammer", "vos ennemis"))
	{
		public boolean testRandomDrop(Random random) {
			return random.nextInt(20) == 0;
		}
	};
	public static final Perk FREE_BULLETS = new Perk("balles gratuites", "§aballes gratuites", List.of("Une chance de ne pas", "consommer de munitions"))
	{
		public boolean testRandomDrop(Random random) {
			return random.nextInt(5) == 0;
		}
	};
	public static final Perk FASTER_RELOAD = new Perk("recharge rapide", "§frecharge rapide", List.of("L'arme recharge plus vite"))
	{
		public boolean testRandomDrop(Random random) {
			return true;
		}
	};
	public static final Perk CRIT_BULLETS = new Perk("dégats critiques", "§cdégats critiques", List.of("Une chance d'infliger des", "dégats critiques"))
	{
		public boolean testRandomDrop(Random random) {
			return random.nextInt(20) == 0;
		}
	};
	public static final Perk EXPLOSIVE_BULLETS = new Perk("balles explosives", "§cballes explosives", List.of("Une chance de tirer", "une balle explosive"))
	{
		public boolean testRandomDrop(Random random) {
			return random.nextInt(30) == 0;
		}
	};
	
	static {
		perks = List.of(FIRE_BULLET, FREE_BULLETS, FASTER_RELOAD, CRIT_BULLETS, EXPLOSIVE_BULLETS);
	}
	
	public static Perk getRandomPerk() {
		return perks.stream().collect(Utils.randomCollector()).get();
	}
	
	private final String name;
	private final String displayName;
	private final List<String> description;
	
	private Perk(String name, String displayName, List<String> description)
	{
		this.name = name;
		this.displayName = displayName;
		this.description = new ArrayList<>(description);
	}
	
	public String getName() {
		return name;
	}
	
	public String getDisplayName() {
		return displayName;
	}
	
	public List<String> getDescription() {
		return new ArrayList<>(description);
	}
	
	public boolean testRandomDropAndHas(Weapon weapon) {
		return testRandomDropAndHas(weapon, new Random());
	}
	
	public boolean testRandomDropAndHas(Weapon weapon, Random random) {
		return weapon.hasPerk(this) && testRandomDrop(random);
	}
	
	public abstract boolean testRandomDrop(Random random);
}

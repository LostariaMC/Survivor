package fr.lumin0u.survivor.weapons;

import fr.lumin0u.survivor.mobs.mob.GrapplingHook;
import fr.lumin0u.survivor.mobs.mob.ZombieHuntingGun;
import fr.lumin0u.survivor.player.WeaponOwner;
import fr.lumin0u.survivor.utils.NMSUtils;
import fr.lumin0u.survivor.weapons.guns.*;
import fr.lumin0u.survivor.weapons.guns.rifles.*;
import fr.lumin0u.survivor.weapons.guns.shotguns.DoubleBarrel;
import fr.lumin0u.survivor.weapons.guns.shotguns.MCS;
import fr.lumin0u.survivor.weapons.guns.shotguns.SPAS12;
import fr.lumin0u.survivor.weapons.guns.snipers.BimBoumSniper;
import fr.lumin0u.survivor.weapons.guns.snipers.Dragunov;
import fr.lumin0u.survivor.weapons.guns.snipers.MosinSniper;
import fr.lumin0u.survivor.weapons.knives.BucherKnife;
import fr.lumin0u.survivor.weapons.knives.Knife;
import fr.lumin0u.survivor.weapons.knives.LittleKnife;
import fr.lumin0u.survivor.weapons.superweapons.*;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public enum WeaponType
{
	LITTLE_KNIFE(LittleKnife.class, RepeatingType.NONE, "Pelle", Material.IRON_SHOVEL, 100, 1, 5, 100, false, -1, 20, List.of("dmg"), List.of(4.2D)),
	BUCHER_KNIFE(BucherKnife.class, RepeatingType.NONE, "Pelle Lourde", Material.NETHERITE_SHOVEL, 30, 1, 20, 300, true, -1, 40, List.of("dmg"), List.of(18.9D)),
	
	M1911(M1911.class, RepeatingType.SEMIAUTOMATIC, "M1911", Material.WOODEN_PICKAXE, 80, 10, 40/*, new McSound("guns.m1911", 30)*/, 250, false, -1, 4, Arrays.asList("dmg", "range", "accuracy"), Arrays.asList(2.3D, 30.0D, 0.3D)),
	FAMAS(Famas.class, RepeatingType.BURSTS, "Famas", Material.GOLDEN_AXE, 210, 30, 86/*, new McSound("guns.famas", 80)*/, 2500, true, -1, 15, Arrays.asList("dmg", "range", "accuracy", "shots", "shotsDelay"), Arrays.asList(5.6D, 60.0D, 0.2D, 3, 3L)),
	AK47(AK47.class, RepeatingType.AUTOMATIC, "AK47", Material.WOODEN_HOE, 240, 30, 92/*, new McSound("guns.ak47", 60)*/, 500, true, -1, 5, Arrays.asList("dmg", "range", "accuracy"), Arrays.asList(3.6D, 40.0D, 0.5D)),
	M16(M16.class, RepeatingType.BURSTS, "M16", Material.STONE_HOE, 300, 30, 124/*, new McSound("guns.m16a3", 40)*/, 2000, true, -1, 15, Arrays.asList("dmg", "range", "accuracy", "shots", "shotsDelay"), Arrays.asList(6.4D, 25.0D, 0.3D, 3, 3L)),
	SCARL(Scare.class, RepeatingType.AUTOMATIC, "SCAR-L", Material.DIAMOND_HOE, 400, 40, 52/*, new McSound("guns.scar", 60)*/, 1000, true, -1, 6, Arrays.asList("dmg", "range", "accuracy"), Arrays.asList(4.5D, 30.0D, 0.2D)),
	SKORPION(Skorpion.class, RepeatingType.AUTOMATIC, "Skorpion", Material.STICK, 140, 20, 45/*, new McSound("guns.scorpion", 50)*/, 3500, true, -1, 2, Arrays.asList("dmg", "range", "accuracy"), Arrays.asList(2.8D, 20.0D, 0.7D)),
	MPLLF(MPLLF.class, RepeatingType.AUTOMATIC, "MPL-LF", Material.FEATHER, 500, 20, 13/*, new McSound("guns.mpllf", 35)*/, 900, true, -1, 2, Arrays.asList("dmg", "range", "accuracy"), Arrays.asList(1.4D, 25.0D, 1.0D)),
	RAILGUN(RailGun.class, RepeatingType.SEMIAUTOMATIC, "Railgun", Material.IRON_PICKAXE, 100, 5, 62/*, new McSound("guns.raygun", 40)*/, 690420, true, -1, 4, Arrays.asList("dmg", "range", "accuracy"), Arrays.asList(7.6D, 50.0D, 0.2D)),
	MP5(MP5.class, RepeatingType.AUTOMATIC, "MP5", Material.GOLDEN_HOE, 400, 20, 26/*, new McSound("guns.mp5", 40)*/, 1, true, -1, 3, Arrays.asList("dmg", "range", "accuracy"), Arrays.asList(3.5D, 25.0D, 0.5D)),
	UMP45(UMP45.class, RepeatingType.AUTOMATIC, "UMP45", Material.IRON_HOE, 360, 40, 41/*, new McSound("guns.ump45", 40)*/, 1, true, -1, 2, Arrays.asList("dmg", "range", "accuracy"), Arrays.asList(2.1D, 30.0D, 0.3D)),
	SPAS12(SPAS12.class, RepeatingType.SEMIAUTOMATIC, "SPAS-12", Material.GOLDEN_SHOVEL, 70, 1, 53/*, new McSound("guns.spas12", 80)*/, 1400, true, -1, 30, Arrays.asList("dmg", "range", "accuracy", "shots"), Arrays.asList(5.7D, 7.5D, 2.0D, 8)),
	MCS(MCS.class, RepeatingType.SEMIAUTOMATIC, "MCS", Material.STONE_PICKAXE, 80, 4, 84/*, new McSound("guns.olympia", 80)*/, 1100, true, -1, 30, Arrays.asList("dmg", "range", "accuracy", "shots"), Arrays.asList(6.9D, 20.0D, 0.8D, 5)),
	DOUBLE_BARREL(DoubleBarrel.class, RepeatingType.SEMIAUTOMATIC, "Double Barrel", Material.DIAMOND_SHOVEL, 60, 2, 70/*, new McSound("guns.doublebarrel", 80)*/, 1, true, -1, 30, Arrays.asList("dmg", "range", "accuracy", "shots"), Arrays.asList(5.8D, 20.0D, 1.1D, 7)),
	DRAGUNOV(Dragunov.class, RepeatingType.SEMIAUTOMATIC, "Dragunov", Material.GOLD_INGOT, 100, 1, 68/*, new McSound("guns.sniper", 60)*/, 1, true, -1, 20, Arrays.asList("dmg", "range", "accuracy"), Arrays.asList(51.7D, 300.0D, 0.05D)),
	MOSIN(MosinSniper.class, RepeatingType.SEMIAUTOMATIC, "Mosin", Material.GOLDEN_PICKAXE, 60, 1, 51/*, new McSound("guns.dragunov", 80)*/, 1, true, -1, 20, Arrays.asList("dmg", "range", "accuracy"), Arrays.asList(137.0D, 300.0D, 0.02D)),
	REVOLVER(Revolver.class, RepeatingType.SEMIAUTOMATIC, "Revolver", Material.DIAMOND_PICKAXE, 120, 6, 43/*, new McSound("guns.magnum", 50)*/, 1, true, -1, 15, Arrays.asList("dmg", "range", "accuracy"), Arrays.asList(8.7D, 35.0D, 0.25D)),
	THOMPSON(Thompson.class, RepeatingType.AUTOMATIC, "Thompson", Material.NETHER_BRICK, 400, 50, 95/*, new McSound("guns.thompson", 70)*/, 2000, true, -1, 4, Arrays.asList("dmg", "range", "accuracy"), Arrays.asList(6.2D, 50.0D, 0.5D)),
	TOMMY_GUN(TommyGun.class, RepeatingType.BURSTS, "Tommy Gun", Material.WOODEN_SHOVEL, 275, 25, 40/*, new McSound("guns.pm63", 35)*/, 500, true, -1, 17, Arrays.asList("dmg", "range", "accuracy", "shots", "shotsDelay"), Arrays.asList(3.4D, 20.0D, 0.8D, 5, 1L)),
	NERF(Nerf.class, RepeatingType.SEMIAUTOMATIC, "Nerf", Material.QUARTZ, 154, 11, 53/*, new McSound("guns.nerf", 15)*/, 1, true, -1, 23, Arrays.asList("dmg", "range", "accuracy"), Arrays.asList(9.7D, 100.0D, 0.2D)),
	
	TRIPLE_RAILGUN(TripleRailGun.class, RepeatingType.SEMIAUTOMATIC, "Railgun II", Material.DIAMOND, 0, 10, 42/*, new McSound("guns.raygun2", 40)*/, 1, true, 8, 8, Arrays.asList("dmg", "range", "accuracy"), Arrays.asList(23.4D, 35.0D, 0.5D)),
	AIRSTRIKE(AirStrike.class, RepeatingType.SEMIAUTOMATIC, "Airstrike", Material.REDSTONE, 0, 1, 1/*, new McSound("random.click", 10)*/, 1, false, 7, 1, List.of(), List.of()),
	MEDIC_KIT(MedicKit.class, RepeatingType.SEMIAUTOMATIC, "Medic Kit", Material.PAPER, 0, 1, 53/*, new McSound("guns.medic", 15)*/, 1, false, 6, 23, List.of(), List.of()),
	LANCEPATATE(LancePatate.class, RepeatingType.SEMIAUTOMATIC, "Lance Patate", Material.STONE_SHOVEL, 0, 5, 64/*, new McSound("guns.m79", 20)*/, 1, false, 8, 15, List.of(), List.of()),
	FREEZER(Freezer.class, RepeatingType.AUTOMATIC, "Freezer", Material.SHEARS, 0, 30, 1/*, new McSound("guns.hurlhiv", 20)*/, 1, true, 8, 4, Arrays.asList("dmg", "range", "accuracy", "particle"), Arrays.asList(2.8D, 10.0D, 1.0D, Particle.CLOUD)),
	BARBECUE(Barbecue.class, RepeatingType.AUTOMATIC, "Lance Flammes", Material.BLAZE_ROD, 0, 20, 1/*, new McSound("guns.flamme14", 20)*/, 1, true, 8, 10, Arrays.asList("dmg", "range", "accuracy", "particle"), Arrays.asList(3.0D, 10.0D, 1.0D, Particle.FLAME)),
	
	GRENADE(Grenade.class, RepeatingType.SEMIAUTOMATIC, "Grenade", Material.CLAY_BALL, 0, 3, 64, 1, false, 6, 1, List.of(), List.of()),
	GRENADEFRAG(GrenadeFrag.class, RepeatingType.SEMIAUTOMATIC, "Grenade Frag", Material.SLIME_BALL, 0, 2, 64, 1, false, 6, 1, List.of(), List.of()),
	GRENADEFLAME(GrenadeFlame.class, RepeatingType.SEMIAUTOMATIC, "Grenade Incendiaire", Material.MAGMA_CREAM, 0, 3, 64, 1, false, 6, 1, List.of(), List.of()),
	TURRET(Turret.class, RepeatingType.SEMIAUTOMATIC, "Tourelle", Material.GOLD_NUGGET, 0, 1, 64, 465278045, false, 6, 1, List.of(), List.of()),
	AMMO_BOX(AmmoBox.class, RepeatingType.SEMIAUTOMATIC, "Boite de munitions", Material.CAKE, 0, 1, 53, 1, false, 6, 1, List.of(), List.of()),
	
	HUNTING_GUN(ZombieHuntingGun.class, RepeatingType.SEMIAUTOMATIC, "Fusil de Chasse", Material.WOODEN_AXE, 10, 1, 60/*, new McSound("guns.mosin", 30)*/, 1, false, -1, 10, Arrays.asList("dmg", "range", "accuracy"), Arrays.asList(15D, 40.0D, 0.65D)),
	GRAPPLING_HOOK(GrapplingHook.class, RepeatingType.SEMIAUTOMATIC, "Grappin", Material.FISHING_ROD, 1, 1, 0/*, new McSound("random.bow", 0.5F, 10)*/, 1, false, -1, 10, Arrays.asList("dmg", "range", "accuracy"), Arrays.asList(0.0D, 5.0D, 0.0D)),
	
	BIMBOUMSNIPER(BimBoumSniper.class, RepeatingType.AUTOMATIC, "Bim Boum Sniper", Material.SADDLE, 2000, 40, 15/*, new McSound("guns.bimboumsniper", 500)*/, 1, false, -1, 3, Arrays.asList("dmg", "range", "accuracy"), Arrays.asList(16.7D, 100.0D, 0.05D));
	
	private final Class<? extends Weapon> weapon;
	private final String name;
	private final Material mat;
	private final int maxAmmo;
	private final int clipSize;
	private final int reloadTime;
	private final int price;
	private final HashMap<String, Object> others;
	private final boolean inMagicBox;
	private final int place;
	private final RepeatingType rt;
	private final int rpm;
	
	WeaponType(Class<? extends Weapon> weapon, RepeatingType rt, String name, Material mat, int maxAmmo, int clipSize, int reloadTime, int price, boolean inMagicBox, int place, int rpm, List<String> others, List<Object> values) {
		assert Arrays.stream(weapon.getDeclaredConstructors()).anyMatch(constructor -> Arrays.equals(constructor.getParameterTypes(), new Class<?>[]{WeaponOwner.class})) : "The weapon class must have a valid constructor";
		
		this.name = name;
		this.mat = mat;
		this.maxAmmo = maxAmmo;
		this.clipSize = clipSize;
		this.reloadTime = reloadTime;
		this.price = price;
		this.weapon = weapon;
		this.inMagicBox = inMagicBox;
		this.place = place;
		this.rt = rt;
		this.rpm = rpm;
		this.others = new HashMap<>();
		
		for(String s : others) {
			this.others.put(s, values.get(others.indexOf(s)));
		}
	}
	
	public ItemStack getItemToSell() {
		ItemStack item = new ItemStack(this.mat);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName("§9" + this.name + " §6" + this.price + "$ §eammo : §6" + this.price / 2 + "$");
		item.setItemMeta(meta);
		return item;
	}
	
	public String getName() {
		return this.name;
	}
	
	public Material getMaterial() {
		return this.mat;
	}
	
	public int getMaxAmmo() {
		return this.maxAmmo;
	}
	
	public int getClipSize() {
		return this.clipSize;
	}
	
	public int getReloadTime() {
		return this.reloadTime;
	}
	
	public int getPrice() {
		return this.price;
	}
	
	public boolean isInMagicBox() {
		return this.inMagicBox;
	}
	
	public int getPlace() {
		return Knife.class.isAssignableFrom(this.weapon) ? 0 : this.place;
	}
	
	public RepeatingType getRepeatingType() {
		return this.rt;
	}
	
	public int getRpm() {
		return this.rpm;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T get(String fieldName) {
		return (T) (others.containsKey(fieldName) ? others.get(fieldName) : NMSUtils.getFieldValue(fieldName, WeaponType.this));
	}
	
	public Class<? extends Weapon> getWeaponClass() {
		return this.weapon;
	}
	
	public <T extends Weapon> T giveNewWeapon(WeaponOwner owner) {
		try {
			return ((Class<T>) this.weapon).getDeclaredConstructor(WeaponOwner.class).newInstance(owner);
		} catch(ReflectiveOperationException | ClassCastException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static List<String> getNames() {
		return Arrays.stream(values()).map(WeaponType::getName).collect(Collectors.toList());
	}
	
	public static WeaponType byName(String name) {
		for(WeaponType wt : values()) {
			if(wt.getName().equals(name)) {
				return wt;
			}
		}
		
		return null;
	}
	
	public static WeaponType by_Name(String name) {
		for(WeaponType wt : values()) {
			if(wt.getName().replaceAll(" ", "_").equals(name)) {
				return wt;
			}
		}
		
		return null;
	}
	
	public static WeaponType getWeaponByItem(ItemStack item) {
		return Arrays.stream(values()).filter(type -> type.mat.equals(item.getType())).findAny().orElse(null);
	}
	
	public boolean isSuperWeaponType() {
		return SuperWeapon.class.isAssignableFrom(this.weapon);
	}
	
	public boolean isKnife() {
		return Knife.class.isAssignableFrom(this.weapon);
	}
	
	public boolean has(String fieldName) {
		return this.others.containsKey(fieldName);
	}
}

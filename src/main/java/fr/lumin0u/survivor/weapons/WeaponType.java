package fr.lumin0u.survivor.weapons;

import fr.lumin0u.survivor.mobs.mob.boss.BlazeBoss.BlazeGun;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.Math.PI;

public enum WeaponType
{
	LITTLE_KNIFE(LittleKnife.class, RepeatingType.NONE, "Pelle", Material.IRON_SHOVEL, 100, 1, 5, 100, false, -1, 10, Map.of("dmg", 4.2D)),
	BUCHER_KNIFE(BucherKnife.class, RepeatingType.NONE, "Pelle Lourde", Material.NETHERITE_SHOVEL, 30, 1, 20, 300, true, -1, 20, Map.of("dmg", 18.9D)),
	
	M1911(M1911.class, RepeatingType.SEMIAUTOMATIC, "M1911", Material.WOODEN_PICKAXE, 160, 10, 40/*, new McSound("guns.m1911", 30)*/, 250, false, -1, 4, Map.of("dmg", 2.8D, "range", 30.0D, "accuracy", PI / 120)),
	FAMAS(Famas.class, RepeatingType.AUTOMATIC, "Famas", Material.GOLDEN_AXE, 420, 30, 86/*, new McSound("guns.famas", 80)*/, 3500, true, -1, 5, Map.of("dmg", 5.6D, "range", 60.0D, "accuracy", PI / 160)),
	AK47(AK47.class, RepeatingType.AUTOMATIC, "AK47", Material.WOODEN_HOE, 480, 30, 92/*, new McSound("guns.ak47", 60)*/, 800, true, -1, 5, Map.of("dmg", 3.6D, "range", 40.0D, "accuracy", PI / 90)),
	M16(M16.class, RepeatingType.BURSTS, "M16", Material.STONE_HOE, 480, 30, 124/*, new McSound("guns.m16a3", 40)*/, 3000, true, -1, 15, Map.of("dmg", 6.4D, "range", 25.0D, "accuracy", PI / 120, "shots", 3, "shotsDelay", 3L)),
	SCARL(Scare.class, RepeatingType.AUTOMATIC, "SCAR-L", Material.DIAMOND_HOE, 600, 40, 52/*, new McSound("guns.scar", 60)*/, 2200, true, -1, 6, Map.of("dmg", 4.5D, "range", 30.0D, "accuracy", PI / 160)),
	SKORPION(Skorpion.class, RepeatingType.AUTOMATIC, "Skorpion", Material.STICK, 320, 20, 45/*, new McSound("guns.scorpion", 50)*/, 3500, true, -1, 2, Map.of("dmg", 2.8D, "range", 20.0D, "accuracy", PI / 40)),
	MPLLF(MPLLF.class, RepeatingType.AUTOMATIC, "MPL-LF", Material.FEATHER, 700, 20, 13/*, new McSound("guns.mpllf", 35)*/, 900, true, -1, 2, Map.of("dmg", 1.4D, "range", 25.0D, "accuracy", PI / 40)),
	RAILGUN(RailGun.class, RepeatingType.SEMIAUTOMATIC, "Railgun", Material.IRON_PICKAXE, 120, 5, 62/*, new McSound("guns.raygun", 40)*/, 690420, true, -1, 4, Map.of("dmg", 7.6D, "range", 50.0D, "accuracy", PI / 120)),
	MP5(MP5.class, RepeatingType.AUTOMATIC, "MP5", Material.GOLDEN_HOE, 700, 20, 26/*, new McSound("guns.mp5", 40)*/, 1, true, -1, 3, Map.of("dmg", 3.5D, "range", 25.0D, "accuracy", PI / 60)),
	UMP45(UMP45.class, RepeatingType.AUTOMATIC, "UMP45", Material.IRON_HOE, 640, 40, 41/*, new McSound("guns.ump45", 40)*/, 1, true, -1, 2, Map.of("dmg", 2.1D, "range", 30.0D, "accuracy", PI / 120)),
	SPAS12(SPAS12.class, RepeatingType.SEMIAUTOMATIC, "SPAS-12", Material.GOLDEN_SHOVEL, 70, 1, 53/*, new McSound("guns.spas12", 80)*/, 1400, true, -1, 30, Map.of("dmg", 5.7D, "range", 7.5D, "accuracy", PI / 50, "shots", 8)),
	MCS(MCS.class, RepeatingType.SEMIAUTOMATIC, "MCS", Material.STONE_PICKAXE, 80, 4, 84/*, new McSound("guns.olympia", 80)*/, 3300, true, -1, 30, Map.of("dmg", 6.9D, "range", 20.0D, "accuracy", PI / 60, "shots", 5)),
	DOUBLE_BARREL(DoubleBarrel.class, RepeatingType.SEMIAUTOMATIC, "Double Barrel", Material.DIAMOND_SHOVEL, 60, 2, 70/*, new McSound("guns.doublebarrel", 80)*/, 1, true, -1, 30, Map.of("dmg", 5.8D, "range", 20.0D, "accuracy", PI / 40, "shots", 7)),
	DRAGUNOV(Dragunov.class, RepeatingType.SEMIAUTOMATIC, "Dragunov", Material.GOLD_INGOT, 60, 1, 68/*, new McSound("guns.sniper", 60)*/, 1, true, -1, 20, Map.of("dmg", 51.7D, "range", 300.0D, "accuracy", PI / 600)),
	MOSIN(MosinSniper.class, RepeatingType.SEMIAUTOMATIC, "Mosin", Material.GOLDEN_PICKAXE, 60, 1, 51/*, new McSound("guns.dragunov", 80)*/, 2500, true, -1, 20, Map.of("dmg", 137.0D, "range", 300.0D, "accuracy", PI / 600)),
	REVOLVER(Revolver.class, RepeatingType.SEMIAUTOMATIC, "Revolver", Material.DIAMOND_PICKAXE, 120, 6, 43/*, new McSound("guns.magnum", 50)*/, 1, true, -1, 15, Map.of("dmg", 17.4D, "range", 35.0D, "accuracy", PI / 300)),
	THOMPSON(Thompson.class, RepeatingType.AUTOMATIC, "Thompson", Material.NETHERITE_PICKAXE, 400, 25, 95/*, new McSound("guns.thompson", 70)*/, 3000, true, -1, 4, Map.of("dmg", 6.2D, "range", 50.0D, "accuracy", PI / 80)),
	TOMMY_GUN(TommyGun.class, RepeatingType.BURSTS, "Tommy Gun", Material.WOODEN_SHOVEL, 400, 25, 40/*, new McSound("guns.pm63", 35)*/, 500, true, -1, 17, Map.of("dmg", 3.4D, "range", 20.0D, "accuracy", PI / 60, "shots", 5, "shotsDelay", 1L)),
	NERF(Nerf.class, RepeatingType.SEMIAUTOMATIC, "Nerf", Material.QUARTZ, 154, 11, 53/*, new McSound("guns.nerf", 15)*/, 1, false, -1, 23, Map.of("dmg", 9.7D, "range", 100.0D, "accuracy", PI / 80)),
	
	TRIPLE_RAILGUN(TripleRailGun.class, RepeatingType.SEMIAUTOMATIC, "Railgun II", Material.DIAMOND, 0, 10, 42/*, new McSound("guns.raygun2", 40)*/, 1, true, 8, 8, Map.of("dmg", 23.4D, "range", 35.0D, "accuracy", PI / 200)),
	AIRSTRIKE(AirStrike.class, RepeatingType.SEMIAUTOMATIC, "Airstrike", Material.REDSTONE, 0, 1, 1/*, new McSound("random.click", 10)*/, 1, false, 7, 1, Map.of()),
	MEDIC_KIT(MedicKit.class, RepeatingType.SEMIAUTOMATIC, "Medic Kit", Material.PAPER, 0, 1, 53/*, new McSound("guns.medic", 15)*/, 1, false, 6, 23, Map.of()),
	LANCEPATATE(LancePatate.class, RepeatingType.SEMIAUTOMATIC, "Lance Patate", Material.STONE_SHOVEL, 0, 5, 64/*, new McSound("guns.m79", 20)*/, 1, false, 8, 15, Map.of()),
	FREEZER(Freezer.class, RepeatingType.AUTOMATIC, "Freezer", Material.SHEARS, 0, 30, 1/*, new McSound("guns.hurlhiv", 20)*/, 1, true, 8, 4, Map.of("dmg", 2.8D, "range", 10.0D, "accuracy", 1.0D, "particle", Particle.CLOUD)),
	BARBECUE(Barbecue.class, RepeatingType.AUTOMATIC, "Lance Flammes", Material.BLAZE_ROD, 0, 20, 1/*, new McSound("guns.flamme14", 20)*/, 1, true, 8, 10, Map.of("dmg", 3.0D, "range", 10.0D, "accuracy", 1.0D, "particle", Particle.FLAME)),
	
	GRENADE(Grenade.class, RepeatingType.SEMIAUTOMATIC, "Grenade", Material.CLAY_BALL, 0, 3, 64, 1, false, 6, 1, Map.of()),
	GRENADEFRAG(GrenadeFrag.class, RepeatingType.SEMIAUTOMATIC, "Grenade Frag", Material.SLIME_BALL, 0, 2, 64, 1, false, 6, 1, Map.of()),
	GRENADEFLAME(GrenadeFlame.class, RepeatingType.SEMIAUTOMATIC, "Grenade Incendiaire", Material.MAGMA_CREAM, 0, 3, 64, 1, false, 6, 1, Map.of()),
	TURRET(Turret.class, RepeatingType.SEMIAUTOMATIC, "Tourelle", Material.GOLD_NUGGET, 0, 1, 64, 465278045, false, 6, 1, Map.of("dmg", 5.0D)),
	AMMO_BOX(AmmoBox.class, RepeatingType.SEMIAUTOMATIC, "Boite de munitions", Material.CAKE, 0, 1, 53, 1, false, 6, 1, Map.of()),
	
	HUNTING_GUN(ZombieHuntingGun.class, RepeatingType.SEMIAUTOMATIC, "Fusil de Chasse", Material.WOODEN_AXE, 10, 1, 60/*, new McSound("guns.mosin", 30)*/, 1, false, -1, 10, Map.of("dmg", 4.6, "range", 40.0D, "accuracy", PI / 90)),
	GRAPPLING_HOOK(GrapplingHook.class, RepeatingType.SEMIAUTOMATIC, "Grappin", Material.FISHING_ROD, 1, 1, 0/*, new McSound("random.bow", 0.5F, 10)*/, 1, false, -1, 10, Map.of("dmg", 0.0D, "range", 5.0D, "accuracy", 0.0)),
	BLAZE_GUN(BlazeGun.class, RepeatingType.BURSTS, "Blaze gun", Material.BLAZE_ROD, 100, 20, 100/*, new McSound("guns.mosin", 30)*/, 1, false, -1, 15, Map.of("dmg", 1.0, "range", 40.0D, "accuracy", PI / 60, "shots", 10, "shotsDelay", 1L)),
	
	BIMBOUMSNIPER(BimBoumSniper.class, RepeatingType.AUTOMATIC, "Bim Boum Sniper", Material.SADDLE, 2000, 40, 15/*, new McSound("guns.bimboumsniper", 500)*/, 1, false, -1, 3, Map.of("dmg", 16.7D, "range", 100.0D, "accuracy", 0.0));
	
	private final Class<? extends Weapon> weapon;
	private final String name;
	private final Material mat;
	private final int maxAmmo;
	private final int clipSize;
	private final int reloadTime;
	private final int price;
	private final Map<String, Object> others;
	private final boolean inMagicBox;
	private final int place;
	private final RepeatingType rt;
	private final int rpm;
	
	WeaponType(Class<? extends Weapon> weapon, RepeatingType rt, String name, Material mat, int maxAmmo, int clipSize, int reloadTime, int price, boolean inMagicBox, int place, int rpm, Map<String, Object> others) {
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
		this.others = others;
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

package fr.lumin0u.survivor.objects;

import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Registry;
import fr.lumin0u.survivor.GameManager;
import fr.lumin0u.survivor.Survivor;
import fr.lumin0u.survivor.player.SvPlayer;
import fr.lumin0u.survivor.utils.ItemBuilder;
import fr.lumin0u.survivor.weapons.Weapon;
import fr.lumin0u.survivor.weapons.superweapons.SuperWeapon;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class AmmoFrameManager implements Listener
{
    public static final Material ITEM_TYPE = Material.NETHERITE_SCRAP;
    
    private ItemFrame itemFrame;
    
    public AmmoFrameManager() {
        Bukkit.getServer().getPluginManager().registerEvents(this, Survivor.getInstance());
    }
    
    public void init(Location l) {
        l.getNearbyEntities(100, 100, 100).stream()
                .filter(ItemFrame.class::isInstance)
                .map(ItemFrame.class::cast)
                .filter(frame -> frame.getItem().getType().equals(ITEM_TYPE))
                .findFirst()
                .ifPresentOrElse(f -> itemFrame = f, () -> Bukkit.broadcastMessage("§cPas de frame de munitions, allez vous plaindre à un membre du staff et au buildeur qui a oublié de la mettre."));
        
        new BukkitRunnable() {
            @Override
            public void run() {
                GameManager.getInstance().getOnlinePlayers().stream()
                        .filter(p -> p.toBukkit().getLocation().distanceSquared(itemFrame.getLocation()) < 25)
                        .forEach(AmmoFrameManager.this::updateItemTo);
            }
        }.runTaskTimer(Survivor.getInstance(), 1, 1);
    }
    
    public int getRefillPrice(Weapon w) {
        return - 5 * (w.getAmmo() - w.getMaxAmmo()) / w.getAmmoBoxRecovery();
    }
    
    public void refillMax(SvPlayer player, Weapon w) {
        int toGive = Math.min((int) (player.getMoney() * w.getAmmoBoxRecovery() / 5), w.getMaxAmmo() - w.getAmmo());
        
        int price = - 5 * (-toGive) / w.getAmmoBoxRecovery();
        
        w.setAmmo(w.getAmmo() + toGive);
        player.addMoney(-price);
        
        updateItemTo(player);
    }
    
    public void updateItemTo(SvPlayer player) {
        String name;
        
        if(player.getWeaponInHand() == null)
            name = "§5MUNITIONS";
        else if(player.getWeaponInHand() instanceof SuperWeapon)
            name = "§cNON RECHARGEABLE";
        else
            name = "§5MUNITIONS §6" + getRefillPrice(player.getWeaponInHand()) + "$";
        
        ItemStack item = new ItemBuilder(ITEM_TYPE)
                .setDisplayName(name)
                .build();
        
        PacketContainer packetName = new PacketContainer(Server.ENTITY_METADATA);
        packetName.getIntegers().write(0, itemFrame.getEntityId());
        packetName.getDataValueCollectionModifier().write(0, List.of(new WrappedDataValue(8, Registry.getItemStackSerializer(false), MinecraftReflection.getMinecraftItemStack(item))));
        player.sendPacket(packetName);
    }
}

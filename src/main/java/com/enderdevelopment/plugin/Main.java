package com.enderdevelopment.plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public final class Main extends JavaPlugin implements Listener {
    private static final long PULL_DURATION_TICKS = 60L;
    private static final double DAMAGE_MULTIPLIER = 1.5;
    private static final double PULL_STRENGTH = 0.35;
    private NamespacedKey spellKey;
    private final Map<UUID, Long> pullExpiry = new HashMap<>();
    private final Map<UUID, UUID> tetherOwners = new HashMap<>();

    @Override
    public void onEnable() {
        spellKey = new NamespacedKey(this, "mace_spell");
        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getScheduler().runTaskTimer(this, this::pullAffectedMobs, 1L, 2L);
    }

    @Override
    public void onDisable() {
        pullExpiry.clear();
        tetherOwners.clear();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("macespell")) {
            return false;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by a player.");
            return true;
        }
        ItemStack mace = new ItemStack(Material.MACE);
        ItemMeta meta = mace.getItemMeta();
        if (meta == null) {
            player.sendMessage(Component.text("Could not create the spell mace.", NamedTextColor.RED));
            return true;
        }
        meta.displayName(Component.text("Mace of the Tether", NamedTextColor.LIGHT_PURPLE));
        meta.lore(java.util.List.of(Component.text("Hits harder, launches mobs, and pulls them to you.", NamedTextColor.GRAY)));
        meta.getPersistentDataContainer().set(spellKey, PersistentDataType.BYTE, (byte) 1);
        mace.setItemMeta(meta);
        player.getInventory().addItem(mace).values().forEach(item -> player.getWorld().dropItemNaturally(player.getLocation(), item));
        player.sendMessage(Component.text("You received the Mace of the Tether.", NamedTextColor.GREEN));
        return true;
    }

    @EventHandler
    public void onMaceHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)
                || !(event.getEntity() instanceof LivingEntity target)
                || target instanceof Player) {
            return;
        }
        ItemStack held = player.getInventory().getItemInMainHand();
        ItemMeta meta = held.getItemMeta();
        if (meta == null || !meta.getPersistentDataContainer().has(spellKey, PersistentDataType.BYTE)) {
            return;
        }
        event.setDamage(event.getDamage() * DAMAGE_MULTIPLIER);
        Vector launch = target.getVelocity().clone();
        launch.setY(Math.max(0.9, launch.getY() + 0.65));
        target.setVelocity(launch);
        UUID targetId = target.getUniqueId();
        pullExpiry.put(targetId, System.currentTimeMillis() + PULL_DURATION_TICKS * 50L);
        tetherOwners.put(targetId, player.getUniqueId());
    }

    private void pullAffectedMobs() {
        long now = System.currentTimeMillis();
        pullExpiry.entrySet().removeIf(entry -> {
            UUID targetId = entry.getKey();
            if (entry.getValue() <= now) {
                tetherOwners.remove(targetId);
                return true;
            }
            Entity entity = Bukkit.getEntity(targetId);
            if (!(entity instanceof LivingEntity target) || !target.isValid() || target.isDead()) {
                tetherOwners.remove(targetId);
                return true;
            }
            Player owner = Bukkit.getPlayer(tetherOwners.get(targetId));
            if (owner == null || !owner.isOnline() || !owner.getWorld().equals(target.getWorld())
                    || owner.getLocation().distanceSquared(target.getLocation()) > 32 * 32) {
                return false;
            }
            Vector towardPlayer = owner.getLocation().toVector().subtract(target.getLocation().toVector());
            if (towardPlayer.lengthSquared() > 0.01) {
                target.setVelocity(target.getVelocity().multiply(0.35).add(towardPlayer.normalize().multiply(PULL_STRENGTH)));
            }
            return false;
        });
    }
}

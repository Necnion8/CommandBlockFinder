package com.gmail.necnionch.myplugin.commandblockfinder.bukkit.wand;

import com.gmail.necnionch.myplugin.commandblockfinder.bukkit.CommandBlockFinderPlugin;
import com.gmail.necnionch.myplugin.commandblockfinder.bukkit.wand.actions.BlockEditAction;
import com.gmail.necnionch.myplugin.commandblockfinder.bukkit.wand.actions.SelectingBlockAction;
import com.gmail.necnionch.myplugin.commandblockfinder.bukkit.wand.actions.WandAction;
import com.google.common.collect.Maps;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PlayerWandManager extends BukkitRunnable implements Listener {

    private final CommandBlockFinderPlugin plugin;
    private final Map<Player, WandInstance> wands = Maps.newHashMap();
    private final LinkedHashMap<Class<? extends WandAction>, WandAction.Creator<?>> actions;

    public PlayerWandManager(CommandBlockFinderPlugin plugin, WandAction.Creator<?>[] actions) {
        this.plugin = plugin;
        this.actions = Stream.of(actions).collect(Collectors.toMap(WandAction.Creator::getActionClass, c -> c, (a, b) -> b, LinkedHashMap::new));
    }


    @EventHandler
    public void onItemSpawn(ItemSpawnEvent event) {
        if (plugin.isWandItemStack(event.getEntity().getItemStack())) {
            event.setCancelled(true);
            event.getEntity().getWorld().playSound(event.getLocation(), Sound.ENTITY_BLAZE_HURT, SoundCategory.BLOCKS, 0.8f, 2f);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent event) {
        if (!EquipmentSlot.HAND.equals(event.getHand()))
            return;

        Player player = event.getPlayer();
        if (!plugin.isWandItemStack(player.getInventory().getItemInMainHand()))
            return;

        if (player.hasPermission(CommandBlockFinderPlugin.WAND_PERMISSION)) {
            if (Action.RIGHT_CLICK_BLOCK.equals(event.getAction())) {
                if (!player.isSneaking())
                    return;
                if (wands.containsKey(player)) {
                    WandInstance wand = wands.get(player);
                    WandAction action = wand.getCurrent();
                    WandAction newAction = wand.nextAction();
                    if (!action.equals(newAction)) {
                        sendActionDisplay(player, newAction);
                        player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, SoundCategory.BLOCKS, 1, 2);
                    } else if (action.available()) {
                        player.playSound(player.getLocation(), Sound.BLOCK_STONE_PLACE, SoundCategory.BLOCKS, 0.5f, 2);
                    }
                }

            } else if (Action.LEFT_CLICK_BLOCK.equals(event.getAction())) {
                if (wands.containsKey(player)) {
                    wands.get(player).getCurrent().onClick(event.getClickedBlock());
                }
            }

        } else {  // no perm
            if (Action.RIGHT_CLICK_BLOCK.equals(event.getAction()) && !player.isSneaking())
                return;
        }
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onSwitch(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();

        if (!player.hasPermission(CommandBlockFinderPlugin.WAND_PERMISSION))
            return;

        if (plugin.isWandItemStack(player.getInventory().getItemInMainHand())) {
            if (player.isSneaking()) {
                event.setCancelled(true);

                if (wands.containsKey(player)) {
                    WandAction action = wands.get(player).getCurrent();
                    if (action.available()) {
                        if (event.getPreviousSlot() < event.getNewSlot() || (event.getPreviousSlot() == 8 && event.getNewSlot() == 0)) {
                            action.onNext();
                        } else {
                            action.onBack();
                        }
                        sendActionDisplay(player, action);
                    }
                }
            } else {
                // clean actionbar
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR);
            }
        }

    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        wands.remove(event.getPlayer());
    }


    public void sendActionDisplay(Player player, WandAction action) {
        if (!action.available()) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR);
            return;
        }

        BaseComponent[] display = action.getDisplay();
        if (display == null || display.length <= 0) {
            ComponentBuilder b = new ComponentBuilder("");

            b.append("[").color(ChatColor.RED);
            display = action.getBackDisplay();
            if (display != null && 0 < display.length ) {
                b.append("", ComponentBuilder.FormatRetention.NONE).color(ChatColor.AQUA).bold(true);
                b.append(display);
            } else {
                b.append(" ______ ").color(ChatColor.GRAY);
            }
            b.append("]", ComponentBuilder.FormatRetention.NONE).color(ChatColor.RED);

            b.append("  <<   ").color(ChatColor.WHITE);
            b.append("[").color(ChatColor.GOLD);
            display = action.getCurrentDisplay();
            if (display != null && 0 < display.length) {
                b.append(display, ComponentBuilder.FormatRetention.NONE);
            } else {
                b.append(" ______ ").color(ChatColor.GRAY);
            }
            b.append("]", ComponentBuilder.FormatRetention.NONE).color(ChatColor.GOLD);

            b.append("   >>  ").color(ChatColor.WHITE);
            b.append("[").color(ChatColor.RED);
            display = action.getNextDisplay();
            if (display != null && 0 < display.length) {
                b.append("", ComponentBuilder.FormatRetention.NONE).color(ChatColor.AQUA).bold(true);
                b.append(display);
            } else {
                b.append(" ______ ").color(ChatColor.GRAY);
            }
            b.append("]", ComponentBuilder.FormatRetention.NONE).color(ChatColor.RED);

            display = b.create();
        }

        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, display);
    }

    public WandAction selectWandAction(Player player, Class<? extends WandAction> actionClass, boolean force) throws IllegalArgumentException {
        return getWandInstance(player).selectAction(actionClass, force);
    }

    public <A extends WandAction> A getWandAction(Player player, Class<A> actionClass) throws IllegalArgumentException {
        return getWandInstance(player).getAction(actionClass);
    }

    public WandInstance getWandInstance(Player player) {
        WandInstance wand;
        if (!wands.containsKey(player)) {
            List<WandAction> actions = this.actions.values().stream().map(c -> c.create(player)).collect(Collectors.toList());
            wand = new WandInstance(this, player, Collections.unmodifiableList(actions));
            wands.put(player, wand);
        } else {
            wand = wands.get(player);
        }
        return wand;
    }


    @Override
    public void run() {
        Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.hasPermission(CommandBlockFinderPlugin.WAND_PERMISSION))
                .filter(p -> plugin.isWandItemStack(p.getInventory().getItemInMainHand()))
                .forEach(p -> {
                    try {
                        WandInstance wand = getWandInstance(p);
                        wand.tick();
                        sendActionDisplay(p, wand.getCurrent());
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                });
    }

}

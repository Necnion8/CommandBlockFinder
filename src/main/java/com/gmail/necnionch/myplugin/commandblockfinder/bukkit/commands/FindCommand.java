package com.gmail.necnionch.myplugin.commandblockfinder.bukkit.commands;

import com.gmail.necnionch.myplugin.commandblockfinder.bukkit.CommandBlockFinderPlugin;
import com.gmail.necnionch.myplugin.commandblockfinder.bukkit.finder.CommandBlockFinder;
import com.gmail.necnionch.myplugin.commandblockfinder.bukkit.finder.FindParam;
import com.gmail.necnionch.myplugin.commandblockfinder.bukkit.finder.FindResult;
import com.gmail.necnionch.myplugin.commandblockfinder.bukkit.wand.PlayerWandManager;
import com.gmail.necnionch.myplugin.commandblockfinder.bukkit.wand.WandInstance;
import com.gmail.necnionch.myplugin.commandblockfinder.bukkit.wand.actions.SelectingBlockAction;
import com.gmail.necnionch.myplugin.commandblockfinder.bukkit.wand.actions.WandAction;
import com.google.common.collect.Lists;
import org.bukkit.*;
import org.bukkit.block.BlockState;
import org.bukkit.block.CommandBlock;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FindCommand implements CommandExecutor, TabExecutor {

    private final CommandBlockFinderPlugin plugin;
    private final CommandBlockFinder finder;
    private final PlayerWandManager wandManager;

    public FindCommand(CommandBlockFinderPlugin plugin, CommandBlockFinder finder, PlayerWandManager wandManager) {
        this.plugin = plugin;
        this.finder = finder;
        this.wandManager = wandManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;

        FindParam param = FindParam.parseString(String.join(" ", args));
        if (!param.allWorld() && param.world() == null)
            param.world(player.getWorld().getName());

        FindResult result = finder.search(player, param, player.getLocation());

        if (!result.getResults().isEmpty()) {
            player.sendMessage(ChatColor.AQUA + "コマンドブロックが " + result.getResults().size() + "件 見つかりました");
            result.getResults().stream()
                    .min(Comparator.comparingLong(cb -> (long) cb.getLocation().distance(player.getLocation())))
                    .ifPresent(cb -> {
                        int index = result.getResults().indexOf(cb);
                        WandInstance wand = wandManager.getWandInstance(player);

                        wand.getAction(SelectingBlockAction.class).setIndex(Math.max(0, index));
                        if (wand.getCurrent() instanceof SelectingBlockAction) {
                            ((SelectingBlockAction) wand.getCurrent()).changeIndex(cb);
                        } else {
                            wandManager.selectWandAction(player, SelectingBlockAction.class, true);
                        }
                    });

            PlayerInventory inv = player.getInventory();
            if (!plugin.isWandItemStack(inv.getItemInMainHand())) {
                boolean wandSelected = false;
                for (int i = 0; i < inv.getSize(); i++) {
                    ItemStack item = inv.getItem(i);
                    if (!plugin.isWandItemStack(item))
                        continue;

                    if (i < 9) {
                        inv.setHeldItemSlot(i);
                        wandSelected = true;
                        break;
                    }

                    ItemStack hand = inv.getItemInMainHand();
                    inv.setItem(i, hand);
                    inv.setItemInMainHand(item);
                    wandSelected = true;
                    break;
                }
                if (!wandSelected) {
                    ItemStack inHand = player.getInventory().getItemInMainHand();

                    if (Material.AIR.equals(inHand.getType())) {
                        player.getInventory().setItemInMainHand(plugin.createWandItemStack());

                    } else if (!plugin.isWandItemStack(inHand)) {
                        player.getInventory().addItem(inHand);
                        player.getInventory().setItemInMainHand(plugin.createWandItemStack());
                    }
                }
            }

        } else {
            player.sendMessage(ChatColor.RED + "コマンドブロックが見つかりませんでした");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player))
            return Collections.emptyList();

        String world = null;
        Integer radius = null;

        for (int i = 0; i < args.length; i++) {
            boolean end = i + 1 >= args.length;
            String s = args[i];

            if (s.startsWith("w:") && world == null) {
                if (2 < s.length())
                    world = s.substring(2);
                if (end) {
                    List<String> names = Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.toList());
                    names.add("all");
                    return names.stream()
                            .map(e -> "w:" + e)
                            .filter(e -> e.toLowerCase(Locale.ROOT).startsWith(s.toLowerCase(Locale.ROOT)))
                            .collect(Collectors.toList());
                }
            } else if (s.startsWith("r:") && radius == null) {
                try {
                    if (2 < s.length()) {
                        radius = Integer.parseInt(s.substring(2));
                    }
                } catch (NumberFormatException ignored) {
                }
                if (end) {
                    String a = Optional.ofNullable(radius).map(String::valueOf).orElse("");
                    return Stream.of("0", "1", "2", "3", "4", "5", "6", "7", "8", "9")
                            .map(e -> "r:" + a + e)
                            .collect(Collectors.toList());
                }
            } else if (end) {
                List<String> cmd = Lists.newArrayList();
                if (world == null)
                    cmd.add("w:");
                if (radius == null)
                    cmd.add("r:");
                return cmd.stream()
                        .filter(e -> e.toLowerCase(Locale.ROOT).startsWith(s.toLowerCase(Locale.ROOT)))
                        .collect(Collectors.toList());
            }
        }
        return Collections.emptyList();
    }

}

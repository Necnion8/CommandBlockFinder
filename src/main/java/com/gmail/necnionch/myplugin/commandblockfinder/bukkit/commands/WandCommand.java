package com.gmail.necnionch.myplugin.commandblockfinder.bukkit.commands;

import com.gmail.necnionch.myplugin.commandblockfinder.bukkit.CommandBlockFinderPlugin;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class WandCommand implements CommandExecutor, TabExecutor {

    private final CommandBlockFinderPlugin plugin;

    public WandCommand(CommandBlockFinderPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            ItemStack inHand = player.getInventory().getItemInMainHand();

            if (Material.AIR.equals(inHand.getType())) {
                player.getInventory().setItemInMainHand(plugin.createWandItemStack());

            } else if (!plugin.isWandItemStack(inHand)) {
                player.getInventory().addItem(inHand);
                player.getInventory().setItemInMainHand(plugin.createWandItemStack());
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        return Collections.emptyList();
    }

}

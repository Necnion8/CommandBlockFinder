package com.gmail.necnionch.myplugin.commandblockfinder.bukkit;

import com.gmail.necnionch.myplugin.commandblockfinder.bukkit.commands.FindCommand;
import com.gmail.necnionch.myplugin.commandblockfinder.bukkit.commands.SetCommand;
import com.gmail.necnionch.myplugin.commandblockfinder.bukkit.commands.WandCommand;
import com.gmail.necnionch.myplugin.commandblockfinder.bukkit.finder.CommandBlockFinder;
import com.gmail.necnionch.myplugin.commandblockfinder.bukkit.wand.PlayerWandManager;
import com.gmail.necnionch.myplugin.commandblockfinder.bukkit.wand.actions.BlockEditAction;
import com.gmail.necnionch.myplugin.commandblockfinder.bukkit.wand.actions.SelectingBlockAction;
import com.gmail.necnionch.myplugin.commandblockfinder.bukkit.wand.actions.WandAction;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.Permission;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class CommandBlockFinderPlugin extends JavaPlugin {
    public static final Permission WAND_PERMISSION = new Permission("commandblockfinder.admin");
    public static final Set<Material> TRANSPARENT_TYPES = Collections.unmodifiableSet(Stream.of(Material.values())
            .filter(Material::isBlock)
            .filter(m -> !m.isOccluding())
            .collect(Collectors.toSet()));

    private final NamespacedKey itemKey = new NamespacedKey(this, "item");
    private final CommandBlockFinder finder = new CommandBlockFinder(this);
    private final PlayerWandManager wandManager = new PlayerWandManager(this, new WandAction.Creator[]{
            new BlockEditAction.Creator(),
            new SelectingBlockAction.Creator(finder),
    });

    @Override
    public void onEnable() {
        Optional.ofNullable(getCommand("wandcommandblock"))
                .ifPresent(cmd -> cmd.setExecutor(new WandCommand(this)));
        Optional.ofNullable(getCommand("findcommandblock"))
                .ifPresent(cmd -> cmd.setExecutor(new FindCommand(this, finder, wandManager)));
        Optional.ofNullable(getCommand("setcommandblock"))
                .ifPresent(cmd -> cmd.setExecutor(new SetCommand(this, wandManager)));

        finder.init();
        getServer().getPluginManager().registerEvents(wandManager, this);
        wandManager.runTaskTimer(this, 0, 1);
    }

    @Override
    public void onDisable() {
        finder.cleanup();
        wandManager.cancel();
    }


    public ItemStack createWandItemStack() {
        ItemStack itemStack = new ItemStack(Material.BLAZE_ROD);
        ItemMeta itemMeta = Objects.requireNonNull(itemStack.getItemMeta());

        itemMeta.setDisplayName(ChatColor.GOLD + "コマンドブロック杖");
        PersistentDataContainer data = itemMeta.getPersistentDataContainer();
        data.set(itemKey, PersistentDataType.STRING, "wand");

        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public boolean isWandItemStack(@Nullable ItemStack itemStack) {
            return Optional.ofNullable(itemStack)
                    .map(ItemStack::getItemMeta)
                    .map(PersistentDataHolder::getPersistentDataContainer)
                    .map(data -> "wand".equalsIgnoreCase(data.get(itemKey, PersistentDataType.STRING)))
                    .orElse(false);
    }


    public CommandBlockFinder getFinderManager() {
        return finder;
    }

    public PlayerWandManager getWandManager() {
        return wandManager;
    }

}

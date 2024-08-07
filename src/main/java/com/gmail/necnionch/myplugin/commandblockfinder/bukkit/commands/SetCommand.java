package com.gmail.necnionch.myplugin.commandblockfinder.bukkit.commands;

import com.gmail.necnionch.myplugin.commandblockfinder.bukkit.CommandBlockFinderPlugin;
import com.gmail.necnionch.myplugin.commandblockfinder.bukkit.wand.PlayerWandManager;
import com.gmail.necnionch.myplugin.commandblockfinder.bukkit.wand.WandInstance;
import com.gmail.necnionch.myplugin.commandblockfinder.bukkit.wand.actions.SelectingBlockAction;
import com.google.common.collect.Maps;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.CommandBlock;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class SetCommand implements CommandExecutor, TabExecutor {

    private final CommandBlockFinderPlugin plugin;
    private final PlayerWandManager wandManager;
    private final Map<Player, Listener> playerListeners = Maps.newHashMap();

    public SetCommand(CommandBlockFinderPlugin plugin, PlayerWandManager wandManager) {
        this.plugin = plugin;
        this.wandManager = wandManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            WandInstance wand = wandManager.getWandInstance(player);

            Location cbLocation = Optional.ofNullable(wand.getCurrent().getSelectedCommandBlock())
                    .map(BlockState::getLocation)
                    .orElse(null);

            String cbCommand = Optional.ofNullable(cbLocation)
                    .map(Location::getBlock)
                    .filter(b -> b.getState() instanceof CommandBlock)
                    .map(b -> (CommandBlock) b.getState())
                    .map(CommandBlock::getCommand)
                    .orElse(null);

            if (cbCommand != null && !playerListeners.containsKey(player)) {
                if (!cbCommand.startsWith("/"))
                    cbCommand = "/" + cbCommand;

                player.spigot().sendMessage(new ComponentBuilder("")
                        .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, cbCommand))
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("クリックで現在のコマンドを補完").create()))
                        .append("コマンドをチャット欄に入力してください ").color(net.md_5.bungee.api.ChatColor.DARK_AQUA)
                        .append("( / で終了)").color(net.md_5.bungee.api.ChatColor.GRAY)
                        .create());

                new ChatListener(player, (text) -> {
                    CommandBlock cb = (CommandBlock) Optional.of(cbLocation.getBlock().getState())
                            .filter(s -> s instanceof CommandBlock)
                            .orElse(null);

                    if (cb != null && text != null && 1 < text.length()) {
                        cb.setCommand(text.substring(1));
                        cb.update(true);
                        try {
                            if (wand.getCurrent() instanceof SelectingBlockAction) {
                                ((SelectingBlockAction) wand.getCurrent()).sendCommandBlockInfo(cb.getLocation(), cb);
                            }
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                        player.sendMessage(ChatColor.DARK_AQUA + "コマンドを設定しました");
                    } else {
                        player.sendMessage(ChatColor.DARK_AQUA + "中止しました");
                    }
                });
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        return Collections.emptyList();
    }


    public class ChatListener implements Listener {

        private final Player player;
        private final Consumer<String> onText;

        public ChatListener(Player player, Consumer<String> onText) {
            this.player = player;
            this.onText = onText;
            playerListeners.put(player, this);
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
        }


        @EventHandler(priority = EventPriority.LOWEST)
        public void onPreprocess(PlayerCommandPreprocessEvent event) {
            if (event.getPlayer().equals(player)) {
                if (null == plugin.getServer().getPluginCommand(event.getMessage().substring(1).split(" ", 2)[0])) {
                    onText.accept(event.getMessage());
                    event.setCancelled(true);
                } else {
                    onText.accept(null);
                }
                HandlerList.unregisterAll(this);
                playerListeners.remove(player, this);
            }
        }

        @EventHandler
        public void onQuit(PlayerQuitEvent event) {
            onText.accept(null);
            HandlerList.unregisterAll(this);
            playerListeners.remove(player, this);
        }

    }

}

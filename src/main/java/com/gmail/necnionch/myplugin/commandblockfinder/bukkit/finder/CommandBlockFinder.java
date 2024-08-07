package com.gmail.necnionch.myplugin.commandblockfinder.bukkit.finder;

import com.gmail.necnionch.myplugin.commandblockfinder.bukkit.CommandBlockFinderPlugin;
import com.google.common.collect.Maps;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.CommandBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommandBlockFinder implements Listener {

    private final CommandBlockFinderPlugin plugin;
//    private final Map<CommandBlock, String> cachedBlocks = Maps.newHashMap();
    private final Map<Player, FindResult> lastResult = Maps.newHashMap();
    private BukkitTask cleanupTimer;

    public CommandBlockFinder(CommandBlockFinderPlugin plugin) {
        this.plugin = plugin;
    }

    public CommandBlockFinderPlugin getPlugin() {
        return plugin;
    }

    public void init() {
        clearCaches();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void cleanup() {
        clearCaches();
        HandlerList.unregisterAll(this);
    }

    public void used() {
        if (cleanupTimer != null)
            cleanupTimer.cancel();
        cleanupTimer = plugin.getServer().getScheduler().runTaskLater(plugin, this::clearCaches, 5 * 60 * 20);
    }

    public void clearCaches() {
        if (cleanupTimer != null)
            cleanupTimer.cancel();
        lastResult.clear();
    }


    public FindResult search(Player player, FindParam param, @Nullable Location location) {
        FindResult result = search(param, location);
        if (!result.getResults().isEmpty()) {
            lastResult.put(player, result);
            used();
        }
        return result;
    }

    public FindResult search(FindParam param, @Nullable Location location) {
        Stream<World> stream;
        if (param.allWorld()) {
            stream = Bukkit.getWorlds().stream();
        } else if (param.world() != null) {
            stream = Optional.ofNullable(Bukkit.getWorld(param.world()))
                    .map(Stream::of)
                    .orElseGet(Stream::of);
        } else {
            return FindResult.empty(this);
        }

        Stream<CommandBlock> cbStream = stream.flatMap(world -> Stream.of(world.getLoadedChunks()))
                .flatMap(chunk -> Stream.of(chunk.getTileEntities()))
                .filter(state -> state instanceof CommandBlock)
                .map(state -> (CommandBlock) state);

        // check radius
        if (param.radius() != null && location != null) {
            cbStream = cbStream
                    .filter(cb -> cb.getWorld().equals(location.getWorld()))
                    .filter(cb -> location.distance(cb.getLocation()) <= param.radius());
        }

        if (param.regex() != null) {
            cbStream = cbStream.filter(cb -> param.regex().matcher(cb.getCommand()).find());
        }

        return new FindResult(this, param.copy(), Collections.unmodifiableList(cbStream.collect(Collectors.toList())));
    }

    public FindResult search(FindParam param) {
        return search(param, null);
    }


    public Map<Player, FindResult> lastResults() {
        return lastResult;
    }


    @EventHandler
    public void onUnloadWorld(WorldUnloadEvent event) {
        lastResult.keySet().removeIf(cb -> event.getWorld().equals(cb.getWorld()));
    }

    @EventHandler
    public void onBreakBlock(BlockBreakEvent event) {
        lastResult.keySet().removeIf(cb -> event.getBlock().getState().equals(cb));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        lastResult.remove(event.getPlayer());
    }

}

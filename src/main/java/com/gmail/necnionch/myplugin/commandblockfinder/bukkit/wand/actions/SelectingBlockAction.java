package com.gmail.necnionch.myplugin.commandblockfinder.bukkit.wand.actions;

import com.gmail.necnionch.myplugin.commandblockfinder.bukkit.finder.CommandBlockFinder;
import com.gmail.necnionch.myplugin.commandblockfinder.bukkit.finder.FindResult;
import com.gmail.necnionch.myplugin.commandblockfinder.bukkit.util.CommandBlockType;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CommandBlock;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static com.gmail.necnionch.myplugin.commandblockfinder.bukkit.CommandBlockFinderPlugin.TRANSPARENT_TYPES;

public class SelectingBlockAction implements WandAction {

    private final CommandBlockFinder finder;
    private final Player player;
    private int index;

    public SelectingBlockAction(Player player, CommandBlockFinder finder) {
        this.player = player;
        this.finder = finder;
    }

    public @Nullable FindResult getLastResult() {
        return finder.lastResults().get(player);
    }

    public void changeIndex(BlockState blockState) {
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, SoundCategory.BLOCKS, 1, 2);
        particleLast = false;

        Location cbLocation = blockState.getLocation();
        Bukkit.getScheduler().runTaskLater(finder.getPlugin(), () -> {
            player.sendBlockChange(cbLocation, Material.WHITE_CONCRETE.createBlockData());
        }, 1);
        Bukkit.getScheduler().runTaskLater(finder.getPlugin(), () -> {
            player.sendBlockChange(cbLocation, blockState.getBlockData());
        }, 2);

        sendCommandBlockInfo(cbLocation, (blockState instanceof CommandBlock) ? (CommandBlock) blockState : null);
    }

    public void changeIndex(Location blockLocation) {
        changeIndex(blockLocation.getBlock().getState());
    }

    public void sendCommandBlockInfo(Location location, @Nullable CommandBlock commandBlock) {

        String tpWorld = Objects.requireNonNull(location.getWorld()).getName();
        if (tpWorld.equals(Bukkit.getWorlds().get(0).getName()))
            tpWorld = "overworld";

        String teleportCommand = String.format("/execute in %s run tp %d %d %d", tpWorld, location.getBlockX(), location.getBlockY(), location.getBlockZ());
        player.sendMessage("");

        if (commandBlock != null) {
            org.bukkit.block.data.type.CommandBlock cbData = (org.bukkit.block.data.type.CommandBlock) commandBlock.getBlockData();
            player.spigot().sendMessage(new ComponentBuilder("")
                    .append("位置: ").color(ChatColor.WHITE)
                    .append(String.format("%d %d %d (%s)", location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getWorld().getName()))
                    .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, teleportCommand))
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("クリックでテレポート").create()))
                    .create());
            player.spigot().sendMessage(new ComponentBuilder("")
                    .append("モード: ").color(ChatColor.WHITE)
                    .append(Objects.requireNonNull(CommandBlockType.fromMaterial(commandBlock.getType())).getDisplayName()).color(ChatColor.GOLD)
                    .append(" | ", ComponentBuilder.FormatRetention.NONE).color(ChatColor.WHITE)
                    .append((cbData.isConditional()) ? "条件付き" : "無条件").color(ChatColor.YELLOW)
                    .append(" | ").color(ChatColor.WHITE)
                    .create());
            player.spigot().sendMessage(new ComponentBuilder("")
                    .append("コマンド(#" + (index + 1) + "): ").color(ChatColor.WHITE)
                    .append(commandBlock.getCommand()).color(ChatColor.YELLOW)
                    .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/setcommandblock"))
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("クリックでコマンドを変更").create()))
                    .create());
        } else {
            player.spigot().sendMessage(new ComponentBuilder("")
                    .append("位置: ").color(ChatColor.WHITE)
                    .append(String.format("%d %d %d (%s)", location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getWorld().getName()))
                    .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, teleportCommand))
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("クリックでテレポート").create()))
                    .create());
            player.spigot().sendMessage(new ComponentBuilder("")
                    .append("モード: ").color(ChatColor.WHITE)
                    .create());
            player.spigot().sendMessage(new ComponentBuilder("")
                    .append("コマンド(#" + (index + 1) + "): ").color(ChatColor.WHITE)
                    .create());
        }
    }


    @Override
    public void onBack() {
        FindResult lastResult = getLastResult();
        if (lastResult == null || 0 >= index)
            return;

        changeIndex(lastResult.getResultLocations().get(--index));
    }

    @Override
    public void onNext() {
        FindResult lastResult = getLastResult();
        if (lastResult == null || index+1 >= lastResult.getResultLocations().size())
            return;

        changeIndex(lastResult.getResultLocations().get(++index));
    }

    @Override
    public void onClick(Block block) {
        if (!(block.getState() instanceof CommandBlock))
            return;
        CommandBlock target = (CommandBlock) block.getState();

        Optional.ofNullable(getLastResult()).ifPresent(res -> {
            int index = res.getResultLocations().indexOf(target.getLocation());
            if (index != -1 && this.index != index) {
                this.index = index;
                changeIndex(res.getResultLocations().get(index));
            }
        });
    }

    @Override
    public void onSelect(boolean current) {
        if (current) {
            Optional.ofNullable(getLastResult())
                    .ifPresent(res -> changeIndex(res.getResultLocations().get(index)));
        }
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public @Nullable BaseComponent[] getBackDisplay() {
        FindResult lastResult = getLastResult();
        if (lastResult == null)
            return null;

        return new ComponentBuilder("前エントリ")
                .color(0 < index ? ChatColor.AQUA : ChatColor.GRAY)
                .create();
    }

    @Override
    public @Nullable BaseComponent[] getNextDisplay() {
        FindResult lastResult = getLastResult();
        if (lastResult == null)
            return null;

        return new ComponentBuilder("次エントリ")
                .color(index+1 < lastResult.getResultLocations().size() ? ChatColor.AQUA : ChatColor.GRAY)
                .create();
    }

    @Override
    public @Nullable BaseComponent[] getCurrentDisplay() {
        FindResult lastResult = getLastResult();
        if (lastResult == null)
            return new ComponentBuilder("検索結果 -   ?  ").color(ChatColor.YELLOW).create();

        return new ComponentBuilder("検索結果 ").color(ChatColor.GOLD)
                .append("(").color(ChatColor.WHITE)
                .append((index + 1) + "/" + lastResult.getResultLocations().size()).color(ChatColor.YELLOW)
                .append(")").color(ChatColor.WHITE)
                .create();
    }

    @Override
    public @Nullable CommandBlock getSelectedCommandBlock() {
        return (CommandBlock) Optional.ofNullable(getLastResult())
                .map(res -> res.getResultLocations().get(index))
                .map(loc -> loc.getBlock().getState())
                .filter(s -> s instanceof CommandBlock)
                .orElse(null);
    }

    @Override
    public boolean available() {
        return finder.lastResults().containsKey(player);
    }


    private int particleTick = 0;
    private boolean particleLast;

    @Override
    public void tick() {
        Optional.ofNullable(getLastResult()).ifPresent(res -> {
            Location loc = res.getResultLocations().get(index);
            if (player.getWorld().equals(loc.getWorld())) {
                if (!particleLast) {
                    particleTick = -5;
                    particleLast = true;
                }

                Particle particle = Particle.REDSTONE;
                if (particleTick < 0) {
                    Particle.DustOptions options = new Particle.DustOptions(Color.WHITE, .2f);
                    float v = 1/ 31f;
                    for (int i = 0; i <= 31; i++) {
                        player.spawnParticle(particle, loc.clone().add(i * v, 1f, 0f), 1, 0, 0, 0, 0, options);
                        player.spawnParticle(particle, loc.clone().add(1 - (i * v), 1f, 1f), 1, 0, 0, 0, 0, options);
                        player.spawnParticle(particle, loc.clone().add(1f, 1f, i * v), 1, 0, 0, 0, 0, options);
                        player.spawnParticle(particle, loc.clone().add(0f, 1f, 1 - (i * v)), 1, 0, 0, 0, 0, options);

                        player.spawnParticle(particle, loc.clone().add(1 - (i * v), 0f, 0f), 1, 0, 0, 0, 0, options);
                        player.spawnParticle(particle, loc.clone().add(i * v, 0f, 1f), 1, 0, 0, 0, 0, options);
                        player.spawnParticle(particle, loc.clone().add(1f, 0f, 1 - (i * v)), 1, 0, 0, 0, 0, options);
                        player.spawnParticle(particle, loc.clone().add(0f, 0f, i * v), 1, 0, 0, 0, 0, options);
//
                        player.spawnParticle(particle, loc.clone().add(0f, 1 - (i * v), 0f), 1, 0, 0, 0, 0, options);
                        player.spawnParticle(particle, loc.clone().add(1f, i * v, 0f), 1, 0, 0, 0, 0, options);
                        player.spawnParticle(particle, loc.clone().add(1f, 1 - (i * v), 1f), 1, 0, 0, 0, 0, options);
                        player.spawnParticle(particle, loc.clone().add(0f, i * v, 1f), 1, 0, 0, 0, 0, options);
                    }

                } else {
                    int i = particleTick;
                    float v = 1 / 15f;
                    Particle.DustOptions options = new Particle.DustOptions(Color.WHITE, .3f);
                    player.spawnParticle(particle, loc.clone().add(i * v, 1f, 0f), 1, 0, 0, 0, 0, options);
                    player.spawnParticle(particle, loc.clone().add(1 - (i * v), 1f, 1f), 1, 0, 0, 0, 0, options);
                    player.spawnParticle(particle, loc.clone().add(1f, 1f, i * v), 1, 0, 0, 0, 0, options);
                    player.spawnParticle(particle, loc.clone().add(0f, 1f, 1 - (i * v)), 1, 0, 0, 0, 0, options);

                    player.spawnParticle(particle, loc.clone().add(1 - (i * v), 0f, 0f), 1, 0, 0, 0, 0, options);
                    player.spawnParticle(particle, loc.clone().add(i * v, 0f, 1f), 1, 0, 0, 0, 0, options);
                    player.spawnParticle(particle, loc.clone().add(1f, 0f, 1 - (i * v)), 1, 0, 0, 0, 0, options);
                    player.spawnParticle(particle, loc.clone().add(0f, 0f, i * v), 1, 0, 0, 0, 0, options);
//
                    player.spawnParticle(particle, loc.clone().add(0f, 1 - (i * v), 0f), 1, 0, 0, 0, 0, options);
                    player.spawnParticle(particle, loc.clone().add(1f, i * v, 0f), 1, 0, 0, 0, 0, options);
                    player.spawnParticle(particle, loc.clone().add(1f, 1 - (i * v), 1f), 1, 0, 0, 0, 0, options);
                    player.spawnParticle(particle, loc.clone().add(0f, i * v, 1f), 1, 0, 0, 0, 0, options);
                }

                particleTick++;
                if (particleTick > 15)
                    particleTick = 0;
            }
        });
    }

    public static class Creator implements WandAction.Creator<SelectingBlockAction> {

        private final CommandBlockFinder finder;

        public Creator(CommandBlockFinder finder) {
            this.finder = finder;
        }

        @Override
        public Class<SelectingBlockAction> getActionClass() {
            return SelectingBlockAction.class;
        }

        @Override
        public SelectingBlockAction create(Player player) {
            return new SelectingBlockAction(player, finder);
        }
    }
}

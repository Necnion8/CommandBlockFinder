package com.gmail.necnionch.myplugin.commandblockfinder.bukkit.wand.actions;

import com.gmail.necnionch.myplugin.commandblockfinder.bukkit.util.CommandBlockType;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CommandBlock;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.gmail.necnionch.myplugin.commandblockfinder.bukkit.CommandBlockFinderPlugin.TRANSPARENT_TYPES;

public class BlockEditAction implements WandAction {
    private static final List<CommandBlockType> TYPES = Arrays.asList(CommandBlockType.values());

    private final Player player;

    public BlockEditAction(Player player) {
        this.player = player;
    }

    public @Nullable CommandBlock getTargetCommandBlock() {
        BlockState state = player.getTargetBlock(TRANSPARENT_TYPES, 4).getState();
        if (state instanceof CommandBlock)
            return ((CommandBlock) state);
        return null;
    }

    public @Nullable CommandBlockType getType() {
        return Optional.ofNullable(getTargetCommandBlock())
                .map(cb -> CommandBlockType.fromMaterial(cb.getType()))
                .orElse(null);
    }

    public void setType(CommandBlockType type) {
        Optional.ofNullable(getTargetCommandBlock())
                .ifPresent(cb -> {
                    BlockData blockData = cb.getBlockData();

                    cb.setType(type.getMaterial());
                    BlockData newBlockData = cb.getBlockData();

                    if (blockData instanceof org.bukkit.block.data.type.CommandBlock) {
                        org.bukkit.block.data.type.CommandBlock cbData = (org.bukkit.block.data.type.CommandBlock) blockData;
                        org.bukkit.block.data.type.CommandBlock newCBData = (org.bukkit.block.data.type.CommandBlock) newBlockData;
                        newCBData.setFacing(cbData.getFacing());
                        newCBData.setConditional(cbData.isConditional());
                    }
                    cb.setBlockData(newBlockData);
                    cb.update(true);

                });
    }

    private CommandBlockType getNextType() {
        CommandBlockType type = getType();
        int index = (type != null) ? TYPES.indexOf(type) + 1 : 0;
        try {
            return TYPES.get(index);
        } catch (IndexOutOfBoundsException e) {
            return TYPES.get(0);
        }
    }

    private CommandBlockType getBackType() {
        CommandBlockType type = getType();
        int index = (type != null) ? TYPES.indexOf(type) - 1 : 0;
        try {
            return TYPES.get(index);
        } catch (IndexOutOfBoundsException e) {
            return TYPES.get(TYPES.size() - 1);
        }
    }


    @Override
    public void onBack() {
        setType(getBackType());
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, SoundCategory.BLOCKS, 1, 2);
    }

    @Override
    public void onNext() {
        setType(getNextType());
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, SoundCategory.BLOCKS, 1, 2);
    }

    @Override
    public void onClick(Block block) {
    }

    @Override
    public void onSelect(boolean current) {
    }

    @Override
    public @Nullable CommandBlock getSelectedCommandBlock() {
        return getTargetCommandBlock();
    }

    @Override
    public boolean available() {
        return player.getTargetBlock(TRANSPARENT_TYPES, 4).getState() instanceof CommandBlock;
    }

    @Override
    public @Nullable BaseComponent[] getBackDisplay() {
        return new BaseComponent[] { getBackType().getDisplayName() };
    }

    @Override
    public @Nullable BaseComponent[] getNextDisplay() {
        return new BaseComponent[] { getNextType().getDisplayName() };
    }

    @Override
    public @Nullable BaseComponent[] getCurrentDisplay() {
        CommandBlockType type = getType();
        if (type == null)
            return new BaseComponent[0];

        return new ComponentBuilder("モード切り替え").color(ChatColor.GOLD).create();
    }


    public static class Creator implements WandAction.Creator<WandAction> {

        @Override
        public Class<WandAction> getActionClass() {
            return WandAction.class;
        }

        @Override
        public WandAction create(Player player) {
//            BlockState state = player.getTargetBlock(null, 4).getState();
//            if (state instanceof CommandBlock)
//                return new BlockEditAction(player, ((CommandBlock) state));
//            throw new IllegalStateException("Not looking to CommandBlock");
            return new BlockEditAction(player);
        }

    }
}

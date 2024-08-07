package com.gmail.necnionch.myplugin.commandblockfinder.bukkit.util;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;

public enum CommandBlockType {
    DEFAULT(Material.COMMAND_BLOCK, "インパルス"),
    CHAIN(Material.CHAIN_COMMAND_BLOCK, "チェーン"),
    REPEATING(Material.REPEATING_COMMAND_BLOCK, "リピート"),
    ;

    private final BaseComponent displayName;
    private final Material blockType;
    CommandBlockType(Material blockType, String displayName) {
        this.blockType = blockType;
        this.displayName = new TextComponent(displayName);
    }

    public BaseComponent getDisplayName() {
        return displayName;
    }

    public Material getMaterial() {
        return blockType;
    }

    public static @Nullable CommandBlockType fromMaterial(Material material) {
        switch (material) {
            case COMMAND_BLOCK:
                return DEFAULT;
            case CHAIN_COMMAND_BLOCK:
                return CHAIN;
            case REPEATING_COMMAND_BLOCK:
                return REPEATING;
        }
        return null;
    }

}

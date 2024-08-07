package com.gmail.necnionch.myplugin.commandblockfinder.bukkit.wand.actions;

import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.block.Block;
import org.bukkit.block.CommandBlock;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public interface WandAction {

    void onNext();
    void onBack();
    void onClick(Block block);
    void onSelect(boolean current);

    default @Nullable BaseComponent[] getDisplay() {
        return null;
    }

    @Nullable BaseComponent[] getBackDisplay();

    @Nullable BaseComponent[] getNextDisplay();

    @Nullable BaseComponent[] getCurrentDisplay();

    boolean available();

    default void tick() {}

    @Nullable CommandBlock getSelectedCommandBlock();


    interface Creator<A extends WandAction> {
        Class<A> getActionClass();
        A create(Player player);
    }

}

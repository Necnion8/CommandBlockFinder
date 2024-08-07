package com.gmail.necnionch.myplugin.commandblockfinder.bukkit.wand;

import com.gmail.necnionch.myplugin.commandblockfinder.bukkit.wand.actions.SelectingBlockAction;
import com.gmail.necnionch.myplugin.commandblockfinder.bukkit.wand.actions.WandAction;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class WandInstance {

    private final PlayerWandManager manager;
    private final Player player;
    private final List<WandAction> actions;
    private @NotNull WandAction current;

    public WandInstance(PlayerWandManager manager, Player player, List<WandAction> actions) {
        this.manager = manager;
        this.player = player;
        this.actions = actions;
        this.current = actions.get(0);
    }

    public PlayerWandManager getManager() {
        return manager;
    }

    public Player getPlayer() {
        return player;
    }

    public @NotNull WandAction getCurrent() {
        return current;
    }

    public WandAction nextAction() {
        int index = actions.indexOf(current) + 1;
        for (int i = 0; i < actions.size(); i++) {
            WandAction action = actions.get((index + i) % actions.size());
            if (action.available()) {
                return changeAction(action);
            }
        }
//        this.current = actions.get(0);
        return current;
    }

    private WandAction changeAction(WandAction newAction) {
        if (this.current.equals(newAction))
            return this.current;
        this.current.onSelect(false);
        newAction.onSelect(true);
        this.current = newAction;
        return this.current;
    }

    public WandAction selectAction(Class<? extends WandAction> actionClass, boolean force) {
        if (current.getClass().equals(actionClass))
            return current;

        WandAction action = actions.stream()
                .filter(a -> a.getClass().equals(actionClass))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown action: " + actionClass.getSimpleName()));
        if (action.available() || force) {
            return changeAction(action);
        }
        return current;
    }

    @SuppressWarnings("unchecked")
    public <A extends WandAction> A getAction(Class<A> actionClass) {
        if (current.getClass().equals(actionClass))
            return ((A) current);

        return (A) actions.stream()
                .filter(a -> a.getClass().equals(actionClass))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown action: " + actionClass.getSimpleName()));
    }

    public void tick() {
        if (current.available()) {
            current.tick();
        }
    }



}

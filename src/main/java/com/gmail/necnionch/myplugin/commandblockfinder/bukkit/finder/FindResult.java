package com.gmail.necnionch.myplugin.commandblockfinder.bukkit.finder;

import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.block.CommandBlock;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class FindResult {

    private final FindParam param;
    private final List<CommandBlock> results;
    private final CommandBlockFinder finder;

    public FindResult(CommandBlockFinder finder, FindParam param, List<CommandBlock> results) {
        this.finder = finder;
        this.param = param;
        this.results = results;
    }

    public static FindResult empty(CommandBlockFinder finder) {
        return new FindResult(finder, new FindParam(), Collections.emptyList());
    }

    public FindParam getParam() {
        return param;
    }

    public List<CommandBlock> getResults() {
        return results;
    }

    public List<Location> getResultLocations() {
        return Collections.unmodifiableList(results.stream()
                .map(BlockState::getLocation)
                .collect(Collectors.toList()));
    }

    public void used() {
        finder.used();
    }

}

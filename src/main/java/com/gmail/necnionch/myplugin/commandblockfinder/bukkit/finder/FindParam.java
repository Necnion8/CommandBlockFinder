package com.gmail.necnionch.myplugin.commandblockfinder.bukkit.finder;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class FindParam {
    private Integer radius;
    private String world;
    private Pattern regex;
    private boolean allWorld;

    public FindParam() {}

    public static FindParam create() {
        return new FindParam();
    }

    public FindParam copy() {
        FindParam p = new FindParam();
        p.radius = radius;
        p.world = world;
        p.regex = regex;
        p.allWorld = allWorld;
        return p;
    }

    public FindParam radius(Integer radius) {
        this.radius = radius;
        return this;
    }

    public FindParam world(String world) {
        this.world = world;
        return this;
    }

    public FindParam allWorld(boolean all) {
        this.allWorld = all;
        return this;
    }

    public FindParam regex(Pattern regex) {
        this.regex = regex;
        return this;
    }

    public Integer radius() {
        return radius;
    }

    public String world() {
        return world;
    }

    public Pattern regex() {
        return regex;
    }

    public boolean allWorld() {
        return allWorld;
    }


    public static FindParam parseString(String param) {
        FindParam p = FindParam.create();
        List<String> sb = Lists.newArrayList();
        for (String s : param.split(" ")) {
            if (s.startsWith("w:") && 2 < s.length()) {
                String arg = s.substring(2);
                if (arg.equalsIgnoreCase("all")) {
                    p.allWorld(true);
                } else {
                    p.world(arg);
                }
            } else if (s.startsWith("r:") && 2 < s.length()) {
                try {
                    p.radius(Integer.parseInt(s.substring(2)));
                } catch (NumberFormatException ignored) {
                }
            } else {
                sb.add(s);
            }
        }

        if (!sb.isEmpty()) {
            String regex = String.join(" ", sb);
            try {
                p.regex(Pattern.compile(regex));
            } catch (PatternSyntaxException e) {
                p.regex(Pattern.compile(Pattern.quote(regex)));
            }
        }
        return p;
    }

}

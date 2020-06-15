package xyz.acrylicstyle.storageBox;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import util.CollectionList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StorageBoxTabCompleter implements TabCompleter {
    private static CollectionList<String> filterArgsList(CollectionList<String> list, String s) {
        return list.filter(s2 -> s2.toLowerCase().replaceAll(".*:(.*)", "$1").startsWith(s.toLowerCase().replaceAll(".*:(.*)", "$1")));
    }

    private static CollectionList<String> filterArgsList(List<String> list, String s) { return filterArgsList(new CollectionList<>(list), s); }

    private static final List<String> emptyList = new ArrayList<>();

    private static final List<String> commands = Arrays.asList(
            "autocollect",
            "changetype",
            "new",
            "resetconfig",
            "bypass",
            "setamount",
            "extract",
            "getstorage",
            "collect"
    );

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 0) return commands;
        if (args.length == 1) return filterArgsList(commands, args[0]);
        return emptyList;
    }
}

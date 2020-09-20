package xyz.acrylicstyle.storageBox;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import util.CollectionList;
import util.ICollectionList;

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
            "extract",
            "collect",
            "convert"
    );

    private static final CollectionList<String> opCommands = new CollectionList<>(
            "resetconfig",
            "bypass",
            "setamount",
            "settype"
    ).concat(ICollectionList.asList(commands));

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 0) return commands;
        if (args.length == 1) {
            return filterArgsList(sender.isOp() ? opCommands : commands, args[0]);
        }
        if (args.length == 2) {
            if (sender.isOp()) {
                if (args[0].equals("settype")) {
                    return filterArgsList(ICollectionList.asList(Material.values()).map(Material::name).toList(), args[1]);
                }
            }
        }
        return emptyList;
    }
}

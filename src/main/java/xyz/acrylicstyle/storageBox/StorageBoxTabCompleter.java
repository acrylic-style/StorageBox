package xyz.acrylicstyle.storageBox;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class StorageBoxTabCompleter implements TabCompleter {
    private static List<String> filterArgsList(List<String> list, String s) {
        return list.stream().filter(s2 -> s2.toLowerCase().replaceAll(".*:(.*)", "$1").startsWith(s.toLowerCase().replaceAll(".*:(.*)", "$1"))).collect(Collectors.toList());
    }

    private static final List<String> emptyList = new ArrayList<>();

    private static final List<String> commands = Arrays.asList(
            "autocollect",
            "changetype",
            "extract",
            "collect",
            "convert",
            "new"
    );

    private static final List<String> opCommands = Arrays.asList(
            "bypass",
            "setamount",
            "settype",
            "autocollect",
            "changetype",
            "extract",
            "collect",
            "convert",
            "new"
    );

    private static List<String> materials = null;

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {
        if (materials == null) {
            materials = Arrays.stream(Material.values()).map(Material::name).collect(Collectors.toList());
        }
        if (args.length == 0) return commands;
        if (args.length == 1) {
            return filterArgsList(sender.hasPermission("storagebox.op") ? opCommands : commands, args[0]);
        }
        if (args.length == 2) {
            if (sender.hasPermission("storagebox.op")) {
                if (args[0].equals("settype")) {
                    return filterArgsList(materials, args[1]);
                }
            }
        }
        return emptyList;
    }
}

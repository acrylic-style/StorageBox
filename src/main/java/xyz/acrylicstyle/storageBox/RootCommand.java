package xyz.acrylicstyle.storageBox;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.acrylicstyle.storageBox.commands.*;
import xyz.acrylicstyle.storageBox.utils.StorageBox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RootCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player)) {
            if (args.length == 0) {
                sender.sendMessage("/sb give <player>");
                return true;
            }
            if (args[0].equals("give")) {
                Player player = Bukkit.getPlayerExact(args[1]);
                if (player != null) {
                    player.getInventory().addItem(StorageBox.getNewStorageBox().getItemStack());
                }
            }
            return true;
        }
        Player player = (Player) sender;
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        List<String> argsList = new ArrayList<>(Arrays.asList(args));
        argsList.remove(0);
        String[] slicedArgs = argsList.toArray(new String[0]);
        if (args[0].equalsIgnoreCase("autocollect")) {
            AutoCollectCommand.onCommand(player);
        } else if (args[0].equalsIgnoreCase("bypass") && player.hasPermission("storagebox.op")) {
            BypassCommand.onCommand(player);
        } else if (args[0].equalsIgnoreCase("changetype")) {
            ChangeTypeCommand.onCommand(player);
        } else if (args[0].equalsIgnoreCase("collect")) {
            CollectCommand.onCommand(player);
        } else if (args[0].equalsIgnoreCase("convert")) {
            ConvertStorageBoxCommand.onCommand(player);
        } else if (args[0].equalsIgnoreCase("extract")) {
            ExtractCommand.onCommand(player, slicedArgs);
        } else if (args[0].equalsIgnoreCase("new")) {
            NewCommand.onCommand(player);
        } else if (args[0].equalsIgnoreCase("setamount") && player.hasPermission("storagebox.op")) {
            SetAmountCommand.onCommand(player, slicedArgs);
        } else if (args[0].equalsIgnoreCase("settype") && player.hasPermission("storagebox.op")) {
            SetTypeCommand.onCommand(player, slicedArgs);
        } else if (args[0].equalsIgnoreCase("sell")) {
            SellCommand.onCommand(player, slicedArgs);
        } else if (args[0].equalsIgnoreCase("buy")) {
            BuyCommand.onCommand(player, slicedArgs);
        } else if (args[0].equalsIgnoreCase("shop")) {
            ShopCommand.onCommand(player);
        } else {
            sendHelp(sender);
        }
        return true;
    }

    public static void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "------------------------------");
        sender.sendMessage(help("autocollect", "アイテムの自動回収を切り替えます。"));
        sender.sendMessage(help("changetype", "StorageBoxのアイテムの中身を変えます。オフハンドに変更先のアイテムを持ってください。"));
        sender.sendMessage(help("collect", "手に持ってるStorage Boxにインベントリに入ってるブロックを収納します。"));
        sender.sendMessage(help("convert", "Storage Boxの種類を変換します。"));
        sender.sendMessage(help("extract <数>", "アイテムをStorage Boxから取り出します。"));
        sender.sendMessage(help("new", "新しいStorage Boxを作成します。"));
        sender.sendMessage(help("sell [数]", "アイテムを売ります。"));
        sender.sendMessage(help("buy [数]", "アイテムを買います。"));
        sender.sendMessage(help("shop", "StorageBoxを買います。"));
        if (sender.hasPermission("storagebox.op")) {
            sender.sendMessage(help("bypass", "アイテムチェックなどを無視します。[OP]"));
            sender.sendMessage(help("setamount <amount>", "アイテムの数を設定します。[OP]"));
            sender.sendMessage(help("settype <Material>", "アイテムの種類を設定します。[OP]"));
        }
        sender.sendMessage(ChatColor.GOLD + "------------------------------");
    }

    private static String help(String command, String description) {
        return ChatColor.YELLOW + "/storage " + command + ChatColor.GRAY + " - " + ChatColor.AQUA + description;
    }
}

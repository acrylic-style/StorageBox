package xyz.acrylicstyle.storageBox;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.acrylicstyle.storageBox.commands.AutoCollectCommand;
import xyz.acrylicstyle.storageBox.commands.BypassCommand;
import xyz.acrylicstyle.storageBox.commands.ChangeTypeCommand;
import xyz.acrylicstyle.storageBox.commands.CollectCommand;
import xyz.acrylicstyle.storageBox.commands.ConvertStorageBoxCommand;
import xyz.acrylicstyle.storageBox.commands.ExtractCommand;
import xyz.acrylicstyle.storageBox.commands.NewCommand;
import xyz.acrylicstyle.storageBox.commands.SetAmountCommand;
import xyz.acrylicstyle.storageBox.commands.SetTypeCommand;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RootCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("hi");
            return true;
        }
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        List<String> argsList = new ArrayList<>(Arrays.asList(args));
        argsList.remove(0);
        String[] slicedArgs = argsList.toArray(new String[0]);
        Player player = (Player) sender;
        if (args[0].equalsIgnoreCase("autocollect")) {
            AutoCollectCommand.onCommand(player);
        } else if (args[0].equalsIgnoreCase("bypass") && player.isOp()) {
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
        } else if (args[0].equalsIgnoreCase("setamount") && player.isOp()) {
            SetAmountCommand.onCommand(player, slicedArgs);
        } else if (args[0].equalsIgnoreCase("settype") && player.isOp()) {
            SetTypeCommand.onCommand(player, slicedArgs);
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
        sender.sendMessage(help("extract <amount>", "アイテムをStorage Boxから取り出します。"));
        sender.sendMessage(help("new", "新しいStorage Boxを作成します。"));
        if (sender.isOp()) {
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

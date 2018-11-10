package at.dalex.api.numberinput;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;

public class Main extends JavaPlugin {

    private static Main instance;

    @Override
    public void onEnable() {
        instance = this;
        getServer().getConsoleSender().sendMessage("§8[§3NumberInputAPI§8] §aPlugin geladen!");
    }

    @Override
    public void onDisable() {
        getServer().getConsoleSender().sendMessage("§8[§3NumberInputAPI§8] §4Plugin deaktiviert.");
    }

    static Main getInstance() {
        return instance;
    }
}

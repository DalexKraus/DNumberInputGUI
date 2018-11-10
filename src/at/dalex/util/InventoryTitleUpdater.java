package at.dalex.util;

import net.minecraft.server.v1_13_R2.ChatMessage;
import net.minecraft.server.v1_13_R2.EntityPlayer;
import net.minecraft.server.v1_13_R2.PacketPlayOutOpenWindow;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class InventoryTitleUpdater {

    public static void changeOpenInventoryTitle(Player p, String title) {
        EntityPlayer entPlayer = ((CraftPlayer) p).getHandle();
        PacketPlayOutOpenWindow packet = new PacketPlayOutOpenWindow(entPlayer.activeContainer.windowId, "minecraft:chest",
                new ChatMessage(title), p.getOpenInventory().getTopInventory().getSize());
        entPlayer.playerConnection.sendPacket(packet);
        entPlayer.updateInventory(entPlayer.activeContainer);
    }
}

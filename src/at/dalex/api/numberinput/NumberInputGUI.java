package at.dalex.api.numberinput;

import at.dalex.util.InventoryTitleUpdater;
import at.dalex.util.ItemCreator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class NumberInputGUI implements Listener {

    private UUID guiId;
    private Player player;
    private String inventoryTitle;
    private Inventory userInventory;
    private int currentValue;
    private int min = Integer.MIN_VALUE, max = Integer.MAX_VALUE;
    private boolean isClosing = false;

    //First is the player, second is the gui's id
    private static HashMap<UUID, UUID> openGUIs = new HashMap<>();
    private static ArrayList<UUID> registeredGUIIDs = new ArrayList<>();

    //Callback
    private NumberInputGUICallback callback;

    private final int INVENTORY_SIZE = 54;
    private final int CURRENT_VALUE_ITEM_SLOT = 43;
    private final int CANCEL_ITEM_FIRST_SLOT = 37;
    private final int APPROVE_ITEM_FIRST_SLOT = 40;

    private int[] weights = { 1, 10, 100, 1000, 10000, 100000, 1000000 };

    public NumberInputGUI(Player player, NumberInputGUICallback callback, String inventoryTitle, int startValue) {
        this.guiId = genGUIID();
        this.player = player;
        this.callback = callback;
        this.currentValue = startValue;
        this.inventoryTitle = inventoryTitle;
        this.userInventory = Bukkit.createInventory(null, INVENTORY_SIZE, createTitle());
        setupInventory();
        //Register listener
        Bukkit.getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }

    private void setupInventory() {
        //Add row
        for (int i = 0; i < 7; i++) {
            ItemStack addItem = ItemCreator.create(Material.CACTUS_GREEN, 1, "§e+§a" + weights[i]);
            userInventory.setItem(10 + i, addItem);
        }
        //Subtract row
        for (int i = 0; i < 7; i++) {
            ItemStack subtractItem = ItemCreator.create(Material.ROSE_RED, 1, "§4-§c" + weights[i]);
            userInventory.setItem(19 + i, subtractItem);
        }
        //Value item
        ItemStack currentValueItem = ItemCreator.create(Material.PAPER, 1, "§7Ausgewählt: §e" + currentValue);
        userInventory.setItem(CURRENT_VALUE_ITEM_SLOT, currentValueItem);

        //Cancel item
        ItemStack cancelItem = ItemCreator.create(Material.RED_BANNER, 1, "§4Abbrechen");
        userInventory.setItem(CANCEL_ITEM_FIRST_SLOT, cancelItem);
        userInventory.setItem(CANCEL_ITEM_FIRST_SLOT + 1, cancelItem);

        //Approve item
        ItemStack approveItem = ItemCreator.create(Material.GREEN_BANNER, 1, "§aAuswählen");
        userInventory.setItem(APPROVE_ITEM_FIRST_SLOT, approveItem);
        userInventory.setItem(APPROVE_ITEM_FIRST_SLOT + 1, approveItem);
    }

    public void setBoundaries(int min, int max) {
        this.min = min;
        this.max = max;
    }

    private String createTitle() {
        return inventoryTitle + "§7: §e" + currentValue;
    }

    private static UUID genGUIID() {
        UUID uuid = null;
        boolean found = false;
        while (!found) {
            uuid = UUID.randomUUID();
            if (!registeredGUIIDs.contains(uuid)) found = true;
        }

        return uuid;
    }


    private void update() {
        //Update value item
        ItemStack valueItem = ItemCreator.create(Material.PAPER, 1, "§7Ausgewählt: §e" + currentValue);
        userInventory.setItem(CURRENT_VALUE_ITEM_SLOT, valueItem);

        //Change title
        InventoryTitleUpdater.changeOpenInventoryTitle(player, createTitle());
        player.updateInventory();
    }

    public void open(Player player) {
        openGUIs.put(player.getUniqueId(), getGuiId());
        isClosing = false;
        player.openInventory(userInventory);
        callback.onOpen(this);
    }

    public void close() {
        if (openGUIs.containsKey(player.getUniqueId())) {
            isClosing = true;
            player.closeInventory();
            callback.onClose(this);
        }
    }

    public void setCallbackListener(NumberInputGUICallback callbackListener) {
        this.callback = callbackListener;
    }

    public UUID getGuiId() {
        return this.guiId;
    }

    public Player getPlayer() {
        return this.player;
    }

    public String getInventoryTitle() {
        return this.inventoryTitle;
    }

    public int getMin() {
        return this.min;
    }

    public int getMax() {
        return this.max;
    }

    public int getCurrentValue() {
        return this.currentValue;
    }

    /* Listeners */
    @EventHandler
    public void onClick(InventoryClickEvent e) {
        Inventory inv = e.getInventory();

        Player p = (Player) e.getWhoClicked();
        if (openGUIs.containsKey(p.getUniqueId())) {

            if (inv.getSize() == INVENTORY_SIZE) {
                //Get the id of the open gui from the player
                UUID guiID = openGUIs.get(p.getUniqueId());

                //Is same gui
                if (guiID.equals(getGuiId())) {
                    int slot = e.getSlot();

                    //Add row
                    if (slot > 9 && slot < 17) {
                        int clickedWeightIndex = slot - 10;
                        int result = currentValue + weights[clickedWeightIndex];
                        //Check bounds
                        if (result >= min && result <= max) {
                            currentValue = result;
                            p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_BURP, 1.0f, 1.0f);
                        }
                        else p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);

                        update();
                    }
                    //Subtract row
                    else if (slot > 18 && slot < 26) {
                        int clickedWeightIndex = slot - 19;
                        int result = currentValue - weights[clickedWeightIndex];
                        //Check bounds
                        if (result >= min && result <= max) {
                            currentValue = result;
                            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1.0f, 1.0f);
                        }
                        else p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
                        update();
                    }

                    //Approve item
                    else if (slot == APPROVE_ITEM_FIRST_SLOT || slot == APPROVE_ITEM_FIRST_SLOT + 1) {
                        callback.onApprove(this);
                        p.closeInventory();
                        isClosing = true;
                    }
                    else if (slot == CANCEL_ITEM_FIRST_SLOT || slot == CANCEL_ITEM_FIRST_SLOT + 1) {
                        close();
                    }
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onDisconnect(PlayerQuitEvent e) {
        if (openGUIs.containsKey(e.getPlayer().getUniqueId())) {
            openGUIs.remove(e.getPlayer().getUniqueId());
            isClosing = false;
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        Inventory inv = e.getInventory();
        Player p = (Player) e.getPlayer();
        if (openGUIs.containsKey(p.getUniqueId())) {
            if (!isClosing) {
                //Remove the player from open guis
                if (inv.getSize() == INVENTORY_SIZE) {
                    openGUIs.remove(p.getUniqueId());
                }
            }
        }
    }
}

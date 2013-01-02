package com.codisimus.plugins.turnstile;

import com.codisimus.plugins.chestlock.ChestLock;
import com.codisimus.plugins.chestlock.Safe;
import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import net.citizensnpcs.api.CitizensManager;
import net.citizensnpcs.resources.npclib.HumanNPC;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Door;

/**
 * Executes Player Commands
 *
 * @author Codisimus
 */
public class TurnstileCommand implements CommandExecutor {
    public static String command;
    private static enum Action {
        HELP, SIGN, MAKE, LINK, PRICE, OWNER, ACCESS, BANK, UNLINK, DELETE,
        FREE, LOCKED, NOFRAUD, COLLECT, LIST, INFO, RENAME, COOLDOWN, RL
    }
    private static enum Help { CREATE, SETUP, SIGN }
    private static final HashSet MAKE_TRANSPARENT = Sets.newHashSet((byte)0, (byte)6,
            (byte)8, (byte)9, (byte)10, (byte)11, (byte)26, (byte)27, (byte)28,
            (byte)30, (byte)31, (byte)32, (byte)37, (byte)38, (byte)39, (byte)40,
            (byte)44, (byte)50, (byte)51, (byte)53, (byte)55, (byte)59, (byte)63,
            (byte)65, (byte)66, (byte)67, (byte)68, (byte)69, (byte)70, (byte)72,
            (byte)75, (byte)76, (byte)77, (byte)78, (byte)90, (byte)92, (byte)101,
            (byte)102, (byte)104, (byte)105, (byte)106, (byte)108, (byte)109,
            (byte)111, (byte)114, (byte)115, (byte)117);
    private static final HashSet LINK_TRANSPARENT = Sets.newHashSet((byte)0, (byte)6,
            (byte)8, (byte)9, (byte)10, (byte)11, (byte)26, (byte)27, (byte)28,
            (byte)30, (byte)31, (byte)32, (byte)37, (byte)38, (byte)39, (byte)40,
            (byte)44, (byte)50, (byte)51, (byte)53, (byte)55, (byte)59, (byte)63,
            (byte)64, (byte)65, (byte)66, (byte)67, (byte)68, (byte)71, (byte)75,
            (byte)76, (byte)78, (byte)85, (byte)90, (byte)92, (byte)96, (byte)101,
            (byte)102, (byte)104, (byte)105, (byte)106, (byte)107, (byte)108,
            (byte)109, (byte)111, (byte)113, (byte)114, (byte)115, (byte)117);
    private static final HashSet TRANSPARENT = Sets.newHashSet((byte)0, (byte)6,
            (byte)8, (byte)9, (byte)10, (byte)11, (byte)26, (byte)27, (byte)28,
            (byte)30, (byte)31, (byte)32, (byte)37, (byte)38, (byte)39, (byte)40,
            (byte)44, (byte)50, (byte)51, (byte)53, (byte)55, (byte)59, (byte)63,
            (byte)65, (byte)66, (byte)67, (byte)68, (byte)75, (byte)76, (byte)78,
            (byte)90, (byte)92, (byte)101, (byte)102, (byte)104, (byte)105,
            (byte)106, (byte)108, (byte)109, (byte)111, (byte)114, (byte)115,
            (byte)117);

    /**
     * Listens for Turnstile commands to execute them
     *
     * @param sender The CommandSender who may not be a Player
     * @param command The command that was executed
     * @param alias The alias that the sender used
     * @param args The arguments for the command
     * @return true always
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        //Cancel if the command is not from a Player
        if (!(sender instanceof Player)) {
            if (args.length > 0 && args[0].equals("rl")) {
                TurnstileMain.rl();
            }

            return true;
        }

        Player player = (Player) sender;

        //Display the help page if the Player did not add any arguments
        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        Action action;

        try {
            action = Action.valueOf(args[0].toUpperCase());
        } catch (IllegalArgumentException notEnum) {
            sendHelp(player);
            return true;
        }

        //Execute the correct command
        switch (action) {
        case SIGN:
            sendSignHelp(player);
            return true;

        case MAKE:
            if (args.length == 2) {
                make(player, args[1]);
            } else {
                sendCreateHelp(player);
            }
            return true;

        case LINK:
            switch (args.length) {
            case 2:
                link(player, args[1]);
                return true;

            case 3:
                try {
                    linkNPC(player, args[1], Integer.parseInt(args[2]));
                    return true;
                } catch (Exception notInt) {
                    break;
                }

            default: break;
            }

            sendCreateHelp(player);
            return true;

        case PRICE:
            try {
                switch (args.length) {
                case 1: //Item price (Contents of target Chest)
                    price(player);
                    return true;

                case 2: //Money price (Name not included)
                    if (args[1].equals("all")) {
                        price(player, null, -411);
                    } else {
                        price(player, null, Double.parseDouble(args[1]));
                    }
                    return true;

                case 3: //Money price (Name included)
                    if (args[1].equals("all")) {
                        price(player, args[1], -411);
                    } else {
                        price(player, args[1], Double.parseDouble(args[2]));
                    }
                    return true;

                case 4: //Item price (Item details)
                    LinkedList<Enchantment> enchantments = getEnchantments(args[3]);

                    if (TurnstileMain.findTurnstile(args[1]) != null) { //Name was included
                        price(player, args[1], Integer.parseInt(args[2]), args[3], enchantments, (short)-1);
                    } else if (enchantments.isEmpty()) { //Durability is included
                        price(player, null, Integer.parseInt(args[1]), args[2], enchantments, Short.parseShort(args[3]));
                    } else { //Enchantments is included
                        price(player, null, Integer.parseInt(args[1]), args[2], enchantments, (short)-1);
                    }
                    return true;

                case 5: //Item price (Item details, Name, and Durability included)
                    LinkedList<Enchantment> enchants = getEnchantments(args[4]);
                    if (enchants.isEmpty()) { //Durability is included
                        price(player, args[1], Integer.parseInt(args[2]), args[3], enchants, Short.parseShort(args[4]));
                    } else { //Enchantments is included
                        price(player, args[1], Integer.parseInt(args[2]), args[3], enchants, (short)-1);
                    }
                    return true;

                default: break;
                }
            } catch (Exception notNumber) {
                notNumber.printStackTrace();
            }

            sendSetupHelp(player);
            return true;

        case NOFRAUD:
            try {
                switch (args.length) {
                case 2:
                    noFraud(player, null, Boolean.parseBoolean(args[1]));
                    return true;
                case 3:
                    noFraud(player, args[1], Boolean.parseBoolean(args[2]));
                    return true;
                default: break;
                }
            } catch (Exception notBool) {
            }

            sendSetupHelp(player);
            return true;

        case OWNER:
            switch (args.length) {
            case 2:
                owner(player, null, args[1]);
                return true;
            case 3:
                owner(player, args[1], args[2]);
                return true;
            default:
                sendSetupHelp(player);
                return true;
            }

        case ACCESS:
            switch (args.length) {
            case 2:
                access(player, null, args[1]);
                return true;
            case 3:
                access(player, args[1], args[2]);
                return true;
            default:
                sendSetupHelp(player);
                return true;
            }

        case BANK:
            switch (args.length) {
            case 2:
                bank(player, null, args[1]);
                return true;
            case 3:
                bank(player, args[1], args[2]);
                return true;
            default:
                sendSetupHelp(player);
                return true;
            }

        case UNLINK:
            if (args.length == 1) {
                unlink(player);
            } else {
                sendCreateHelp(player);
            }
            return true;

        case DELETE:
            switch (args.length) {
            case 1:
                delete(player, null);
                return true;
            case 2:
                delete(player, args[1]);
                return true;
            default:
                sendCreateHelp(player);
                return true;
            }

        case FREE:
            switch (args.length) {
            case 2:
                free(player, null, args[1]);
                return true;
            case 3:
                free(player, args[1], args[2]);
                return true;
            default:
                sendSetupHelp(player);
                return true;
            }

        case LOCKED:
            switch (args.length) {
            case 2:
                locked(player, null, args[1]);
                return true;
            case 3:
                locked(player, args[1], args[2]);
                return true;
            default:
                sendSetupHelp(player);
                return true;
            }

        case COOLDOWN:
            switch (args.length) {
            case 1:
                cooldown(player, null);
                return true;
            case 2:
                cooldown(player, args[1]);
                return true;
            default:
                sendSetupHelp(player);
                return true;
            }

        case COLLECT:
            if (args.length == 1) {
                collect(player);
            } else {
                sendSetupHelp(player);
            }
            return true;

        case LIST:
            if (args.length == 1) {
                list(player);
            } else {
                sendHelp(player);
            }
            return true;

        case INFO:
            switch (args.length) {
            case 1:
                info(player, null);
                return true;
            case 2:
                info(player, args[1]);
                return true;
            default:
                sendHelp(player);
                return true;
            }

        case RENAME:
            switch (args.length) {
            case 2:
                rename(player, null, args[1]);
                return true;
            case 3:
                rename(player, args[1], args[2]);
                return true;
            default:
                sendHelp(player);
                return true;
            }

        case RL:
            if (args.length == 1) {
                TurnstileMain.rl(player);
            } else {
                sendHelp(player);
            }
            return true;

        case HELP:
            if (args.length == 2) {
                Help help;

                try {
                    help = Help.valueOf(args[1].toUpperCase());
                } catch (Exception notEnum) {
                    sendHelp(player);
                    return true;
                }

                switch (help) {
                case CREATE:
                    sendCreateHelp(player);
                    break;
                case SETUP:
                    sendSetupHelp(player);
                    break;
                case SIGN:
                    sendSignHelp(player);
                    break;
                }
            } else {
                sendHelp(player);
            }

            return true;

        default:
            sendHelp(player);
            return true;
        }
    }

    /**
     * Creates a new Turnstile of the given name
     *
     * @param player The Player creating the Turnstile
     * @param name The name of the Turnstile being created (must not already exist)
     */
    private static void make(Player player, String name) {
        //Cancel if the Player does not have permission to use the command
        if (!TurnstileMain.hasPermission(player, "make")) {
            player.sendMessage(TurnstileMessages.permission);
            return;
        }

        //Cancel if the Turnstile already exists
        if (TurnstileMain.findTurnstile(name) != null) {
            player.sendMessage("A Turnstile named " + name + " already exists.");
            return;
        }

        Block block = player.getTargetBlock(MAKE_TRANSPARENT, 10);
        switch (block.getType()) {
        case FENCE: break;
        case TRAP_DOOR: break;
        case FENCE_GATE: break;
        case NETHER_FENCE: break;

        case WOOD_DOOR: //Fall through
        case WOODEN_DOOR: //Fall through
        case IRON_DOOR: //Fall through
        case IRON_DOOR_BLOCK: //Make sure the bottom half of the Door is used
            if (((Door)block.getState().getData()).isTopHalf()) {
                block = block.getRelative(BlockFace.DOWN);
            }
            break;

        default: //Cancel because an invalid Block type is targeted
            player.sendMessage("You must target a Door or Fence.");
            return;
        }

        //Do not charge cost to make a Turnstile is 0 or Player has the 'turnstile.makefree' node
        int price = TurnstileMain.cost;
        if (price > 0 && (!TurnstileMain.useMakeFreeNode || !TurnstileMain.hasPermission(player, "makefree"))) {
            //Cancel if the Player could not afford it
            if (!Econ.charge(player.getName(), null, price)) {
                player.sendMessage("You do not have enough money to make the Turnstile");
                return;
            }

            player.sendMessage(price + " deducted,");
        }

        Turnstile turnstile = new Turnstile(name, player.getName(), block);
        player.sendMessage("Turnstile " + name + " made!");
        TurnstileMain.addTurnstile(turnstile);
    }

    /**
     * Links the target Block to the specified Turnstile
     *
     * @param player The Player linking the Block they are targeting
     * @param name The name of the Turnstile the Block will be linked to
     */
    private static void link(Player player, String name) {
        //Cancel if the Player does not have permission to use the command
        if (!TurnstileMain.hasPermission(player, "make")) {
            player.sendMessage(TurnstileMessages.permission);
            return;
        }

        Turnstile turnstile = TurnstileMain.findTurnstile(name);

        //Cancel if the Turnstile does not exist
        if (turnstile == null) {
            player.sendMessage("Turnstile "+name+" does not exist.");
            return;
        }

        //Cancel if the Player does not own the Turnstile
        if (!turnstile.isOwner(player)) {
            player.sendMessage("Only the Turnstile Owner can do that.");
            return;
        }

        Block block = player.getTargetBlock(LINK_TRANSPARENT, 10);

        //Cancel if a correct block type is not targeted
        switch (block.getType()) {
        case CHEST:
            //Make the Chest unlockable if ChestLock is enabled
            if (TurnstileMain.pm.isPluginEnabled("ChestLock")) {
                Safe safe = ChestLock.findSafe(block);
                if (safe == null) {
                    safe = new Safe(player.getName(), block);
                    safe.locked = false;
                    safe.lockable = false;

                    ChestLock.addSafe(safe);
                }
            }
            //Fall through
        case STONE_PLATE: //Fall through
        case WOOD_PLATE: //Fall through
        case STONE_BUTTON:
            turnstile.buttons.add(new TurnstileButton(block));
            break;

        default:
            player.sendMessage("You must link the Turnstile to a Button,"
                                + "Chest, Pressure plate, or Sign.");
            return;
        }

        player.sendMessage("Succesfully linked to Turnstile " + name + "!");
        turnstile.save();
    }

    /**
     * Links the given NPC to the specified Turnstile
     *
     * @param player The Player linking the Block they are targeting
     * @param name The name of the Turnstile the Block will be linked to
     * @param uid The unique ID of the NPC to be linked
     */
    private static void linkNPC(Player player, String name, int uid) {
        //Cancel if the Player does not have permission to use the command
        if (!TurnstileMain.hasPermission(player, "make")) {
            player.sendMessage(TurnstileMessages.permission);
            return;
        }

        Turnstile turnstile = TurnstileMain.findTurnstile(name);

        //Cancel if the Turnstile does not exist
        if (turnstile == null) {
            player.sendMessage("Turnstile " + name + " does not exist.");
            return;
        }

        //Cancel if the Player does not own the Turnstile
        if (!turnstile.isOwner(player)) {
            player.sendMessage("Only the Turnstile Owner can do that.");
            return;
        }

        //Cancel if the UID does not match an NPC
        HumanNPC npc = CitizensManager.getNPC(uid);
        if (npc == null) {
            player.sendMessage(uid+" is not a valid Citizens UID");
            return;
        }

        turnstile.buttons.add(new TurnstileButton(npc.getBaseLocation().getBlock()));

        player.sendMessage(npc.getName() + " succesfully linked to Turnstile " + name + "!");
        turnstile.save();
    }

    /**
     * Sets the price of the specified Turnstile to an item
     * If a name is not provided, the Turnstile of the target Block is modified
     *
     * @param player The Player setting the price
     * @param name The name of the Turnstile being modified
     * @param amount The amount/stack size of the item
     * @param itemString The id or String of the item Material
     * @param durability The Durability of the item
     */
    private static void price(Player player, String name, int amount, String itemString, LinkedList<Enchantment> enchantments, short durability) {
        //Cancel if the Player does not have permission to use the command
        if (!TurnstileMain.hasPermission(player, "set.price")) {
            player.sendMessage(TurnstileMessages.permission);
            return;
        }

        Turnstile turnstile = getTurnstile(player, name);
        if (turnstile == null) {
            return;
        }

        //Cancel if the Player does not own the Turnstile
        if (!turnstile.isOwner(player)) {
            player.sendMessage("Only the Turnstile Owner can do that.");
            return;
        }

        int id;
        Material type = Material.getMaterial(itemString);
        if (type == null) {
            try {
                id = Integer.parseInt(itemString);
            } catch (Exception notInt) {
                player.sendMessage(itemString+" is not a valid Item Name/ID");
                return;
            }
        } else {
            id = type.getId();
        }

        turnstile.items.clear();

        turnstile.items.add(enchantments.isEmpty()
                            ? new Item(id, durability, amount)
                            : new Item(id, enchantments, amount));

        turnstile.itemsEarned = 0;

        player.sendMessage("Price of Turnstile " + turnstile.name
                            + " has been set to "
                            + turnstile.itemsToInfoString() + "!");
        player.sendMessage("Pay with items by placing them into a linked Chest.");
        turnstile.save();
    }

    /**
     * Sets the price of the Turnstile of the target Chest to the contents of the Chest
     *
     * @param player The Player setting the price
     */
    private static void price(Player player) {
        //Cancel if the Player does not have permission to use the command
        if (!TurnstileMain.hasPermission(player, "set.price")) {
            player.sendMessage(TurnstileMessages.permission);
            return;
        }

        Block block = player.getTargetBlock(LINK_TRANSPARENT, 10);

        if (block.getTypeId() != 54) {
            player.sendMessage("You must target a Linked Chest");
            return;
        }

        Turnstile turnstile = TurnstileMain.findTurnstile(block);
        if (turnstile == null) {
            player.sendMessage("You must target a Linked Chest");
            return;
        }

        //Cancel if the Player does not own the Turnstile
        if (!turnstile.isOwner(player)) {
            player.sendMessage("Only the Turnstile Owner can do that.");
            return;
        }

        turnstile.items.clear();

        for (ItemStack item: ((Chest) block.getState()).getInventory().getContents()) {
            if (item != null) {
                turnstile.items.add(new Item(item));
            }
        }

        turnstile.itemsEarned = 0;

        player.sendMessage("Price of Turnstile " + turnstile.name
                            + " has been set to "
                            + turnstile.itemsToInfoString() + "!");
        player.sendMessage("Pay with items by placing them into a linked Chest.");
        turnstile.save();
    }

    /**
     * Sets the price of the specified Turnstile
     * If a name is not provided, the Turnstile of the target Block is modified
     *
     * @param player The Player setting the price
     * @param name The name of the Turnstile being modified
     * @param price The new price
     */
    private static void price(Player player, String name, double price) {
        //Cancel if the Player does not have permission to use the command
        if (!TurnstileMain.hasPermission(player, "set.price")) {
            player.sendMessage(TurnstileMessages.permission);
            return;
        }

        Turnstile turnstile = getTurnstile(player, name);
        if (turnstile == null) {
            return;
        }

        //Cancel if the Player does not own the Turnstile
        if (!turnstile.isOwner(player)) {
            player.sendMessage("Only the Turnstile Owner can do that.");
            return;
        }

        //Set price to money amount or -411 to take all the Players money
        turnstile.price = price;
        player.sendMessage("Price of Turnstile " + turnstile.name
                            + " has been set to " + price + "!");

        turnstile.save();
    }

    /**
     * Sets the time range that the specified Turnstile is locked
     * If a name is not provided, the Turnstile of the target Block is modified
     *
     * @param player The Player modifying the Turnstile
     * @param name The name of the Turnstile being modified
     * @param bool True if the Turnstile will be set to noFraud mode
     */
    private static void noFraud(Player player, String name, boolean bool) {
        //Cancel if the Player does not have permission to use the command
        if (!TurnstileMain.hasPermission(player, "set.nofraud")) {
            player.sendMessage(TurnstileMessages.permission);
            return;
        }

        Turnstile turnstile = getTurnstile(player, name);
        if (turnstile == null) {
            return;
        }

        //Cancel if the Player does not own the Turnstile
        if (!turnstile.isOwner(player)) {
            player.sendMessage("Only the Turnstile Owner can do that.");
            return;
        }

        //Toggle whether the Safe is lockable
        if (turnstile.noFraud) {
            if (bool) {
                player.sendMessage("Turnstile " + turnstile.name + " is already in NoFraud mode.");
            } else {
                player.sendMessage("Turnstile " + turnstile.name + " is no longer set to NoFraud mode.");
            }
        } else {
            if (bool) {
                player.sendMessage("Turnstile " + turnstile.name + " set to NoFraud mode.");
            } else {
                player.sendMessage("Turnstile " + turnstile.name + " is not set to NoFraud mode.");
            }
        }

        turnstile.noFraud = bool;
        turnstile.save();
    }

    /**
     * Changes the Owner of the specified Turnstile
     * If a name is not provided, the Turnstile of the target Block is modified
     *
     * @param player The Player changing the Owner
     * @param name The name of the Turnstile being modified
     * @param owner The new Owner
     */
    private static void owner(Player player, String name, String owner) {
        //Cancel if the Player does not have permission to use the command
        if (!TurnstileMain.hasPermission(player, "set.owner")) {
            player.sendMessage(TurnstileMessages.permission);
            return;
        }

        Turnstile turnstile = getTurnstile(player, name);
        if (turnstile == null) {
            return;
        }

        //Cancel if the Player does not own the Turnstile
        if (!turnstile.isOwner(player)) {
            player.sendMessage("Only the Turnstile Owner can do that.");
            return;
        }

        turnstile.owner = owner;
        player.sendMessage("Money from Turnstile " + turnstile.name
                            + " will go to " + owner + "!");
        turnstile.save();
    }

    /**
     * Modifies the access value of the specified Turnstile
     * If a name is not provided, the Turnstile of the target Block is modified
     *
     * @param player The Player modifying the Turnstile
     * @param name The name of the Turnstile being modified
     * @param access The new access value
     */
    private static void access(Player player, String name, String access) {
        //Cancel if the Player does not have permission to use the command
        if (!TurnstileMain.hasPermission(player, "set.access")) {
            player.sendMessage(TurnstileMessages.permission);
            return;
        }

        Turnstile turnstile = getTurnstile(player, name);
        if (turnstile == null) {
            return;
        }

        //Cancel if the Player does not own the Turnstile
        if (!turnstile.isOwner(player)) {
            player.sendMessage("Only the Turnstile Owner can do that.");
            return;
        }

        if (access.equals("public")) {
            turnstile.access = null;
        } else {
            turnstile.access = new LinkedList<String>();

            if (!access.equals("private")) {
                if (access.contains(",")) {
                    turnstile.access.addAll(Arrays.asList(access.split(",")));
                } else {
                    turnstile.access.add(access);
                }
            }
        }

        player.sendMessage("Access to Turnstile " + turnstile.name
                            + " has been set to " + access + "!");
        turnstile.save();
    }

    /**
     * Modifies the bank value of the specified Turnstile
     * If a name is not provided, the Turnstile of the target Block is modified
     *
     * @param player The Player modifying the Turnstile
     * @param name The name of the Turnstile being modified
     * @param bank The new bank value
     */
    private static void bank(Player player, String name, String bank) {
        //Cancel if the Player does not have permission to use the command
        if (!TurnstileMain.hasPermission(player, "set.bank")) {
            player.sendMessage(TurnstileMessages.permission);
            return;
        }

        Turnstile turnstile = getTurnstile(player, name);
        if (turnstile == null) {
            return;
        }

        //Cancel if the Player does not own the Turnstile
        if (!turnstile.isOwner(player)) {
            player.sendMessage("Only the Turnstile Owner can do that.");
            return;
        }

        turnstile.owner = "bank:"+bank;
        player.sendMessage("Money from Turnstile "+turnstile.name+" will go to "+bank+"!");
        turnstile.save();
    }

    /**
     * Unlinks the target Block from the specified Turnstile
     *
     * @param player The Player unlinking the Block they are targeting
     */
    private static void unlink(Player player) {
        //Cancel if the Player does not have permission to use the command
        if (!TurnstileMain.hasPermission(player, "make")) {
            player.sendMessage(TurnstileMessages.permission);
            return;
        }

        Block block = player.getTargetBlock(LINK_TRANSPARENT, 10);

        //Cancel if a correct block type is not targeted
        switch (block.getType()) {
            case CHEST: //Fall through
            case STONE_PLATE: //Fall through
            case WOOD_PLATE: //Fall through
            case STONE_BUTTON: break;

            default:
                player.sendMessage("You must link the Turnstile to a Button, Chest, or Pressure plate.");
                return;
        }

        //Cancel if the Turnstile does not exist
        Turnstile turnstile = TurnstileMain.findTurnstile(block);
        if (turnstile == null) {
            player.sendMessage("Target Block is not linked to a Turnstile.");
            return;
        }

        //Cancel if the Player does not own the Turnstile
        if (!turnstile.isOwner(player)) {
            player.sendMessage("Only the Turnstile Owner can do that.");
            return;
        }

        Iterator itr = turnstile.buttons.iterator();
        while (itr.hasNext()) {
            TurnstileButton button = (TurnstileButton)itr.next();
            if (block.getX() == button.x && block.getY() == button.y &&
                    block.getZ() == button.z &&
                    block.getWorld().getName().equals(button.world)) {
                itr.remove();
            }
        }

        player.sendMessage("Sucessfully unlinked!");
        turnstile.save();
    }

    /**
     * Deletes the specified Turnstile
     * If a name is not provided, the Turnstile of the target Block is deleted
     *
     * @param player The Player deleting the Turnstile
     * @param name The name of the Turnstile to be deleted
     */
    private static void delete(Player player, String name) {
        //Cancel if the Player does not have permission to use the command
        if (!TurnstileMain.hasPermission(player, "make")) {
            player.sendMessage(TurnstileMessages.permission);
            return;
        }

        Turnstile turnstile = getTurnstile(player, name);
        if (turnstile == null) {
            return;
        }

        //Cancel if the Player does not own the Turnstile
        if (!turnstile.isOwner(player)) {
            player.sendMessage("Only the Turnstile Owner can do that.");
            return;
        }

        TurnstileMain.removeTurnstile(turnstile);
        player.sendMessage("Turnstile " + turnstile.name + " was deleted!");
    }

    /**
     * Sets the time range that the specified Turnstile is free
     * If a name is not provided, the Turnstile of the target Block is modified
     *
     * @param player The Player modifying the Turnstile
     * @param name The name of the Turnstile being modified
     * @param range The given time range
     */
    private static void free(Player player, String name, String range) {
        //Cancel if the Player does not have permission to use the command
        if (!TurnstileMain.hasPermission(player, "set.free")) {
            player.sendMessage(TurnstileMessages.permission);
            return;
        }

        Turnstile turnstile = getTurnstile(player, name);
        if (turnstile == null) {
            return;
        }

        //Cancel if the Player does not own the Turnstile
        if (!turnstile.isOwner(player)) {
            player.sendMessage("Only the Turnstile Owner can do that.");
            return;
        }

        //Split range into start time and end time
        String[] time = range.split("-");
        try {
            turnstile.freeStart = Long.parseLong(time[0]);
            turnstile.freeEnd = Long.parseLong(time[1]);
        } catch (Exception notLong) {
            player.sendMessage("Invalid format: The time must be set to a range"
                                + " (ex. /" + command + " free 0-1000)");
        }

        player.sendMessage("Turnstile " + turnstile.name
                            + " is free to use from " + time[0]
                            + " to " + time[1] + "!");
        turnstile.save();
    }

    /**
     * Sets the time range that the specified Turnstile is locked
     * If a name is not provided, the Turnstile of the target Block is modified
     *
     * @param player The Player modifying the Turnstile
     * @param name The name of the Turnstile being modified
     * @param range The given time range
     */
    private static void locked(Player player, String name, String range) {
        //Cancel if the Player does not have permission to use the command
        if (!TurnstileMain.hasPermission(player, "set.locked")) {
            player.sendMessage(TurnstileMessages.permission);
            return;
        }

        Turnstile turnstile = getTurnstile(player, name);
        if (turnstile == null) {
            return;
        }

        //Cancel if the Player does not own the Turnstile
        if (!turnstile.isOwner(player)) {
            player.sendMessage("Only the Turnstile Owner can do that.");
            return;
        }

        //Split range into start time and end time
        String[] time = range.split("-");
        turnstile.lockedStart = Long.parseLong(time[0]);
        turnstile.lockedEnd = Long.parseLong(time[1]);

        player.sendMessage("Turnstile " + turnstile.name + " is locked from "
                            + time[0] + " to " + time[1] + "!");
        turnstile.save();
    }

    /**
     * Initiates a Conversation for modifying the cooldown of a Turnstile
     *
     * @param player The Player Setting the cooldown
     * @param name The name of the Turnstile being modified
     */
    private static void cooldown(Player player, String name) {
        //Cancel if the Player does not have permission to use the command
        if (!TurnstileMain.hasPermission(player, "set.cooldown")) {
            player.sendMessage(TurnstileMessages.permission);
            return;
        }

        Turnstile turnstile = getTurnstile(player, name);
        if (turnstile == null) {
            return;
        }

        //Cancel if the Player does not own the Turnstile
        if (!turnstile.isOwner(player)) {
            player.sendMessage("Only the Turnstile Owner can do that.");
            return;
        }

        new CooldownConvo(player, turnstile);
    }

    /**
     * Allows the Owner to collect items from the target Chest
     *
     * @param player The Player collecting the items
     */
    private static void collect(Player player) {
        //Cancel if the Player does not have permission to use the command
        if (!TurnstileMain.hasPermission(player, "collect")) {
            player.sendMessage(TurnstileMessages.permission);
            return;
        }

        Block block = player.getTargetBlock(LINK_TRANSPARENT, 10);

        //Cancel if a Chest is not targeted
        if (block.getTypeId() != 54) {
            player.sendMessage("You must target the Turnstile Chest you wish to collect from.");
            return;
        }

        Turnstile turnstile = TurnstileMain.findTurnstile(block);

        //Cancel if the Chest is not linked to a Turnstile
        if (turnstile == null) {
            player.sendMessage("You must target the Turnstile Chest you wish to collect from.");
            return;
        }

        //Cancel if the Player does not own the Turnstile
        if (!turnstile.isOwner(player)) {
            player.sendMessage("Only the Turnstile Owner can do that.");
            return;
        }

        turnstile.collect(player);
    }

    /**
     * Displays a list of current Turnstiles
     *
     * @param player The Player requesting the list
     */
    private static void list(Player player) {
        //Cancel if the Player does not have permission to use the command
        if (!TurnstileMain.hasPermission(player, "list")) {
            player.sendMessage(TurnstileMessages.permission);
            return;
        }

        String list = "Current Turnstiles:  ";

        for (Turnstile turnstile: TurnstileMain.getTurnstiles()) {
            list = list.concat(turnstile.name + ", ");
        }

        player.sendMessage(list.substring(0, list.length() - 2));
    }

    /**
     * Displays the info of the specified Turnstile
     * If a name is not provided, the Turnstile of the target Block is used
     *
     * @param player The Player requesting the info
     * @param name The name of the Turnstile
     */
    private static void info(Player player, String name) {
        //Cancel if the Player does not have permission to use the command
        if (!TurnstileMain.hasPermission(player, "info")) {
            player.sendMessage(TurnstileMessages.permission);
            return;
        }

        Turnstile turnstile = getTurnstile(player, name);
        if (turnstile == null) {
            return;
        }

        //Cancel if the Player does not own the Turnstile
        if (!turnstile.isOwner(player)) {
            player.sendMessage("Only the Turnstile Owner can do that.");
            return;
        }

        player.sendMessage("Turnstile Info");
        player.sendMessage("Name: " + turnstile.name);
        player.sendMessage("Owner: " + turnstile.owner);
        player.sendMessage("Location: " + turnstile.world + "'" + turnstile.x
                            + "'" + turnstile.y + "'" + turnstile.z);
        player.sendMessage("Items: " + turnstile.itemsToInfoString());

        //Only display if an Economy plugin is present
        if (Econ.economy != null) {
            player.sendMessage("Price: " + Econ.format(turnstile.price)
                                + ", NoFraud: " + turnstile.noFraud);
            player.sendMessage("MoneyEarned: " + turnstile.moneyEarned
                                + ", ItemsEarned: " + turnstile.itemsEarned);
            player.sendMessage("Free: " + turnstile.freeStart
                                + "-" + turnstile.freeEnd);
        }

        player.sendMessage("Locked: " + turnstile.lockedStart
                            + "-" + turnstile.lockedEnd);
        player.sendMessage("NoFraud: " + turnstile.noFraud);

        if (turnstile.access == null) {
            player.sendMessage("Access: Public");
        } else if (turnstile.access.isEmpty()) {
            player.sendMessage("Access: Private");
        } else {
            player.sendMessage("Access: " + turnstile.access.toString());
        }

        String buttons = "Buttons:  ";
        for (TurnstileButton button: turnstile.buttons) {
            buttons.concat(button.toString() + ", ");
        }

        player.sendMessage(buttons.substring(0, buttons.length() - 2));
    }

    /**
     * Changes the name of the specified Turnstile
     * If a name is not provided, the Turnstile of the target Block is modified
     *
     * @param player The Player renaming the Turnstile
     * @param name The name of the Turnstile being renamed
     * @param newName The new name
     */
    private static void rename(Player player, String name, String newName) {
        //Cancel if the Player does not have permission to use the command
        if (!TurnstileMain.hasPermission(player, "make")) {
            player.sendMessage(TurnstileMessages.permission);
            return;
        }

        Turnstile turnstile = TurnstileMain.findTurnstile(newName);

        //Cancel if the Turnstile already exists
        if (turnstile != null) {
            player.sendMessage("Turnstile " + newName + " already exists.");
            return;
        }

        turnstile = getTurnstile(player, name);
        if (turnstile == null) {
            return;
        }

        //Cancel if the Player does not own the Turnstile
        if (!turnstile.isOwner(player)) {
            player.sendMessage("Only the Turnstile owner can do that.");
            return;
        }

        //Remove the Turnstile that will be renamed
        TurnstileMain.removeTurnstile(turnstile);
        turnstile.name = newName;

        //Readd the Turnstile with the new name
        TurnstileMain.addTurnstile(turnstile);

        player.sendMessage("Turnstile " + name + " renamed to " + newName + ".");
    }

    /**
     * Displays the Turnstile Help Page to the given Player
     *
     * @param player The Player needing help
     */
    private static void sendHelp(Player player) {
        player.sendMessage("§e     Turnstile Help Page:");
        player.sendMessage("§2/"+command+" list§b List all Turnstiles");
        player.sendMessage("§2/"+command+" info [Name])§b Display info of Turnstile");
        player.sendMessage("§2/"+command+" help create§b Display Turnstile Create Help Page");
        player.sendMessage("§2/"+command+" help setup§b Display Turnstile Setup Help Page");
        player.sendMessage("§2/"+command+" help sign§b Display Turnstile Sign Help Page");
        player.sendMessage("§2/"+command+" rl§b Reload Turnstile Plugin");
    }

    /**
     * Displays the Turnstile Create Help Page to the given Player
     *
     * @param player The Player needing help
     */
    private static void sendCreateHelp(Player player) {
        player.sendMessage("§e     Turnstile Create Help Page:");
        player.sendMessage("§2/"+command+" make <Name>§b Make target Block into a Turnstile");
        player.sendMessage("§2/"+command+" rename [Name] <NewName>§b Rename a Turnstile");
        player.sendMessage("§2/"+command+" link <Name>§b Link target Block with Turnstile");
        if (TurnstileMain.citizens) {
            player.sendMessage("§2/"+command+" link <Name> <NPC-UID>§b Link NPC with Turnstile");
        }
        player.sendMessage("§2/"+command+" unlink§b Unlink target Block with Turnstile");
        player.sendMessage("§2/"+command+" delete [Name]§b Delete Turnstile");
    }

    /**
     * Displays the Turnstile Setup Help Page to the given Player
     *
     * @param player The Player needing help
     */
    private static void sendSetupHelp(Player player) {
        player.sendMessage("§e     Turnstile Setup Help Page:");
        player.sendMessage("§2/"+command+" price [Name] <Price>§b Set cost of Turnstile");
        player.sendMessage("§2/"+command+" price§b Set cost to items in target Chest");
        player.sendMessage("§2/"+command+" price [Name] <Amount> <Item> [Durability]§b Set cost to item");
        player.sendMessage("§2/"+command+" price [Name] <Amount> <Item> <Enchantment1&Enchantment2...>§b Set cost to item");
        player.sendMessage("§2/"+command+" nofraud [Name] <true|false>§b Set noFraud mode");
        player.sendMessage("§2/"+command+" access [Name] public§b Allow anyone to open the Turnstile");
        player.sendMessage("§2/"+command+" access [Name] private§b Allow only the Owner to open");
        player.sendMessage("§2/"+command+" access [Name] <Group1,Group2,...>");
        player.sendMessage("§bAllow only specific Groups to open the Turnstile");
        player.sendMessage("§2/"+command+" free [Name] <StartTick>-<EndTick>§b Free during timespan");
        player.sendMessage("§2/"+command+" locked [Name] <StartTick>-<EndTick>§b Locked for timespan");
        player.sendMessage("§2/"+command+" cooldown [Name]§b Set the cooldown options");
        player.sendMessage("§2/"+command+" allow <Amount>§b Allow only x Players per cooldown");
        player.sendMessage("§2/"+command+" locked [Name] <StartTick>-<EndTick>§b Locked for timespan");
        player.sendMessage("§2/"+command+" collect§b Retrieve items from the target Turnstile chest");
        player.sendMessage("§2/"+command+" owner [Name] <Player>§b Send money for Turnstile to Player");
        player.sendMessage("§2/"+command+" bank [Name] <Bank>§b Send money for Turnstile to Bank");
    }

    /**
     * Displays the Turnstile Sign Help Page to the given Player
     *
     * @param player The Player needing help
     */
    private static void sendSignHelp(Player player) {
        player.sendMessage("§e     Turnstile Sign Help Page:");
        player.sendMessage("§2Turnstile Signs can automatically update information");
        player.sendMessage("§2Each Sign can display two pieces of information such as:");
        player.sendMessage("§2Name:§b The name of the Turnstile");
        player.sendMessage("§2Price:§b The amount of money to use the Turnstile");
        player.sendMessage("§2Cost:§b The item cost to use the Turnstile");
        player.sendMessage("§2Counter:§b The amount of Players who used the Turnstile");
        player.sendMessage("§2Money:§b The amount of money the Turnstile has earned");
        player.sendMessage("§2Items:§b The amount of items the Turnstile has earned");
        player.sendMessage("§2Access:§b Whether the Turnstile is public or private");
        player.sendMessage("§2Status:§b Whether the Turnstile is open, free, or locked");
        player.sendMessage("§2Turnstile Signs are created using the following format:");
        player.sendMessage("§b    "+command+" link");
        player.sendMessage("§b  <Turnstile Name>");
        player.sendMessage("§b<Information type 1>");
        player.sendMessage("§b<Information type 2>");
    }

    /**
     * Returns the wanted Turnstile if it exists based on the given name and Player
     *
     * @param player The given Player who may be targeting a block linked to a Turnstile
     * @param name The given name of the Turnstile (null if the target block is wanted
     * @return The Turnstile if it exists
     */
    private static Turnstile getTurnstile(Player player, String name) {
        Turnstile turnstile;

        if (name == null) {
            //Find the Turnstile using the target Block
            turnstile = TurnstileMain.findTurnstile(player.getTargetBlock(TRANSPARENT, 10));

            if (turnstile == null ) {
                //Turnstile does not exist
                player.sendMessage("Target Block is not linked to a Turnstile");
            }
        } else {
            //Find the Turnstile using the given name
            turnstile = TurnstileMain.findTurnstile(name);

            if (turnstile == null ) {
                //Turnstile does not exist
                player.sendMessage("Turnstile " + name + " does not exsist.");
            }
        }

        return turnstile;
    }

    /**
     * Retrieves Enchantments from the given string
     *
     * @param string The String of the Enchantments
     * @return The Enchantments of the item
     */
    public static LinkedList<Enchantment> getEnchantments(String string) {
        LinkedList<Enchantment> enchantments = new LinkedList<Enchantment>();

        for (String split: string.split("&")) {
            for (Enchantment enchantment: Enchantment.values()) {
                if (enchantment.getName().equalsIgnoreCase(split)) {
                    enchantments.add(enchantment);
                }
            }
        }

        return enchantments;
    }

    /**
     * Retrieves a short value from the given string
     *
     * @param player The Player that will receive error messages
     * @param string The String that contains the item
     */
    public static short getData(Player player, String string) {
        if (!string.contains(":")) {
            return 0;
        }

        string = string.substring(string.indexOf(':') + 1);
        short data;

        try {
            data = Short.parseShort(string);
        } catch (Exception notShort) {
            if (player != null) {
                player.sendMessage(string+" is not a valid enchantment or data value");
            }
            return -1;
        }

        return data;
    }
}

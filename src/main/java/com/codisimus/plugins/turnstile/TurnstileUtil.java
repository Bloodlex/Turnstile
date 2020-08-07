package com.codisimus.plugins.turnstile;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.material.Gate;
import org.bukkit.material.Openable;
import org.bukkit.material.TrapDoor;

/**
 * @author Codisimus
 */
public class TurnstileUtil {
    public static void openDoor(Block block) {
        switch (block.getType()) {
            case OAK_FENCE:
            case ACACIA_FENCE:
            case JUNGLE_FENCE:
            case SPRUCE_FENCE:
            case BIRCH_FENCE:
            case DARK_OAK_FENCE:
                block.setType(Material.AIR);
                break;
            case OAK_TRAPDOOR:
            case ACACIA_TRAPDOOR:
            case JUNGLE_TRAPDOOR:
            case SPRUCE_TRAPDOOR:
            case BIRCH_TRAPDOOR:
            case DARK_OAK_TRAPDOOR:
                BlockState state = block.getState();
                TrapDoor trapDoor = (TrapDoor) state.getData();
                trapDoor.setOpen(true);
                state.update();
                break;
            case OAK_FENCE_GATE:
            case ACACIA_FENCE_GATE:
            case JUNGLE_FENCE_GATE:
            case SPRUCE_FENCE_GATE:
            case BIRCH_FENCE_GATE:
            case DARK_OAK_FENCE_GATE:
                BlockState gateState = block.getState();
                Gate fenceGate = (Gate) gateState.getData();
                fenceGate.setOpen(true);
                gateState.update();
                break;
            case IRON_DOOR:
            case OAK_DOOR:
            case ACACIA_DOOR:
            case JUNGLE_DOOR:
            case SPRUCE_DOOR:
            case BIRCH_DOOR:
            case DARK_OAK_DOOR:
                block = getBottomHalf(block);
                BlockData data = block.getBlockData();
                if (isDoorClosed(block)) {
                    block.setBlockData(data, true);
                    block.getWorld().playEffect(block.getLocation(), Effect.DOOR_TOGGLE, 0);
                }
                break;
            default:
                //Not supported
                break;
        }
    }

    public static void closeDoor(Block block) {
        switch (block.getType()) {
            case AIR:
                block.setType(Material.OAK_FENCE);
                break;
            case OAK_TRAPDOOR:
            case ACACIA_TRAPDOOR:
            case JUNGLE_TRAPDOOR:
            case SPRUCE_TRAPDOOR:
            case BIRCH_TRAPDOOR:
            case DARK_OAK_TRAPDOOR:
            case OAK_FENCE_GATE:
            case ACACIA_FENCE_GATE:
            case JUNGLE_FENCE_GATE:
            case SPRUCE_FENCE_GATE:
            case BIRCH_FENCE_GATE:
            case DARK_OAK_FENCE_GATE:
                BlockState state = block.getState();
                Openable door = (Openable) state.getData();
                door.setOpen(false);
                state.update();
                break;
            case IRON_DOOR:
            case OAK_DOOR:
            case ACACIA_DOOR:
            case JUNGLE_DOOR:
            case SPRUCE_DOOR:
            case BIRCH_DOOR:
            case DARK_OAK_DOOR:
                block = getBottomHalf(block);
                BlockData data = block.getBlockData();
                if (!isDoorClosed(block)) {
                    block.setBlockData(data, true);
                    block.getWorld().playEffect(block.getLocation(), Effect.DOOR_TOGGLE, 0);
                }
                break;
            default:
                //Not supported
                break;
        }
    }

    public static boolean isDoorClosed(Block block) {
        byte data = block.getData();
        if ((data & 0x8) == 0x8) {
            block = block.getRelative(BlockFace.DOWN);
            data = block.getData();
        }
        return ((data & 0x4) == 0);
    }

    public static Block getBottomHalf(Block block) {
        switch (block.getType()) {
            case IRON_DOOR:
            case OAK_DOOR:
            case ACACIA_DOOR:
            case JUNGLE_DOOR:
            case SPRUCE_DOOR:
            case BIRCH_DOOR:
            case DARK_OAK_DOOR:
                byte data = block.getData();
                if ((data & 0x8) == 0x8) {
                    block = block.getRelative(BlockFace.DOWN);
                }
                break;
            default:
                break;
        }
        return block;
    }
}

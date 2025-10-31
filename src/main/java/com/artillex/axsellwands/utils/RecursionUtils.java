package com.artillex.axsellwands.utils;

import org.bukkit.block.ShulkerBox;
import org.bukkit.inventory.BlockStateMeta;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Utility to recursively flatten inventories and container ItemStacks (e.g., shulker boxes).
 *
 * Usage:
 *  List<ItemStack> items = RecursionUtils.flattenInventoryRecursive(inv, maxDepth);
 *
 * Notes:
 * - Treats container items that use BlockStateMeta with a ShulkerBox block state.
 * - Limits recursion with maxDepth (0 = only top-level contents; 1 = one level of nesting, etc.).
 * - Attempts a best-effort protection against infinite loops using identity hash tracking.
 */
public final class RecursionUtils {

    private RecursionUtils() {}

    /**
     * Flatten an Inventory into a list of ItemStacks by recursively extracting items inside recognized
     * container ItemStacks (currently supports ShulkerBox via BlockStateMeta).
     *
     * @param inv the starting inventory (may be null)
     * @param maxDepth maximum recursion depth (0 = only top-level)
     * @return flattened list of ItemStacks; null slots are skipped
     */
    public static List<ItemStack> flattenInventoryRecursive(Inventory inv, int maxDepth) {
        List<ItemStack> result = new ArrayList<>();
        if (inv == null) return result;
        Set<Integer> seen = new HashSet<>();
        for (ItemStack item : inv.getContents()) {
            if (item == null) continue;
            flattenItem(item, result, 0, maxDepth, seen);
        }
        return result;
    }

    private static void flattenItem(ItemStack item, List<ItemStack> out, int depth, int maxDepth, Set<Integer> seen) {
        if (item == null) return;

        // Reentrancy / infinite loop protection: use identity hash (best-effort)
        int id = System.identityHashCode(item);
        if (seen.contains(id)) {
            out.add(item);
            return;
        }

        // If reached max depth, treat as a leaf
        if (depth > maxDepth) {
            out.add(item);
            return;
        }

        seen.add(id);

        // If item has BlockStateMeta and is a ShulkerBox, extract its inventory and recurse
        if (item.hasItemMeta() && item.getItemMeta() instanceof BlockStateMeta) {
            BlockStateMeta meta = (BlockStateMeta) item.getItemMeta();
            if (meta.getBlockState() instanceof ShulkerBox) {
                ShulkerBox box = (ShulkerBox) meta.getBlockState();
                Inventory boxInv = box.getInventory();
                for (ItemStack inner : boxInv.getContents()) {
                    if (inner == null) continue;
                    flattenItem(inner, out, depth + 1, maxDepth, seen);
                }
                // Optionally include the container item itself (uncomment if desired):
                // out.add(item);
                return;
            }
            // Other BlockStateMeta types can be added here if needed
        }

        // Not a recognized container -> add the item as-is
        out.add(item);
    }
}
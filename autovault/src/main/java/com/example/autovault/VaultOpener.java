package com.example.autovault;

import com.example.autovault.config.AutoVaultConfig;
import net.minecraft.block.BlockState;
import net.minecraft.block.VaultBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.Set;

/**
 * Watches the player's inventory and nearby ground items for the "trigger"
 * items (Heavy Core / Enchanted Golden Apple / Trident). When one shows up,
 * it auto-opens a vault, but ONLY if both of these are true:
 *
 *   1. The correct key (Ominous Trial Key / Trial Key) is the item you are
 *      CURRENTLY HOLDING in your main hand — the mod never swaps items into
 *      your hand for you.
 *   2. You are directly looking at a matching vault (i.e. it's what your
 *      crosshair is on), within normal interact reach — same as if you'd
 *      right-clicked it yourself.
 */
public final class VaultOpener {

    private VaultOpener() {}

    private static int lastHeavyCoreCount = -1;
    private static int lastGodAppleCount = -1;
    private static int lastTridentCount = -1;

    private static final Set<Integer> seenItemEntityIds = new HashSet<>();

    public static void onClientTick(MinecraftClient client) {
        AutoVaultConfig cfg = AutoVaultConfig.get();
        if (!cfg.enabled) return;

        ClientPlayerEntity player = client.player;
        World world = client.world;
        if (player == null || world == null) return;

        Item ominousTriggerItem = (cfg.ominousTrigger == AutoVaultConfig.OminousTrigger.HEAVY_CORE)
                ? Items.HEAVY_CORE
                : Items.ENCHANTED_GOLDEN_APPLE;

        // --- Inventory-based detection -------------------------------------------------
        int heavyCoreCount = countInInventory(player.getInventory(), Items.HEAVY_CORE);
        int godAppleCount = countInInventory(player.getInventory(), Items.ENCHANTED_GOLDEN_APPLE);
        int tridentCount = countInInventory(player.getInventory(), Items.TRIDENT);

        boolean ominousInventoryTrigger = false;
        if (cfg.ominousTrigger == AutoVaultConfig.OminousTrigger.HEAVY_CORE) {
            if (lastHeavyCoreCount >= 0 && heavyCoreCount > lastHeavyCoreCount) ominousInventoryTrigger = true;
        } else {
            if (lastGodAppleCount >= 0 && godAppleCount > lastGodAppleCount) ominousInventoryTrigger = true;
        }
        boolean tridentInventoryTrigger = cfg.watchTridentForNormalVault
                && lastTridentCount >= 0 && tridentCount > lastTridentCount;

        lastHeavyCoreCount = heavyCoreCount;
        lastGodAppleCount = godAppleCount;
        lastTridentCount = tridentCount;

        // --- Ground item detection ------------------------------------------------------
        boolean ominousGroundTrigger = false;
        boolean tridentGroundTrigger = false;

        Box searchBox = player.getBoundingBox().expand(cfg.itemDetectionRadius);
        for (ItemEntity itemEntity : world.getEntitiesByClass(ItemEntity.class, searchBox, e -> true)) {
            if (seenItemEntityIds.contains(itemEntity.getId())) continue;
            ItemStack stack = itemEntity.getStack();
            if (stack.isOf(ominousTriggerItem)) {
                ominousGroundTrigger = true;
                seenItemEntityIds.add(itemEntity.getId());
            } else if (cfg.watchTridentForNormalVault && stack.isOf(Items.TRIDENT)) {
                tridentGroundTrigger = true;
                seenItemEntityIds.add(itemEntity.getId());
            }
        }
        // Keep the "seen" set from growing forever.
        if (seenItemEntityIds.size() > 4096) {
            seenItemEntityIds.clear();
        }

        if (ominousInventoryTrigger || ominousGroundTrigger) {
            attemptOpenVault(client, player, world, true, Items.OMINOUS_TRIAL_KEY);
        }
        if (tridentInventoryTrigger || tridentGroundTrigger) {
            attemptOpenVault(client, player, world, false, Items.TRIAL_KEY);
        }
    }

    private static int countInInventory(PlayerInventory inv, Item item) {
        int count = 0;
        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getStack(i);
            if (stack.isOf(item)) count += stack.getCount();
        }
        return count;
    }

    private static void attemptOpenVault(MinecraftClient client, ClientPlayerEntity player, World world,
                                          boolean wantOminous, Item keyItem) {
        // Condition 1: the key has to be what you're actually holding right now.
        ItemStack heldStack = player.getMainHandStack();
        if (!heldStack.isOf(keyItem)) {
            return; // silently do nothing — this fires every tick, so no spammy message here
        }

        // You have to be directly looking at a matching vault.
        BlockPos targetPos = findLookedAtVault(client, world, wantOminous);
        if (targetPos == null) {
            return; // holding the right key, but not aimed at a qualifying vault
        }

        Direction face = Direction.UP;
        Vec3d hitPos = Vec3d.ofCenter(targetPos).add(0, 0.5, 0);
        BlockHitResult hitResult = new BlockHitResult(hitPos, face, targetPos, false);

        if (client.interactionManager != null) {
            client.interactionManager.interactBlock(player, Hand.MAIN_HAND, hitResult);
        }
        player.swingHand(Hand.MAIN_HAND);
    }

    /**
     * Looks for a matching vault your crosshair is currently on, within
     * normal interaction reach. This covers "I'm looking right at it".
     */
    private static BlockPos findLookedAtVault(MinecraftClient client, World world, boolean wantOminous) {
        HitResult target = client.crosshairTarget;
        if (!(target instanceof BlockHitResult blockHit)) return null;
        if (target.getType() != HitResult.Type.BLOCK) return null;

        BlockPos pos = blockHit.getBlockPos();
        if (!isMatchingVault(world, pos, wantOminous)) return null;
        return pos;
    }

    private static boolean isMatchingVault(World world, BlockPos pos, boolean wantOminous) {
        BlockState state = world.getBlockState(pos);
        if (!(state.getBlock() instanceof VaultBlock)) return false;
        try {
            return state.get(VaultBlock.OMINOUS) == wantOminous;
        } catch (Exception e) {
            // Property name changed between versions; treat as no match
            // rather than crash.
            return false;
        }
    }
}

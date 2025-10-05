package com.github.wallev.carvein.event;

import com.github.wallev.carvein.util.FoodListUtil;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.decoration.TableBlockEntity;
import com.simibubi.create.AllBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * 事件监听器类 - 处理玩家标记请求位置和制作食物列表的功能
 * 提供两个主要功能：
 * 1. 标记桌子或容器位置作为订单交付点
 * 2. 从容器中提取食物列表到剪贴板
 */
@Mod.EventBusSubscriber
public class MarkRequestTablePosEvent {
    // 用于在玩家持久数据中存储桌子位置的标签名
    public static final String TABLE_POS_TAG = "tablepos";

    /**
     * 处理玩家右键点击方块事件，用于标记桌子或物品容器位置
     * 当玩家手持剪贴板点击桌子或带有物品栏的方块时，会记录该位置
     */
    @SubscribeEvent
    public static void caller(PlayerInteractEvent.RightClickBlock event) {
        // 首先尝试执行制作食物列表的功能，如果成功则直接返回
        if (makeFoods(event)) {
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
            return;
        }

        Player player = event.getEntity();
        BlockPos pos = event.getPos();
        // 检查玩家主手物品是否为剪贴板
        if (!player.getMainHandItem().is(AllBlocks.CLIPBOARD.asItem())) {
            return;
        }

        // 获取点击位置的方块实体
        BlockEntity blockEntity1 = player.level().getBlockEntity(pos);
        if (blockEntity1 == null) {
            return;
        }

        // 如果方块实体是桌子，记录桌子位置
        if (blockEntity1 instanceof TableBlockEntity tableBlockEntity) {
            player.getPersistentData().put(TABLE_POS_TAG, NbtUtils.writeBlockPos(pos));
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
            return;
        }

        // 尝试获取方块的物品栏能力
        IItemHandler iItemHandler = blockEntity1.getCapability(ForgeCapabilities.ITEM_HANDLER).orElse(null);
        if (iItemHandler == null) {
            return;
        }
        // 记录带有物品栏的方块位置
        player.getPersistentData().put(TABLE_POS_TAG, NbtUtils.writeBlockPos(pos));
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }

    /**
     * 处理玩家使用剪贴板shift+右键点击方块时的食物列表制作功能
     * 从方块的物品栏中提取食物并写入剪切板——菜单
     *
     * @param event 玩家右键点击方块事件
     * @return 如果成功制作食物列表则返回true，否则返回false
     */
    public static boolean makeFoods(PlayerInteractEvent.RightClickBlock event) {
        BlockPos pos = event.getPos();
        Player player = event.getEntity();
        // 检查是否按下了Shift键
        if (!player.isShiftKeyDown()) {
            return false;
        }

        // 检查玩家主手物品是否为剪贴板
        if (!player.getMainHandItem().is(AllBlocks.CLIPBOARD.asItem())) {
            return false;
        }

        // 获取点击位置的方块实体
        BlockEntity blockEntity = player.level().getBlockEntity(pos);
        if (blockEntity == null) {
            return false;
        }

        // 尝试获取方块的物品栏能力
        IItemHandler iItemHandler = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).orElse(null);
        if (iItemHandler == null) {
            return false;
        }

        // 收集物品栏中的所有非空物品堆叠
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < iItemHandler.getSlots(); i++) {
            ItemStack stackInSlot = iItemHandler.getStackInSlot(i);
            if (stackInSlot.isEmpty())
                continue;
            items.add(stackInSlot);
        }

        // 将收集的食物列表写入剪贴板
        FoodListUtil.writeFoods(items, player);
        return true;
    }
}
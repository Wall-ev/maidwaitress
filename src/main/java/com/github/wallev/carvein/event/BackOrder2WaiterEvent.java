package com.github.wallev.carvein.event;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.wallev.carvein.init.ModEntities;
import com.github.wallev.carvein.util.FoodListUtil;
import com.github.wallev.carvein.waitress.WaitressTask;
import com.google.common.collect.Lists;
import com.simibubi.create.AllBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.ItemHandlerHelper;
import studio.fantasyit.maid_storage_manager.maid.task.StorageManageTask;

import java.util.Comparator;
import java.util.List;

/**
 * 事件监听器类 - 处理玩家向服务员女仆提交订单的功能
 * 使用剪贴板作为订单提交工具，让服务员女仆从存储管理女仆处获取食物
 */
@Mod.EventBusSubscriber
public class BackOrder2WaiterEvent {

    /**
     * 处理玩家使用剪贴板右键点击服务员女仆时的食物请求逻辑
     * 当条件满足时，会设置女仆的AI记忆，使其前往指定位置或玩家处
     */
    @SubscribeEvent
    public static void requestFood(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        Entity target = event.getTarget();

        ItemStack itemStack = event.getItemStack();
        // 检查条件：手持剪贴板、点击的是女仆实体、女仆是服务员、女仆具有调整行走的记忆
        if (itemStack.is(AllBlocks.CLIPBOARD.asItem()) &&
                target instanceof EntityMaid maid0 &&
                maid0.getTask().getUid().equals(WaitressTask.UID) &&
                maid0.getBrain().hasMemoryValue(ModEntities.REJUST_WLAK.get())) {
            // 获取玩家当前位置，并设置搜索范围
            BlockPos playerPos = player.blockPosition();
            int offset = 32;
            AABB aabb = new AABB(playerPos.getX() - offset, playerPos.getY() - offset, playerPos.getZ() - offset,
                    playerPos.getX() + offset, playerPos.getY() + offset, playerPos.getZ() + offset);
            // 在范围内搜索最近的存储管理女仆
            player.level().getEntitiesOfClass(EntityMaid.class, aabb).stream()
                    .filter(maid -> maid.getTask().getUid().equals(StorageManageTask.TASK_ID))
                    .filter(maid -> maid0.isWithinRestriction(maid.blockPosition()))
                    .min(Comparator.comparingDouble(maid -> maid.distanceToSqr(playerPos.getCenter())))
                    .stream()
                    .findFirst()
                    .ifPresent(maid -> {
                        // 设置服务员女仆的AI记忆，包括请求目标、行走目标和注视目标
                        Brain<EntityMaid> brain = maid0.getBrain();
                        brain.setMemory(ModEntities.REQUEST_MAID.get(), maid);
                        brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(maid, 0.7f, 2));
                        brain.setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(maid, true));

                        // 清除调整行走的记忆，并从剪贴板获取食物列表
                        brain.eraseMemory(ModEntities.REJUST_WLAK.get());
                        List<ItemStack> foodList = FoodListUtil.getFoodList(itemStack);
                        brain.setMemory(ModEntities.REQUEST_FOODS.get(), Lists.newArrayList(foodList));

                        // 将剪贴板插入女仆的可用物品栏
                        ItemHandlerHelper.insertItemStacked(maid0.getAvailableInv(true), itemStack.split(1), false);

                        // 尝试从玩家持久数据中读取桌子位置，如果存在则设置，否则设置玩家为目标
                        Tag tag = player.getPersistentData().get(MarkRequestTablePosEvent.TABLE_POS_TAG);
                        if (tag instanceof CompoundTag compoundTag) {
                            BlockPos blockPos = NbtUtils.readBlockPos(compoundTag);
                            brain.setMemory(ModEntities.REQUEST_PLACE_POS.get(), blockPos);
                        } else {
                            brain.setMemory(ModEntities.REQUEST_PLACE_PLAYER.get(), player);
                        }

                        // 设置事件结果为成功并取消事件
                        event.setCancellationResult(InteractionResult.SUCCESS);
                        event.setCanceled(true);
                    });
        }
    }
}
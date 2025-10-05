package com.github.wallev.carvein.waitress.behavior;

import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidCheckRateTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.util.ItemsUtil;
import com.github.wallev.carvein.init.ModEntities;
import com.github.wallev.carvein.util.FoodListUtil;
import com.github.wallev.maidsoulkitchen.util.TileUtil;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.decoration.TableBlockEntity;
import com.google.common.collect.ImmutableMap;
import com.simibubi.create.AllBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.List;
/**
 * 服务员女仆将食物送到餐桌任务类
 * 负责将厨师制作好的食物从工作台取出并放置到指定的餐桌上
 */
public class TakeFood2TableTask extends MaidCheckRateTask {
    /**
     * 构造函数，设置任务启动所需的记忆模块条件
     * 需要满足：行走目标已设置、请求食物列表已设置、请求放置位置已设置、
     * 工作位置已设置、等待食物状态为不存在（即食物已准备好）
     */
    public TakeFood2TableTask() {
        super(ImmutableMap.of(
                MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_PRESENT,
                ModEntities.REQUEST_FOODS.get(), MemoryStatus.VALUE_PRESENT,
                ModEntities.REQUEST_PLACE_POS.get(), MemoryStatus.VALUE_PRESENT,
                ModEntities.WORK_POS.get(), MemoryStatus.VALUE_PRESENT,
                ModEntities.WAIT_TO_FOOD.get(), MemoryStatus.VALUE_ABSENT
        ));
    }

    /**
     * 检查任务启动的额外条件
     * 判断女仆是否已到达目标位置附近（3.2格范围内）
     *
     * @param worldIn 服务端世界实例
     * @param maid 执行任务的女仆实体
     * @return 是否满足额外启动条件
     */
    @Override
    protected boolean checkExtraStartConditions(ServerLevel worldIn, EntityMaid maid) {
        Brain<EntityMaid> brain = maid.getBrain();
        return brain.getMemory(MemoryModuleType.WALK_TARGET).map(targetPos -> {
            Vec3 targetV3d = targetPos.getTarget().currentPosition();
            if (maid.distanceToSqr(targetV3d) > Math.pow(3.2, 2)) {
                return false;
            }
            return true;
        }).orElse(false);
    }

    /**
     * 任务开始执行的逻辑
     * 1. 获取请求放置食物的餐桌位置
     * 2. 检查餐桌位置和方块实体是否有效
     * 3. 获取餐桌的物品处理器
     * 4. 获取请求食物列表并验证其有效性
     * 5. 从剪贴板获取食物清单并查找可用食物
     * 6. 将可用食物放置到餐桌上并更新请求列表
     * 7. 播放放置动作和音效
     * 8. 根据请求列表是否为空来决定清理记忆或继续等待更多食物
     *
     * @param worldIn 服务端世界实例
     * @param maid 执行任务的女仆实体
     * @param gameTimeIn 当前游戏时间
     */
    @Override
    protected void start(ServerLevel worldIn, EntityMaid maid, long gameTimeIn) {
        BlockPos tablePos = maid.getBrain().getMemory(ModEntities.REQUEST_PLACE_POS.get())
                .orElse(null);

        // 检查餐桌位置是否有效，无效则清理记忆并结束任务
        if (tablePos == null) {
            maid.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
            maid.getBrain().eraseMemory(ModEntities.REQUEST_PLACE_POS.get());
            maid.getBrain().eraseMemory(ModEntities.WORK_POS.get());
            return;
        }

        // 检查餐桌方块实体是否存在，不存在则清理记忆并结束任务
        BlockEntity table = worldIn.getBlockEntity(tablePos);
        if (table == null) {
            maid.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
            maid.getBrain().eraseMemory(ModEntities.REQUEST_PLACE_POS.get());
            maid.getBrain().eraseMemory(ModEntities.WORK_POS.get());
            return;
        }

        // 获取餐桌的物品处理器
        IItemHandler iItemHandler = table.getCapability(ForgeCapabilities.ITEM_HANDLER).orElse(null);
        if (table instanceof TableBlockEntity tableBlockEntity) {
            iItemHandler = tableBlockEntity.getItems();
        }

        // 检查请求食物列表是否有效，无效则清理记忆并结束任务
        List<ItemStack> requestFoods = maid.getBrain().getMemory(ModEntities.REQUEST_FOODS.get()).orElse(null);
        if (requestFoods == null || requestFoods.isEmpty()) {
            maid.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
            maid.getBrain().eraseMemory(ModEntities.REQUEST_PLACE_POS.get());
            maid.getBrain().eraseMemory(ModEntities.WORK_POS.get());
            return;
        }

        // 获取剪贴板并提取食物列表，查找可用的食物
        ItemStack clipBoard = ItemsUtil.getStack(maid, itemStack -> itemStack.is(AllBlocks.CLIPBOARD.asItem()));
        List<ItemStack> foodList = FoodListUtil.getFoodList(clipBoard);
        List<ItemStack> availableFoods = FoodListUtil.findRequestFoods(foodList, maid);

        // 将所有可用食物放置到餐桌上，并从请求列表中移除已放置的食物
        while (!availableFoods.isEmpty()) {
            ItemStack food = availableFoods.remove(0);
            requestFoods.removeIf(itemStack -> {
                return ItemStack.isSameItemSameTags(itemStack, food);
            });
            ItemHandlerHelper.insertItemStacked(iItemHandler, food.copyAndClear(), false);
        }

        // 播放放置动作和音效，增强交互反馈
        maid.swing(InteractionHand.MAIN_HAND);
        maid.playSound(SoundEvents.ITEM_PICKUP, 1.0F, maid.getRandom().nextFloat() * 0.1F + 1.0F);
        TileUtil.makeChanged(table);

        // 清除行走目标记忆
        maid.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);

        // 根据请求列表是否为空来决定后续操作
        if (requestFoods.isEmpty()) {
            // 请求列表为空，清理所有相关记忆，任务完成
            maid.getBrain().eraseMemory(ModEntities.REQUEST_FOODS.get());
            maid.getBrain().eraseMemory(ModEntities.REQUEST_PLACE_POS.get());
            maid.getBrain().eraseMemory(ModEntities.WORK_POS.get());
            maid.getBrain().eraseMemory(ModEntities.CHEF.get());
        } else {
            // 请求列表不为空，继续等待厨师制作更多食物
            // 这里获取负责制作食物的厨师女仆，并设置等待状态
            maid.getBrain().getMemory(ModEntities.CHEF.get()).ifPresent(maid1 -> {
                BehaviorUtils.setWalkAndLookTargetMemories(maid, maid1, 0.7f, 2);
                maid.getBrain().setMemory(ModEntities.WAIT_TO_FOOD.get(), true);
            });
        }

    }
}
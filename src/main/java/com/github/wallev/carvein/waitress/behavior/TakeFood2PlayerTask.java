package com.github.wallev.carvein.waitress.behavior;

import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidCheckRateTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.util.ItemsUtil;
import com.github.wallev.carvein.init.ModEntities;
import com.github.wallev.carvein.util.FoodListUtil;
import com.google.common.collect.ImmutableMap;
import com.simibubi.create.AllBlocks;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * 服务员女仆将食物带给玩家任务类
 * 负责将厨师制作好的食物从工作台取出并交付给指定玩家
 */
public class TakeFood2PlayerTask extends MaidCheckRateTask {
    /**
     * 构造函数，设置任务启动所需的记忆模块条件
     * 需要满足：行走目标已设置、请求放置位置已设置、工作位置已设置
     */
    public TakeFood2PlayerTask() {
        super(ImmutableMap.of(
                MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_PRESENT,
                ModEntities.REQUEST_PLACE_POS.get(), MemoryStatus.VALUE_PRESENT,
                ModEntities.WORK_POS.get(), MemoryStatus.VALUE_PRESENT
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
     * 1. 获取请求放置食物的目标玩家
     * 2. 如果玩家不存在，清除相关记忆并结束任务
     * 3. 获取剪贴板并从中提取食物列表
     * 4. 查找可用的食物并逐一交付给玩家
     * 5. 播放交付动作和音效
     * 6. 清除相关记忆模块，完成任务
     *
     * @param worldIn 服务端世界实例
     * @param maid 执行任务的女仆实体
     * @param gameTimeIn 当前游戏时间
     */
    @Override
    protected void start(ServerLevel worldIn, EntityMaid maid, long gameTimeIn) {
        Player player = maid.getBrain().getMemory(ModEntities.REQUEST_PLACE_PLAYER.get())
                .orElse(null);

        // 如果目标玩家不存在，清理记忆并结束任务
        if (player == null) {
            maid.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
            maid.getBrain().eraseMemory(ModEntities.REQUEST_MAID.get());
            maid.getBrain().eraseMemory(ModEntities.WORK_POS.get());
            return;
        }

        // 获取剪贴板物品并提取食物列表
        ItemStack clipBoard = ItemsUtil.getStack(maid, itemStack -> itemStack.is(AllBlocks.CLIPBOARD.asItem()));
        List<ItemStack> foodList = FoodListUtil.getFoodList(clipBoard);

        // 查找并收集可用的食物
        List<ItemStack> availableFoods = FoodListUtil.findRequestFoods(foodList, maid);

        // 将所有可用食物逐一交付给玩家
        while (!availableFoods.isEmpty()) {
            ItemStack food = availableFoods.remove(0);
            player.addItem(food);
        }

        // 播放交付动作和音效，增强交互反馈
        maid.swing(InteractionHand.MAIN_HAND);
        maid.playSound(SoundEvents.ITEM_PICKUP, 1.0F, maid.getRandom().nextFloat() * 0.1F + 1.0F);

        // 清除相关记忆，标记任务完成
        maid.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        maid.getBrain().eraseMemory(ModEntities.REQUEST_PLACE_POS.get());
        maid.getBrain().eraseMemory(ModEntities.WORK_POS.get());

    }
}

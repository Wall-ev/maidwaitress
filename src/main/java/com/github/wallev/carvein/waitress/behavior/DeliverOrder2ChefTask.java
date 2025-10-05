package com.github.wallev.carvein.waitress.behavior;

import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidCheckRateTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.wallev.carvein.init.ModEntities;
import com.github.wallev.carvein.util.FoodListUtil;
import com.google.common.collect.ImmutableMap;
import com.simibubi.create.AllBlocks;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import studio.fantasyit.maid_storage_manager.util.InvUtil;
import studio.fantasyit.maid_storage_manager.util.RequestItemUtil;

import java.util.List;

/**
 * 服务员女仆向厨师女仆交付订单任务类
 * 负责将包含食物订单的剪贴板交付给负责制作食物的厨师女仆
 */
public class DeliverOrder2ChefTask extends MaidCheckRateTask {

    /**
     * 构造函数，设置任务启动所需的记忆模块条件
     * 需要满足：行走目标已设置、请求厨师女仆已设置
     */
    public DeliverOrder2ChefTask() {
        super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_PRESENT, ModEntities.REQUEST_MAID.get(), MemoryStatus.VALUE_PRESENT));
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
     * 1. 获取目标厨师女仆
     * 2. 清除行走目标和请求女仆记忆
     * 3. 查找并获取剪贴板物品
     * 4. 从剪贴板中提取食物列表
     * 5. 创建虚拟物品栈并尝试放置到厨师女仆物品栏
     * 6. 设置等待食物制作完成的标记
     * 7. 记录负责制作食物的厨师女仆
     *
     * @param worldIn 服务端世界实例
     * @param maid 执行任务的女仆实体
     * @param gameTimeIn 当前游戏时间
     */
    @Override
    protected void start(ServerLevel worldIn, EntityMaid maid, long gameTimeIn) {
        EntityMaid chef = maid.getBrain().getMemory(ModEntities.REQUEST_MAID.get())
                .orElse(null);
        maid.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        maid.getBrain().eraseMemory(ModEntities.REQUEST_MAID.get());

        if (chef == null) {
            return;
        }

        // 查找剪贴板物品
        ItemStack clipItem = ItemStack.EMPTY;
        CombinedInvWrapper availableInv = maid.getAvailableInv(true);
        for (int i = 0; i < availableInv.getSlots(); i++) {
            ItemStack stackInSlot = availableInv.getStackInSlot(i);
            if (availableInv.getStackInSlot(i).is(AllBlocks.CLIPBOARD.asItem())) {
                clipItem = stackInSlot;
                break;
            }
        }

        // 从剪贴板获取食物列表并创建虚拟物品栈交付给厨师女仆
        List<ItemStack> list = FoodListUtil.getFoodList(clipItem);
        ItemStack itemStack = RequestItemUtil.makeVirtualItemStack(list, null, maid, "request_food_from_srerai");
        InvUtil.tryPlace(chef.getAvailableInv(true), itemStack);

        // 设置等待食物制作完成的状态标记并记录厨师女仆
        maid.getBrain().setMemory(ModEntities.WAIT_TO_FOOD.get(), true);
        maid.getBrain().setMemory(ModEntities.CHEF.get(), chef);
    }
}
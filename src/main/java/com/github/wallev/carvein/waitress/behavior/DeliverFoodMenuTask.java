package com.github.wallev.carvein.waitress.behavior;

import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidCheckRateTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.wallev.carvein.init.ModEntities;
import com.google.common.collect.ImmutableMap;
import com.simibubi.create.AllBlocks;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;

/**
 * 服务员女仆交付食物菜单任务类
 * 负责将包含食物清单的剪贴板交付给请求的玩家
 */
public class DeliverFoodMenuTask extends MaidCheckRateTask {
    /**
     * 构造函数，设置任务启动所需的记忆模块条件
     * 需要满足：行走目标已设置、请求玩家已设置
     */
    public DeliverFoodMenuTask() {
        super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_PRESENT, ModEntities.REQUEST_PLAYER.get(), MemoryStatus.VALUE_PRESENT));
    }

    /**
     * 检查任务启动的额外条件
     * 判断女仆是否已到达目标位置附近（3.2格范围内）
     *
     * @param worldIn 服务端世界实例
     * @param maid 执行任务的女仆实体
     * @return 是否满足额外启动条件
     */
    protected boolean checkExtraStartConditions(ServerLevel worldIn, EntityMaid maid) {
        Brain<EntityMaid> brain = maid.getBrain();
        return brain.getMemory(MemoryModuleType.WALK_TARGET).map(targetPos -> {
            Vec3 targetV3d = targetPos.getTarget().currentPosition();
            return !(maid.distanceToSqr(targetV3d) > Math.pow(3.2, 2));
        }).orElse(false);
    }

    /**
     * 任务开始执行的逻辑
     * 1. 获取请求玩家
     * 2. 清除行走目标和请求玩家记忆
     * 3. 如果玩家存在，查找并交付剪贴板物品
     * 4. 设置调整行走状态的标记
     *
     * @param level 服务端世界实例
     * @param maid 执行任务的女仆实体
     * @param gameTick 当前游戏刻
     */
    @Override
    protected void start(ServerLevel level, EntityMaid maid, long gameTick) {
        Player player = maid.getBrain().getMemory(ModEntities.REQUEST_PLAYER.get())
                .orElse(null);
        maid.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        maid.getBrain().eraseMemory(ModEntities.REQUEST_PLAYER.get());
        if (player == null) {
            return;
        }

        CombinedInvWrapper availableInv = maid.getAvailableInv(true);
        for (int i = 0; i < availableInv.getSlots(); i++) {
            ItemStack stackInSlot = availableInv.getStackInSlot(i);
            if (availableInv.getStackInSlot(i).is(AllBlocks.CLIPBOARD.asItem())) {
                if (player.addItem(stackInSlot)) {
                    availableInv.setStackInSlot(i, ItemStack.EMPTY);
                    maid.swing(InteractionHand.MAIN_HAND);

                    maid.getBrain().setMemory(ModEntities.REJUST_WLAK.get(), true);
                    break;

                }
            }
        }
    }
}
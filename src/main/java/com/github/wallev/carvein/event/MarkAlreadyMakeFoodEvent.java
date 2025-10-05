package com.github.wallev.carvein.event;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.wallev.carvein.api.StorageMaidRetMaidEvent;
import com.github.wallev.carvein.init.ModEntities;
import com.github.wallev.carvein.waitress.WaitressTask;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 事件监听器类 - 处理存储女仆返回物品给服务员女仆后的状态标记与行为设置
 * 当服务员女仆从存储女仆处获得食物后，此事件会更新服务员的AI状态和行走目标
 */
@Mod.EventBusSubscriber
public class MarkAlreadyMakeFoodEvent {

    /**
     * 处理存储女仆返回物品给服务员女仆的事件
     * 该方法会清除等待食物的记忆，并根据目标位置设置行走和注视点
     */
    @SubscribeEvent
    public static void mark(StorageMaidRetMaidEvent event) {
        // 获取接收物品的目标实体
        Entity targetEntity = event.getTargetEntity();

        // 检查目标实体是否为服务员女仆
        if (targetEntity instanceof EntityMaid targetMaid && targetMaid.getTask().getUid().equals(WaitressTask.UID)) {
            Brain<EntityMaid> brain = targetMaid.getBrain();
            // 清除等待食物的记忆标记，表示已获得食物
            brain.eraseMemory(ModEntities.WAIT_TO_FOOD.get());

            // 检查是否有请求放置的位置记忆（如桌子位置）
            if (brain.hasMemoryValue(ModEntities.REQUEST_PLACE_POS.get())) {
                brain.getMemory(ModEntities.REQUEST_PLACE_POS.get()).ifPresent(pos -> {
                    // 设置前往目标位置的行走和注视记忆
                    BehaviorUtils.setWalkAndLookTargetMemories(targetMaid, pos, 0.5f, 3);
                    // 设置工作位置记忆，使用BlockPosTracker跟踪位置
                    targetMaid.getBrain().setMemory(ModEntities.WORK_POS.get(), new BlockPosTracker(pos));
                });
                return; // 处理完成，直接返回
            }

            // 如果没有位置记忆，检查是否有请求放置的玩家记忆
            if (brain.hasMemoryValue(ModEntities.REQUEST_PLACE_PLAYER.get())) {
                brain.getMemory(ModEntities.REQUEST_PLACE_PLAYER.get()).ifPresent(player -> {
                    // 设置前往玩家位置的行走和注视记忆
                    BehaviorUtils.setWalkAndLookTargetMemories(targetMaid, player, 0.5f, 3);
                    // 设置工作位置记忆，使用BlockPosTracker跟踪玩家当前位置
                    targetMaid.getBrain().setMemory(ModEntities.WORK_POS.get(), new BlockPosTracker(player.position()));
                });
                return; // 处理完成，直接返回
            }
        }
    }
}
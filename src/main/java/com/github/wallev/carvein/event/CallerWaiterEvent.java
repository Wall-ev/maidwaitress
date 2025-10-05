package com.github.wallev.carvein.event;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.wallev.carvein.init.ModEntities;
import com.github.wallev.carvein.waitress.WaitressTask;
import lekavar.lma.drinkbeer.blocks.CallBellBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Comparator;

/**
 * 事件监听器类 - 处理玩家通过呼叫铃召唤服务员女仆的功能
 * 当玩家右键点击呼叫铃时，会寻找最近的服务员女仆并使其前往玩家位置
 */
@Mod.EventBusSubscriber
public class CallerWaiterEvent {

    /**
     * 处理玩家右键点击呼叫铃方块时的事件
     * 该方法会在玩家点击呼叫铃后寻找最近的服务员女仆并设置其行动目标
     */
    @SubscribeEvent
    public static void caller(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        BlockPos pos = event.getPos();

        // 检查玩家点击的是否是呼叫铃方块
        BlockState blockState = player.level().getBlockState(pos);
        if (blockState.getBlock() instanceof CallBellBlock) {
            // 获取玩家当前位置，并设置搜索范围（32格立方体）
            BlockPos playerPos = player.blockPosition();
            int offset = 32;
            AABB aabb = new AABB(playerPos.getX() - offset, playerPos.getY() - offset, playerPos.getZ() - offset,
                    playerPos.getX() + offset, playerPos.getY() + offset, playerPos.getZ() + offset);

            // 在范围内搜索最近的服务员女仆
            player.level().getEntitiesOfClass(EntityMaid.class, aabb).stream()
                    .filter(maid -> maid.getTask().getUid().equals(WaitressTask.UID)) // 筛选任务为服务员的女仆
                    .min(Comparator.comparingDouble(maid -> maid.distanceToSqr(playerPos.getCenter()))) // 按距离排序取最近的
                    .stream()
                    .findFirst()
                    .ifPresent(maid -> {
                        // 找到女仆后，设置其AI记忆使其前往玩家位置
                        Brain<EntityMaid> brain = maid.getBrain();
                        brain.setMemory(ModEntities.REQUEST_PLAYER.get(), player); // 设置请求玩家
                        brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(player, 0.7f, 2)); // 设置行走目标
                        brain.setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(player, true)); // 设置注视目标
                    });
        }
    }
}
package com.github.wallev.carvein;

import com.github.tartaricacid.touhoulittlemaid.api.ILittleMaid;
import com.github.tartaricacid.touhoulittlemaid.api.LittleMaidExtension;
import com.github.tartaricacid.touhoulittlemaid.api.entity.ai.IExtraMaidBrain;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.ExtraMaidBrainManager;
import com.github.tartaricacid.touhoulittlemaid.entity.task.TaskManager;
import com.github.wallev.carvein.init.ModEntities;
import com.github.wallev.carvein.waitress.WaitressTask;
import com.google.common.collect.Lists;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

import java.util.List;

/**
 * 女仆插件类，实现ILittleMaid接口，用于扩展Touhou Little Maid模组的功能
 * 通过@LittleMaidExtension注解标记为小女仆扩展插件
 */
@LittleMaidExtension
public class MaidPlugin implements ILittleMaid {

    /**
     * 向小女仆的大脑添加额外的内存模块类型，用于存储服务员系统所需的各种状态和信息
     * @param manager 额外女仆大脑管理器，负责管理所有女仆的额外内存模块
     */
    @Override
    public void addExtraMaidBrain(ExtraMaidBrainManager manager) {
        // 创建并添加一个额外的女仆大脑实例，定义了服务员系统所需的全部内存模块类型
        manager.addExtraMaidBrain(new IExtraMaidBrain() {
            /**
             * 获取额外的内存模块类型列表，这些类型将被添加到女仆的大脑中
             * @return 包含所有需要的内存模块类型的列表
             */
            @Override
            public List<MemoryModuleType<?>> getExtraMemoryTypes() {
                return Lists.newArrayList(
                        ModEntities.REQUEST_PLAYER.get(),       // 请求服务的玩家
                        ModEntities.REQUEST_MAID.get(),         // 请求的目标女仆
                        ModEntities.REJUST_WLAK.get(),          // 需要重新调整行走的状态
                        ModEntities.WAIT_TO_FOOD.get(),         // 等待食物制作完成的状态
                        ModEntities.REQUEST_PLACE_PLAYER.get(), // 请求放置食物的玩家
                        ModEntities.REQUEST_PLACE_POS.get(),    // 请求放置食物的位置
                        ModEntities.REQUEST_FOODS.get(),        // 请求的食物列表
                        ModEntities.CHEF.get(),                 // 负责制作食物的厨师女仆
                        ModEntities.WORK_POS.get()              // 工作位置
                );
            }
        });
    }

    /**
     * 向小女仆的任务管理器中添加自定义任务
     * @param manager 任务管理器，负责管理所有可分配给女仆的任务
     */
    @Override
    public void addMaidTask(TaskManager manager) {
        // 添加服务员任务到任务管理器中，使女仆能够执行服务员相关的工作
        manager.add(new WaitressTask());
    }
}


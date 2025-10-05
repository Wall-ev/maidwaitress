package com.github.wallev.carvein.waitress;

import com.github.tartaricacid.touhoulittlemaid.api.task.IMaidTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.wallev.carvein.Carvein;
import com.github.wallev.carvein.init.ModEntities;
import com.github.wallev.carvein.waitress.behavior.DeliverFoodMenuTask;
import com.github.wallev.carvein.waitress.behavior.DeliverOrder2ChefTask;
import com.github.wallev.carvein.waitress.behavior.TakeFood2PlayerTask;
import com.github.wallev.carvein.waitress.behavior.TakeFood2TableTask;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 服务员女仆任务类，实现了IMaidTask接口，为女仆提供餐厅服务相关的AI行为控制。
 * 该类负责注册所有服务员相关的行为任务，包括递送菜单、传递订单、送餐到餐桌和送餐给玩家等。
 */
public class WaitressTask implements IMaidTask {
    /**
     * 服务员任务的唯一标识符，用于在游戏中区分不同的女仆任务类型
     */
    public static final ResourceLocation UID = new ResourceLocation(Carvein.MOD_ID, "watiress");

    /**
     * 获取任务的唯一标识符
     * @return 包含modid和任务名称的ResourceLocation对象
     */
    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    /**
     * 获取任务的图标物品栈，用于在游戏界面中显示该任务
     * @return 红色床的物品栈作为任务图标
     */
    @Override
    public ItemStack getIcon() {
        return Items.RED_BED.getDefaultInstance();
    }

    /**
     * 获取女仆执行该任务时的环境音效
     * @param entityMaid 执行任务的女仆实体
     * @return null，该任务没有特定的环境音效
     */
    @Override
    public @Nullable SoundEvent getAmbientSound(EntityMaid entityMaid) {
        return null;
    }

    /**
     * 创建女仆大脑中的行为任务列表，定义了服务员女仆可以执行的所有行为及其优先级
     * @param entityMaid 要创建行为任务的女仆实体
     * @return 包含优先级和行为控制对象的Pair列表
     */
    @Override
    public List<Pair<Integer, BehaviorControl<? super EntityMaid>>> createBrainTasks(EntityMaid entityMaid) {
        return Lists.newArrayList(
                Pair.of(5, new DeliverFoodMenuTask()),     // 递送食物菜单任务
                Pair.of(5, new DeliverOrder2ChefTask()),   // 向厨师交付订单任务
                Pair.of(5, new TakeFood2TableTask()),      // 将食物送到餐桌任务
                Pair.of(5, new TakeFood2PlayerTask())      // 将食物带给玩家任务
        );
    }

    /**
     * 控制女仆在执行该任务时是否可以进食
     * @param maid 执行任务的女仆实体
     * @return false，服务员女仆在工作期间不允许进食
     */
    @Override
    public boolean enableEating(EntityMaid maid) {
        return false;
    }

    /**
     * 控制女仆在执行该任务时是否可以环顾四周和随机行走
     * @param maid 执行任务的女仆实体
     * @return 如果女仆需要重新调整行走或正在等待食物，则返回false；否则返回true
     */
    @Override
    public boolean enableLookAndRandomWalk(EntityMaid maid) {
        return !(maid.getBrain().hasMemoryValue(ModEntities.REJUST_WLAK.get()) || maid.getBrain().hasMemoryValue(ModEntities.WAIT_TO_FOOD.get()));
    }
}


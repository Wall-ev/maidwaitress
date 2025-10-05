package com.github.wallev.carvein.mixin;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.wallev.carvein.api.StorageMaidRetMaidEvent;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import studio.fantasyit.maid_storage_manager.maid.behavior.request.ret.RequestRetBehavior;

/**
 * RequestRetBehavior类的Mixin扩展，用于在女仆完成请求任务后广播特定事件
 * 该Mixin通过注入代码到tryReadyMaid方法末尾，实现了存储女仆返回事件的触发机制
 */
@Mixin(RequestRetBehavior.class)
public class RequestRetMoveBehaviorMixin {

    /**
     * 在tryReadyMaid方法执行完毕后注入代码，广播StorageMaidRetMaidEvent事件
     * @param targetEntity 服务员
     * @param chefMaid 厨师
     * @param cir 回调信息，包含方法的返回值
     */
    @Inject(method = "tryReadyMaid", at = @At("TAIL"), remap = false)
    private void broadcastEvent(EntityMaid targetEntity, EntityMaid chefMaid, CallbackInfoReturnable<Boolean> cir) {
        // 创建存储女仆返回事件实例，包含请求任务的执行者和目标
        StorageMaidRetMaidEvent storageMaidRetMaidEvent = new StorageMaidRetMaidEvent(chefMaid, targetEntity);
        // 将事件发布到Minecraft Forge的事件总线上，供其他模块监听和处理
        MinecraftForge.EVENT_BUS.post(storageMaidRetMaidEvent);
    }

}


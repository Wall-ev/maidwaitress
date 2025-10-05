package com.github.wallev.carvein.api;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.eventbus.api.Event;

/**
 * 仓管制作好了物品后递给目标实体事件
 * PS: 因为仓管目前没有这个事件，所以自己写一个，然后通过mixin触发
 */
public class StorageMaidRetMaidEvent extends Event {

    // 厨师——仓管
    public final EntityMaid originMaid;
    public final Entity targetEntity;

    public StorageMaidRetMaidEvent(EntityMaid originMaid, Entity targetEntity) {
        this.originMaid = originMaid;
        this.targetEntity = targetEntity;
    }

    public EntityMaid getOriginMaid() {
        return originMaid;
    }

    public Entity getTargetEntity() {
        return targetEntity;
    }
}

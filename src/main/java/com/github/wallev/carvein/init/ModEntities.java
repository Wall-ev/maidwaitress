package com.github.wallev.carvein.init;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.wallev.carvein.Carvein;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;
import java.util.Optional;

/**
 * 模组实体相关内存模块类型注册类
 * 负责注册各种女仆AI记忆模块，用于存储和管理女仆的工作状态、请求信息等
 */
public class ModEntities {
    public static final DeferredRegister<MemoryModuleType<?>> MEMORY_MODULE_TYPES = DeferredRegister.create(ForgeRegistries.MEMORY_MODULE_TYPES, Carvein.MOD_ID);

    // 工作位置跟踪器 - 存储女仆的工作位置，用于将服务员导航至厨师的位置，等待食物的完成制作
    public static RegistryObject<MemoryModuleType<PositionTracker>> WORK_POS = MEMORY_MODULE_TYPES.register("work_pos", () -> new MemoryModuleType<>(Optional.empty()));

    // 请求食物列表 - 存储玩家请求的食物物品栈列表，用于向厨师请求需要的食物
    public static RegistryObject<MemoryModuleType<List<ItemStack>>> REQUEST_FOODS = MEMORY_MODULE_TYPES.register("request_foods", () -> new MemoryModuleType<>(Optional.empty()));

    // 请求行走标记 - 标记女仆是否需要调整行走状态，能否自由行动，非必须
    public static RegistryObject<MemoryModuleType<Boolean>> REJUST_WLAK = MEMORY_MODULE_TYPES.register("rejuest_walk", () -> new MemoryModuleType<>(Optional.empty()));

    // 等待食物标记 - 标记女仆是否处于等待食物制作完成状态，能否自由行动，非必须
    public static RegistryObject<MemoryModuleType<Boolean>> WAIT_TO_FOOD = MEMORY_MODULE_TYPES.register("wait_to_food", () -> new MemoryModuleType<>(Optional.empty()));

    // 请求放置位置 - 存储请求放置食物的方块位置，用于将食物放置在指定位置
    public static RegistryObject<MemoryModuleType<BlockPos>> REQUEST_PLACE_POS = MEMORY_MODULE_TYPES.register("request_place_pos", () -> new MemoryModuleType<>(Optional.empty()));

    // 请求放置玩家 - 存储请求放置食物的目标玩家实体，用于将食物交付给指定玩家
    public static RegistryObject<MemoryModuleType<Player>> REQUEST_PLACE_PLAYER = MEMORY_MODULE_TYPES.register("request_place_player", () -> new MemoryModuleType<>(Optional.empty()));

    // 请求玩家 - 存储发起请求的玩家实体，用于告诉服务员是哪个顾客需要点单
    public static RegistryObject<MemoryModuleType<Player>> REQUEST_PLAYER = MEMORY_MODULE_TYPES.register("request_player", () -> new MemoryModuleType<>(Optional.empty()));

    // 请求女仆 - 存储请求服务的目标女仆实体，用于标记服务员需要通知的是哪个厨师
    public static RegistryObject<MemoryModuleType<EntityMaid>> REQUEST_MAID = MEMORY_MODULE_TYPES.register("request_maid", () -> new MemoryModuleType<>(Optional.empty()));

    // 食物女仆 - 存储负责制作/提供食物的女仆实体——仓管(厨师)
    public static RegistryObject<MemoryModuleType<EntityMaid>> CHEF = MEMORY_MODULE_TYPES.register("chef", () -> new MemoryModuleType<>(Optional.empty()));

}

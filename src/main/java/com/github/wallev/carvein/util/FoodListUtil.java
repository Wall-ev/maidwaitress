package com.github.wallev.carvein.util;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.simibubi.create.content.equipment.clipboard.ClipboardEntry;
import com.simibubi.create.content.schematics.cannon.MaterialChecklist;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * 食物列表工具类，提供与食物订单相关的各种实用方法，包括从剪贴板读取食物列表、
 * 将食物列表写入剪贴板以及在女仆物品栏中查找请求的食物等功能
 */
public class FoodListUtil {

    /**
     * 从剪贴板物品中读取已勾选的食物列表
     * @param clipItem 剪贴板物品栈
     * @return 包含所有已勾选食物的物品栈列表
     */
    public static List<ItemStack> getFoodList(ItemStack clipItem) {
        return ClipboardEntry.readAll(clipItem).stream()
                .flatMap(Collection::stream)
                .filter(clipboardEntry -> clipboardEntry.checked)
                .map(clipboardEntry -> clipboardEntry.icon)
                .toList();
    }

    /**
     * 将食物列表写入剪切板（菜单），然后设置到玩家主手中，便于设置菜单
     * @param foods 要写入的食物列表
     * @param player 执行操作的玩家
     */
    public static void writeFoods(List<ItemStack> foods, Player player) {
        MaterialChecklist checklist = new MaterialChecklist();
        for (ItemStack food : foods) {
            int i = checklist.required.getInt(food.getItem());
            if (i == 0) {
                checklist.required.put(food.getItem(), food.getCount());
            } else {
                checklist.required.put(food.getItem(), food.getCount() + 1);
            }

            checklist.required.putIfAbsent(food.getItem(), 1);
            checklist.collect(food);
        }
        ItemStack writtenClipboard = checklist.createWrittenClipboard();
        player.setItemInHand(InteractionHand.MAIN_HAND, writtenClipboard);
    }

    /**
     * 在女仆的可用物品栏中查找请求的食物列表
     * @param requests 请求的食物物品栈列表
     * @param maid 提供物品栏的女仆实体
     * @return 在女仆物品栏中找到的符合请求的食物列表
     */
    public static List<ItemStack> findRequestFoods(List<ItemStack> requests, EntityMaid maid) {
        // 获取女仆的可用物品栏（包括主手、副手和背包）
        CombinedInvWrapper availableInv = maid.getAvailableInv(true);

        // 用于存储找到的符合请求的食物列表
        List<ItemStack> list = new ArrayList<>();
        // 创建请求食物列表的副本，用于迭代处理
        LinkedList<ItemStack> stacks = new LinkedList<>(requests);
        // 循环处理请求列表中的每个食物，直到列表为空
        while (!stacks.isEmpty()) {
            // 取出请求列表中的第一个食物物品栈
            ItemStack stack = stacks.removeFirst();
            // 遍历女仆物品栏中的所有物品槽位
            for (int i = 0; i < availableInv.getSlots(); i++) {
                // 获取当前槽位中的物品栈
                ItemStack stackInSlot = availableInv.getStackInSlot(i);
                // 检查槽位中的物品是否与请求的物品相同（包括物品类型和NBT标签）
                if (ItemStack.isSameItemSameTags(stack, stackInSlot)) {
                    // 如果匹配，则将该物品添加到结果列表中
                    list.add(stackInSlot);
                }
            }
        }
        // 返回找到的符合请求的食物列表
        return list;
    }
}
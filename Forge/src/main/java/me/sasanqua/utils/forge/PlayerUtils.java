package me.sasanqua.utils.forge;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public final class PlayerUtils {

	public static void offerItemStack(EntityPlayer player, ItemStack stack) {
		if (stack.isEmpty()) {
			return;
		}
		boolean flag = player.addItemStackToInventory(stack);
		if (flag) {
			player.inventoryContainer.detectAndSendChanges();
		} else {
			EntityItem entityItem = player.entityDropItem(stack, 0.5F);
			if (entityItem != null) {
				entityItem.setNoPickupDelay();
				entityItem.setOwner(player.getName());
			}
		}
	}

}

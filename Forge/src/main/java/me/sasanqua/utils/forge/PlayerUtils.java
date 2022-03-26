package me.sasanqua.utils.forge;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public final class PlayerUtils {

	public static void offerItemStack(PlayerEntity player, ItemStack stack) {
		if (stack.isEmpty()) {
			return;
		}
		boolean flag = player.addItem(stack);
		if (flag) {
			player.inventoryMenu.broadcastChanges();
		} else {
			ItemEntity entityItem = player.drop(stack, false);
			if (entityItem != null) {
				entityItem.setNoPickUpDelay();
				entityItem.setOwner(player.getUUID());
			}
		}
	}

}

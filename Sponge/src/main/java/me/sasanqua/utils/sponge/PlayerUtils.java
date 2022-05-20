package me.sasanqua.utils.sponge;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.entity.MainPlayerInventory;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult;

public final class PlayerUtils {

	public static void offerItemStack(Player player, ItemStack stack) {
		if (stack.isEmpty()) {
			return;
		}
		InventoryTransactionResult result = player.getInventory()
				.query(QueryOperationTypes.INVENTORY_TYPE.of(MainPlayerInventory.class))
				.offer(stack);
		result.getRejectedItems().forEach(item -> {
			Entity itemEntity = player.getWorld().createEntity(EntityTypes.ITEM, player.getPosition());
			itemEntity.offer(Keys.REPRESENTED_ITEM, item);
			itemEntity.offer(Keys.PICKUP_DELAY, 0);
			player.getWorld().spawnEntity(itemEntity);
		});

	}

}

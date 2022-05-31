package me.sasanqua.utils.sponge;

import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.entity.PrimaryPlayerInventory;
import org.spongepowered.api.item.inventory.query.QueryTypes;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult;
import org.spongepowered.api.util.Ticks;

public final class PlayerUtils {

	public static void offerItemStack(final Player player, final ItemStack stack) {
		if (stack.isEmpty()) {
			return;
		}
		final InventoryTransactionResult result = player.inventory()
				.query(QueryTypes.INVENTORY_TYPE.get().of(PrimaryPlayerInventory.class))
				.offer(stack);
		result.rejectedItems().forEach(item -> {
			final Entity itemEntity = player.world().createEntity(EntityTypes.ITEM, player.position());
			itemEntity.offer(Keys.ITEM_STACK_SNAPSHOT, item);
			itemEntity.offer(Keys.PICKUP_DELAY, Ticks.of(0));
			player.world().spawnEntity(itemEntity);
		});

	}

}

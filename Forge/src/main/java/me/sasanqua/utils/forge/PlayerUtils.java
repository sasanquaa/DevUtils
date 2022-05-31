package me.sasanqua.utils.forge;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.TeleportationRepositioner;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Teleporter;
import net.minecraft.world.server.ServerWorld;

import java.util.Optional;

public final class PlayerUtils {

	public static void offerItemStack(final ServerPlayerEntity player, final ItemStack stack) {
		if (stack.isEmpty()) {
			return;
		}
		final boolean flag = player.addItem(stack);
		if (flag) {
			player.inventoryMenu.broadcastChanges();
		} else {
			final ItemEntity entityItem = player.drop(stack, false);
			if (entityItem != null) {
				entityItem.setNoPickUpDelay();
				entityItem.setOwner(player.getUUID());
			}
		}
	}

	public static void forceTeleport(final ServerPlayerEntity player, final ServerWorld world, final BlockPos pos) {
		transferWorld(player, world);
		player.setPos(pos.getX(), pos.getY(), pos.getZ());
	}

	private static void transferWorld(final ServerPlayerEntity player, final ServerWorld world) {
		if (!player.getCommandSenderWorld().equals(world)) {
			player.changeDimension(world, new VanillaTeleporter(world));
		}
	}

	private static final class VanillaTeleporter extends Teleporter {

		VanillaTeleporter(final ServerWorld worldIn) {
			super(worldIn);
		}

		@Override
		public Optional<TeleportationRepositioner.Result> findPortalAround(final BlockPos p_242957_1_, final boolean p_242957_2_) {
			return Optional.empty();
		}

		@Override
		public Optional<TeleportationRepositioner.Result> createPortal(final BlockPos p_242956_1_, final Direction.Axis p_242956_2_) {
			return Optional.empty();
		}

	}

}

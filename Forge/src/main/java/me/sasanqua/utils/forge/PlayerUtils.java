package me.sasanqua.utils.forge;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;

public final class PlayerUtils {

	public static void offerItemStack(final EntityPlayerMP player, final ItemStack stack) {
		if (stack.isEmpty()) {
			return;
		}
		final boolean flag = player.addItemStackToInventory(stack);
		if (flag) {
			player.inventoryContainer.detectAndSendChanges();
		} else {
			final EntityItem entityItem = player.entityDropItem(stack, 0.5F);
			if (entityItem != null) {
				entityItem.setNoPickupDelay();
				entityItem.setOwner(player.getName());
			}
		}
	}

	public static void forceTeleport(final EntityPlayerMP player, final WorldServer world, final BlockPos pos) {
		transferWorld(player, world);
		player.setPositionAndUpdate(pos.getX(), pos.getY(), pos.getZ());
	}

	public static boolean attemptTeleport(final EntityPlayerMP player, final WorldServer world, BlockPos pos) {
		transferWorld(player, world);
		if (!player.attemptTeleport(pos.getX(), pos.getY(), pos.getZ())) {
			pos = world.getSpawnPoint();
			return player.attemptTeleport(pos.getX(), pos.getY(), pos.getZ());
		}
		return true;
	}

	private static void transferWorld(final EntityPlayerMP player, final WorldServer world) {
		if (!player.getServerWorld().equals(world)) {
			FMLCommonHandler.instance()
					.getMinecraftServerInstance()
					.getPlayerList()
					.transferPlayerToDimension(player, world.provider.getDimension(), new VanillaTeleporter(world));
		}
	}

	private static final class VanillaTeleporter extends Teleporter {

		VanillaTeleporter(final WorldServer worldIn) {
			super(worldIn);
		}

		@Override
		public void placeInPortal(final Entity entityIn, final float rotationYaw) {
		}

		@Override
		public boolean placeInExistingPortal(final Entity entityIn, final float rotationYaw) {
			return false;
		}

		@Override
		public boolean makePortal(final Entity entityIn) {
			return false;
		}

	}

}

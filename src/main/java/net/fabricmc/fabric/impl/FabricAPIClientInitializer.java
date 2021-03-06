/*
 * Copyright (c) 2016, 2017, 2018 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.fabricmc.fabric.impl;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.block.BlockPickInteractionAware;
import net.fabricmc.fabric.api.entity.EntityPickInteractionAware;
import net.fabricmc.fabric.api.event.client.player.ClientPickBlockCallback;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.impl.client.gui.ScreenProviderRegistryImpl;
import net.fabricmc.fabric.impl.registry.RegistrySyncManager;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public class FabricAPIClientInitializer implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientSidePacketRegistry.INSTANCE.register(RegistrySyncManager.ID, (ctx, buf) -> {
			// if not hosting server, apply packet
			RegistrySyncManager.receivePacket(ctx, buf, !MinecraftClient.getInstance().isInSingleplayer());
		});

		ClientPickBlockCallback.EVENT.register(((player, result, container) -> {
			if (result instanceof BlockHitResult) {
				BlockView view = player.getEntityWorld();
				BlockPos pos = ((BlockHitResult) result).getBlockPos();
				BlockState state = view.getBlockState(pos);

				if (state.getBlock() instanceof BlockPickInteractionAware) {
					container.setStack(((BlockPickInteractionAware) state.getBlock()).getPickedStack(state, view, pos, player, result));
				}
			} else if (result instanceof EntityHitResult) {
				Entity entity = ((EntityHitResult) result).getEntity();

				if (entity instanceof EntityPickInteractionAware) {
					container.setStack(((EntityPickInteractionAware) entity).getPickedStack(player, result));
				}
			}

			return true;
		}));

		((ScreenProviderRegistryImpl) ScreenProviderRegistryImpl.INSTANCE).init();
	}
}

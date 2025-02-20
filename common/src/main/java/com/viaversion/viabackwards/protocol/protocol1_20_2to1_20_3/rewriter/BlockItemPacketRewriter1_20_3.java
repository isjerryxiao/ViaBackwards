/*
 * This file is part of ViaBackwards - https://github.com/ViaVersion/ViaBackwards
 * Copyright (C) 2023 ViaVersion and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.viaversion.viabackwards.protocol.protocol1_20_2to1_20_3.rewriter;

import com.viaversion.viabackwards.ViaBackwards;
import com.viaversion.viabackwards.api.rewriters.ItemRewriter;
import com.viaversion.viabackwards.protocol.protocol1_20_2to1_20_3.Protocol1_20_2To1_20_3;
import com.viaversion.viaversion.api.data.ParticleMappings;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_20_2;
import com.viaversion.viaversion.api.type.types.version.Types1_20_3;
import com.viaversion.viaversion.protocols.protocol1_20_2to1_20.packet.ServerboundPackets1_20_2;
import com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.packet.ClientboundPackets1_20_3;
import com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.rewriter.RecipeRewriter1_20_3;
import com.viaversion.viaversion.rewriter.BlockRewriter;

public final class BlockItemPacketRewriter1_20_3 extends ItemRewriter<ClientboundPackets1_20_3, ServerboundPackets1_20_2, Protocol1_20_2To1_20_3> {

    public BlockItemPacketRewriter1_20_3(final Protocol1_20_2To1_20_3 protocol) {
        super(protocol, Type.ITEM1_20_2, Type.ITEM1_20_2_ARRAY);
    }

    @Override
    public void registerPackets() {
        final BlockRewriter<ClientboundPackets1_20_3> blockRewriter = BlockRewriter.for1_20_2(protocol);
        blockRewriter.registerBlockAction(ClientboundPackets1_20_3.BLOCK_ACTION);
        blockRewriter.registerBlockChange(ClientboundPackets1_20_3.BLOCK_CHANGE);
        blockRewriter.registerVarLongMultiBlockChange1_20(ClientboundPackets1_20_3.MULTI_BLOCK_CHANGE);
        blockRewriter.registerEffect(ClientboundPackets1_20_3.EFFECT, 1010, 2001);
        blockRewriter.registerChunkData1_19(ClientboundPackets1_20_3.CHUNK_DATA, ChunkType1_20_2::new);
        blockRewriter.registerBlockEntityData(ClientboundPackets1_20_3.BLOCK_ENTITY_DATA);

        registerSetCooldown(ClientboundPackets1_20_3.COOLDOWN);
        registerWindowItems1_17_1(ClientboundPackets1_20_3.WINDOW_ITEMS);
        registerSetSlot1_17_1(ClientboundPackets1_20_3.SET_SLOT);
        registerEntityEquipmentArray(ClientboundPackets1_20_3.ENTITY_EQUIPMENT);
        registerClickWindow1_17_1(ServerboundPackets1_20_2.CLICK_WINDOW);
        registerTradeList1_19(ClientboundPackets1_20_3.TRADE_LIST);
        registerCreativeInvAction(ServerboundPackets1_20_2.CREATIVE_INVENTORY_ACTION);
        registerWindowPropertyEnchantmentHandler(ClientboundPackets1_20_3.WINDOW_PROPERTY);

        protocol.registerClientbound(ClientboundPackets1_20_3.SPAWN_PARTICLE, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT); // 0 - Particle ID
                map(Type.BOOLEAN); // 1 - Long Distance
                map(Type.DOUBLE); // 2 - X
                map(Type.DOUBLE); // 3 - Y
                map(Type.DOUBLE); // 4 - Z
                map(Type.FLOAT); // 5 - Offset X
                map(Type.FLOAT); // 6 - Offset Y
                map(Type.FLOAT); // 7 - Offset Z
                map(Type.FLOAT); // 8 - Particle Data
                map(Type.INT); // 9 - Particle Count
                handler(wrapper -> {
                    final int id = wrapper.get(Type.VAR_INT, 0);
                    final ParticleMappings particleMappings = protocol.getMappingData().getParticleMappings();
                    if (id == particleMappings.id("vibration")) {
                        final int positionSourceType = wrapper.read(Type.VAR_INT);
                        if (positionSourceType == 0) {
                            wrapper.write(Type.STRING, "minecraft:block");
                        } else if (positionSourceType == 1) {
                            wrapper.write(Type.STRING, "minecraft:entity");
                        } else {
                            ViaBackwards.getPlatform().getLogger().warning("Unknown position source type: " + positionSourceType);
                            wrapper.cancel();
                        }
                    }
                });
                handler(getSpawnParticleHandler(Type.VAR_INT));
            }
        });

        new RecipeRewriter1_20_3<ClientboundPackets1_20_3>(protocol) {
            @Override
            public void handleCraftingShaped(final PacketWrapper wrapper) throws Exception {
                // Move width and height up
                final String group = wrapper.read(Type.STRING);
                final int craftingBookCategory = wrapper.read(Type.VAR_INT);

                final int width = wrapper.passthrough(Type.VAR_INT);
                final int height = wrapper.passthrough(Type.VAR_INT);

                wrapper.write(Type.STRING, group);
                wrapper.write(Type.VAR_INT, craftingBookCategory);

                final int ingredients = height * width;
                for (int i = 0; i < ingredients; i++) {
                    handleIngredient(wrapper);
                }
                rewrite(wrapper.passthrough(itemType())); // Result
                wrapper.passthrough(Type.BOOLEAN); // Show notification
            }
        }.register(ClientboundPackets1_20_3.DECLARE_RECIPES);

        protocol.registerClientbound(ClientboundPackets1_20_3.EXPLOSION, wrapper -> {
            wrapper.passthrough(Type.DOUBLE); // X
            wrapper.passthrough(Type.DOUBLE); // Y
            wrapper.passthrough(Type.DOUBLE); // Z
            wrapper.passthrough(Type.FLOAT); // Power

            final int blocks = wrapper.read(Type.VAR_INT);
            final byte[][] toBlow = new byte[blocks][3];
            for (int i = 0; i < blocks; i++) {
                toBlow[i] = new byte[]{
                    wrapper.read(Type.BYTE), // Relative X
                    wrapper.read(Type.BYTE), // Relative Y
                    wrapper.read(Type.BYTE) // Relative Z
                };
            }

            final float knockbackX = wrapper.read(Type.FLOAT); // Knockback X
            final float knockbackY = wrapper.read(Type.FLOAT); // Knockback Y
            final float knockbackZ = wrapper.read(Type.FLOAT); // Knockback Z

            final int blockInteraction = wrapper.read(Type.VAR_INT); // Block interaction type
            // 0 = keep, 1 = destroy, 2 = destroy_with_decay, 3 = trigger_block
            if (blockInteraction == 1 || blockInteraction == 2) {
                wrapper.write(Type.VAR_INT, blocks);
                for (final byte[] relativeXYZ : toBlow) {
                    wrapper.write(Type.BYTE, relativeXYZ[0]);
                    wrapper.write(Type.BYTE, relativeXYZ[1]);
                    wrapper.write(Type.BYTE, relativeXYZ[2]);
                }
            } else {
                // Explosion doesn't destroy blocks
                wrapper.write(Type.VAR_INT, 0);
            }

            wrapper.write(Type.FLOAT, knockbackX);
            wrapper.write(Type.FLOAT, knockbackY);
            wrapper.write(Type.FLOAT, knockbackZ);

            // TODO Probably needs handling
            wrapper.read(Types1_20_3.PARTICLE); // Small explosion particle
            wrapper.read(Types1_20_3.PARTICLE); // Large explosion particle
            wrapper.read(Type.STRING); // Explosion sound
            wrapper.read(Type.OPTIONAL_FLOAT); // Sound range
        });
    }
}
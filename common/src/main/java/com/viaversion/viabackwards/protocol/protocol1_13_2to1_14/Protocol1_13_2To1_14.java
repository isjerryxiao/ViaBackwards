/*
 * This file is part of ViaBackwards - https://github.com/ViaVersion/ViaBackwards
 * Copyright (C) 2016-2023 ViaVersion and contributors
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
package com.viaversion.viabackwards.protocol.protocol1_13_2to1_14;

import com.viaversion.viabackwards.api.BackwardsProtocol;
import com.viaversion.viabackwards.api.data.BackwardsMappings;
import com.viaversion.viabackwards.api.rewriters.TranslatableRewriter;
import com.viaversion.viabackwards.protocol.protocol1_13_2to1_14.data.CommandRewriter1_14;
import com.viaversion.viabackwards.protocol.protocol1_13_2to1_14.packets.BlockItemPackets1_14;
import com.viaversion.viabackwards.protocol.protocol1_13_2to1_14.packets.EntityPackets1_14;
import com.viaversion.viabackwards.protocol.protocol1_13_2to1_14.packets.PlayerPackets1_14;
import com.viaversion.viabackwards.protocol.protocol1_13_2to1_14.packets.SoundPackets1_14;
import com.viaversion.viabackwards.protocol.protocol1_13_2to1_14.storage.ChunkLightStorage;
import com.viaversion.viabackwards.protocol.protocol1_13_2to1_14.storage.DifficultyStorage;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.ClientWorld;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_14;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.ClientboundPackets1_13;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.ServerboundPackets1_13;
import com.viaversion.viaversion.protocols.protocol1_14to1_13_2.ClientboundPackets1_14;
import com.viaversion.viaversion.protocols.protocol1_14to1_13_2.Protocol1_14To1_13_2;
import com.viaversion.viaversion.protocols.protocol1_14to1_13_2.ServerboundPackets1_14;
import com.viaversion.viaversion.rewriter.ComponentRewriter;
import com.viaversion.viaversion.rewriter.StatisticsRewriter;

public class Protocol1_13_2To1_14 extends BackwardsProtocol<ClientboundPackets1_14, ClientboundPackets1_13, ServerboundPackets1_14, ServerboundPackets1_13> {

    public static final BackwardsMappings MAPPINGS = new BackwardsMappings("1.14", "1.13.2", Protocol1_14To1_13_2.class);
    private final EntityPackets1_14 entityRewriter = new EntityPackets1_14(this);
    private final BlockItemPackets1_14 blockItemPackets = new BlockItemPackets1_14(this);
    private final TranslatableRewriter<ClientboundPackets1_14> translatableRewriter = new TranslatableRewriter<>(this, ComponentRewriter.ReadType.JSON);

    public Protocol1_13_2To1_14() {
        super(ClientboundPackets1_14.class, ClientboundPackets1_13.class, ServerboundPackets1_14.class, ServerboundPackets1_13.class);
    }

    @Override
    protected void registerPackets() {
        super.registerPackets();

        translatableRewriter.registerBossBar(ClientboundPackets1_14.BOSSBAR);
        translatableRewriter.registerComponentPacket(ClientboundPackets1_14.CHAT_MESSAGE);
        translatableRewriter.registerCombatEvent(ClientboundPackets1_14.COMBAT_EVENT);
        translatableRewriter.registerComponentPacket(ClientboundPackets1_14.DISCONNECT);
        translatableRewriter.registerTabList(ClientboundPackets1_14.TAB_LIST);
        translatableRewriter.registerTitle(ClientboundPackets1_14.TITLE);
        translatableRewriter.registerPing();

        new CommandRewriter1_14(this).registerDeclareCommands(ClientboundPackets1_14.DECLARE_COMMANDS);
        new PlayerPackets1_14(this).register();
        new SoundPackets1_14(this).register();

        new StatisticsRewriter<>(this).register(ClientboundPackets1_14.STATISTICS);

        cancelClientbound(ClientboundPackets1_14.UPDATE_VIEW_POSITION);
        cancelClientbound(ClientboundPackets1_14.UPDATE_VIEW_DISTANCE);

        registerClientbound(ClientboundPackets1_14.TAGS, wrapper -> {
            int blockTagsSize = wrapper.passthrough(Type.VAR_INT);
            for (int i = 0; i < blockTagsSize; i++) {
                wrapper.passthrough(Type.STRING);
                int[] blockIds = wrapper.passthrough(Type.VAR_INT_ARRAY_PRIMITIVE);
                for (int j = 0; j < blockIds.length; j++) {
                    int id = blockIds[j];
                    // Ignore new blocktags
                    int blockId = MAPPINGS.getNewBlockId(id);
                    blockIds[j] = blockId;
                }
            }

            int itemTagsSize = wrapper.passthrough(Type.VAR_INT);
            for (int i = 0; i < itemTagsSize; i++) {
                wrapper.passthrough(Type.STRING);
                int[] itemIds = wrapper.passthrough(Type.VAR_INT_ARRAY_PRIMITIVE);
                for (int j = 0; j < itemIds.length; j++) {
                    int itemId = itemIds[j];
                    // Ignore new itemtags
                    int oldId = MAPPINGS.getItemMappings().getNewId(itemId);
                    itemIds[j] = oldId;
                }
            }

            int fluidTagsSize = wrapper.passthrough(Type.VAR_INT); // fluid tags
            for (int i = 0; i < fluidTagsSize; i++) {
                wrapper.passthrough(Type.STRING);
                wrapper.passthrough(Type.VAR_INT_ARRAY_PRIMITIVE);
            }

            // Eat entity tags
            int entityTagsSize = wrapper.read(Type.VAR_INT);
            for (int i = 0; i < entityTagsSize; i++) {
                wrapper.read(Type.STRING);
                wrapper.read(Type.VAR_INT_ARRAY_PRIMITIVE);
            }
        });

        registerClientbound(ClientboundPackets1_14.UPDATE_LIGHT, null, wrapper -> {
            int x = wrapper.read(Type.VAR_INT);
            int z = wrapper.read(Type.VAR_INT);
            int skyLightMask = wrapper.read(Type.VAR_INT);
            int blockLightMask = wrapper.read(Type.VAR_INT);
            int emptySkyLightMask = wrapper.read(Type.VAR_INT);
            int emptyBlockLightMask = wrapper.read(Type.VAR_INT);

            byte[][] skyLight = new byte[16][];
            // we don't need void and +256 light
            if (isSet(skyLightMask, 0)) {
                wrapper.read(Type.BYTE_ARRAY_PRIMITIVE);
            }
            for (int i = 0; i < 16; i++) {
                if (isSet(skyLightMask, i + 1)) {
                    skyLight[i] = wrapper.read(Type.BYTE_ARRAY_PRIMITIVE);
                } else if (isSet(emptySkyLightMask, i + 1)) {
                    skyLight[i] = ChunkLightStorage.EMPTY_LIGHT;
                }
            }
            if (isSet(skyLightMask, 17)) {
                wrapper.read(Type.BYTE_ARRAY_PRIMITIVE);
            }

            byte[][] blockLight = new byte[16][];
            if (isSet(blockLightMask, 0)) {
                wrapper.read(Type.BYTE_ARRAY_PRIMITIVE);
            }
            for (int i = 0; i < 16; i++) {
                if (isSet(blockLightMask, i + 1)) {
                    blockLight[i] = wrapper.read(Type.BYTE_ARRAY_PRIMITIVE);
                } else if (isSet(emptyBlockLightMask, i + 1)) {
                    blockLight[i] = ChunkLightStorage.EMPTY_LIGHT;
                }
            }
            if (isSet(blockLightMask, 17)) {
                wrapper.read(Type.BYTE_ARRAY_PRIMITIVE);
            }

            //TODO Soft memory leak: Don't store light if chunk is already loaded
            wrapper.user().get(ChunkLightStorage.class).setStoredLight(skyLight, blockLight, x, z);
            wrapper.cancel();
        });
    }

    private static boolean isSet(int mask, int i) {
        return (mask & (1 << i)) != 0;
    }

    @Override
    public void init(UserConnection user) {
        // Register ClientWorld
        if (!user.has(ClientWorld.class)) {
            user.put(new ClientWorld());
        }

        user.addEntityTracker(this.getClass(), new EntityTrackerBase(user, EntityTypes1_14.PLAYER));

        if (!user.has(ChunkLightStorage.class)) {
            user.put(new ChunkLightStorage());
        }

        user.put(new DifficultyStorage());
    }

    @Override
    public BackwardsMappings getMappingData() {
        return MAPPINGS;
    }

    @Override
    public EntityPackets1_14 getEntityRewriter() {
        return entityRewriter;
    }

    @Override
    public BlockItemPackets1_14 getItemRewriter() {
        return blockItemPackets;
    }

    @Override
    public TranslatableRewriter<ClientboundPackets1_14> getTranslatableRewriter() {
        return translatableRewriter;
    }
}

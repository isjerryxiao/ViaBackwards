package nl.matsv.viabackwards.protocol.protocol1_15_2to1_16;

import nl.matsv.viabackwards.ViaBackwards;
import nl.matsv.viabackwards.api.BackwardsProtocol;
import nl.matsv.viabackwards.api.entities.storage.EntityTracker;
import nl.matsv.viabackwards.api.rewriters.SoundRewriter;
import nl.matsv.viabackwards.api.rewriters.TranslatableRewriter;
import nl.matsv.viabackwards.protocol.protocol1_15_2to1_16.chat.TranslatableRewriter1_16;
import nl.matsv.viabackwards.protocol.protocol1_15_2to1_16.data.BackwardsMappings;
import nl.matsv.viabackwards.protocol.protocol1_15_2to1_16.packets.BlockItemPackets1_16;
import nl.matsv.viabackwards.protocol.protocol1_15_2to1_16.packets.EntityPackets1_16;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.rewriters.TagRewriter;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion.protocols.protocol1_14to1_13_2.ServerboundPackets1_14;
import us.myles.ViaVersion.protocols.protocol1_15to1_14_4.ClientboundPackets1_15;
import us.myles.ViaVersion.protocols.protocol1_16to1_15_2.ClientboundPackets1_16;
import us.myles.ViaVersion.protocols.protocol1_16to1_15_2.Protocol1_16To1_15_2;
import us.myles.ViaVersion.protocols.protocol1_16to1_15_2.ServerboundPackets1_16;
import us.myles.ViaVersion.protocols.protocol1_16to1_15_2.data.MappingData;
import us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2.storage.ClientWorld;

import java.util.UUID;

public class Protocol1_15_2To1_16 extends BackwardsProtocol<ClientboundPackets1_16, ClientboundPackets1_15, ServerboundPackets1_16, ServerboundPackets1_14> {

    private BlockItemPackets1_16 blockItemPackets;
    private TranslatableRewriter translatableRewriter;

    public Protocol1_15_2To1_16() {
        super(ClientboundPackets1_16.class, ClientboundPackets1_15.class, ServerboundPackets1_16.class, ServerboundPackets1_14.class);
    }

    @Override
    protected void registerPackets() {
        executeAsyncAfterLoaded(Protocol1_16To1_15_2.class, BackwardsMappings::init);

        translatableRewriter = new TranslatableRewriter1_16(this);
        translatableRewriter.registerBossBar(ClientboundPackets1_16.BOSSBAR);
        translatableRewriter.registerCombatEvent(ClientboundPackets1_16.COMBAT_EVENT);
        translatableRewriter.registerDisconnect(ClientboundPackets1_16.DISCONNECT);
        translatableRewriter.registerTabList(ClientboundPackets1_16.TAB_LIST);
        translatableRewriter.registerTitle(ClientboundPackets1_16.TITLE);
        translatableRewriter.registerPing();

        (blockItemPackets = new BlockItemPackets1_16(this, translatableRewriter)).register();
        EntityPackets1_16 entityPackets = new EntityPackets1_16(this);
        entityPackets.register();

        registerOutgoing(ClientboundPackets1_16.CHAT_MESSAGE, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> wrapper.write(Type.STRING, translatableRewriter.processText(wrapper.read(Type.STRING))));
                map(Type.BYTE);
                map(Type.UUID, Type.NOTHING); // Sender
            }
        });

        registerOutgoing(ClientboundPackets1_16.OPEN_WINDOW, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // Window Id
                map(Type.VAR_INT); // Window Type
                handler(wrapper -> wrapper.write(Type.STRING, translatableRewriter.processText(wrapper.read(Type.STRING))));
                handler(wrapper -> {
                    int windowType = wrapper.get(Type.VAR_INT, 1);
                    if (windowType == 20) { // Smithing table
                        wrapper.set(Type.VAR_INT, 1, 7); // Open anvil inventory
                    } else if (windowType > 20) {
                        wrapper.set(Type.VAR_INT, 1, --windowType);
                    }
                });
            }
        });

        SoundRewriter soundRewriter = new SoundRewriter(this,
                id -> BackwardsMappings.soundMappings.getNewId(id), stringId -> BackwardsMappings.soundMappings.getNewId(stringId));
        soundRewriter.registerSound(ClientboundPackets1_16.SOUND);
        soundRewriter.registerSound(ClientboundPackets1_16.ENTITY_SOUND);
        soundRewriter.registerNamedSound(ClientboundPackets1_16.NAMED_SOUND);
        soundRewriter.registerStopSound(ClientboundPackets1_16.STOP_SOUND);

        // Login success
        registerOutgoing(State.LOGIN, 0x02, 0x02, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> {
                    // Transform int array to plain string
                    UUID uuid = wrapper.read(Type.UUID_INT_ARRAY);
                    wrapper.write(Type.STRING, uuid.toString());
                });
            }
        });

        registerOutgoing(ClientboundPackets1_16.ADVANCEMENTS, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> {
                    wrapper.passthrough(Type.BOOLEAN); // Reset/clear
                    int size = wrapper.passthrough(Type.VAR_INT);
                    for (int i = 0; i < size; i++) {
                        wrapper.passthrough(Type.STRING); // Identifier
                        // Parent
                        if (wrapper.passthrough(Type.BOOLEAN)) {
                            wrapper.passthrough(Type.STRING);
                        }
                        // Display data
                        if (wrapper.passthrough(Type.BOOLEAN)) {
                            wrapper.passthrough(Type.STRING); // Title
                            wrapper.passthrough(Type.STRING); // Description
                            blockItemPackets.handleItemToClient(wrapper.passthrough(Type.FLAT_VAR_INT_ITEM)); // Icon
                            wrapper.passthrough(Type.VAR_INT); // Frame type
                            int flags = wrapper.passthrough(Type.INT); // Flags
                            if ((flags & 1) != 0) {
                                wrapper.passthrough(Type.STRING); // Background texture
                            }
                            wrapper.passthrough(Type.FLOAT); // X
                            wrapper.passthrough(Type.FLOAT); // Y
                        }

                        wrapper.passthrough(Type.STRING_ARRAY); // Criteria
                        int arrayLength = wrapper.passthrough(Type.VAR_INT);
                        for (int array = 0; array < arrayLength; array++) {
                            wrapper.passthrough(Type.STRING_ARRAY); // String array
                        }
                    }
                });
            }
        });

        registerOutgoing(ClientboundPackets1_16.STATISTICS, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> {
                    int size = wrapper.passthrough(Type.VAR_INT);

                    int newSize = size;
                    for (int i = 0; i < size; i++) {
                        int categoryId = wrapper.read(Type.VAR_INT);
                        int statisticId = wrapper.read(Type.VAR_INT);
                        // New statistics
                        if (statisticId > 49) {
                            wrapper.read(Type.VAR_INT);
                            newSize--;
                            continue;
                        }

                        wrapper.write(Type.VAR_INT, categoryId);
                        wrapper.write(Type.VAR_INT, statisticId);
                        wrapper.passthrough(Type.VAR_INT); // value
                    }

                    if (newSize != size) {
                        wrapper.set(Type.VAR_INT, 0, newSize);
                    }
                });
            }
        });

        new TagRewriter(this, id -> BackwardsMappings.blockMappings.getNewId(id), id ->
                MappingData.oldToNewItems.inverse().get(id), entityPackets::getOldEntityId).register(ClientboundPackets1_16.TAGS);

        cancelIncoming(ServerboundPackets1_14.UPDATE_JIGSAW_BLOCK);
    }

    public static int getNewBlockStateId(int id) {
        int newId = BackwardsMappings.blockStateMappings.getNewId(id);
        if (newId == -1) {
            ViaBackwards.getPlatform().getLogger().warning("Missing 1.16 blockstate id for 1.15 block " + id);
            return 0;
        }
        return newId;
    }

    public static int getNewBlockId(int id) {
        int newId = BackwardsMappings.blockMappings.getNewId(id);
        if (newId == -1) {
            ViaBackwards.getPlatform().getLogger().warning("Missing 1.16 block id for 1.15 block " + id);
            return id;
        }
        return newId;
    }

    @Override
    public void init(UserConnection user) {
        if (!user.has(ClientWorld.class)) {
            user.put(new ClientWorld(user));
        }
        if (!user.has(EntityTracker.class)) {
            user.put(new EntityTracker(user));
        }
        user.get(EntityTracker.class).initProtocol(this);
    }

    public BlockItemPackets1_16 getBlockItemPackets() {
        return blockItemPackets;
    }

    public TranslatableRewriter getTranslatableRewriter() {
        return translatableRewriter;
    }
}

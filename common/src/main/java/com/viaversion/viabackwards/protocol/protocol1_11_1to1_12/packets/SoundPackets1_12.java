/*
 * This file is part of ViaBackwards - https://github.com/ViaVersion/ViaBackwards
 * Copyright (C) 2016-2022 ViaVersion and contributors
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

package com.viaversion.viabackwards.protocol.protocol1_11_1to1_12.packets;

import com.viaversion.viabackwards.api.rewriters.LegacySoundRewriter;
import com.viaversion.viabackwards.protocol.protocol1_11_1to1_12.Protocol1_11_1To1_12;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.protocol.remapper.PacketRemapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_12to1_11_1.ClientboundPackets1_12;

public class SoundPackets1_12 extends LegacySoundRewriter<Protocol1_11_1To1_12> {

    public SoundPackets1_12(Protocol1_11_1To1_12 protocol) {
        super(protocol);
    }

    @Override
    protected void registerPackets() {
        protocol.registerClientbound(ClientboundPackets1_12.NAMED_SOUND, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.STRING); // 0 - Sound name
                map(Type.VAR_INT); // 1 - Sound Category
                map(Type.INT); // 2 - x
                map(Type.INT); // 3 - y
                map(Type.INT); // 4 - z
                map(Type.FLOAT); // 5 - Volume
                map(Type.FLOAT); // 6 - Pitch
            }
        });

        protocol.registerClientbound(ClientboundPackets1_12.SOUND, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // 0 - Sound name
                map(Type.VAR_INT); // 1 - Sound Category
                map(Type.INT); // 2 - x
                map(Type.INT); // 3 - y
                map(Type.INT); // 4 - z
                map(Type.FLOAT); // 5 - Volume
                map(Type.FLOAT); // 6 - Pitch

                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int oldId = wrapper.get(Type.VAR_INT, 0);
                        int newId = handleSounds(oldId);
                        if (newId == -1) {
                            wrapper.cancel();
                            return;
                        }

                        if (hasPitch(oldId)) {
                            wrapper.set(Type.FLOAT, 1, handlePitch(oldId));
                        }
                        wrapper.set(Type.VAR_INT, 0, newId);
                    }
                });
            }
        });
    }


    @Override
    protected void registerRewrites() {
        //TODO use the diff file to also have named sound remaps
        // (there were *A LOT* of refactored names)

        // Replacement sounds, suggestions are always welcome
        // Automatically generated from PAaaS
        added(26, 277, 1.4f); // block.end_portal.spawn -> entity.lightning.thunder
        added(27, -1); // block.end_portal_frame.fill

        added(72, 70); // block.note.bell -> block.note.harp
        added(73, 70); // block.note.chime -> block.note.harp
        added(74, 70); // block.note.flute -> block.note.harp
        added(75, 70); // block.note.guitar -> block.note.harp
        added(80, 70); // block.note.xylophone -> block.note.harp

        added(150, -1); // entity.boat.paddle_land
        added(151, -1); // entity.boat.paddle_water

        added(152, -1); // entity.bobber.retrieve

        added(195, -1); // entity.endereye.death

        added(274, 198, 0.8f); // entity.illusion_illager.ambient -> entity.evocation_illager.ambient
        added(275, 199, 0.8f); // entity.illusion_illager.cast_spell -> entity.evocation_illager.cast_spell
        added(276, 200, 0.8f); // entity.illusion_illager.death -> entity.evocation_illager.death
        added(277, 201, 0.8f); // entity.illusion_illager.hurt -> entity.evocation_illager.hurt
        added(278, 191, 0.9f); // entity.illusion_illager.mirror_move -> entity.endermen.teleport
        added(279, 203, 1.5f); // entity.illusion_illager.prepare_blindness -> entity.evocation_illager.prepare_summon
        added(280, 202, 0.8f); // entity.illusion_illager.prepare_mirror -> entity.evocation_illager.prepare_attack

        added(319, 133, 0.6f); // entity.parrot.ambient -> entity.bat.ambient
        added(320, 134, 1.7f); // entity.parrot.death -> entity.bat.death
        added(321, 219, 1.5f); // entity.parrot.eat -> entity.generic.eat
        added(322, 136, 0.7f); // entity.parrot.fly -> entity.bat.loop
        added(323, 135, 1.6f); // entity.parrot.hurt -> entity.bat.hurt
        added(324, 138, 1.5f); // entity.parrot.imitate.blaze -> entity.blaze.ambient
        added(325, 163, 1.5f); // entity.parrot.imitate.creeper -> entity.creeper.primed
        added(326, 170, 1.5f); // entity.parrot.imitate.elder_guardian -> entity.elder_guardian.ambient
        added(327, 178, 1.5f); // entity.parrot.imitate.enderdragon -> entity.enderdragon.ambient
        added(328, 186, 1.5f); // entity.parrot.imitate.enderman -> entity.endermen.ambient
        added(329, 192, 1.5f); // entity.parrot.imitate.endermite -> entity.endermite.ambient
        added(330, 198, 1.5f); // entity.parrot.imitate.evocation_illager -> entity.evocation_illager.ambient
        added(331, 226, 1.5f); // entity.parrot.imitate.ghast -> entity.ghast.ambient
        added(332, 259, 1.5f); // entity.parrot.imitate.husk -> entity.husk.ambient
        added(333, 198, 1.3f); // entity.parrot.imitate.illusion_illager -> entity.evocation_illager.ambient
        added(334, 291, 1.5f); // entity.parrot.imitate.magmacube -> entity.magmacube.squish
        added(335, 321, 1.5f); // entity.parrot.imitate.polar_bear -> entity.polar_bear.ambient
        added(336, 337, 1.5f); // entity.parrot.imitate.shulker -> entity.shulker.ambient
        added(337, 347, 1.5f); // entity.parrot.imitate.silverfish -> entity.silverfish.ambient
        added(338, 351, 1.5f); // entity.parrot.imitate.skeleton -> entity.skeleton.ambient
        added(339, 363, 1.5f); // entity.parrot.imitate.slime -> entity.slime.squish
        added(340, 376, 1.5f); // entity.parrot.imitate.spider -> entity.spider.ambient
        added(341, 385, 1.5f); // entity.parrot.imitate.stray -> entity.stray.ambient
        added(342, 390, 1.5f); // entity.parrot.imitate.vex -> entity.vex.ambient
        added(343, 400, 1.5f); // entity.parrot.imitate.vindication_illager -> entity.vindication_illager.ambient
        added(344, 403, 1.5f); // entity.parrot.imitate.witch -> entity.witch.ambient
        added(345, 408, 1.5f); // entity.parrot.imitate.wither -> entity.wither.ambient
        added(346, 414, 1.5f); // entity.parrot.imitate.wither_skeleton -> entity.wither_skeleton.ambient
        added(347, 418, 1.5f); // entity.parrot.imitate.wolf -> entity.wolf.ambient
        added(348, 427, 1.5f); // entity.parrot.imitate.zombie -> entity.zombie.ambient
        added(349, 438, 1.5f); // entity.parrot.imitate.zombie_pigman -> entity.zombie_pig.ambient
        added(350, 442, 1.5f); // entity.parrot.imitate.zombie_villager -> entity.zombie_villager.ambient
        added(351, 155); // entity.parrot.step -> entity.chicken.step

        added(368, 316); // entity.player.hurt_drown -> entity.player.hurt
        added(369, 316); // entity.player.hurt_on_fire -> entity.player.hurt

        // No replacement sounds for these, since it could be confusing, the toast doesn't show up
        added(544, -1); // ui.toast.in
        added(545, -1); // ui.toast.out
        added(546, 317, 1.5f); // ui.toast.challenge_complete
    }
}

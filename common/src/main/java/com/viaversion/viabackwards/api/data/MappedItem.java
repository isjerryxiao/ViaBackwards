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
package com.viaversion.viabackwards.api.data;

import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.ChatRewriter;
import org.checkerframework.checker.nullness.qual.Nullable;

public class MappedItem {

    private final int id;
    private final String jsonName;
    private final Integer customModelData;

    public MappedItem(final int id, final String name) {
        this(id, name, null);
    }

    public MappedItem(final int id, final String name, @Nullable final Integer customModelData) {
        this.id = id;
        this.jsonName = ChatRewriter.legacyTextToJsonString("§f" + name, true);
        this.customModelData = customModelData;
    }

    public int getId() {
        return id;
    }

    public String getJsonName() {
        return jsonName;
    }

    public @Nullable Integer customModelData() {
        return customModelData;
    }
}

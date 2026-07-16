/*
 * prismatic
 * Copyright (c) 2026 Harrison Boyd
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package dev.hboyd.prismatic.paper.persistent_data.type;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.ComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

/**
 * Persistent data type for {@link Component}s.
 *
 * <p>
 * Provides data types which serialize into MiniMessage, plain text legacy section or legacy ampersand format. Some
 * formats may not preserve all data.
 * </p>
 *
 * @see org.bukkit.persistence.PersistentDataType
 * @see PersistentDataContainer
 */
public final class SerializedComponentPersistentDataType implements PersistentDataType<String, Component> {
    public static final SerializedComponentPersistentDataType MINIMESSAGE = new SerializedComponentPersistentDataType(MiniMessage.miniMessage());
    public static final SerializedComponentPersistentDataType PLAINTEXT = new SerializedComponentPersistentDataType(PlainTextComponentSerializer.plainText());
    public static final SerializedComponentPersistentDataType LEGACY_SECTION = new SerializedComponentPersistentDataType(LegacyComponentSerializer.legacySection());
    public static final SerializedComponentPersistentDataType LEGACY_AMPERSAND = new SerializedComponentPersistentDataType(LegacyComponentSerializer.legacyAmpersand());

    private final ComponentSerializer<Component, ? extends Component, String> serializer;

    private SerializedComponentPersistentDataType(final ComponentSerializer<Component, ? extends Component, String> serializer) {
        this.serializer = serializer;
    }

    @Override
    public Class<String> getPrimitiveType() {
        return String.class;
    }

    @Override
    public Class<Component> getComplexType() {
        return Component.class;
    }

    @Override
    public String toPrimitive(final Component component, final PersistentDataAdapterContext context) {
        return this.serializer.serialize(component);
    }

    @Override
    public Component fromPrimitive(final String data, final PersistentDataAdapterContext context) {
        return this.serializer.deserialize(data);
    }
}

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

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * Persistent data type for {@link UUID}s.
 *
 * @see org.bukkit.persistence.PersistentDataType
 * @see PersistentDataContainer
 */
public final class UUIDPersistentDataType implements PersistentDataType<byte[], UUID> {
    public static final UUIDPersistentDataType INSTANCE = new UUIDPersistentDataType();

    private UUIDPersistentDataType() {}

    @Override
    public Class<byte[]> getPrimitiveType() {
        return byte[].class;
    }

    @Override
    public Class<UUID> getComplexType() {
        return UUID.class;
    }

    @Override
    public byte[] toPrimitive(final UUID complex, final PersistentDataAdapterContext context) {
        final ByteBuffer bb = ByteBuffer.wrap(new byte[16]);

        bb.putLong(complex.getMostSignificantBits());
        bb.putLong(complex.getLeastSignificantBits());

        return bb.array();
    }

    @Override
    public UUID fromPrimitive(final byte[] primitive, final PersistentDataAdapterContext context) {
        final ByteBuffer byteBuffer = ByteBuffer.wrap(primitive);

        return new UUID(byteBuffer.getLong(), byteBuffer.getLong());
    }
}

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

package dev.hboyd.prismatic.paper.nbt;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.BinaryTagIO;
import net.kyori.adventure.nbt.BinaryTagType;
import net.kyori.adventure.nbt.BinaryTagTypes;
import net.kyori.adventure.nbt.ByteArrayBinaryTag;
import net.kyori.adventure.nbt.ByteBinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.DoubleBinaryTag;
import net.kyori.adventure.nbt.FloatBinaryTag;
import net.kyori.adventure.nbt.IntArrayBinaryTag;
import net.kyori.adventure.nbt.IntBinaryTag;
import net.kyori.adventure.nbt.LongArrayBinaryTag;
import net.kyori.adventure.nbt.LongBinaryTag;
import net.kyori.adventure.nbt.ShortBinaryTag;
import net.kyori.adventure.nbt.StringBinaryTag;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jspecify.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A persistent data container which is backed by a {@link CompoundBinaryTag}.
 *
 * @see PersistentDataContainer
 */
public class NBTPersistentDataContainer implements PersistentDataContainer {
    private static final Map<Object, BinaryTagType<?>> BINARY_TAG_TYPE_MAP = Map.of(
            Byte.class, BinaryTagTypes.BYTE,
            Short.class, BinaryTagTypes.SHORT,
            Integer.class, BinaryTagTypes.INT,
            Long.class, BinaryTagTypes.LONG,
            Float.class, BinaryTagTypes.FLOAT,
            Double.class, BinaryTagTypes.DOUBLE,
            String.class, BinaryTagTypes.STRING,
            byte[].class, BinaryTagTypes.BYTE_ARRAY,
            int[].class, BinaryTagTypes.INT_ARRAY,
            long[].class, BinaryTagTypes.LONG_ARRAY
    );

    private CompoundBinaryTag compoundBinaryTag;

    /**
     * Construct an empty nbt persistent data container.
     */
    public NBTPersistentDataContainer() {
        this(CompoundBinaryTag.empty());
    }

    /**
     * Construct a nbt persistent data container with the given compound binary tag.
     *
     * @param compoundBinaryTag the tag
     */
    public NBTPersistentDataContainer(final CompoundBinaryTag compoundBinaryTag) {
        this.compoundBinaryTag = compoundBinaryTag;
    }

    /**
     * Get the compound binary tag.
     *
     * @return a compound binary tag
     */
    public CompoundBinaryTag compoundBinaryTag() {
        return this.compoundBinaryTag;
    }

    @Override
    public <P, C> void set(final NamespacedKey key, final PersistentDataType<P, C> type, final C value) {
        final P primitive = type.toPrimitive(value, NBTPersistentDataAdapterContext.INSTANCE);
        if (type.getPrimitiveType() == Byte.class)
            this.compoundBinaryTag = this.compoundBinaryTag.putByte(key.asString(), (Byte) primitive);
        else if (type.getPrimitiveType() == Short.class)
            this.compoundBinaryTag = this.compoundBinaryTag.putShort(key.asString(), (Short) primitive);
        else if (type.getPrimitiveType() == Integer.class)
            this.compoundBinaryTag = this.compoundBinaryTag.putInt(key.asString(), (Integer) primitive);
        else if (type.getPrimitiveType() == Long.class)
            this.compoundBinaryTag = this.compoundBinaryTag.putLong(key.asString(), (Long) primitive);
        else if (type.getPrimitiveType() == Float.class)
            this.compoundBinaryTag = this.compoundBinaryTag.putFloat(key.asString(), (Float) primitive);
        else if (type.getPrimitiveType() == Double.class)
            this.compoundBinaryTag = this.compoundBinaryTag.putDouble(key.asString(), (Double) primitive);
        else if (type.getPrimitiveType() == String.class)
            this.compoundBinaryTag = this.compoundBinaryTag.putString(key.asString(), (String) primitive);
        else if (type.getPrimitiveType() == byte[].class)
            this.compoundBinaryTag = this.compoundBinaryTag.putByteArray(key.asString(), (byte[]) primitive);
        else if (type.getPrimitiveType() == int[].class)
            this.compoundBinaryTag = this.compoundBinaryTag.putIntArray(key.asString(), (int[]) primitive);
        else if (type.getPrimitiveType() == long[].class)
            this.compoundBinaryTag = this.compoundBinaryTag.putLongArray(key.asString(), (long[]) primitive);
        else if (type.getPrimitiveType() == PersistentDataContainer.class) {
            try {
                final CompoundBinaryTag valueTag = BinaryTagIO.reader()
                        .read(new ByteArrayInputStream(((PersistentDataContainer) primitive).serializeToBytes()));
                this.compoundBinaryTag = this.compoundBinaryTag.put(key.asString(), valueTag);
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        } else throw new RuntimeException("Unknown primitive type: " + type.getPrimitiveType());
    }

    @Override
    public void remove(final NamespacedKey key) {
        Objects.requireNonNull(key, "key");

        this.compoundBinaryTag = this.compoundBinaryTag.remove(key.asString());
    }

    @Override
    public void readFromBytes(final byte[] bytes, final boolean clear) throws IOException {
        final CompoundBinaryTag readTag = BinaryTagIO.reader().read(new ByteArrayInputStream(bytes));

        if (clear) this.compoundBinaryTag = readTag;
        else this.compoundBinaryTag = this.compoundBinaryTag.put(readTag);
    }

    @Override
    public <P, C> boolean has(final NamespacedKey key, final PersistentDataType<P, C> type) {
        Objects.requireNonNull(key, "key");

        return this.compoundBinaryTag.contains(key.asString(), BINARY_TAG_TYPE_MAP.get(type.getPrimitiveType()));
    }

    @Override
    public boolean has(final NamespacedKey key) {
        Objects.requireNonNull(key, "key");

        return this.compoundBinaryTag.contains(key.asString());
    }

    @SuppressWarnings("unchecked")
    @Override
    public @Nullable <P, C> C get(final NamespacedKey key, final PersistentDataType<P, C> type) {
        if (!this.compoundBinaryTag.contains(key.asString())) return null;

        final P primitive;
        if (type.getPrimitiveType() == Byte.class)
            primitive = (P) Byte.valueOf(this.compoundBinaryTag.getByte(key.asString()));
        else if (type.getPrimitiveType() == Short.class)
            primitive = (P) Short.valueOf(this.compoundBinaryTag.getShort(key.asString()));
        else if (type.getPrimitiveType() == Integer.class)
            primitive = (P) Integer.valueOf(this.compoundBinaryTag.getInt(key.asString()));
        else if (type.getPrimitiveType() == Long.class)
            primitive = (P) Long.valueOf(this.compoundBinaryTag.getLong(key.asString()));
        else if (type.getPrimitiveType() == Float.class)
            primitive = (P) Float.valueOf(this.compoundBinaryTag.getFloat(key.asString()));
        else if (type.getPrimitiveType() == Double.class)
            primitive = (P) Double.valueOf(this.compoundBinaryTag.getDouble(key.asString()));
        else if (type.getPrimitiveType() == String.class)
            primitive = (P) this.compoundBinaryTag.getString(key.asString());
        else if (type.getPrimitiveType() == byte[].class)
            primitive = (P) this.compoundBinaryTag.getByteArray(key.asString());
        else if (type.getPrimitiveType() == int[].class)
            primitive = (P) this.compoundBinaryTag.getIntArray(key.asString());
        else if (type.getPrimitiveType() == long[].class)
            primitive = (P) this.compoundBinaryTag.getLongArray(key.asString());
        else if (type.getPrimitiveType() == PersistentDataContainer.class) {
            final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            try {
                BinaryTagIO.writer().write(this.compoundBinaryTag.getCompound(key.asString()), byteArrayOutputStream);
                final PersistentDataContainer pdc = NBTPersistentDataAdapterContext.INSTANCE.newPersistentDataContainer();
                pdc.readFromBytes(byteArrayOutputStream.toByteArray());
                primitive = (P) pdc;
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        } else throw new RuntimeException("Unknown primitive type: " + type.getPrimitiveType());

        return type.fromPrimitive(primitive, NBTPersistentDataAdapterContext.INSTANCE);
    }

    @Override
    public <P, C> C getOrDefault(final NamespacedKey key, final PersistentDataType<P, C> type, final C defaultValue) {
        return Optional.ofNullable(this.get(key, type)).orElse(defaultValue);
    }

    @Override
    public Set<NamespacedKey> getKeys() {
        return this.compoundBinaryTag.keySet().stream()
                .map(NamespacedKey::fromString)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean isEmpty() {
        return this.compoundBinaryTag.isEmpty();
    }

    @Override
    public void copyTo(final PersistentDataContainer other, final boolean replace) {
        for (final NamespacedKey key : this.getKeys()) {
            if (!replace && other.has(key)) continue;

            final BinaryTag tag = this.compoundBinaryTag.get(key.asString());

            if (tag.type() == BinaryTagTypes.BYTE)
                other.set(key, PersistentDataType.BYTE, ((ByteBinaryTag) tag).value());
            else if (tag.type() == BinaryTagTypes.SHORT)
                other.set(key, PersistentDataType.SHORT, ((ShortBinaryTag) tag).value());
            else if (tag.type() == BinaryTagTypes.INT)
                other.set(key, PersistentDataType.INTEGER, ((IntBinaryTag) tag).value());
            else if (tag.type() == BinaryTagTypes.LONG)
                other.set(key, PersistentDataType.LONG, ((LongBinaryTag) tag).value());
            else if (tag.type() == BinaryTagTypes.FLOAT)
                other.set(key, PersistentDataType.FLOAT, ((FloatBinaryTag) tag).value());
            else if (tag.type() == BinaryTagTypes.DOUBLE)
                other.set(key, PersistentDataType.DOUBLE, ((DoubleBinaryTag) tag).value());
            else if (tag.type() == BinaryTagTypes.STRING)
                other.set(key, PersistentDataType.STRING, ((StringBinaryTag) tag).value());
            else if (tag.type() == BinaryTagTypes.BYTE_ARRAY)
                other.set(key, PersistentDataType.BYTE_ARRAY, ((ByteArrayBinaryTag) tag).value());
            else if (tag.type() == BinaryTagTypes.INT_ARRAY)
                other.set(key, PersistentDataType.INTEGER_ARRAY, ((IntArrayBinaryTag) tag).value());
            else if (tag.type() == BinaryTagTypes.LONG_ARRAY)
                other.set(key, PersistentDataType.LONG_ARRAY, ((LongArrayBinaryTag) tag).value());
        }
    }

    @Override
    public PersistentDataAdapterContext getAdapterContext() {
        return NBTPersistentDataAdapterContext.INSTANCE;
    }

    @Override
    public byte[] serializeToBytes() throws IOException {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        BinaryTagIO.writer().write(this.compoundBinaryTag, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    @Override
    public int getSize() {
        return this.compoundBinaryTag.size();
    }
}

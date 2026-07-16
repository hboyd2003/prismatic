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

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Objects;

/**
 * Persistent data type for {@link TextColor}s.
 *
 * <p>
 * Presumes colors which begin with a {@code #} are hex and all others are a {@link NamedTextColor}.
 * </p>
 *
 * @see org.bukkit.persistence.PersistentDataType
 * @see PersistentDataContainer
 */
public final class TextColorPersistentDataType implements PersistentDataType<String, TextColor> {
    public static final TextColorPersistentDataType INSTANCE = new TextColorPersistentDataType();

    private TextColorPersistentDataType() {
    }

    @Override
    public Class<String> getPrimitiveType() {
        return String.class;
    }

    @Override
    public Class<TextColor> getComplexType() {
        return TextColor.class;
    }

    @Override
    public String toPrimitive(final TextColor textColor, final PersistentDataAdapterContext context) {
        if (textColor instanceof final NamedTextColor namedTextColor) return namedTextColor.toString();

        return textColor.asHexString();
    }

    @Override
    public TextColor fromPrimitive(final String data, final PersistentDataAdapterContext context) {
        if (!data.startsWith(TextColor.HEX_PREFIX))
            return Objects.requireNonNull(NamedTextColor.NAMES.value(data));

        return TextColor.fromHexString(data);
    }
}

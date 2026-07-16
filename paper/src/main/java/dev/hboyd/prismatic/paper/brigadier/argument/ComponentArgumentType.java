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

package dev.hboyd.prismatic.paper.brigadier.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.ComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

/**
 * An argument type that parses a string into a {@link Component}.
 * Supports plaintext, minimessage, legacy section and legacy ampersand formats.
 */
public final class ComponentArgumentType implements CustomArgumentType<Component, String> {
    /**
     * An argument type that parses the input into a {@link Component} using the MiniMessage format.
     */
    public static final ComponentArgumentType MINIMESSAGE = new ComponentArgumentType(MiniMessage.miniMessage());
    /**
     * An argument type that parses the input into a {@link Component}.
     */
    public static final ComponentArgumentType PLAINTEXT = new ComponentArgumentType(PlainTextComponentSerializer.plainText());
    /**
     * An argument type that parses the input into a {@link Component} using the legacy section format.
     */
    public static final ComponentArgumentType LEGACY_SECTION = new ComponentArgumentType(LegacyComponentSerializer.legacySection());
    /**
     * An argument type that parses the input into a {@link Component} using the legacy ampersand format.
     */
    public static final ComponentArgumentType LEGACY_AMPERSAND = new ComponentArgumentType(LegacyComponentSerializer.legacyAmpersand());

    private final ComponentSerializer<Component, ? extends Component, String> serializer;

    private ComponentArgumentType(final ComponentSerializer<Component, ? extends Component, String> serializer) {
        this.serializer = serializer;
    }

    @Override
    public Component parse(final StringReader reader) throws CommandSyntaxException {
        return this.serializer.deserialize(reader.readString());
    }

    @Override
    public <S> Component parse(final StringReader reader, final S source) throws CommandSyntaxException {
        return CustomArgumentType.super.parse(reader, source);
    }

    @Override
    public ArgumentType<String> getNativeType() {
        return StringArgumentType.string();
    }
}

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

package dev.hboyd.prismatic.paper;

import com.mojang.brigadier.Message;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;

import java.util.stream.Stream;

/**
 * Utilities for {@link com.mojang.brigadier.Message}.
 */
public final class MessageUtil {
    private MessageUtil() {}

    /**
     * Creates a translatable message with the given arguments.
     *
     * @param key  the translation key
     * @param args the translation arguments
     * @return the serialized translatable message
     */
    public static Message translatableMessage(final String key, final ComponentLike... args) {
        return MessageComponentSerializer.message().serialize(Component.translatable(key, args));
    }

    /**
     * Creates a translatable message with the given arguments.
     *
     * @param key  the translation key
     * @param args the translation arguments
     * @return the serialized translatable message
     */
    public static Message translatableMessage(final String key, final Object... args) {
        final ComponentLike[] convertedArgs = Stream.of(args)
                .map(arg -> arg instanceof final ComponentLike component
                        ? component
                        : Component.text(arg.toString()))
                .toArray(ComponentLike[]::new);

        return MessageComponentSerializer.message().serialize(Component.translatable(key, convertedArgs));
    }
}

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

package dev.hboyd.prismatic.paper.brigadier;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType;
import com.mojang.brigadier.exceptions.Dynamic4CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicNCommandExceptionType;
import dev.hboyd.prismatic.paper.MessageUtil;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.jetbrains.annotations.Contract;

/**
 * Utilities for commands.
 */
public final class CommandUtil {
    private CommandUtil() {
    }

    /**
     * Sends a translatable system chat message to the executor of the given command context. If no executor is present
     * the message is sent to the sender.
     *
     * @param commandContext a command context
     * @param key            a translation key
     * @param args           the arguments to be inserted into the translatable message
     */
    public static void sendTranslatableResponse(final CommandContext<CommandSourceStack> commandContext,
                                                final String key,
                                                final ComponentLike... args) {
        Audience audience = commandContext.getSource().getExecutor();
        if (audience == null) audience = commandContext.getSource().getSender();

        audience.sendMessage(Component.translatable(key, args));
    }

    /**
     * Converts the given command syntax exception to text and sends it to the given audience as a system chat message.
     *
     * @param audience  an audience to receive the error message
     * @param exception a syntax exception
     */
    public static void sendSyntaxExceptionMessage(final Audience audience, final CommandSyntaxException exception) {
        Component messageComponent = MessageComponentSerializer.message().deserialize(exception.getRawMessage());
        if (exception.getContext() != null) {
            messageComponent = messageComponent.append(Component.text(" at position " + exception.getCursor() + ": " + exception.getContext()));
        }
        audience.sendMessage(messageComponent);
    }

    /**
     * Creates a dynamic command exception type that uses a translation key to generate its message. Non-component
     * arguments are converted into components when building the translation component.
     *
     * <p>Similar to:
     * {@snippet :
     * new DynamicCommandExceptionType(arg ->
     *     MessageComponentSerializer.message().serialize(Component.translatable(key, arg)));
     * }</p>
     *
     * @param translationKey a translation key
     * @return a dynamic exception type that accepts one argument
     */
    @Contract(value = "_ -> new", pure = true)
    public static DynamicCommandExceptionType dynamicExceptionOfTranslation(final String translationKey) {
        return new DynamicCommandExceptionType(arg1 ->
                MessageUtil.translatableMessage(translationKey, arg1));
    }

    /**
     * Creates a dynamic command exception type that uses a translation key for its message. Non-component arguments are
     * converted into components when building the translation component.
     *
     * <p>Similar to:
     * {@snippet :
     * new Dynamic2CommandExceptionType((arg1, arg2) ->
     *     MessageComponentSerializer.message().serialize(Component.translatable(key, arg1, arg2)));
     * }</p>
     *
     * @param translationKey a translation key
     * @return a dynamic exception type that accepts two arguments
     */
    @Contract(value = "_ -> new", pure = true)
    public static Dynamic2CommandExceptionType dynamic2ExceptionOfTranslation(final String translationKey) {
        return new Dynamic2CommandExceptionType((arg1, arg2) ->
                MessageUtil.translatableMessage(translationKey, arg1, arg2));
    }

    /**
     * Creates a dynamic command exception type that uses a translation key for its message. Non-component arguments are
     * converted into components when building the translation component.
     *
     * <p>Similar to:
     * {@snippet :
     * new Dynamic3CommandExceptionType((arg1, arg2, arg3) ->
     *     MessageComponentSerializer.message().serialize(Component.translatable(key, arg1, arg2, arg3)));
     * }</p>
     *
     * @param translationKey a translation key
     * @return a dynamic exception type that accepts three arguments
     */
    @Contract(value = "_ -> new", pure = true)
    public static Dynamic3CommandExceptionType dynamic3ExceptionOfTranslation(final String translationKey) {
        return new Dynamic3CommandExceptionType((arg1, arg2, arg3) ->
                MessageUtil.translatableMessage(translationKey, arg1, arg2, arg3));
    }

    /**
     * Creates a dynamic command exception type that uses a translation key for its message. Non-component arguments are
     * converted into components when building the translation component.
     *
     * <p>Similar to:
     * {@snippet :
     * new Dynamic4CommandExceptionType((arg1, arg2, arg3, arg4) ->
     *     MessageComponentSerializer.message().serialize(Component.translatable(key, arg1, arg2, arg3, arg4)));
     * }</p>
     *
     * @param translationKey a translation key
     * @return a dynamic exception type that accepts three arguments
     */
    @Contract(value = "_ -> new", pure = true)
    public static Dynamic4CommandExceptionType dynamic4ExceptionOfTranslation(final String translationKey) {
        return new Dynamic4CommandExceptionType((arg1, arg2, arg3, arg4) ->
                MessageUtil.translatableMessage(translationKey, arg1, arg2, arg3, arg4));
    }

    /**
     * Creates a dynamic command exception type that uses a translation key for its message. Non-component arguments are
     * converted into components when building the translation component.
     *
     * <p>Similar to:
     * {@snippet :
     * new DynamicNCommandExceptionType(args ->
     *     MessageComponentSerializer.message().serialize(Component.translatable(key, args)));
     * }</p>
     *
     * @param translationKey a translation key
     * @return a dynamic exception type that accepts three arguments
     */
    @Contract(value = "_ -> new", pure = true)
    public static DynamicNCommandExceptionType dynamicNExceptionOfTranslation(final String translationKey) {
        return new DynamicNCommandExceptionType(args -> MessageUtil.translatableMessage(translationKey, args));
    }
}

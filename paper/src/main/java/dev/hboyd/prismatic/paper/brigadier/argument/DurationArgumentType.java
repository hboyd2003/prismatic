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
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.hboyd.prismatic.paper.MessageUtil;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;

import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.concurrent.CompletableFuture;

/**
 * An argument for {@link Duration}s.
 */
public final class DurationArgumentType implements CustomArgumentType<Duration, String> {
    public static final DurationArgumentType INSTANCE = new DurationArgumentType();

    public static final SimpleCommandExceptionType INVALID_DURATION_EXCEPTION = new SimpleCommandExceptionType(
            MessageUtil.translatableMessage("argument.duration.invalid"));

    private DurationArgumentType() {}

    @Override
    public Duration parse(final StringReader reader) throws CommandSyntaxException {
        reader.readString();
        try {
            return Duration.parse(reader.readString());
        } catch (final DateTimeParseException e) {
            reader.setCursor(reader.getCursor() + e.getErrorIndex());
            final CommandSyntaxException invalidDurationException = INVALID_DURATION_EXCEPTION.createWithContext(reader);
            reader.setCursor(reader.getCursor() - e.getErrorIndex());
            throw invalidDurationException;
        }
    }

    @Override
    public ArgumentType<String> getNativeType() {
        return StringArgumentType.string();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        final String input = builder.getRemaining();

        if (input.isEmpty()) {
            builder.suggest("P");
            return builder.buildFuture();
        }

        if (input.endsWith("-")) {
            for (char c = '0'; c <= '9'; c++) {
                builder.suggest(String.valueOf(c));
            }
            return builder.buildFuture();
        }

        if (input.equals("P")) {
            for (char c = '0'; c <= '9'; c++) {
                builder.suggest(String.valueOf(c));
            }
            builder.suggest("T");
            return builder.buildFuture();
        }

        final char lastChar = input.charAt(input.length() - 1);

        if (Character.isDigit(lastChar)) {
            for (char c = '0'; c <= '9'; c++) {
                builder.suggest(String.valueOf(c));
            }

            if (input.contains("T")) {
                builder.suggest("H");
                builder.suggest("M");
                builder.suggest("S");
                builder.suggest(".");
                builder.suggest(",");
            } else {
                builder.suggest("Y");
                builder.suggest("W");
                builder.suggest("M");
                builder.suggest("D");
                builder.suggest(".");
                builder.suggest(",");
                if (input.length() >= 5) {
                    builder.suggest("-");
                }
            }
        } else if (lastChar == 'Y' || lastChar == 'W' || lastChar == 'M' || lastChar == 'D' || lastChar == 'H' || lastChar == 'S') {
            for (int i = 0; i <= 9; i++) {
                builder.suggest(i);
            }
            builder.suggest(".");
            builder.suggest(",");
        } else if (lastChar == 'T' || lastChar == '.' || lastChar == ',') {
            for (int i = 0; i <= 9; i++) {
                builder.suggest(i);
            }
        }

        return builder.buildFuture();
    }

}

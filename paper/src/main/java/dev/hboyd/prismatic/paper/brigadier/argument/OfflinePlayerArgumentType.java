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
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.hboyd.prismatic.paper.MessageUtil;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import io.papermc.paper.command.brigadier.argument.resolvers.PlayerProfileListResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.util.Buildable;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * An argument for {@link OfflinePlayer}s.
 */
public final class OfflinePlayerArgumentType implements CustomArgumentType<OfflinePlayerArgumentTypeResolver, PlayerProfileListResolver>, Buildable<OfflinePlayerArgumentType, OfflinePlayerArgumentType.Builder> {
    public static final OfflinePlayerArgumentType OFFLINE_PLAYERS =
            new OfflinePlayerArgumentType(() -> Arrays.asList(Bukkit.getOfflinePlayers()),
                    null,
                    null,
                    false);
    public static final OfflinePlayerArgumentType OFFLINE_PLAYER =
            new OfflinePlayerArgumentType(() -> Arrays.asList(Bukkit.getOfflinePlayers()),
                    null,
                    null,
                    true);

    private final Supplier<Collection<OfflinePlayer>> offlinePlayerSupplier;
    private final @Nullable Predicate<OfflinePlayer> offlinePlayerFilter;
    private final boolean singleSelector;
    private final @Nullable SimpleCommandExceptionType notMatchedException;

    private OfflinePlayerArgumentType(final Supplier<Collection<OfflinePlayer>> offlinePlayerSupplier,
                                      @Nullable final Predicate<OfflinePlayer> offlinePlayerFilter,
                                      @Nullable final SimpleCommandExceptionType notMatchedException,
                                      final boolean singleSelector) {
        this.offlinePlayerSupplier = offlinePlayerSupplier;
        this.offlinePlayerFilter = offlinePlayerFilter;
        this.singleSelector = singleSelector;
        this.notMatchedException = notMatchedException;
    }

    @Override
    public OfflinePlayerArgumentTypeResolver parse(final StringReader reader) throws CommandSyntaxException {
        return new OfflinePlayerArgumentTypeResolver(ArgumentTypes.playerProfiles().parse(reader),
                this.getFilteredPlayers(),
                this.singleSelector,
                this.notMatchedException);
    }

    @Override
    public ArgumentType<PlayerProfileListResolver> getNativeType() {
        return ArgumentTypes.playerProfiles();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        final String input = builder.getRemaining().trim();
        // Use the playerProfiles parser only for selectors for performance
        if (input.startsWith("@")) return this.singleSelector
                    ? ArgumentTypes.player().listSuggestions(context, builder)
                    : ArgumentTypes.playerProfiles().listSuggestions(context, builder);

        if (input.isEmpty()) {
            applySelectorSuggestions(builder);
            for (final OfflinePlayer offlinePlayer : this.getFilteredPlayers()) {
                final String name = offlinePlayer.getName();
                if (name == null) builder.suggest(offlinePlayer.getUniqueId().toString());
                else builder.suggest(name, MessageComponentSerializer.message().serialize(Component.text(offlinePlayer.getUniqueId().toString())));
            }
        } else {
            for (final OfflinePlayer offlinePlayer : this.getFilteredPlayers()) {
                final String name = offlinePlayer.getName();
                if (name != null && name.startsWith(input))
                    builder.suggest(name, MessageComponentSerializer.message().serialize(Component.text(offlinePlayer.getUniqueId().toString())));
                else if (offlinePlayer.getUniqueId().toString().startsWith(input))
                    builder.suggest(offlinePlayer.getUniqueId().toString());
            }
        }

        return builder.buildFuture();
    }

    /**
     * Create a builder for an offline player argument.
     *
     * @return the builder
     */
    @Contract(value = " -> new", pure = true)
    public static Builder offlinePlayerBuilder() {
        return OFFLINE_PLAYER.toBuilder();
    }

    /**
     * Create a builder for an offline players argument.
     *
     * @return the builder
     */
    @Contract(value = " -> new", pure = true)
    public static Builder offlinePlayersBuilder() {
        return OFFLINE_PLAYERS.toBuilder();
    }

    @Override
    public Builder toBuilder() {
        return new Builder(this);
    }

    private Collection<OfflinePlayer> getFilteredPlayers() {
        return this.offlinePlayerSupplier.get().stream()
                .filter(player -> this.offlinePlayerFilter == null || this.offlinePlayerFilter.test(player))
                .toList();
    }

    private static void applySelectorSuggestions(final SuggestionsBuilder builder) {
        builder.suggest("@a", MessageUtil.translatableMessage("argument.entity.selector.allPlayers"));
        builder.suggest("@e", MessageUtil.translatableMessage("argument.entity.selector.allEntities"));
        builder.suggest("@n", MessageUtil.translatableMessage("argument.entity.selector.nearestEntity"));
        builder.suggest("@p", MessageUtil.translatableMessage("argument.entity.selector.nearestPlayer"));
        builder.suggest("@r", MessageUtil.translatableMessage("argument.entity.selector.randomPlayer"));
        builder.suggest("@s", MessageUtil.translatableMessage("argument.entity.selector.self"));
    }

    /**
     * A builder for a {@link OfflinePlayerArgumentType}.
     */
    public static final class Builder implements Buildable.Builder<OfflinePlayerArgumentType> {
        private Supplier<Collection<OfflinePlayer>> offlinePlayerSupplier;
        private @Nullable Predicate<OfflinePlayer> offlinePlayerFilter;
        private @Nullable SimpleCommandExceptionType notMatchedException;
        private boolean singleSelector;

        private Builder(final OfflinePlayerArgumentType offlinePlayerArgumentType) {
            this.offlinePlayerSupplier = offlinePlayerArgumentType.offlinePlayerSupplier;
            this.offlinePlayerFilter = offlinePlayerArgumentType.offlinePlayerFilter;
            this.notMatchedException = offlinePlayerArgumentType.notMatchedException;
            this.singleSelector = offlinePlayerArgumentType.singleSelector;
        }

        /**
         * Set the supplier of offline players.
         *
         * @param offlinePlayerSupplier the supplier
         * @return this builder
         */
        @Contract(value = "_ -> this", mutates = "this")
        public Builder offlinePlayerSupplier(final Supplier<Collection<OfflinePlayer>> offlinePlayerSupplier) {
            this.offlinePlayerSupplier = offlinePlayerSupplier;
            return this;
        }

        /**
         * Set the filter used to determine of an offline player should be considered.
         *
         * @param offlinePlayerFilter the filter
         * @return this builder
         */
        @Contract(value = "_ -> this", mutates = "this")
        public Builder offlinePlayerFilter(final Predicate<OfflinePlayer> offlinePlayerFilter) {
            this.offlinePlayerFilter = offlinePlayerFilter;
            return this;
        }

        /**
         * Set the exception to throw when an offline player exists but is not matched by the filter.
         *
         * @param notMatchedException the exception
         * @return this builder
         */
        @Contract(value = "_ -> this", mutates = "this")
        public Builder notMatchedException(@Nullable final SimpleCommandExceptionType notMatchedException) {
            this.notMatchedException = notMatchedException;
            return this;
        }

        /**
         * Set weather to restrict the selector to one offline player.
         *
         * @param singleSelector if the selector is restricted to one offline players
         * @return this builder
         */
        @Contract(value = "_ -> this", mutates = "this")
        public Builder singleSelector(final boolean singleSelector) {
            this.singleSelector = singleSelector;
            return this;
        }

        @Override
        @Contract(value = " -> new", pure = true)
        public OfflinePlayerArgumentType build() {
            return new OfflinePlayerArgumentType(this.offlinePlayerSupplier,
                    this.offlinePlayerFilter,
                    this.notMatchedException,
                    this.singleSelector);
        }
    }
}

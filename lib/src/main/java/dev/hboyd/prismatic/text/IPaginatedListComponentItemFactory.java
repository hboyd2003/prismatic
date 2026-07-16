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

package dev.hboyd.prismatic.text;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.ComponentLike;
import org.checkerframework.checker.index.qual.Positive;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

/**
 * A factory items in a paginated list component.
 *
 * @see PaginatedListComponent
 */
public interface IPaginatedListComponentItemFactory {
    /**
     * Create a paginated list component item factory with the given items.
     *
     * @param items the items
     * @return an item factory
     */
    @Contract("_ -> new")
    static IPaginatedListComponentItemFactory of(final List<? extends ComponentLike> items) {
        return new ListPaginatedListComponentItemFactory(Objects.requireNonNull(items));
    }

    /**
     * Create a paginated list component item factory with the given items.
     *
     * @param items the items
     * @return an item factory
     */
    @Contract("_ -> new")
    static IPaginatedListComponentItemFactory of(final Collection<? extends ComponentLike> items) {
        return new ListPaginatedListComponentItemFactory(Objects.requireNonNull(items).stream().toList());
    }

    /**
     * Create a paginated list component item factory using a predicate to determine if an item is available and a
     * function which supplies the item.
     *
     * @param isAvailablePredicate the predicate
     * @param itemSupplier         the supplier
     * @return an item factory
     */
    @Contract(value = "_, _ -> new", pure = true)
    static IPaginatedListComponentItemFactory of(final BiPredicate<Integer, Audience> isAvailablePredicate,
                                                 final BiFunction<Integer, Audience, ? extends ComponentLike> itemSupplier) {
        return new IPaginatedListComponentItemFactory() {
            @Override
            public boolean isAvailable(final int index, final Audience audience) {
                return isAvailablePredicate.test(index, audience);
            }

            @Override
            public ComponentLike get(final int index, final Audience audience) throws IndexOutOfBoundsException {
                return itemSupplier.apply(index, audience);
            }
        };
    }

    /**
     * Create a paginated list component item factory the given supplier.
     *
     * @param itemSupplier the supplier
     * @return an item factory
     */
    @Contract(value = "_ -> new", pure = true)
    static IPaginatedListComponentItemFactory of(final BiFunction<Integer, Audience, ? extends ComponentLike> itemSupplier) {
        return of((index, audience) -> {
            try {
                //noinspection ConstantValue
                return itemSupplier.apply(index, audience) != null;
            } catch (final RuntimeException _) {
                return false;
            }
        }, itemSupplier);
    }

    /**
     * Create a paginated list component item factory which will has no items and will throw an index out of bounds
     * exception if used.
     *
     * @return the factory
     */
    @Contract(value = " -> new", pure = true)
    static IPaginatedListComponentItemFactory empty() {
        return of((_, _) -> false,
                (_, _) -> {
                    throw new IndexOutOfBoundsException();
                });
    }

    /**
     * Get weather an item exists at the given index and is visible to the given audience.
     *
     * @param index    an index
     * @param audience an audience
     * @return weather an item is available
     */
    @ApiStatus.OverrideOnly
    boolean isAvailable(@Positive int index, Audience audience);

    /**
     * Get the item at the given index.
     *
     * @param index    the index
     * @param audience the audience
     * @return a component
     * @throws IndexOutOfBoundsException when no item exists
     */
    @ApiStatus.OverrideOnly
    ComponentLike get(int index, Audience audience) throws IndexOutOfBoundsException;
}

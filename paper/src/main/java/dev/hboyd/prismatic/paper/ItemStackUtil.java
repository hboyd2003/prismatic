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

import net.kyori.adventure.nbt.BinaryTagIO;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.text.renderer.TranslatableComponentRenderer;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Objects;

/**
 * Utilities for {@link ItemStack}s.
 */
public final class ItemStackUtil {

    private ItemStackUtil() {
        /* This utility class should not be instantiated */
    }

    /**
     * Render all translatable components for the given item stack with the given locale using the given renderer.
     *
     * <p>A copy of the item stack is NOT made. It is modified directly.</p>
     *
     * @param itemStack the item stack
     * @param locale    the locale
     * @param renderer  the renderer
     * @return the given item stack
     */
    @Contract("_, _, _ -> param1")
    public static ItemStack renderTranslations(final ItemStack itemStack,
                                               final Locale locale,
                                               final TranslatableComponentRenderer<Locale> renderer) {
        Objects.requireNonNull(itemStack, "itemStack");
        Objects.requireNonNull(locale, "locale");
        Objects.requireNonNull(renderer, "renderer");

        itemStack.editMeta(itemMeta -> {
            if (itemMeta.hasCustomName()) {
                //noinspection DataFlowIssue
                itemMeta.customName(renderer.render(itemMeta.customName(), locale));
            }
            if (itemMeta.hasItemName()) {
                itemMeta.itemName(renderer.render(itemMeta.itemName(), locale));
            }
            if (itemMeta.hasLore()) {
                //noinspection DataFlowIssue
                itemMeta.lore(itemMeta.lore().stream()
                        .map(component -> renderer.render(component, locale))
                        .toList());
            }
        });

        return itemStack;
    }

    /**
     * Render all translatable components for the given item stack with the given locale using a renderer backed by the
     * global translator.
     *
     * <p>A copy of the item stack is NOT made. It is modified directly.</p>
     *
     * @param itemStack the item stack
     * @param locale    the locale
     * @return the given item stack
     * @see GlobalTranslator
     */
    @Contract("_, _ -> param1")
    public static ItemStack renderTranslations(final ItemStack itemStack, final Locale locale) {
        return renderTranslations(itemStack, locale, GlobalTranslator.renderer());
    }

    /**
     * Render all translatable components for the given item stack with the default locale using a renderer backed by
     * the global translator.
     *
     * <p>A copy of the item stack is NOT made. It is modified directly.</p>
     *
     * @param itemStack the item stack
     * @return the given item stack
     * @see GlobalTranslator
     */
    @Contract("_ -> param1")
    public static ItemStack renderTranslations(final ItemStack itemStack) {
        return renderTranslations(itemStack, Locale.getDefault());
    }

    /**
     * Convert the given item stack into a compound binary tag.
     *
     * @param itemStack the item stack
     * @return a compound binary tag
     * @throws IOException when an error occurs reading the compound binary tag
     */
    @Contract(pure = true)
    public static CompoundBinaryTag asCompoundBinaryTag(final ItemStack itemStack) throws IOException {
        Objects.requireNonNull(itemStack, "itemStack");

        try (final ByteArrayInputStream bais = new ByteArrayInputStream(itemStack.serializeAsBytes())) {
            return BinaryTagIO.reader().read(bais, BinaryTagIO.Compression.GZIP);
        }
    }

    /**
     * Convert the given compound binary tag into an item stack.
     *
     * @param compoundBinaryTag the compound binary tag
     * @return a compound binary tag
     * @throws IOException when an error occurs writing the compound binary tag
     */
    @Contract(pure = true)
    public static ItemStack fromCompoundBinaryTag(final CompoundBinaryTag compoundBinaryTag) throws IOException {
        Objects.requireNonNull(compoundBinaryTag, "compoundBinaryTag");

        try (final ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            BinaryTagIO.writer().write(compoundBinaryTag, baos, BinaryTagIO.Compression.GZIP);
            return ItemStack.deserializeBytes(baos.toByteArray());
        }
    }
}

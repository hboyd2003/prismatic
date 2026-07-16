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

import dev.hboyd.prismatic.paper.persistent_data.PersistentDataTypeRegistry;
import net.kyori.adventure.key.Key;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.jspecify.annotations.NullMarked;

/**
 * Persistent data type for {@link PotionEffect}s.
 *
 * @see org.bukkit.persistence.PersistentDataType
 * @see PersistentDataContainer
 */
public final class PotionEffectPersistentDataType implements PersistentDataType<PersistentDataContainer, PotionEffect> {
    public static final PotionEffectPersistentDataType INSTANCE = new PotionEffectPersistentDataType();

    private static final NamespacedKey HIDDEN_EFFECT = new NamespacedKey(NamespacedKey.MINECRAFT, "hidden_effect");
    private static final NamespacedKey AMPLIFIER = new NamespacedKey(NamespacedKey.MINECRAFT, "amplifier");
    private static final NamespacedKey DURATION = new NamespacedKey(NamespacedKey.MINECRAFT, "duration");
    private static final NamespacedKey TYPE = new NamespacedKey(NamespacedKey.MINECRAFT, "effect");
    private static final NamespacedKey AMBIENT = new NamespacedKey(NamespacedKey.MINECRAFT, "ambient");
    private static final NamespacedKey PARTICLES = new NamespacedKey(NamespacedKey.MINECRAFT, "particles");
    private static final NamespacedKey ICON = new NamespacedKey(NamespacedKey.MINECRAFT, "icon");

    private PotionEffectPersistentDataType() {
    }

    @Override
    public Class<PersistentDataContainer> getPrimitiveType() {
        return PersistentDataContainer.class;
    }

    @Override
    public Class<PotionEffect> getComplexType() {
        return PotionEffect.class;
    }

    @Override
    @NullMarked
    public PersistentDataContainer toPrimitive(final PotionEffect potionEffect, final PersistentDataAdapterContext context) {
        final PersistentDataContainer potionEffectPDC = context.newPersistentDataContainer();

        potionEffectPDC.set(TYPE, PersistentDataTypeRegistry.get(Key.class), potionEffect.getType().key());
        potionEffectPDC.set(DURATION, PersistentDataType.INTEGER, potionEffect.getDuration());
        potionEffectPDC.set(AMPLIFIER, PersistentDataType.INTEGER, potionEffect.getAmplifier());
        potionEffectPDC.set(AMBIENT, PersistentDataType.BOOLEAN, potionEffect.isAmbient());
        potionEffectPDC.set(PARTICLES, PersistentDataType.BOOLEAN, potionEffect.hasParticles());
        potionEffectPDC.set(ICON, PersistentDataType.BOOLEAN, potionEffect.hasIcon());
        if (potionEffect.getHiddenPotionEffect() != null)
            potionEffectPDC.set(HIDDEN_EFFECT, this, potionEffect.getHiddenPotionEffect());

        return potionEffectPDC;
    }

    @Override
    @NullMarked
    public PotionEffect fromPrimitive(final PersistentDataContainer potionEffectPDC, final PersistentDataAdapterContext context) {
        PotionEffect hiddenPotionEffect = null;
        if (potionEffectPDC.has(HIDDEN_EFFECT, this)) {
            hiddenPotionEffect = potionEffectPDC.get(HIDDEN_EFFECT, this);
        }

        //noinspection UnstableApiUsage
        return new PotionEffect(
                Registry.MOB_EFFECT.get(potionEffectPDC.get(TYPE, PersistentDataTypeRegistry.get(Key.class))),
                potionEffectPDC.get(DURATION, PersistentDataType.INTEGER),
                potionEffectPDC.get(AMPLIFIER, PersistentDataType.INTEGER),
                potionEffectPDC.get(AMBIENT, PersistentDataType.BOOLEAN),
                potionEffectPDC.get(PARTICLES, PersistentDataType.BOOLEAN),
                potionEffectPDC.get(ICON, PersistentDataType.BOOLEAN),
                hiddenPotionEffect);
    }
}

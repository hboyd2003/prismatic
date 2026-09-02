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

import net.kyori.adventure.Adventure;
import net.kyori.adventure.text.object.PlayerHeadObjectContents;
import net.kyori.adventure.text.serializer.commons.ComponentTreeConstants;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

/**
 * Persistent data type for {@link net.kyori.adventure.text.object.PlayerHeadObjectContents.ProfileProperty}s.
 *
 * @see org.bukkit.persistence.PersistentDataType
 * @see PersistentDataContainer
 */
public final class ProfilePropertyPersistentDataType implements PersistentDataType<PersistentDataContainer, PlayerHeadObjectContents.ProfileProperty> {
    public static final ProfilePropertyPersistentDataType INSTANCE = new ProfilePropertyPersistentDataType();

    private static final NamespacedKey PROFILE_PROPERTY_NAME = new NamespacedKey(Adventure.NAMESPACE, ComponentTreeConstants.PROFILE_PROPERTY_NAME);
    private static final NamespacedKey PROFILE_PROPERTY_VALUE = new NamespacedKey(Adventure.NAMESPACE, ComponentTreeConstants.PROFILE_PROPERTY_VALUE);
    private static final NamespacedKey PROFILE_PROPERTY_SIGNATURE = new NamespacedKey(Adventure.NAMESPACE, ComponentTreeConstants.PROFILE_PROPERTY_SIGNATURE);

    private ProfilePropertyPersistentDataType() {}

    @Override
    public Class<PersistentDataContainer> getPrimitiveType() {
        return PersistentDataContainer.class;
    }

    @Override
    public Class<PlayerHeadObjectContents.ProfileProperty> getComplexType() {
        return PlayerHeadObjectContents.ProfileProperty.class;
    }

    @Override
    public PersistentDataContainer toPrimitive(final PlayerHeadObjectContents.ProfileProperty profileProperty, final PersistentDataAdapterContext context) {
        final PersistentDataContainer profilePropertyPDC = context.newPersistentDataContainer();

        profilePropertyPDC.set(PROFILE_PROPERTY_NAME, PersistentDataType.STRING, profileProperty.name());
        profilePropertyPDC.set(PROFILE_PROPERTY_VALUE, PersistentDataType.STRING, profileProperty.value());
        if (profileProperty.signature() != null) {
            profilePropertyPDC.set(PROFILE_PROPERTY_SIGNATURE, PersistentDataType.STRING, profileProperty.signature());
        }

        return profilePropertyPDC;
    }

    @Override
    public PlayerHeadObjectContents.ProfileProperty fromPrimitive(final PersistentDataContainer profilePropertyPDC, final PersistentDataAdapterContext context) {
        String signature = null;
        if (profilePropertyPDC.has(PROFILE_PROPERTY_SIGNATURE))
            signature = profilePropertyPDC.get(PROFILE_PROPERTY_SIGNATURE, PersistentDataType.STRING);

        return PlayerHeadObjectContents.property(
                profilePropertyPDC.get(PROFILE_PROPERTY_NAME, PersistentDataType.STRING),
                profilePropertyPDC.get(PROFILE_PROPERTY_VALUE, PersistentDataType.STRING),
                signature);
    }
}

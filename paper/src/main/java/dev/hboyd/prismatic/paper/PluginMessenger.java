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

import io.papermc.paper.connection.PlayerConnection;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Namespaced;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.plugin.messaging.PluginMessageListenerRegistration;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Unmodifiable;

import java.io.Closeable;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A wrapper around a {@link Messenger} which is tied to a specific {@link Plugin}.
 */
public class PluginMessenger implements Closeable, Namespaced {
    private final Plugin plugin;
    private final Messenger messenger;

    /**
     * Construct a plugin messenger with the given plugin and messenger.
     *
     * @param plugin    a plugin
     * @param messenger a messenger
     */
    public PluginMessenger(final Plugin plugin, final Messenger messenger) {
        this.plugin = Objects.requireNonNull(plugin);
        this.messenger = Objects.requireNonNull(messenger);
    }

    /**
     * Get the namespace of the plugin this messenger is tied to.
     *
     * @return the namespace
     */
    @Override
    public String namespace() {
        return this.plugin.namespace();
    }

    /**
     * Check if the specified channel is reserved and cannot be listened to.
     *
     * @param channelKey a plugin channel key
     * @return weather the channel is reserved or not
     */
    public boolean isReservedChannel(final Key channelKey) {
        return this.messenger.isReservedChannel(channelKey.asMinimalString());
    }

    /**
     * Unregister all registered incoming plugin channels for this messenger.
     */
    public void unregisterIncomingPluginChannels() {
        this.messenger.unregisterIncomingPluginChannel(this.plugin);
    }

    /**
     * Check weather the specified plugin message listener registration is valid.
     *
     * <p>A registration is considered valid if it has not been unregistered and that the plugin is still enabled.</p>
     *
     * @param registration a registration
     * @return weather the registration is valid or not
     */
    public boolean isRegistrationValid(final PluginMessageListenerRegistration registration) {
        return this.messenger.isRegistrationValid(registration);
    }

    /**
     * Unregister the given outgoing plugin channel for this messenger's plugin.
     *
     * @param channelKey a plugin channel key
     */
    public void unregisterOutgoingPluginChannel(final Key channelKey) {
        this.messenger.unregisterOutgoingPluginChannel(this.plugin, channelKey.asMinimalString());
    }

    /**
     * Unregister the given listener from the specified incoming plugin channel for this messenger's plugin.
     *
     * @param channelKey a plugin channel key
     * @param listener   a listener
     */
    public void unregisterIncomingPluginChannel(final Key channelKey,
                                                final PluginMessageListener listener) {
        this.messenger.unregisterIncomingPluginChannel(this.plugin, channelKey.asMinimalString(), listener);
    }

    /**
     * Get all incoming plugin channel registrations for this messenger's plugin .
     *
     * @return the channel registrations
     */
    public @Unmodifiable Set<PluginMessageListenerRegistration> incomingChannelRegistrations() {
        return this.messenger.getIncomingChannelRegistrations(this.plugin);
    }

    /**
     * Check if this messenger's plugin is registered to receive incoming messages through the requested channel.
     *
     * @param channelKey a plugin channel key
     * @return weather the channel is registered or not
     */
    public boolean isIncomingChannelRegistered(final Key channelKey) {
        return this.messenger.isIncomingChannelRegistered(this.plugin, channelKey.asMinimalString());
    }

    /**
     * Dispatch the specified message to any registered listeners.
     *
     * @param source     a source
     * @param channelKey a plugin channel key
     * @param message    a message
     */
    @ApiStatus.Experimental
    public void dispatchIncomingMessage(final PlayerConnection source,
                                        final Key channelKey,
                                        final byte[] message) {
        this.messenger.dispatchIncomingMessage(source, channelKey.asMinimalString(), message);
    }

    /**
     * Get all registered outgoing plugin channels for this messenger's plugin .
     *
     * @return outgoing plugin channels
     */
    public @Unmodifiable Set<Key> outgoingChannels() {
        return this.messenger.getOutgoingChannels(this.plugin).stream()
                .map(Key::key)
                .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Unregister all outgoing plugin channel registrations for this messenger's plugin.
     */
    public void unregisterOutgoingPluginChannels() {
        this.messenger.unregisterOutgoingPluginChannel(this.plugin);
    }

    /**
     * Unregister the given plugin channel for this messenger's plugin.
     *
     * @param channelKey a plugin channel key
     */
    public void unregisterIncomingPluginChannel(final Key channelKey) {
        this.messenger.unregisterIncomingPluginChannel(this.plugin, channelKey.asMinimalString());
    }

    /**
     * Get the incoming plugin channel registrations for this messenger's plugin.
     *
     * @param channelKey Channel to filter registrations by.
     * @return incoming channel registrations
     */
    public @Unmodifiable Set<PluginMessageListenerRegistration> incomingChannelRegistrations(final Key channelKey) {
        return this.messenger.getIncomingChannelRegistrations(this.plugin, channelKey.asMinimalString());
    }

    /**
     * Get weather the given outgoing plugin channel is registered for this messenger's plugin.
     *
     * @param channelKey a plugin channel key
     * @return weather the channel is registered
     */
    public boolean isOutgoingChannelRegistered(final Key channelKey) {
        return this.messenger.isOutgoingChannelRegistered(this.plugin, channelKey.asMinimalString());
    }

    /**
     * Register the given plugin message listener with the specified incoming plugin channel for this messenger's
     * plugin.
     *
     * @param channelKey a plugin channel key
     * @param listener   a plugin message listener
     * @return the resultant message listener registration
     * @throws IllegalArgumentException when the listener is already registered for this channel, messenger and plugin
     */
    public PluginMessageListenerRegistration registerIncomingPluginChannel(final Key channelKey,
                                                                           final PluginMessageListener listener) {
        return this.messenger.registerIncomingPluginChannel(this.plugin, channelKey.asMinimalString(), listener);
    }

    /**
     * Register the requested outgoing plugin channel for this messenger's plugin.
     *
     * @param channelKey a plugin channel key
     */
    public void registerOutgoingPluginChannel(final Key channelKey) {
        this.messenger.registerOutgoingPluginChannel(this.plugin, channelKey.asMinimalString());
    }

    /**
     * Get all incoming plugin channels for this messenger's plugin.
     *
     * @return the incoming channels
     */
    public @Unmodifiable Set<Key> incomingChannels() {
        return this.messenger.getIncomingChannels(this.plugin)
                .stream()
                .map(Key::key)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public void close() {
        this.unregisterIncomingPluginChannels();
        this.unregisterOutgoingPluginChannels();
    }
}

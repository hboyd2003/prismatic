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

package dev.hboyd.prismatic.configurate.serializer;

import com.google.common.net.InetAddresses;
import org.spongepowered.configurate.serialize.ScalarSerializer;
import org.spongepowered.configurate.serialize.SerializationException;

import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.function.Predicate;

/**
 * Configurate serializer for {@link InetSocketAddress}s.
 */
public class InetSocketAddressSerializer extends ScalarSerializer<InetSocketAddress> {
    public static final InetSocketAddressSerializer INSTANCE = new InetSocketAddressSerializer(65533);

    private final int defaultPort;

    /**
     * Construct an inet socket address serializer with a default port.
     *
     * @param defaultPort the default port
     */
    public InetSocketAddressSerializer(final int defaultPort) {
        super(InetSocketAddress.class);
        this.defaultPort = defaultPort;
    }

    @Override
    public InetSocketAddress deserialize(final Type type, final Object obj) throws SerializationException {
        final String input = obj.toString();

        final int lastColonIndex = input.lastIndexOf(':');
        final String addressString;
        final int port;
        if (lastColonIndex == -1 || input.endsWith("]")) {
            port = this.defaultPort;
            addressString = input;
        } else if (lastColonIndex == input.length() - 1) {
            port = this.defaultPort;
            addressString = input.substring(0, lastColonIndex);
        } else {
            try {
                port = Integer.parseInt(input.substring(lastColonIndex + 1));
            } catch (final NumberFormatException e) {
                throw new SerializationException(type, "invalid port", e);
            }
            addressString = input.substring(0, lastColonIndex);
        }

        InetAddress address;
        try {
            address = InetAddresses.forString(addressString);
        } catch (final IllegalArgumentException _) {
            try {
                address = InetAddress.getByName(addressString);
            } catch (final UnknownHostException e) {
                throw new SerializationException(type, "invalid host or ip", e);
            }
        }

        return new InetSocketAddress(address, port);
    }

    @Override
    protected Object serialize(final InetSocketAddress inetSocketAddress, final Predicate<Class<?>> typeSupported) {
        return inetSocketAddress.getHostString() + ":" + inetSocketAddress.getPort();
    }
}


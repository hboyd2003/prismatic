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

package dev.hboyd.prismatic.key;

import org.jetbrains.annotations.Nullable;

import java.io.Serial;
import java.util.Arrays;

/**
 * Represents an invalid attempt to create a {@link PathedKey}.
 */
public final class InvalidPathedKeyException extends IllegalArgumentException {
    @Serial
    private static final long serialVersionUID = -4395526911727161014L;

    private final String keyNamespace;
    private final String[] elements;
    private final char elementDelimiter;

    InvalidPathedKeyException(final String keyNamespace,
                              final String[] elements,
                              final char elementDelimiter,
                              final @Nullable String message) {
        super(message);
        this.keyNamespace = keyNamespace;
        this.elements = elements;
        this.elementDelimiter = elementDelimiter;
    }

    /**
     * Gets the invalid namespace.
     *
     * @return the namespace
     */
    public String keyNamespace() {
        return this.keyNamespace;
    }

    /**
     * Get the elements.
     *
     * @return the elements
     */
    public String[] keyElements() {
        return Arrays.copyOf(this.elements, this.elements.length);
    }

    /**
     * Get element delimiter.
     *
     * @return the element delimiter
     */
    public char keyElementDelimiter() {
        return this.elementDelimiter;
    }
}

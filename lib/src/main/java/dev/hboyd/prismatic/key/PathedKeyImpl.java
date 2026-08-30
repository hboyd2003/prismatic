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

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern;
import org.intellij.lang.annotations.RegExp;
import org.jetbrains.annotations.UnmodifiableView;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.regex.Pattern;

final class PathedKeyImpl implements PathedKey {
    private static final @RegExp String NAMESPACE_PATTERN = "[a-z0-9_\\-.]+";
    private static final @RegExp String VALUE_PATTERN = "[a-z0-9_\\-./]+";

    private final String namespace;
    private final String[] elements;
    private final char elementDelimiter;

    PathedKeyImpl(@KeyPattern.Namespace final String namespace,
                  final char elementDelimiter,
                  final String[] elements) {
        this(namespace, elementDelimiter, elements, true);
    }

    private PathedKeyImpl(final String namespace,
                          final char elementDelimiter,
                          final String[] elements,
                          final boolean validateAndSplit) {
        final String[] splitElements;
        if (validateAndSplit) {
            Objects.requireNonNull(namespace, "namespace");
            if (elements.length == 0) throw new IllegalArgumentException("Pathed key must have at least one element");

            final OptionalInt invalidNamespaceCharIndex = Key.checkNamespace(namespace);
            if (invalidNamespaceCharIndex.isPresent()) {
                throw buildInvalidPathedKeyException("namespace",
                        namespace,
                        elements,
                        elementDelimiter,
                        invalidNamespaceCharIndex.getAsInt(),
                        namespace.charAt(invalidNamespaceCharIndex.getAsInt()),
                        NAMESPACE_PATTERN);
            }

            if (!Key.allowedInValue(elementDelimiter))
                throw new InvalidPathedKeyException(namespace,
                        elements,
                        elementDelimiter,
                        "Non %s character '%s' as delimiter of PathedKey[%s] ".formatted(VALUE_PATTERN,
                                elementDelimiter,
                                asString(namespace, elements, elementDelimiter)));

            splitElements = validateAndSplit(namespace, 0, elements, elementDelimiter).toArray(String[]::new);
        } else {
            splitElements = elements;
        }

        this.namespace = namespace;
        this.elements = splitElements;
        this.elementDelimiter = elementDelimiter;
    }

    private static List<String> validateAndSplit(final String namespace,
                                                 final int existingLength,
                                                 final String[] elementsToSplit,
                                                 final char elementDelimiter) {
        final List<String> splitElements = new ArrayList<>();
        int currentLength = existingLength;
        for (int j = 0; j < elementsToSplit.length; j++) {
            for (final String splitElement : splitWithDelimiter(elementsToSplit[j], elementDelimiter)) {
                if (splitElement.isEmpty()) throw new InvalidPathedKeyException(namespace,
                        elementsToSplit,
                        elementDelimiter,
                        "Blank element of PathedKey[%s] at index %d".formatted(
                                asString(namespace, elementsToSplit, elementDelimiter),
                                currentLength
                        ));

                splitElements.add(splitElement);
                for (int i = 0; i < splitElement.length(); i++) {
                    final char character = splitElement.charAt(i);
                    if (!Key.allowedInValue(character)) {
                        throw buildInvalidPathedKeyException("value",
                                namespace,
                                elementsToSplit,
                                elementDelimiter,
                                currentLength + i,
                                character,
                                VALUE_PATTERN);
                    }
                }
                currentLength += splitElement.length();
            }
            if (j < elementsToSplit.length - 1) currentLength++;
        }

        return splitElements;
    }

    public static int compare(final Key key1, final Key key2) {
        Objects.requireNonNull(key1, "key1");
        Objects.requireNonNull(key2, "key2");

        if (key1 instanceof final PathedKey pathedKey1 && key2 instanceof final PathedKey pathedKey2) {
            final Iterator<String> pathedKey1ValueElementIterator = pathedKey1.elementIterator();
            final Iterator<String> pathedKey2ValueElementIterator = pathedKey2.elementIterator();

            int pathedKey1Depth = 0;
            int pathedKey2Depth = 0;
            while (pathedKey1ValueElementIterator.hasNext() || pathedKey2ValueElementIterator.hasNext()) {
                String pathedKey1Element = null;
                if (pathedKey1ValueElementIterator.hasNext()) {
                    pathedKey1Element = pathedKey1ValueElementIterator.next();
                    pathedKey1Depth++;
                }

                String pathedKey2Element = null;
                if (pathedKey2ValueElementIterator.hasNext()) {
                    pathedKey2Element = pathedKey2ValueElementIterator.next();
                    pathedKey2Depth++;
                }

                if (pathedKey1Element != null && pathedKey2Element != null) {
                    final int currentValueComparison = pathedKey1Element.compareTo(pathedKey2Element);
                    if (currentValueComparison != 0) return currentValueComparison;
                }
            }

            final int depthDifference = pathedKey1Depth - pathedKey2Depth;
            if (depthDifference == 0)
                return pathedKey1.namespace().compareTo(pathedKey2.namespace());
            return depthDifference;
        }

        return Key.comparator().compare(key1, key2);
    }

    private static InvalidPathedKeyException buildInvalidPathedKeyException(final String partName,
                                                                            final String namespace,
                                                                            final String[] elements,
                                                                            final char elementDelimiter,
                                                                            final int index,
                                                                            final char invalidCharacter,
                                                                            final String pattern) {
        return new InvalidPathedKeyException(namespace,
                elements,
                elementDelimiter,
                "Non %s character in %s of PathedKey[%s] at index %d ('%s', bytes: %s)".formatted(
                        pattern,
                        partName,
                        asString(namespace, elements, elementDelimiter),
                        index,
                        invalidCharacter,
                        Arrays.toString(String.valueOf(invalidCharacter).getBytes(StandardCharsets.UTF_8))
                ));
    }

    private static String asString(final String namespace, final String[] elements, final char elementDelimiter) {
        return namespace + DEFAULT_SEPARATOR + String.join(String.valueOf(elementDelimiter), elements);
    }

    private static String[] splitWithDelimiter(final String string, final char elementDelimiter) {
        return string.split(Pattern.quote(String.valueOf(elementDelimiter)),
                -1); // -1 == do not discard trailing empty strings
    }

    @Override
    public PathedKey then(final char elementDelimiter, final String... elements) throws InvalidPathedKeyException {
        if (elements.length == 0) return this;

        final List<String> newElements = new ArrayList<>(Arrays.stream(this.elements).toList());
        newElements.addAll(validateAndSplit(this.namespace, this.elements.length, elements, elementDelimiter));
        return new PathedKeyImpl(this.namespace,
                this.elementDelimiter,
                newElements.toArray(String[]::new),
                false);
    }

    @Override
    public PathedKeyImpl parent(final int n) throws IndexOutOfBoundsException {
        if (n == 0) return this;
        if (n < 0 || n >= this.elements.length) throw new IndexOutOfBoundsException();
        return new PathedKeyImpl(this.namespace,
                this.elementDelimiter,
                Arrays.copyOfRange(this.elements, 0, this.elements.length - n),
                false);
    }

    @Override
    public int size() {
        return this.elements.length;
    }

    @Override
    public String[] elements() {
        return Arrays.copyOf(this.elements, this.elements.length);
    }

    @Override
    public @UnmodifiableView List<String> elementList() {
        // While List.of() would work, this uses the array as the backing directly avoiding the need for a copy.
        //noinspection Java9CollectionFactory
        return Collections.unmodifiableList(Arrays.asList(this.elements));
    }

    @Override
    public @KeyPattern.Value String element(final int index) throws IndexOutOfBoundsException {
        //noinspection PatternValidation
        return this.elements[index];
    }

    @Override
    public @KeyPattern.Value String value() {
        //noinspection PatternValidation
        return String.join(String.valueOf(this.elementDelimiter), this.elements);
    }

    @Override
    public @KeyPattern.Value String value(final int index) throws IndexOutOfBoundsException {
        if (index < 0 || index >= this.elements.length) throw new IndexOutOfBoundsException();

        final StringBuilder valueBuilder = new StringBuilder();
        valueBuilder.append(this.elements[0]);
        for (int i = 1; i <= index; i++) {
            valueBuilder.append(this.elementDelimiter).append(this.elements[i]);
        }

        //noinspection PatternValidation
        return valueBuilder.toString();
    }

    @Override
    public @KeyPattern.Namespace String namespace() {
        return this.namespace;
    }

    @Override
    public PathedKey key(final int index) throws IndexOutOfBoundsException {
        if (index < 0 || index >= this.elements.length) throw new IndexOutOfBoundsException();
        return new PathedKeyImpl(this.namespace,
                this.elementDelimiter,
                Arrays.copyOfRange(this.elements, 0, index + 1));
    }

    @Override
    public char elementDelimiter() {
        return this.elementDelimiter;
    }

    @Override
    public PathedKey elementDelimiter(final char elementDelimiter) {
        return new PathedKeyImpl(this.namespace, elementDelimiter, this.elements);
    }

    @Override
    public boolean startsWith(final char elementDelimiter, final String... elements) {
        if (elements.length == 0) throw new IllegalArgumentException("Cannot check if a key starts with no elements");

        final Iterator<String> elementIterator = this.elementIterator();
        for (final String element : elements) {
            for (final String splitElement : splitWithDelimiter(element, elementDelimiter))
                if (!elementIterator.hasNext() || !elementIterator.next().equals(splitElement)) return false;
        }

        return true;
    }

    @Override
    public boolean endsWith(final char elementDelimiter, final String... elements) {
        if (elements.length == 0) throw new IllegalArgumentException("Cannot check if a key ends with no elements");
        if (elements.length > this.elements.length) return false;

        int j = elements.length - 1;
        int i = this.elements.length - 1;
        while (i >= 0 && j >= 0) {
            final String[] splitElements = splitWithDelimiter(elements[j], elementDelimiter);
            for (int k = splitElements.length - 1; k >= 0; k--) {
                if (i < 0 || !this.elements[i].equals(splitElements[k]))
                    return false;
                if (k > 0) i--;
            }

            i--;
            j--;
        }

        return true;
    }

    @Override
    public Iterator<PathedKey> iterator() {
        return new Iterator<>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return this.index < PathedKeyImpl.this.elements.length;
            }

            @Override
            public PathedKey next() {
                if (!this.hasNext()) throw new NoSuchElementException();
                return new PathedKeyImpl(PathedKeyImpl.this.namespace,
                        PathedKeyImpl.this.elementDelimiter,
                        Arrays.copyOfRange(PathedKeyImpl.this.elements, 0, ++this.index),
                        false);
            }
        };
    }

    @Override
    public Iterator<String> valueIterator() {
        return new Iterator<>() {
            private String currentValue = "";
            private int index = 0;

            @Override
            public boolean hasNext() {
                return this.index < PathedKeyImpl.this.elements.length;
            }

            @Override
            public String next() {
                if (!this.hasNext()) throw new NoSuchElementException();

                if (!this.currentValue.isEmpty()) this.currentValue += PathedKeyImpl.this.elementDelimiter();

                this.currentValue += PathedKeyImpl.this.elements[this.index];
                this.index++;
                return this.currentValue;
            }
        };
    }

    @Override
    public Iterator<String> elementIterator() {
        return new Iterator<>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return this.index < PathedKeyImpl.this.elements.length;
            }

            @Override
            public String next() {
                if (!this.hasNext()) throw new NoSuchElementException();
                return PathedKeyImpl.this.elements[this.index++];
            }
        };
    }

    @Override
    public String asString() {
        return asString(this.namespace, this.elements, this.elementDelimiter);
    }

    @Override
    public int hashCode() {
        // We hash with value to preserve compatibility with the default key implementation
        return 31 * this.namespace.hashCode() + this.value().hashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) return true;
        if (!(other instanceof final Key otherKey)) return false;

        final boolean baseEqual = Objects.equals(this.namespace, otherKey.namespace())
                && Objects.equals(this.value(), otherKey.value());

        if (baseEqual && otherKey instanceof final PathedKey otherPathedKey)
            // We still compare by element delimiter to prevent a pathed key equaling another pathed key where one has a single element that matches the value of the other key
            return Objects.equals(this.elementDelimiter, otherPathedKey.elementDelimiter());

        return baseEqual;
    }

    @Override
    public String toString() {
        return this.asString();
    }

}

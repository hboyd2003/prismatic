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
import net.kyori.adventure.key.Namespaced;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * An identifying object used to fetch and/or store unique objects.
 *
 * <p>A key consists of:</p>
 * <dl>
 *   <dt>namespace</dt>
 *   <dd>the 'space' the key exists in. Typically an organization, project or group associated with the key</dd>
 *   <dt>value</dt>
 *   <dd>what the key leads to, e.g "translations" or "entity.firework_rocket.blast"</dd>
 * </dl>
 *
 * <p>Values are made up of elements separated by a character.</p>
 *
 * <p>Valid characters for namespaces are <a href="https://regexr.com/5ibbm">{@code [a-z0-9_.-]}</a>.</p>
 *
 * <p>Valid characters for values are <a href="https://regexr.com/5if3m">{@code [a-z0-9/._-]}</a>.</p>
 *
 * <p>Some examples of possible custom keys:</p>
 * <ul>
 *   <li> com.example:weapon.amazing-weapon_damage-attribute</li>
 *   <li> my_organization:music.song_1</li>
 *   <li> my_organization:item.magic_button</li>
 * </ul>
 */
public sealed interface PathedKey extends Key, Iterable<PathedKey> permits PathedKeyImpl {
    /**
     * Default delimiter used to separate elements in the value.
     */
    char DEFAULT_ELEMENT_DELIMITER = '.';

    /**
     * Create a pathed key with the given namespace, elements and element delimiter.
     *
     * <p>The given elements will be split using the given element delimiter.</p>
     *
     * @param namespace        the namespace
     * @param elementDelimiter the delimiter
     * @param elements         the elements
     * @return the pathed key
     * @throws InvalidPathedKeyException when the namespace, an element or the element delimiter is blank or contains an
     *                                   invalid character
     */
    @Contract(value = "_, _, _ -> new", pure = true)
    static PathedKey of(@KeyPattern.Namespace final String namespace,
                        final char elementDelimiter,
                        final String... elements) throws InvalidPathedKeyException {
        return new PathedKeyImpl(namespace, elementDelimiter, elements);
    }

    /**
     * Create a pathed key with the given namespace, elements and default element delimiter.
     *
     * <p>The given elements will be split using this key's element delimiter.</p>
     *
     * @param namespace the namespace
     * @param elements  the elements
     * @return the pathed key
     * @throws InvalidPathedKeyException when the namespace, an element or the element delimiter is blank or contains an
     *                                   invalid character
     */
    @Contract(value = "_, _ -> new", pure = true)
    static PathedKey of(@KeyPattern.Namespace final String namespace,
                        final String... elements) throws InvalidPathedKeyException {
        return of(namespace, DEFAULT_ELEMENT_DELIMITER, elements);
    }

    /**
     * Create a pathed key.
     *
     * <p>This will parse {@code string} as a key, using the given character as a separator between the namespace and
     * the value. The value will be split into elements using the given element delimiter. Delimiters and {@code -} can
     * be escaped with a {@code -}.</p>
     *
     * <p>The namespace is optional. If you do not provide one (for example, if you provide just {@code player} or
     * {@code :player} as the string) then {@link #MINECRAFT_NAMESPACE} will be used as a namespace and {@code string}
     * will be used as the value, removing the colon if necessary.</p>
     *
     * @param keyString          a string
     * @param namespaceSeparator a character
     * @param elementDelimiter   a character
     * @return the pathed key
     * @throws InvalidPathedKeyException when the namespace or value is blank or contain an invalid character
     * @throws IllegalArgumentException  when the element delimiter is an invalid character
     */
    @Contract(value = "_, _, _ -> new", pure = true)
    static PathedKey of(@KeyPattern final String keyString,
                        final char namespaceSeparator,
                        final char elementDelimiter) throws InvalidPathedKeyException, IllegalArgumentException {
        Objects.requireNonNull(keyString, "string");
        //noinspection PatternValidation
        final int separatorIndex = keyString.indexOf(namespaceSeparator);
        //noinspection PatternValidation
        return of(separatorIndex >= 1 ? keyString.substring(0, separatorIndex) : MINECRAFT_NAMESPACE,
                elementDelimiter,
                separatorIndex >= 0 ? keyString.substring(separatorIndex + 1) : keyString);
    }

    /**
     * Create a pathed key.
     *
     * <p>This will parse {@code string} as a key, using {@code :} as a separator between the namespace and the
     * value. The value will be split into elements using the given element delimiter. Delimiters and {@code -} can be
     * escaped with a {@code -}.</p>
     *
     * <p>The namespace is optional. If you do not provide one (for example, if you provide just {@code player} or
     * {@code :player} as the string) then {@link #MINECRAFT_NAMESPACE} will be used as a namespace and {@code string}
     * will be used as the value, removing the colon if necessary.</p>
     *
     * @param keyString a string
     * @return the pathed key
     * @throws InvalidPathedKeyException when the namespace or value contains an invalid character
     */
    @Contract(value = "_ -> new", pure = true)
    static PathedKey of(final String keyString) throws InvalidPathedKeyException {
        //noinspection PatternValidation
        return of(keyString, DEFAULT_SEPARATOR, DEFAULT_ELEMENT_DELIMITER);
    }

    /**
     * Create a pathed key with the given namespace and elements.
     *
     * @param namespace the namespace
     * @param value     the value
     * @return the pathed key
     * @throws InvalidPathedKeyException when the namespace or value is blank or contains an invalid character
     */
    @Contract(value = "_, _ -> new", pure = true)
    static PathedKey of(@KeyPattern.Namespace final String namespace,
                        @KeyPattern.Value final String value) throws InvalidPathedKeyException {
        return of(namespace, DEFAULT_ELEMENT_DELIMITER, value);
    }

    /**
     * Create a pathed key using the given key and element delimiter.
     *
     * @param key              a key
     * @param elementDelimiter a character
     * @return the pathed key
     * @throws InvalidPathedKeyException when the namespace or value is blank or contains an invalid character
     */
    @Contract(value = "_, _ -> new", pure = true)
    static PathedKey of(final Key key, final char elementDelimiter) throws IllegalArgumentException {
        //noinspection PatternValidation
        return of(key.namespace(), elementDelimiter, key.value());
    }

    /**
     * Create a pathed key using the given key.
     *
     * @param key a key
     * @return the pathed key
     */
    @Contract(value = "_ -> new", pure = true)
    static PathedKey of(final Key key) {
        return of(key, DEFAULT_ELEMENT_DELIMITER);
    }

    /**
     * Create a pathed key with the namespace of the given namespaces and value.
     *
     * @param namespaced the namespaced
     * @param value      the value
     * @return the pathed key
     * @throws InvalidPathedKeyException when the namespace or value is blank or contain an invalid character
     */
    @Contract(value = "_, _ -> new", pure = true)
    static PathedKey of(final Namespaced namespaced,
                        @KeyPattern.Value final String value) throws InvalidPathedKeyException {
        //noinspection PatternValidation
        return of(Objects.requireNonNull(namespaced, "namespaced").namespace(), value);
    }

    /**
     * Get the comparator.
     *
     * @return a comparator for keys
     */
    @Contract(pure = true)
    static Comparator<? super Key> comparator() {
        return PathedKeyImpl::compare;
    }

    /**
     * Create a key with the same namespace, element delimiter and with the given elements added as the next element in
     * the path. If no elements are given, this is returned.
     *
     * <p>The given elements will be split using both this key's and the given element delimiter.</p>
     *
     * @param elementDelimiter a character
     * @param elements         the elements
     * @return a pathed key
     * @throws InvalidPathedKeyException when an element contains an invalid character
     */
    @Contract(value = "_, _ -> new", pure = true)
    PathedKey then(final char elementDelimiter, final String... elements) throws InvalidPathedKeyException;

    /**
     * Create a key with the same namespace and value appended with the given value's elements determined based on this
     * key's element delimiter appended to the current value.
     *
     * <p>The given elements will be split using this key's element delimiter.</p>
     *
     * @param elements the elements
     * @return a pathed key
     * @throws InvalidPathedKeyException when an element contains an invalid character
     */
    @Contract(value = "_ -> new", pure = true)
    default PathedKey then(final String... elements) throws InvalidPathedKeyException {
        return this.then(this.elementDelimiter(), elements);
    }

    /**
     * Create a key with the same namespace and value but with the last {@code n} path elements removed.
     *
     * @param n an integer
     * @return a pathed key
     * @throws IndexOutOfBoundsException when no parent N exists
     */
    @Contract(value = "_ -> new", pure = true)
    PathedKey parent(final int n) throws IndexOutOfBoundsException;

    /**
     * Create a key with the same namespace and value but with the last path element removed.
     *
     * @return a pathed key
     * @throws NoSuchElementException when no parent exists
     */
    @Contract(value = " -> new", pure = true)
    default PathedKey parent() throws NoSuchElementException {
        try {
            return this.parent(1);
        } catch (final IndexOutOfBoundsException e) {
            throw new NoSuchElementException(e);
        }
    }

    /**
     * Get the depth of the value.
     *
     * @return the depth
     */
    @Contract(pure = true)
    default int depth() {
        return this.size() - 1;
    }

    /**
     * Get the number of elements present in the value.
     *
     * @return the size
     */
    int size();

    /**
     * Get a copy of the elements that make up the value.
     *
     * @return the elements
     */
    @Contract(pure = true)
    String[] elements();

    /**
     * Get an unmodifiable list of the elements that make up the value.
     *
     * @return the elements
     */
    @Contract(pure = true)
    @Unmodifiable
    List<String> elementList();

    /**
     * Get the element at the given index.
     *
     * @param index the index
     * @return the element
     * @throws IndexOutOfBoundsException when no element exists at the index
     */
    @Contract(pure = true)
    @KeyPattern.Value
    String element(final int index) throws IndexOutOfBoundsException;

    /**
     * Get the last element in the value.
     *
     * @return an element
     */
    @Contract(pure = true)
    @KeyPattern.Value
    default String element() {
        //noinspection PatternValidation
        return this.element(this.depth());
    }

    /**
     * Get the value or path.
     *
     * @return the value
     */
    @Override
    @Contract(pure = true)
    @KeyPattern.Value
    String value();

    /**
     * Get the value that represents the value at the given index.
     *
     * @param index the index
     * @return a value
     */
    @Contract(pure = true)
    @KeyPattern.Value
    String value(final int index);

    @Override
    @Contract(pure = true)
    @KeyPattern.Namespace
    String namespace();

    @Override
    @Contract(pure = true)
    default Key key() {
        return this;
    }

    /**
     * Get the pathed key that represents the value element at the given index.
     *
     * @param index the index
     * @return a pathed key
     * @throws IndexOutOfBoundsException when no element exists at the index
     */
    @Contract("_ -> new")
    PathedKey key(int index) throws IndexOutOfBoundsException;

    /**
     * Get the character that separates each value element.
     *
     * @return a character
     */
    @Contract(pure = true)
    char elementDelimiter();

    /**
     * Create a pathed key with the same namespace and value but with the given element delimiter. Characters in the
     * value which match the given element delimiter are escaped.
     *
     * <p>Elements which already contain the given element delimiter will be split into separate elements.</p>
     *
     * @param elementDelimiter a character
     * @return a pathed key
     */
    @Contract("_ -> new")
    PathedKey elementDelimiter(final char elementDelimiter);

    /**
     * Check if the key's value starts with the given elements.
     *
     * <p>The given elements will be split using the given element delimiter.</p>
     *
     * @param elementDelimiter the element delimiter
     * @param elements         the elements
     * @return if this key's value starts with the given elements
     */
    @Contract(pure = true)
    boolean startsWith(final char elementDelimiter, final String... elements);

    /**
     * Check if the key starts with the given start value.
     *
     * <p>The given elements will be split using this key's element delimiter.</p>
     *
     * @param elements the elements
     * @return if this key's value starts with the given key value
     */
    @Contract(pure = true)
    default boolean startsWith(final String... elements) {
        return this.startsWith(this.elementDelimiter(), elements);
    }

    /**
     * Check if this key starts with the given start key.
     *
     * <p>This pathed key starts with another pathed key when starting from the first element in each key, every
     * element in each element in the other key is equal to that in this key.</p>
     *
     * @param startKey a pathed key
     * @return if this key's value starts with the given pathed key
     */
    @Contract(pure = true)
    default boolean startsWith(final PathedKey startKey) {
        return this.startsWith(startKey.value());
    }

    /**
     * Check if the key's value ends with the given elements.
     *
     * <p>The given elements will be split using the given element delimiter.</p>
     *
     * @param elementDelimiter the element delimiter
     * @param elements         the elements
     * @return if this key's value ends with the given elements
     */
    @Contract(pure = true)
    boolean endsWith(final char elementDelimiter, final String... elements);

    /**
     * Check if the key ends with the given start value.
     *
     * <p>The given elements will be split using this key's element delimiter.</p>
     *
     * @param elements the elements
     * @return if this key's value ends with the given key value
     */
    @Contract(pure = true)
    default boolean endsWith(final String... elements) {
        return this.endsWith(this.elementDelimiter(), elements);
    }

    /**
     * Check if this key ends with the given start key.
     *
     * <p>This pathed key ends with another pathed key when starting from the last element in each key, every element
     * in each element in the other key is equal to that in this key.</p>
     *
     * @param endKey a pathed key
     * @return if this key's value ends with the given pathed key
     */
    @Contract(pure = true)
    default boolean endsWith(final PathedKey endKey) {
        return this.endsWith(endKey.value());
    }

    /**
     * Create an iterator over the value path.
     *
     * <p>The first key returned by the iterator represents the root value element, the second represents the second
     * value element and so on. The last key returned is equal to this.</p>
     *
     * @return an iterator
     */
    @Override
    @Contract(value = " -> new", pure = true)
    Iterator<PathedKey> iterator();

    /**
     * Create a spliterator over the value path. The spliterator is sized and ordered with non-null, immutable, and
     * distinct elements.
     *
     * <p>The first key returned by the spliterator represents the root value element, the second represents the second
     * value element and so on. The last key returned is equal to this.</p>
     *
     * @return a spliterator
     */
    @Override
    @Contract(value = " -> new", pure = true)
    default Spliterator<PathedKey> spliterator() {
        return Spliterators.spliterator(this.iterator(),
                this.size(),
                Spliterator.NONNULL | Spliterator.IMMUTABLE | Spliterator.SIZED | Spliterator.DISTINCT | Spliterator.ORDERED);
    }

    /**
     * Create a stream over the value path.
     *
     * <p>The first key in the stream represents the root value element, the second represents the second value element
     * and so on. The last key is equal to this.</p>
     *
     * @return a stream
     */
    @Contract(value = " -> new", pure = true)
    default Stream<PathedKey> stream() {
        return StreamSupport.stream(this.spliterator(), false);
    }

    /**
     * Create an iterator over the value path.
     *
     * <p>The first value returned by the iterator represents the root value element, the second represents the second
     * value element and so on. The last key returned is equal to this key's value.</p>
     *
     * @return an iterator
     */
    @Contract(value = " -> new", pure = true)
    Iterator<String> valueIterator();

    /**
     * Create a spliterator over the value path. The spliterator is sized and ordered with non-null, immutable, and
     * distinct elements.
     *
     * <p>The first value returned by the spliterator represents the root value element, the second represents the second
     * value element and so on. The last key returned is equal to this key's value.</p>
     *
     * @return a spliterator
     */
    @Contract(value = " -> new", pure = true)
    default Spliterator<String> valueSpliterator() {
        return Spliterators.spliterator(this.valueIterator(),
                this.size(),
                Spliterator.NONNULL | Spliterator.IMMUTABLE | Spliterator.SIZED | Spliterator.DISTINCT | Spliterator.ORDERED);
    }

    /**
     * Create a stream over the value elements.
     *
     * <p>The first value in the stream represents the root value element, the second represents the second
     * value element and so on. The last key is equal to this key's value.</p>
     *
     * @return a stream
     */
    @Contract(value = " -> new", pure = true)
    default Stream<String> valueStream() {
        return StreamSupport.stream(this.valueSpliterator(), false);
    }

    /**
     * Create an iterator over each individual value element.
     *
     * <p>The first element returned by the iterator is the root value element, the second is solely the second value
     * element and so on.</p>
     *
     * @return an iterator
     */
    @Contract(value = " -> new", pure = true)
    Iterator<String> elementIterator();

    /**
     * Create a spliterator over each individual value element. The spliterator is sized and ordered with non-null, and
     * immutable elements.
     *
     * <p>The first element returned by the spliterator is the root value element, the second is solely the second value
     * element and so on.</p>
     *
     * @return a spliterator
     */
    @Contract(value = " -> new", pure = true)
    default Spliterator<String> elementSpliterator() {
        return Spliterators.spliterator(this.elementIterator(),
                this.size(),
                Spliterator.NONNULL | Spliterator.IMMUTABLE | Spliterator.SIZED | Spliterator.ORDERED);
    }

    /**
     * Create a stream over each individual value element.
     *
     * <p>The first element in the stream is the root value element, the second is solely the second value
     * element and so on.</p>
     *
     * @return a stream
     */
    @Contract(value = " -> new", pure = true)
    default Stream<String> elementStream() {
        return StreamSupport.stream(this.elementSpliterator(), false);
    }

    @Override
    @Contract(pure = true)
    String asString();

    @Override
    @Contract(pure = true)
    String toString();

    @Override
    @Contract(pure = true)
    default int compareTo(final Key that) {
        return comparator().compare(this, that);
    }
}

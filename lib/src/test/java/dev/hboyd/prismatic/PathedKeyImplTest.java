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

package dev.hboyd.prismatic;

import com.google.common.collect.ImmutableList;
import dev.hboyd.prismatic.key.InvalidPathedKeyException;
import dev.hboyd.prismatic.key.PathedKey;
import net.kyori.adventure.key.Key;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class PathedKeyImplTest {

    @Nested
    class Construction {
        private static Stream<Arguments> invalidValueCharacterArguments() {
            return Stream.of(IntStream.rangeClosed(' ', ','),
                            IntStream.rangeClosed(':', '^'),
                            IntStream.of('`'),
                            IntStream.rangeClosed('{', '~'),
                            IntStream.of('❌', '㎜', '★', 'ａ', '．'))
                    .flatMapToInt(stream -> stream)
                    .mapToObj(integer -> Arguments.of((char) integer));
        }

        private static Stream<Arguments> validNamespaceCharacterArguments() {
            return Stream.of(IntStream.rangeClosed('0', '9'),
                            IntStream.rangeClosed('a', 'z'),
                            IntStream.of('_', '-', '.'))
                    .flatMapToInt(stream -> stream)
                    .mapToObj(integer -> Arguments.of((char) integer));
        }

        private static Stream<Arguments> validValueCharacterArguments() {
            return Stream.concat(validNamespaceCharacterArguments(), Stream.of(Arguments.of('/')));
        }

        private static Stream<Arguments> invalidNamespaceCharacterArguments() {
            return Stream.concat(invalidValueCharacterArguments(), Stream.of(Arguments.of('/')));
        }

        @Test
        void parsesNamespaceAndValueFromAString() {
            final PathedKey key = PathedKey.of("namespace:value.path");

            assertEquals("namespace", key.namespace());
            assertEquals("value.path", key.value());
            assertEquals(PathedKey.DEFAULT_ELEMENT_DELIMITER, key.elementDelimiter());
        }

        @Test
        void usesTheMinecraftNamespaceWhenAStringHasNoNamespace() {
            final PathedKey key = PathedKey.of("value.path");

            assertEquals(Key.MINECRAFT_NAMESPACE, key.namespace());
            assertEquals("value.path", key.value());
        }

        @Test
        void usesTheMinecraftNamespaceWhenAStringStartsWithTheNamespaceSeparator() {
            final PathedKey key = PathedKey.of(":value.path");

            assertEquals(Key.MINECRAFT_NAMESPACE, key.namespace());
            assertEquals("value.path", key.value());
        }

        @Test
        void joinsElementsIntoAPath() {
            assertEquals("a.b.c", PathedKey.of("namespace", "a", "b", "c").value());
        }

        @Test
        void keepsTheNamespaceAndValueOfAnExistingKey() {
            final PathedKey key = PathedKey.of(Key.key("namespace", "value.path"));

            assertEquals("namespace", key.namespace());
            assertEquals("value.path", key.value());
        }

        @Test
        void keepsTheNamespaceAndValueOfAnExistingKeyWithCustomDelimiter() {
            final PathedKey key = PathedKey.of(Key.key("namespace", "value.path"), '/');

            assertEquals("namespace", key.namespace());
            assertEquals("value.path", key.value());
        }

        @ParameterizedTest
        @MethodSource("invalidNamespaceCharacterArguments")
        @SuppressWarnings("PatternValidation")
        void rejectsAnInvalidCharacterInTheNamespace(final char invalidCharacter) {
            final String namespace = "prefix" + invalidCharacter + "suffix";
            final InvalidPathedKeyException exception = assertThrows(InvalidPathedKeyException.class,
                    () -> PathedKey.of(namespace, "value"));

            assertEquals(namespace, exception.keyNamespace());
            assertTrue(exception.getMessage().contains("at index 6"));
            assertTrue(exception.getMessage().contains("in namespace of"), exception.getMessage());
        }

        @ParameterizedTest
        @MethodSource("invalidValueCharacterArguments")
        @SuppressWarnings("PatternValidation")
        void rejectsInvalidCharacterInValue(final char invalidCharacter) {
            final String value = "pre.fix" + invalidCharacter + "su.ffix";
            final InvalidPathedKeyException exception = assertThrows(InvalidPathedKeyException.class,
                    () -> PathedKey.of("namespace", value));

            assertEquals("namespace", exception.keyNamespace());
            assertTrue(exception.getMessage().contains("at index 6"), exception.getMessage());
            assertTrue(exception.getMessage().contains("in value of"), exception.getMessage());
        }

        @ParameterizedTest
        @MethodSource("invalidValueCharacterArguments")
        void rejectsSeparatorThatIsNotAllowedInAValue(final char invalidCharacter) {
            assertThrows(IllegalArgumentException.class, () -> PathedKey.of("namespace", invalidCharacter, "value"));
        }

        @ParameterizedTest
        @MethodSource("validValueCharacterArguments")
        void acceptsSeparatorThatIsAllowedInAValue(final char validCharacter) {
            final String value;
            if (validCharacter == 'a' || validCharacter == 'b') value = "c.d";
            else value = "a.b";
            assertDoesNotThrow(() -> PathedKey.of("namespace", validCharacter, value));
        }

        @Test
        void asStringJoinsTheNamespaceAndValueWithTheNamespaceSeparator() {
            assertEquals("namespace:value.path", PathedKey.of("namespace", "value.path").asString());
        }

        @Test
        void asMinimalStringOmitsTheMinecraftNamespace() {
            assertEquals("value.path", PathedKey.of("value.path").asMinimalString());
        }

        @Test
        void asMinimalStringDoesNotOmitNonMinecraftNamespace() {
            assertEquals("not.minecraft:value.path", PathedKey.of("not.minecraft", "value.path").asMinimalString());
        }

        @Test
        void throwsOnEmptyTrailingElement() {
            assertThrows(InvalidPathedKeyException.class, () -> PathedKey.of("namespace", "value.path."));
        }

        @Test
        void throwsOnEmptyElement() {
            assertThrows(InvalidPathedKeyException.class, () -> PathedKey.of("namespace", "value..path"));
        }
    }

    @Nested
    class Then {

        @Test
        void appendsAnElementToThePath() {
            assertEquals("a.b", PathedKey.of("namespace", "a").then("b").value());
        }

        @Test
        void appendsMultipleElementsToThePath() {
            assertEquals("a.b.c", PathedKey.of("namespace", "a").then("b", "c").value());
        }

        @Test
        void appendsAnElementWithTheKeysOwnSeparator() {
            assertEquals("a/b", PathedKey.of("namespace", '/', "a").then("b").value());
        }

        @Test
        void appendsMultipleElementsWithTheKeysOwnSeparator() {
            assertEquals("a/b/c", PathedKey.of("namespace", '/', "a").then("b", "c").value());
        }

        @Test
        void returnsTheSameKeyWhenNoElementsAreGiven() {
            final PathedKey key = PathedKey.of("namespace", "a");

            assertSame(key, key.then(new String[]{}));
        }

        @Test
        void preservesTheNamespaceAndSeparator() {
            final PathedKey key = PathedKey.of("namespace", '/', "a").then("b");

            assertEquals("namespace", key.namespace());
            assertEquals('/', key.elementDelimiter());
        }

        @Test
        void doesNotModifyTheOriginalKey() {
            final PathedKey key = PathedKey.of("namespace", "a");

            key.then("b");
            key.then("b", "c");

            assertEquals("a", key.value());
        }

        @Test
        void rejectsAnElementWithAnInvalidCharacter() {
            final PathedKey key = PathedKey.of("namespace", "a");

            assertThrows(InvalidPathedKeyException.class, () -> key.then("B"));
            assertThrows(InvalidPathedKeyException.class, () -> key.then("b", "C"));
        }

        @Test
        void rejectsANullElement() {
            final PathedKey key = PathedKey.of("namespace", "a");

            assertThrows(NullPointerException.class, () -> key.then((String) null));
        }

        @Test
        void throwsOnEmptyTrailingElement() {
            final PathedKey key = PathedKey.of("namespace", "a.b.c");

            assertThrows(InvalidPathedKeyException.class, () -> key.then("d..e"));
        }

        @Test
        void throwsOnEmptyElement() {
            final PathedKey key = PathedKey.of("namespace", "a.b.c");

            assertThrows(InvalidPathedKeyException.class, () -> key.then("d.e."));
        }
    }

    @Nested
    class Parent {

        @Test
        void dropsTheLastElement() {
            assertEquals("a.b", PathedKey.of("namespace", "a.b.c").parent().value());
        }

        @Test
        void dropsTheLastElementOfATwoElementPath() {
            assertEquals("a", PathedKey.of("namespace", "a.b").parent().value());
        }

        @Test
        void throwsIfNoParent() {
            assertThrows(NoSuchElementException.class, () -> PathedKey.of("namespace", "a").parent());
        }

        @Test
        void walksUpTheWholePath() {
            final PathedKey child = PathedKey.of("namespace", '/', "a/b/c");
            final PathedKey parent = child.parent();
            final PathedKey grandparent = parent.parent();

            assertEquals("a/b", parent.value());
            assertEquals("a", grandparent.value());
            assertThrows(NoSuchElementException.class, grandparent::parent);
        }

        @Test
        void preservesTheNamespaceAndSeparator() {
            final PathedKey parent = PathedKey.of("namespace", '/', "a/b").parent();

            assertEquals("namespace", parent.namespace());
            assertEquals('/', parent.elementDelimiter());
        }

        @Test
        void undoesThen() {
            final PathedKey key = PathedKey.of("namespace", "a.b");

            assertEquals(key, key.then("c").parent());
        }
    }

    @Nested
    class Elements {

        @Test
        void theLastElementOfAMultiElementPath() {
            assertEquals("c", PathedKey.of("namespace", "a.b.c").element());
        }

        @Test
        void theLastElementOfASingleElementPath() {
            assertEquals("a", PathedKey.of("namespace", "a").element());
        }

        @Test
        void theLastElementUsesTheKeysOwnSeparator() {
            assertEquals("b", PathedKey.of("namespace", '/', "a/b").element());
        }

        @Test
        void anElementByIndex() {
            final PathedKey key = PathedKey.of("namespace", "a.b.c");

            assertEquals("a", key.element(0));
            assertEquals("b", key.element(1));
            assertEquals("c", key.element(2));
        }

        @Test
        void theOnlyElementOfASingleElementPathByIndex() {
            assertEquals("a", PathedKey.of("namespace", "a").element(0));
        }

        @Test
        void anElementByIndexUsesCustomSeparator() {
            assertEquals("b", PathedKey.of("namespace", '/', "a/b").element(1));
        }

        @Test
        void rejectsAnIndexPastTheEndOfThePath() {
            final PathedKey key = PathedKey.of("namespace", "a.b.c");

            assertThrows(IndexOutOfBoundsException.class, () -> key.element(3));
        }

        @Test
        void rejectsANegativeIndex() {
            final PathedKey key = PathedKey.of("namespace", "a.b.c");

            assertThrows(IndexOutOfBoundsException.class, () -> key.element(-1));
        }
    }

    @Nested
    class StartsWith {

        @Test
        void startsWithMatchesTheFirstElement() {
            final PathedKey key = PathedKey.of("namespace", "a.b.c");

            assertTrue(key.startsWith("a"));
            assertFalse(key.startsWith("b"));
            assertFalse(key.startsWith("c"));
        }

        @Test
        void startsWithMatchesElementsInOrder() {
            final PathedKey key = PathedKey.of("namespace", "a", "b", "c");

            assertTrue(key.startsWith("a", "b"));
            assertFalse(key.startsWith("a", "c"));
        }

        @Test
        void matchesWhenMixedNonSplitElements() {
            final PathedKey key = PathedKey.of("namespace", "a", "b", "c", "d", "e", "f", "g");

            assertTrue(key.startsWith("a.b", "c", "d.e", "f"));
        }

        @Test
        void matchWholeElementsOnly() {
            final PathedKey key = PathedKey.of("namespace", "foo.bar");

            assertFalse(key.startsWith("fo"));
        }

        @Test
        void matchesWithCustomDelimiter() {
            final PathedKey key = PathedKey.of("namespace", '/', "aa", "b");

            assertTrue(key.startsWith("aa"));
        }

        @Test
        void throwsOnNullArguments() {
            final PathedKey key = PathedKey.of("namespace", "a.b");

            assertThrows(NullPointerException.class, () -> key.startsWith((String) null));
        }

        @Test
        void doesNotMatchWhenNumberOfGivenElementsIsMoreThanItself() {
            final PathedKey key = PathedKey.of("namespace", "a.b");

            assertFalse(key.startsWith("a", "b", "c", "d"));
        }

        @Test
        void doesNotMatchWhenNumberOfSplitElementsIsMoreThanItself() {
            final PathedKey key = PathedKey.of("namespace", "a.b");

            assertFalse(key.endsWith("a.b.c.d"));
        }
    }

    @Nested
    class EndsWith {

        @Test
        void startsWithMatchesTheFirstElement() {
            final PathedKey key = PathedKey.of("namespace", "a.b.c");

            assertTrue(key.endsWith("c"));
            assertFalse(key.endsWith("b"));
            assertFalse(key.endsWith("a"));
        }

        @Test
        void startsWithMatchesElementsInOrder() {
            final PathedKey key = PathedKey.of("namespace", "a", "b", "c");

            assertTrue(key.endsWith("b", "c"));
            assertFalse(key.endsWith("c", "b"));
        }

        @Test
        void matchesWhenMixedNonSplitElements() {
            final PathedKey key = PathedKey.of("namespace", "a", "b", "c", "d", "e", "f", "g");

            assertTrue(key.endsWith("b.c", "d", "e.f", "g"));
        }

        @Test
        void matchWholeElementsOnly() {
            final PathedKey key = PathedKey.of("namespace", "foo.bar");

            assertFalse(key.endsWith("ba"));
        }

        @Test
        void matchesWithCustomDelimiter() {
            final PathedKey key = PathedKey.of("namespace", '/', "aa", "b");

            assertTrue(key.endsWith("b"));
        }

        @Test
        void throwsOnNullArguments() {
            final PathedKey key = PathedKey.of("namespace", "a.b");

            assertThrows(NullPointerException.class, () -> key.endsWith((String) null));
        }

        @Test
        void doesNotMatchWhenNumberOfGivenElementsIsMoreThanItself() {
            final PathedKey key = PathedKey.of("namespace", "a.b");

            assertFalse(key.endsWith("c", "d", "a", "b"));
        }

        @Test
        void doesNotMatchWhenNumberOfSplitElementsIsMoreThanItself() {
            final PathedKey key = PathedKey.of("namespace", "a.b");

            assertFalse(key.endsWith("c.d.a.b"));
        }
    }

    @Test
    void aSingleElementPathStartsAndEndsWithItsOnlyElement() {
        final PathedKey key = PathedKey.of("namespace", "a");

        assertTrue(key.startsWith("a"));
        assertTrue(key.endsWith("a"));
    }

    @Nested
    class ValueElementIteration {

        @Test
        void valueElementIteratorYieldsEachElement() {
            assertEquals(List.of("a", "b", "c"),
                    ImmutableList.copyOf(PathedKey.of("namespace", "a.b.c").elementIterator()));
        }

        @Test
        void valueElementIteratorYieldsTheOnlyElementOfASingleElementPath() {
            assertEquals(List.of("a"), ImmutableList.copyOf(PathedKey.of("namespace", "a").elementIterator()));
        }

        @Test
        void valueElementIteratorUsesTheKeysOwnSeparator() {
            assertEquals(List.of("a", "b"),
                    ImmutableList.copyOf(PathedKey.of("namespace", '/', "a/b").elementIterator()));
        }

        @Test
        void valueElementIteratorThrowsWhenExhausted() {
            final Iterator<String> iterator = PathedKey.of("namespace", "a").elementIterator();

            assertTrue(iterator.hasNext());
            assertEquals("a", iterator.next());
            assertFalse(iterator.hasNext());
            assertThrows(NoSuchElementException.class, iterator::next);
        }
    }

    @Nested
    class ValueIteration {

        @Test
        void valueIteratorYieldsEachPathPrefix() {
            assertEquals(List.of("a", "a.b", "a.b.c"),
                    ImmutableList.copyOf(PathedKey.of("namespace", "a.b.c").valueIterator()));
        }

        @Test
        void valueIteratorYieldsTheWholeValueOfASingleElementPath() {
            assertEquals(List.of("a"), ImmutableList.copyOf(PathedKey.of("namespace", "a").valueIterator()));
        }

        @Test
        void valueIteratorUsesTheKeysOwnSeparator() {
            assertEquals(List.of("a", "a/b"),
                    ImmutableList.copyOf(PathedKey.of("namespace", '/', "a/b").valueIterator()));
        }

        @Test
        void valueIteratorThrowsWhenExhausted() {
            final Iterator<String> iterator = PathedKey.of("namespace", "a").valueIterator();

            assertTrue(iterator.hasNext());
            assertEquals("a", iterator.next());
            assertFalse(iterator.hasNext());
            assertThrows(NoSuchElementException.class, iterator::next);
        }
    }

    @Nested
    class Iteration {

        @Test
        void iteratorYieldsAKeyForEachPathPrefix() {
            assertEquals(List.of(PathedKey.of("namespace", "a"),
                            PathedKey.of("namespace", "a.b"),
                            PathedKey.of("namespace", "a.b.c")),
                    ImmutableList.copyOf(PathedKey.of("namespace", "a.b.c").iterator()));
        }

        @Test
        void iteratorPreservesTheNamespaceAndSeparator() {
            for (final PathedKey key : PathedKey.of("namespace", '/', "a/b")) {
                assertEquals("namespace", key.namespace());
                assertEquals('/', key.elementDelimiter());
            }
        }

        @Test
        void iteratorThrowsWhenExhausted() {
            final Iterator<PathedKey> iterator = PathedKey.of("namespace", "a").iterator();

            assertTrue(iterator.hasNext());
            assertEquals(PathedKey.of("namespace", "a"), iterator.next());
            assertFalse(iterator.hasNext());
            assertThrows(NoSuchElementException.class, iterator::next);
        }

        @Test
        void anEnhancedForLoopWalksTheWholePath() {
            final List<String> values = new ArrayList<>();

            for (final PathedKey key : PathedKey.of("namespace", "a.b.c")) values.add(key.value());

            assertEquals(List.of("a", "a.b", "a.b.c"), values);
        }
    }

    @Nested
    class Comparison {

        @Test
        void equalKeysCompareEqual() {
            assertEquals(0, PathedKey.of("namespace", "a.b")
                    .compareTo(PathedKey.of("namespace", "a.b")));
        }

        @Test
        void keysWithDifferentPathSeparatorsButAreTheSameCompareEqual() {
            assertEquals(0, PathedKey.of("namespace", '/', "a/b")
                    .compareTo(PathedKey.of("namespace", "a.b")));
        }

        @Test
        void shorterPathsComeFirst() {
            final PathedKey shorter = PathedKey.of("namespace", "a");
            final PathedKey longer = PathedKey.of("namespace", "a.b");

            assertTrue(shorter.compareTo(longer) < 0);
            assertTrue(longer.compareTo(shorter) > 0);
        }

        @Test
        void elementsAreComparedInOrder() {
            final PathedKey first = PathedKey.of("namespace", "a.b");
            final PathedKey second = PathedKey.of("namespace", "a.c");

            assertTrue(first.compareTo(second) < 0);
            assertTrue(second.compareTo(first) > 0);
        }

        @Test
        void anEarlierElementOutweighsALaterOne() {
            final PathedKey first = PathedKey.of("namespace", "a.z");
            final PathedKey second = PathedKey.of("namespace", "b.a");

            assertTrue(first.compareTo(second) < 0);
            assertTrue(second.compareTo(first) > 0);
        }

        @Test
        void fallsBackToKeyOrderingForAPlainKey() {
            final PathedKey key = PathedKey.of("namespace", "a.b");

            assertEquals(0, key.compareTo(Key.key("namespace", "a.b")));
            assertTrue(key.compareTo(Key.key("namespace", "a.c")) < 0);
        }

        @Test
        void throwsOnNullKey() {
            final PathedKey key = PathedKey.of("namespace", "a.b");

            assertThrows(NullPointerException.class, () -> key.compareTo(null));
        }
    }

    @Nested
    class Equality {

        @Test
        void keysWithTheSameNamespaceValueAndSeparatorAreEqual() {
            final PathedKey first = PathedKey.of("namespace", "a.b");
            final PathedKey second = PathedKey.of("namespace", "a.b");

            assertEquals(first, second);
            assertEquals(first.hashCode(), second.hashCode());
        }

        @Test
        void aKeyEqualsItself() {
            final PathedKey key = PathedKey.of("namespace", "a.b");

            assertEquals(key, key);
        }

        @Test
        void keysWithDifferentNamespacesAreNotEqual() {
            assertNotEquals(PathedKey.of("namespace", "a.b"), PathedKey.of("other", "a.b"));
        }

        @Test
        void keysWithDifferentValuesAreNotEqual() {
            assertNotEquals(PathedKey.of("namespace", "a.b"), PathedKey.of("namespace", "a.c"));
        }

        @Test
        void keysWithDifferentSeparatorsAreNotEqual() {
            assertNotEquals(PathedKey.of("namespace", PathedKey.DEFAULT_ELEMENT_DELIMITER, "a.b"),
                    PathedKey.of("namespace", '/', "a.b"));
        }

        @Test
        void aKeyIsNotEqualToNullOrAnUnrelatedType() {
            final PathedKey key = PathedKey.of("namespace", "a.b");

            assertNotEquals(null, key);
            assertNotEquals("namespace:a.b", key);
        }

        @Test
        void pathedKeyAndKeyWithSameNamespaceAndValueAreEqual() {
            final PathedKey pathedKey = PathedKey.of("namespace", "a.b");
            final Key key = Key.key("namespace", "a.b");

            assertEquals(key.equals(pathedKey), pathedKey.equals(key));
        }
    }

    private static Stream<Arguments> keyElementAtDepthArguments() {
        return Stream.of(Arguments.of(new String[]{"a", "b", "c", "d", "e"}, 0),
                Arguments.of(new String[]{"a", "b", "c", "d", "e"}, 1),
                Arguments.of(new String[]{"a", "b", "c", "d", "e"}, 2),
                Arguments.of(new String[]{"a", "b", "c", "d", "e"}, 3),
                Arguments.of(new String[]{"a", "b", "c", "d", "e"}, 4));
    }

    @ParameterizedTest
    @MethodSource("keyElementAtDepthArguments")
    void indexedElementGetsCorrectDepth(final String[] elements, final int depthN) {
        final PathedKey pathedKey = PathedKey.of("namespace", elements);

        assertEquals(elements[depthN], pathedKey.element(depthN));
    }

    @Test
    void indexedElementThrowsExceptionIfOutOfBounds() {
        final PathedKey pathedKey = PathedKey.of("namespace", "a.b.c.d.e");

        assertThrows(IndexOutOfBoundsException.class, () -> pathedKey.value(5));
    }

    private static Stream<Arguments> keyValueAtDepthArguments() {
        return Stream.of(Arguments.of("a.b.c.d.e", "a", 0),
                Arguments.of("a.b.c.d.e", "a.b", 1),
                Arguments.of("a.b.c.d.e", "a.b.c", 2),
                Arguments.of("a.b.c.d.e", "a.b.c.d", 3),
                Arguments.of("a.b.c.d.e", "a.b.c.d.e", 4));
    }

    @ParameterizedTest
    @MethodSource("keyValueAtDepthArguments")
    void indexedValueGetsCorrectDepth(final String originalKeyValue, final String keyValueAtDepthN, final int depth) {
        final PathedKey pathedKey = PathedKey.of("namespace", originalKeyValue);

        assertEquals(keyValueAtDepthN, pathedKey.value(depth));
    }

    @Test
    void indexedValueThrowsExceptionIfOutOfBounds() {
        final PathedKey pathedKey = PathedKey.of("namespace", "a.b.c.d.e");

        assertThrows(IndexOutOfBoundsException.class, () -> pathedKey.value(5));
    }

    private static Stream<Arguments> keyElementsAtDepthArguments() {
        return Stream.of(Arguments.of(new String[]{"a", "b", "c", "d", "e"}, new String[]{"a"}, 0),
                Arguments.of(new String[]{"a", "b", "c", "d", "e"}, new String[]{"a", "b"}, 1),
                Arguments.of(new String[]{"a", "b", "c", "d", "e"}, new String[]{"a", "b", "c"}, 2),
                Arguments.of(new String[]{"a", "b", "c", "d", "e"}, new String[]{"a", "b", "c", "d"}, 3),
                Arguments.of(new String[]{"a", "b", "c", "d", "e"}, new String[]{"a", "b", "c", "d", "e"}, 4));
    }

    @ParameterizedTest
    @MethodSource("keyElementsAtDepthArguments")
    void indexedKeyGetsCorrectDepth(final String[] elements, final String[] elementsAtDepthN, final int depthN) {
        final PathedKey pathedKey = PathedKey.of("namespace", elements);

        assertArrayEquals(elementsAtDepthN, pathedKey.key(depthN).elements());
    }

    @Test
    void indexedKeyThrowsExceptionIfOutOfBounds() {
        final PathedKey pathedKey = PathedKey.of("namespace", "a.b.c.d.e");

        assertThrows(IndexOutOfBoundsException.class, () -> pathedKey.key(5));
    }

    @Test
    void keyAndPathedKeyEqualWhenSame() {
        final String keyString = "key.namespace:the.value.of.the.key";

        final PathedKey pathedKey = PathedKey.of(keyString);
        final Key key = Key.key(keyString);

        assertEquals(pathedKey, key);
    }

    @Test
    void pathedKeyWithSingleElementAndDifferingDelimiterNotEqual() {
        final PathedKey pathedKey = PathedKey.of("namespace", '/', "a.b.c.d");
        final PathedKey otherPathedKey = PathedKey.of("namespace", '.', "a", "b", "c", "d");

        assertEquals(pathedKey.value(), otherPathedKey.value());
        assertNotEquals(pathedKey, otherPathedKey);
    }
}

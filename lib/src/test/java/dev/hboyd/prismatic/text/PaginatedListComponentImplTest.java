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

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class PaginatedListComponentImplTest {
    private static final int TEST_PAGINATED_LIST_ITEM_COUNT = 1000;
    private static final PaginatedListComponent TEST_PAGINATED_LIST = PaginatedListComponent.builder()
            .itemFactory(IPaginatedListComponentItemFactory.of(IntStream.range(0,
                            TEST_PAGINATED_LIST_ITEM_COUNT)
                    .mapToObj(Component::text)
                    .toList()))
            .build();
    private static final PaginatedListStyle STYLE = PaginatedListStyleImpl.DEFAULT;
    private static final Pattern PAGE_SELECTOR_PATTERN = Pattern.compile("(?<=<|, )\\d+(?=, |>)");
    private static final Pattern ITEM_LINE_PATTERN = Pattern.compile("[0-9]+");

    private static PaginatedListComponent paginatedListWith(final int itemCount) {
        return PaginatedListComponent.builder()
                .itemFactory(IPaginatedListComponentItemFactory.of(IntStream.range(0, itemCount)
                        .mapToObj(Component::text)
                        .toList()))
                .build();
    }

    private static String renderToPlain(final PaginatedListComponent list, final int page) {
        return PlainTextComponentSerializer.plainText().serialize(list.render(page));
    }

    private static List<String> itemsOf(final PaginatedListComponent list, final int page) {
        return renderToPlain(list, page).lines()
                .filter(line -> ITEM_LINE_PATTERN.matcher(line).matches())
                .toList();
    }

    private static List<Integer> selectorsOf(final PaginatedListComponent list, final int page) {
        final String[] lines = renderToPlain(list, page)
                .split("\n");
        final String pageSelector = lines[lines.length - 1];
        final Matcher pageSelectorMatcher = PAGE_SELECTOR_PATTERN.matcher(pageSelector);
        return pageSelectorMatcher.results()
                .map(matchResult -> Integer.parseInt(pageSelector.substring(matchResult.start(), matchResult.end())))
                .toList();
    }

    private static Stream<Arguments> pageArguments() {
        return IntStream.range(1, Math.ceilDiv(TEST_PAGINATED_LIST_ITEM_COUNT, STYLE.itemsPerPage()))
                .mapToObj(Arguments::of);
    }

    private static Stream<Arguments> midRangePageArguments() {
        return IntStream.range(5, 108).mapToObj(Arguments::of);
    }

    @Test
    void rendersOnlyTheRequestedPage() {
        assertEquals(IntStream.range(STYLE.itemsPerPage(), STYLE.itemsPerPage() * 2)
                        .mapToObj(Integer::toString)
                        .toList(),
                itemsOf(TEST_PAGINATED_LIST, 2));
    }

    @Test
    void rendersPartialLastPage() {
        assertEquals(List.of("999"),
                itemsOf(TEST_PAGINATED_LIST, Math.ceilDiv(TEST_PAGINATED_LIST_ITEM_COUNT, STYLE.itemsPerPage())));
    }

    @Test
    void rendersEmptyList() {
        final PaginatedListComponent emptyPaginatedList = PaginatedListComponent.builder().build();
        assertEquals(List.of(), itemsOf(emptyPaginatedList, 1));
        assertEquals(List.of(), selectorsOf(emptyPaginatedList, 1));
    }

    @Test
    void omitsPageSelectorForSinglePage() {
        assertEquals(List.of(), selectorsOf(paginatedListWith(STYLE.itemsPerPage()), 1));
    }

    @ParameterizedTest
    @MethodSource("pageArguments")
    void showsCorrectNumberOfPageSelectors(final int page) {
        final List<Integer> selectors = selectorsOf(TEST_PAGINATED_LIST, page);

        assertEquals(STYLE.pageSelectorCount(), selectors.size());
        assertTrue(selectors.contains(page));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4})
    void doesNotShowPageSelectorsLessThanOne(final int page) {
        assertEquals(IntStream.rangeClosed(1, STYLE.pageSelectorCount()).boxed().toList(),
                selectorsOf(TEST_PAGINATED_LIST, page));
    }

    @ParameterizedTest
    @ValueSource(ints = {112, 111, 110, 109})
    void doesNotShowPageSelectorsThatAreNotAvailable(final int page) {
        final PaginatedListComponent list = paginatedListWith(TEST_PAGINATED_LIST_ITEM_COUNT);

        assertEquals(IntStream.rangeClosed(Math.ceilDiv(TEST_PAGINATED_LIST_ITEM_COUNT,
                                STYLE.itemsPerPage()) - STYLE.pageSelectorCount() + 1,
                        Math.ceilDiv(TEST_PAGINATED_LIST_ITEM_COUNT, STYLE.itemsPerPage())).boxed().toList(),
                selectorsOf(list, page));
    }

    @ParameterizedTest
    @MethodSource("midRangePageArguments")
    void centersCurrentPageInPageSelectors(final int page) {
        final int firstSelector = page - (STYLE.pageSelectorCount() / 2);

        assertEquals(IntStream.rangeClosed(firstSelector,
                        firstSelector + STYLE.pageSelectorCount() - 1).boxed().toList(),
                selectorsOf(TEST_PAGINATED_LIST, page));
    }

    @Test
    void onlyShowsSelectorsForAvailablePages() {
        final PaginatedListComponent list = paginatedListWith(STYLE.itemsPerPage() * 2 + 1); // 2 full pages and one extra item

        assertEquals(IntStream.rangeClosed(1, 3).boxed().toList(), selectorsOf(list, 1));
        assertEquals(IntStream.rangeClosed(1, 3).boxed().toList(), selectorsOf(list, 2));
        assertEquals(IntStream.rangeClosed(1, 3).boxed().toList(), selectorsOf(list, 3));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void throwsOnInvalidPage(final int page) {
        assertThrows(IllegalArgumentException.class, () -> TEST_PAGINATED_LIST.render(page));
    }
}

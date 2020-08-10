package fr.jmini.utils.pathorder;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

public class AbsolutePathComparatorTest {

    @Test
    void testGetNameSuffix() throws Exception {
        assertThat(AbsolutePathComparator.getNameSuffix("test.adoc")).isNull();
        assertThat(AbsolutePathComparator.getNameSuffix("test")).isNull();
        assertThat(AbsolutePathComparator.getNameSuffix(".adoc")).isNull();
        assertThat(AbsolutePathComparator.getNameSuffix("test.internal.adoc")).isEqualTo("internal");
        assertThat(AbsolutePathComparator.getNameSuffix("test..adoc")).isEqualTo("");
        assertThat(AbsolutePathComparator.getNameSuffix("..adoc")).isEqualTo("");
    }

    @Test
    void testNameWithoutSuffix() throws Exception {
        assertThat(AbsolutePathComparator.getNameWithoutSuffix("test.adoc")).isEqualTo("test");
        assertThat(AbsolutePathComparator.getNameWithoutSuffix("test")).isEqualTo("test");
        assertThat(AbsolutePathComparator.getNameWithoutSuffix(".adoc")).isEqualTo("");
        assertThat(AbsolutePathComparator.getNameWithoutSuffix("test.internal.adoc")).isEqualTo("test");
        assertThat(AbsolutePathComparator.getNameWithoutSuffix("test..adoc")).isEqualTo("test");
        assertThat(AbsolutePathComparator.getNameWithoutSuffix("..adoc")).isEqualTo("");
    }

    @Test
    void testGetCommonPath() throws Exception {
        assertThat(AbsolutePathComparator.getCommonPath(Paths.get("/abc/xxx/file.txt"), Paths.get("/abc/xxx/other.txt"))).isEqualByComparingTo(Paths.get("/abc/xxx"));
        assertThat(AbsolutePathComparator.getCommonPath(Paths.get("/abc/xxx/file.txt"), Paths.get("/abc/yyy/file.txt"))).isEqualByComparingTo(Paths.get("/abc"));
        assertThat(AbsolutePathComparator.getCommonPath(Paths.get("/abc/xxx/file.txt"), Paths.get("/abc/file.txt"))).isEqualByComparingTo(Paths.get("/abc"));
        assertThat(AbsolutePathComparator.getCommonPath(Paths.get("/abc/xxx/file.txt"), Paths.get("/xyz/file.txt"))).isEqualByComparingTo(Paths.get("/"));
        assertThat(AbsolutePathComparator.getCommonPath(Paths.get("/abc/xxx/file.txt"), Paths.get("/"))).isEqualByComparingTo(Paths.get("/"));
        assertThat(AbsolutePathComparator.getCommonPath(Paths.get("/"), Paths.get("/abc/xxx/file.txt"))).isEqualByComparingTo(Paths.get("/"));
        assertThat(AbsolutePathComparator.getCommonPath(Paths.get("C:/Test/This"), Paths.get("C:/Test/That"))).isEqualByComparingTo(Paths.get("C:/Test"));
    }

    @Test
    void testCompare() throws Exception {
        Function<Path, SortConfig> orderSupplier = p -> null;
        List<String> nameSuffixes = Collections.emptyList();

        List<String> list1 = Arrays.asList(
                "/folder/file.adoc",
                "/folder/xxx/file2.adoc",
                "/folder/xxx/page1.adoc");
        runCompare(orderSupplier, nameSuffixes, list1, list1, Collections.emptyList());

        List<String> list2 = Arrays.asList(
                "/folder/xxx/page1.adoc",
                "/folder/xxx/file2.adoc",
                "/folder/file.adoc");
        runCompare(orderSupplier, nameSuffixes, list2, list1, Collections.emptyList());

        List<String> list3 = Arrays.asList(
                "/folder/file.adoc",
                "/folder/xxx/index.adoc",
                "/folder/xxx/alpha.adoc");
        runCompare(orderSupplier, nameSuffixes, list3, list3, Collections.emptyList());

        List<String> list4 = Arrays.asList(
                "/folder/file.adoc",
                "/folder/file.internal.adoc",
                "/folder/file.private.adoc");
        runCompare(orderSupplier, nameSuffixes, list4, list4, Collections.emptyList());

        List<String> list5 = Arrays.asList(
                "/folder/file.private.adoc",
                "/folder/file.adoc",
                "/folder/file.internal.adoc");
        runCompare(orderSupplier, nameSuffixes, list5, list4, Collections.emptyList());

        List<String> list6 = Arrays.asList(
                "/folder/index.adoc",
                "/folder/content1.adoc",
                "/folder/content2.adoc");
        runCompare(orderSupplier, nameSuffixes, list6, list6, Collections.emptyList());

        List<String> list7 = Arrays.asList(
                "/folder/content2.adoc",
                "/folder/content1.adoc",
                "/folder/index.adoc");
        runCompare(orderSupplier, nameSuffixes, list7, list6, Collections.emptyList());
    }

    @Test
    void testCompareWithSuffix() throws Exception {
        Function<Path, SortConfig> orderSupplier = p -> null;
        List<String> nameSuffixes = Arrays.asList("lorem", "ipsum", "dolor");

        List<String> list1 = Arrays.asList(
                "/folder/file.adoc",
                "/folder/file.lorem.adoc",
                "/folder/file.ipsum.adoc",
                "/folder/file.dolor.adoc");
        runCompare(orderSupplier, nameSuffixes, list1, list1, Collections.emptyList());

        List<String> list2 = Arrays.asList(
                "/folder/file.ipsum.adoc",
                "/folder/file.lorem.adoc",
                "/folder/file.adoc",
                "/folder/file.dolor.adoc");
        runCompare(orderSupplier, nameSuffixes, list2, list1, Collections.emptyList());

        List<String> list3 = Arrays.asList(
                "/folder/index.adoc",
                "/folder/index.ipsum.adoc",
                "/folder/file.adoc",
                "/folder/file.lorem.adoc",
                "/folder/file.ipsum.adoc");
        runCompare(orderSupplier, nameSuffixes, list3, list3, Collections.emptyList());

        List<String> list4 = Arrays.asList(
                "/folder/file.adoc",
                "/folder/file.lorem.adoc",
                "/folder/file.ipsum.adoc",
                "/folder/index.adoc",
                "/folder/index.ipsum.adoc");
        runCompare(orderSupplier, nameSuffixes, list4, list3, Collections.emptyList());

        List<String> list5 = Arrays.asList(
                "/folder/alice.adoc",
                "/folder/alice.ipsum.adoc",
                "/folder/bob.adoc",
                "/folder/bob.lorem.adoc",
                "/folder/bob.ipsum.adoc",
                "/folder/charlie.adoc",
                "/folder/charlie.ipsum.adoc");
        runCompare(orderSupplier, nameSuffixes, list5, list5, Collections.emptyList());

        List<String> list6 = Arrays.asList(
                "/folder/alice.ipsum.adoc",
                "/folder/charlie.ipsum.adoc",
                "/folder/bob.ipsum.adoc",
                "/folder/bob.adoc",
                "/folder/bob.lorem.adoc",
                "/folder/charlie.adoc",
                "/folder/alice.adoc");
        runCompare(orderSupplier, nameSuffixes, list6, list5, Collections.emptyList());
    }

    @Test
    void testCompareWithOrder() throws Exception {
        Function<Path, SortConfig> orderSupplier = p -> new SortConfigImpl(Arrays.asList("lorem", "ipsum", "dolor"), null);
        List<String> nameSuffixes = Collections.emptyList();

        List<String> expectedMessages = Arrays.asList(
                "No ordering indication for 'test' in '/folder', putting it at the end",
                "No ordering indication for 'other1' in '/folder', putting it at the end",
                "No ordering indication for 'other5' in '/folder', putting it at the end",
                "No ordering indication for 'other10' in '/folder', putting it at the end",
                "No ordering indication for 'file' in '/folder', putting it at the end");
        List<String> list1 = Arrays.asList(
                "/folder/file.adoc",
                "/folder/test/lorem.adoc",
                "/folder/test/ipsum.adoc",
                "/folder/test/dolor.adoc");
        runCompare(orderSupplier, nameSuffixes, list1, list1, expectedMessages);

        List<String> list2 = Arrays.asList(
                "/folder/test/ipsum.adoc",
                "/folder/test/lorem.adoc",
                "/folder/file.adoc",
                "/folder/test/dolor.adoc");
        runCompare(orderSupplier, nameSuffixes, list2, list1, expectedMessages);

        List<String> list3 = Arrays.asList(
                "/folder/file.adoc",
                "/folder/test/index.adoc",
                "/folder/test/lorem.adoc",
                "/folder/test/dolor.adoc");
        runCompare(orderSupplier, nameSuffixes, list3, list3, expectedMessages);

        List<String> list4 = Arrays.asList(
                "/folder/index.adoc",
                "/folder/index.internal.adoc",
                "/folder/index.private.adoc",
                "/folder/lorem.adoc",
                "/folder/lorem.internal.adoc",
                "/folder/lorem.private.adoc",
                "/folder/ipsum.adoc");
        runCompare(orderSupplier, nameSuffixes, list4, list4, expectedMessages);

        List<String> list5 = Arrays.asList(
                "/folder/index.internal.adoc",
                "/folder/lorem.internal.adoc",
                "/folder/index.adoc",
                "/folder/ipsum.adoc",
                "/folder/lorem.adoc",
                "/folder/index.private.adoc",
                "/folder/lorem.private.adoc");
        runCompare(orderSupplier, nameSuffixes, list5, list4, expectedMessages);

        List<String> list6 = Arrays.asList(
                "/folder/lorem.html",
                "/folder/ipsum.html",
                "/folder/dolor.html",
                "/folder/other1.html",
                "/folder/other5.html",
                "/folder/other10.html");
        runCompare(orderSupplier, Order.NATURAL, nameSuffixes, list6, list6, expectedMessages);

        List<String> list7 = Arrays.asList(
                "/folder/other1.html",
                "/folder/lorem.html",
                "/folder/other10.html",
                "/folder/dolor.html",
                "/folder/other5.html",
                "/folder/ipsum.html");
        runCompare(orderSupplier, Order.NATURAL, nameSuffixes, list7, list6, expectedMessages);

        List<String> list8 = Arrays.asList(
                "/folder/lorem.html",
                "/folder/ipsum.html",
                "/folder/dolor.html",
                "/folder/other1.html",
                "/folder/other10.html",
                "/folder/other5.html");
        runCompare(orderSupplier, Order.NATURAL, nameSuffixes, list8, list6, expectedMessages);
        runCompare(orderSupplier, Order.LEXICOGRAPHIC, nameSuffixes, list6, list8, expectedMessages);
        runCompare(orderSupplier, Order.LEXICOGRAPHIC, nameSuffixes, list7, list8, expectedMessages);
        runCompare(orderSupplier, Order.LEXICOGRAPHIC, nameSuffixes, list8, list8, expectedMessages);
    }

    @Test
    void testCompareWithOrderAndSuffix() throws Exception {
        Function<Path, SortConfig> orderSupplier = p -> new SortConfigImpl(Arrays.asList("lorem", "ipsum", "dolor"), null);
        List<String> nameSuffixes = Arrays.asList("private", "internal");
        List<String> expectedMessages = Collections.emptyList();

        List<String> list1 = Arrays.asList(
                "/folder/lorem.txt",
                "/folder/lorem.private.txt",
                "/folder/lorem.internal.txt",
                "/folder/ipsum.txt",
                "/folder/ipsum.internal.txt",
                "/folder/dolor.txt",
                "/folder/dolor.private.txt");
        runCompare(orderSupplier, nameSuffixes, list1, list1, expectedMessages);

        List<String> list2 = Arrays.asList(
                "/folder/dolor.txt",
                "/folder/lorem.internal.txt",
                "/folder/ipsum.internal.txt",
                "/folder/ipsum.txt",
                "/folder/lorem.private.txt",
                "/folder/dolor.private.txt",
                "/folder/lorem.txt");
        runCompare(orderSupplier, nameSuffixes, list2, list1, expectedMessages);

        List<String> list3 = Arrays.asList(
                "/folder/index.txt",
                "/folder/index.private.txt",
                "/folder/index.internal.txt",
                "/folder/lorem.txt",
                "/folder/lorem.private.txt",
                "/folder/lorem.internal.txt",
                "/folder/ipsum.txt");
        runCompare(orderSupplier, nameSuffixes, list3, list3, expectedMessages);

        List<String> list4 = Arrays.asList(
                "/folder/index.internal.txt",
                "/folder/lorem.internal.txt",
                "/folder/index.txt",
                "/folder/ipsum.txt",
                "/folder/lorem.txt",
                "/folder/index.private.txt",
                "/folder/lorem.private.txt");
        runCompare(orderSupplier, nameSuffixes, list4, list3, expectedMessages);
    }

    @Test
    void testCompareWithDefaultSortNotSet() throws Exception {
        Function<Path, SortConfig> orderSupplier = p -> new SortConfigImpl(null, null);
        List<String> nameSuffixes = Collections.emptyList();

        List<String> list1 = Arrays.asList(
                "/folder/alice.adoc",
                "/folder/bob.adoc",
                "/folder/charlie.adoc");
        runCompare(orderSupplier, nameSuffixes, list1, list1, Collections.emptyList());

        List<String> list2 = Arrays.asList(
                "/folder/bob.adoc",
                "/folder/charlie.adoc",
                "/folder/alice.adoc");
        runCompare(orderSupplier, nameSuffixes, list2, list1, Collections.emptyList());

        List<String> list3 = Arrays.asList(
                "/p/index.adoc",
                "/p/alpha.adoc",
                "/p/beta.adoc");
        runCompare(orderSupplier, nameSuffixes, list3, list3, Collections.emptyList());

        List<String> list4 = Arrays.asList(
                "/p/index.adoc",
                "/p/alpha.adoc",
                "/p/beta.adoc");
        runCompare(orderSupplier, nameSuffixes, list4, list3, Collections.emptyList());
    }

    @Test
    void testCompareWithDefaultSortLexicographic() throws Exception {
        Function<Path, SortConfig> orderSupplier = p -> new SortConfigImpl(null, Order.LEXICOGRAPHIC);
        List<String> nameSuffixes = Collections.emptyList();

        List<String> list1 = Arrays.asList(
                "/folder/alice.adoc",
                "/folder/bob.adoc",
                "/folder/charlie.adoc");
        runCompare(orderSupplier, nameSuffixes, list1, list1, Collections.emptyList());

        List<String> list2 = Arrays.asList(
                "/folder/bob.adoc",
                "/folder/charlie.adoc",
                "/folder/alice.adoc");
        runCompare(orderSupplier, nameSuffixes, list2, list1, Collections.emptyList());

        List<String> list3 = Arrays.asList(
                "/p/index.adoc",
                "/p/alpha.adoc",
                "/p/beta.adoc");
        runCompare(orderSupplier, nameSuffixes, list3, list3, Collections.emptyList());

        List<String> list4 = Arrays.asList(
                "/p/index.adoc",
                "/p/alpha.adoc",
                "/p/beta.adoc");
        runCompare(orderSupplier, nameSuffixes, list4, list3, Collections.emptyList());
    }

    @Test
    void testCompareWithDefaultSortLexicographicReversed() throws Exception {
        Function<Path, SortConfig> orderSupplier = p -> new SortConfigImpl(null, Order.LEXICOGRAPHIC_REVERSED);
        List<String> nameSuffixes = Collections.emptyList();

        List<String> list1 = Arrays.asList(
                "/folder/charlie.adoc",
                "/folder/bob.adoc",
                "/folder/alice.adoc");
        runCompare(orderSupplier, nameSuffixes, list1, list1, Collections.emptyList());

        List<String> list2 = Arrays.asList(
                "/folder/bob.adoc",
                "/folder/charlie.adoc",
                "/folder/alice.adoc");
        runCompare(orderSupplier, nameSuffixes, list2, list1, Collections.emptyList());

        List<String> list3 = Arrays.asList(
                "/p/index.adoc",
                "/p/beta.adoc",
                "/p/alpha.adoc");
        runCompare(orderSupplier, nameSuffixes, list3, list3, Collections.emptyList());

        List<String> list4 = Arrays.asList(
                "/p/alpha.adoc",
                "/p/index.adoc",
                "/p/beta.adoc");
        runCompare(orderSupplier, nameSuffixes, list4, list3, Collections.emptyList());
    }

    @Test
    void testCompareWithDefaultSortNatural() throws Exception {
        Function<Path, SortConfig> orderSupplier = p -> new SortConfigImpl(null, Order.NATURAL);
        List<String> nameSuffixes = Collections.emptyList();

        List<String> list1 = Arrays.asList(
                "/folder/alice.adoc",
                "/folder/bob.adoc",
                "/folder/charlie.adoc");
        runCompare(orderSupplier, nameSuffixes, list1, list1, Collections.emptyList());

        List<String> list2 = Arrays.asList(
                "/folder/bob.adoc",
                "/folder/charlie.adoc",
                "/folder/alice.adoc");
        runCompare(orderSupplier, nameSuffixes, list2, list1, Collections.emptyList());

        List<String> list3 = Arrays.asList(
                "/p/index.adoc",
                "/p/alpha.adoc",
                "/p/beta.adoc");
        runCompare(orderSupplier, nameSuffixes, list3, list3, Collections.emptyList());

        List<String> list4 = Arrays.asList(
                "/p/index.adoc",
                "/p/alpha.adoc",
                "/p/beta.adoc");
        runCompare(orderSupplier, nameSuffixes, list4, list3, Collections.emptyList());

        List<String> list5 = Arrays.asList(
                "/p/file5.adoc",
                "/p/file9.adoc",
                "/p/file10.adoc");
        runCompare(orderSupplier, nameSuffixes, list5, list5, Collections.emptyList());

        List<String> list6 = Arrays.asList(
                "/p/file10.adoc",
                "/p/file5.adoc",
                "/p/file9.adoc");
        runCompare(orderSupplier, nameSuffixes, list6, list5, Collections.emptyList());
    }

    @Test
    void testCompareWithDefaultSortNaturalReversed() throws Exception {
        Function<Path, SortConfig> orderSupplier = p -> new SortConfigImpl(null, Order.NATURAL_REVERSED);
        List<String> nameSuffixes = Collections.emptyList();

        List<String> list1 = Arrays.asList(
                "/folder/charlie.adoc",
                "/folder/bob.adoc",
                "/folder/alice.adoc");
        runCompare(orderSupplier, nameSuffixes, list1, list1, Collections.emptyList());

        List<String> list2 = Arrays.asList(
                "/folder/bob.adoc",
                "/folder/charlie.adoc",
                "/folder/alice.adoc");
        runCompare(orderSupplier, nameSuffixes, list2, list1, Collections.emptyList());

        List<String> list3 = Arrays.asList(
                "/p/index.adoc",
                "/p/beta.adoc",
                "/p/alpha.adoc");
        runCompare(orderSupplier, nameSuffixes, list3, list3, Collections.emptyList());

        List<String> list4 = Arrays.asList(
                "/p/alpha.adoc",
                "/p/index.adoc",
                "/p/beta.adoc");
        runCompare(orderSupplier, nameSuffixes, list4, list3, Collections.emptyList());

        List<String> list5 = Arrays.asList(
                "/p/file11.adoc",
                "/p/file7.adoc",
                "/p/file3.adoc");
        runCompare(orderSupplier, nameSuffixes, list5, list5, Collections.emptyList());

        List<String> list6 = Arrays.asList(
                "/p/file3.adoc",
                "/p/file11.adoc",
                "/p/file7.adoc");
        runCompare(orderSupplier, nameSuffixes, list6, list5, Collections.emptyList());
    }

    @Test
    void testCompareWithSuffixAndOrder() throws Exception {
        Function<Path, SortConfig> orderSupplier = p -> new SortConfigImpl(null, Order.NATURAL_REVERSED);
        List<String> nameSuffixes = Arrays.asList("lorem", "ipsum");

        List<String> list1 = Arrays.asList(
                "/folder/2-19-1-test",
                "/folder/2-19-1-test.lorem.adoc",
                "/folder/2-19-1-test.ipsum.adoc",
                "/folder/2-19-0-test",
                "/folder/2-19-0-test.lorem.adoc",
                "/folder/2-19-0-test.ipsum.adoc",
                "/folder/2-19-0-large.adoc",
                "/folder/2-18-test.adoc",
                "/folder/2-18-0-test.adoc",
                "/folder/2-18-0-info-1-test.adoc",
                "/folder/2-18-0-info-1-test.lorem.adoc",
                "/folder/2-18-0-info-0-test.adoc",
                "/folder/2-18-0-info-0-test.lorem.adoc",
                "/folder/2-17-0-test.adoc",
                "/folder/2-9-3-test.adoc",
                "/folder/2-7-10-test.adoc",
                "/folder/2-1-0-test.adoc");
        runCompare(orderSupplier, nameSuffixes, list1, list1, Collections.emptyList());

        List<String> list2 = Arrays.asList(
                "/folder/2-9-3-test.adoc",
                "/folder/2-19-1-test.ipsum.adoc",
                "/folder/2-19-1-test.lorem.adoc",
                "/folder/2-19-1-test",
                "/folder/2-7-10-test.adoc",
                "/folder/2-19-0-test.lorem.adoc",
                "/folder/2-18-0-info-1-test.adoc",
                "/folder/2-19-0-test",
                "/folder/2-18-test.adoc",
                "/folder/2-19-0-test.ipsum.adoc",
                "/folder/2-19-0-large.adoc",
                "/folder/2-18-0-info-1-test.lorem.adoc",
                "/folder/2-18-0-test.adoc",
                "/folder/2-17-0-test.adoc",
                "/folder/2-1-0-test.adoc",
                "/folder/2-18-0-info-0-test.adoc",
                "/folder/2-18-0-info-0-test.lorem.adoc");
        runCompare(orderSupplier, nameSuffixes, list2, list1, Collections.emptyList());
    }

    private void runCompare(Function<Path, SortConfig> orderSupplier, List<String> nameSuffixes, List<String> list, List<String> expected, List<String> expectedMessages) {
        runCompare(orderSupplier, Order.LEXICOGRAPHIC, nameSuffixes, list, expected, expectedMessages);
    }

    private void runCompare(Function<Path, SortConfig> orderSupplier, Order orderWhenNotDefined, List<String> nameSuffixes, List<String> list, List<String> expected, List<String> expectedMessages) {
        AbsolutePathComparator comparator = new AbsolutePathComparator(orderSupplier, nameSuffixes, orderWhenNotDefined);
        List<String> result = list.stream()
                .map(Paths::get)
                .sorted(comparator)
                .map(Path::toString)
                .collect(Collectors.toList());
        assertThat(result).isEqualTo(expected);
        assertThat(comparator.getMessages()).isSubsetOf(expectedMessages);
    }

}

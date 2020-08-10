package fr.jmini.utils.pathorder;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

public class AbsolutePathComparator implements Comparator<Path> {

    private Map<Path, SortConfig> configMap = new HashMap<>();
    private Function<Path, SortConfig> sortConfigSupplier;
    private Set<String> messages = new HashSet<>();
    private List<String> suffixes;
    private Order orderWhenNotDefined;

    public AbsolutePathComparator(Function<Path, SortConfig> sortConfigSupplier, List<String> suffixes, Order orderWhenNotDefined) {
        this.sortConfigSupplier = sortConfigSupplier;
        this.suffixes = suffixes;
        this.orderWhenNotDefined = orderWhenNotDefined;
    }

    @Override
    public int compare(Path p1, Path p2) {
        if (Objects.equals(p1, p2)) {
            return 0;
        }
        Path commonPath = getCommonPath(p1, p2);
        String name1 = commonPath.relativize(p1)
                .getName(0)
                .toString();
        String nameWithoutSuffix1 = getNameWithoutSuffix(name1);
        String name2 = commonPath.relativize(p2)
                .getName(0)
                .toString();
        String nameWithoutSuffix2 = getNameWithoutSuffix(name2);

        Order defaultOrder;
        SortConfig sortConfig = configMap.computeIfAbsent(commonPath, sortConfigSupplier);
        if (sortConfig != null) {
            if (sortConfig.getDefaultOrder() != null) {
                defaultOrder = sortConfig.getDefaultOrder();
            } else {
                defaultOrder = orderWhenNotDefined;
            }
            List<String> order = sortConfig.getOrder();
            if (order != null) {
                if (!order.contains("index")) {
                    if ("index".equals(nameWithoutSuffix1)) {
                        if ("index".equals(nameWithoutSuffix2)) {
                            return compareNameAndSuffixes(name1, name2, defaultOrder);
                        }
                        return -1;
                    } else if ("index".equals(nameWithoutSuffix2)) {
                        return 1;
                    }
                }
                if (order.contains(nameWithoutSuffix1)) {
                    if (order.contains(nameWithoutSuffix2)) {
                        int result = order.indexOf(nameWithoutSuffix1) - order.indexOf(nameWithoutSuffix2);
                        if (result == 0) {
                            return compareNameAndSuffixes(name1, name2, defaultOrder);
                        }
                        return result;
                    } else {
                        messages.add("No ordering indication for '" + nameWithoutSuffix2 + "' in '" + commonPath + "', putting it at the end");
                        return -1;
                    }
                } else {
                    messages.add("No ordering indication for '" + nameWithoutSuffix1 + "' in '" + commonPath + "', putting it at the end");
                    if (order.contains(nameWithoutSuffix2)) {
                        return 1;
                    }
                }
            }
        } else {
            defaultOrder = orderWhenNotDefined;
        }
        if ("index".equals(nameWithoutSuffix1)) {
            if ("index".equals(nameWithoutSuffix2)) {
                return compareNameAndSuffixes(name1, name2, defaultOrder);
            }
            return -1;
        } else if ("index".equals(nameWithoutSuffix2)) {
            return 1;
        }
        int result = compareNameWithoutSuffixes(nameWithoutSuffix1, nameWithoutSuffix2, defaultOrder);
        if (result == 0) {
            return compareNameAndSuffixes(name1, name2, defaultOrder);
        }
        return result;
    }

    private int compareNameAndSuffixes(String name1, String name2, Order defaultOrder) {
        String suffix1 = getNameSuffix(name1);
        String suffix2 = getNameSuffix(name2);
        if (suffixes == null || suffixes.isEmpty() || Objects.equals(suffix1, suffix2)) {
            return compareNameWithoutSuffixes(name1, name2, defaultOrder);
        }
        if (suffix1 == null) {
            return -1;
        }
        if (suffix2 == null) {
            return 1;
        }
        return suffixes.indexOf(suffix1) - suffixes.indexOf(suffix2);
    }

    private int compareNameWithoutSuffixes(String name1, String name2, Order defaultOrder) {
        switch (defaultOrder) {
        case LEXICOGRAPHIC:
            return name1.compareTo(name2);
        case LEXICOGRAPHIC_REVERSED:
            return name2.compareTo(name1);
        case NATURAL:
            return AlphanumComparator.compare(name1, name2);
        case NATURAL_REVERSED:
            return AlphanumComparator.compare(name2, name1);
        default:
            throw new IllegalStateException("Illegal defaultOrder value: " + defaultOrder);
        }
    }

    public Set<String> getMessages() {
        return messages;
    }

    static Path getCommonPath(Path p1, Path p2) {
        if (Objects.equals(p1, p2)) {
            return p1;
        }

        Path result = null;
        if (p1.isAbsolute() && p2.isAbsolute() && Objects.equals(p1.getRoot(), p2.getRoot())) {
            result = p1.getRoot();
        } else if (!p1.isAbsolute() && !p2.isAbsolute()) {
            result = Paths.get("");
        }

        if (result != null) {
            Iterator<Path> i1 = p1.iterator();
            Iterator<Path> i2 = p2.iterator();
            Path path1 = null;
            Path path2 = null;
            while (i1.hasNext() && i2.hasNext() && Objects.equals(path1, path2)) {
                if (path1 != null) {
                    result = result.resolve(path1);
                }
                path1 = i1.next();
                path2 = i2.next();
            }
        }
        return result;
    }

    static String getNameSuffix(String fileName) {
        int extensionPosition = fileName.lastIndexOf('.');
        if (extensionPosition > -1) {
            int nameSuffixPosition = fileName.substring(0, extensionPosition)
                    .lastIndexOf('.');
            if (nameSuffixPosition > -1) {
                return fileName.substring(nameSuffixPosition + 1, extensionPosition);
            }
        }
        return null;
    }

    static String getNameWithoutSuffix(String fileName) {
        int extensionPosition = fileName.lastIndexOf('.');
        if (extensionPosition > -1) {
            int nameSuffixPosition = fileName.substring(0, extensionPosition)
                    .lastIndexOf('.');
            if (nameSuffixPosition > -1) {
                return fileName.substring(0, nameSuffixPosition);
            }
            return fileName.substring(0, extensionPosition);
        }
        return fileName;
    }
}

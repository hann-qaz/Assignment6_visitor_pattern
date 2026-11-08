package kz.edu.aitu.visitor;

import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TreeIntegrityTest {
    @Test
    void negativeSizeIsRejectedButZeroIsValid() {
        assertThrows(IllegalArgumentException.class, () -> new FileNode("bad.txt", -1));
        assertEquals(0, new FileNode("empty.txt", 0).getSize());
    }

    @Test
    void invalidNamesAndNullChildrenAreRejected() {
        for (String name : new String[]{null, "", "  ", ".", "..", "a/b", "a\\b"}) {
            assertThrows(IllegalArgumentException.class, () -> new FileNode(name, 1));
            assertThrows(IllegalArgumentException.class, () -> new DirectoryNode(name));
        }
        DirectoryNode root = new DirectoryNode("root");
        assertThrows(IllegalArgumentException.class, () -> root.addChild(null));
        assertTrue(root.getChildren().isEmpty());
    }

    @Test
    void duplicateNamesAcrossElementTypesLeaveTreeUnchanged() {
        DirectoryNode root = new DirectoryNode("root");
        FileNode original = new FileNode("same", 10);
        DirectoryNode duplicate = new DirectoryNode("same");
        root.addChild(original);
        assertThrows(IllegalArgumentException.class, () -> root.addChild(duplicate));
        assertEquals(List.of(original), root.getChildren());
        assertSame(root, original.getParent());
        assertNull(duplicate.getParent());
        DirectoryNode other = new DirectoryNode("other");
        other.addChild(duplicate);
        assertSame(other, duplicate.getParent());
    }

    @Test
    void filesAndDirectoriesCannotHaveTwoParents() {
        DirectoryNode first = new DirectoryNode("first");
        DirectoryNode second = new DirectoryNode("second");
        FileNode file = new FileNode("file.txt", 5);
        DirectoryNode folder = new DirectoryNode("folder");
        first.addChild(file);
        first.addChild(folder);
        assertThrows(IllegalArgumentException.class, () -> second.addChild(file));
        assertThrows(IllegalArgumentException.class, () -> second.addChild(folder));
        assertThrows(IllegalArgumentException.class, () -> first.addChild(file));
        assertEquals(List.of(file, folder), first.getChildren());
        assertTrue(second.getChildren().isEmpty());
        assertSame(first, file.getParent());
        assertSame(first, folder.getParent());
    }

    @Test
    void selfAndAncestorCyclesAreRejectedAndValidNestingStillWorks() {
        DirectoryNode root = new DirectoryNode("root");
        DirectoryNode middle = new DirectoryNode("middle");
        DirectoryNode leaf = new DirectoryNode("leaf");
        root.addChild(middle);
        middle.addChild(leaf);
        assertThrows(IllegalArgumentException.class, () -> root.addChild(root));
        assertThrows(IllegalArgumentException.class, () -> leaf.addChild(root));
        assertNull(root.getParent());
        assertTrue(leaf.getChildren().isEmpty());
        leaf.addChild(new FileNode("valid.txt", 31));
        TotalSizeVisitor visitor = new TotalSizeVisitor();
        visitor.scan(root);
        assertEquals(31, visitor.getTotalSize());
    }

    @Test
    void childrenAreImmutableSnapshots() {
        DirectoryNode root = new DirectoryNode("root");
        FileNode first = new FileNode("first.txt", 1);
        root.addChild(first);
        List<Node> snapshot = root.getChildren();
        assertThrows(UnsupportedOperationException.class, snapshot::clear);
        assertThrows(UnsupportedOperationException.class,
                () -> snapshot.add(new FileNode("injected.txt", 2)));
        root.addChild(new FileNode("second.txt", 3));
        assertEquals(List.of(first), snapshot);
        assertEquals(2, root.getChildren().size());
    }
}

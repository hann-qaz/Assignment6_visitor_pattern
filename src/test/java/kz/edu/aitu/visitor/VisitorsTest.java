package kz.edu.aitu.visitor;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class VisitorsTest {
    @Test
    void sizeIncludesEveryNestedFileOnce() {
        DirectoryNode root = new DirectoryNode("root");
        DirectoryNode lectures = new DirectoryNode("lectures");
        DirectoryNode week = new DirectoryNode("week");
        root.addChild(new FileNode("index.txt", 23));
        root.addChild(lectures);
        lectures.addChild(new FileNode("slides.pdf", 710));
        lectures.addChild(week);
        week.addChild(new FileNode("task.txt", 91));
        TotalSizeVisitor visitor = new TotalSizeVisitor();
        visitor.scan(root);
        assertEquals(824, visitor.getTotalSize());
    }

    @Test
    void sizeUsesLongAndSupportsFileRoot() {
        TotalSizeVisitor visitor = new TotalSizeVisitor();
        visitor.scan(new FileNode("backup.zip", 3_500_000_000L));
        assertEquals(3_500_000_000L, visitor.getTotalSize());
    }

    @Test
    void emptyDirectoryAndZeroSizeFileHaveZeroTotal() {
        DirectoryNode root = new DirectoryNode("empty");
        TotalSizeVisitor visitor = new TotalSizeVisitor();
        visitor.scan(root);
        assertEquals(0, visitor.getTotalSize());
        root.addChild(new FileNode("blank.txt", 0));
        visitor.scan(root);
        assertEquals(0, visitor.getTotalSize());
    }

    @Test
    void sizeResetsAfterTreeChangesAndDifferentRoot() {
        DirectoryNode root = new DirectoryNode("root");
        root.addChild(new FileNode("first.txt", 12));
        TotalSizeVisitor visitor = new TotalSizeVisitor();
        visitor.scan(root);
        visitor.scan(root);
        assertEquals(12, visitor.getTotalSize());
        root.addChild(new FileNode("second.txt", 8));
        visitor.scan(root);
        assertEquals(20, visitor.getTotalSize());
        visitor.scan(new DirectoryNode("other"));
        assertEquals(0, visitor.getTotalSize());
    }

    @Test
    void sizeOverflowIsRejectedAndNextScanCanRecover() {
        DirectoryNode root = new DirectoryNode("huge");
        root.addChild(new FileNode("first.bin", Long.MAX_VALUE));
        root.addChild(new FileNode("second.bin", 1));
        TotalSizeVisitor visitor = new TotalSizeVisitor();
        assertThrows(ArithmeticException.class, () -> visitor.scan(root));
        visitor.scan(new FileNode("small.bin", 7));
        assertEquals(7, visitor.getTotalSize());
    }
}

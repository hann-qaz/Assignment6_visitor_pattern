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

    @Test
    void statisticsIncludeRootAndDeepEmptyDirectories() {
        DirectoryNode root = new DirectoryNode("root");
        DirectoryNode folder = new DirectoryNode("folder");
        DirectoryNode deep = new DirectoryNode("deep");
        DirectoryNode empty = new DirectoryNode("empty");
        root.addChild(folder);
        root.addChild(new FileNode("a.txt", 2));
        folder.addChild(deep);
        deep.addChild(empty);
        folder.addChild(new FileNode("b.txt", 4));
        StatisticsVisitor visitor = new StatisticsVisitor();
        visitor.scan(root);
        assertEquals(2, visitor.getFileCount());
        assertEquals(4, visitor.getDirectoryCount());
        assertEquals(3, visitor.getMaximumDepth());
    }

    @Test
    void statisticsForEmptyRootAndFileRootStartAtDepthZero() {
        StatisticsVisitor visitor = new StatisticsVisitor();
        visitor.scan(new DirectoryNode("empty"));
        assertEquals(0, visitor.getFileCount());
        assertEquals(1, visitor.getDirectoryCount());
        assertEquals(0, visitor.getMaximumDepth());
        visitor.scan(new FileNode("single.txt", 5));
        assertEquals(1, visitor.getFileCount());
        assertEquals(0, visitor.getDirectoryCount());
        assertEquals(0, visitor.getMaximumDepth());
    }

    @Test
    void statisticsResetAndUseTheSelectedRootDepth() {
        DirectoryNode root = new DirectoryNode("outside");
        DirectoryNode folder = new DirectoryNode("folder");
        root.addChild(folder);
        folder.addChild(new FileNode("inside.txt", 6));
        StatisticsVisitor visitor = new StatisticsVisitor();
        visitor.scan(root);
        assertEquals(2, visitor.getMaximumDepth());
        visitor.scan(folder);
        visitor.scan(folder);
        assertEquals(1, visitor.getFileCount());
        assertEquals(1, visitor.getDirectoryCount());
        assertEquals(1, visitor.getMaximumDepth());
        folder.addChild(new FileNode("new.txt", 7));
        visitor.scan(folder);
        assertEquals(2, visitor.getFileCount());
    }

    @Test
    void operationOrderDoesNotAffectResults() {
        DirectoryNode root = Main.createDemoTree();
        TotalSizeVisitor size = new TotalSizeVisitor();
        SearchVisitor search = new SearchVisitor(name -> name.endsWith(".txt"), 120);
        StatisticsVisitor statistics = new StatisticsVisitor();
        statistics.scan(root);
        search.scan(root);
        size.scan(root);
        assertEquals(3368, size.getTotalSize());
        assertEquals(java.util.List.of("documents/notes.txt", "notes.txt"), search.getMatches());
        assertEquals(5, statistics.getFileCount());
        assertEquals(5, statistics.getDirectoryCount());
        assertEquals(3, statistics.getMaximumDepth());
        size.scan(root);
        statistics.scan(root);
        search.scan(root);
        assertEquals(3368, size.getTotalSize());
        assertEquals(5, statistics.getFileCount());
        assertEquals(5, statistics.getDirectoryCount());
        assertEquals(3, statistics.getMaximumDepth());
        assertEquals(java.util.List.of("documents/notes.txt", "notes.txt"), search.getMatches());
    }

    @Test
    void nullRootsAreRejected() {
        assertThrows(NullPointerException.class, () -> new TotalSizeVisitor().scan(null));
        assertThrows(NullPointerException.class, () -> new StatisticsVisitor().scan(null));
        SearchVisitor search = new SearchVisitor(name -> true, 0);
        assertThrows(NullPointerException.class, () -> search.scan(null));
    }
}

package kz.edu.aitu.visitor;

import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SearchVisitorTest {
    @Test
    void searchCombinesNameAndInclusiveSizeConditions() {
        DirectoryNode root = new DirectoryNode("root");
        root.addChild(new FileNode("short.txt", 39));
        root.addChild(new FileNode("edge.txt", 40));
        root.addChild(new FileNode("large.txt", 90));
        root.addChild(new FileNode("large.pdf", 200));
        SearchVisitor visitor = new SearchVisitor(name -> name.endsWith(".txt"), 40);
        visitor.scan(root);
        assertEquals(List.of("edge.txt", "large.txt"), visitor.getMatches());
    }

    @Test
    void pathsAreSortedAndRelativeToChosenSubtree() {
        DirectoryNode root = new DirectoryNode("outside");
        DirectoryNode chosen = new DirectoryNode("chosen");
        DirectoryNode nested = new DirectoryNode("a");
        root.addChild(chosen);
        root.addChild(new FileNode("outside.txt", 999));
        chosen.addChild(new FileNode("z.txt", 70));
        chosen.addChild(nested);
        nested.addChild(new FileNode("b.txt", 80));
        chosen.addChild(new FileNode("a.txt", 90));
        SearchVisitor visitor = new SearchVisitor(name -> name.endsWith(".txt"), 0);
        visitor.scan(chosen);
        assertEquals(List.of("a.txt", "a/b.txt", "z.txt"), visitor.getMatches());
        visitor.scan(chosen);
        assertEquals(List.of("a.txt", "a/b.txt", "z.txt"), visitor.getMatches());
    }

    @Test
    void fileRootUsesItsNameAndResultsAreSnapshots() {
        SearchVisitor visitor = new SearchVisitor(name -> name.startsWith("quiz"), 0);
        visitor.scan(new FileNode("quiz.txt", 0));
        List<String> result = visitor.getMatches();
        assertEquals(List.of("quiz.txt"), result);
        assertThrows(UnsupportedOperationException.class, () -> result.add("wrong"));
        visitor.scan(new DirectoryNode("empty"));
        assertTrue(visitor.getMatches().isEmpty());
        assertEquals(List.of("quiz.txt"), result);
    }

    @Test
    void emptyDirectoriesAndNoMatchesAreHandled() {
        DirectoryNode root = new DirectoryNode("notes.txt");
        root.addChild(new DirectoryNode("empty.txt"));
        root.addChild(new FileNode("notes.pdf", 100));
        SearchVisitor visitor = new SearchVisitor(name -> name.endsWith(".txt"), 0);
        visitor.scan(root);
        assertTrue(visitor.getMatches().isEmpty());
    }

    @Test
    void repeatedSearchReflectsNewFilesWithoutLeakingOldPaths() {
        DirectoryNode root = new DirectoryNode("root");
        root.addChild(new FileNode("first.txt", 5));
        SearchVisitor visitor = new SearchVisitor(name -> true, 0);
        visitor.scan(root);
        root.addChild(new FileNode("second.txt", 6));
        visitor.scan(root);
        assertEquals(List.of("first.txt", "second.txt"), visitor.getMatches());
        visitor.scan(new FileNode("other.txt", 1));
        assertEquals(List.of("other.txt"), visitor.getMatches());
    }

    @Test
    void invalidSearchConfigurationIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new SearchVisitor(name -> true, -1));
        assertThrows(NullPointerException.class, () -> new SearchVisitor(null, 0));
    }

    @Test
    void searchCanRunAgainAfterCriterionThrows() {
        DirectoryNode root = new DirectoryNode("root");
        DirectoryNode folder = new DirectoryNode("folder");
        root.addChild(folder);
        folder.addChild(new FileNode("bad.txt", 1));
        SearchVisitor visitor = new SearchVisitor(name -> {
            if (name.equals("bad.txt")) { throw new IllegalArgumentException("Bad input"); }
            return true;
        }, 0);
        assertThrows(IllegalArgumentException.class, () -> visitor.scan(root));
        visitor.scan(new FileNode("good.txt", 2));
        assertEquals(List.of("good.txt"), visitor.getMatches());
    }
}

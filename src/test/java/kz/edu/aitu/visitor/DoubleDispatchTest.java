package kz.edu.aitu.visitor;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DoubleDispatchTest {
    private static class RecordingVisitor implements Visitor {
        FileNode file;
        DirectoryNode directory;

        public void visit(FileNode file) { this.file = file; }
        public void visit(DirectoryNode directory) { this.directory = directory; }
    }

    @Test
    void fileCallsFileOverload() {
        FileNode file = new FileNode("essay.txt", 19);
        Element element = file;
        RecordingVisitor visitor = new RecordingVisitor();
        element.accept(visitor);
        assertSame(file, visitor.file);
        assertNull(visitor.directory);
    }

    @Test
    void directoryCallsDirectoryOverloadWithoutWalking() {
        DirectoryNode directory = new DirectoryNode("course");
        directory.addChild(new FileNode("essay.txt", 19));
        Element element = directory;
        RecordingVisitor visitor = new RecordingVisitor();
        element.accept(visitor);
        assertSame(directory, visitor.directory);
        assertNull(visitor.file);
    }
}

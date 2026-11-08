package kz.edu.aitu.visitor;

import java.util.Objects;

public final class StatisticsVisitor implements Visitor {
    private int fileCount;
    private int directoryCount;
    private int maximumDepth;
    private int currentDepth;

    public void scan(Element root) {
        fileCount = 0;
        directoryCount = 0;
        maximumDepth = 0;
        currentDepth = 0;
        Objects.requireNonNull(root, "Root cannot be null").accept(this);
    }

    @Override
    public void visit(FileNode file) {
        fileCount++;
        maximumDepth = Math.max(maximumDepth, currentDepth);
    }

    @Override
    public void visit(DirectoryNode directory) {
        directoryCount++;
        maximumDepth = Math.max(maximumDepth, currentDepth);
        currentDepth++;
        try {
            for (Node child : directory.getChildren()) {
                child.accept(this);
            }
        } finally {
            currentDepth--;
        }
    }

    public int getFileCount() {
        return fileCount;
    }

    public int getDirectoryCount() {
        return directoryCount;
    }

    public int getMaximumDepth() {
        return maximumDepth;
    }
}

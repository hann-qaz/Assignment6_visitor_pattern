package kz.edu.aitu.visitor;

import java.util.Objects;

public final class TotalSizeVisitor implements Visitor {
    private long totalSize;

    public void scan(Element root) {
        totalSize = 0;
        Objects.requireNonNull(root, "Root cannot be null").accept(this);
    }

    @Override
    public void visit(FileNode file) {
        totalSize = Math.addExact(totalSize, file.getSize());
    }

    @Override
    public void visit(DirectoryNode directory) {
        for (Node child : directory.getChildren()) {
            child.accept(this);
        }
    }

    public long getTotalSize() {
        return totalSize;
    }
}

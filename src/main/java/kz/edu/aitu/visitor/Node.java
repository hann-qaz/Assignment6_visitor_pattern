package kz.edu.aitu.visitor;

public abstract class Node implements Element {
    private final String name;
    private DirectoryNode parent;

    protected Node(String name) {
        if (name == null || name.isBlank() || name.contains("/")
                || name.contains("\\") || name.equals(".") || name.equals("..")) {
            throw new IllegalArgumentException("Use a non-empty name without path separators");
        }
        this.name = name;
    }

    public final String getName() {
        return name;
    }

    public final DirectoryNode getParent() {
        return parent;
    }

    // Only DirectoryNode changes ownership after checking the tree.
    final void attachTo(DirectoryNode parent) {
        this.parent = parent;
    }
}

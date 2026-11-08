package kz.edu.aitu.visitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public final class SearchVisitor implements Visitor {
    private final Predicate<String> filenameCriterion;
    private final long minimumSize;
    private final List<String> matches = new ArrayList<>();
    private String currentPath = "";

    public SearchVisitor(Predicate<String> filenameCriterion, long minimumSize) {
        this.filenameCriterion = Objects.requireNonNull(filenameCriterion, "Filename criterion is required");
        if (minimumSize < 0) {
            throw new IllegalArgumentException("Minimum size cannot be negative");
        }
        this.minimumSize = minimumSize;
    }

    public void scan(Element root) {
        matches.clear();
        currentPath = "";
        Objects.requireNonNull(root, "Root cannot be null").accept(this);
    }

    @Override
    public void visit(FileNode file) {
        if (filenameCriterion.test(file.getName()) && file.getSize() >= minimumSize) {
            matches.add(currentPath.isEmpty() ? file.getName() : currentPath);
        }
    }

    @Override
    public void visit(DirectoryNode directory) {
        String directoryPath = currentPath;
        try {
            for (Node child : directory.getChildren()) {
                currentPath = directoryPath.isEmpty()
                        ? child.getName() : directoryPath + "/" + child.getName();
                child.accept(this);
            }
        } finally {
            currentPath = directoryPath;
        }
    }

    public List<String> getMatches() {
        List<String> sorted = new ArrayList<>(matches);
        Collections.sort(sorted);
        return List.copyOf(sorted);
    }
}

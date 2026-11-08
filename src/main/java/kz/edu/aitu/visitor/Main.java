package kz.edu.aitu.visitor;

public class Main {
    public static void main(String[] args) {
        if (args.length > 2) {
            throw new IllegalArgumentException("Usage: Main [filename substring] [minimum bytes]");
        }
        String namePart = args.length > 0 ? args[0] : ".txt";
        long minimumSize = args.length > 1 ? Long.parseLong(args[1]) : 120;
        DirectoryNode root = createDemoTree();
        TotalSizeVisitor size = new TotalSizeVisitor();
        SearchVisitor search = new SearchVisitor(name -> name.contains(namePart), minimumSize);
        StatisticsVisitor statistics = new StatisticsVisitor();
        size.scan(root);
        search.scan(root);
        statistics.scan(root);

        System.out.println("Root: " + root.getName());
        System.out.println("Total size: " + size.getTotalSize() + " bytes");
        System.out.println("Search: name contains '" + namePart + "', size >= " + minimumSize);
        for (String path : search.getMatches()) {
            System.out.println("  " + path);
        }
        if (search.getMatches().isEmpty()) {
            System.out.println("  No matches");
        }
        System.out.println("Files: " + statistics.getFileCount());
        System.out.println("Directories: " + statistics.getDirectoryCount());
        System.out.println("Maximum depth: " + statistics.getMaximumDepth());
    }

    public static DirectoryNode createDemoTree() {
        DirectoryNode root = new DirectoryNode("workspace");
        DirectoryNode documents = new DirectoryNode("documents");
        DirectoryNode archive = new DirectoryNode("archive");
        DirectoryNode images = new DirectoryNode("images");
        root.addChild(new FileNode("notes.txt", 120));
        root.addChild(documents);
        documents.addChild(new FileNode("notes.txt", 340));
        documents.addChild(new FileNode("plan.pdf", 800));
        documents.addChild(archive);
        archive.addChild(new FileNode("old.txt", 60));
        root.addChild(images);
        images.addChild(new FileNode("logo.png", 2048));
        root.addChild(new DirectoryNode("empty"));
        return root;
    }
}

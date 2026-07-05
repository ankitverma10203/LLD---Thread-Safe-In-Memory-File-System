import model.FileSystem;
import model.Directory;

/**
 * Small example demonstrating the in-memory file system usage.
 */
public class App {
    public static void main(String[] args) {
        FileSystem fs = new FileSystem();
        Directory root = fs.getRoot();

        System.out.println("Root name: " + root.getName());

        System.out.println("Adding directory 'docs' and file 'todo.txt' to root...");
        root.addDirectory("docs");
        root.addFile("todo.txt");
                // Show current children of root
                System.out.println("Root children: " + root.listChildren());

                // Add a nested file inside 'docs'
                Directory docs = root.getDirectory("docs");
                if (docs != null) {
                    docs.addFile("notes.md");
                    System.out.println("Docs children: " + docs.listChildren());
                }

                System.out.println("Removing 'todo.txt' -> " + root.removeChild("todo.txt"));
                System.out.println("Removing 'docs' -> " + root.removeChild("docs"));

                System.out.println("Done.");
    }
}

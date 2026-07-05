import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import model.Directory;
import model.FileSystem;

public class FileSystemTest {

  @Test
  public void basicOperationsShouldWork() {
    FileSystem fs = new FileSystem();
    Directory root = fs.getRoot();

    // add directory and file
    root.addDirectory("docs");
    root.addFile("todo.txt");

    assertTrue(root.listChildren().contains("docs"));
    assertTrue(root.listChildren().contains("todo.txt"));

    Directory docs = root.getDirectory("docs");
    assertNotNull(docs);

    docs.addFile("notes.md");
    assertTrue(docs.listChildren().contains("notes.md"));

    // delete operations
    assertTrue(root.removeChild("todo.txt"));
    assertTrue(root.removeChild("docs"));
  }
}


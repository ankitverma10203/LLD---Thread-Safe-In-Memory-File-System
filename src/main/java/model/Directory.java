package model;

import java.util.HashMap;
import java.util.Map;

class Directory extends FileSystemElement {
  private Map<String, FileSystemElement> children;

  public Directory(String name, Directory parent) {
    super(name, parent);
    this.children = new HashMap<>(); 
  }

  public void addFile(String fileName) {
      if (children.containsKey(fileName)) return;

      children.put(fileName, new File(fileName, this));
  }

  public void addDirectory(String directoryName) {
      if (children.containsKey(directoryName)) return;
      
      children.put(directoryName, new Directory(directoryName, this));
  }
}

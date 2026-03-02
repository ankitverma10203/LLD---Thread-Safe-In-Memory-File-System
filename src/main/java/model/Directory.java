package model;

class Directory extends FileSystemElement {
  private Map<String, FileSystemElement> children;

  public Directory(String name, Directory parent) {
    super(name, parent);
    this.children = new HashMap<>(); 
  }

  public void addFile(String fileName) {
      if (children.containsKey(fileName)) return;

      children.add(new File(fileName, this));
  }

  public void addDirectory(String directoryName) {
      if (children.containsKey(directoryName)) return;
      
      children.add(new Directory(directoryName, this));
  }
}

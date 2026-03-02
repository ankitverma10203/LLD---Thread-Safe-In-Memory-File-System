package model;

class FileSystemElement {
  private String name;
  private FileSystemElement parent;

  public FileSystemElement(String name, FileSystemElement parent) {
    this.name = name;
    this.parent = parent;
  }

  public String getName() {
    return name;
  }

}

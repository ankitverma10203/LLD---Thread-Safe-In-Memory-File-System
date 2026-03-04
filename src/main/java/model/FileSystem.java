package model;

class FileSystem {
  private final Directory root;

  public FileSystem() {
    root = new Directory("root", null);
  }

  public Directory getRoot() {
    return root;
  }
}

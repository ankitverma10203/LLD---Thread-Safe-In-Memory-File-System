package model;

import java.util.concurrent.locks.ReentrantReadWriteLock;

abstract class FileSystemElement {
  protected String name;
  protected Directory parent;
  protected final ReentrantReadWriteLock lock;

  public FileSystemElement(String name, Directory parent) {
    this.name = name;
    this.parent = parent;
    this.lock = new ReentrantReadWriteLock();
  }

  public String getName() {
    return name;
  }

  public abstract boolean delete(boolean forced);

}

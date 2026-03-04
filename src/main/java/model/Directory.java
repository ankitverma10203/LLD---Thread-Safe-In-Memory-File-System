package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class Directory extends FileSystemElement {
  private Map<String, FileSystemElement> children;

  public Directory(String name, Directory parent) {
    super(name, parent);
    this.children = new ConcurrentHashMap<>();
  }

  public void addFile(String fileName) {
    this.lock.writeLock().lock();

    try {
      children.putIfAbsent(fileName, new File(fileName, this));
    } finally {
      this.lock.writeLock().lock();
    }
  }

  public void addDirectory(String directoryName) {
    this.lock.writeLock().lock();

    try {
      children.putIfAbsent(directoryName, new Directory(directoryName, this));
    } finally {
      this.lock.writeLock().unlock();
    }
  }

  public boolean removeChild(String childName) {
    this.lock.writeLock().lock();
    try {

      return children.remove(childName) != null;
    } finally {
      this.lock.writeLock().unlock();
    }
  }

  public boolean delete(boolean forced) {

    List<FileSystemElement> snapshot;

    lock.writeLock().lock();
    try {
      if (parent == null)
        return false;

      if (!forced && !children.isEmpty()) {
        System.out.println(name + " is not empty");
        return false;
      }

      snapshot = new ArrayList<>(children.values());
      children.clear();

    } finally {
      lock.writeLock().unlock();
    }

    if (forced) {
      for (FileSystemElement child : snapshot) {
        child.delete(true);
      }
    }

    return parent.removeChild(this.name);

  }
}

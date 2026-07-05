package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Directory extends FileSystemElement {
  private final Map<String, FileSystemElement> children;

  public Directory(String name, Directory parent) {
    super(name, parent);
    this.children = new ConcurrentHashMap<>();
  }

  public void addFile(String fileName) {
    this.lock.writeLock().lock();

    try {
      children.putIfAbsent(fileName, new File(fileName, this));
    } finally {
      this.lock.writeLock().unlock();
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

  public java.util.List<String> listChildren() {
    this.lock.readLock().lock();
    try {
      return new ArrayList<>(children.keySet());
    } finally {
      this.lock.readLock().unlock();
    }
  }

  public Directory getDirectory(String name) {
    this.lock.readLock().lock();
    try {
      FileSystemElement e = children.get(name);
      if (e instanceof Directory) {
        return (Directory) e;
      }
      return null;
    } finally {
      this.lock.readLock().unlock();
    }
  }

  public String treeString() {
    StringBuilder sb = new StringBuilder();
    buildTreeString(sb, "");
    return sb.toString();
  }

  private void buildTreeString(StringBuilder sb, String indent) {
    this.lock.readLock().lock();
    try {
      sb.append(indent).append(name).append("/\n");
      for (FileSystemElement child : children.values()) {
        if (child instanceof Directory) {
          ((Directory) child).buildTreeString(sb, indent + "  ");
        } else {
          sb.append(indent).append("  ").append(child.getName()).append("\n");
        }
      }
    } finally {
      this.lock.readLock().unlock();
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

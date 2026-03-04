package model;

class File extends FileSystemElement {

  public File(String name, Directory parent) {
    super(name, parent);
  }

  @Override
  public boolean delete(boolean forced) {
    this.lock.writeLock().lock();

    try {
      return parent.removeChild(this.name);
    } finally {
      this.lock.writeLock().unlock();
    }
  }
}

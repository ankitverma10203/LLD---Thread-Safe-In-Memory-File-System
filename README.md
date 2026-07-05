# In-Memory Thread-Safe File System

Quickstart
---------

This repository contains a small thread-safe in-memory filesystem implemented in Java. A simple example application and unit tests were added to demonstrate and validate the API.

To compile and run the example (requires a JDK):

```sh
mkdir -p out
javac -d out src/main/java/model/*.java src/main/java/*.java
java -cp out App
```

To run the unit tests with Maven (recommended):

```sh
mvn test
```

If Maven or the JDK are not installed on your machine you can run the example from an IDE (IntelliJ/VSCode) as well.

Overview
--------

This project implements a simplified **in-memory file system** in Java that supports:

- Directories
- Files
- Recursive deletion
- Forced deletion
- Thread-safe operations

The design models a hierarchical filesystem similar to Unix where directories can contain files or other directories.

The primary goal of this project is to demonstrate:

- Object-oriented design
- Concurrency control
- Safe recursive operations
- Handling of shared mutable state

---

# Architecture

The filesystem is modeled using three main classes.

```
FileSystem
   |
   └── Directory (root)
           |
           ├── Directory
           │      └── File
           │
           └── File
```

### Core Components

#### FileSystem

Acts as the entry point of the system.

Responsibilities:

- Initializes the root directory
- Provides access to the root

```
FileSystem
 └── Directory root
```

---

#### FileSystemElement

Abstract base class representing a filesystem node.

Common properties shared by files and directories:

- name
- parent directory
- concurrency lock

Responsibilities:

- Store metadata
- Provide a delete interface
- Provide synchronization primitive

---

#### Directory

Represents a directory node.

Responsibilities:

- Maintain child elements
- Add files
- Add directories
- Delete directories
- Manage recursive deletion

Children are stored in a map:

```
Map<String, FileSystemElement> children
```

---

#### File

Represents a leaf node in the filesystem.

Responsibilities:

- Maintain metadata
- Support deletion

Files do not contain children.

---

# Concurrency Design

This filesystem supports **concurrent operations** across multiple threads.

The design uses:

```
ReentrantReadWriteLock
```

Each filesystem node maintains its own lock.

### Why Node-Level Locks?

Locking per node allows:

- fine-grained concurrency
- minimal blocking between unrelated directories
- better scalability

Example:

```
Thread 1 → modifies /A/B
Thread 2 → modifies /X/Y
```

These operations do not block each other.

---

# Delete Operation

Deletion supports two modes:

### Non-Forced Delete

```
delete(false)
```

Rules:

- Directory must be empty
- Otherwise deletion fails

Example:

```
/A
 └── file1

delete(A, false) → fails
```

---

### Forced Delete

```
delete(true)
```

Rules:

- Recursively deletes all children
- Deletes the directory itself

Example:

```
/A
 ├── B
 │   └── file1
 └── file2
```

Forced delete will remove:

```
file1
B
file2
A
```

---

# Thread Safety

The system ensures thread safety through **explicit locking**.

Example:

```
lock.writeLock().lock()
try {
    modify structure
} finally {
    lock.writeLock().unlock()
}
```

This protects shared mutable state such as:

```
children map
```

---

# Avoiding Deadlocks

Recursive deletion introduces a risk of **deadlocks**.

Example scenario:

```
Thread 1: delete(A)
Thread 2: delete(B)
```

If:

```
A → child B
```

Then the following lock order could occur:

```
Thread 1: lock(A) → waiting for lock(B)
Thread 2: lock(B) → waiting for lock(A)
```

This produces a **circular wait condition**.

To avoid this, the implementation uses the **copy-then-operate pattern**:

1. Acquire directory lock
2. Copy children references to a snapshot
3. Clear the directory children
4. Release the lock
5. Recursively delete the snapshot elements

This ensures:

- Parent locks are not held during recursive deletes
- Lock ordering remains consistent
- Circular waits cannot occur

---

# Snapshot Strategy

Children are copied before deletion:

```
List<FileSystemElement> snapshot =
    new ArrayList<>(children.values())
```

Important note:

The snapshot copies **references**, not objects.

This allows:

- safe iteration
- structural modification of the original map
- recursive deletion without concurrent modification errors

---

# Tradeoffs

## 1. Memory Overhead

Snapshots require temporary memory allocation.

However the cost is acceptable because:

- snapshots are short-lived
- only references are copied

---

## 2. Eventual Consistency During Delete

While a forced delete is running:

- children may already be removed from the parent
- but their own delete operations may still be executing

This creates a **temporary intermediate state**.

The system prioritizes **deadlock avoidance over strict atomic deletion**.

---

## 3. Lock Granularity

Using node-level locks improves concurrency but introduces complexity.

Advantages:

- better scalability
- less blocking

Disadvantages:

- harder reasoning about lock ordering
- potential deadlock risk if not designed carefully

---

## 4. HashMap vs ConcurrentHashMap

The design uses explicit locking around a standard map.

Reasons:

- operations already protected by write locks
- simpler logic
- avoids unnecessary overhead

Using `ConcurrentHashMap` would also work but is not strictly required.

---

# Assumptions

The current implementation assumes:

- no symbolic links
- no file metadata beyond name
- no file content
- no permission model
- single process memory

The filesystem is intended for **demonstration and learning purposes**.

---

# Possible Improvements

Several enhancements could be implemented.

### Path Based API

Current operations act directly on objects.

A production system would support:

```
mkdir("/a/b/c")
touch("/a/b/file")
delete("/a/b", true)
```

---

### Read Operations

Add operations such as:

```
list(directory)
find(path)
exists(path)
```

---

### Lock Ordering Strategy

Introduce a strict lock ordering rule (e.g., root → leaf) to formally eliminate deadlocks.

---

### File Content Support

Files currently only store metadata.

A future version could support:

```
read(file)
write(file)
append(file)
```

---

### Persistence

Currently the filesystem exists only in memory.

Future improvement:

```
snapshot to disk
log-structured persistence
```

---

# Complexity

| Operation        | Complexity       |
| ---------------- | ---------------- |
| Add File         | O(1)             |
| Add Directory    | O(1)             |
| Remove Child     | O(1)             |
| Delete Directory | O(n) (recursive) |

Where **n** is the number of descendants.

---

# Summary

This project demonstrates a simplified **thread-safe in-memory filesystem** with:

- hierarchical structure
- recursive deletion
- concurrency control
- deadlock avoidance

The design emphasizes:

- clarity
- correctness
- safe concurrent mutation

while highlighting tradeoffs commonly encountered in real distributed systems and storage engines.

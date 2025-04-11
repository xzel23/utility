# Upgrading to version 18

## The minimum Java version is now 21

Functionality that has become obsolete with Java 21 has been removed.

### NetUtil

The `NetUtil` utility class has been removed. It was originally introduced to facilitate reading text content directly from an URL. The JDK has offered similar functionality for several versions.

### ThreadFactoryBuilder

The `ThreadFactoryBuilder` class provided a builder pattern for creating customized `ThreadFactory` instances. In Java 21, the new `Thread.Builder` API provides similar functionality with more features:

```java
// Old way using ThreadFactoryBuilder
ThreadFactory factory = ThreadFactoryBuilder.builder()
    .prefix("worker-")
    .daemon(true)
    .priority(Thread.NORM_PRIORITY)
    .build();
Thread thread = factory.newThread(runnable);

// New way using Thread.Builder in Java 21
Thread thread = Thread.ofPlatform()
    .name("worker-", 0)
    .daemon(true)
    .priority(Thread.NORM_PRIORITY)
    .factory()
    .newThread(runnable);
```

Additionally, Java 21 introduces virtual threads which are lightweight threads that can significantly improve application throughput when dealing with blocking operations:

```java
// Creating a virtual thread in Java 21
Thread vThread = Thread.ofVirtual()
    .name("virtual-worker-", 0)
    .factory()
    .newThread(runnable);
```

### MathUtil

`MathUtil.clamp(...)` has been removed with all of its overloads. The method has become obsolete with the introduction of `Math.clamp(...)` in the JDK itself.

Note that the argument order differs: `MathUtil.clamp(min, max, arg)` vs `Math.clamp(arg, min, max)`.

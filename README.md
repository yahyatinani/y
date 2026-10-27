# y

A Kotlin Multiplatform library for persistent, structurally shared collections, chunked lazy sequences, and functional utilities.

<!-- [![Build](https://github.com/yahyatinani/y/actions/workflows/main.yml/badge.svg)](https://github.com/yahyatinani/y/actions/workflows/main.yml) -->
[![Maven Central Version](https://img.shields.io/maven-central/v/io.github.yahyatinani.y/y-core)](https://central.sonatype.com/artifact/io.github.yahyatinani.y/y-core)
[![License](https://img.shields.io/badge/License-EPL%201.0-blue.svg)](https://opensource.org/licenses/EPL-1.0)
[![Kotlin](https://img.shields.io/badge/Kotlin-Multiplatform-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![codecov](https://codecov.io/gh/yahyatinani/y/branch/main/graph/badge.svg?token=O7NV8M4TTP)](https://codecov.io/gh/yahyatinani/y)

**Status:** pre-1.0. The API may change before 1.0. Actively developed.

`y` aims to bring the ergonomics and power of persistent data structures and functional programming concepts to Kotlin across multiple targets. It is inspired by approaches from languages and libraries that emphasize immutable, efficient, and composable data structures.

## Why use y?

- Persistent collections with structural sharing
- Efficient immutable list, vector, map, and set implementations
- Lazy, chunked sequences for working with large data without eager materialization
- Functional combinators and collection-oriented APIs
- Kotlin Multiplatform support for shared logic across targets
- Designed for clarity, safety, and predictable performance

## Installation

```kotlin
dependencies {
  implementation("io.github.yahyatinani.y:y-core:<version>")
  // optional
  implementation("io.github.yahyatinani.y:y-concurrency:<version>")
}
``` 

For snapshots, add the Central Portal snapshot repository:

```kotlin
repositories {
    maven {
        name = "CentralPortalSnapshots"
        url = uri("https://central.sonatype.com/repository/maven-snapshots/")
        content { includeGroup("io.github.yahyatinani.y") }
    }
}
```

## Targets

JVM and the full Kotlin/Native target range:

| Platform | Targets |
|---|---|
| **JVM** | `jvm` (with Java interop) |
| **Linux** | `linuxX64` |
| **Windows** | `mingwX64` |
| **macOS** | `macosX64`, `macosArm64` |
| **iOS** | `iosX64`, `iosArm64`, `iosSimulatorArm64` |
| **tvOS** | `tvosX64`, `tvosArm64`, `tvosSimulatorArm64` |
| **watchOS** | `watchosX64`, `watchosArm32`, `watchosArm64`, `watchosSimulatorArm64` |

Android does not yet have a first-class `androidTarget`; Android projects can consume the JVM variant.

## Modules

- **`y-core`** — persistent collection implementations, sequence utilities, and
  foundational functional APIs.
- **`y-concurrency`** — lock-free atomic references (CAS retry loops via
  AtomicFu) and concurrency primitives built on the core collections.

## Project structure

```text
y/
├── y-core/           # Core persistent collections and functional APIs
├── y-concurrency/    # Concurrency primitives built on the core
├── build.gradle      # Root Gradle configuration
├── settings.gradle   # Included modules
```

## Quick start

Create persistent collections with the same style as normal Kotlin
construction:

```kotlin
import io.github.yahyatinani.y.core.*

val list1 = l<Int>(1, 2, 3)             // (1 2 3)
val emptyList = l<Int>()                // ()

val list2 = listOf(1, 2, 3).toPlist()   // (1 2 3)

val updated = list1.conj(4)             // (4 1 2 3)
val size = list1.count                  // 3

val vec1 = v<Int>(1, 2, 3)              // [1 2 3]
val vec2 = listOf(1, 2, 3).toPvector()  // [1 2 3]

val vectorUpdated = vec1.conj(4)        // [1 2 3 4]
val vectorItem = vec1[1]                // 2

val map1 = m<String, Int>("1" to 1, "2" to 2)  // {"1" 1, "2" 2}
val set1 = hs<Int>(1, 2, 2, 3, 3)              // #{1 2 3}
```

Converting from Kotlin collections:

```kotlin
val map = mapOf("1" to 1, "2" to 2).toPArrayMap()
val hashMap = mapOf("1" to 1, "2" to 2).toPhashMap()
val hashSet = setOf(1, 2, 2, 3, 3).toPhashSet()
```

### Lazy sequences

`y` exposes lazy sequences through `LazySeq<E>`, which implements `ISeq<E>`,
`List<E>`, and `Sequence<E>`. A `LazySeq` defers evaluation until it is forced
by calling `first()`, `rest()`, `next()`, `seq()`, or iterating it.

The most direct way to build one is `cons`, which pairs a head element with a
tail that isn't evaluated until requested:

```kotlin
val numbers = lazySeq<Int> {
  cons(1, lazySeq {
    cons(2, lazySeq {
      cons(3, null)
    })
  })
}

numbers.first()                // 1
numbers.rest().first()         // 2
numbers.rest().rest().first()  // 3
```

Only the node you ask for is realized. `numbers.rest().rest()` doesn't build
the third `cons` until you call `first()` on it.

You can also wrap an ordinary Kotlin collection or array:

```kotlin
import io.github.yahyatinani.y.core.seq

val xs = seq(listOf(1, 2, 3))

xs?.first()                   // 1
xs?.rest()?.first()           // 2
xs?.rest()?.rest()?.first()   // 3
```

The core interface is small:

```kotlin
interface ISeq<out E> : IPersistentCollection<E> {
  fun first(): E
  fun rest(): ISeq<E>
  fun next(): ISeq<E>?
  fun cons(e: E): ISeq<E>
}
```

- `first()` returns the current element.
- `rest()` returns the tail, which may be empty but is never null.
- `next()` returns `null` when no elements remain.
- `cons(e)` prepends an element, returning a new sequence.

Sequences are immutable, persistent, and lazy. That makes them a natural fit
for streams, generator-style iteration, and pipelines where you want to stop
before the whole structure is materialized.

## Complexity

| Operation | `PList` | `PVector` | `PMap` | `PSet` |
|---|---|---|---|---|
| Access by index | O(n) | O(log₃₂ n) | — | — |
| Lookup by key | — | — | O(log₃₂ n) | O(log₃₂ n) |
| `conj` (append) | O(1) | O(log₃₂ n) | O(log₃₂ n) | O(log₃₂ n) |
| `assoc` (update) | — | O(log₃₂ n) | O(log₃₂ n) | — |
| Iteration | O(n) | O(n) | O(n) | O(n) |

## Building

This project uses Gradle.

```bash
./gradlew test
```

To run the full verification tasks:

```bash
./gradlew check
```

## License

This project is licensed under the Eclipse Public License 1.0. See the [LICENSE.md](LICENSE.md) file for details.

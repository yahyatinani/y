Changelog
========
## v0.0.2.1

#### Fixed
* Calling toString() on a Keyword in Native caused a InvalidMutabilityException

## v0.0.2

#### Added:

- **y-collections** module:

  - <u>Data structures:</u>

    - PersistentList
    - PersistentVector
    - PersistentArrayMap
    - PersistentHashMap
    - PersistentHashSet
    - Seq
    - LazySeq
    - ArrayChunk
    - Keyword

  - <u>Functions :</u>

    - ```kotlin
      List<E>.toPlist() : PersistentList<E>
      ```

    - ```kotlin
      l(vararg elements: E) : PersistentList<E>
      ```

    - ```kotlin
      Map<K, V>.toPArrayMap() : PersistentArrayMap<K, V>
      ```

    - ```kotlin
      m(vararg pairs: Pair<K, V>) : PersistentArrayMap<K, V>
      ```

    - ```kotlin
      hashMap(vararg pairs: Pair<K, V>) : PersistentHashMap<K, V>
      ```

    - ```kotlin
      Map<K, V>.toPhashMap() : PersistentHashMap<K, V>
      ```

    - ```kotlin
      Set<E>.toPhashSet() : PersistentHashSet<E>
      ```

    - ```kotlin
      hs(vararg e: E) : PersistentHashSet<E>
      ```

    - ```kotlin
      v(vararg elements: E): PersistentVector<E>
      ```

    - ```kotlin
      List<E>.toPvector(): PersistentVector<E>
      ```

    - ```
      lazyChunkedSeq(iterator: Iterator<E>): ISeq<E> 
      ```

    - ```kotlin
      k(name: String): Keyword
      ```

#### Updated:

- The project was Kotlin/JVM only, now it is Kotlin Multiplatform that targets the JVM and Native (no Javascript support)

- **y-common** module became **y-core** module because *y-core* can depend on other modules while *y-common* is a shared module that contains only code that is ,well, common between other modules.

#### Removed:

- **Monads** (*Either, Option and Result*) are removed.

- <u>Functions :</u>

  - ```kotlin
    assertCondition(value: T, msg: String, p: (T) -> Boolean)
    ```

  - ```kotlin
    assertCondition(value: T, p: (T) -> Boolean)
    ```

  - ```kotlin
    assertTrue
    ```

  - ```kotlin
    assertFalse
    ```

  - ```kotlin
    assertNotNull
    ```

  - ```kotlin
    assertPositive
    ```

  - ```kotlin
    assertInRange
    ```

  - ```kotlin
    assertPositiveOrZero
    ```

## v0.0.1

#### Added:

- ##### y-common module (kotlin/JVM):

  - <u>Functions:</u> 

    - ```kotlin
      identity(x: T)
      ```

    - ```kotlin
      inc(x)
      ```

    - ```kotlin
      dec(x)
      ```

    - ```kotlin
      str(x)
      ```

    - ```kotlin
      curry(f)
      ```

    - ```kotlin
      complement(f)
      ```

    - ```kotlin
      compose(f,g)
      ```

    - ```kotlin
      assertCondition(value: T, msg: String, p: (T) -> Boolean)
      ```

    - ```kotlin
      assertCondition(value: T, p: (T) -> Boolean)
      ```

    - ```kotlin
      assertTrue
      ```

    - ```kotlin
      assertFalse
      ```

    - ```kotlin
      assertNotNull
      ```

    - ```kotlin
      assertPositive
      ```

    - ```kotlin
      assertInRange
      ```

    - ```kotlin
      assertPositiveOrZero
      ```

  - <u>Monads:</u>

    - Either
    - Option
    - Result
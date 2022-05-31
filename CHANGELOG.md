Changelog
========

## v0.2.0

#### Added

* `com.github.whyrising.y.core.util.m<K,V>(vararg a:Any?)`

#### Updated

* Moved code in `y-core` from `com.github.whyrising.y` to
  `com.github.whyrising.y.core`.
* Move all collections code to com.github.whyrising.y.core.collections.
* Make `Symbol` and `fun s(name: String): Symbol` public.

## v0.1.1

#### Fixed

* Fix missing Atomicfu dependency in projects that use y library by exposing it
  as api.

#### Updated

* Update to kotlin 1.6.21.

## v0.1.0

#### Added

* ```kotlin
  fun <T, R> map(coll: Any?, f: (T) -> R): LazySeq<R>
  ```
* ```kotlin
  fun <E> Iterable<E>.seq(): ISeq<E>
    
  fun <E> Array<E>.seq(): ISeq<E>
    
  fun <E> Sequence<E>.seq(): ISeq<E>
    
  fun CharSequence.seq(): ISeq<Char>
    
  fun ShortArray.seq(): ISeq<Short>
    
  fun IntArray.seq(): ISeq<Int>
    
  fun FloatArray.seq(): ISeq<Float>
    
  fun DoubleArray.seq(): ISeq<Double>
    
  fun LongArray.seq(): ISeq<Long>
    
  fun ByteArray.seq(): ISeq<Byte>
    
  fun CharArray.seq(): ISeq<Char>
    
  fun BooleanArray.seq(): ISeq<Boolean>
    
  fun <K, V> Map<K, V>.seq(): ISeq<MapEntry<K, V>>
  ```

#### Fixed

* All tests now run on all targeted platforms.

#### Updated

* Update to Kotlin `1.6.20`
* `y-collections` module merged into `y-core` module, resulting in only two
  modules total: `y-core` and `y-concurrency`.
* Reimplement deserializing of the following collections serializers to improve
  performance drastically:
    * PersistentVector
    * PersistentArrayMap
    * PersistentHashMap
    * PersistentHashSet

## v0.0.10

#### Fixed:

* `PersistentQueue.pop()` should return the empty queue when only one element in
  the queue.

## v0.0.9

#### Fixed:

* `PersistentQueue.equals(other)`
* `PersistentQueue.equiv(other)`

## v0.0.8

#### Added:

* `class PersistentQueue<E>`

    * Utility functions to create the queue:

        * ```kotlin
          // returns an empty queue fun <E> q(coll:Any?): PersistentQueue<E>
          fun <E> q(): PersistentQueue<E>
          ```

## v0.0.7

#### Added:

* ```kotlin
  fun <E> first(x: Any?): E?
  ```

* concat function:

    * ```kotlin
      fun <E> concat(): LazySeq<E>
      ```

    * ```kotlin
      fun <E> concat(x: Any?): LazySeq<E> 
      ```

    * ```kotlin
      fun <E> concat(x: Any?, y: Any?): LazySeq<E>
      ```

    * ```kotlin
      fun <E> concat(x: Any?, y: Any?, vararg zs: Any?): LazySeq<E>
      ```

* ```kotlin
  fun <T> conj(
      coll: IPersistentCollection<T>?,
      x: T,
      vararg xs: T
  ): IPersistentCollection<T>
  ```

* ```kotlin
  fun <T> isEvery(pred: (T) -> Boolean, coll: Any?): Boolean
  ```

* ```kotlin
  fun IPersistentVector<E>.component1()
  //...
  fun IPersistentVector<E>.component6()
  ```

* ```kotlin
  fun IPersistentVector<E>.get(index: Int)
  ```

* ```kotlin
  operator fun <K, V> IPersistentMap<K, V>.get(key: K): V?
  ```

* ```kotlin
  fun consChunk()
  ```

* ```kotlin
  fun nextChunks()
  ```

* ```kotlin
  inline fun <E> lazySeq(crossinline body: () -> Any?): LazySeq<E>
  ```

#### Updated:

* `ISeq<E>` interface:
    * Add `fun next(): ISeq<E>?`

## v0.0.6

* #### Performance/Optimization:

    * Reimplement `PersistentArrayMap<K, V>`.

## v0.0.5

#### Added:

* ```kotlin
  fun <K, V> get(map: ILookup<K, V>?, key: K, default: V? = null): V?
  ```

* ```kotlin
  fun <K, V> getFrom(map: Any?, key: K, default: V? = null): V?
  ```

* ```kotlin
  fun <K, V> assoc(map: Associative<K, V>?, kv: Pair<K, V>): Associative<K, V>
  ```

* ```kotlin
  fun <K, V> assoc(
      map: Associative<K, V>?,
      kv: Pair<K, V>,
      vararg kvs: Pair<K, V>
  ):Associative<K, V>
  ```

* ```kotlin
  fun <E> ISeq<E>.component1(): E
  ```

* ```kotlin
  fun <E> ISeq<E>.component2(): ISeq<E>
  ```

* ```kotlin
  fun <K, V> Map<K, V>.toPmap(): IPersistentMap<K, V>
  ```

* ```kotlin
  fun <E> cons(x: E, coll: Any?): ISeq<E>
  ```

* ```kotlin
  fun <E> vec(coll: Iterable<E>): IPersistentVector<E>
  ```

* ArraySeq<E>

#### Updated:

* APIs:
    * `m(pairs)` now returns `IPersistentMap<K, V>` instead
      of `PersistentArrayMap<K, V>`.
    * v(args) now returns `IPersistentVector<E>` instead
      of `PersistentVector<E>`.
    * `toSeq()` became `seq()` and moved to core.

#### Removed:

* ```kotlin
  Map<K, V>.toPArrayMap()
  ```

* ```kotlin
  Map<K, V>.toPhashMap()
  ```

* ```kotlin
  fun <E> List.toPVector(): IPersistentVector<E>
  ```

## v0.0.4

#### Updated:

* Update to Kotlin 1.5.21

#### Fixed:

* Missing Atomicfu dependency in artifacts by using Atomicfu Gradle plugin.
* Transients locks in multi-threaded environement.

## v0.0.3

#### Added:

* **y-concurrency** module:
    * Atom<T>
    * Functions:
        * atom(x:T): Atom<T>

#### Updated:

* Moved Keyword to JVM only (for now).

#### Fixed:

* Serialization exceptions in collections

## v0.0.2.1

#### Fixed:

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
          fun <E> List<E>.toPlist(): PersistentList<E>
          ```

        - ```kotlin
          fun l(vararg elements: E): PersistentList<E>
          ```

        - ```kotlin
          fun Map<K, V>.toPArrayMap(): PersistentArrayMap<K, V>
          ```

        - ```kotlin
          fun m(vararg pairs: Pair<K, V>): PersistentArrayMap<K, V>
          ```

        - ```kotlin
          fun hashMap(vararg pairs: Pair<K, V>): PersistentHashMap<K, V>
          ```

        - ```kotlin
          fun Map<K, V>.toPhashMap(): PersistentHashMap<K, V>
          ```

        - ```kotlin
          fun Set<E>.toPhashSet(): PersistentHashSet<E>
          ```

        - ```kotlin
          fun hs(vararg e: E): PersistentHashSet<E>
          ```

        - ```kotlin
          fun v(vararg elements: E): PersistentVector<E>
          ```

        - ```kotlin
          fun List<E>.toPvector(): PersistentVector<E>
          ```

        - ```kotlin
          fun lazyChunkedSeq(iterator: Iterator<E>): ISeq<E>
          ```

        - ```kotlin
          fun k(name: String): Keyword
          ```

#### Updated:

- The project was Kotlin/JVM only, now it is Kotlin Multiplatform that targets
  the JVM and Native (no Javascript support)

- **y-common** module became **y-core** module because *y-core* can depend on
  other modules while *y-common* is a shared module that contains only code that
  is ,well, common between other modules.

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
          fun assertCondition(value: T, p: (T) -> Boolean)
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
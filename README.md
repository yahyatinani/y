![Y](docs/art/logo-with-text.png)
========

[![build](https://github.com/yahyatinani/y/actions/workflows/main.yml/badge.svg)](https://github.com/yahyatinani/y/actions/workflows/main.yml) [![Sonatype Nexus (Snapshots)](https://img.shields.io/maven-central/v/com.github.whyrising.y/y-core?color=blue&label=latest%20release&server=https%3A%2F%2Foss.sonatype.org)](http://search.maven.org/#search|ga|1|com.github.whyrising.y) ![GitHub](https://img.shields.io/github/license/whyrising/y) [![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/com.github.whyrising.y/y-core?label=latest%20snapshot&server=https%3A%2F%2Foss.sonatype.org)](https://oss.sonatype.org/content/repositories/snapshots/com/github/whyrising/y/) [![codecov](https://codecov.io/gh/yahyatinani/y/branch/main/graph/badge.svg?token=O7NV8M4TTP)](https://codecov.io/gh/yahyatinani/y)

`y` is a Kotlin multiplatform library that aims to port and provide effective
concepts inspired by other languages or libraries.

## Modules:

### y-core:

This module provides core data structures and utility functions.

- ##### Data structures:

    - List:

      ```kotlin
      val list1 = l<Int>(1,2,3) // (1 2 3)
      val empty = l<Int>() // ()
          
      // From Kotlin's List<E> to PersistentList<E>
      val list2 = listOf<Int>(1, 2, 3).toPlist() // (1 2 3)
      ```

      To add an item to a list, use `conj(e)`, it puts the item at the front
      of the list and returns a new list.

      ```kotlin
      l<Int>(1,2,3).conj(4) // (4 1 2 3)
      ```

      To get the size of a list:

      ```kotlin
      l<Int>(1,2,3).count // 3
      ```

    - Vector:

        ```kotlin
      val vec1 = v<Int>(1, 2, 3) // [1 2 3]
        val empty = v<Int>() // []
            
        // From Kotlin's List<E> to PersistentVector<E>
        val vec2 = listOf<Int>(1, 2, 3).toPvector() // [1 2 3]
      ```

      To add an item to a vector, use `conj(e)`, it puts the item at the end
      of the vector and returns a new vector.

      ```kotlin
      v<Int>(1, 2, 3).conj(4) // [1 2 3 4]
      ```

      To get the size of a vector:

      ```kotlin
      v<Int>(1, 2, 3).count // 3
      ```

      To get an item by index, you can use `get(index)` operator
      or `nth(index)`/`nth(index, def)`

      ```kotlin
      v<Int>(1, 2, 3)[1] // 2
      v<Int>(1, 2, 3).nth(2) // 3
      v<Int>(1, 2, 3).nth(5, -1) // -1
      ```

      To get the items in reverse order as a sequence, use `reverse()`

      ```kotlin
      v<Int>(1, 2, 3).reverse() // (3 2 1)
      ```

    - PersistentArrayMap:

      This map should be only used when you have very small maps and want to
      maintain key order, since it is just an array of `[key,val..key,val]`.
      Subsequent `assoc-ing` will eventually return a `PersistentHashMap`.

        ```kotlin
        val map1 = m<String, Int>("1" to 1, "2" to 2) // {"1" 1, "2" 2}
              
        //From Kotlin's Map<K,V> to PersistentArrayMap<K,V>
        val map2 = mapOf<String, Int>("1" to 1, "2" to 2).toPArrayMap() // {"1" 1, "2" 2}
        ```

    - PersistentHashMap:

      This map requires keys that correctly support hashCode and equals.

        ```kotlin
        val map1 = hashMap<String, Int>("1" to 1, "2" to 2) // {"1" 1, "2" 2}
            
        //From Kotlin's Map<K,V> to PersistentHashMap<K,V>
        val map2 = mapOf<String, Int>("1" to 1, "2" to 2).toPhashMap() // {"1" 1, "2" 2}
        ```

    - PersistentHashSet:

      ```kotlin
      val set1 = hs<Int>(1, 2, 2, 3, 3) // #{1 2 3}
          
      //From Kotlin's Set<E> to PersistentHashSet<E>
      val set2 = setOf<Int>(1, 2, 2, 3 ,3).toPhashSet() // #{1 2 3}
      ```

    - Sequence:

      All collections support a member function `seq()` that return a
      sequence of type `ISeq<E>` that can walk the entire collection. A
      sequance provides three key member functions:

        - `first()` : return the first element in the sequence.
        - `rest()` : returns all of the rest elements of the sequence that
          came after first element, as a sequence.
        - `cons(element)` : always adds to the front of the sequence and
          returns a sequence.

    - ###### Keywords (WIP, only on JVM for now):

      Keywords are identifiers that provide very fast equality tests, and they
      have a string name. If you call toString() on a keyword it returns the
      name prefixed by a ':' which is not part of the name.

      ```kotlin
      // To create a Keyword, use the utility function `k(name)`:
      val keyword = k("a") // :a
      
      // invoke(map, default = null) operator
      val map = hashMap("a" to 1)
      val key = k("a")
      val default = k("none")
      key(map, default) // :none
      ```

package com.github.whyrising.y

interface ITransientAssociative<out K, out V> :
    ITransientCollection<Any?>,
    ILookup<K, V>

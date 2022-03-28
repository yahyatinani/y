package com.github.whyrising.y.collections.list

import com.github.whyrising.y.collections.seq.IPersistentCollection
import com.github.whyrising.y.collections.seq.Sequential
import com.github.whyrising.y.collections.stack.IPersistentStack

interface IPersistentList<out E> :
    IPersistentCollection<E>,
    IPersistentStack<E>,
    Sequential

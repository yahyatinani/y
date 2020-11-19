package com.github.whyrising.y.list

import com.github.whyrising.y.seq.IPersistentCollection
import com.github.whyrising.y.seq.Sequential
import com.github.whyrising.y.stack.IPersistentStack

interface IPersistentList<out E> :
    IPersistentCollection<E>,
    IPersistentStack<E>,
    Sequential

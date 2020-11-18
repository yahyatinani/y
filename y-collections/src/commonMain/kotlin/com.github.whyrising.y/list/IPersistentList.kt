package com.github.whyrising.y.list

import com.github.whyrising.y.stack.IPersistentStack
import com.github.whyrising.y.seq.IPersistentCollection
import com.github.whyrising.y.seq.Sequential

interface IPersistentList<out E> :
    IPersistentCollection<E>,
    IPersistentStack<E>,
    Sequential

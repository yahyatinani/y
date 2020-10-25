package com.github.whyrising.y

interface IPersistentList<out E> :
    IPersistentCollection<E>,
    IPersistentStack<E>,
    Sequential

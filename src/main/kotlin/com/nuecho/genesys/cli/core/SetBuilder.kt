package com.nuecho.genesys.cli.core

class SetBuilder<T> {
    private val items: MutableSet<T> = mutableSetOf()

    fun add(item: T?): SetBuilder<T> {
        if (item != null) items += item
        return this
    }

    fun add(item: Iterable<T>?): SetBuilder<T> {
        if (item != null) items.addAll(item)
        return this
    }

    fun toSet() = items
}

fun <T> setBuilder() = SetBuilder<T>()

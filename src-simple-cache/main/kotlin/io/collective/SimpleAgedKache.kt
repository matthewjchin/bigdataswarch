package io.collective

import java.time.Clock

class SimpleAgedKache {

    val count: Int = 0

    constructor(clock: Clock?) {

    }

    constructor() {
    }

    fun put(key: Any?, value: Any?, retentionInMillis: Int) {

    }

    fun isEmpty(): Boolean {
        return false
    }

    fun size(): Int {
        return count
    }

    fun get(key: Any?): Any? {
        return null
    }
}
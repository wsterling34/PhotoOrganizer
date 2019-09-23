package com.wsterling.photos2

import com.wsterling.util.BinaryUtils
import java.util.*

fun main() {

    val size = 1024
    val bytes = ByteArray(size)
    val random = Random(99)

    for (i in 0 until size) {
        bytes[i] = random.nextInt().toByte()
    }

    val count = 1

    val t0 = System.currentTimeMillis()

    for (i in 0 until count) {
        val s1 = BinaryUtils.encodeAsString(bytes)
        println(s1)
    }

    val elapsedSecs = (System.currentTimeMillis() - t0).toDouble() / 1000
    println(elapsedSecs)

}
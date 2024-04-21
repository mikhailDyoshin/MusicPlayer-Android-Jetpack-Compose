package com.example.musicplayerapp.utils

import kotlin.math.abs

fun modulo(dividend: Int, divider: Int): Int {
    if (dividend < 0) {
        return (divider+dividend) % divider
    }
    return dividend % divider
}
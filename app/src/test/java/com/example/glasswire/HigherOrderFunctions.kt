package com.example.glasswire

import org.junit.Assert
import org.junit.Test

class HigherOrderFunctions {

    data class Item(
        val number: Int,
        val string: String,
    )

    @Test
    fun `add single attribute from a list of data class`() {
        // Given
        val list: List<Item> = listOf(
            Item(1, "defes"),
            Item(2, "xcvc"),
            Item(3, "fgfg"),
            Item(4, "dsfd"),
            Item(5, "efses"),
        )

        val actualResult: Int = list.sumOf { it.number }
        val expectedResult: Int = 15

        Assert.assertEquals(expectedResult, actualResult)

    }

    data class Item2(
        val numberOne: Int,
        val numberTwo: Int
    )

    @Test
    fun `add two attributes from a list of data class`() {
        // Given
        val list: List<Item2> = listOf(
            Item2(1, 1),
            Item2(1, 1),
            Item2(1, 1),
            Item2(1, 1),
            Item2(1, 1),
            Item2(1, 1),
        )

        val actualResult = list.sumOf { it.numberOne + it.numberTwo }

        val expectedResult = 12

        Assert.assertEquals(expectedResult, actualResult)
    }


}
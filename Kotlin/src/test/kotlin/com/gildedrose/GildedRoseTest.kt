package com.gildedrose

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

internal class GildedRoseTest {

    @Test
    fun foo() {
        val items = listOf(Item("foo", 0, 0))
        val app = GildedRose(items)
        app.updateQuality()
        assertEquals("foo", app.items[0].name)
    }

    @Test
    fun regularItemDecreasesInQuality() {
        // Given
        val items = listOf(Item("+5 Dexterity Vest", 10, 20))

        // When
        val app = createAndUpdateApp(items)

        // Then
        assertThat(app.items[0].name).isEqualTo("+5 Dexterity Vest")
        assertThat(app.items[0].sellIn).isEqualTo(9)
        assertThat(app.items[0].quality).isEqualTo(19)
    }

    @Test
    fun qualityIsNeverLessThan0() {
        // Given
        val items = listOf(Item("Foo", 10, 0))

        // When
        val app = createAndUpdateApp(items)

        // Then
        assertThat(app.items[0].quality).isEqualTo(0)
    }

    @Test
    fun qualityDegradesTwiceAsFastPastSellDate() {
        // Given
        val items = listOf(Item("Foo", 2, 10))

        // When
        val app = createAndUpdateApp(items, 4)

        // Then
        // 10 - 1 - 1 - 2 - 2 = 4
        assertThat(app.items[0].sellIn).isEqualTo(-2)
        assertThat(app.items[0].quality).isEqualTo(4)
    }

    @Test
    fun agedBrieIncreasesInQuality() {
        // Given
        val items = listOf(Item(AGED_BRIE, 5, 10))

        // When
        val app = createAndUpdateApp(items, 3)

        // Then
        assertThat(app.items[0].quality).isEqualTo(13)
    }

    @Test
    fun agedBrieIncreasesTwiceAsFastPastSellDate() {
        // Given
        val items = listOf(Item(AGED_BRIE, 2, 10))

        // When
        val app = createAndUpdateApp(items, 4)

        // Then
        // 10 + 1 + 1 + 2 + 2 = 16
        assertThat(app.items[0].quality).isEqualTo(16)
    }

    @Test
    fun qualityIsNeverMoreThan50() {
        // Given
        val items = listOf(Item(AGED_BRIE, 5, 40))

        // When
        val app = createAndUpdateApp(items, 15)

        // Then
        assertThat(app.items[0].quality).isEqualTo(50)
    }

    @Test
    fun sulfurasIsStationary() {
        // Given
        val items = listOf(Item(SULFURAS, 5, 10))

        // When
        val app = createAndUpdateApp(items, 20)

        // Then
        assertThat(app.items[0].sellIn).isEqualTo(5)
        assertThat(app.items[0].quality).isEqualTo(10)
    }

    @Test
    fun initialQualityCanBeOutOfBounds() {
        // Given
        val items = listOf(
                Item(SULFURAS, 5, 80),
                Item(SULFURAS, 4, -2))

        // When
        val app = createAndUpdateApp(items, 2)

        // Then
        assertThat(app.items[0].quality).isEqualTo(80)
        assertThat(app.items[1].quality).isEqualTo(-2)
    }

    @ParameterizedTest
    @MethodSource
    fun backstagePasses(item: Item, days: Int, expectedSellIn: Int, expectedQuality: Int) {
        // Given
        val items = listOf(item)

        // When
        val app = createAndUpdateApp(items, days)

        // Then
        assertThat(app.items[0].sellIn).isEqualTo(expectedSellIn)
        assertThat(app.items[0].quality).isEqualTo(expectedQuality)
    }

    companion object {
        @JvmStatic
        fun backstagePasses(): Stream<Arguments> {

            return Stream.of(
                    Arguments.of(Item(BACKSTAGE_PASSES, 12, 2), 0, 12, 2),
                    Arguments.of(Item(BACKSTAGE_PASSES, 12, 2), 1, 11, 3),
                    Arguments.of(Item(BACKSTAGE_PASSES, 12, 2), 2, 10, 4),
                    Arguments.of(Item(BACKSTAGE_PASSES, 12, 2), 3,  9, 6),
                    Arguments.of(Item(BACKSTAGE_PASSES, 12, 2), 4,  8, 8),
                    Arguments.of(Item(BACKSTAGE_PASSES, 12, 2), 5,  7, 10),
                    Arguments.of(Item(BACKSTAGE_PASSES, 12, 2), 6,  6, 12),
                    Arguments.of(Item(BACKSTAGE_PASSES, 12, 2), 7,  5, 14),
                    Arguments.of(Item(BACKSTAGE_PASSES, 12, 2), 8,  4, 17),
                    Arguments.of(Item(BACKSTAGE_PASSES, 12, 2), 9,  3, 20),
                    Arguments.of(Item(BACKSTAGE_PASSES, 12, 2), 10, 2, 23),
                    Arguments.of(Item(BACKSTAGE_PASSES, 12, 2), 11, 1, 26),
                    Arguments.of(Item(BACKSTAGE_PASSES, 12, 2), 12, 0, 29),
                    Arguments.of(Item(BACKSTAGE_PASSES, 12, 2), 13, -1, 0),
            )
        }
    }

    @Test
    fun conjuredItemDegradesTwiceAsFastAsRegularItem() {
        // Given
        val items = listOf(
                Item("Spoon", 5, 10),
                Item("Conjured spoon", 5, 10)
        )

        // When
        val app = createAndUpdateApp(items, 3)

        // Then
        val spoon = app.items[0]
        val conjuredSpoon = app.items[1]

        assertThat(spoon.name).isEqualTo("Spoon")
        assertThat(spoon.sellIn).isEqualTo(2)
        assertThat(spoon.quality).isEqualTo(7)
        assertThat(conjuredSpoon.name).isEqualTo("Conjured spoon")
        assertThat(conjuredSpoon.sellIn).isEqualTo(2)
        assertThat(conjuredSpoon.quality).isEqualTo(4)
    }

    private fun createAndUpdateApp(items: List<Item>, times: Int = 1): GildedRose {
        val app = GildedRose(items)
        repeat(times) { _ ->  app.updateQuality() }

        return app
    }

}

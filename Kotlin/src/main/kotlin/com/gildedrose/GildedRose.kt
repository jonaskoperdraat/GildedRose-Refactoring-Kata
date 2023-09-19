package com.gildedrose

const val AGED_BRIE = "Aged Brie"
const val BACKSTAGE_PASSES = "Backstage passes to a TAFKAL80ETC concert"
const val SULFURAS = "Sulfuras, Hand of Ragnaros"

val udpateStrategies: List<UpdateStrategy> = listOf(
        AgedBrieStrategy(),
        BackstagePassesStrategy(),
        SulfurasStrategy(),
        ConjuredItemStrategy())

class GildedRose(var items: List<Item>) {
    fun updateQuality() {
        for (item in items) {
            val strategy = udpateStrategies.find { s -> s.appliesTo(item) } ?: GenericStrategy()

            val degradation = strategy.getQualityDegradation(item)
            // This is a bit weird; the quality is only updated, and coerced to be max 50 and min 0,
            // when there's an actual quality change. There's this requirement that 50 >= quality >= 0,
            // but at the same time, the validation test initialized a Sulfuras item with
            // quality 80, which it keeps during the entire duration of the test.
            if (degradation != 0) {
                item.quality = (item.quality - degradation).coerceAtLeast(0).coerceAtMost(50)
            }
            item.sellIn -= strategy.getSellInDecrement(item)
        }
    }
}

interface UpdateStrategy {
    fun appliesTo(item: Item): Boolean
    fun getSellInDecrement(item: Item): Int = 1
    fun getQualityDegradation(item: Item): Int = 0
}

class GenericStrategy : UpdateStrategy {
    override fun appliesTo(item: Item) = true
    override fun getQualityDegradation(item: Item) = if (item.sellIn > 0) 1 else 2
}

class AgedBrieStrategy : UpdateStrategy {
    override fun appliesTo(item: Item) = item.name == AGED_BRIE
    override fun getQualityDegradation(item: Item) = if (item.sellIn > 0) -1 else -2
}

class SulfurasStrategy : UpdateStrategy {
    override fun appliesTo(item: Item) = item.name == SULFURAS
    override fun getQualityDegradation(item: Item) = 0
    override fun getSellInDecrement(item: Item) = 0
}

class BackstagePassesStrategy : UpdateStrategy {
    override fun appliesTo(item: Item) = item.name == BACKSTAGE_PASSES
    override fun getQualityDegradation(item: Item) = when {
        item.sellIn <= 0 -> item.quality
        item.sellIn <= 5 -> -3
        item.sellIn <= 10 -> -2
        else -> -1
    }
}

class ConjuredItemStrategy : UpdateStrategy {
    override fun appliesTo(item: Item) = item.name.lowercase().contains("conjured")
    override fun getQualityDegradation(item: Item) = if (item.sellIn > 0) 2 else 4
}

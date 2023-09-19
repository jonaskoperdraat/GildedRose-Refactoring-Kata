package com.gildedrose

const val AGED_BRIE = "Aged Brie"
const val BACKSTAGE_PASSES = "Backstage passes to a TAFKAL80ETC concert"
const val SULFURAS = "Sulfuras, Hand of Ragnaros"

val updateStrategies: List<UpdateStrategy> = listOf(
        AgedBrieStrategy(),
        BackstagePassesStrategy(),
        SulfurasStrategy(),
        ConjuredItemStrategy())

private const val MAX_QUALITY = 50
private const val MIN_QUALITY = 0

class GildedRose(var items: List<Item>) {
    fun updateQuality() {
        items.forEach {
            (updateStrategies
                    .find { s -> s.appliesTo(it) } ?: GenericStrategy())
                    .update(it)
        }
    }
}

interface UpdateStrategy {
    fun appliesTo(item: Item): Boolean
    fun update(item: Item)
}

class GenericStrategy : UpdateStrategy {
    override fun appliesTo(item: Item) = true
    override fun update(item: Item) {
        item.quality = (item.quality - if (item.sellIn > 0) 1 else 2)
                .coerceIn(MIN_QUALITY, MAX_QUALITY)
        item.sellIn--
    }
}

class AgedBrieStrategy : UpdateStrategy {
    override fun appliesTo(item: Item) = item.name == AGED_BRIE
    override fun update(item: Item) {
        item.quality = (item.quality + if (item.sellIn > 0) 1 else 2)
                .coerceIn(MIN_QUALITY, MAX_QUALITY)
        item.sellIn--
    }
}

class SulfurasStrategy : UpdateStrategy {
    override fun appliesTo(item: Item) = item.name == SULFURAS
    override fun update(item: Item) {
        // Do nothing
    }
}

class BackstagePassesStrategy : UpdateStrategy {
    override fun appliesTo(item: Item) = item.name == BACKSTAGE_PASSES
    override fun update(item: Item) {
        item.quality = (item.quality + when {
            item.sellIn <= 0 -> -item.quality
            item.sellIn <= 5 -> 3
            item.sellIn <= 10 -> 2
            else -> 1
        })
                .coerceIn(MIN_QUALITY, MAX_QUALITY)
        item.sellIn--
    }
}

class ConjuredItemStrategy : UpdateStrategy {
    override fun appliesTo(item: Item) = item.name.lowercase().contains("conjured")
    override fun update(item: Item) {
        item.quality = (item.quality - if (item.sellIn > 0) 2 else 4)
                .coerceIn(MIN_QUALITY, MAX_QUALITY)
        item.sellIn--
    }
}

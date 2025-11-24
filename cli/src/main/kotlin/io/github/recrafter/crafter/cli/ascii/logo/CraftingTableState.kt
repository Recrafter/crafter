package io.github.recrafter.crafter.cli.ascii.logo

enum class CraftingTableState {

    PREPARING_1,
    PREPARING_2,
    CRAFTING;

    companion object {
        const val EMPTY_TILE: String = "/___"
        const val OCCUPIED_TILE: String = "/-*-"
    }
}

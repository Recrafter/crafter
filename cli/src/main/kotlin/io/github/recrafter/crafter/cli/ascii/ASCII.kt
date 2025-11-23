package io.github.recrafter.crafter.cli.ascii

import io.github.diskria.kotlin.utils.extensions.nextEnumOrNull
import io.github.diskria.kotlin.utils.extensions.replaceMultiLine
import io.github.diskria.kotlin.utils.extensions.trimMarginEnd
import io.github.diskria.kotlin.utils.extensions.wrapWithSpace

object ASCII {

    private val craftingRow: String = CraftingTableState.EMPTY_TILE.repeat(3)

    private val hashAtlas: String = """
        ${HashState.IDLE}      __ __  ${HashState.SPEED_25}  __  __ __  ${HashState.SPEED_50}   __ _____  ${HashState.SPEED_75} _ _____ __  ${HashState.SPEED_100} _______ __  
        ${HashState.IDLE}   __/ // /_ ${HashState.SPEED_25}   __/ // /_ ${HashState.SPEED_50} _ __/ // /_ ${HashState.SPEED_75}  ___/ // /_ ${HashState.SPEED_100} ____/ // /_ 
        ${HashState.IDLE}  /_  _  __/ ${HashState.SPEED_25} _ _  _  __/ ${HashState.SPEED_50} _ _  _  __/ ${HashState.SPEED_75} _ _  _  __/ ${HashState.SPEED_100} _ _  _  __/  
        ${HashState.IDLE} /_  _  __/  ${HashState.SPEED_25} /_  _  __/  ${HashState.SPEED_50} /_  _  __/  ${HashState.SPEED_75} /_  _  __/  ${HashState.SPEED_100} /_  _  __/  
        ${HashState.IDLE}  /_//_/     ${HashState.SPEED_25}  /_//_/     ${HashState.SPEED_50}  /_//_/     ${HashState.SPEED_75}  /_//_/     ${HashState.SPEED_100}  /_//_/     
        """.trimIndent()

    private val creeperAtlas: String = """
        ${CreeperState.A_LETTER}  ______    ${CreeperState.WINK}  ______    
        ${CreeperState.A_LETTER} /\  __ \   ${CreeperState.WINK} /\ + - \   
        ${CreeperState.A_LETTER} \ \  __ \  ${CreeperState.WINK} \ \  __ \  
        ${CreeperState.A_LETTER}  \ \_\ \_\ ${CreeperState.WINK}  \ \_\ \_\ 
        ${CreeperState.A_LETTER}   \/_/\/_/ ${CreeperState.WINK}   \/_/\/_/ 
        """.trimIndent()

    private val craftingTableAtlas: String = """
        ${CraftingTableState.PREPARING_1}    _ _ ____ _ _  ${CraftingTableState.PREPARING_2}    __ _ __ _ __  ${CraftingTableState.CRAFTING}    ____________ 
        ${CraftingTableState.PREPARING_1}   /_ _/_ _/_ _/| ${CraftingTableState.PREPARING_2}   /__ / _ / __/  ${CraftingTableState.CRAFTING}   $craftingRow/|
        ${CraftingTableState.PREPARING_1}  /_ _/_ _/_ _/   ${CraftingTableState.PREPARING_2}  /__ / _ / __/ / ${CraftingTableState.CRAFTING}  $craftingRow/ /
        ${CraftingTableState.PREPARING_1} /_ _/_ _/_ _/ /  ${CraftingTableState.PREPARING_2} /__ / _ / __/    ${CraftingTableState.CRAFTING} $craftingRow/ /
        ${CraftingTableState.PREPARING_1} |_ _ _ _ _ _|    ${CraftingTableState.PREPARING_2} | _ _ _ _ _ |/   ${CraftingTableState.CRAFTING} |___________|/
        """.trimIndent()

    /**
     * ASCII logo generated via <a href="https://patorjk.com/software/taag/">patorjk.com</a>.
     * Fonts used: <b>Speed</b> for the `#` symbol and <b>Sub-Zero</b> for the text “Crafter”.
     */
    fun generateLogo(
        hashState: HashState = HashState.IDLE,
        creeperState: CreeperState = CreeperState.A_LETTER,
    ): String = """
        ${LogoElement.HASH}   ______     ______    ${LogoElement.CREEPER}  ______    ______    ______     ______   
        ${LogoElement.HASH}  /\  ___\   /\  == \   ${LogoElement.CREEPER} /\  ___\  /\__  _\  /\  ___\   /\  == \  
        ${LogoElement.HASH}  \ \ \____  \ \  __<   ${LogoElement.CREEPER} \ \  __\  \/_/\ \/  \ \  __\   \ \  __<  
        ${LogoElement.HASH}   \ \_____\  \ \_\ \_\ ${LogoElement.CREEPER}  \ \_\       \ \_\   \ \_____\  \ \_\ \_\
        ${LogoElement.HASH}    \/_____/   \/_/ /_/ ${LogoElement.CREEPER}   \/_/        \/_/    \/_____/   \/_/ /_/
        """.trimIndent()
        .replaceMultiLine(LogoElement.HASH.name, getAtlasFrame(hashAtlas, hashState))
        .replaceMultiLine(LogoElement.CREEPER.name, getAtlasFrame(creeperAtlas, creeperState))

    private fun getAtlasFrame(atlas: String, state: Enum<*>): String {
        val nextFrameEnum = state.nextEnumOrNull() ?: state
        val trimmedLeft = atlas.trimMargin(state.name)
        val trimmedRight = trimmedLeft.trimMarginEnd(nextFrameEnum.name.wrapWithSpace())
        return trimmedRight.trimIndent()
    }
}

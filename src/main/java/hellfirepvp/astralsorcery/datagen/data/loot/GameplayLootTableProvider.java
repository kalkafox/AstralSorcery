/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.datagen.data.loot;

import net.minecraft.data.loot.GiftLootTables;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.resources.ResourceLocation;

import java.util.function.BiConsumer;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: GameplayLootTableProvider
 * Created by HellFirePvP
 * Date: 20.12.2020 / 21:46
 */
public class GameplayLootTableProvider extends GiftLoot {

    @Override
    public void accept(BiConsumer<ResourceLocation, LootTable.Builder> registrar) {

    }
}

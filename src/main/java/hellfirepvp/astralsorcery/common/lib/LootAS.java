/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.lib;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.loot.CopyConstellation;
import hellfirepvp.astralsorcery.common.loot.CopyCrystalProperties;
import hellfirepvp.astralsorcery.common.loot.CopyGatewayColor;
import hellfirepvp.astralsorcery.common.loot.LinearLuckBonus;
import hellfirepvp.astralsorcery.common.loot.RandomCrystalProperty;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.resources.ResourceLocation;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: LootAS
 * Created by HellFirePvP
 * Date: 02.05.2020 / 15:41
 */
public class LootAS {

    private LootAS() {}

    public static final ResourceLocation SHRINE_CHEST = AstralSorcery.key("shrine_chest");

    public static final ResourceLocation STARFALL_SHOOTING_STAR_REWARD = AstralSorcery.key("gameplay/starfall/shooting_star");

    public static class Functions {

        public static LootItemFunctionType<LinearLuckBonus> LINEAR_LUCK_BONUS;
        public static LootItemFunctionType<RandomCrystalProperty> RANDOM_CRYSTAL_PROPERTIES;
        public static LootItemFunctionType<CopyCrystalProperties> COPY_CRYSTAL_PROPERTIES;
        public static LootItemFunctionType<CopyConstellation> COPY_CONSTELLATION;
        public static LootItemFunctionType<CopyGatewayColor> COPY_GATEWAY_COLOR;

    }
}

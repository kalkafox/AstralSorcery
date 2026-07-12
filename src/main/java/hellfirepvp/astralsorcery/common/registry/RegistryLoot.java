/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.registry;

import com.mojang.serialization.MapCodec;
import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.loot.*;
import hellfirepvp.astralsorcery.common.loot.global.LootModifierPerkVoidTrash;
import hellfirepvp.astralsorcery.common.loot.global.LootModifierScorchingHeat;
import hellfirepvp.astralsorcery.common.registry.internal.AstralRegistries;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;

import static hellfirepvp.astralsorcery.common.lib.LootAS.*;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RegistryLoot
 * Created by HellFirePvP
 * Date: 20.07.2019 / 21:48
 */
public class RegistryLoot {

    private RegistryLoot() {}

    public static void init() {
        registerGlobalModifier(LootModifierScorchingHeat.CODEC, AstralSorcery.key("scorching_heat"));
        registerGlobalModifier(LootModifierPerkVoidTrash.CODEC, AstralSorcery.key("perk_void_trash"));

        Functions.LINEAR_LUCK_BONUS = registerFunction(LinearLuckBonus.CODEC, AstralSorcery.key("linear_luck_bonus"));
        Functions.RANDOM_CRYSTAL_PROPERTIES = registerFunction(RandomCrystalProperty.CODEC, AstralSorcery.key("random_crystal_property"));
        Functions.COPY_CRYSTAL_PROPERTIES = registerFunction(CopyCrystalProperties.CODEC, AstralSorcery.key("copy_crystal_properties"));
        Functions.COPY_CONSTELLATION = registerFunction(CopyConstellation.CODEC, AstralSorcery.key("copy_constellation"));
        Functions.COPY_GATEWAY_COLOR = registerFunction(CopyGatewayColor.CODEC, AstralSorcery.key("copy_gateway_color"));
    }

    private static <T extends LootItemConditionalFunction> LootItemFunctionType<T> registerFunction(MapCodec<T> codec, ResourceLocation key) {
        return AstralRegistries.register(AstralRegistries.LOOT_FUNCTION_TYPES, key, new LootItemFunctionType<>(codec));
    }

    private static void registerGlobalModifier(MapCodec<? extends IGlobalLootModifier> codec, ResourceLocation key) {
        AstralRegistries.register(AstralRegistries.GLOBAL_LOOT_MODIFIER_SERIALIZERS, key, codec);
    }

}

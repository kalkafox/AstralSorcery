/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting.nojson.starlight;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.client.effect.function.VFXAlphaFunction;
import hellfirepvp.astralsorcery.client.effect.function.VFXColorFunction;
import hellfirepvp.astralsorcery.client.effect.handler.EffectHelper;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.common.crafting.helper.ingredient.CrystalIngredient;
import hellfirepvp.astralsorcery.common.crystal.CrystalAttributes;
import hellfirepvp.astralsorcery.common.data.config.entry.CraftingConfig;
import hellfirepvp.astralsorcery.common.item.crystal.ItemAttunedRockCrystal;
import hellfirepvp.astralsorcery.common.item.crystal.ItemCrystalBase;
import hellfirepvp.astralsorcery.common.item.crystal.ItemRockCrystal;
import hellfirepvp.astralsorcery.common.lib.ColorsAS;
import hellfirepvp.astralsorcery.common.lib.CrystalPropertiesAS;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: GrowCrystalSizeRecipe
 * Created by HellFirePvP
 * Date: 05.05.2020 / 17:15
 */
public class GrowCrystalSizeRecipe extends LiquidStarlightRecipe {

    public GrowCrystalSizeRecipe() {
        super(AstralSorcery.key("crystal_grow"));
    }

    @Override
    public List<Ingredient> getInputForRender() {
        return Collections.singletonList(new CrystalIngredient(false, false));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Ingredient> getOutputForRender() {
        return Collections.singletonList(new CrystalIngredient(false, false));
    }

    @Override
    public boolean doesStartRecipe(ItemStack item) {
        if (!CraftingConfig.CONFIG.liquidStarlightCrystalGrowth.get()) {
            return false;
        }
        return !item.isEmpty() && item.getItem() instanceof ItemCrystalBase;
    }

    @Override
    public boolean matches(ItemEntity trigger, Level level, BlockPos at) {
        List<Entity> otherEntities = getEntitiesInBlock(level, at);
        otherEntities.remove(trigger);
        return otherEntities.isEmpty();
    }

    @Override
    public void doServerCraftTick(ItemEntity trigger, Level level, BlockPos at) {
        Random r = new Random(Mth.getSeed(at));
        if (!level.isClientSide() && getAndIncrementCraftingTick(trigger) > 80 + r.nextInt(40)) {
            ItemStack stack = trigger.getItem();
            CrystalAttributes attr = ((ItemCrystalBase) stack.getItem()).getAttributes(stack);
            if (attr != null && level.setBlock(at, Blocks.AIR.defaultBlockState())) {
                if (attr.getTotalTierLevel() >= ((ItemCrystalBase) stack.getItem()).getMaxPropertyTiers()) {
                    return;
                }
                float chance = 1;
                if (attr.getTotalTierLevel() >= ((ItemCrystalBase) stack.getItem()).getGeneratedPropertyTiers()) {
                    chance = 0.5F;
                }
                if (random.nextFloat() < chance) {
                    attr = attr.modifyLevel(CrystalPropertiesAS.Properties.PROPERTY_SIZE, 1);
                    ((ItemCrystalBase) stack.getItem()).setAttributes(stack, attr);
                }
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void doClientEffectTick(ItemEntity trigger, Level level, BlockPos at) {
        Color c = ColorsAS.DEFAULT_GENERIC_PARTICLE;
        if (trigger.getItem().getItem() instanceof ItemRockCrystal ||
                trigger.getItem().getItem() instanceof ItemAttunedRockCrystal) {
            c = ColorsAS.ROCK_CRYSTAL;
        }
        for (int i = 0; i < 3; i++) {
            Vector3 pos = Vector3.atEntityCenter(trigger);
            MiscUtils.applyRandomOffset(pos, random, 0.15F);

            Vector3 motion = Vector3.RotAxis.Y_AXIS.clone();
            motion.mirror(Math.toRadians(10 + random.nextInt(20)), Vector3.RotAxis.X_AXIS)
                    .mirror(random.nextFloat() * Math.PI * 2, Vector3.RotAxis.Y_AXIS)
                    .normalize().mul(0.07F + random.nextFloat() * 0.04F);

            EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                    .spawn(pos)
                    .alpha1arg(VFXAlphaFunction.FADE_OUT)
                    .setDeltaMovement(motion)
                    .setScaleMultiplier(0.05F + random.nextFloat() * 0.2F)
                    .color(VFXColorFunction.constant(c))
                    .setMaxAge(30 + random.nextInt(20));
        }
    }
}

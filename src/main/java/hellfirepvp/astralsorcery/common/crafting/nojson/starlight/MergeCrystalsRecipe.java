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
import hellfirepvp.astralsorcery.client.effect.function.VFXMotionController;
import hellfirepvp.astralsorcery.client.effect.handler.EffectHelper;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.common.crafting.helper.ingredient.CrystalIngredient;
import hellfirepvp.astralsorcery.common.crystal.CrystalAttributes;
import hellfirepvp.astralsorcery.common.data.config.entry.CraftingConfig;
import hellfirepvp.astralsorcery.common.item.crystal.ItemCrystalBase;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import hellfirepvp.astralsorcery.common.util.item.ItemUtils;
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
import hellfirepvp.astralsorcery.common.util.Constants;

import java.util.*;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: MergeCrystalsRecipe
 * Created by HellFirePvP
 * Date: 29.05.2020 / 20:54
 */
public class MergeCrystalsRecipe extends LiquidStarlightRecipe {

    public MergeCrystalsRecipe() {
        super(AstralSorcery.key("merge_crystals"));
    }

    @Override
    public List<Ingredient> getInputForRender() {
        return Arrays.asList(new CrystalIngredient(false, false),
                new CrystalIngredient(false, false).toVanilla());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Ingredient> getOutputForRender() {
        return Collections.singletonList(new CrystalIngredient(false, false).toVanilla());
    }

    @Override
    public boolean doesStartRecipe(ItemStack item) {
        if (!CraftingConfig.CONFIG.liquidStarlightDropInfusedWood.get()) {
            return false;
        }
        return !item.isEmpty() && item.getItem() instanceof ItemCrystalBase;
    }

    @Override
    public boolean matches(ItemEntity trigger, Level level, BlockPos at) {
        List<Entity> otherEntities = getEntitiesInBlock(level, at);
        otherEntities.remove(trigger);
        Optional<Entity> crystalEntity = otherEntities.stream()
                .filter(e -> e instanceof ItemEntity)
                .filter(e -> ((ItemEntity) e).getItem().getItem() instanceof ItemCrystalBase)
                .findFirst();
        return crystalEntity.isPresent() && otherEntities.size() == 1;
    }

    @Override
    public void doServerCraftTick(ItemEntity trigger, Level level, BlockPos at) {
        Random r = new Random(Mth.getSeed(at));
        if (!level.isClientSide() && getAndIncrementCraftingTick(trigger) > 40 + r.nextInt(20)) {
            ItemStack crystalFoundOne, crystalFoundTwo;
            if ((crystalFoundOne = consumeItemEntityInBlock(level, at, 1, stack -> stack.getItem() instanceof ItemCrystalBase)) != null &&
                    (crystalFoundTwo = consumeItemEntityInBlock(level, at, 1, stack -> stack.getItem() instanceof ItemCrystalBase)) != null &&
                    level.setBlock(at, Blocks.AIR.defaultBlockState(), Constants.BlockFlags.DEFAULT_AND_RERENDER)) {

                ItemCrystalBase crystalOne = (ItemCrystalBase) crystalFoundOne.getItem();
                CrystalAttributes attrOne = crystalOne.getAttributes(crystalFoundOne);
                attrOne = attrOne != null ? attrOne : CrystalAttributes.Builder.properties(false).build();

                ItemCrystalBase crystalTwo = (ItemCrystalBase) crystalFoundTwo.getItem();
                CrystalAttributes attrTwo = crystalTwo.getAttributes(crystalFoundTwo);
                attrTwo = attrTwo != null ? attrTwo : CrystalAttributes.Builder.properties(false).build();

                CrystalAttributes mergeTo = attrOne.getTotalTierLevel() >= attrTwo.getTotalTierLevel() ? attrOne : attrTwo;
                CrystalAttributes mergeFrom = attrOne.getTotalTierLevel() >= attrTwo.getTotalTierLevel() ? attrTwo : attrOne;

                ItemStack resultStack = attrOne.getTotalTierLevel() >= attrTwo.getTotalTierLevel() ? crystalFoundOne.copy() : crystalFoundTwo.copy();
                ItemCrystalBase resultCrystal = (ItemCrystalBase) resultStack.getItem();
                CrystalAttributes.Builder resultBuilder = CrystalAttributes.Builder.properties(false).addAll(mergeTo);

                int freeProperties = resultCrystal.getMaxPropertyTiers() - mergeTo.getTotalTierLevel();
                int copyAmount = Math.min(freeProperties, mergeFrom.getTotalTierLevel());
                int mergeCount = 0;
                for (int i = 0; i < copyAmount; i++) {
                    CrystalAttributes.Attribute attr = MiscUtils.getWeightedRandomEntry(mergeFrom.getCrystalAttributes(), random, CrystalAttributes.Attribute::getTier);
                    if (attr != null) {
                        mergeFrom = mergeFrom.modifyLevel(attr.getProperty(), -1);
                        if (random.nextFloat() <= (1F - Math.min(mergeCount, 3) * 0.25F)) {
                            resultBuilder.addProperty(attr.getProperty(), 1);
                        }
                        mergeCount++;
                    }
                }

                resultCrystal.setAttributes(resultStack, resultBuilder.build());
                ItemUtils.dropItemNaturally(level, trigger.getX(), trigger.getY(), trigger.getZ(), resultStack);
            }
        }
    }

    @Override
    public void doClientEffectTick(ItemEntity trigger, Level level, BlockPos at) {
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
                    .setScaleMultiplier(0.1F + random.nextFloat() * 0.2F)
                    .color(VFXColorFunction.WHITE)
                    .setMaxAge(35 + random.nextInt(20));
        }
        for (int i = 0; i < 4; i++) {
            Vector3 target = Vector3.atEntityCenter(trigger);
            Vector3 pos = target.clone().add(Vector3.random().normalize().mul(3 + random.nextFloat()));

            EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                    .spawn(pos)
                    .alpha1arg(VFXAlphaFunction.PYRAMID.andThen(VFXAlphaFunction.proximity(target::clone, 2)))
                    .motion(VFXMotionController.target(target::clone, 0.1F))
                    .setScaleMultiplier(0.15F + random.nextFloat() * 0.1F)
                    .color(VFXColorFunction.WHITE)
                    .setMaxAge(20 + random.nextInt(20));
        }
    }
}

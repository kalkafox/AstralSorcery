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
import hellfirepvp.astralsorcery.client.effect.function.VFXMotionController;
import hellfirepvp.astralsorcery.client.effect.handler.EffectHelper;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.common.crafting.helper.ingredient.CrystalIngredient;
import hellfirepvp.astralsorcery.common.crystal.CrystalAttributeItem;
import hellfirepvp.astralsorcery.common.crystal.CrystalAttributes;
import hellfirepvp.astralsorcery.common.crystal.CrystalGenerator;
import hellfirepvp.astralsorcery.common.data.config.entry.CraftingConfig;
import hellfirepvp.astralsorcery.common.item.ItemStardust;
import hellfirepvp.astralsorcery.common.item.crystal.ItemCrystalBase;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.lib.ItemsAS;
import hellfirepvp.astralsorcery.common.tile.TileCelestialCrystals;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.*;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: FormCelestialCrystalClusterRecipe
 * Created by HellFirePvP
 * Date: 01.10.2019 / 21:32
 */
public class FormCelestialCrystalClusterRecipe extends LiquidStarlightRecipe {

    public FormCelestialCrystalClusterRecipe() {
        super(AstralSorcery.key("form_celestial_crystal_cluster"));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Ingredient> getInputForRender() {
        return Arrays.asList(Ingredient.fromStacks(new ItemStack(ItemsAS.STARDUST)),
                new CrystalIngredient(false, false).toVanilla());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Ingredient> getOutputForRender() {
        return Collections.singletonList(Ingredient.fromStacks(new ItemStack(BlocksAS.CELESTIAL_CRYSTAL_CLUSTER)));
    }

    @Override
    public boolean doesStartRecipe(ItemStack item) {
        if (!CraftingConfig.CONFIG.liquidStarlightFormCelestialCrystalCluster.get()) {
            return false;
        }
        return item.getItem() instanceof ItemStardust;
    }

    @Override
    public boolean matches(ItemEntity trigger, Level level, BlockPos at) {
        if (!level.getBlockState(at.below()).isTopSolid(level, at.below(), trigger, Direction.UP)) {
            return false;
        }
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
        if (!level.isClientSide() && getAndIncrementCraftingTick(trigger) > 50 + r.nextInt(20)) {
            ItemStack crystalFound;
            if (consumeItemEntityInBlock(level, at, ItemsAS.STARDUST) != null &&
                    (crystalFound = consumeItemEntityInBlock(level, at, 1, stack -> stack.getItem() instanceof ItemCrystalBase)) != null) {

                if (level.setBlock(at, BlocksAS.CELESTIAL_CRYSTAL_CLUSTER.defaultBlockState())) {
                    TileCelestialCrystals cluster = MiscUtils.getTileAt(level, at, TileCelestialCrystals.class, true);
                    if (cluster != null) {
                        CrystalAttributes attr = ((CrystalAttributeItem) crystalFound.getItem()).getAttributes(crystalFound);
                        ItemStack targetCrystal = new ItemStack(ItemsAS.CELESTIAL_CRYSTAL);
                        ((CrystalAttributeItem) crystalFound.getItem()).setAttributes(targetCrystal, attr);
                        cluster.setAttributes(CrystalGenerator.upgradeProperties(targetCrystal));
                    }
                }
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void doClientEffectTick(ItemEntity trigger, Level level, BlockPos at) {
        for (int i = 0; i < 3; i++) {
            Vector3 pos = Vector3.atEntityCorner(trigger);
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
                    .setMaxAge(30 + random.nextInt(20));
        }
        for (int i = 0; i < 4; i++) {
            Vector3 target = Vector3.atEntityCorner(trigger);
            Vector3 pos = target.clone().add(Vector3.random().normalize().mul(3 + random.nextFloat()));

            EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                    .spawn(pos)
                    .alpha1arg(VFXAlphaFunction.PYRAMID.andThen(VFXAlphaFunction.proximity(target::clone, 2)))
                    .motion(VFXMotionController.target(target::clone, 0.1F))
                    .setScaleMultiplier(0.15F + random.nextFloat() * 0.1F)
                    .setMaxAge(20 + random.nextInt(20));
        }
    }
}

/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.tile;

import hellfirepvp.astralsorcery.client.effect.function.VFXAlphaFunction;
import hellfirepvp.astralsorcery.client.effect.function.VFXColorFunction;
import hellfirepvp.astralsorcery.client.effect.handler.EffectHelper;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.common.constellation.world.DayTimeHelper;
import hellfirepvp.astralsorcery.common.crafting.recipe.WellLiquefaction;
import hellfirepvp.astralsorcery.common.crafting.recipe.WellLiquefactionContext;
import hellfirepvp.astralsorcery.common.crystal.CrystalAttributeItem;
import hellfirepvp.astralsorcery.common.crystal.CrystalAttributes;
import hellfirepvp.astralsorcery.common.entity.EntityFlare;
import hellfirepvp.astralsorcery.common.fluid.BlockLiquidStarlight;
import hellfirepvp.astralsorcery.common.fluid.FluidLiquidStarlight;
import hellfirepvp.astralsorcery.common.lib.RecipeTypesAS;
import hellfirepvp.astralsorcery.common.lib.TileEntityTypesAS;
import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.play.server.PktPlayEffect;
import hellfirepvp.astralsorcery.common.tile.base.network.TileReceiverBase;
import hellfirepvp.astralsorcery.common.tile.network.StarlightReceiverWell;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import hellfirepvp.astralsorcery.common.util.sound.SoundHelper;
import hellfirepvp.astralsorcery.common.util.tile.FluidTankAccess;
import hellfirepvp.astralsorcery.common.util.tile.PrecisionSingleFluidTank;
import hellfirepvp.astralsorcery.common.util.tile.TileInventoryFiltered;
import hellfirepvp.astralsorcery.common.util.world.SkyCollectionHelper;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.WorldGenLevel;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.fluids.FluidType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.FluidType;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: TileWell
 * Created by HellFirePvP
 * Date: 30.06.2019 / 21:53
 */
public class TileWell extends TileReceiverBase<StarlightReceiverWell> {

    private static final int TANK_SIZE = 2 * FluidType.BUCKET_VOLUME;

    private WellLiquefaction runningRecipe = null;

    private FluidTankAccess access;
    private PrecisionSingleFluidTank tank;

    private TileInventoryFiltered inventory;
    private double starlightBuffer = 0;
    private float posDistribution = -1;

    public TileWell(BlockPos pos, BlockState state) {
        super(TileEntityTypesAS.WELL, pos, state);

        this.tank = new PrecisionSingleFluidTank(TANK_SIZE);
        this.tank.setAllowInput(false);
        this.tank.addUpdateFunction(this::markForUpdate);
        this.access = new FluidTankAccess();
        this.access.putTank(0, tank, Direction.DOWN);

        this.inventory = new TileInventoryFiltered(this, () -> 1, Direction.DOWN);
        this.inventory.filterMaxStackSize((slot, stack) -> 1);
        this.inventory.canExtract((slot, amount, existing) -> false);
        this.inventory.updateType((slot, toAdd, existing) -> {
            if (toAdd.isEmpty()) {
                return true;
            }
            return existing.isEmpty() && RecipeTypesAS.TYPE_WELL.findRecipe(new WellLiquefactionContext(toAdd)) != null;
        });
    }

    @Override
    public void tick() {
        super.tick();

        if (!getLevel().isClientSide()) {
            if (this.doesSeeSky()) {
                this.collectStarlight();
            }

            ItemStack stack = this.getItems().getStackInSlot(0);
            if (!stack.isEmpty()) {
                if (!getLevel().isEmptyBlock(getBlockPos().above())) {
                    breakCatalyst();
                } else {
                    if (runningRecipe == null) {
                        runningRecipe = RecipeTypesAS.TYPE_WELL.findRecipe(new WellLiquefactionContext(this));
                    }

                    if (runningRecipe != null) {
                        int statMultiplier = 1;
                        if (stack.getItem() instanceof CrystalAttributeItem) {
                            CrystalAttributes attributes = ((CrystalAttributeItem) stack.getItem()).getAttributes(stack);
                            if (attributes != null) {
                                statMultiplier = attributes.getTotalTierLevel();
                            }
                        }
                        double gain = Math.sqrt(starlightBuffer) * (statMultiplier * runningRecipe.getProductionMultiplier());
                        if (gain > 0 && tank.getFluidAmount() <= TANK_SIZE) {

                            fillAndDiscardRest(runningRecipe, gain);
                            if (random.nextInt(750) == 0) {
                                EntityFlare.spawnAmbientFlare(getLevel(), getBlockPos().offset(-3 + random.nextInt(7), 1, -3 + random.nextInt(7)));
                            }
                        }
                        starlightBuffer = 0;
                        if (random.nextInt(1 + (int) (1000 * (statMultiplier * runningRecipe.getShatterMultiplier()))) == 0) {
                            breakCatalyst();
                            EntityFlare.spawnAmbientFlare(getLevel(), getBlockPos().offset(-3 + random.nextInt(7), 1, -3 + random.nextInt(7)));
                        }
                    } else {
                        breakCatalyst();
                    }
                }
            }
            this.starlightBuffer = 0;
        } else {
            doClientEffects();
        }
    }

    private void fillAndDiscardRest(WellLiquefaction recipe, double gain) {
        Fluid produced = recipe.getFluidOutput();
        if (produced == null) {
            return;
        }

        if (tank.getFluidAmount() < 10) { //Fix fluids never changing on "empty" wells
            tank.setFluid(produced);
        }

        if (tank.getType().isEmpty()) {
            tank.setFluid(produced);
        } else if (!produced.equals(tank.getType().getFluid())) {
            return;
        }
        tank.addAmount(gain);
    }

    public void breakCatalyst() {
        this.inventory.setStackInSlot(0, ItemStack.EMPTY);
        this.runningRecipe = null;

        PktPlayEffect effect = new PktPlayEffect(PktPlayEffect.Type.SMALL_CRYSTAL_BREAK)
                .addData(buf -> ByteBufUtils.writeVector(buf, new Vector3(this).add(0.5, 1.3, 0.5)));
        PacketChannel.CHANNEL.sendToAllAround(effect, PacketChannel.pointFromPos(getLevel(), getBlockPos(), 32));

        SoundHelper.playSoundAround(SoundEvents.GLASS_BREAK, getLevel(), getBlockPos(), 1F, 1F);
        markForUpdate();
    }

    @Nonnull
    public ItemStack getCatalyst() {
        return this.getItems().getStackInSlot(0);
    }

    @OnlyIn(Dist.CLIENT)
    private void doClientEffects() {
        ItemStack stack = this.inventory.getStackInSlot(0);
        if (!stack.isEmpty()) {
            runningRecipe = RecipeTypesAS.TYPE_WELL.findRecipe(new WellLiquefactionContext(this));

            if (runningRecipe != null) {
                Color color = Color.WHITE;
                if (runningRecipe.getCatalystColor() != null) {
                    color = runningRecipe.getCatalystColor();
                }
                doCatalystEffect(color);
            }
        }
        if (tank.getFluidAmount() > 0 && tank.getType().getFluid() instanceof FluidLiquidStarlight) {
            BlockLiquidStarlight.playLiquidStarlightBlockEffect(random,
                    new Vector3(this).add(0, 0.4 + tank.getPercentageFilled() * 0.5, 0),
                    0.7F);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void doCatalystEffect(Color color) {
        if (random.nextInt(6) == 0) {
            Vector3 at = new Vector3(this)
                    .add(0.5, 1, 0.5)
                    .add(random.nextFloat() * 0.15 * (random.nextBoolean() ? 1 : -1),
                            random.nextFloat() * 0.2,
                            random.nextFloat() * 0.15 * (random.nextBoolean() ? 1 : -1));

            EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                    .spawn(at)
                    .alpha1arg(VFXAlphaFunction.FADE_OUT)
                    .color(VFXColorFunction.constant(color))
                    .setMaxAge(25 + random.nextInt(20));
        }
    }

    private void collectStarlight() {
        double sbDayDistribution = DayTimeHelper.getCurrentDaytimeDistribution(level);
        sbDayDistribution = 0.3 + (0.7 * sbDayDistribution);
        int yLevel = getBlockPos().getY();
        float dstr;
        if (yLevel > 120) {
            dstr = 1F;
        } else {
            dstr = yLevel / 120F;
        }
        if (posDistribution == -1) {
            if (level instanceof WorldGenLevel) {
                posDistribution = SkyCollectionHelper.getSkyNoiseDistribution((WorldGenLevel) level, getBlockPos());
            } else {
                posDistribution = 0.3F;
            }
        }

        sbDayDistribution *= dstr;
        sbDayDistribution *= 1 + (1.2 * posDistribution);
        starlightBuffer += Math.max(0.0001, sbDayDistribution);
    }

    public void receiveStarlight(double amount) {
        this.starlightBuffer += amount;
        this.markForUpdate();
    }

    @Nonnull
    public PrecisionSingleFluidTank getTank() {
        return tank;
    }

    @Nonnull
    public TileInventoryFiltered getItems() {
        return inventory;
    }

    @Nonnull
    @Override
    public StarlightReceiverWell provideEndpoint(BlockPos at) {
        return new StarlightReceiverWell(at);
    }

    @Override
    public void readCustomNBT(CompoundTag pattern, HolderLookup.Provider registries) {
        super.readCustomNBT(pattern, registries);

        this.tank.load(pattern.getCompound("tank"));
        this.inventory = this.inventory.deserialize(pattern.getCompound("inventory"));
    }

    @Override
    public void writeCustomNBT(CompoundTag pattern, HolderLookup.Provider registries) {
        super.writeCustomNBT(pattern, registries);

        pattern.put("tank", this.tank.save());
        pattern.put("inventory", this.inventory.serialize());
    }

    @Nullable
    public IFluidHandler getExposedFluidHandler(@Nullable Direction side) {
        return this.access.getFluidHandler(side);
    }

    @Nullable
    public IItemHandler getExposedItemHandler(@Nullable Direction side) {
        return this.inventory.getItemHandler(side);
    }
}

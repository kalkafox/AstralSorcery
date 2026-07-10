/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting.recipe.infusion;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.client.effect.function.RefreshFunction;
import hellfirepvp.astralsorcery.client.effect.function.VFXAlphaFunction;
import hellfirepvp.astralsorcery.client.effect.function.VFXColorFunction;
import hellfirepvp.astralsorcery.client.effect.function.VFXMotionController;
import hellfirepvp.astralsorcery.client.effect.handler.EffectHelper;
import hellfirepvp.astralsorcery.client.effect.source.orbital.FXOrbitalInfuserLiquid;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.client.util.ColorizationHelper;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;
import hellfirepvp.astralsorcery.common.auxiliary.ChaliceHelper;
import hellfirepvp.astralsorcery.common.crafting.recipe.LiquidInfusion;
import hellfirepvp.astralsorcery.common.data.research.ResearchManager;
import hellfirepvp.astralsorcery.common.tile.TileChalice;
import hellfirepvp.astralsorcery.common.tile.TileInfuser;
import hellfirepvp.astralsorcery.common.util.ColorUtils;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.RecipeHelper;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import hellfirepvp.astralsorcery.common.util.item.ItemUtils;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import hellfirepvp.astralsorcery.common.util.Constants;
import net.neoforged.neoforge.fluids.FluidAttributes;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.util.LogicalSidedProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ActiveLiquidInfusionRecipe
 * Created by HellFirePvP
 * Date: 09.11.2019 / 11:24
 */
public class ActiveLiquidInfusionRecipe {

    private static final Random random = new Random();
    private static final int CHALICE_DISTANCE = 8;

    private final LiquidInfusion recipeToCraft;
    private final UUID playerCraftingUUID;

    private int ticksCrafting = 0;
    private final Set<BlockPos> supportingChalices = new HashSet<>();
    private CompoundTag craftingData = new CompoundTag();

    private Object orbitalLiquid = null;

    public ActiveLiquidInfusionRecipe(Level level, BlockPos center, LiquidInfusion recipeToCraft, UUID playerCraftingUUID) {
        this(recipeToCraft, playerCraftingUUID);

        if (this.recipeToCraft.acceptsChaliceInput()) {
            this.findChalices(level, center);
        }
    }

    private ActiveLiquidInfusionRecipe(LiquidInfusion recipeToCraft, UUID playerCraftingUUID) {
        this.recipeToCraft = recipeToCraft;
        this.playerCraftingUUID = playerCraftingUUID;
    }

    private void findChalices(Level level, BlockPos center) {
        ChaliceHelper.findNearbyChalicesCombined(level, center, this.getChaliceRequiredFluidInput(), CHALICE_DISTANCE)
                .ifPresent(chalices -> chalices.forEach(chalice -> this.supportingChalices.add(chalice.getBlockPos())));
    }

    public boolean matches(TileInfuser infuser) {
        if (!this.getRecipeToCraft().matches(infuser, this.tryGetCraftingPlayerServer(), LogicalSide.SERVER)) {
            return false;
        }

        if (!this.supportingChalices.isEmpty()) {
            if (!ChaliceHelper.doChalicesContainCombined(infuser.getLevel(), this.supportingChalices, this.getChaliceRequiredFluidInput())) {
                this.supportingChalices.clear();
            }
        }
        return true;
    }

    public void tick() {
        this.ticksCrafting++;
    }

    @OnlyIn(Dist.CLIENT)
    public void tickClient(TileInfuser infuser) {
        FluidStack required = new FluidStack(this.getRecipeToCraft().getLiquidInput(), FluidAttributes.BUCKET_VOLUME);

        if (orbitalLiquid == null || ((FXOrbitalInfuserLiquid) orbitalLiquid).isRemoved()) {
            ResourceLocation recipeName = this.getRecipeToCraft().getId();

            orbitalLiquid = EffectHelper.spawnSource(
                    new FXOrbitalInfuserLiquid(new Vector3(infuser).add(0.5F, 0, 0.5F), required)
                            .setOrbitAxis(Vector3.RotAxis.Y_AXIS)
                            .setOrbitRadius(2F)
                            .setBranches(4)
                            .setMaxAge(300)
                            .refresh(RefreshFunction.tileExistsAnd(infuser,
                                    (tInfuser, fx) -> tInfuser.getActiveRecipe() != null &&
                                            recipeName.equals(tInfuser.getActiveRecipe().getRecipeToCraft().getId()))));
            ((FXOrbitalInfuserLiquid) orbitalLiquid).setActive();
        }

        for (int i = 0; i < 2; i++) {
            playLiquidEffect(infuser, required);
        }

        for (int i = 0; i < 7; i++) {
            playLiquidPoolEffect(infuser, required);
        }

        if (!this.supportingChalices.isEmpty()) {
            playLiquidDrawEffect(infuser, required);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void playLiquidDrawEffect(TileInfuser infuser, FluidStack required) {
        Collection<BlockPos> chalices = this.supportingChalices;
        if (chalices.isEmpty()) {
            return;
        }
        Vector3 target = new Vector3(infuser).add(0.5, 1.1, 0.5);
        TextureAtlasSprite tas = RenderingUtils.getParticleIcon(required);
        VFXColorFunction<?> colorFn = (fx, pTicks) -> new Color(ColorUtils.getOverlayColor(required));
        for (int i = 0; i < 2 * this.supportingChalices.size(); i++) {
            BlockPos chalice = MiscUtils.getRandomEntry(chalices, random);
            Vector3 pos = new Vector3(chalice).add(0.5, 1.4, 0.5);

            int lifetime = 30;
            lifetime *= Math.max(pos.distance(target) / 3, 1);

            if (random.nextInt(3) != 0) {
                MiscUtils.applyRandomOffset(pos, random, 0.3F);
                EffectHelper.of(EffectTemplatesAS.GENERIC_ATLAS_PARTICLE)
                        .spawn(pos)
                        .pickSprite(tas)
                        .selectFraction(0.2F)
                        .setScaleMultiplier(0.01F + random.nextFloat() * 0.04F)
                        .color(colorFn)
                        .alpha1arg(VFXAlphaFunction.proximity(() -> target, 2F).andThen(VFXAlphaFunction.FADE_OUT))
                        .motion(VFXMotionController.target(target::clone, 0.08F))
                        .setMaxAge(lifetime);
            } else {
                MiscUtils.applyRandomOffset(pos, random, 0.4F);
                EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                        .spawn(pos)
                        .setScaleMultiplier(0.15F + random.nextFloat() * 0.1F)
                        .color(colorFn)
                        .alpha1arg(VFXAlphaFunction.proximity(() -> target, 2F).andThen(VFXAlphaFunction.FADE_OUT))
                        .motion(VFXMotionController.target(target::clone, 0.08F))
                        .setMaxAge(lifetime);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void playLiquidEffect(TileInfuser infuser, FluidStack required) {
        Vector3 vec = infuser.getRandomInfuserOffset();
        MiscUtils.applyRandomOffset(vec, random, 0.05F);
        EffectHelper.of(EffectTemplatesAS.GENERIC_ATLAS_PARTICLE)
                .spawn(vec)
                .pickSprite(RenderingUtils.getParticleIcon(required))
                .selectFraction(0.2F)
                .setScaleMultiplier(0.03F + random.nextFloat() * 0.03F)
                .color((fx, pTicks) -> new Color(ColorUtils.getOverlayColor(required)))
                .alpha1arg(VFXAlphaFunction.FADE_OUT)
                .motion(VFXMotionController.target(() -> new Vector3(infuser).add(0.5, 1.1, 0.5), 0.3F))
                .setMaxAge(40);
    }

    @OnlyIn(Dist.CLIENT)
    private void playLiquidPoolEffect(TileInfuser infuser, FluidStack required) {
        List<BlockPos> posList = TileInfuser.getLiquidOffsets().stream()
                .map(pos -> pos.offset(infuser.getBlockPos()))
                .collect(Collectors.toList());

        BlockPos at = MiscUtils.getRandomEntry(posList, random);
        if (at != null) {
            EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                    .spawn(new Vector3(at).add(random.nextFloat(), 1, random.nextFloat()))
                    .setScaleMultiplier(0.1F + random.nextFloat() * 0.15F)
                    .color((fx, pTicks) -> ColorizationHelper.getColor(required).orElse(Color.WHITE))
                    .setAlphaMultiplier(1F)
                    .alpha1arg(VFXAlphaFunction.FADE_OUT)
                    .setDeltaMovement(new Vector3(0, 0.15, 0))
                    .setGravityStrength(0.005F + random.nextFloat() * 0.008F);
        }
    }

    public void clearEffects() {
        if (orbitalLiquid != null) {
            //Only exists on client after all
            clearClientEffect();
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void clearClientEffect() {
        ((FXOrbitalInfuserLiquid) orbitalLiquid).requestRemoval();
    }

    public void createItemOutputs(TileInfuser infuser, Consumer<ItemStack> output) {
        Consumer<ItemStack> informer = stack -> ResearchManager.informCraftedInfuser(infuser, this, stack);

        ItemStack inputStack = infuser.getItemInput();
        Consumer<ItemStack> handleCrafted = informer.andThen(output);
        if (this.recipeToCraft.doesCopyNBTToOutputs()) {
            handleCrafted = ((Consumer<ItemStack>) stack -> stack.setRepairCost(inputStack.getTag())).andThen(handleCrafted);
        }
        handleCrafted.accept(this.getRecipeToCraft().getOutput(inputStack));
        this.getRecipeToCraft().onRecipeCompletion(infuser);
    }

    public void consumeInputs(TileInfuser infuser) {
        ItemUtils.decrementItem(infuser::getItemInput, infuser::setItemInput, infuser::dropItemOnTop);
    }

    public void consumeFluidsInput(TileInfuser infuser) {
        float chaliceSupplied = 0F;
        if (!this.supportingChalices.isEmpty()) {
            FluidStack required = this.getChaliceRequiredFluidInput();
            Optional<List<TileChalice>> chalices = ChaliceHelper.findNearbyChalicesCombined(infuser.getLevel(), infuser.getBlockPos(), required, CHALICE_DISTANCE);
            if (chalices.isPresent()) {
                FluidStack left = required.copy();
                for (TileChalice chalice : chalices.get()) {
                    left.shrink(chalice.getTank().drain(left, IFluidHandler.FluidAction.EXECUTE).getAmount());
                    if (left.isEmpty()) {
                        break;
                    }
                }
                if (left.isEmpty()) {
                    return; // Chalices provided fluid
                }
                chaliceSupplied = ((float) required.getAmount()) / left.getAmount();
            }
        }

        LiquidInfusion infusion = this.getRecipeToCraft();
        float chance = infusion.getConsumptionChance() * (1F - chaliceSupplied);
        if (infusion.doesConsumeMultipleFluids()) {
            for (BlockPos at : TileInfuser.getLiquidOffsets()) {
                if (random.nextFloat() < chance) {
                    infuser.getLevel().setBlock(at.offset(infuser.getBlockPos()), Blocks.AIR.defaultBlockState(), Constants.BlockFlags.DEFAULT_AND_RERENDER);
                }
            }
        } else {
            BlockPos at = MiscUtils.getRandomEntry(TileInfuser.getLiquidOffsets(), random).offset(infuser.getBlockPos());
            if (random.nextFloat() < chance) {
                infuser.getLevel().setBlock(at, Blocks.AIR.defaultBlockState(), Constants.BlockFlags.DEFAULT_AND_RERENDER);
            }
        }
    }

    public FluidStack getChaliceRequiredFluidInput() {
        int amount = Math.round(FluidAttributes.BUCKET_VOLUME * recipeToCraft.getConsumptionChance());
        amount *= 0.75; //Bonus for using chalices

        amount = recipeToCraft.doesConsumeMultipleFluids() ? amount * TileInfuser.getLiquidOffsets().size() : amount;
        return new FluidStack(this.getRecipeToCraft().getLiquidInput(), amount);
    }

    public int getTotalCraftingTime() {
        int averageTickTime = this.recipeToCraft.getCraftingTickTime();

        int fixTime = Math.round(averageTickTime * 0.25F);
        int chaliceTime = Math.round(averageTickTime * 0.75F);
        chaliceTime /= this.supportingChalices.size() + 1;

        return fixTime + chaliceTime;
    }

    public CompoundTag getCraftingData() {
        return craftingData;
    }

    public UUID getPlayerCraftingUUID() {
        return playerCraftingUUID;
    }

    public int getTicksCrafting() {
        return ticksCrafting;
    }

    public LiquidInfusion getRecipeToCraft() {
        return recipeToCraft;
    }

    public boolean isFinished() {
        return getTicksCrafting() >= getTotalCraftingTime();
    }

    @Nullable
    public Player tryGetCraftingPlayerServer() {
        MinecraftServer srv = LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER);
        return srv.getPlayerList().getPlayerByUUID(this.getPlayerCraftingUUID());
    }

    @Nullable
    public static ActiveLiquidInfusionRecipe deserialize(CompoundTag pattern, @Nullable ActiveLiquidInfusionRecipe prev) {
        RecipeManager mgr = RecipeHelper.getRecipeManager();
        if (mgr == null) {
            return null;
        }

        ResourceLocation recipeKey = ResourceLocation.parse(pattern.getString("recipeToCraft"));
        Optional<?> recipe = mgr.getRecipe(recipeKey);
        if (!recipe.isPresent() || !(recipe.get() instanceof LiquidInfusion)) {
            AstralSorcery.log.info("Recipe with unknown/invalid name found: " + recipeKey);
            return null;
        }
        LiquidInfusion altarRecipe = (LiquidInfusion) recipe.get();

        UUID uuidCraft = pattern.getUniqueId("playerCraftingUUID");
        int tick = pattern.getInt("ticksCrafting");
        ListTag chalices = pattern.getList("supportingChalices", Constants.NBT.TAG_COMPOUND);

        Set<BlockPos> chalicePositions = new HashSet<>();
        for (int i = 0; i < chalices.size(); i++) {
            CompoundTag tag = chalices.getCompound(i);
            chalicePositions.add(NBTHelper.readBlockPosFromNBT(tag));
        }

        ActiveLiquidInfusionRecipe task = new ActiveLiquidInfusionRecipe(altarRecipe, uuidCraft);
        task.ticksCrafting = tick;
        task.craftingData = pattern.getCompound("craftingData");
        task.supportingChalices.addAll(chalicePositions);

        if (prev != null && prev.orbitalLiquid != null) {
            task.orbitalLiquid = prev.orbitalLiquid;
        }
        return task;
    }

    @Nonnull
    public CompoundTag serialize() {
        ListTag chalicePositions = new ListTag();
        this.supportingChalices.forEach(pos -> chalicePositions.add(NBTHelper.writeBlockPosToNBT(pos, new CompoundTag())));

        CompoundTag pattern = new CompoundTag();
        pattern.putString("recipeToCraft", getRecipeToCraft().getId().toString());
        pattern.putUniqueId("playerCraftingUUID", getPlayerCraftingUUID());
        pattern.putInt("ticksCrafting", getTicksCrafting());
        pattern.put("craftingData", craftingData);
        pattern.put("supportingChalices", chalicePositions);
        return pattern;
    }
}

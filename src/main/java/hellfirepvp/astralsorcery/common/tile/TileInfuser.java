/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.tile;

import com.google.common.collect.ImmutableSet;
import hellfirepvp.astralsorcery.client.effect.function.VFXAlphaFunction;
import hellfirepvp.astralsorcery.client.effect.handler.EffectHelper;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;
import hellfirepvp.astralsorcery.client.util.sound.PositionedLoopSound;
import hellfirepvp.astralsorcery.common.crafting.recipe.LiquidInfusion;
import hellfirepvp.astralsorcery.common.crafting.recipe.LiquidInfusionContext;
import hellfirepvp.astralsorcery.common.crafting.recipe.infusion.ActiveLiquidInfusionRecipe;
import hellfirepvp.astralsorcery.common.entity.EntityFlare;
import hellfirepvp.astralsorcery.common.item.wand.WandInteractable;
import hellfirepvp.astralsorcery.common.lib.RecipeTypesAS;
import hellfirepvp.astralsorcery.common.lib.SoundsAS;
import hellfirepvp.astralsorcery.common.lib.StructureTypesAS;
import hellfirepvp.astralsorcery.common.lib.TileEntityTypesAS;
import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.play.server.PktPlayEffect;
import hellfirepvp.astralsorcery.common.structure.types.StructureType;
import hellfirepvp.astralsorcery.common.tile.base.TileEntityTick;
import hellfirepvp.astralsorcery.common.util.ColorUtils;
import hellfirepvp.astralsorcery.common.util.MapStream;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import hellfirepvp.astralsorcery.common.util.sound.CategorizedSoundEvent;
import hellfirepvp.astralsorcery.common.util.sound.SoundHelper;
import hellfirepvp.astralsorcery.common.util.tile.TileInventory;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.ForgeHooks;
import hellfirepvp.astralsorcery.common.util.Constants;
import net.neoforged.neoforge.fluids.FluidAttributes;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.fml.LogicalSide;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.fluids.FluidType;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: TileInfuser
 * Created by HellFirePvP
 * Date: 09.11.2019 / 11:18
 */
public class TileInfuser extends TileEntityTick implements WandInteractable {

    private static final Set<BlockPos> LIQUID_OFFSETS = ImmutableSet.of(
            new BlockPos( 1, -1,  2),
            new BlockPos( 0, -1,  2),
            new BlockPos(-1, -1,  2),

            new BlockPos( 1, -1, -2),
            new BlockPos( 0, -1, -2),
            new BlockPos(-1, -1, -2),

            new BlockPos( 2, -1,  1),
            new BlockPos( 2, -1,  0),
            new BlockPos( 2, -1, -1),

            new BlockPos(-2, -1,  1),
            new BlockPos(-2, -1,  0),
            new BlockPos(-2, -1, -1)
    );

    private TileInventory inventory;

    private ActiveLiquidInfusionRecipe activeRecipe = null;
    private Set<ResourceLocation> knownRecipes = new HashSet<>();

    private Object clientCraftSound = null;

    public TileInfuser() {
        super(TileEntityTypesAS.INFUSER);
        this.inventory = new TileInventory(this, () -> 1);
    }

    @Nullable
    @Override
    public StructureType getRequiredStructureType() {
        return StructureTypesAS.PTYPE_INFUSER;
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.getLevel().isClientSide()) {
            this.doCraftingCycle();
        } else {
            if (this.getActiveRecipe() != null) {
                this.getActiveRecipe().tickClient(this);
                this.doCraftSound();
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void doCraftSound() {
        if (SoundHelper.getSoundVolume(SoundSource.BLOCKS) > 0) {
            if (clientCraftSound == null || ((PositionedLoopSound) clientCraftSound).hasStoppedPlaying()) {
                CategorizedSoundEvent sound = SoundsAS.INFUSER_CRAFT_LOOP;

                clientCraftSound = SoundHelper.playSoundLoopFadeInClient(sound,
                        new Vector3(this).add(0.5, 0.5, 0.5),
                        1F,
                        1F,
                        false,
                        (s) -> isRemoved() ||
                                SoundHelper.getSoundVolume(SoundSource.BLOCKS) <= 0 ||
                                this.getActiveRecipe() == null)
                        .setFadeInTicks(30)
                        .setFadeOutTicks(20);
            }
        } else {
            clientCraftSound = null;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void finishCraftingEffects(PktPlayEffect pkt) {
        ResourceLocation recipeName = ByteBufUtils.readResourceLocation(pkt.getExtraData());
        BlockPos at = ByteBufUtils.readPos(pkt.getExtraData());

        Level level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }

        TileInfuser thisInfuser = MiscUtils.getTileAt(level, at, TileInfuser.class, false);
        if (thisInfuser != null) {
            Recipe<?> recipe = level.getRecipeManager().getRecipes(RecipeTypesAS.TYPE_INFUSION.getType()).get(recipeName);
            if (recipe instanceof LiquidInfusion) {
                FluidStack stack = new FluidStack(((LiquidInfusion) recipe).getLiquidInput(), FluidType.BUCKET_VOLUME);
                Vector3 pos = new Vector3(at).add(0.5, 1, 0.5);
                for (int i = 0; i < 30; i++) {
                    playLiquidFinish(pos, stack);
                }
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static void playLiquidFinish(Vector3 at, FluidStack stack) {
        Vector3 motion = new Vector3();
        MiscUtils.applyRandomOffset(motion, random, 0.05F);

        EffectHelper.of(EffectTemplatesAS.GENERIC_ATLAS_PARTICLE)
                .spawn(at)
                .pickSprite(RenderingUtils.getParticleIcon(stack))
                .selectFraction(0.2F)
                .setScaleMultiplier(0.02F + random.nextFloat() * 0.05F)
                .setDeltaMovement(motion)
                .color((fx, pTicks) -> new Color(ColorUtils.getOverlayColor(stack)))
                .alpha1arg(VFXAlphaFunction.FADE_OUT)
                .setMaxAge(40);
    }

    private void doCraftingCycle() {
        if (this.activeRecipe == null) {
            return;
        }

        if (!this.hasMultiblock() || !this.activeRecipe.matches(this)) {
            this.abortCrafting();
            return;
        }

        if (this.activeRecipe.isFinished()) {
            this.finishRecipe();
            return;
        }

        this.activeRecipe.tick();
        this.markForUpdate();
    }

    private void finishRecipe() {
        ResourceLocation recipeName = this.activeRecipe.getRecipeToCraft().getId();

        ForgeHooks.setCraftingPlayer(this.activeRecipe.tryGetCraftingPlayerServer());
        this.activeRecipe.createItemOutputs(this, this::dropItemOnTop);
        this.activeRecipe.consumeInputs(this);
        this.activeRecipe.consumeFluidsInput(this);
        ForgeHooks.setCraftingPlayer(null);
        this.abortCrafting();

        SoundHelper.playSoundAround(SoundsAS.INFUSER_CRAFT_FINISH, this.getLevel(), this.getBlockPos(), 1F, 1F);

        PktPlayEffect pkt = new PktPlayEffect(PktPlayEffect.Type.INFUSER_RECIPE_FINISH)
                .addData(buf -> {
                    ByteBufUtils.writeResourceLocation(buf, recipeName);
                    ByteBufUtils.writePos(buf, this.getBlockPos());
                });
        PacketChannel.CHANNEL.sendToAllAround(pkt, PacketChannel.pointFromPos(this.getLevel(), this.getBlockPos(), 32));

        EntityFlare.spawnAmbientFlare(getLevel(), getBlockPos().offset(-3 + random.nextInt(7), 1 + random.nextInt(3), -3 + random.nextInt(7)));

        this.knownRecipes.add(recipeName);
    }

    private void abortCrafting() {
        this.activeRecipe.clearEffects();
        this.activeRecipe = null;
        markForUpdate();
    }

    protected LiquidInfusion findRecipe(Player crafter) {
        return RecipeTypesAS.TYPE_INFUSION.findRecipe(new LiquidInfusionContext(this, crafter, LogicalSide.SERVER));
    }

    protected boolean startCrafting(LiquidInfusion recipe, Player crafter) {
        if (this.getActiveRecipe() != null) {
            return false;
        }

        this.activeRecipe = new ActiveLiquidInfusionRecipe(getLevel(), getBlockPos(), recipe, crafter.getUUID());
        markForUpdate();

        SoundHelper.playSoundAround(SoundsAS.INFUSER_CRAFT_START, SoundSource.BLOCKS, this.level, new Vector3(this).add(0.5, 0.5, 0.5), 1F, 1F);
        return true;
    }

    @Override
    public boolean onInteract(Level level, BlockPos pos, Player player, Direction direction, boolean sneak) {
        if (!level.isClientSide() && this.hasMultiblock() && !this.getItemInput().isEmpty()) {
            if (this.getActiveRecipe() != null) {
                if (this.getActiveRecipe().matches(this)) {
                    return true;
                }

                abortCrafting();
            }
            LiquidInfusion recipe = this.findRecipe(player);
            if (recipe != null) {
                this.startCrafting(recipe, player);
            }
            return true;
        }
        return false;
    }

    @Nonnull
    public ItemStack getItemInput() {
        return this.inventory.getStackInSlot(0);
    }

    @Nonnull
    public ItemStack setItemInput(@Nonnull ItemStack stack) {
        ItemStack prev = this.getItemInput();
        this.inventory.setStackInSlot(0, stack);
        return prev;
    }

    @Nullable
    public ActiveLiquidInfusionRecipe getActiveRecipe() {
        return activeRecipe;
    }

    @Nonnull
    public static Set<BlockPos> getLiquidOffsets() {
        return LIQUID_OFFSETS;
    }

    @Nonnull
    public Map<BlockPos, Fluid> getLiquids() {
        return MapStream.ofKeys(getLiquidOffsets(), pos -> getLevel().getFluidState(getBlockPos().offset(pos)).getType()).toMap();
    }

    @Nonnull
    public Vector3 getRandomInfuserOffset() {
        Vector3 vec = new Vector3(this).add(0, 0.8, 0);
        switch (random.nextInt(4)) {
            case 3:
                vec.add(0.5, 0, 0.875);
                break;
            case 2:
                vec.add(0.5, 0, 0.125);
                break;
            case 1:
                vec.add(0.125, 0, 0.5);
                break;
            case 0:
                vec.add(0.875, 0, 0.5);
                break;
        }
        return vec;
    }

    @Override
    public void readCustomNBT(CompoundTag pattern) {
        super.readCustomNBT(pattern);

        this.inventory = this.inventory.deserialize(pattern.getCompound("inventory"));
        this.knownRecipes = NBTHelper.readSet(pattern, "knownRecipes", Constants.NBT.TAG_STRING, nbt -> ResourceLocation.parse(nbt.getString()));

        if (pattern.contains("activeRecipe", Constants.NBT.TAG_COMPOUND)) {
            this.activeRecipe = ActiveLiquidInfusionRecipe.deserialize(pattern.getCompound("activeRecipe"), this.activeRecipe);
        } else {
            if (this.activeRecipe != null) {
                this.activeRecipe.clearEffects();
            }
            this.activeRecipe = null;
        }
    }

    @Override
    public void writeCustomNBT(CompoundTag pattern) {
        super.writeCustomNBT(pattern);

        pattern.put("inventory", this.inventory.serialize());
        NBTHelper.writeList(pattern, "knownRecipes", this.knownRecipes, key -> StringTag.valueOf(key.toString()));

        if (this.activeRecipe != null) {
            pattern.put("activeRecipe", this.activeRecipe.serialize());
        }
    }

    @Nullable
    public IItemHandler getExposedItemHandler(@Nullable Direction side) {
        return this.inventory.getItemHandler(side);
    }
}

/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.item;

import com.google.common.collect.Lists;
import hellfirepvp.astralsorcery.client.data.config.entry.RenderingConfig;
import hellfirepvp.astralsorcery.client.effect.function.VFXColorFunction;
import hellfirepvp.astralsorcery.client.effect.handler.EffectHelper;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.client.util.AreaOfInfluencePreview;
import hellfirepvp.astralsorcery.common.CommonProxy;
import hellfirepvp.astralsorcery.common.constellation.world.DayTimeHelper;
import hellfirepvp.astralsorcery.common.data.research.ProgressionTier;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.item.base.OverrideInteractItem;
import hellfirepvp.astralsorcery.common.lib.CapabilitiesAS;
import hellfirepvp.astralsorcery.common.lib.ColorsAS;
import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.play.server.PktPlayEffect;
import hellfirepvp.astralsorcery.common.tile.base.TileAreaOfInfluence;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import hellfirepvp.astralsorcery.common.util.world.SkyCollectionHelper;
import hellfirepvp.astralsorcery.common.util.world.WorldSeedCache;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.NonNullList;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import hellfirepvp.astralsorcery.common.util.Constants;
import net.neoforged.neoforge.common.util.TriPredicate;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.fml.LogicalSide;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ItemResonator
 * Created by HellFirePvP
 * Date: 24.04.2020 / 20:30
 */
public class ItemResonator extends Item implements OverrideInteractItem {

    private static final java.util.Random random = new java.util.Random();

    public ItemResonator() {
        super(new Properties()
                .stacksTo(1)
);
    }

    @Override
    public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items) {
        if (this.isInGroup(group)) {
            ItemStack resonator = new ItemStack(this);
            setUpgradeUnlocked(resonator, ResonatorUpgrade.STARLIGHT);
            items.add(resonator);

            ItemStack upgradedResonator = new ItemStack(this);
            setUpgradeUnlocked(upgradedResonator, ResonatorUpgrade.values());
            items.add(upgradedResonator);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag extended) {
        ResonatorUpgrade current = getCurrentUpgrade(Minecraft.getInstance().player, stack);
        for (ResonatorUpgrade upgrade : getUpgrades(stack)) {
            ChatFormatting color = upgrade.equals(current) ? ChatFormatting.GOLD : ChatFormatting.BLUE;
            tooltip.add(Component.translatable(upgrade.getUnlocalizedTypeName()).withStyle(color));
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!selected) {
            selected = entity instanceof LivingEntity && ((LivingEntity) entity).getOffhandItem() == stack;
        }

        if (!level.isClientSide()) {
            if (selected && entity instanceof ServerPlayer) {
                ServerPlayer player = (ServerPlayer) entity;
                if (getCurrentUpgrade(player, stack) == ResonatorUpgrade.FLUID_FIELDS) {
                    float distribution = DayTimeHelper.getCurrentDaytimeDistribution(level);
                    if (distribution <= 1E-4) {
                        return;
                    }
                    if (random.nextFloat() < distribution && random.nextInt(12) == 0) {
                        int offsetX = random.nextInt(30) * (random.nextBoolean() ? 1 : -1);
                        int offsetZ = random.nextInt(30) * (random.nextBoolean() ? 1 : -1);

                        BlockPos pos = level.getHeight(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
                                new BlockPos(entity.getPosition()).offset(offsetX, 0, offsetZ));
                        if (pos.distSqr(entity.position()) > 5625) { // 75 blocks away
                            return;
                        }

                        ChunkAccess ch = level.getChunk(pos);
                        if (ch instanceof LevelChunk) {
                            Optional.ofNullable(((LevelChunk) ch).getData(CapabilitiesAS.CHUNK_FLUID)).ifPresent(entry -> {
                                FluidStack display = entry.drain(1, IFluidHandler.FluidAction.SIMULATE);
                                if (!display.isEmpty()) {
                                    PktPlayEffect pkt = new PktPlayEffect(PktPlayEffect.Type.LIQUID_FOUNTAIN).addData(buf -> {
                                        ByteBufUtils.writeFluidStack(buf, display);
                                        ByteBufUtils.writeVector(buf, new Vector3(pos));
                                    });
                                    PacketChannel.CHANNEL.sendToAllAround(pkt, PacketChannel.pointFromPos(level, pos, 32));
                                }
                            });
                        }
                    }
                }
            }
        } else {
            clientInventoryTick(stack, level, entity, slot, selected);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void clientInventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!(entity instanceof Player)) {
            return;
        }
        Player player = (Player) entity;

        if (selected &&
                getCurrentUpgrade(player, stack) == ResonatorUpgrade.STARLIGHT &&
                WorldSeedCache.getSeedIfPresent(level.dimension()).isPresent()) {

            float distribution = DayTimeHelper.getCurrentDaytimeDistribution(level);
            if (distribution <= 1E-4) {
                return;
            }
            BlockPos center = player.position();
            int offsetX = center.getX();
            int offsetZ = center.getZ();
            BlockPos.Mutable mPos = new BlockPos.Mutable();
            int minY = RenderingConfig.CONFIG.minYFosicDisplay.get();

            for (int xx = -48; xx <= 48; xx++) {
                for (int zz = -48; zz <= 48; zz++) {
                    mPos.setPos(level.getHeight(Heightmap.Type.WORLD_SURFACE, mPos.setPos(offsetX + xx, 0, offsetZ + zz)));
                    mPos.setY(Math.max(mPos.getY(), minY));

                    float perc = SkyCollectionHelper.getSkyNoiseDistributionClient(level.dimension(), mPos).get();

                    float fPerc = (float) Math.pow((perc - 0.4F) * 1.65F, 2);
                    if (perc >= 0.4F && random.nextFloat() <= fPerc) {
                        if (random.nextFloat() <= fPerc && random.nextInt(6) == 0) {

                            EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                                    .spawn(new Vector3(mPos).add(random.nextFloat(), 0.15, random.nextFloat()))
                                    .color(VFXColorFunction.constant(ColorsAS.RESONATOR_STARFIELD))
                                    .setScaleMultiplier(4F)
                                    .setAlphaMultiplier(distribution * fPerc);
                            if (perc >= 0.8F && random.nextInt(3) == 0) {

                                EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                                        .spawn(new Vector3(mPos).add(random.nextFloat(), 0.15, random.nextFloat()))
                                        .setScaleMultiplier(0.3F)
                                        .color(VFXColorFunction.WHITE)
                                        .setGravityStrength(-0.001F)
                                        .setAlphaMultiplier(distribution);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean shouldInterceptBlockInteract(LogicalSide direction, Player player, InteractionHand hand, BlockPos pos, Direction face) {
        ResonatorUpgrade upgrade = getCurrentUpgrade(player, player.getItemInHand(hand));
        return upgrade == ResonatorUpgrade.AREA_SIZE && MiscUtils.getTileAt(player.getCommandSenderWorld(), pos, TileAreaOfInfluence.class, false) != null;
    }

    @Override
    public boolean doBlockInteract(LogicalSide direction, Player player, InteractionHand hand, BlockPos pos, Direction face) {
        ResonatorUpgrade upgrade = getCurrentUpgrade(player, player.getItemInHand(hand));
        if (upgrade == ResonatorUpgrade.AREA_SIZE && player.getCommandSenderWorld().isClientSide()) {
            TileAreaOfInfluence aoeTile = MiscUtils.getTileAt(player.getCommandSenderWorld(), pos, TileAreaOfInfluence.class, false);
            if (aoeTile != null) {
                playAreaOfInfluenceEffect(aoeTile);
            }
        }
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    private void playAreaOfInfluenceEffect(TileAreaOfInfluence aoeTile) {
        AreaOfInfluencePreview.INSTANCE.showOrRemoveIdentical(aoeTile);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide() && player.isShiftKeyDown()) {
            if (cycleUpgrade(player, player.getItemInHand(hand))) {
                return InteractionResultHolder.success(player.getItemInHand(hand));
            }
        }
        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }

    public static boolean cycleUpgrade(@Nonnull Player player, ItemStack stack) {
        ResonatorUpgrade current = getCurrentUpgrade(player, stack);
        ResonatorUpgrade next = getNextSelectableUpgrade(player, stack);
        return next != null && !next.equals(current) && setCurrentUpgrade(player, stack, next);
    }

    @Nullable
    public static ResonatorUpgrade getNextSelectableUpgrade(@Nonnull Player viewing, ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemResonator)) {
            return null;
        }
        ResonatorUpgrade current = getCurrentUpgrade(viewing, stack);
        int currentOrd = current.ordinal();
        int test = currentOrd;
        do {
            test++;
            test %= ResonatorUpgrade.values().length;
            ResonatorUpgrade testUpgrade = ResonatorUpgrade.values()[test];
            if (testUpgrade.canSwitchTo(viewing, stack) && !testUpgrade.equals(current)) {
                return testUpgrade;
            }
        } while (test != currentOrd);
        return null;
    }

    public static boolean setCurrentUpgrade(Player setting, ItemStack stack, ResonatorUpgrade upgrade) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemResonator)) {
            return false;
        }
        if (upgrade.canSwitchTo(setting, stack)) {
            NBTHelper.getPersistentData(stack).putInt("selected_upgrade", upgrade.ordinal());
            return true;
        }
        return false;
    }

    public static ItemStack setCurrentUpgradeUnsafe(ItemStack stack, ResonatorUpgrade upgrade) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemResonator)) {
            return stack;
        }
        NBTHelper.getPersistentData(stack).putInt("selected_upgrade", upgrade.ordinal());
        return stack;
    }

    @Nonnull
    public static ResonatorUpgrade getCurrentUpgrade(@Nullable Player viewing, ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemResonator)) {
            return ResonatorUpgrade.STARLIGHT; //Fallback
        }
        CompoundTag cmp = NBTHelper.getPersistentData(stack);
        int current = cmp.getInt("selected_upgrade");
        ResonatorUpgrade upgrade = ResonatorUpgrade.values()[Mth.clamp(current, 0, ResonatorUpgrade.values().length - 1)];
        if (viewing != null) {
            if (!upgrade.canSwitchTo(viewing, stack)) {
                return ResonatorUpgrade.STARLIGHT;
            }
        }
        return upgrade;
    }

    public static ItemStack setUpgradeUnlocked(ItemStack stack, ResonatorUpgrade... upgrades) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemResonator)) {
            return stack;
        }
        for (ResonatorUpgrade upgrade : upgrades) {
            upgrade.applyUpgrade(stack);
        }
        return stack;
    }

    public static boolean hasUpgrade(ItemStack stack, ResonatorUpgrade upgrade) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemResonator)) {
            return false;
        }
        return upgrade.hasUpgrade(stack);
    }

    public static List<ResonatorUpgrade> getUpgrades(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemResonator)) {
            return Lists.newArrayList();
        }
        List<ResonatorUpgrade> upgrades = Lists.newLinkedList();
        for (ResonatorUpgrade ru : ResonatorUpgrade.values()) {
            if (ru.hasUpgrade(stack)) {
                upgrades.add(ru);
            }
        }
        return upgrades;
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        return getCurrentUpgrade(null, stack).getUnlocalizedItemName();
    }

    public static enum ResonatorUpgrade {

        STARLIGHT("starlight",
                (player, direction, stack) -> true),
        FLUID_FIELDS("liquid",
                (player, direction, stack) -> ResearchHelper.getProgress(player, direction).getTierReached().isThisLaterOrEqual(ProgressionTier.TRAIT_CRAFT)),
        AREA_SIZE("structure",
                (player, direction, stack) -> ResearchHelper.getProgress(player, direction).getTierReached().isThisLaterOrEqual(ProgressionTier.ATTUNEMENT));

        private final TriPredicate<Player, LogicalSide, ItemStack> check;
        private final String appendixUpgrade;

        private ResonatorUpgrade(String appendixUpgrade, TriPredicate<Player, LogicalSide, ItemStack> check) {
            this.check = check;
            this.appendixUpgrade = appendixUpgrade;
        }

        public String getAppendix() {
            return appendixUpgrade;
        }

        public String getUnlocalizedItemName() {
            return "item.astralsorcery.resonator." + this.appendixUpgrade;
        }

        public String getUnlocalizedTypeName() {
            return "item.astralsorcery.resonator.upgrade." + this.appendixUpgrade;
        }

        public boolean hasUpgrade(ItemStack stack) {
            int id = ordinal();
            CompoundTag cmp = NBTHelper.getPersistentData(stack);
            if (cmp.contains("upgrades", Constants.NBT.TAG_LIST)) {
                ListTag list = cmp.getList("upgrades", Constants.NBT.TAG_INT);
                for (int i = 0; i < list.size(); i++) {
                    if (list.getInt(i) == id) {
                        return true;
                    }
                }
            }
            return false;
        }

        public boolean canSwitchTo(@Nonnull Player player, ItemStack stack) {
            LogicalSide direction = player.level().isClientSide() ? LogicalSide.CLIENT : LogicalSide.SERVER;
            return hasUpgrade(stack) && check.test(player, direction, stack);
        }

        public void applyUpgrade(ItemStack stack) {
            if (hasUpgrade(stack)) return;

            CompoundTag cmp = NBTHelper.getPersistentData(stack);
            if (!cmp.contains("upgrades", Constants.NBT.TAG_LIST)) {
                cmp.put("upgrades", new ListTag());
            }
            ListTag list = cmp.getList("upgrades", Constants.NBT.TAG_INT);
            list.add(IntTag.valueOf(ordinal()));
        }
    }
}

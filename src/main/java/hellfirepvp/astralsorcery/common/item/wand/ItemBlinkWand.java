/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.item.wand;

import net.minecraft.network.chat.MutableComponent;

import net.minecraft.network.chat.Component;

import com.google.common.collect.Iterables;
import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.client.effect.function.VFXAlphaFunction;
import hellfirepvp.astralsorcery.client.effect.function.VFXColorFunction;
import hellfirepvp.astralsorcery.client.effect.handler.EffectHelper;
import hellfirepvp.astralsorcery.client.effect.vfx.FXFacingParticle;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.common.CommonProxy;
import hellfirepvp.astralsorcery.common.auxiliary.charge.AlignmentChargeHandler;
import hellfirepvp.astralsorcery.common.event.helper.EventHelperDamageCancelling;
import hellfirepvp.astralsorcery.common.item.armor.ItemMantle;
import hellfirepvp.astralsorcery.common.item.base.AlignmentChargeConsumer;
import hellfirepvp.astralsorcery.common.lib.ColorsAS;
import hellfirepvp.astralsorcery.common.lib.ConstellationsAS;
import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.play.server.PktShootEntity;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.RaytraceAssist;
import hellfirepvp.astralsorcery.common.util.block.BlockUtils;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.LogicalSide;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ItemBlinkWand
 * Created by HellFirePvP
 * Date: 01.03.2020 / 08:41
 */
public class ItemBlinkWand extends Item implements AlignmentChargeConsumer {

    private static final java.util.Random random = new java.util.Random();

    private static final float COST_PER_BLINK = 700F;
    private static final float COST_PER_DASH = 850F;

    public ItemBlinkWand() {
        super(new Properties()
                .stacksTo(1)
);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        tooltip.add(getBlinkMode(stack).getDisplay().withStyle(ChatFormatting.GOLD));
    }

    @Override
    public float getAlignmentChargeCost(Player player, ItemStack stack) {
        if (player.getCooldowns().isOnCooldown(this)) {
            return 0F;
        }
        if (getBlinkMode(stack) == BlinkMode.TELEPORT) {
            return COST_PER_BLINK;
        } else if (player.isUsingItem()) {
            ItemStack held = player.getUseItem();
            if (!held.isEmpty() && held.getItem() instanceof ItemBlinkWand) {
                int timeLeft = player.getUseItemRemainingTicks();
                float power = 0.2F + Math.min(1F, Math.min(50, stack.getUseDuration() - timeLeft) / 50F) * 0.8F;
                return COST_PER_DASH * power;
            }
        }
        return 0F;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            BlinkMode nextMode = getBlinkMode(held).next();
            setBlinkMode(held, nextMode);
            player.move(nextMode.getDisplay(), true);
        } else if (!player.getCooldowns().isOnCooldown(this)) {
            player.setActiveHand(hand);
        }
        return InteractionResultHolder.consume(held);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72_000;
    }

    @Override
    public void releaseUsing(ItemStack stack, Level worldIn, LivingEntity entityLiving, int timeLeft) {
        if (worldIn.isClientSide() || !(entityLiving instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player = (ServerPlayer) entityLiving;

        BlinkMode mode = getBlinkMode(stack);
        if (mode == BlinkMode.TELEPORT) {
            Vector3 origin = Vector3.atEntityCorner(player).addY(0.5F);
            Vector3 forwards = new Vector3(player.getLook(1F)).normalize().mul(40F).add(origin);
            List<BlockPos> blockLine = new ArrayList<>();
            RaytraceAssist rta = new RaytraceAssist(origin, forwards);
            rta.forEachBlockPos(pos -> {
                return MiscUtils.executeWithChunk(player.getCommandSenderWorld(), pos, () -> {
                    if (BlockUtils.isReplaceable(player.getCommandSenderWorld(), pos) && BlockUtils.isReplaceable(player.getCommandSenderWorld(), pos.above())) {
                        blockLine.add(pos);
                        return true;
                    }
                    return false;
                }, false);
            });

            if (!blockLine.isEmpty()) {
                BlockPos at = Iterables.last(blockLine);
                if (origin.distance(at) > 5) {
                    if (AlignmentChargeHandler.INSTANCE.drainCharge(player, LogicalSide.SERVER, COST_PER_BLINK, false)) {
                        player.setPositionAndUpdate(at.getX() + 0.5, at.getY(), at.getZ() + 0.5);
                        if (!player.isCreative()) {
                            player.getCooldowns().addCooldown(stack.getItem(), 40);
                        }
                    }
                }
            }
        } else if (mode == BlinkMode.LAUNCH) {
            float multiplier = 0.8F;
            if (!entityLiving.isFallFlying()) {
                multiplier = 2.4F;
            }
            float power = 0.2F + Math.min(1F, Math.min(50, stack.getUseDuration() - timeLeft) / 50F) * multiplier;
            if (power > 0.3F) {
                float chargeCost = COST_PER_DASH * 0.8F;
                if (AlignmentChargeHandler.INSTANCE.drainCharge(player, LogicalSide.SERVER, chargeCost, false)) {
                    Vector3 motion = new Vector3(player.getLook(1F)).normalize().mul(power * 3F);
                    if (motion.getY() > 0) {
                        motion.setY(Mth.clamp(motion.getY() + (0.2F * power), 0.2F * power, Float.MAX_VALUE));
                    }

                    player.setDeltaMovement(motion.toVector3d());
                    player.fallDistance = 0F;

                    if (ItemMantle.getEffect(player, ConstellationsAS.vicio) != null) {
                        AstralSorcery.getProxy().scheduleClientside(player::startFallFlying, 2);
                    }

                    PktShootEntity pkt = new PktShootEntity(player.getId(), motion);
                    pkt.setEffectLength(power);
                    PacketChannel.CHANNEL.sendToAllAround(pkt, PacketChannel.pointFromPos(worldIn, player.position(), 64));

                    if (!player.isFallFlying()) {
                        EventHelperDamageCancelling.markInvulnerableToNextDamage(player, DamageSource.FALL);
                    }
                }
            }
        }
    }

    @Override
    public void onUsingTick(ItemStack stack, LivingEntity entity, int count) {
        if (entity.getCommandSenderWorld().isClientSide()) {
            float perc = 0.2F + Math.min(1F, Math.min(50, stack.getUseDuration() - count) / 50F) * 0.8F;
            playUseParticles(stack, entity, count, perc);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void playUseParticles(ItemStack stack, LivingEntity entity, int useTicks, float usagePercent) {
        if (!(entity instanceof Player)) {
            return;
        }
        Player player = (Player) entity;
        if (player.getCooldowns().isOnCooldown(this)) {
            return;
        }
        if (getBlinkMode(stack) == BlinkMode.LAUNCH) {
            Vector3 forwards = new Vector3(entity.getLook(1F)).normalize().mul(20);
            Vector3 pos = Vector3.atEntityCorner(entity).addY(entity.getEyeHeight());
            Vector3 motion = forwards.clone().normalize().mul(-0.8F + random.nextFloat() * -0.5F);
            Vector3 perp = forwards.clone().perpendicular().normalize();

            for (int i = 0; i < Math.round(usagePercent * 6); i++) {
                float dst = i == 0 ? random.nextFloat() * 0.4F : 0.2F + random.nextFloat() * 0.4F;
                float speedModifier = i == 0 ? 0.005F : 0.5F + random.nextFloat() * 0.5F;
                float angleDeg = random.nextFloat() * 360F;

                Vector3 angle = perp.clone().mirror(angleDeg, forwards).normalize();
                Vector3 at = pos.clone()
                        .add(forwards.clone().mul(0.7F + random.nextFloat() * 0.3F))
                        .add(angle.clone().mul(dst));
                Vector3 mot = motion.clone().add(angle.clone().mul(0.1F + random.nextFloat() * 0.15F)).mul(speedModifier);

                FXFacingParticle p = EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                        .setOwner(entity.getUUID())
                        .spawn(at)
                        .setScaleMultiplier(0.3F + random.nextFloat() * 0.3F)
                        .setAlphaMultiplier(usagePercent)
                        .setDeltaMovement(mot)
                        .color(VFXColorFunction.constant(ColorsAS.CONSTELLATION_VICIO))
                        .setMaxAge(20 + random.nextInt(15));
                if (random.nextBoolean()) {
                    p.color(VFXColorFunction.WHITE);
                }
            }
        } else if (getBlinkMode(stack) == BlinkMode.TELEPORT) {
            Vector3 origin = Vector3.atEntityCorner(entity).addY(0.5F);
            Vector3 forwards = new Vector3(entity.getLook(1F)).normalize().mul(40F).add(origin);
            List<Vector3> lineState = new ArrayList<>();
            RaytraceAssist rta = new RaytraceAssist(origin, forwards);
            boolean clearLine = rta.forEachStep(v -> {
                BlockPos pos = v.toBlockPos();
                return MiscUtils.executeWithChunk(entity.getCommandSenderWorld(), pos, () -> {
                    if (BlockUtils.isReplaceable(entity.getCommandSenderWorld(), pos) && BlockUtils.isReplaceable(entity.getCommandSenderWorld(), pos.above())) {
                        lineState.add(v);
                        return true;
                    }
                    return false;
                }, false);
            });

            if (!lineState.isEmpty()) {
                Vector3 last = Iterables.last(lineState);

                for (Vector3 v : lineState) {
                    if (v == last || random.nextInt(300) == 0) {
                        VFXColorFunction<?> colorFn = VFXColorFunction.constant(ColorsAS.CONSTELLATION_VICIO);
                        float scale = 0.4F + random.nextFloat() * 0.2F;
                        float speedModifier = random.nextFloat() * 0.02F;
                        int age = 20 + random.nextInt(15);
                        if (random.nextInt(3) == 0) {
                            colorFn = VFXColorFunction.WHITE;
                        }
                        if (v == last) {
                            scale *= 1.5F;
                            speedModifier *= 4;
                            age *= 0.7F;
                            if (!clearLine) {
                                colorFn = VFXColorFunction.constant(ColorsAS.CONSTELLATION_AEVITAS);
                            } else {
                                colorFn = VFXColorFunction.constant(ColorsAS.CONSTELLATION_EVORSIO);
                            }
                            if (random.nextInt(5) == 0) {
                                colorFn = VFXColorFunction.WHITE;
                            }
                        }

                        EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                                .setOwner(entity.getUUID())
                                .spawn(v)
                                .setScaleMultiplier(scale)
                                .setAlphaMultiplier(usagePercent)
                                .alpha1arg(VFXAlphaFunction.FADE_OUT)
                                .setDeltaMovement(Vector3.random().normalize().mul(speedModifier))
                                .color(colorFn)
                                .setMaxAge(age);
                    }
                }
            }
        }
    }

    public static void setBlinkMode(@Nonnull ItemStack stack, @Nonnull BlinkMode mode) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemBlinkWand)) {
            return;
        }
        CompoundTag nbt = NBTHelper.getPersistentData(stack);
        nbt.putInt("blinkMode", mode.ordinal());
    }

    @Nonnull
    public static BlinkMode getBlinkMode(@Nonnull ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemBlinkWand)) {
            return BlinkMode.LAUNCH;
        }
        CompoundTag nbt = NBTHelper.getPersistentData(stack);
        return MiscUtils.getEnumEntry(BlinkMode.class, nbt.getInt("blinkMode"));
    }

    public static enum BlinkMode {

        LAUNCH("launch"),
        TELEPORT("teleport");

        private final String name;

        BlinkMode(String name) {
            this.name = name;
        }

        public MutableComponent getName() {
            return Component.translatable("astralsorcery.misc.blink.mode." + this.name);
        }

        public MutableComponent getDisplay() {
            return Component.translatable("astralsorcery.misc.blink.mode", this.getName());
        }

        @Nonnull
        private BlinkMode next() {
            int next = (this.ordinal() + 1) % values().length;
            return MiscUtils.getEnumEntry(BlinkMode.class, next);
        }
    }
}

/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.constellation.mantle;

import hellfirepvp.astralsorcery.common.registry.internal.AbstractAstralRegistryEntry;
import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.client.effect.function.VFXAlphaFunction;
import hellfirepvp.astralsorcery.client.effect.function.VFXColorFunction;
import hellfirepvp.astralsorcery.client.effect.handler.EffectHelper;
import hellfirepvp.astralsorcery.client.effect.vfx.FXFacingParticle;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.common.constellation.IWeakConstellation;
import hellfirepvp.astralsorcery.common.data.config.base.ConfigEntry;
import hellfirepvp.astralsorcery.common.event.PlayerAffectionFlags;
import hellfirepvp.astralsorcery.common.item.armor.ItemMantle;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import hellfirepvp.observerlib.common.util.tick.ITickHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.NeoForge;
import hellfirepvp.observerlib.common.util.tick.TickEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.LogicalSide;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.EnumSet;
import java.util.Random;
import java.util.function.Consumer;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: MantleEffect
 * Created by HellFirePvP
 * Date: 17.02.2020 / 20:13
 */
public abstract class MantleEffect extends AbstractAstralRegistryEntry<MantleEffect> implements ITickHandler {

    protected static final Random rand = new Random();

    private final PlayerAffectionFlags.AffectionFlag playerAffectionFlag;
    private final IWeakConstellation constellation;

    public MantleEffect(IWeakConstellation constellation) {
        this.constellation = constellation;
        this.setRegistryName(this.constellation.getRegistryName());
        this.playerAffectionFlag = new PlayerAffectionFlags.NoOpAffectionFlag(AstralSorcery.key("mantle_effect_" + constellation.getSimpleName()));

        this.attachEventListeners(NeoForge.EVENT_BUS);
        this.attachTickHandlers(AstralSorcery.getProxy().getTickManager()::register);
    }

    public final IWeakConstellation getAssociatedConstellation() {
        return this.constellation;
    }

    public abstract Config getConfig();

    public final PlayerAffectionFlags.AffectionFlag getPlayerAffectionFlag() {
        return playerAffectionFlag;
    }

    protected void attachEventListeners(IEventBus bus) {}

    protected void attachTickHandlers(Consumer<ITickHandler> registrar) {
        if (this.usesTickMethods()) {
            registrar.accept(this);
        }
    }

    protected void tickServer(Player player) {}

    @OnlyIn(Dist.CLIENT)
    protected void tickClient(Player player) {}

    protected boolean usesTickMethods() {
        return false;
    }

    @OnlyIn(Dist.CLIENT)
    protected void playCapeSparkles(Player player, float chance) {
        if (player == Minecraft.getInstance().player && Minecraft.getInstance().gameSettings.getPointOfView().func_243192_a()) {
            chance *= 0.1F;
        }
        if (rand.nextFloat() < chance) {
            Color c = this.getAssociatedConstellation().getConstellationColor();
            if (c != null) {
                float width = player.getWidth() * 0.8F;
                double x = player.getPosX() + rand.nextFloat() * width * (rand.nextBoolean() ? 1 : -1);
                double y = player.getPosY() + rand.nextFloat() * (player.getHeight() / 3);
                double z = player.getPosZ() + rand.nextFloat() * width * (rand.nextBoolean() ? 1 : -1);
                Vector3 pos = new Vector3(x, y, z);

                FXFacingParticle fx = this.spawnFacingParticle(player, pos)
                        .color(VFXColorFunction.constant(c))
                        .alpha(VFXAlphaFunction.FADE_OUT)
                        .setScaleMultiplier(0.4F + rand.nextFloat() * 0.4F)
                        .setMaxAge(20 + rand.nextInt(10));
                if (rand.nextInt(3) == 0) {
                    fx.color(VFXColorFunction.constant(this.getAssociatedConstellation().getTierRenderColor()));
                }

                if (rand.nextFloat() > 0.35F) {
                    this.spawnFacingParticle(player, pos)
                            .color(VFXColorFunction.WHITE)
                            .alpha(VFXAlphaFunction.FADE_OUT)
                            .setScaleMultiplier(0.2F + rand.nextFloat() * 0.2F)
                            .setMaxAge(10 + rand.nextInt(10));
                }
            }
        }
    }

    @Nonnull
    @OnlyIn(Dist.CLIENT)
    protected FXFacingParticle spawnFacingParticle(Player player, Vector3 at) {
        return EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                .setOwner(player.getUniqueID())
                .spawn(at);
    }

    @Override
    public final void tick(TickEvent.Type type, Object... context) {
        if (!this.getConfig().enabled.get()) {
            return;
        }

        Player pl = (Player) context[0];
        LogicalSide side = (LogicalSide) context[1];
        boolean hasMantle = ItemMantle.getEffect(pl, this.getAssociatedConstellation()) != null;
        if (!hasMantle) {
            return;
        }

        if (side.isServer()) {
            if (!(pl instanceof ServerPlayer) || MiscUtils.isPlayerFakeMP((ServerPlayer) pl)) {
                return;
            }
            PlayerAffectionFlags.markPlayerAffected(pl, this.playerAffectionFlag);
            this.tickServer(pl);
        } else {
            this.tickClient(pl);
        }
    }

    @Nonnull
    protected CompoundTag getData(LivingEntity entity) {
        if (entity == null) {
            return new CompoundTag();
        }
        ItemStack stack = entity.getItemStackFromSlot(EquipmentSlotType.CHEST);
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemMantle)) {
            return new CompoundTag();
        }
        return NBTHelper.getPersistentData(stack);
    }

    @Override
    public EnumSet<TickEvent.Type> getHandledTypes() {
        return EnumSet.of(TickEvent.Type.PLAYER);
    }

    @Override
    public boolean canFire(TickEvent.Phase phase) {
        return phase == TickEvent.Phase.END;
    }

    @Override
    public String getName() {
        return this.getClass().getName();
    }

    public static class Config extends ConfigEntry {

        private final boolean defaultEnabled = true;

        public ModConfigSpec.BooleanValue enabled;

        public Config(String constellationName) {
            super(String.format("constellation.mantle.%s", constellationName));
        }

        @Override
        public void createEntries(ModConfigSpec.Builder cfgBuilder) {
            this.enabled = cfgBuilder
                    .comment("Set this to false to disable this mantle effect")
                    .translation(translationKey("enabled"))
                    .define("enabled", this.defaultEnabled);
        }
    }
}

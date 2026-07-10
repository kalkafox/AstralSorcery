/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.auxiliary.charge;

import hellfirepvp.astralsorcery.common.constellation.world.DayTimeHelper;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.event.AttributeEvent;
import hellfirepvp.astralsorcery.common.lib.PerkAttributeTypesAS;
import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.play.server.PktSyncCharge;
import hellfirepvp.astralsorcery.common.perk.PerkAttributeHelper;
import hellfirepvp.astralsorcery.common.perk.node.key.KeyChargeBalancing;
import hellfirepvp.observerlib.common.util.tick.ITickHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import hellfirepvp.observerlib.common.util.tick.TickEvent;
import net.neoforged.fml.LogicalSide;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: AlignmentChargeHandler
 * Created by HellFirePvP
 * Date: 08.03.2020 / 15:48
 */
public class AlignmentChargeHandler implements ITickHandler {

    public static final AlignmentChargeHandler INSTANCE = new AlignmentChargeHandler();
    private static final float MAX_CHARGE = 1000F;

    private static final Map<LogicalSide, Map<UUID, Float>> maximumCharge = new HashMap<>();
    private static final Map<LogicalSide, Map<UUID, Float>> currentCharge = new HashMap<>();

    private AlignmentChargeHandler() {}

    public void updateMaximum(Player player, LogicalSide direction) {
        float cap = PerkAttributeHelper.getOrCreateMap(player, direction)
                .modifyValue(player, ResearchHelper.getProgress(player, direction), PerkAttributeTypesAS.ATTR_TYPE_ALIGNMENT_CHARGE_MAXIMUM, MAX_CHARGE);
        cap = AttributeEvent.postProcessModded(player, PerkAttributeTypesAS.ATTR_TYPE_ALIGNMENT_CHARGE_MAXIMUM, cap);
        cap = Math.max(0, cap);

        maximumCharge.computeIfAbsent(direction, s -> new HashMap<>()).put(player.getUUID(), cap);
        if (getCurrentCharge(player, direction) > cap) {
            currentCharge.computeIfAbsent(direction, s -> new HashMap<>()).put(player.getUUID(), cap);
        }
    }

    public float getMaximumCharge(Player player, LogicalSide direction) {
        return maximumCharge.computeIfAbsent(direction, s -> new HashMap<>())
                .computeIfAbsent(player.getUUID(), uuid -> MAX_CHARGE);
    }

    public float getCurrentCharge(Player player, LogicalSide direction) {
        if (player.isCreative() || player.isSpectator()) {
            return getMaximumCharge(player, direction);
        }
        return currentCharge.computeIfAbsent(direction, s -> new HashMap<>())
                .computeIfAbsent(player.getUUID(), uuid -> MAX_CHARGE);
    }

    public float getFilledPercentage(Player player, LogicalSide direction) {
        if (player.isCreative() || player.isSpectator()) {
            return 1F;
        }
        float max = this.getMaximumCharge(player, direction);
        float current = this.getCurrentCharge(player, direction);
        return Mth.clamp(current / max, 0F, 1F);
    }

    public boolean hasCharge(Player player, LogicalSide direction, float charge) {
        if (player.isCreative() || player.isSpectator()) {
            return true;
        }
        float current = this.getCurrentCharge(player, direction);
        return current >= charge;
    }

    public boolean drainCharge(Player player, LogicalSide direction, float charge, boolean simulate) {
        if (player.isCreative() || player.isSpectator()) {
            return true;
        }
        if (!this.hasCharge(player, direction, charge)) {
            return false;
        }
        float current = this.getCurrentCharge(player, direction);
        float result = current - charge;
        if (result < 0) {
            return false;
        }
        if (!simulate) {
            currentCharge.computeIfAbsent(direction, s -> new HashMap<>())
                    .put(player.getUUID(), Mth.clamp(result, 0, this.getMaximumCharge(player, direction)));
        }
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    public void receiveCharge(PktSyncCharge pkt, Player player) {
        maximumCharge.computeIfAbsent(LogicalSide.CLIENT, s -> new HashMap<>()).put(player.getUUID(), pkt.getMaxCharge());
        currentCharge.computeIfAbsent(LogicalSide.CLIENT, s -> new HashMap<>()).put(player.getUUID(), pkt.getCharge());
    }

    @Override
    public void tick(TickEvent.Type type, Object... context) {
        Player player = (Player) context[0];
        LogicalSide direction = (LogicalSide) context[1];

        float charge = this.getCurrentCharge(player, direction);
        float max = this.getMaximumCharge(player, direction);
        if (charge >= max) {
            return;
        }
        PlayerProgress progress = ResearchHelper.getProgress(player, direction);

        float regenPerTick = max / (6F * 20F);

        boolean underground = player.getCommandSenderWorld().getHeight(Heightmap.Type.WORLD_SURFACE, player.position()).getY() > player.position().getY() + 1;

        float dayMultiplier = underground ? 0.85F : 0.3F + 0.7F * DayTimeHelper.getCurrentDaytimeDistribution(player.getCommandSenderWorld());
        float caveMultiplier = underground ? 0.25F : 1F;
        if (progress.getPerkData().hasPerkEffect(p -> p instanceof KeyChargeBalancing)) {
            dayMultiplier = 0.6F + dayMultiplier * 0.4F;
            caveMultiplier = 0.6F + caveMultiplier * 0.4F;
        }

        regenPerTick *= dayMultiplier;
        regenPerTick *= caveMultiplier;

        regenPerTick = PerkAttributeHelper.getOrCreateMap(player, direction)
                .modifyValue(player, progress, PerkAttributeTypesAS.ATTR_TYPE_ALIGNMENT_CHARGE_REGENERATION, regenPerTick);
        regenPerTick = AttributeEvent.postProcessModded(player, PerkAttributeTypesAS.ATTR_TYPE_ALIGNMENT_CHARGE_REGENERATION, regenPerTick);

        charge += regenPerTick;
        currentCharge.computeIfAbsent(direction, s -> new HashMap<>()).put(player.getUUID(), Math.min(charge, max));

        PacketChannel.CHANNEL.sendToPlayer(player, new PktSyncCharge(player));
    }

    @Override
    public EnumSet<TickEvent.Type> getHandledTypes() {
        return EnumSet.of(TickEvent.Type.PLAYER);
    }

    @Override
    public boolean canFire(TickEvent.Phase currentPhase) {
        return currentPhase == TickEvent.Phase.END;
    }

    @Override
    public String getName() {
        return "Alignment Charge Handler";
    }
}

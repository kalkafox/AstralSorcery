/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.constellation.mantle.effect;

import hellfirepvp.astralsorcery.common.auxiliary.charge.AlignmentChargeHandler;
import hellfirepvp.astralsorcery.common.constellation.mantle.MantleEffect;
import hellfirepvp.astralsorcery.common.item.armor.ItemMantle;
import hellfirepvp.astralsorcery.common.lib.ConstellationsAS;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.LogicalSide;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: MantleEffectEvorsio
 * Created by HellFirePvP
 * Date: 31.03.2020 / 17:31
 */
public class MantleEffectEvorsio extends MantleEffect {

    public static EvorsioConfig CONFIG = new EvorsioConfig();

    public MantleEffectEvorsio() {
        super(ConstellationsAS.evorsio);
    }

    @Override
    protected void attachEventListeners(IEventBus bus) {
        super.attachEventListeners(bus);
        bus.addListener(EventPriority.LOWEST, this::onBreak);
    }

    private void onBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (ItemMantle.getEffect(player, ConstellationsAS.evorsio) != null) {
            LogicalSide direction = player.getCommandSenderWorld().isClientSide() ? LogicalSide.CLIENT : LogicalSide.SERVER;
            if (direction.isServer()) {
                float charge = Math.min(AlignmentChargeHandler.INSTANCE.getCurrentCharge(player, direction), CONFIG.chargeCostPerBreak.get());
                AlignmentChargeHandler.INSTANCE.drainCharge(player, direction, charge, false);
            }
        }
    }

    @Override
    protected boolean usesTickMethods() {
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    protected void tickClient(Player player) {
        super.tickClient(player);

        this.playCapeSparkles(player, 0.1F);
    }

    @Override
    public Config getConfig() {
        return CONFIG;
    }

    public static class EvorsioConfig extends Config {

        private final int defaultChargeCostPerBreak = 2;

        public ModConfigSpec.IntValue chargeCostPerBreak;

        public EvorsioConfig() {
            super("evorsio");
        }

        @Override
        public void createEntries(ModConfigSpec.Builder cfgBuilder) {
            super.createEntries(cfgBuilder);

            this.chargeCostPerBreak = cfgBuilder
                    .comment("Set the amount alignment charge consumed per block break enhanced by the mantle effect")
                    .translation(translationKey("chargeCostPerBreak"))
                    .defineInRange("chargeCostPerBreak", this.defaultChargeCostPerBreak, 0, 1000);
        }
    }
}

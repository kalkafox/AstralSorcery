/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.perk.reader;

import hellfirepvp.astralsorcery.common.event.AttributeEvent;
import hellfirepvp.astralsorcery.common.perk.PerkAttributeLimiter;
import hellfirepvp.astralsorcery.common.perk.PerkAttributeMap;
import hellfirepvp.astralsorcery.common.perk.type.AttributeTypeBreakSpeed;
import hellfirepvp.astralsorcery.common.perk.type.PerkAttributeType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.LogicalSide;
import org.apache.commons.lang3.tuple.Pair;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ReaderBreakSpeed
 * Created by HellFirePvP
 * Date: 25.08.2019 / 17:49
 */
public class ReaderBreakSpeed extends ReaderFlatAttribute {

    public ReaderBreakSpeed(PerkAttributeType type) {
        super(type, 1);
    }

    @Override
    public double getDefaultValue(PerkAttributeMap statMap, Player player, LogicalSide direction) {
        AttributeTypeBreakSpeed.evaluateBreakSpeedWithoutPerks = true;
        double speedModifier;
        try {
            speedModifier = player.getDigSpeed(Blocks.COBBLESTONE.defaultBlockState(), BlockPos.ZERO);
        } finally {
            AttributeTypeBreakSpeed.evaluateBreakSpeedWithoutPerks = false;
        }
        return speedModifier;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public PerkStatistic countParticles(PerkAttributeMap statMap, Player player) {
        String limitStr = "";
        Double limit = null;
        if (PerkAttributeLimiter.hasLimit(this.getType())) {
            Pair<Double, Double> limits = PerkAttributeLimiter.getMaxResults(this.getType());
            limit = limits.getRight();
            limitStr = I18n.format("perk.reader.astralsorcery.limit.percent", Mth.floor(limit * 100));
        }

        double value = player.getDigSpeed(Blocks.COBBLESTONE.defaultBlockState(), BlockPos.ZERO);

        String postProcess = "";
        double post = AttributeEvent.postProcessModded(player, this.getType(), value);
        if (Math.abs(value - post) > 1E-4 &&
                (limit == null || Math.abs(post - limit) > 1E-4)) {
            if (Math.abs(post) >= 1E-4) {
                postProcess = I18n.format("perk.reader.astralsorcery.postprocess.default",
                        (post >= 0 ? "+" : "") + formatDecimal(post));
            }
            value = post;
        }

        String strOut = (value >= 0 ? "+" : "") + formatDecimal(value);
        return new PerkStatistic(this.getType(), strOut, limitStr, postProcess);
    }
}

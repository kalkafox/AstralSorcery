/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.world.feature.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ReplaceBlockConfig
 * Created by HellFirePvP
 * Date: 20.11.2020 / 16:56
 */
public class ReplaceBlockConfig implements FeatureConfiguration {

    public static final Codec<ReplaceBlockConfig> CODEC = RecordCodecBuilder.create((codecInstance) -> {
        return codecInstance.group(RuleTest.CODEC.fieldOf("target").forGetter((config) -> {
            return config.target;
        }), BlockState.CODEC.fieldOf("state").forGetter((config) -> {
            return config.state;
        })).apply(codecInstance, ReplaceBlockConfig::new);
    });

    public final RuleTest target;
    public final BlockState state;

    public ReplaceBlockConfig(RuleTest target, BlockState state) {
        this.target = target;
        this.state = state;
    }
}

/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.advancement;

import com.mojang.serialization.Codec;
import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.advancement.instance.ConstellationInstance;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: AttuneCrystalTrigger
 * Created by HellFirePvP
 * Date: 27.10.2018 / 14:01
 */
public class AttuneCrystalTrigger extends SimpleCriterionTrigger<ConstellationInstance> {

    public static final ResourceLocation ID = AstralSorcery.key("attune_crystal");

    @Override
    public Codec<ConstellationInstance> codec() {
        return ConstellationInstance.CODEC;
    }

    public void trigger(ServerPlayer player, IConstellation cst) {
        this.trigger(player, (i) -> i.test(cst));
    }
}

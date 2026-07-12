/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.advancement.instance;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.constellation.IMajorConstellation;
import hellfirepvp.astralsorcery.common.constellation.IMinorConstellation;
import hellfirepvp.astralsorcery.common.constellation.IWeakConstellation;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ConstellationInstance
 * Created by HellFirePvP
 * Date: 11.05.2020 / 20:26
 */
public record ConstellationInstance(Optional<ContextAwarePredicate> player,
                                    boolean constellationMajor,
                                    boolean constellationWeak,
                                    boolean constellationMinor,
                                    List<ResourceLocation> constellations) implements SimpleCriterionTrigger.SimpleInstance {

    public static final Codec<ConstellationInstance> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(ConstellationInstance::player),
            Codec.BOOL.optionalFieldOf("major", false).forGetter(ConstellationInstance::constellationMajor),
            Codec.BOOL.optionalFieldOf("weak", false).forGetter(ConstellationInstance::constellationWeak),
            Codec.BOOL.optionalFieldOf("minor", false).forGetter(ConstellationInstance::constellationMinor),
            ResourceLocation.CODEC.listOf().optionalFieldOf("constellations", List.of()).forGetter(ConstellationInstance::constellations)
    ).apply(inst, ConstellationInstance::new));

    public static Criterion<ConstellationInstance> any(SimpleCriterionTrigger<ConstellationInstance> trigger) {
        return trigger.createCriterion(new ConstellationInstance(Optional.empty(), false, false, false, List.of()));
    }

    public static Criterion<ConstellationInstance> anyMajor(SimpleCriterionTrigger<ConstellationInstance> trigger) {
        return trigger.createCriterion(new ConstellationInstance(Optional.empty(), true, false, false, List.of()));
    }

    public static Criterion<ConstellationInstance> anyWeak(SimpleCriterionTrigger<ConstellationInstance> trigger) {
        return trigger.createCriterion(new ConstellationInstance(Optional.empty(), false, true, false, List.of()));
    }

    public static Criterion<ConstellationInstance> anyMinor(SimpleCriterionTrigger<ConstellationInstance> trigger) {
        return trigger.createCriterion(new ConstellationInstance(Optional.empty(), false, false, true, List.of()));
    }

    public static Criterion<ConstellationInstance> anyOf(SimpleCriterionTrigger<ConstellationInstance> trigger, IConstellation... cst) {
        return trigger.createCriterion(new ConstellationInstance(Optional.empty(), false, false, false,
                Arrays.stream(cst).map(IConstellation::getRegistryName).collect(Collectors.toList())));
    }

    public boolean test(IConstellation discovered) {
        if (this.constellationMajor && !(discovered instanceof IMajorConstellation)) {
            return false;
        }
        if (this.constellationWeak && (!(discovered instanceof IWeakConstellation) || discovered instanceof IMajorConstellation)) {
            return false;
        }
        if (this.constellationMinor && !(discovered instanceof IMinorConstellation)) {
            return false;
        }
        return this.constellations.isEmpty() || this.constellations.contains(discovered.getRegistryName());
    }
}

/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.constellation.engraving;

import hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry;
import hellfirepvp.astralsorcery.common.constellation.DrawnConstellation;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.constellation.star.StarLocation;
import hellfirepvp.astralsorcery.common.constellation.world.DayTimeHelper;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import hellfirepvp.astralsorcery.common.util.Constants;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.List;
import java.util.*;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: EngravedStarMap
 * Created by HellFirePvP
 * Date: 01.05.2020 / 07:31
 */
public class EngravedStarMap {

    private static final Random random = new Random();

    private final Map<ResourceLocation, Float> distributions;
    private final List<DrawnConstellation> drawInformation;

    private EngravedStarMap(Map<ResourceLocation, Float> distributions, List<DrawnConstellation> drawnConstellations) {
        this.distributions = distributions;
        this.drawInformation = drawnConstellations;
    }

    public static EngravedStarMap buildStarMap(Level level, List<DrawnConstellation> constellations) {
        float nightPerc = DayTimeHelper.getCurrentDaytimeDistribution(level);

        Map<DrawnConstellation, List<Rectangle.Double>> cstCoordinates = new HashMap<>();
        for (DrawnConstellation drawnCst : constellations) {
            cstCoordinates.put(drawnCst, createConstellationOffsets(drawnCst));
        }

        Map<ResourceLocation, Float> distributionMap = new HashMap<>();
        for (DrawnConstellation drawn : cstCoordinates.keySet()) {
            List<Rectangle.Double> positions = cstCoordinates.get(drawn);
            Set<Rectangle.Double> foundPositions = new HashSet<>();

            for (DrawnConstellation otherCst : cstCoordinates.keySet()) {
                if (drawn.equals(otherCst) || drawn.getConstellation().equals(otherCst.getConstellation())) {
                    continue;
                }

                List<Rectangle.Double> otherPositions = cstCoordinates.get(otherCst);

                for (Rectangle.Double starPos : positions) {
                    for (Rectangle.Double otherStarPos : otherPositions) {
                        if (starPos.intersects(otherStarPos)) {
                            foundPositions.add(starPos);
                        }
                    }
                }
            }

            IConstellation drawnConstellation = drawn.getConstellation();
            float percent = 0.1F + 0.9F * Mth.clamp(((foundPositions.size() * 1.5F) / positions.size()) * nightPerc, 0F, 1F);
            float existingPercent = distributionMap.getOrDefault(drawnConstellation.getRegistryName(), 0.1F);
            if (percent >= existingPercent) {
                distributionMap.put(drawnConstellation.getRegistryName(), percent);
            }
        }
        return new EngravedStarMap(distributionMap, constellations);
    }

    private static List<Rectangle.Double> createConstellationOffsets(DrawnConstellation cst) {
        float width = DrawnConstellation.CONSTELLATION_STAR_SIZE;

        List<Rectangle.Double> positions = new ArrayList<>();
        for (StarLocation star : cst.getConstellation().getStars()) {
            //Rescale from 32 to 20 width, move to drawn offset coordinates
            double starX = star.x * DrawnConstellation.CONSTELLATION_SIZE_PART
                    + cst.getPoint().getX()
                    - DrawnConstellation.CONSTELLATION_DRAW_SIZE / 2F
                    - (width / 2F);
            double starY = star.y * DrawnConstellation.CONSTELLATION_SIZE_PART
                    + cst.getPoint().getY()
                    - DrawnConstellation.CONSTELLATION_DRAW_SIZE / 2F
                    - (width / 2F);

            positions.add(new Rectangle.Double(starX, starY, width, width));
        }
        return positions;
    }

    public boolean canAffect(@Nonnull ItemStack stack) {
        for (ResourceLocation key : this.getConstellationKeys()) {
            IConstellation cst = ConstellationRegistry.getConstellation(key);
            if (cst != null) {
                EngravingEffect effect = cst.getEngravingEffect();
                if (effect != null && !effect.getApplicableEffects(stack).isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Nonnull
    public ItemStack applyEffects(@Nonnull ItemStack stack) {
        List<Tuple<EngravingEffect.ApplicableEffect, Float>> engravings = new LinkedList<>();
        List<IConstellation> constellations = new LinkedList<>();
        this.getConstellationKeys().stream()
                .map(ConstellationRegistry::getConstellation)
                .filter(Objects::nonNull)
                .forEach(constellations::add);

        for (IConstellation cst : constellations) {
            EngravingEffect effect = cst.getEngravingEffect();
            if (effect != null) {
                List<EngravingEffect.ApplicableEffect> applicable = effect.getApplicableEffects(stack);
                if (!applicable.isEmpty()) {
                    float distribution = this.getDistribution(cst);
                    for (EngravingEffect.ApplicableEffect applicableEffect : applicable) {
                        engravings.add(new Tuple<>(applicableEffect, distribution));
                    }
                }
            }
        }
        engravings.sort(Comparator.comparing(tpl -> {
            EngravingEffect.ApplicableEffect effect = tpl.getA();
            if (effect instanceof EngravingEffect.EnchantmentEffect) {
                if (!((EngravingEffect.EnchantmentEffect) effect).isIgnoreCompatibility()) {
                    return 0; //Sort incompatible enchantments first since the others won't care.
                }
                return 1;
            }
            return 2;
        }));

        for (Tuple<EngravingEffect.ApplicableEffect, Float> tpl : engravings) {
            EngravingEffect.ApplicableEffect effect = tpl.getA();
            float distribution = tpl.getB();
            stack = effect.apply(stack, distribution, random);
        }
        return stack;
    }

    public Collection<DrawnConstellation> getDrawnConstellations() {
        return Collections.unmodifiableCollection(this.drawInformation);
    }

    public Collection<ResourceLocation> getConstellationKeys() {
        return Collections.unmodifiableCollection(this.distributions.keySet());
    }

    public float getDistribution(IConstellation cst) {
        return this.distributions.getOrDefault(cst.getRegistryName(), 0F);
    }

    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        distributions.forEach((constellationKey, percent) -> {
            CompoundTag cstTag = new CompoundTag();
            NBTHelper.setResourceLocation(cstTag, "cst", constellationKey);
            cstTag.putFloat("percent", percent);
            list.add(cstTag);
        });
        tag.put("distributions", list);
        ListTag listDrawn = new ListTag();
        drawInformation.forEach(drawCst -> {
            CompoundTag cstTag = new CompoundTag();
            NBTHelper.setResourceLocation(cstTag, "cst", drawCst.getConstellation().getRegistryName());
            cstTag.putInt("x", drawCst.getPoint().x);
            cstTag.putInt("y", drawCst.getPoint().y);
            listDrawn.add(cstTag);
        });
        tag.put("drawInformation", listDrawn);
        return tag;
    }

    public static EngravedStarMap deserialize(CompoundTag tag) {
        Map<ResourceLocation, Float> distributionMap = new HashMap<>();
        ListTag list = tag.getList("distributions", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag cstTag = list.getCompound(i);
            ResourceLocation constellationKey = ResourceLocation.parse(cstTag.getString("cst"));
            float percent = cstTag.getFloat("percent");
            if (percent > 0) {
                distributionMap.put(constellationKey, percent);
            }
        }
        List<DrawnConstellation> drawnConstellations = new ArrayList<>();
        ListTag listDrawn = tag.getList("drawInformation", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < listDrawn.size(); i++) {
            CompoundTag cstTag = listDrawn.getCompound(i);
            IConstellation cst = ConstellationRegistry.getConstellation(ResourceLocation.parse(cstTag.getString("cst")));
            Point offset = new Point(cstTag.getInt("x"), cstTag.getInt("y"));
            drawnConstellations.add(new DrawnConstellation(offset, cst));
        }
        return new EngravedStarMap(distributionMap, drawnConstellations);
    }
}

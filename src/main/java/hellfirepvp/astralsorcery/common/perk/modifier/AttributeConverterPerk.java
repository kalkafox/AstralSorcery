/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.perk.modifier;

import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.lib.RegistriesAS;
import hellfirepvp.astralsorcery.common.perk.PerkConverter;
import hellfirepvp.astralsorcery.common.perk.ProgressGatedPerk;
import hellfirepvp.astralsorcery.common.perk.source.AttributeConverterProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.util.GsonHelper;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.LogicalSide;

import java.awt.*;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: AttributeConverterPerk
 * Created by HellFirePvP
 * Date: 08.08.2019 / 18:09
 */
public class AttributeConverterPerk extends ProgressGatedPerk implements AttributeConverterProvider {

    private final Set<PerkConverter> converters = Sets.newHashSet();

    public AttributeConverterPerk(ResourceLocation name, float x, float y) {
        super(name, x, y);
    }

    public <T> T addConverter(PerkConverter converter) {
        this.converters.add(converter);
        return (T) this;
    }

    public <T> T addRangedConverter(float radius, PerkConverter converter) {
        return this.addConverter(converter.asRangedConverter(new Point.Float(this.getOffset().x, this.getOffset().y), radius));
    }

    @Override
    public Collection<PerkConverter> getConverters(Player player, LogicalSide direction, boolean ignoreRequirements) {
        if (!ignoreRequirements && ResearchHelper.getProgress(player, direction).getPerkData().isPerkSealed(this)) {
            return Collections.emptyList();
        }

        return Collections.unmodifiableSet(converters);
    }

    @Override
    public void applyPerkLogic(Player player, LogicalSide direction) {}

    @Override
    public void removePerkLogic(Player player, LogicalSide direction) {}

    @Override
    public void deserializeData(JsonObject perkData) {
        super.deserializeData(perkData);

        this.converters.clear();

        if (GsonHelper.convertToInt(perkData, "converters")) {
            JsonArray array = GsonHelper.getAsJsonArray(perkData, "converters");
            for (int i = 0; i < array.size(); i++) {
                JsonObject serializedConverter = GsonHelper.getAsJsonObject(array.get(i), "converters[%s]");
                String key = GsonHelper.getAsString(serializedConverter, "name");
                PerkConverter converter = RegistriesAS.REGISTRY_PERK_ATTRIBUTE_CONVERTERS.getValue(ResourceLocation.parse(key));
                if (converter == null) {
                    throw new JsonParseException("Unknown converter: " + key);
                }
                if (serializedConverter.has("radius")) {
                    float radius = GsonHelper.getAsFloat(serializedConverter, "radius");
                    this.addRangedConverter(radius, converter);
                } else {
                    this.addConverter(converter);
                }
            }
        }
    }

    @Override
    public void serializeData(JsonObject perkData) {
        super.serializeData(perkData);

        if (!this.converters.isEmpty()) {
            JsonArray converters = new JsonArray();
            for (PerkConverter converter : this.converters) {
                JsonObject serializedConverter = new JsonObject();
                serializedConverter.addProperty("name", converter.getRegistryName().toString());
                if (converter instanceof PerkConverter.Radius) {
                    serializedConverter.addProperty("radius", ((PerkConverter.Radius) converter).getRadius());
                }
                converters.add(serializedConverter);
            }
            perkData.add("converters", converters);
        }
    }
}

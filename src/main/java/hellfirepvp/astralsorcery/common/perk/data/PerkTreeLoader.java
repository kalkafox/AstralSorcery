/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.perk.data;

import com.google.gson.*;
import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.perk.AbstractPerk;
import hellfirepvp.astralsorcery.common.perk.PerkTree;
import hellfirepvp.astralsorcery.common.util.MapStream;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: PerkTreeLoader
 * Created by HellFirePvP
 * Date: 12.08.2020 / 22:15
 */
public class PerkTreeLoader extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static final PerkTreeLoader INSTANCE = new PerkTreeLoader();

    private PerkTreeLoader() {
        super(GSON, "perks");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> dataMap, ResourceManager resourceManager, ProfilerFiller profiler) {
        Collection<JsonObject> loadingPerkObjects = MapStream.of(dataMap)
                .filterKey(key -> !key.getPath().startsWith("_"))
                .filterValue(JsonElement::isJsonObject)
                .mapValue(JsonElement::getAsJsonObject)
                .valueStream()
                .collect(Collectors.toList());
        PerkTree.PERK_TREE.updateOriginPerkTree(loadPerkTree(loadingPerkObjects));
    }

    public static PerkTreeData loadPerkTree(Collection<JsonObject> perkTreeObjects) {
        PerkTreeData newTree = new PerkTreeData();

        int count = 0;
        for(JsonObject serializedPerkData : perkTreeObjects) {

            ResourceLocation perkRegistryName = ResourceLocation.parse(GsonHelper.getString(serializedPerkData, "registry_name"));
            ResourceLocation customClass = PerkTypeHandler.DEFAULT.getKey();
            if (serializedPerkData.has("perk_class")) {
                customClass = ResourceLocation.parse(GsonHelper.getString(serializedPerkData, "perk_class"));
                if (!PerkTypeHandler.hasCustomType(customClass)) {
                    throw new JsonParseException("Unknown perk_class: " + customClass.toString());
                }
            }

            float posX = GsonHelper.getFloat(serializedPerkData, "x");
            float posY = GsonHelper.getFloat(serializedPerkData, "y");

            AbstractPerk perk = PerkTypeHandler.convert(perkRegistryName, posX, posY, customClass);
            if (serializedPerkData.has("name")) {
                String name = GsonHelper.getString(serializedPerkData, "name");
                perk.setName(name);
            }
            if (serializedPerkData.has("hiddenUnlessAllocated")) {
                perk.setHiddenUnlessAllocated(GsonHelper.getBoolean(serializedPerkData, "hiddenUnlessAllocated"));
            }

            if (serializedPerkData.has("data")) {
                JsonObject perkData = GsonHelper.getAsJsonObject(serializedPerkData, "data");
                perk.deserializeData(perkData);
            }

            LoadedPerkData connector = newTree.addPerk(perk, serializedPerkData);
            if (serializedPerkData.has("connection")) {
                JsonArray connectionArray = GsonHelper.getAsJsonArray(serializedPerkData, "connection");
                for (int i = 0; i < connectionArray.size(); i++) {
                    JsonElement connection = connectionArray.get(i);
                    String connectedPerkKey = GsonHelper.getString(connection, String.format("connection[%s]", i));
                    connector.addConnection(ResourceLocation.parse(connectedPerkKey));
                }
            }

            count++;
        }

        AstralSorcery.log.info("Built PerkTree with {} perks!", count);

        return newTree;
    }
}

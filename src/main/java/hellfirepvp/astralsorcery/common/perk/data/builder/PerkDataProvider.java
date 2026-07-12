/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.perk.data.builder;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import hellfirepvp.astralsorcery.common.perk.AbstractPerk;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

import java.awt.*;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: PerkDataWriter
 * Created by HellFirePvP
 * Date: 14.08.2020 / 19:09
 */
public abstract class PerkDataProvider implements DataProvider {

    protected final PackOutput output;

    public PerkDataProvider(PackOutput output) {
        this.output = output;
    }

    public abstract void registerPerks(Consumer<FinishedPerk> registrar);

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        Path path = this.output.getOutputFolder();

        List<FinishedPerk> builtPerks = new ArrayList<>();
        List<CompletableFuture<?>> futures = new ArrayList<>();
        this.registerPerks(finishedPerk -> {
            ResourceLocation perkName = finishedPerk.perk.getRegistryName();
            Point.Float offset = finishedPerk.perk.getOffset();
            if (builtPerks.stream().anyMatch(knownPerk -> knownPerk.perk.getOffset().equals(offset))) {
                throw new IllegalArgumentException("Duplicate perk registration at " + offset + " for " + perkName);
            }
            if (builtPerks.contains(finishedPerk)) {
                throw new IllegalArgumentException("Duplicate perk registry name: " + perkName);
            }
            builtPerks.add(finishedPerk);
            futures.add(DataProvider.saveStable(cache, finishedPerk.serialize(),
                    path.resolve(String.format("data/%s/perks/%s.json", perkName.getNamespace(), perkName.getPath()))));
        });

        JsonObject allPerks = new JsonObject();
        builtPerks.sort(Comparator.naturalOrder());
        builtPerks.forEach(perk -> allPerks.add(perk.perk.getRegistryName().toString(), perk.serialize()));
        futures.add(DataProvider.saveStable(cache, allPerks, path.resolve("data/astralsorcery/perks/_full_tree.json")));

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    @Override
    public String getName() {
        return "Perks";
    }

    public static class FinishedPerk implements Comparable<FinishedPerk> {

        private final AbstractPerk perk;
        private final List<ResourceLocation> connections;

        public FinishedPerk(AbstractPerk perk, List<ResourceLocation> connections) {
            this.perk = perk;
            this.connections = connections;
        }

        private JsonObject serialize() {
            JsonObject object = this.perk.serializePerk();
            JsonArray array = new JsonArray();
            for (ResourceLocation connection : this.connections) {
                array.add(connection.toString());
            }
            object.add("connection", array);
            return object;
        }

        @Override
        public int compareTo(FinishedPerk that) {
            return this.perk.getRegistryName().compareTo(that.perk.getRegistryName());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FinishedPerk that = (FinishedPerk) o;
            return Objects.equals(perk.getRegistryName(), that.perk.getRegistryName());
        }

        @Override
        public int hashCode() {
            return Objects.hash(perk.getRegistryName());
        }
    }
}

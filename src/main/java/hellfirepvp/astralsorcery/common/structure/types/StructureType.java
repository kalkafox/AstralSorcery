/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.structure.types;

import hellfirepvp.astralsorcery.common.registry.internal.AstralRegistryEntry;
import hellfirepvp.observerlib.api.ChangeSubscriber;
import hellfirepvp.observerlib.api.ObserverHelper;
import hellfirepvp.observerlib.api.util.BlockArray;
import hellfirepvp.observerlib.common.change.ChangeObserverStructure;
import hellfirepvp.observerlib.common.change.ObserverProviderStructure;
import hellfirepvp.observerlib.common.registry.RegistryProviders;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: StructureType
 * Created by HellFirePvP
 * Date: 30.05.2019 / 15:07
 */
public class StructureType implements AstralRegistryEntry<StructureType> {

    private final ResourceLocation name;
    private final Supplier<BlockArray> structureSupplier;

    public StructureType(ResourceLocation name, Supplier<BlockArray> structureSupplier) {
        this.name = name;
        this.structureSupplier = structureSupplier;
    }

    public BlockArray getFeature() {
        return this.structureSupplier.get();
    }

    public Component getDisplayName() {
        return Component.translatable(String.format("structure.%s.%s.name", name.getNamespace(), name.getPath()));
    }

    public ChangeSubscriber<ChangeObserverStructure> observe(Level level, BlockPos pos) {
        Object provider = RegistryProviders.getProvider(this.name);
        if (!(provider instanceof ObserverProviderStructure structureProvider)) {
            throw new IllegalStateException("Missing registered structure observer provider: " + this.name);
        }
        return ObserverHelper.getHelper().observeArea(level, pos, structureProvider);
    }

    @Override
    public final StructureType setRegistryName(ResourceLocation name) {
        return this;
    }

    @Nullable
    @Override
    public ResourceLocation getRegistryName() {
        return this.name;
    }

}

/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.data.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import hellfirepvp.observerlib.common.data.CachedWorldData;
import hellfirepvp.observerlib.common.data.WorldCacheDomain;
import hellfirepvp.observerlib.common.data.base.SectionWorldData;
import hellfirepvp.observerlib.common.data.base.WorldSection;
import hellfirepvp.observerlib.common.util.CodecUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RockCrystalBuffer
 * Created by HellFirePvP
 * Date: 17.08.2019 / 22:42
 */
public class RockCrystalBuffer extends SectionWorldData<RockCrystalBuffer, RockCrystalBuffer.BufferSection> {

    public static final Codec<RockCrystalBuffer> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            WorldCacheDomain.SaveKey.CODEC.fieldOf("key").forGetter(CachedWorldData::getSaveKey)
    ).apply(builder, key -> new RockCrystalBuffer(CodecUtil.unwrap(key))));

    public RockCrystalBuffer(WorldCacheDomain.SaveKey<RockCrystalBuffer> key) {
        super(key, BufferSection.CODEC, 10);
    }

    @Override
    protected BufferSection createNewSection(int sectionX, int sectionZ) {
        return new BufferSection(sectionX, sectionZ);
    }

    public List<BlockPos> collectPositions(ChunkPos center, int chunkRadius) {
        List<BlockPos> out = new LinkedList<>();
        for (int xx = -chunkRadius; xx <= chunkRadius; xx++) {
            for (int zz = -chunkRadius; zz <= chunkRadius; zz++) {
                ChunkPos other = new ChunkPos(center.x + xx, center.z + zz);
                BufferSection section = this.getSection(other.getWorldPosition());
                if (section != null) {
                    this.read(() -> out.addAll(section.crystalPositions));
                }
            }
        }
        return out;
    }

    public void addOre(BlockPos pos) {
        BufferSection section = this.getOrCreateSection(pos);
        this.write(() -> section.crystalPositions.add(pos));
        setChanged(section);
    }

    public void removeOre(BlockPos pos) {
        BufferSection section = this.getSection(pos);
        if (section != null) {
            this.write(() -> section.crystalPositions.remove(pos));
            setChanged(section);
        }
    }

    public static class BufferSection extends WorldSection {

        public static final Codec<BufferSection> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                Codec.INT.fieldOf("sX").forGetter(WorldSection::x),
                Codec.INT.fieldOf("sZ").forGetter(WorldSection::z),
                BlockPos.CODEC.listOf().fieldOf("posList").forGetter(section -> List.copyOf(section.crystalPositions))
        ).apply(builder, (sX, sZ, positions) -> {
            BufferSection section = new BufferSection(sX, sZ);
            section.crystalPositions.addAll(positions);
            return section;
        }));

        private final Set<BlockPos> crystalPositions = new HashSet<>();

        private BufferSection(int sX, int sZ) {
            super(sX, sZ);
        }
    }

}

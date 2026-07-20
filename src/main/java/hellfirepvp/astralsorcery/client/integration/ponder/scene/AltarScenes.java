/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.integration.ponder.scene;

import hellfirepvp.astralsorcery.common.lib.ConstellationsAS;
import hellfirepvp.astralsorcery.common.tile.TileAttunementAltar;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.Selection;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: AltarScenes
 * Created by HellFirePvP
 * Date: 20.07.2026
 *
 * Storyboards for {@link hellfirepvp.astralsorcery.client.integration.ponder.AstralSorceryPonderPlugin}:
 * {@link #attunementAltar} (the build) and {@link #attuning} (marking out a constellation
 * with Spectral Relays and activating the altar). Coordinates below are schematic-local,
 * matching the offset each provider applies (altar at local (9,1,9); the pattern's x/z
 * -9..9 and y -1..4 shifted by +9/+1/+9 - see {@link hellfirepvp.astralsorcery.datagen.assets.AttunementAltarSchematics}).
 *
 * Every {@code .text(...)} call in a storyboard is registered in call order as
 * {@code astralsorcery.ponder.<sceneId>.text_<N>} (0-indexed) by Ponder itself; each
 * scene's title is {@code astralsorcery.ponder.<sceneId>.header}. Keep
 * assets/astralsorcery/lang/en_us.json's entries in the same order as these calls.
 */
public class AltarScenes {

    private static final int FLOOR_MIN = 2;
    private static final int FLOOR_MAX = 16;
    private static final int FLOOR_BLOCK_DELAY_TICKS = 1;

    public static void attunementAltar(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("altar_attunement", "Building the Attunement Altar");
        scene.configureBasePlate(0, 0, 19);
        // The schematic (19x19) is much wider than Ponder's default framing assumes,
        // so scale the view down or the whole scene fills (and overflows) the screen.
        scene.scaleSceneView(0.4f);
        scene.showBasePlate();
        scene.idle(10);

        Selection floor = util.select().fromTo(FLOOR_MIN, 0, FLOOR_MIN, FLOOR_MAX, 0, FLOOR_MAX);
        int floorBlockCount = (FLOOR_MAX - FLOOR_MIN + 1) * (FLOOR_MAX - FLOOR_MIN + 1);
        scene.overlay().showOutlineWithText(floor, floorBlockCount * FLOOR_BLOCK_DELAY_TICKS + 40)
                .colored(PonderPalette.BLUE)
                .attachKeyFrame()
                .text("A 15x15 platform of Black Marble anchors the altar");
        for (int x = FLOOR_MIN; x <= FLOOR_MAX; x++) {
            for (int z = FLOOR_MIN; z <= FLOOR_MAX; z++) {
                scene.world().showSection(util.select().position(x, 0, z), Direction.UP);
                scene.idle(FLOOR_BLOCK_DELAY_TICKS);
            }
        }
        scene.idle(30);

        Selection archRing = util.select().fromTo(2, 0, 1, 16, 0, 1)
                .add(util.select().fromTo(2, 0, 17, 16, 0, 17))
                .add(util.select().fromTo(1, 0, 2, 1, 0, 16))
                .add(util.select().fromTo(17, 0, 2, 17, 0, 16));
        scene.world().showSection(archRing, Direction.UP);
        scene.overlay().showOutlineWithText(archRing, 60)
                .colored(PonderPalette.BLUE)
                .attachKeyFrame()
                .text("Marble Arch frames the platform on all four sides");
        scene.idle(70);

        Selection corners = cornerWing(util, 0, 0)
                .add(cornerWing(util, 0, 18))
                .add(cornerWing(util, 18, 0))
                .add(cornerWing(util, 18, 18));
        scene.world().showSection(corners, Direction.UP);
        scene.overlay().showOutlineWithText(corners, 60)
                .colored(PonderPalette.BLUE)
                .attachKeyFrame()
                .text("Loose Arch pieces wrap each corner");
        scene.idle(70);

        Selection pillars = util.select().fromTo(1, 1, 1, 1, 5, 1)
                .add(util.select().fromTo(1, 1, 17, 1, 5, 17))
                .add(util.select().fromTo(17, 1, 1, 17, 5, 1))
                .add(util.select().fromTo(17, 1, 17, 17, 5, 17));
        scene.world().showSection(pillars, Direction.DOWN);
        scene.overlay().showOutlineWithText(pillars, 80)
                .colored(PonderPalette.GREEN)
                .attachKeyFrame()
                .text("Four Marble Pillars rise from Runed bases, capped with Chiseled Marble");
        scene.idle(90);

        Selection altar = util.select().position(9, 1, 9);
        scene.world().showSection(altar, Direction.DOWN);
        scene.overlay().showOutlineWithText(altar, 60)
                .colored(PonderPalette.GREEN)
                .attachKeyFrame()
                .text("The Attunement Altar itself sits at the center");
        scene.idle(70);

        scene.markAsFinished();
    }

    // The five loose Marble Arch blocks in one diagonal corner of the platform,
    // mirroring PonderAttunementAltarStructureProvider#cornerWing.
    private static Selection cornerWing(SceneBuildingUtil util, int cornerX, int cornerZ) {
        int dx = cornerX == 0 ? 1 : -1;
        int dz = cornerZ == 0 ? 1 : -1;
        return util.select().position(cornerX, 0, cornerZ)
                .add(util.select().position(cornerX, 0, cornerZ + dz))
                .add(util.select().position(cornerX, 0, cornerZ + dz * 2))
                .add(util.select().position(cornerX + dx, 0, cornerZ))
                .add(util.select().position(cornerX + dx * 2, 0, cornerZ));
    }

    // Octans' four star offsets (see PonderAttuningStructureProvider#OCTANS_RELAY_OFFSETS),
    // shifted into schematic-local coordinates (+9, +1, +9).
    private static final int[][] OCTANS_RELAYS_LOCAL = {
            {14, 1, 14},
            {10, 1, 4},
            {7, 1, 7},
            {4, 1, 5},
    };

    public static void attuning(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("altar_attunement_attuning", "Attuning the Altar");
        scene.configureBasePlate(0, 0, 19);
        scene.scaleSceneView(0.4f);
        scene.showBasePlate();
        scene.idle(10);

        Selection relay1 = util.select().position(OCTANS_RELAYS_LOCAL[0][0], OCTANS_RELAYS_LOCAL[0][1], OCTANS_RELAYS_LOCAL[0][2]);
        Selection relay2 = util.select().position(OCTANS_RELAYS_LOCAL[1][0], OCTANS_RELAYS_LOCAL[1][1], OCTANS_RELAYS_LOCAL[1][2]);
        Selection relay3 = util.select().position(OCTANS_RELAYS_LOCAL[2][0], OCTANS_RELAYS_LOCAL[2][1], OCTANS_RELAYS_LOCAL[2][2]);
        Selection relay4 = util.select().position(OCTANS_RELAYS_LOCAL[3][0], OCTANS_RELAYS_LOCAL[3][1], OCTANS_RELAYS_LOCAL[3][2]);
        Selection relays = relay1.add(relay2).add(relay3).add(relay4);

        Selection altarStructure = util.select().fromTo(0, 0, 0, 18, 5, 18).substract(relays);
        scene.world().showSection(altarStructure, Direction.UP);
        scene.overlay().showOutlineWithText(altarStructure, 70)
                .colored(PonderPalette.BLUE)
                .attachKeyFrame()
                .text("Once built, the Attunement Altar can be attuned to a constellation");
        scene.idle(80);

        scene.world().showSection(relay1, Direction.UP);
        scene.overlay().showOutlineWithText(relay1, 60)
                .colored(PonderPalette.BLUE)
                .attachKeyFrame()
                .text("Spectral Relays placed around it mark out a constellation's star pattern - here, Octans");
        scene.idle(70);

        scene.world().showSection(relay2, Direction.UP);
        scene.idle(20);
        scene.world().showSection(relay3, Direction.UP);
        scene.idle(20);
        scene.world().showSection(relay4, Direction.UP);
        scene.idle(30);

        Vec3 v1 = util.vector().centerOf(OCTANS_RELAYS_LOCAL[0][0], OCTANS_RELAYS_LOCAL[0][1] + 1, OCTANS_RELAYS_LOCAL[0][2]);
        Vec3 v2 = util.vector().centerOf(OCTANS_RELAYS_LOCAL[1][0], OCTANS_RELAYS_LOCAL[1][1] + 1, OCTANS_RELAYS_LOCAL[1][2]);
        Vec3 v3 = util.vector().centerOf(OCTANS_RELAYS_LOCAL[2][0], OCTANS_RELAYS_LOCAL[2][1] + 1, OCTANS_RELAYS_LOCAL[2][2]);
        Vec3 v4 = util.vector().centerOf(OCTANS_RELAYS_LOCAL[3][0], OCTANS_RELAYS_LOCAL[3][1] + 1, OCTANS_RELAYS_LOCAL[3][2]);
        scene.overlay().showLine(PonderPalette.BLUE, v1, v2, 90);
        scene.overlay().showLine(PonderPalette.BLUE, v1, v3, 90);
        scene.overlay().showLine(PonderPalette.BLUE, v2, v3, 90);
        scene.overlay().showLine(PonderPalette.BLUE, v3, v4, 90);
        scene.overlay().showOutlineWithText(relays, 90)
                .colored(PonderPalette.BLUE)
                .attachKeyFrame()
                .text("This relay layout matches Octans' star pattern exactly");
        scene.idle(100);

        BlockPos altarPos = new BlockPos(9, 1, 9);
        Selection altarSelection = util.select().position(altarPos);

        // The altar's real detection (server-only structure/sky pass, night+moon-phase
        // gating) can't run in Ponder's client-only sandbox, so hand it the constellation
        // directly. From here on, its actual tickEffectsConstellation()/tickConstellationBeams()
        // are what's rendering the star flares and inter-relay beams below - not a scripted effect.
        scene.world().modifyBlockEntity(altarPos, TileAttunementAltar.class, altar -> {
            altar.forceActiveForPonder();
            altar.forceActiveConstellationForPonder(ConstellationsAS.octans);
        });
        scene.effects().indicateSuccess(altarPos);
        scene.overlay().showOutlineWithText(altarSelection, 80)
                .colored(PonderPalette.GREEN)
                .attachKeyFrame()
                .text("Recognizing Octans, the Attunement Altar activates and begins attuning");
        scene.idle(90);

        scene.markAsFinished();
    }
}

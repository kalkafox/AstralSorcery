/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.screen.journal.page;

import com.mojang.blaze3d.vertex.PoseStack;
import hellfirepvp.astralsorcery.common.data.research.ResearchNode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;

import javax.annotation.Nullable;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RenderablePage
 * Created by HellFirePvP
 * Date: 03.08.2019 / 18:53
 */
public abstract class RenderablePage {

    @Nullable
    private final ResearchNode node;
    private final int nodePage;

    public RenderablePage(@Nullable ResearchNode node, int nodePage) {
        this.node = node;
        this.nodePage = nodePage;
    }

    public abstract void render(PoseStack renderStack, float x, float y, float z, float pTicks, float xpos, float ypos);

    public void postRender(PoseStack renderStack, float x, float y, float z, float pTicks, float xpos, float ypos) {}

    public boolean propagateMouseClick(double xpos, double mouseZ) {
        return false;
    }

    public boolean propagateMouseDrag(double mouseDX, double mouseDZ) {
        return false;
    }

    public static Font getFont() {
        return Minecraft.getInstance().font;
    }

    @Nullable
    protected final ResearchNode getResearchNode() {
        return this.node;
    }

    protected final int getNodePage() {
        return this.nodePage;
    }
}

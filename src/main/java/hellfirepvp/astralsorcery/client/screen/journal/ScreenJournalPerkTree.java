/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.screen.journal;

import net.minecraft.network.chat.MutableComponent;

import net.minecraft.network.chat.Component;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.client.ClientScheduler;
import hellfirepvp.astralsorcery.client.lib.SpritesAS;
import hellfirepvp.astralsorcery.client.lib.TexturesAS;
import hellfirepvp.astralsorcery.client.render.IDrawRenderTypeBuffer;
import hellfirepvp.astralsorcery.client.resource.SpriteSheetResource;
import hellfirepvp.astralsorcery.client.screen.helper.ScalingPoint;
import hellfirepvp.astralsorcery.client.screen.helper.ScreenRenderBoundingBox;
import hellfirepvp.astralsorcery.client.screen.helper.SizeHandler;
import hellfirepvp.astralsorcery.client.screen.journal.overlay.ScreenJournalOverlayPerkStatistics;
import hellfirepvp.astralsorcery.client.screen.journal.perk.BatchPerkContext;
import hellfirepvp.astralsorcery.client.screen.journal.perk.DynamicPerkRender;
import hellfirepvp.astralsorcery.client.screen.journal.perk.PerkRenderGroup;
import hellfirepvp.astralsorcery.client.screen.journal.perk.PerkTreeSizeHandler;
import hellfirepvp.astralsorcery.client.util.RenderingDrawUtils;
import hellfirepvp.astralsorcery.client.util.RenderingGuiUtils;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;
import hellfirepvp.astralsorcery.client.util.ScreenTextEntry;
import hellfirepvp.astralsorcery.client.util.draw.BufferContext;
import hellfirepvp.astralsorcery.common.constellation.IMajorConstellation;
import hellfirepvp.astralsorcery.common.data.research.PerkAllocationType;
import hellfirepvp.astralsorcery.common.data.research.PlayerPerkData;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.item.useables.ItemPerkSeal;
import hellfirepvp.astralsorcery.common.lib.ItemsAS;
import hellfirepvp.astralsorcery.common.lib.SoundsAS;
import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.play.client.PktPerkGemModification;
import hellfirepvp.astralsorcery.common.network.play.client.PktRequestPerkSealAction;
import hellfirepvp.astralsorcery.common.network.play.client.PktUnlockPerk;
import hellfirepvp.astralsorcery.common.perk.*;
import hellfirepvp.astralsorcery.common.perk.node.socket.GemSocketItem;
import hellfirepvp.astralsorcery.common.perk.node.socket.GemSocketPerk;
import hellfirepvp.astralsorcery.common.perk.source.AttributeConverterProvider;
import hellfirepvp.astralsorcery.common.perk.tree.PerkTreePoint;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import hellfirepvp.astralsorcery.common.util.item.ItemUtils;
import hellfirepvp.astralsorcery.common.util.sound.SoundHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Tuple;
import net.minecraft.Util;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import net.minecraft.util.text.*;
import net.neoforged.fml.LogicalSide;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.awt.Color;
import java.awt.*;
import java.util.List;
import java.util.*;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ScreenJournalPerkTree
 * Created by HellFirePvP
 * Date: 08.08.2019 / 18:29
 */
public class ScreenJournalPerkTree extends ScreenJournal {

    private static final Rectangle rectSealBox = new Rectangle(29, 16, 16, 16);
    private static final Rectangle rectSearchTextEntry = new Rectangle(300, 16, 88, 15);

    private static Long lastPreparedBuffer = null;
    private static BatchPerkContext drawBuffer;
    private static BatchPerkContext.TextureObjectGroup searchContext;
    private static BatchPerkContext.TextureObjectGroup sealContext;

    private SizeHandler sizeHandler;
    private ScreenRenderBoundingBox guiBox;

    private ScalingPoint mousePosition, previousMousePosition;

    private AbstractPerk unlockPrimed = null;

    private AbstractPerk sealBreakPrimed = null;
    private int tickSealBreak = 0;

    private int guiOffsetX, guiOffsetY;
    public boolean expectReinit = false;

    private final Map<AbstractPerk, Rectangle.Float> thisFramePerks = Maps.newHashMap();
    private final Map<AbstractPerk, Long> unlockEffects = Maps.newHashMap();
    private final Map<AbstractPerk, Long> breakEffects = Maps.newHashMap();

    private final ScreenTextEntry searchTextEntry = new ScreenTextEntry();
    private final List<AbstractPerk> searchMatches = Lists.newArrayList();

    private GemSocketPerk socketMenu = null;
    private Rectangle.Float rSocketMenu = null;
    private final Map<Rectangle.Float, Integer> slotsSocketMenu = Maps.newHashMap();
    private Rectangle rStatStar = null;

    private ItemStack mouseSealStack = ItemStack.EMPTY;
    private ItemStack foundSeals = ItemStack.EMPTY;

    public ScreenJournalPerkTree() {
        super(Component.translatable("screen.astralsorcery.tome.perks"), 30);
        this.closeWithInventoryKey = false;
        this.searchTextEntry.setChangeCallback(this::updateSearchHighlight);

        buildTree();
    }

    private void buildTree() {
        this.guiBox = new ScreenRenderBoundingBox(10, 10, guiWidth - 10, guiHeight - 10);

        this.sizeHandler = new PerkTreeSizeHandler();
        this.sizeHandler.setScaleSpeed(0.04F);
        this.sizeHandler.setMaxScale(1F);
        this.sizeHandler.setMinScale(0.1F);
        this.sizeHandler.updateSize();

        this.mousePosition = ScalingPoint.createPoint(0, 0, this.sizeHandler.getScalingFactor(), false);
    }

    public static void refreshDrawBuffer() {
        lastPreparedBuffer = null;
    }

    public static void initializeDrawBuffer() {
        PerkTree.PERK_TREE.getVersion(LogicalSide.CLIENT).ifPresent(version -> {
            if (lastPreparedBuffer == null || version.longValue() != lastPreparedBuffer) {
                drawBuffer = new BatchPerkContext();

                searchContext = drawBuffer.addContext(SpritesAS.SPR_PERK_SEARCH, BatchPerkContext.PRIORITY_OVERLAY);
                sealContext = drawBuffer.addContext(SpritesAS.SPR_PERK_SEAL, BatchPerkContext.PRIORITY_FOREGROUND);

                List<PerkRenderGroup> groups = Lists.newArrayList();
                for (PerkTreePoint<?> p : PerkTree.PERK_TREE.getPerkPoints(LogicalSide.CLIENT)) {
                    p.addGroups(groups);
                }
                for (PerkRenderGroup group : groups) {
                    group.batchRegister(drawBuffer);
                }

                lastPreparedBuffer = version;
            }
        });
    }

    @Override
    protected void init() {
        super.init();

        if (this.expectReinit) {
            this.expectReinit = false;
            return;
        }

        this.guiOffsetX = leftPos + 10;
        this.guiOffsetY = topPos + 10;

        boolean shifted = false;
        PlayerProgress progress = ResearchHelper.getClientProgress();

        IMajorConstellation attunement = progress.getAttunedConstellation();
        if (attunement != null) {
            AbstractPerk root = PerkTree.PERK_TREE.getRootPerk(LogicalSide.CLIENT, attunement);
            if (root != null) {
                Point.Float shift = this.sizeHandler.evRelativePos(root.getOffset());
                this.moveMouse(Mth.floor(shift.x), Mth.floor(shift.y));
                shifted = true;
            }
        }

        if (!shifted) {
            this.moveMouse(Mth.floor(this.sizeHandler.getTotalWidth() / 2),
                    Mth.floor(this.sizeHandler.getTotalHeight() / 2));
        }

        this.applyMovedMouseOffset();
    }

    @Override
    public void render(PoseStack renderStack, int xpos, int ypos, float pTicks) {
        initializeDrawBuffer();

        this.thisFramePerks.clear();

        double guiFactor = Minecraft.getInstance().getWindow().getGuiScale();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(Mth.floor((leftPos + 39) * guiFactor), Mth.floor((topPos + 44) * guiFactor),
                Mth.floor((guiWidth - 76) * guiFactor), Mth.floor((guiHeight - 71) * guiFactor));

        this.setBlitOffset(-50);
        this.drawBackground(renderStack);
        this.setBlitOffset(0);

        this.drawPerkTree(renderStack, pTicks);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        RenderSystem.depthMask(false);
        this.drawDefault(renderStack, TexturesAS.TEX_GUI_BOOK_FRAME_FULL, xpos, ypos);
        RenderSystem.depthMask(true);

        drawSearchBox(renderStack);
        drawMiscInfo(renderStack, xpos, ypos, pTicks);
        drawSocketContextMenu(renderStack);
        drawSealBox(renderStack);

        this.setBlitOffset(510);
        drawHoverTooltips(renderStack, xpos, ypos);
        this.setBlitOffset(0);

        if (!this.mouseSealStack.isEmpty()) {
            renderStack.pushPose();
            renderStack.translate(xpos - 8, ypos - 8, this.getGuiZLevel());
            RenderingUtils.renderItemStackGUI(renderStack, this.mouseSealStack, null);
            renderStack.popPose();
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (Minecraft.getInstance().player != null) {
            int count = ItemPerkSeal.getPlayerSealCount(Minecraft.getInstance().player);
            if (count > 0) {
                this.foundSeals = new ItemStack(ItemsAS.PERK_SEAL, count);
            } else {
                this.foundSeals = ItemStack.EMPTY;
            }
        } else {
            this.foundSeals = ItemStack.EMPTY;
        }

        this.tickSealBreak--;
        if (this.tickSealBreak <= 0) {
            this.tickSealBreak = 0;
            this.sealBreakPrimed = null;
        }
    }

    private void drawSealBox(PoseStack renderStack) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        TexturesAS.TEX_GUI_MENU_SLOT.bindTexture();
        RenderingGuiUtils.drawTexturedRect(renderStack, leftPos + rectSealBox.x - 1, topPos + rectSealBox.y - 1, this.getGuiZLevel(), rectSealBox.width + 2, rectSealBox.height + 2, TexturesAS.TEX_GUI_MENU_SLOT);
        RenderSystem.disableBlend();

        if (!this.foundSeals.isEmpty()) {
            renderStack.pushPose();
            renderStack.translate(leftPos + rectSealBox.x, topPos + rectSealBox.y, this.getGuiZLevel());
            RenderingUtils.renderItemStackGUI(renderStack, this.foundSeals, null);
            renderStack.popPose();
        }
    }

    private void drawHoverTooltips(PoseStack renderStack, int xpos, int ypos) {
        Player player = Minecraft.getInstance().player;

        for (Rectangle.Float r : this.slotsSocketMenu.keySet()) {
            if (r.contains(xpos, ypos)) {
                Integer slot = this.slotsSocketMenu.get(r);
                ItemStack in = player.inventory.getStackInSlot(slot);
                if (!in.isEmpty()) {
                    Font fr = in.getItem().getFont(in);
                    if (fr == null) {
                        fr = Minecraft.getInstance().font;
                    }
                    List<FormattedText> toolTip = new ArrayList<>();
                    toolTip.addAll(this.getTooltipFromItem(in));
                    RenderingDrawUtils.renderBlueTooltipComponents(renderStack, xpos, ypos, this.getGuiZLevel(), toolTip, fr, true);
                }
                return;
            }
        }

        if (rStatStar.contains(xpos, ypos)) {
            RenderingDrawUtils.renderBlueTooltipComponents(renderStack, rStatStar.x + rStatStar.width / 2F, rStatStar.y + rStatStar.height, this.getGuiZLevel(),
                    Lists.newArrayList(Component.translatable("perk.reader.astralsorcery.infostar")), font, false);
            return;
        }

        if (!this.foundSeals.isEmpty() && rectSealBox.contains(xpos - leftPos, ypos - topPos)) {
            List<FormattedText> toolTip = new ArrayList<>();
            toolTip.addAll(this.foundSeals.getTooltipLines(Minecraft.getInstance().player,
                    Minecraft.getInstance().options.advancedItemTooltips ? TooltipFlag.TooltipFlags.ADVANCED : TooltipFlag.TooltipFlags.NORMAL));
            toolTip.add(MutableComponent.EMPTY);
            toolTip.add(Component.translatable("perk.info.astralsorcery.sealed.usage").withStyle(ChatFormatting.GRAY));

            RenderingDrawUtils.renderBlueTooltipComponents(renderStack, xpos, ypos, this.getGuiZLevel(), toolTip, font, false);
        } else {
            for (Map.Entry<AbstractPerk, Rectangle.Float> rctPerk : this.thisFramePerks.entrySet()) {
                if (rctPerk.getValue().contains(xpos, ypos) && this.guiBox.isInBox(xpos - leftPos, ypos - topPos)) {
                    List<FormattedText> toolTip = new LinkedList<>();
                    AbstractPerk perk = rctPerk.getKey();
                    PlayerProgress prog = ResearchHelper.getClientProgress();
                    PlayerPerkData perkData = prog.getPerkData();

                    perk.getLocalizedTooltip().forEach(lineState -> {
                        Style style = lineState.getStyle();
                        if (style.getColor() == null) {
                            lineState.withStyle(ChatFormatting.GRAY).withStyle(ChatFormatting.ITALIC);
                        }
                        toolTip.add(lineState);
                    });

                    if (perkData.isPerkSealed(perk)) {
                        toolTip.add(Component.translatable("perk.info.astralsorcery.sealed").withStyle(ChatFormatting.RED));
                        toolTip.add(Component.translatable("perk.info.astralsorcery.sealed.break").withStyle(ChatFormatting.RED));
                    } else if (perkData.hasPerkEffect(perk)) {
                        toolTip.add(Component.translatable("perk.info.astralsorcery.active").withStyle(ChatFormatting.GREEN));
                    } else if (perk.mayUnlockPerk(prog, player)) {
                        toolTip.add(Component.translatable("perk.info.astralsorcery.available").withStyle(ChatFormatting.BLUE));
                    } else {
                        toolTip.add(Component.translatable("perk.info.astralsorcery.locked").withStyle(ChatFormatting.GRAY));
                    }

                    if (Minecraft.getInstance().options.advancedItemTooltips && perk.getCategory() != AbstractPerk.CATEGORY_BASE) {
                        toolTip.add(perk.getCategory().getName().withStyle(ChatFormatting.GRAY).withStyle(ChatFormatting.ITALIC));
                    }
                    Collection<MutableComponent> modInfo = perk.getSource();
                    if (modInfo != null) {
                        for (MutableComponent cmp : modInfo) {
                            toolTip.add(cmp.withStyle(ChatFormatting.BLUE).withStyle(ChatFormatting.ITALIC));
                        }
                    }
                    if (Minecraft.getInstance().options.renderDebug) {
                        toolTip.add(MutableComponent.EMPTY);
                        toolTip.add(Component.literal(perk.getRegistryName().toString()).withStyle(ChatFormatting.GRAY));
                        toolTip.add(Component.translatable("astralsorcery.misc.ctrlcopy").withStyle(ChatFormatting.GRAY));
                    }
                    RenderingDrawUtils.renderBlueTooltipComponents(renderStack, xpos, ypos, this.getGuiZLevel(), toolTip, font, true);
                    break;
                }
            }
        }
    }

    private <T extends AbstractPerk & GemSocketPerk> void drawSocketContextMenu(PoseStack renderStack) {
        this.rSocketMenu = null;
        this.slotsSocketMenu.clear();

        if (socketMenu != null) {
            T sMenuPerk = (T) socketMenu;
            Map<Integer, ItemStack> found = ItemUtils.findItemsIndexedInPlayerInventory(Minecraft.getInstance().player, stack -> {
                if (stack.isEmpty() || !(stack.getItem() instanceof GemSocketItem)) {
                    return false;
                }
                GemSocketItem item = (GemSocketItem) stack.getItem();
                return item.canBeInserted(stack, sMenuPerk, Minecraft.getInstance().player, ResearchHelper.getClientProgress(), LogicalSide.CLIENT);
            });
            if (found.isEmpty()) { // Close then.
                closeSocketMenu();
                return;
            }

            Point.Float offset = this.sizeHandler.scalePointToGui(this, this.mousePosition, sMenuPerk.getPoint().getOffset());
            float offsetX = Mth.floor(offset.x);
            float offsetY = Mth.floor(offset.y);

            float scale = this.sizeHandler.getScalingFactor();
            float scaledSlotSize = 18F * scale;

            int realWidth = Math.min(5, found.size());
            int realHeight = (found.size() / 5 + (found.size() % 5 == 0 ? 0 : 1));

            float width  = realWidth * scaledSlotSize;
            float height = realHeight * scaledSlotSize;
            this.rSocketMenu = new Rectangle.Float(offsetX + (12 * scale) - 4, offsetY - (12 * scale) - 4, width + 4, height + 4);

            if (!this.guiBox.isInBox(rSocketMenu.x - leftPos, rSocketMenu.y - topPos) ||
                    !this.guiBox.isInBox(rSocketMenu.x + rSocketMenu.width - leftPos, rSocketMenu.y + rSocketMenu.height - topPos)) {
                closeSocketMenu();
                return;
            }

            renderStack.pushPose();
            renderStack.translate(offsetX, offsetY, getGuiZLevel());
            renderStack.scale(scale, scale, 1F);
            RenderingDrawUtils.renderBlueTooltipBox(renderStack, 0, 0, realWidth * 18, realHeight * 18);
            renderStack.popPose();

            float inventoryOffsetX = offsetX + 12 * scale;
            float inventoryOffsetY = offsetY - 12 * scale;
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            TexturesAS.TEX_GUI_MENU_SLOT_GEM_CONTEXT.bindTexture();
            RenderingUtils.draw(GL11.GL_QUADS, DefaultVertexFormat.POSITION_COLOR_TEX, buf -> {
                for (int index = 0; index < found.size(); index++) {
                    float addedX = (index % 5) * scaledSlotSize;
                    float addedY = (index / 5) * scaledSlotSize;
                    RenderingGuiUtils.rect(buf, renderStack,inventoryOffsetX + addedX, inventoryOffsetY + addedY, this.getGuiZLevel(), scaledSlotSize, scaledSlotSize).draw();
                }
            });
            RenderSystem.disableBlend();

            offsetX += 12 * scale;
            offsetY -= 12 * scale;

            int index = 0;
            for (Integer slotId : found.keySet()) {
                ItemStack stack = found.get(slotId);
                float addedX = (index % 5) * scaledSlotSize;
                float addedY = (index / 5) * scaledSlotSize;
                Rectangle.Float r = new Rectangle.Float(offsetX + addedX, offsetY + addedY, scaledSlotSize, scaledSlotSize);

                renderStack.pushPose();
                renderStack.translate(offsetX + addedX + 1, offsetY + addedY + 1, getGuiZLevel());
                renderStack.scale(scale, scale, 1F);
                RenderingUtils.renderItemStackGUI(renderStack, stack, null);
                renderStack.popPose();

                slotsSocketMenu.put(r, slotId);
                index++;
            }
        }
    }

    private void drawMiscInfo(PoseStack renderStack, int xpos, int ypos, float pTicks) {
        PlayerProgress prog = ResearchHelper.getClientProgress();
        Player player = Minecraft.getInstance().player;

        int availablePerks;
        if (prog.isAttuned() && (availablePerks = prog.getPerkData().getAvailablePerkPoints(player, LogicalSide.CLIENT)) > 0) {
            renderStack.pushPose();
            renderStack.translate(leftPos + 50, topPos + 18, this.getGuiZLevel());
            FormattedText nodes = Component.translatable("perk.info.astralsorcery.points", availablePerks);
            RenderingDrawUtils.renderStringAt(nodes, renderStack, font, 0xCCCCCC, true);
            renderStack.popPose();
        }

        renderStack.pushPose();
        renderStack.translate(leftPos + 288, topPos + 20, this.getGuiZLevel());
        rStatStar = RenderingDrawUtils.drawInfoStar(renderStack, IDrawRenderTypeBuffer.defaultBuffer(), 16, pTicks);
        rStatStar.translate(leftPos + 288, topPos + 20);
        renderStack.popPose();
    }

    private void drawSearchBox(PoseStack renderStack) {
        TexturesAS.TEX_GUI_TEXT_FIELD.bindTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderingUtils.draw(GL11.GL_QUADS, DefaultVertexFormat.POSITION_COLOR_TEX, buf -> {
            RenderingGuiUtils.rect(buf, renderStack, leftPos + 300, topPos + 16, this.getGuiZLevel(), 88.5F, 15).draw();
        });
        RenderSystem.disableBlend();

        String text = this.searchTextEntry.getText();

        int length = font.width(text);
        boolean addDots = length > 75;
        while (length > 75) {
            text = text.substring(1);
            length = font.width("..." + text);
        }
        if (addDots) {
            text = "..." + text;
        }

        if ((ClientScheduler.getClientTick() % 20) > 10) {
            text += "_";
        }

        renderStack.pushPose();
        renderStack.translate(leftPos + 304, topPos + 20, this.getGuiZLevel());
        RenderingDrawUtils.renderStringAt(font, renderStack, Component.literal(text), 0xCCCCCC);
        renderStack.popPose();
    }

    private void drawPerkTree(PoseStack renderStack, float a) {
        Player player = Minecraft.getInstance().player;
        PlayerProgress progress = ResearchHelper.getClientProgress();
        PlayerPerkData perkData = progress.getPerkData();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        TexturesAS.TEX_GUI_LINE_CONNECTION.bindTexture();
        RenderingUtils.draw(GL11.GL_QUADS, DefaultVertexFormat.POSITION_COLOR_TEX, buf -> {
            for (Tuple<AbstractPerk, AbstractPerk> perkConnection : PerkTree.PERK_TREE.getConnections()) {
                if (!perkConnection.getA().isVisible(progress, player) ||
                        !perkConnection.getB().isVisible(progress, player)) {
                    continue;
                }
                AllocationStatus status;

                int alloc = 0;
                if (perkData.hasPerkAllocation(perkConnection.getA(), PerkAllocationType.UNLOCKED)) {
                    alloc++;
                }
                if (perkData.hasPerkAllocation(perkConnection.getB(), PerkAllocationType.UNLOCKED)) {
                    alloc++;
                }
                if (alloc == 2) {
                    status = AllocationStatus.ALLOCATED;
                } else if (alloc == 1 && progress.getPerkData().hasFreeAllocationPoint(player, LogicalSide.CLIENT)) {
                    status = AllocationStatus.UNLOCKABLE;
                } else {
                    status = AllocationStatus.UNALLOCATED;
                }

                Point.Float offsetOne = perkConnection.getA().getPoint().getOffset();
                Point.Float offsetTwo = perkConnection.getB().getPoint().getOffset();
                drawConnection(buf, renderStack, status, offsetOne, offsetTwo, ClientScheduler.getClientTick() + (int) offsetOne.x + (int) offsetOne.y + (int) offsetTwo.x + (int) offsetTwo.y);
            }
        });
        RenderSystem.disableBlend();

        drawBuffer.beginDrawingPerks();

        List<Runnable> renderDynamic = Lists.newArrayList();
        for (PerkTreePoint<?> perkPoint : PerkTree.PERK_TREE.getPerkPoints(LogicalSide.CLIENT)) {
            AbstractPerk perk = perkPoint.getPerk();
            if (!perk.isVisible(progress, player)) {
                continue;
            }
            Point.Float offset = perkPoint.getOffset();
            Rectangle.Float perkRect = drawPerk(drawBuffer, renderStack, perkPoint,
                    a, ClientScheduler.getClientTick() + (int) offset.x + (int) offset.y,
                    perkData.isPerkSealed(perk),
                    renderDynamic);
            if (perkRect != null) {
                this.thisFramePerks.put(perk, perkRect);
            }
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        drawBuffer.draw();
        RenderSystem.disableBlend();

        renderDynamic.forEach(Runnable::run);

        this.unlockEffects.keySet().removeIf(perk -> !drawPerkUnlock(perk, renderStack, this.unlockEffects.get(perk)));
        this.breakEffects.keySet().removeIf(perk -> !drawPerkSealBreak(perk, renderStack, this.breakEffects.get(perk), a));
    }

    private boolean drawPerkSealBreak(AbstractPerk perk, PoseStack renderStack, long tick, float pTicks) {
        int count = (int) (ClientScheduler.getClientTick() - tick);
        SpriteSheetResource sealBreakSprite = SpritesAS.SPR_PERK_SEAL_BREAK;
        if (count >= sealBreakSprite.getFrameCount()) {
            return false;
        }
        Point.Float offset = this.sizeHandler.scalePointToGui(this, this.mousePosition, perk.getOffset());

        float sealFade = 1.0F - (((float) count) + pTicks) / ((float) sealBreakSprite.getFrameCount());
        float width = 22;
        Rectangle.Float rct;
        if ((rct = thisFramePerks.get(perk)) != null) {
            width = rct.width;
        }
        float sealWidth = width * 0.75F;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        SpritesAS.SPR_PERK_SEAL.bindTexture();
        RenderingUtils.draw(GL11.GL_QUADS, DefaultVertexFormat.POSITION_COLOR_TEX, buf -> {
            Point.Float pOffset = perk.getPoint().getOffset();
            drawSeal(buf, renderStack, sealWidth, offset.x, offset.y, ClientScheduler.getClientTick() + (int) pOffset.x + (int) pOffset.y, sealFade * 0.75F);
        });

        float uLength = sealBreakSprite.getUWidth();
        float vLength = sealBreakSprite.getVWidth();
        Tuple<Float, Float> uv = sealBreakSprite.getUVOffset(count);

        sealBreakSprite.bindTexture();
        RenderingUtils.draw(GL11.GL_QUADS, DefaultVertexFormat.POSITION_COLOR_TEX, buf -> {
            RenderingGuiUtils.rect(buf, renderStack, offset.x - sealWidth, offset.y - sealWidth, this.getGuiZLevel(), sealWidth * 2, sealWidth * 2)
                    .color(1F, 1F, 1F, 0.85F)
                    .tex(uv.getA(), uv.getB(), uLength, vLength)
                    .draw();
        });
        RenderSystem.disableBlend();
        return true;
    }

    private boolean drawPerkUnlock(AbstractPerk perk, PoseStack renderStack, long tick) {
        int count = (int) (ClientScheduler.getClientTick() - tick);
        SpriteSheetResource spritePerkUnlock = SpritesAS.SPR_PERK_UNLOCK;
        if (count >= spritePerkUnlock.getFrameCount()) {
            return false;
        }
        Point.Float offset = this.sizeHandler.scalePointToGui(this, this.mousePosition, perk.getOffset());

        float width = 22;
        Rectangle.Float rct;
        if ((rct = thisFramePerks.get(perk)) != null) {
            width = rct.width;
        }
        float unlockWidth = width * 2.5F;

        float uLength = spritePerkUnlock.getUWidth();
        float vLength = spritePerkUnlock.getVWidth();
        Tuple<Float, Float> uv = spritePerkUnlock.getUVOffset(count);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        spritePerkUnlock.bindTexture();
        RenderingUtils.draw(GL11.GL_QUADS, DefaultVertexFormat.POSITION_COLOR_TEX, buf -> {
            RenderingGuiUtils.rect(buf, renderStack, offset.x - unlockWidth, offset.y - unlockWidth, this.getGuiZLevel(), unlockWidth * 2, unlockWidth * 2)
                    .tex(uv.getA(), uv.getB(), uLength, vLength)
                    .draw();
        });
        RenderSystem.disableBlend();
        return true;
    }

    @Nullable
    private Rectangle.Float drawPerk(BatchPerkContext ctx, PoseStack renderStack, PerkTreePoint<?> perkPoint,
                                      float pTicks, long effectTick, boolean renderSeal,
                                      Collection<Runnable> outRenderDynamic) {
        Point.Float offset = this.sizeHandler.scalePointToGui(this, this.mousePosition, perkPoint.getOffset());

        float scale = this.sizeHandler.getScalingFactor();
        AllocationStatus status = perkPoint.getPerk().getPerkStatus(Minecraft.getInstance().player, LogicalSide.CLIENT);
        Rectangle.Float drawSize = perkPoint.renderPerkAtBatch(ctx, renderStack, status, effectTick, pTicks, offset.x, offset.y, this.getGuiZLevel(), scale);

        if (perkPoint instanceof DynamicPerkRender) {
            outRenderDynamic.add(() ->
                    ((DynamicPerkRender) perkPoint).renderAt(status, renderStack, effectTick, pTicks, offset.x, offset.y, this.getGuiZLevel(), scale));
        }

        if (drawSize == null) {
            return null;
        }

        if (renderSeal) {
            this.drawSeal(ctx, renderStack, drawSize.width * 0.75, offset.x, offset.y, effectTick);
        }

        if (this.searchMatches.contains(perkPoint.getPerk())) {
            drawSearchMarkHalo(ctx, renderStack, drawSize, offset.x, offset.y);
        }

        float mapDrawSize = 28F;
        if (perkPoint.getPerk() instanceof AttributeConverterProvider) {
            for (PerkConverter converter : ((AttributeConverterProvider) perkPoint.getPerk()).getConverters(Minecraft.getInstance().player, LogicalSide.CLIENT, true)) {
                if (converter instanceof PerkConverter.Radius) {
                    float radius = ((PerkConverter.Radius) converter).getRadius();

                    drawSearchHalo(ctx, renderStack, mapDrawSize * radius * scale, offset.x, offset.y);
                }
            }
        }

        return new Rectangle.Float(offset.x - (drawSize.width / 2), offset.y - (drawSize.height / 2), drawSize.width, drawSize.height);
    }

    private void drawSeal(BatchPerkContext ctx, PoseStack renderStack, double size, double x, double y, long spriteOffsetTick) {
        BufferContext batch = ctx.getContext(sealContext);
        drawSeal(batch, renderStack, size, x, y, spriteOffsetTick, 1F);
    }

    private void drawSeal(BufferBuilder vb, PoseStack renderStack, double size, double x, double y, long spriteOffsetTick, float alpha) {
        SpriteSheetResource tex = SpritesAS.SPR_PERK_SEAL;
        if (tex == null) {
            return;
        }

        float uLength = tex.getULength();
        float vLength = tex.getVLength();
        Tuple<Float, Float> frameUV = tex.getUVOffset(spriteOffsetTick);
        Vector3 starVec = new Vector3(x - size, y - size, 0);

        Matrix4f offset = renderStack.last().pose();
        for (int i = 0; i < 4; i++) {
            int u = ((i + 1) & 2) >> 1;
            int v = ((i + 2) & 2) >> 1;

            Vector3 pos = starVec.clone().addX(size * u * 2).addY(size * v * 2);
            pos.drawPos(offset, vb)
                    .color(1F, 1F, 1F, alpha)
                    .tex(frameUV.getA() + uLength * u, frameUV.getB() + vLength * v)
                    .endVertex();
        }
    }

    private void drawSearchMarkHalo(BatchPerkContext ctx, PoseStack renderStack, Rectangle.Float draw, float x, float y) {
        drawSearchHalo(ctx, renderStack, (draw.width + draw.height) / 2F, x, y);
    }

    private void drawSearchHalo(BatchPerkContext ctx, PoseStack renderStack, float size, float x, float y) {
        BufferContext batch = ctx.getContext(searchContext);
        SpriteSheetResource searchMark = SpritesAS.SPR_PERK_SEARCH;

        searchMark.bindTexture();
        Vector3 starVec = new Vector3(x - size, y - size, 0);
        float uLength = searchMark.getUWidth();
        float vLength = searchMark.getVWidth();
        Tuple<Float, Float> frameUV = searchMark.getUVOffset();

        Matrix4f offset = renderStack.last().pose();
        for (int i = 0; i < 4; i++) {
            int u = ((i + 1) & 2) >> 1;
            int v = ((i + 2) & 2) >> 1;

            Vector3 pos = starVec.clone().addX(size * u * 2).addY(size * v * 2);
            pos.drawPos(offset, batch)
                    .color(0.8F, 0.1F, 0.1F, 1F)
                    .tex(frameUV.getA() + uLength * u, frameUV.getB() + vLength * v)
                    .endVertex();
        }
    }

    private void drawConnection(BufferBuilder vb, PoseStack renderStack, AllocationStatus status, Point.Float source, Point.Float target, long effectTick) {
        Point.Float offsetSrc = this.sizeHandler.scalePointToGui(this, this.mousePosition, source);
        Point.Float offsetDst = this.sizeHandler.scalePointToGui(this, this.mousePosition, target);
        Color overlay = status.getPerkConnectionColor();

        double effectPart = (Math.sin(Math.toRadians(((effectTick) * 8) % 360D)) + 1D) / 4D;
        float br = 0.1F + 0.4F * (2F - ((float) effectPart));
        float rR = (overlay.getRed()   / 255F) * br;
        float rG = (overlay.getGreen() / 255F) * br;
        float rB = (overlay.getBlue()  / 255F) * br;
        float rA = (overlay.getAlpha() / 255F) * br;

        Vector3 fromStar = new Vector3(offsetSrc.x, offsetSrc.y, 0);
        Vector3 toStar   = new Vector3(offsetDst.x, offsetDst.y, 0);

        double width = 4.0D * this.sizeHandler.getScalingFactor();

        Vector3 dir = toStar.clone().subtract(fromStar);
        Vector3 degLot = dir.clone().cross(new Vector3(0, 0, 1)).normalize().mul(width);//.multiply(j == 0 ? 1 : -1);

        Vector3 vec00 = fromStar.clone().add(degLot);
        Vector3 vecV = degLot.clone().mul(-2);

        Matrix4f offset = renderStack.last().pose();
        for (int i = 0; i < 4; i++) {
            int u = ((i + 1) & 2) >> 1;
            int v = ((i + 2) & 2) >> 1;

            Vector3 pos = vec00.clone().add(dir.clone().mul(u)).add(vecV.clone().mul(v));
            pos.drawPos(offset, vb)
                    .color(rR, rG, rB, rA)
                    .tex(u, v)
                    .endVertex();
        }
    }

    @Override
    protected void mouseDragTick(double xpos, double ypos, double mouseDiffX, double mouseDiffY, double mouseOffsetX, double mouseOffsetY) {
        super.mouseDragTick(xpos, ypos, mouseDiffX, mouseDiffY, mouseOffsetX, mouseOffsetY);
        if (this.mouseSealStack.isEmpty()) {
            moveMouse((float) mouseDiffX, (float) mouseDiffY);
        }
    }

    @Override
    protected void mouseDragStop(double xpos, double ypos, double mouseDiffX, double mouseDiffY) {
        super.mouseDragStop(xpos, ypos, mouseDiffX, mouseDiffY);
        if (this.mouseSealStack.isEmpty()) {
            applyMovedMouseOffset();
        }
    }

    private void moveMouse(float changeX, float changeY) {
        if (this.previousMousePosition != null) {
            mousePosition.updateScaledPos(
                    sizeHandler.clampX(previousMousePosition.getScaledPosX() + changeX),
                    sizeHandler.clampY(previousMousePosition.getScaledPosY() + changeY),
                    sizeHandler.getScalingFactor());
        } else {
            mousePosition.updateScaledPos(
                    sizeHandler.clampX(changeX),
                    sizeHandler.clampY(changeY),
                    sizeHandler.getScalingFactor());
        }
    }

    private void applyMovedMouseOffset() {
        this.previousMousePosition = ScalingPoint.createPoint(
                this.mousePosition.getScaledPosX(),
                this.mousePosition.getScaledPosY(),
                this.sizeHandler.getScalingFactor(),
                true);
    }

    private void drawBackground(PoseStack renderStack) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableAlphaTest();
        RenderSystem.defaultAlphaFunc();
        TexturesAS.TEX_GUI_BACKGROUND_PERKS.bindTexture();
        RenderingUtils.draw(GL11.GL_QUADS, DefaultVertexFormat.POSITION_COLOR_TEX, buf -> {
            RenderingGuiUtils.rect(buf, renderStack, leftPos - 10, topPos - 10, this.getGuiZLevel(), guiWidth + 20, guiHeight + 20)
                    .color(0.5F ,0.5F, 0.5F, 1F)
                    .draw();
        });
        RenderSystem.disableAlphaTest();
    }

    private void updateSearchHighlight() {
        this.searchMatches.clear();

        String matchText = this.searchTextEntry.getText().toLowerCase(Locale.ROOT);
        if (matchText.length() < 3) return;
        for (PerkTreePoint<?> point : PerkTree.PERK_TREE.getPerkPoints(LogicalSide.CLIENT)) {
            AbstractPerk perk = point.getPerk();
            if (perk instanceof ProgressGatedPerk && !((ProgressGatedPerk) perk).canSeeClient()) {
                continue;
            }
            if (perk.getCategory().getName().getString().toLowerCase(Locale.ROOT).contains(matchText)) {
                this.searchMatches.add(perk);
            } else {
                for (MutableComponent tooltip : perk.getLocalizedTooltip()) {
                    if (tooltip.getString().toLowerCase(Locale.ROOT).contains(matchText)) {
                        this.searchMatches.add(perk);
                        break;
                    }
                }
            }
        }
        MutableComponent sealedInfo = Component.translatable("perk.info.astralsorcery.sealed");
        if (sealedInfo.getString().toLowerCase(Locale.ROOT).contains(matchText)) {
            PlayerProgress prog = ResearchHelper.getClientProgress();
            for (AbstractPerk sealed : prog.getPerkData().getSealedPerks()) {
                if (!this.searchMatches.contains(sealed)) {
                    this.searchMatches.add(sealed);
                }
            }
        }
    }

    private void closeSocketMenu() {
        this.socketMenu = null;
        this.rSocketMenu = null;
        this.slotsSocketMenu.clear();
    }

    @Override
    public boolean mouseReleased(double xpos, double ypos, int state) {
        if (super.mouseReleased(xpos, ypos, state)) {
            return true;
        }

        Player player = Minecraft.getInstance().player;

        if (!this.mouseSealStack.isEmpty()) {
            this.mouseSealStack = ItemStack.EMPTY;
            if (Minecraft.getInstance().player == null) {
                return false;
            }

            PlayerPerkData perkData = ResearchHelper.getClientProgress().getPerkData();
            for (Map.Entry<AbstractPerk, Rectangle.Float> rctPerk : this.thisFramePerks.entrySet()) {
                if (rctPerk.getValue().contains(xpos, ypos) && this.guiBox.isInBox(xpos - leftPos, ypos - topPos)) {
                    if (perkData.hasPerkEffect(rctPerk.getKey()) &&
                            !perkData.isPerkSealed(rctPerk.getKey()) &&
                            ItemPerkSeal.useSeal(player, true)) {
                        PktRequestPerkSealAction pkt = new PktRequestPerkSealAction(rctPerk.getKey(), true);
                        PacketChannel.CHANNEL.sendToServer(pkt);
                        return true;
                    }
                }
            }
        }

        if (this.unlockPrimed == null) {
            return false;
        }

        for (Map.Entry<AbstractPerk, Rectangle.Float> rctPerk : this.thisFramePerks.entrySet()) {
            if (this.unlockPrimed.equals(rctPerk.getKey()) && rctPerk.getValue().contains(xpos, ypos) && this.guiBox.isInBox(xpos - leftPos, ypos - topPos)) {
                AbstractPerk perk = rctPerk.getKey();
                PlayerProgress prog = ResearchHelper.getClientProgress();
                PlayerPerkData perkData = prog.getPerkData();
                if (!perkData.hasPerkAllocation(perk) && perk.mayUnlockPerk(prog, player)) {
                    PktUnlockPerk pkt = new PktUnlockPerk(false, rctPerk.getKey());
                    PacketChannel.CHANNEL.sendToServer(pkt);
                    break;
                }
            }
        }

        this.unlockPrimed = null;
        return true;
    }

    @Override
    public boolean mouseScrolled(double xpos, double ypos, double scroll) {
        if (scroll < 0) {
            this.sizeHandler.handleZoomOut();
            this.rescaleMouse();
            return true;
        }
        if (scroll > 0)  {
            this.sizeHandler.handleZoomIn();
            this.rescaleMouse();
            return true;
        }
        return false;
    }

    private void rescaleMouse() {
        this.mousePosition.rescale(this.sizeHandler.getScalingFactor());
        if (this.previousMousePosition != null) {
            this.previousMousePosition.rescale(this.sizeHandler.getScalingFactor());
        }
        this.moveMouse(0, 0);
    }

    @Override
    public boolean mouseDragged(double xpos, double ypos, int mouseButton, double dragX, double dragY) {
        this.unlockPrimed = null;

        return super.mouseDragged(xpos, ypos, mouseButton, dragX, dragY);
    }

    @Override
    protected boolean shouldRightClickCloseScreen(double xpos, double ypos) {
        if (rectSearchTextEntry.contains(xpos - leftPos, ypos - topPos)) {
            searchTextEntry.setText("");
            return false;
        }
        if (socketMenu != null &&
                rSocketMenu != null &&
                !rSocketMenu.contains(xpos, ypos)) {
            closeSocketMenu();
            return false;
        }

        for (Map.Entry<AbstractPerk, Rectangle.Float> rctPerk : this.thisFramePerks.entrySet()) {
            if (rctPerk.getValue().contains(xpos, ypos) && this.guiBox.isInBox(xpos - leftPos, ypos - topPos)) {
                AbstractPerk perk = rctPerk.getKey();
                if (perk instanceof GemSocketPerk) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean mouseClicked(double xpos, double ypos, int mouseButton) {
        if (super.mouseClicked(xpos, ypos, mouseButton)) {
            return true;
        }

        Minecraft mc = Minecraft.getInstance();

        if (socketMenu != null &&
                (mouseButton == 0 || mouseButton == 1) &&
                rSocketMenu != null &&
                !rSocketMenu.contains(xpos, ypos)) {
            closeSocketMenu();
        }

        if (mouseButton == 0) {
            if (socketMenu != null) {
                for (Rectangle.Float r : slotsSocketMenu.keySet()) {
                    if (r.contains(xpos, ypos) && !socketMenu.hasItem(mc.player, LogicalSide.CLIENT)) {
                        int slotId = slotsSocketMenu.get(r);
                        if (tryInsertGem(slotId, socketMenu)) {
                            return true;
                        }
                    }
                }
            }

            if (handleBookmarkClick(xpos, ypos)) {
                return true;
            }

            if (rectSealBox.contains(xpos - leftPos, ypos - topPos)) {
                if (!this.foundSeals.isEmpty()) {
                    this.mouseSealStack = new ItemStack(ItemsAS.PERK_SEAL);
                }
                return true;
            }

            if (rStatStar.contains(xpos, ypos)) {
                this.expectReinit = true;
                mc.displayGuiScreen(new ScreenJournalOverlayPerkStatistics(this));
                return true;
            }
        }

        PlayerProgress prog = ResearchHelper.getClientProgress();
        PlayerPerkData perkData = prog.getPerkData();
        for (Map.Entry<AbstractPerk, Rectangle.Float> rctPerk : this.thisFramePerks.entrySet()) {
            if (rctPerk.getValue().contains(xpos, ypos) && this.guiBox.isInBox(xpos - leftPos, ypos - topPos)) {
                AbstractPerk perk = rctPerk.getKey();
                if (mouseButton == 0 && mc.options.renderDebug && hasControlDown()) {
                    String perkKey = perk.getRegistryName().toString();
                    Minecraft.getInstance().keyboardHandler.setClipboardString(perkKey);
                    mc.player.sendSystemMessage(Component.translatable("astralsorcery.misc.ctrlcopy.copied", perkKey));
                    break;
                }
                if (mouseButton == 1) {
                    if (perkData.hasPerkEffect(perk) && perk instanceof GemSocketPerk) {
                        if (((GemSocketPerk) perk).hasItem(mc.player, LogicalSide.CLIENT)) {
                            PktPerkGemModification pkt = PktPerkGemModification.dropItem(perk);
                            PacketChannel.CHANNEL.sendToServer(pkt);
                            AstralSorcery.getProxy().scheduleClientside(() -> {
                                if (mc.screen == this) { //Only if user hasn't closed
                                    updateSearchHighlight();
                                }
                            }, 10);
                            SoundHelper.playSoundClient(SoundEvents.GLASS_PLACE, .35F, 9f);
                        } else {
                            this.socketMenu = (GemSocketPerk) perk;
                        }
                        return true;
                    }
                } else if (mouseButton == 0) {
                    if (perk.handleMouseClick(this, xpos, ypos)) {
                        return true;
                    }

                    if (!perkData.hasPerkAllocation(perk) && perk.mayUnlockPerk(prog, mc.player)) {
                        this.unlockPrimed = perk;
                    } else if (this.sealBreakPrimed != null && this.tickSealBreak > 0) {
                        PktRequestPerkSealAction pkt = new PktRequestPerkSealAction(perk, false);
                        PacketChannel.CHANNEL.sendToServer(pkt);
                    } else if (prog.getPerkData().isPerkSealed(perk)) {
                        this.sealBreakPrimed = perk;
                        this.tickSealBreak = 4;
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private <T extends AbstractPerk & GemSocketPerk> boolean tryInsertGem(int slotId, GemSocketPerk perk) {
        if (!(perk instanceof AbstractPerk)) {
            return false;
        }
        T socketPerk = (T) perk;

        ItemStack potentialStack = minecraft.player.inventory.getStackInSlot(slotId);
        if (!potentialStack.isEmpty() &&
                potentialStack.getItem() instanceof GemSocketItem) {
            GemSocketItem gemItem = (GemSocketItem) potentialStack.getItem();
            if (gemItem.canBeInserted(potentialStack, socketPerk, minecraft.player, ResearchHelper.getClientProgress(), LogicalSide.CLIENT)) {
                PktPerkGemModification pkt = PktPerkGemModification.insertItem(socketPerk, slotId);
                PacketChannel.CHANNEL.sendToServer(pkt);

                closeSocketMenu();
                SoundHelper.playSoundClient(SoundEvents.GLASS_PLACE, .35F, 9f);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (this.searchTextEntry.keyTyped(key)) {
            return true;
        }
        return super.keyPressed(key, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char charCode, int keyModifiers) {
        if (this.searchTextEntry.charTyped(charCode)) {
            return true;
        }
        return super.charTyped(charCode, keyModifiers);
    }

    public void playUnlockAnimation(AbstractPerk perk) {
        this.unlockEffects.put(perk, ClientScheduler.getClientTick());
        SoundHelper.playSoundClient(SoundsAS.PERK_UNLOCK, 0.3F, 1F);
    }

    public void playSealBreakAnimation(AbstractPerk perk) {
        this.updateSearchHighlight();
        this.breakEffects.put(perk, ClientScheduler.getClientTick());
        SoundHelper.playSoundClient(SoundsAS.PERK_UNSEAL, 0.3F, 1F);
    }

    public void playSealApplyAnimation(AbstractPerk perk) {
        this.updateSearchHighlight();
        SoundHelper.playSoundClient(SoundsAS.PERK_SEAL, 0.3F, 1F);
    }
}

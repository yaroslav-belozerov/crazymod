package tech.tarakoshka.mending_mastery;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.Util;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import tech.tarakoshka.mending_mastery.data.MyDataComponents;
import tech.tarakoshka.mending_mastery.items.wand.MagicWand;
import tech.tarakoshka.mending_mastery.network.ClientNetworkHandler;
import tech.tarakoshka.mending_mastery.util.ScreenPoint;
import tech.tarakoshka.mending_mastery.util.TrajPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static tech.tarakoshka.mending_mastery.items.wand.WandShapes.*;

public class ModHud implements HudElement {

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        if (mc.player.getMainHandItem().getItem() instanceof MagicWand) {
            handleMagicWand(guiGraphics, mc);
        }
        ;
    }

    private static final List<ScreenPoint> trajectoryPoints = new ArrayList<>();
    private static ScreenPoint lastStart;
    public static final int MAX_POINTS = 100;

    private static void handleMagicWand(GuiGraphics gui, Minecraft mc) {
        if (mc.player == null) return;

        var itemStack = mc.player.getMainHandItem();
        var isShooting = itemStack.get(MyDataComponents.WAND_SHOOTING);
        if (!Boolean.TRUE.equals(isShooting)) {
            trajectoryPoints.clear();
            return;
        }

        var w = mc.getWindow();
        var mx = (int) mc.mouseHandler.getScaledXPos(w);
        var my = (int) mc.mouseHandler.getScaledYPos(w);
        if (lastStart == null) {
            lastStart = new ScreenPoint(mx, my, Util.timeSource.get(TimeUnit.MILLISECONDS));
        } else if (trajectoryPoints.isEmpty()) {
            lastStart = null;
            return;
        }

        if (trajectoryPoints.size() > MAX_POINTS) {
            trajectoryPoints.removeFirst();
        }
        long currentTime = Util.timeSource.get(TimeUnit.MILLISECONDS);
        trajectoryPoints.removeIf(point -> currentTime - point.timestamp() > 2000);

        var normalX = mx - lastStart.x() + (w.getGuiScaledWidth() >> 1);
        var normalY = my - lastStart.y() + (w.getGuiScaledHeight() >> 1);
        var pt = new ScreenPoint(normalX, normalY, Util.timeSource.get(TimeUnit.MILLISECONDS));
        trajectoryPoints.add(pt);

        int width = gui.guiWidth() / 6;
        int ptHalfSize = 5;
        var spell = itemStack.get(MyDataComponents.VALID_SPELL);
        var isSpellValid = !(spell == null || spell.isBlank());
        if (isSpellValid) {
            gui.drawString(mc.font, spell, (gui.guiWidth() - mc.font.width(spell)) / 2, gui.guiHeight() / 2 + width, 0xFF000000);
            for (var e : shapes.entrySet()) {
                if (spell.equals(e.getKey())) {
                    for (TrajPoint p : e.getValue()) {
                        int x = (int) (p.x() * width + gui.guiWidth() / 2.0 - width / 2.0);
                        int y = (int) (p.y() * width + gui.guiHeight() / 2.0 - width / 2.0);
                        gui.fill(x - ptHalfSize, y - ptHalfSize, x + ptHalfSize, y + ptHalfSize, 0xFF00FF00);
                    }
                }
            }
        } else {
            for (var e : shapes.entrySet()) {
                var similar = calculateSimilarity(trajectoryPoints, e.getValue());
                if (similar < .3) {
                    ClientNetworkHandler.sendCustomPacket(e.getKey());
                    trajectoryPoints.clear();
                }
            }
            for (TrajPoint p : normalize(normalizeScale(trajectoryPoints), 30)) {
                int x = (int) (p.x() * width + gui.guiWidth() / 2.0 - width / 2.0);
                int y = (int) (p.y() * width + gui.guiHeight() / 2.0 - width / 2.0);
                gui.fill(x - ptHalfSize, y - ptHalfSize, x + ptHalfSize, y + ptHalfSize, 0xFFFF0000);
            }
        }
    }
}

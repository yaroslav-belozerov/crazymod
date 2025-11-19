package tech.tarakoshka.mymod;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.Util;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import tech.tarakoshka.mymod.data.MyDataComponents;
import tech.tarakoshka.mymod.items.Homer;
import tech.tarakoshka.mymod.network.ClientNetworkHandler;
import tech.tarakoshka.mymod.util.ScreenPoint;
import tech.tarakoshka.mymod.util.TrajPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.max;
import static tech.tarakoshka.mymod.util.Shapes.*;

public class MyHudElement implements HudElement {

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (!(mc.player.getMainHandItem().getItem() instanceof Homer item)) return;
        var shootingHomer = mc.player.getMainHandItem().get(MyDataComponents.SHOOTING_HOMER);
        if (!Boolean.TRUE.equals(shootingHomer)) {
            trajectoryPoints.clear();
            return;
        }
        var itemStack = mc.player.getMainHandItem();

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

        int width = guiGraphics.guiWidth() / 6;
        int ptHalfSize = 5;
        var spell = itemStack.get(MyDataComponents.VALID_SPELL);
        var isSpellValid = !(spell == null || spell.isBlank());
        if (isSpellValid) {
            guiGraphics.drawString(mc.font, spell, (guiGraphics.guiWidth() - mc.font.width(spell)) / 2, guiGraphics.guiHeight() / 2 + width, 0xFF000000);
            for (var e : shapes.entrySet()) {
                if (spell.equals(e.getKey())) {
                    for (TrajPoint p : e.getValue()) {
                        int x = (int) (p.x() * width + guiGraphics.guiWidth() / 2.0 - width / 2.0);
                        int y = (int) (p.y() * width + guiGraphics.guiHeight() / 2.0 - width / 2.0);
                        guiGraphics.fill(x - ptHalfSize, y - ptHalfSize, x + ptHalfSize, y + ptHalfSize, 0xFF00FF00);
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
                int x = (int) (p.x() * width + guiGraphics.guiWidth() / 2.0 - width / 2.0);
                int y = (int) (p.y() * width + guiGraphics.guiHeight() / 2.0 - width / 2.0);
                guiGraphics.fill(x - ptHalfSize, y - ptHalfSize, x + ptHalfSize, y + ptHalfSize, 0xFFFF0000);
            }
        }
    }


    private static final List<ScreenPoint> trajectoryPoints = new ArrayList<>();
    private static ScreenPoint lastStart;
    public static final int MAX_POINTS = 100;
}

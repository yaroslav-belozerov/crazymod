package tech.tarakoshka.mymod.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import org.apache.commons.lang3.tuple.Triple;
import tech.tarakoshka.mymod.MyMod;
import tech.tarakoshka.mymod.data.MyDataComponents;
import tech.tarakoshka.mymod.items.Homer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.Math.max;

public class Shapes {
    public static int SHAPE_POINTS = 30;

    public static void registerShapePayload() {
        PayloadTypeRegistry.playC2S().register(CustomStringPayload.TYPE, CustomStringPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(CustomStringPayload.TYPE, (payload, context) -> {
            String message = payload.message();
            context.server().execute(() -> {
                var hand = context.player().getMainHandItem();
                if (hand.getItem() instanceof Homer) {
                    hand.set(MyDataComponents.VALID_SPELL, message);
                }
                MyMod.LOGGER.info("received!!!:{}", message);
            });
        });
    }

    private static List<TrajPoint> swoosh() {
        ImmutableList.Builder<TrajPoint> builder = new ImmutableList.Builder<>();
        builder.add(new TrajPoint(0, 1));
        builder.add(new TrajPoint(1, 1));
        builder.add(new TrajPoint(0.5, 0));
        return normalize(builder.build(), SHAPE_POINTS);
    };

    private static List<TrajPoint> V() {
        ImmutableList.Builder<TrajPoint> builder = new ImmutableList.Builder<>();
        builder.add(new TrajPoint(0, 0));
        builder.add(new TrajPoint(0.5, 1));
        builder.add(new TrajPoint(1, 0));
        return normalize(builder.build(), SHAPE_POINTS);
    };

    private static List<TrajPoint> C() {
        ImmutableList.Builder<TrajPoint> builder = new ImmutableList.Builder<>();
        for (int i = 0; i < SHAPE_POINTS; i++) {
            double angle = Math.PI * 0.25 + (Math.PI * 1.5 * i / (SHAPE_POINTS - 1));
            builder.add(new TrajPoint(0.5 + 0.5 * Math.cos(angle), 0.5 + 0.5 * Math.sin(angle)));
        }
        return builder.build().reverse();
    }

    private static List<TrajPoint> circle() {
        ImmutableList.Builder<TrajPoint> builder = new ImmutableList.Builder<>();
        for (int i = 0; i < SHAPE_POINTS; i++) {
            double angle = 2 * Math.PI * i / SHAPE_POINTS;
            builder.add(new TrajPoint(0.5 + 0.5 * Math.cos(angle), 0.5 + 0.5 * Math.sin(angle)));
        }
        return builder.build();
    };

    private static Map<String, List<TrajPoint>> getShapes() {
        ImmutableMap.Builder<String, List<TrajPoint>> builder = new ImmutableMap.Builder<>();
        builder.put("swoosh", swoosh());
        builder.put("circle", circle());
        builder.put("C", C());
        builder.put("V", V());
        return builder.build();
    }

    public static Triple<Float, Float, Float> mapColorFromSpell(String spellName) {
        return switch (spellName) {
            case "swoosh" -> Triple.of(0f, 1f, 0f);
            case "circle" -> Triple.of(0f, 0f, 1f);
            case "C" -> Triple.of(1f, 0f, 1f);
            case "V" -> Triple.of(0f, 1f, 1f);
            case null, default -> Triple.of(1f, 1f, 1f);
        };
    }


    public static Map<String, List<TrajPoint>> shapes = getShapes();

    public static double calculateSimilarity(List<ScreenPoint> path, List<TrajPoint> normalPath) {
        if (path.isEmpty() || normalPath.isEmpty()) return Double.MAX_VALUE;

        List<TrajPoint> normalized = normalize(normalizeScale(path), SHAPE_POINTS);

        if (normalized.size() < SHAPE_POINTS || normalPath.size() < SHAPE_POINTS) {
            return Double.MAX_VALUE;
        }

        double totalDistance = 0;
        for (int i = 0; i < SHAPE_POINTS; i++) {
            totalDistance += normalized.get(i).distance(normalPath.get(i));
        }

        return totalDistance / SHAPE_POINTS;
    }

    public static List<TrajPoint> normalizeScale(List<ScreenPoint> path) {
        if (path.isEmpty()) return new ArrayList<>();

        double minX = Double.MAX_VALUE, maxX = -Double.MAX_VALUE;
        double minY = Double.MAX_VALUE, maxY = -Double.MAX_VALUE;

        for (ScreenPoint p : path) {
            minX = Math.min(minX, p.x());
            maxX = max(maxX, p.x());
            minY = Math.min(minY, p.y());
            maxY = max(maxY, p.y());
        }

        double width = maxX - minX;
        double height = maxY - minY;
        double scale = max(width, height);

        if (scale == 0) scale = 1;

        List<TrajPoint> normalized = new ArrayList<>();
        for (ScreenPoint p : path) {
            double nx = (p.x() - minX) / scale;
            double ny = (p.y() - minY) / scale;
            normalized.add(new TrajPoint(nx, ny));
        }

        return normalized;
    }

    public static List<TrajPoint> normalize(List<TrajPoint> path, int targetPoints) {
        if (path.size() < 2) return new ArrayList<>(path);

        double totalLength = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            totalLength += path.get(i).distance(path.get(i + 1));
        }

        List<TrajPoint> normalized = new ArrayList<>();
        normalized.add(path.getFirst());

        double targetSegmentLength = totalLength / (targetPoints - 1);
        double accumulatedLength = 0;

        int pathIndex = 0;

        for (int i = 1; i < targetPoints - 1; i++) {
            double targetLength = i * targetSegmentLength;

            while (pathIndex < path.size() - 1) {
                double segmentLength = path.get(pathIndex).distance(path.get(pathIndex + 1));

                if (accumulatedLength + segmentLength >= targetLength) {
                    double remaining = targetLength - accumulatedLength;
                    double t = remaining / segmentLength;

                    TrajPoint p1 = path.get(pathIndex);
                    TrajPoint p2 = path.get(pathIndex + 1);
                    TrajPoint interpolated = new TrajPoint(
                            p1.x() + t * (p2.x() - p1.x()),
                            p1.y() + t * (p2.y() - p1.y())
                    );
                    normalized.add(interpolated);
                    break;
                }

                accumulatedLength += segmentLength;
                pathIndex++;
            }
        }

        normalized.add(path.getLast());
        return normalized;
    }
}
package tech.tarakoshka.mymod.util;

public record ScreenPoint(int x, int y, long timestamp) {
    public double distance(ScreenPoint other) {
        int dx = x - other.x;
        int dy = y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }
}


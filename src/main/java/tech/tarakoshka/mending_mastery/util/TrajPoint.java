package tech.tarakoshka.mending_mastery.util;


public record TrajPoint(double x, double y) {
    public double distance(TrajPoint other) {
        double dx = x - other.x;
        double dy = y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }
}

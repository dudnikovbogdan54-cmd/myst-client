package dev.myst.client.module.marks;

/** Persistent waypoint data. Public fields are intentionally Gson-friendly. */
public final class Waypoint {
    public String name;
    public double x, y, z;
    public String dimension;
    public int color;
    public boolean enabled;

    public Waypoint() { this("", 0, 0, 0, "minecraft:overworld", 0xFFB58AFF, true); }

    public Waypoint(String name, double x, double y, double z, String dimension, int color, boolean enabled) {
        this.name = name; this.x = x; this.y = y; this.z = z;
        this.dimension = dimension; this.color = color; this.enabled = enabled;
    }
}

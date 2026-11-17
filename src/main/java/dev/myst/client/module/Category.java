
package dev.myst.client.module;
public enum Category { VISUALS("Visuals"), WORLD("World"), HUD("HUD"), MARKS("Marks"), SETTINGS("Settings"); private final String label; Category(String label){this.label=label;} public String label(){return label;} }

package dev.myst.client;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
/** Small configurable crosshair rendered by Minecraft's GUI pipeline. */
public final class CustomCrosshair {
 private static boolean enabled=true;private static int color=0xFFFFFFFF,size=5,gap=3,thickness=1;
 private CustomCrosshair(){}
 public static void render(DrawContext ctx,RenderTickCounter tickCounter){if(!enabled)return;MinecraftClient mc=MinecraftClient.getInstance();if(mc.player==null||mc.options.hudHidden)return;int x=mc.getWindow().getScaledWidth()/2,y=mc.getWindow().getScaledHeight()/2,arm=Math.max(1,size),off=Math.max(0,gap);ctx.drawText(mc.textRenderer,"—",x-off-arm/2-3,y-4,color,false);ctx.drawText(mc.textRenderer,"—",x+off+arm/2-1,y-4,color,false);if(thickness>1){ctx.drawText(mc.textRenderer,"│",x-1,y-off-arm/2-5,color,false);ctx.drawText(mc.textRenderer,"│",x-1,y+off+arm/2-3,color,false);}else ctx.drawText(mc.textRenderer,"·",x-1,y-4,color,false);}
 public static boolean isEnabled(){return enabled;}public static void setEnabled(boolean v){enabled=v;}public static void toggle(){enabled=!enabled;}
 public static int getColor(){return color;}public static void setColor(int v){color=v;}public static int getSize(){return size;}public static void setSize(int v){size=Math.max(1,Math.min(16,v));}
 public static int getGap(){return gap;}public static void setGap(int v){gap=Math.max(0,Math.min(16,v));}public static int getThickness(){return thickness;}public static void setThickness(int v){thickness=Math.max(1,Math.min(4,v));}
}
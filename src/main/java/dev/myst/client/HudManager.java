package dev.myst.client;

import dev.myst.client.module.ModuleManager;
import dev.myst.client.render.MathUtil;
import dev.myst.client.render.Render2D;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.LivingEntity;
import java.util.Locale;

public final class HudManager {
 private static boolean ready;
 private static float alpha,hp=1,damage=1;
 private HudManager(){}
 public static void initialize(){if(ready)return;ready=true;HudRenderCallback.EVENT.register((ctx,tick)->render(ctx));}
 private static int[] pos(String id,int x,int y){return ConfigManager.HUD.computeIfAbsent(id,k->new int[]{x,y});}
 public static void render(DrawContext c){
  MinecraftClient mc=MinecraftClient.getInstance();
  if(mc.player==null||mc.world==null||mc.options.hudHidden)return;
  if(ModuleManager.enabled("Watermark")){
   int[] p=pos("watermark",8,8);
   String s="MYST VISUALS  •  "+mc.player.getName().getString()+"  •  "+mc.getCurrentFps()+" FPS  •  "+ping(mc)+" ms";
   int w=mc.textRenderer.getWidth(s)+18;
   Render2D.drawShadow(c,p[0],p[1],w,22,11,0x99000000,4,4);
   Render2D.drawRoundedRect(c,p[0],p[1],w,22,11,0xEB111217);
   Render2D.drawRoundedRect(c,p[0]+8,p[1]+18,w-16,2,1,MathUtil.gradient((System.currentTimeMillis()%4000)/4000f,0xFF9B68FF,0xFF4EDCF2));
   c.drawText(mc.textRenderer,s,p[0]+9,p[1]+7,0xFFF4F1FF,false);
  }
  if(ModuleManager.enabled("Info HUD")){
   int[] p=pos("info",8,36);
   String[] a={"X "+fmt(mc.player.getX()),"Y "+fmt(mc.player.getY()),"Z "+fmt(mc.player.getZ()),String.format(Locale.ROOT,"%.2f b/s",Math.hypot(mc.player.getVelocity().x,mc.player.getVelocity().z)*20),direction(mc.player.getYaw())};
   int x=p[0];for(String v:a){int w=mc.textRenderer.getWidth(v)+16;Render2D.drawRoundedRect(c,x,p[1],w,20,10,0xD9111219);c.drawText(mc.textRenderer,v,x+8,p[1]+6,0xFFEDEAF5,false);x+=w+5;}
  }
  renderTarget(c,mc);
 }
 private static void renderTarget(DrawContext c,MinecraftClient mc){
  LivingEntity t=mc.targetedEntity instanceof LivingEntity e&&e.isAlive()?e:null;
  alpha=MathUtil.fastLerp(alpha,t==null?0:1,10,1/60f);
  if(!ModuleManager.enabled("Target HUD")||alpha<.02f)return;
  int[] p=pos("target",mc.getWindow().getScaledWidth()/2+18,mc.getWindow().getScaledHeight()/2+20);
  String name=t==null?"":t.getName().getString();
  float h=t==null?0:t.getHealth()/Math.max(1,t.getMaxHealth());
  if(t!=null){if(h<hp)damage=hp;hp=MathUtil.fastLerp(hp,h,12,1/60f);damage=MathUtil.fastLerp(damage,h,3,1/60f);}
  int w=166;
  Render2D.drawShadow(c,p[0],p[1],w,52,10,0x99000000,4,4);
  Render2D.drawRoundedRect(c,p[0],p[1],w,52,10,0xEB111217);
  Render2D.drawRoundedRect(c,p[0]+6,p[1]+6,36,40,8,0xFF292333);
  if(t!=null){
   c.enableScissor(p[0]+7,p[1]+7,p[0]+41,p[1]+46);
   try{InventoryScreen.drawEntity(c,p[0]+7,p[1]+7,p[0]+41,p[1]+46,20,0.0625f,0,0,t);}
   finally{c.disableScissor();}
  }else c.drawText(mc.textRenderer,"?",p[0]+20,p[1]+21,0xFFFFFFFF,false);
  c.drawText(mc.textRenderer,name.isEmpty()?"No target":name,p[0]+48,p[1]+9,0xFFF5F2FF,false);
  c.drawText(mc.textRenderer,t==null?"":String.format(Locale.ROOT,"%.1f / %.1f HP",t.getHealth(),t.getMaxHealth()),p[0]+48,p[1]+23,0xFFBEB7CA,false);
  Render2D.drawRoundedRect(c,p[0]+48,p[1]+39,108,6,3,0xFF393342);
  Render2D.drawRoundedRect(c,p[0]+48,p[1]+39,(int)(108*Math.max(0,Math.min(1,damage))),6,3,0xFFB65D79);
  Render2D.drawRoundedRect(c,p[0]+48,p[1]+39,(int)(108*Math.max(0,Math.min(1,hp))),6,3,0xFF7DE0B2);
 }
 private static int ping(MinecraftClient c){var n=c.getNetworkHandler();var e=n==null?null:n.getPlayerListEntry(c.player.getUuid());return e==null?0:e.getLatency();}
 private static String fmt(double n){return String.format(Locale.ROOT,"%.1f",n);}
 private static String direction(float y){return new String[]{"S","W","N","E"}[Math.floorMod((int)Math.floor(y/90+0.5),4)];}
}

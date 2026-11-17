
package dev.myst.client.module;
import dev.myst.client.CustomCrosshair;import dev.myst.client.module.Module.*;import dev.myst.client.module.marks.Waypoints;import dev.myst.client.module.visuals.*;import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;import net.minecraft.client.MinecraftClient;import org.lwjgl.glfw.GLFW;import java.util.*;
public final class ModuleManager{
 private static final List<Module> MODULES=new ArrayList<>();private static final Set<Integer> DOWN=new HashSet<>();private static boolean initialized;private ModuleManager(){}
 public static void initialize(){if(initialized)return;initialized=true;
  add("Fullbright","Повышенная яркость мира",Category.VISUALS,false,0,null);
  add("Custom Crosshair","Прицел с настройкой цвета",Category.VISUALS,true,0,CustomCrosshair::setEnabled);
  add("Jump Circles","Неоновые кольца при прыжке",Category.VISUALS,false,0,JumpCircles::setEnabled);
  add("Trails","Плавный след игрока",Category.VISUALS,false,0,Trails::setEnabled);
  add("China Hat","Неоновый конус над игроком",Category.VISUALS,false,0,ChinaHat::setEnabled);
  add("Waypoints","3D-метки и ориентиры",Category.MARKS,true,0,v->Waypoints.setEnabled(v));
  add("Watermark","Информация о клиенте",Category.HUD,true,0,null);
  add("Info HUD","XYZ, скорость и направление",Category.HUD,true,0,null);
  add("Target HUD","Плавная панель цели",Category.HUD,true,0,null);
  find("Custom Crosshair").add(new ColorSetting("Color",CustomCrosshair.getColor()));find("Custom Crosshair").add(new SliderSetting("Size",5,1,16,1));
  ClientTickEvents.END_CLIENT_TICK.register(ModuleManager::tick);
 }
 private static void add(String n,String d,Category c,boolean e,int k,java.util.function.Consumer<Boolean> f){MODULES.add(new Module(n,d,c,e,k,f));}
 public static List<Module> all(){return Collections.unmodifiableList(MODULES);}public static Module find(String n){return MODULES.stream().filter(m->m.name().equalsIgnoreCase(n)).findFirst().orElse(null);}
 public static boolean enabled(String n){Module m=find(n);return m!=null&&m.enabled();}
 private static void tick(MinecraftClient mc){if(mc.player==null)return;Set<Integer> down=new HashSet<>();for(Module m:MODULES){int key=m.keyCode();if(key>0&&GLFW.glfwGetKey(mc.getWindow().getHandle(),key)==GLFW.GLFW_PRESS){down.add(key);if(!DOWN.contains(key)){m.toggle();dev.myst.client.ConfigManager.save();}}}DOWN.clear();DOWN.addAll(down);}
}

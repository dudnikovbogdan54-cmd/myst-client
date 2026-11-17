
package dev.myst.client.module;
import java.util.*;
public class Module {
 public abstract static class Setting<T>{private final String name;private T value;protected Setting(String n,T v){name=n;value=v;}public String name(){return name;}public T get(){return value;}public void set(T v){value=v;}}
 public static final class BooleanSetting extends Setting<Boolean>{public BooleanSetting(String n,boolean v){super(n,v);}public void toggle(){set(!get());}}
 public static final class SliderSetting extends Setting<Double>{private final double min,max,step;public SliderSetting(String n,double v,double min,double max,double step){super(n,v);this.min=min;this.max=max;this.step=step;}public double min(){return min;}public double max(){return max;}public double step(){return step;}@Override public void set(Double v){super.set(Math.max(min,Math.min(max,min+Math.round((v-min)/step)*step)));}}
 public static final class ColorSetting extends Setting<Integer>{public ColorSetting(String n,int v){super(n,v);}}
 public static final class ModeSetting extends Setting<String>{private final List<String> modes;public ModeSetting(String n,String v,String... modes){super(n,v);this.modes=List.of(modes);}public List<String> modes(){return modes;}public void next(){if(!modes.isEmpty())set(modes.get((modes.indexOf(get())+1)%modes.size()));}}
 private final String name,description;private final Category category;private final List<Setting<?>> settings=new ArrayList<>();private boolean enabled;private int keyCode;private final java.util.function.Consumer<Boolean> toggle;
 public Module(String n,String d,Category c,boolean e,int key,java.util.function.Consumer<Boolean> toggle){name=n;description=d;category=c;enabled=e;keyCode=key;this.toggle=toggle;if(toggle!=null)toggle.accept(e);}
 public String name(){return name;}public String description(){return description;}public Category category(){return category;}public boolean enabled(){return enabled;}public int keyCode(){return keyCode;}public void keyCode(int k){keyCode=Math.max(0,k);}
 public void setEnabled(boolean v){if(enabled==v)return;enabled=v;if(toggle!=null)toggle.accept(v);}public void toggle(){setEnabled(!enabled);}
 public List<Setting<?>> settings(){return Collections.unmodifiableList(settings);}public <T extends Setting<?>> T add(T s){settings.add(s);return s;}
}

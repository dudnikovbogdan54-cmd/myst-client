
package dev.myst.client;
import com.google.gson.*;import dev.myst.client.module.*;import dev.myst.client.module.Module;import dev.myst.client.module.Module.*;import net.fabricmc.loader.api.FabricLoader;import java.nio.file.*;import java.util.*;
public final class ConfigManager{
 private static final Gson G=new GsonBuilder().setPrettyPrinting().create();private static final Path FILE=FabricLoader.getInstance().getConfigDir().resolve("mystclient.json");public static final Map<String,int[]> HUD=new LinkedHashMap<>();
 private static final class Data{Map<String,Mod> modules=new LinkedHashMap<>();Map<String,int[]> hud=new LinkedHashMap<>();}private static final class Mod{boolean enabled;int key;Map<String,JsonElement> settings=new LinkedHashMap<>();}
 private ConfigManager(){}
 public static void initialize(){HUD.putIfAbsent("watermark",new int[]{8,8});HUD.putIfAbsent("info",new int[]{8,36});HUD.putIfAbsent("target",new int[]{8,72});load();}
 public static void load(){try{if(!Files.exists(FILE)){save();return;}Data d=G.fromJson(Files.readString(FILE),Data.class);if(d==null)return;if(d.hud!=null)HUD.putAll(d.hud);if(d.modules!=null)for(Module m:ModuleManager.all()){Mod x=d.modules.get(m.name());if(x==null)continue;m.keyCode(x.key);m.setEnabled(x.enabled);for(Setting<?> st:m.settings()){JsonElement e=x.settings.get(st.name());if(e==null)continue;if(st instanceof BooleanSetting b)b.set(e.getAsBoolean());else if(st instanceof SliderSetting sl)sl.set(e.getAsDouble());else if(st instanceof ColorSetting co)co.set(e.getAsInt());else if(st instanceof ModeSetting mo)mo.set(e.getAsString());}}}catch(Exception e){System.err.println("[Myst] config load: "+e);}}
 public static void save(){try{Data d=new Data();for(Module m:ModuleManager.all()){Mod x=new Mod();x.enabled=m.enabled();x.key=m.keyCode();for(Setting<?> st:m.settings())x.settings.put(st.name(),G.toJsonTree(st.get()));d.modules.put(m.name(),x);}d.hud.putAll(HUD);Files.createDirectories(FILE.getParent());Files.writeString(FILE,G.toJson(d));}catch(Exception e){System.err.println("[Myst] config save: "+e);}}
}


package dev.myst.client;
import dev.myst.client.module.ModuleManager;
import dev.myst.client.module.marks.Waypoints;
import dev.myst.client.module.visuals.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
public final class MystClient implements ClientModInitializer {
 public static final String MOD_ID="mystclient"; private boolean kDown,shiftDown;
 @Override public void onInitializeClient(){
  StreamerConfig.load(); ModuleManager.initialize(); ConfigManager.initialize();
  Waypoints.initialize(); JumpCircles.initialize(); Trails.initialize(); ChinaHat.initialize(); HudManager.initialize();
  ClientCommandRegistrationCallback.EVENT.register((dispatcher,registry)->dispatcher.register(ClientCommandManager.literal("mystgui").executes(ctx->{net.minecraft.client.MinecraftClient.getInstance().execute(()->net.minecraft.client.MinecraftClient.getInstance().setScreen(new ClickGuiScreen()));return 1;})));
  ClientTickEvents.END_CLIENT_TICK.register(client->{if(client.player==null)return;long w=client.getWindow().getHandle();boolean k=GLFW.glfwGetKey(w,GLFW.GLFW_KEY_K)==GLFW.GLFW_PRESS;boolean r=GLFW.glfwGetKey(w,GLFW.GLFW_KEY_RIGHT_SHIFT)==GLFW.GLFW_PRESS;if((k&&!kDown)||(r&&!shiftDown)){if(client.currentScreen instanceof ClickGuiScreen)client.setScreen(null);else client.setScreen(new ClickGuiScreen());}kDown=k;shiftDown=r;});
 }
}

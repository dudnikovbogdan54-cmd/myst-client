package dev.myst.client;

import dev.myst.client.module.*;
import dev.myst.client.module.Module;
import dev.myst.client.module.Module.*;
import dev.myst.client.render.Render2D;
import net.minecraft.client.gui.*;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import java.awt.Color;
import java.util.*;

public final class ClickGuiScreen extends Screen {
 private Category tab=Category.VISUALS;
 private int scroll,maxScroll;
 private double smooth;
 private Module binding;
 private int x,y,w,h,contentBottom;
 private Control dragging;
 private final List<Control> controls=new ArrayList<>();
 private static final int BG=0xEB111217,CARD=0xFF1B1D26,WHITE=0xFFF4F2F9,MUTED=0xFF9997A8,ACCENT=0xFFAA79FF;
 private record Control(Setting<?> setting,String kind,int x,int y,int w,int h){}
 public ClickGuiScreen(){super(Text.literal("Myst Visuals"));}
 @Override protected void init(){w=Math.min(620,width-16);h=Math.min(390,height-16);x=(width-w)/2;y=(height-h)/2;}
 @Override public void render(DrawContext c,int mx,int my,float dt){
  c.fill(0,0,width,height,0x77000000);
  Render2D.drawShadow(c,x,y,w,h,15,0xAA000000,7,6);
  Render2D.drawRoundedRect(c,x,y,w,h,15,BG);
  Render2D.drawRoundedOutline(c,x,y,w,h,15,1,0xFF383744);
  Render2D.drawRoundedRect(c,x+14,y+14,36,36,10,ACCENT);
  c.drawText(textRenderer,"M",x+27,y+27,WHITE,false);
  c.drawText(textRenderer,"MYST VISUALS",x+60,y+16,WHITE,false);
  c.drawText(textRenderer,"CLIENT CONTROL CENTER",x+60,y+29,MUTED,false);
  Category[] tabs={Category.VISUALS,Category.WORLD,Category.HUD,Category.MARKS,Category.SETTINGS};
  for(int i=0;i<tabs.length;i++){int yy=y+66+i*38;boolean a=tab==tabs[i];if(a)Render2D.drawRoundedRect(c,x+14,yy,116,31,8,0xFF292235);if(a)Render2D.drawRoundedRect(c,x+14,yy+5,3,21,2,ACCENT);c.drawText(textRenderer,tabs[i].label(),x+27,yy+11,a?WHITE:MUTED,false);}
  int left=x+146,top=y+66,bottom=y+h-18;
  c.drawText(textRenderer,tab.label().toUpperCase(Locale.ROOT),left,y+25,WHITE,false);
  c.enableScissor(left,top,x+w-16,bottom);
  controls.clear();
  int yy=top+4-(int)smooth;
  for(Module m:ModuleManager.all())if(m.category()==tab){
   int settingsHeight=0;for(Setting<?> s:m.settings())settingsHeight+=s instanceof ColorSetting?58:s instanceof SliderSetting?38:28;
   int ch=47+settingsHeight;
   Render2D.drawRoundedRect(c,left,yy,w-162,ch,10,CARD);
   c.drawText(textRenderer,m.name(),left+13,yy+9,WHITE,false);
   c.drawText(textRenderer,m.description(),left+13,yy+24,MUTED,false);
   Render2D.drawRoundedRect(c,x+w-70,yy+10,40,20,10,m.enabled()?ACCENT:0xFF44434E);
   Render2D.drawRoundedRect(c,x+w-(m.enabled()?48:68),yy+12,16,16,8,0xFFF7F5FC);
   int row=yy+43;
   for(Setting<?> setting:m.settings()){
    if(setting instanceof BooleanSetting b){
     c.drawText(textRenderer,setting.name(),left+13,row+5,WHITE,false);
     String val=b.get()?"ON":"OFF";int tw=textRenderer.getWidth(val);
     c.drawText(textRenderer,val,left+w-180-tw,row+5,b.get()?0xFF8DE5B4:MUTED,false);
     controls.add(new Control(setting,"boolean",left+10,row,w-185,22));row+=28;
    }else if(setting instanceof ModeSetting mode){
     c.drawText(textRenderer,setting.name(),left+13,row+5,WHITE,false);
     String val=mode.get();int tw=textRenderer.getWidth(val);
     Render2D.drawRoundedRect(c,left+w-180-tw-20,row,tw+18,21,7,0xFF302B3B);
     c.drawText(textRenderer,val,left+w-171-tw,row+6,ACCENT,false);
     controls.add(new Control(setting,"mode",left+10,row,w-185,22));row+=28;
    }else if(setting instanceof SliderSetting slider){
     c.drawText(textRenderer,setting.name(),left+13,row+2,WHITE,false);
     String val=String.format(Locale.ROOT,"%.2f",slider.get());c.drawText(textRenderer,val,left+w-180-textRenderer.getWidth(val),row+2,0xFFD6D0E2,false);
     int sx=left+13,sw=w-202,sy=row+21;
     Render2D.drawRoundedRect(c,sx,sy,sw,5,3,0xFF383442);
     float f=(float)((slider.get()-slider.min())/(slider.max()-slider.min()));
     Render2D.drawRoundedRect(c,sx,sy,Math.max(3,(int)(sw*f)),5,3,ACCENT);
     Render2D.drawRoundedRect(c,sx+(int)(sw*f)-5,sy-3,10,11,5,0xFFF6F1FF);
     controls.add(new Control(setting,"slider",sx,sy-6,sw,17));row+=38;
    }else if(setting instanceof ColorSetting color){
     c.drawText(textRenderer,setting.name(),left+13,row+2,WHITE,false);
     int selected=color.get();
     Render2D.drawRoundedRect(c,left+w-185,row,17,17,9,selected);
     c.drawText(textRenderer,String.format("#%08X",selected),left+w-162,row+5,0xFFD6D0E2,false);
     int hx=left+13,hw=w-202,hy=row+25;
     for(int i=0;i<24;i++){int col=Color.HSBtoRGB(i/24f,1,1)|0xFF000000;int sx=hx+i*hw/24;Render2D.drawRoundedRect(c,sx,hy,Math.max(2,hw/24+1),7,3,col);}
     float[] hsb=Color.RGBtoHSB((selected>>16)&255,(selected>>8)&255,selected&255,null);
     int hxPos=hx+(int)(hsb[0]*hw);Render2D.drawRoundedOutline(c,hxPos-3,hy-2,6,11,3,1,0xFFFFFFFF);
     int ay=hy+13;Render2D.drawRoundedRect(c,hx,ay,hw,6,3,0xFF484650);
     Render2D.drawRoundedRect(c,hx,ay,(int)(hw*((selected>>>24)&255)/255f),6,3,selected|0xFF000000);
     controls.add(new Control(setting,"hue",hx,hy-3,hw,13));
     controls.add(new Control(setting,"alpha",hx,ay-3,hw,13));
     row+=58;
    }
   }
   yy+=ch+9;
  }
  contentBottom=yy;
  maxScroll=Math.max(0,contentBottom-(bottom-4));
  scroll=Math.max(0,Math.min(scroll,maxScroll));
  c.disableScissor();
  smooth+=(scroll-smooth)*.28;
  Render2D.drawRoundedRect(c,x+w-144,y+h-43,126,27,8,0xFF292235);
  c.drawText(textRenderer,"HUD EDITOR",x+w-129,y+h-33,WHITE,false);
  c.drawText(textRenderer,"K / RIGHT SHIFT • ESC",x+18,y+h-31,MUTED,false);
  super.render(c,mx,my,dt);
 }
 private void updateControl(Control control,double mouseX){
  double f=Math.max(0,Math.min(1,(mouseX-control.x())/Math.max(1.0,control.w())));
  Setting<?> s=control.setting();
  if(s instanceof SliderSetting slider){slider.set(slider.min()+f*(slider.max()-slider.min()));if("Size".equalsIgnoreCase(slider.name()))CustomCrosshair.setSize((int)Math.round(slider.get()));}
  else if(s instanceof ColorSetting color){
   int old=color.get();int a=(old>>>24)&255,r=(old>>>16)&255,g=(old>>>8)&255,b=old&255;
   float[] hsb=Color.RGBtoHSB(r,g,b,null);
   if(control.kind().equals("hue")){int rgb=Color.HSBtoRGB((float)f,Math.max(.55f,hsb[1]),Math.max(.65f,hsb[2]));color.set((a<<24)|(rgb&0xFFFFFF));}
   else color.set(((int)(f*255)<<24)|(old&0xFFFFFF));
   CustomCrosshair.setColor(color.get());
  }
  ConfigManager.save();
 }
 @Override public boolean mouseClicked(Click click,boolean dbl){
  int mx=(int)click.x(),my=(int)click.y();
  if(click.button()==0){
   if(mx>=x+w-150&&my>=y+h-48){client.setScreen(new HudEditorScreen());return true;}
   for(Control control:controls)if(mx>=control.x()&&mx<=control.x()+control.w()&&my>=control.y()&&my<=control.y()+control.h()){
    if(control.kind().equals("slider")||control.kind().equals("hue")||control.kind().equals("alpha")){dragging=control;updateControl(control,mx);return true;}
    if(control.setting() instanceof BooleanSetting b)b.toggle();
    else if(control.setting() instanceof ModeSetting mode)mode.next();
    ConfigManager.save();return true;
   }
   Category[] tabs={Category.VISUALS,Category.WORLD,Category.HUD,Category.MARKS,Category.SETTINGS};
   for(int i=0;i<tabs.length;i++)if(mx>=x+10&&mx<=x+138&&my>=y+64+i*38&&my<=y+95+i*38){tab=tabs[i];scroll=0;smooth=0;return true;}
   int left=x+146,yy=y+70-(int)smooth;
   for(Module m:ModuleManager.all())if(m.category()==tab){int sh=0;for(Setting<?> s:m.settings())sh+=s instanceof ColorSetting?58:s instanceof SliderSetting?38:28;int ch=47+sh;
    if(mx>=left&&mx<=x+w-16&&my>=yy&&my<=yy+ch){
     if(binding==m){binding=null;return true;}
     if(mx>x+w-78&&my<yy+40){m.toggle();ConfigManager.save();return true;}
     if(my<yy+40&&mx>left+100){m.toggle();ConfigManager.save();return true;}
     if(my<yy+42){binding=m;return true;}
    }yy+=ch+9;
   }
  }
  return super.mouseClicked(click,dbl);
 }
 @Override public boolean mouseDragged(Click click,double dx,double dy){if(dragging!=null&&click.button()==0){updateControl(dragging,click.x());return true;}return super.mouseDragged(click,dx,dy);}
 @Override public boolean mouseReleased(Click click){if(dragging!=null){dragging=null;ConfigManager.save();return true;}return super.mouseReleased(click);}
 @Override public boolean keyPressed(KeyInput input){int key=input.key();if(binding!=null){binding.keyCode(key==GLFW.GLFW_KEY_ESCAPE||key==GLFW.GLFW_KEY_DELETE?0:key);binding=null;ConfigManager.save();return true;}if(key==GLFW.GLFW_KEY_ESCAPE){close();return true;}return super.keyPressed(input);}
 @Override public boolean mouseScrolled(double mx,double my,double hx,double vy){scroll=Math.max(0,Math.min(maxScroll,scroll-(int)(vy*28)));return true;}
 @Override public boolean shouldPause(){return false;}
}

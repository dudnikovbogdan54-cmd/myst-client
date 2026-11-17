
package dev.myst.client;
import dev.myst.client.render.Render2D;import net.minecraft.client.gui.*;import net.minecraft.client.input.KeyInput;import net.minecraft.client.gui.screen.Screen;import net.minecraft.text.Text;
public final class HudEditorScreen extends Screen{private String drag;private int ox,oy;public HudEditorScreen(){super(Text.literal("Myst HUD Editor"));}
 @Override public void render(DrawContext c,int mx,int my,float d){c.fill(0,0,width,height,0x88000000);c.drawCenteredTextWithShadow(textRenderer,"HUD EDITOR • перетаскивай элементы",width/2,12,0xFFFFFFFF);draw(c,"watermark","WATERMARK",8,8,190,24);draw(c,"info","INFO HUD",8,40,180,24);draw(c,"target","TARGET HUD",8,72,160,44);super.render(c,mx,my,d);}
 private void draw(DrawContext c,String id,String label,int x,int y,int w,int h){int[] p=ConfigManager.HUD.computeIfAbsent(id,k->new int[]{x,y});Render2D.drawShadow(c,p[0],p[1],w,h,8,0xAA000000,3,3);Render2D.drawRoundedRect(c,p[0],p[1],w,h,8,0xDD17151F);Render2D.drawRoundedOutline(c,p[0],p[1],w,h,8,1,0xFFB58AFF);c.drawText(textRenderer,label,p[0]+8,p[1]+(h-8)/2,0xFFFFFFFF,false);}
 @Override public boolean mouseClicked(Click click,boolean dbl){if(click.button()==0){int x=(int)click.x(),y=(int)click.y();for(String id:new String[]{"watermark","info","target"}){int[] p=ConfigManager.HUD.get(id);if(p!=null&&x>=p[0]&&x<=p[0]+200&&y>=p[1]&&y<=p[1]+55){drag=id;ox=x-p[0];oy=y-p[1];return true;}}}return super.mouseClicked(click,dbl);}
 @Override public boolean mouseDragged(Click click,double dx,double dy){if(drag!=null){int[] p=ConfigManager.HUD.get(drag);p[0]=(int)click.x()-ox;p[1]=(int)click.y()-oy;return true;}return super.mouseDragged(click,dx,dy);}
 @Override public boolean mouseReleased(Click click){if(drag!=null){drag=null;ConfigManager.save();return true;}return super.mouseReleased(click);}
 @Override public boolean keyPressed(KeyInput input){int key=input.key();if(key==256){client.setScreen(new ClickGuiScreen());return true;}return super.keyPressed(input);}@Override public boolean shouldPause(){return false;}
}

package dev.myst.client.render;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
/** Buffer-based GUI primitives; no DrawContext.fill and no immediate-mode GL. */
public final class Render2D {
 private Render2D(){}
 private static void v(VertexConsumer b,MatrixStack.Entry m,float x,float y,int c){b.vertex(m,x,y,0).color(c);}
 private static void q(VertexConsumer b,MatrixStack.Entry m,float x,float y,float w,float h,int c){v(b,m,x,y,c);v(b,m,x,y+h,c);v(b,m,x+w,y+h,c);v(b,m,x+w,y,c);}
 public static void drawRoundedRect(VertexConsumer b,MatrixStack.Entry m,float x,float y,float w,float h,float radius,int color){
  if(b==null||m==null||w<=0||h<=0)return;float r=MathUtil.clamp(radius,0,Math.min(w,h)*.5f);if(r<=.01f){q(b,m,x,y,w,h,color);return;}
  float cx=x+w*.5f,cy=y+h*.5f;v(b,m,cx,cy,color);float[][] cs={{x+w-r,y+r,-90},{x+w-r,y+h-r,0},{x+r,y+h-r,90},{x+r,y+r,180}};
  for(float[] a:cs)for(int i=0;i<=8;i++){double z=Math.toRadians(a[2]+90.0*i/8);v(b,m,a[0]+(float)Math.cos(z)*r,a[1]+(float)Math.sin(z)*r,color);}
  v(b,m,x+w-r,y,color);
 }
 public static void drawShadow(VertexConsumer b,MatrixStack.Entry m,float x,float y,float w,float h,float radius,int color,int layers,float spread){
  int n=Math.max(1,layers);for(int i=n;i>0;i--){float d=spread*i/n;float a=(float)(n-i+1)/(n*(n+1)*.5f);drawRoundedRect(b,m,x-d,y-d,w+2*d,h+2*d,radius+d,MathUtil.multiplyAlpha(color,a));}
 }
 public static void drawRoundedOutline(VertexConsumer b,MatrixStack.Entry m,float x,float y,float w,float h,float radius,float thickness,int color){
  if(w<=0||h<=0||thickness<=0)return;float t=Math.min(thickness,Math.min(w,h)*.5f),r=MathUtil.clamp(radius,0,Math.min(w,h)*.5f);
  q(b,m,x+r,y,w-2*r,t,color);q(b,m,x+r,y+h-t,w-2*r,t,color);q(b,m,x,y+r,t,h-2*r,color);q(b,m,x+w-t,y+r,t,h-2*r,color);
  corner(b,m,x+r,y+r,r,t,180,color);corner(b,m,x+w-r,y+r,r,t,270,color);corner(b,m,x+w-r,y+h-r,r,t,0,color);corner(b,m,x+r,y+h-r,r,t,90,color);
 }
 private static void corner(VertexConsumer b,MatrixStack.Entry m,float x,float y,float r,float t,int start,int c){
  for(int i=0;i<8;i++){double a=Math.toRadians(start+i*11.25),z=Math.toRadians(start+(i+1)*11.25);float ix=x+(float)Math.cos(a)*(r-t),iy=y+(float)Math.sin(a)*(r-t),ox=x+(float)Math.cos(a)*r,oy=y+(float)Math.sin(a)*r,ix2=x+(float)Math.cos(z)*(r-t),iy2=y+(float)Math.sin(z)*(r-t),ox2=x+(float)Math.cos(z)*r,oy2=y+(float)Math.sin(z)*r;v(b,m,ix,iy,c);v(b,m,ox,oy,c);v(b,m,ox2,oy2,c);v(b,m,ix2,iy2,c);}
 }
 public static void drawCircle(VertexConsumer b,MatrixStack.Entry m,float x,float y,float r,int c){if(r<=0)return;for(int i=0;i<48;i++){double a=Math.PI*2*i/48,z=Math.PI*2*(i+1)/48;v(b,m,x,y,c);v(b,m,x+(float)Math.cos(a)*r,y+(float)Math.sin(a)*r,c);v(b,m,x+(float)Math.cos(z)*r,y+(float)Math.sin(z)*r,c);}}
}
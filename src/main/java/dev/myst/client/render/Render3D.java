package dev.myst.client.render;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
/** World-space geometry emitted to a compatible caller-owned buffer (GLES-safe). */
public final class Render3D {
 private Render3D(){}
 private static void v(VertexConsumer b,MatrixStack.Entry m,Vec3d p,int c){b.vertex(m,(float)p.x,(float)p.y,(float)p.z).color(c);}
 public static void drawLine(VertexConsumer b,MatrixStack.Entry m,Vec3d a,Vec3d z,int c){if(b!=null&&m!=null){v(b,m,a,c);v(b,m,z,c);}}
 public static void drawRing(VertexConsumer b,MatrixStack.Entry m,Vec3d p,float r,float y,int c,int segments){if(r<=0)return;int n=Math.max(8,segments);for(int i=0;i<n;i++){double a=Math.PI*2*i/n,z=Math.PI*2*(i+1)/n;drawLine(b,m,new Vec3d(p.x+Math.cos(a)*r,p.y+y,p.z+Math.sin(a)*r),new Vec3d(p.x+Math.cos(z)*r,p.y+y,p.z+Math.sin(z)*r),c);}}
 public static void drawDiamond(VertexConsumer b,MatrixStack.Entry m,Vec3d p,float w,float h,int c){Vec3d t=p.add(0,h*.5,0),r=p.add(w*.5,0,0),d=p.add(0,-h*.5,0),l=p.add(-w*.5,0,0);drawLine(b,m,t,r,c);drawLine(b,m,r,d,c);drawLine(b,m,d,l,c);drawLine(b,m,l,t,c);}
 /** Four vertices for a QUADS consumer. */ public static void drawRibbon(VertexConsumer b,MatrixStack.Entry m,Vec3d ls,Vec3d rs,Vec3d le,Vec3d re,int sc,int ec){v(b,m,ls,sc);v(b,m,rs,sc);v(b,m,re,ec);v(b,m,le,ec);}
}
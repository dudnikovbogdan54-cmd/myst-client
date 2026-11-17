package dev.myst.client.render;
public final class MathUtil {
 private MathUtil(){}
 public static float lerp(float a,float b,float t){return a+(b-a)*clamp(t,0f,1f);}
 public static double lerp(double a,double b,double t){return a+(b-a)*clamp(t,0d,1d);}
 public static float fastLerp(float a,float b,float speed,float dt){return lerp(a,b,1f-(float)Math.exp(-Math.max(0f,speed)*Math.max(0f,dt)));}
 public static double fastLerp(double a,double b,double speed,double dt){return lerp(a,b,1d-Math.exp(-Math.max(0d,speed)*Math.max(0d,dt)));}
 public static float easeOutCubic(float t){t=clamp(t,0f,1f);return 1f-(float)Math.pow(1f-t,3);}
 public static double easeOutCubic(double t){t=clamp(t,0d,1d);return 1d-Math.pow(1d-t,3);}
 public static float clamp(float v,float min,float max){return Math.max(min,Math.min(max,v));}
 public static double clamp(double v,double min,double max){return Math.max(min,Math.min(max,v));}
 public static int withAlpha(int c,float a){return(c&0x00FFFFFF)|(Math.round(clamp(a,0f,1f)*255f)<<24);}
 public static int multiplyAlpha(int c,float f){return withAlpha(c,((c>>>24)&255)/255f*f);}
 public static int blend(int a,int b,float t){t=clamp(t,0f,1f);int o=0;for(int s:new int[]{24,16,8,0}){int x=(a>>>s)&255,y=(b>>>s)&255;o|=Math.round(x+(y-x)*t)<<s;}return o;}
 public static int gradient(float t,int... c){if(c==null||c.length==0)return-1;if(c.length==1)return c[0];float p=clamp(t,0f,1f)*(c.length-1);int i=Math.min(c.length-2,(int)p);return blend(c[i],c[i+1],p-i);}
}
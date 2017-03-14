/********************************************************************************/
/*                                                                              */
/*              CuminGraphics.java                                              */
/*                                                                              */
/*      Graphics implementation for interpreting SWING/AWT views                */
/*                                                                              */
/********************************************************************************/
/*      Copyright 2011 Brown University -- Steven P. Reiss                    */
/*********************************************************************************
 *  Copyright 2011, Brown University, Providence, RI.                            *
 *                                                                               *
 *                        All Rights Reserved                                    *
 *                                                                               *
 * This program and the accompanying materials are made available under the      *
 * terms of the Eclipse Public License v1.0 which accompanies this distribution, *
 * and is available at                                                           *
 *      http://www.eclipse.org/legal/epl-v10.html                                *
 *                                                                               *
 ********************************************************************************/

/* SVN: $Id$ */



package edu.brown.cs.seede.cumin;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import java.util.Map;

import edu.brown.cs.ivy.xml.IvyXmlWriter;
import edu.brown.cs.seede.cashew.CashewClock;
import edu.brown.cs.seede.cashew.CashewConstants;
import edu.brown.cs.seede.cashew.CashewValue;

public class CuminGraphics extends Graphics2D implements CuminConstants
{




/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private Shape		user_clip;
private AffineTransform user_transform;
private Color		fg_color;
private Paint   	user_paint;
private Color		bg_color;
private Stroke		user_stroke;
private Composite	user_composite;
private GraphicsConfiguration graphics_config;
private RenderingHints	user_hints;
private Font		user_font;
private Graphics	base_graphics;
private IvyXmlWriter    xml_output;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

public CuminGraphics(CashewValue b,CashewClock cc)
{
   CashewValue gv = null;
   if (b.getDataType(cc).isCompatibleWith(CashewConstants.COMPONENT_TYPE)) {
      // gv = evaluate b.getGraphics();
    }
   initialize(gv);
   
   xml_output = new IvyXmlWriter();
   xml_output.begin("SWING");
   xml_output.field("OBJECT",b.getString(cc));
}

private CuminGraphics(CuminGraphics g)
{
   initialize(g);
}



@Override public Graphics create()
{
   return new CuminGraphics(this);
}


@Override public void dispose() 			{ }




/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

String getResult()
{
   return xml_output.toString();
}



/********************************************************************************/
/*										*/
/*	Setup methods								*/
/*										*/
/********************************************************************************/

private void initialize(Graphics g)
{
   user_clip = g.getClip();
   user_transform = new AffineTransform();
   fg_color = g.getColor();
   bg_color = null;
   user_paint = null;
   user_composite = null;
   graphics_config = null;
   user_hints = null;
   user_stroke = new BasicStroke();
   user_font = g.getFont();
   base_graphics = g;
   
   if (g != null && g instanceof Graphics2D) {
      Graphics2D g2 = (Graphics2D) g;
      user_transform = g2.getTransform();
      bg_color = g2.getBackground();
      user_paint = g2.getPaint();
      user_composite = g2.getComposite();
      graphics_config = g2.getDeviceConfiguration();
      user_hints = g2.getRenderingHints();
      user_stroke = g2.getStroke();
    }
   
   if (g != null && g instanceof CuminGraphics) {
      CuminGraphics bg = (CuminGraphics) g;
      xml_output = bg.xml_output;
    }
   else {
      xml_output = new IvyXmlWriter();
    }
}



void initialize(CashewValue cv)
{
   // user_clip = g.getClip();
   user_transform = new AffineTransform();
   // fg_color = g.getColor();
   bg_color = null;
   user_paint = null;
   user_composite = null;
   graphics_config = null;
   user_hints = null;
   user_stroke = new BasicStroke();
   // user_font = g.getFont();
   // base_graphics = g;
   
   if (cv.getDataType(null).isCompatibleWith(CashewConstants.GRAPHICS2D_TYPE)) {
      // user_transform = g2.getTransform();
      // bg_color = g2.getBackground();
      // user_paint = g2.getPaint();
      // user_composite = g2.getComposite();
      // graphics_config = g2.getDeviceConfiguration();
      // user_hints = g2.getRenderingHints();
      // user_stroke = g2.getStroke();
    }
   
   xml_output = new IvyXmlWriter();
}




/********************************************************************************/
/*										*/
/*	Basic Graphic drawing primitives					*/
/*										*/
/********************************************************************************/

@Override public void drawArc(int x,int y,int w,int h,int sa,int aa)
{
   Shape s = new Arc2D.Float(x,y,w,h,sa,aa,Arc2D.OPEN);
   draw(s);
}

@Override public void fillArc(int x,int y,int w,int h,int sa,int aa)
{
   Shape s = new Arc2D.Float(x,y,w,h,sa,aa,Arc2D.OPEN);
   fill(s);
}


@Override public void drawLine(int x1,int y1,int x2,int y2)
{
   Shape s = new Line2D.Float(x1,y1,x2,y2);
   draw(s);
}


@Override public void drawOval(int x,int y,int w,int h)
{
   Shape s = new Ellipse2D.Float(x,y,w,h);
   draw(s);
}

@Override public void fillOval(int x,int y,int w,int h)
{
   Shape s = new Ellipse2D.Float(x,y,w,h);
   fill(s);
}


@Override public void drawPolygon(int [] xp,int [] yp,int n)
{
   Shape s = new Polygon(xp,yp,n);
   draw(s);
}

@Override public void fillPolygon(int [] xp,int [] yp,int n)
{
   Shape s = new Polygon(xp,yp,n);
   fill(s);
}


@Override public void fillRect(int x,int y,int w,int h)
{
   Shape s = new Rectangle(x,y,w,h);
   fill(s);
}



@Override public void drawPolyline(int [] xp,int [] yp,int n)
{
   if (n == 0) return;
   
   Path2D.Float p2 = new Path2D.Float();
   p2.moveTo(xp[0],yp[0]);
   for (int i = 1; i < n; ++i) {
      p2.lineTo(xp[i],yp[i]);
    }
   draw(p2);
}



@Override public void drawRoundRect(int x,int y,int w,int h,int aw,int ah)
{
   Shape s = new RoundRectangle2D.Float(x,y,w,h,aw,ah);
   draw(s);
}

@Override public void fillRoundRect(int x,int y,int w,int h,int aw,int ah)
{
   Shape s = new RoundRectangle2D.Float(x,y,w,h,aw,ah);
   fill(s);
}


@Override public void draw(Shape s)
{
   if (user_stroke == null) user_stroke = new BasicStroke();
   
   Shape ss = user_stroke.createStrokedShape(s);
   fill(ss);
}


@Override public void fill(Shape s)
{
   generateReport(s);
}


@Override public void clearRect(int x,int y,int w,int h)
{
   Composite c = user_composite;
   Paint p = user_paint;
   setComposite(AlphaComposite.Src);
   setColor(getBackground());
   fillRect(x, y, w, h);
   setPaint(p);
   setComposite(c);
}


@Override public void copyArea(int x,int y,int w,int h,int dx,int dy)
{
   Rectangle r = new Rectangle(x+dx,y+dy,w,h);
   
   // change this if we care what the contents are
   fill(r);
}




/********************************************************************************/
/*										*/
/*	Image methods								*/
/*										*/
/********************************************************************************/

@Override public boolean drawImage(Image img,int x,int y,int w,int h,Color bg,ImageObserver o)
{
   fillRect(x,y,w,h);
   
   return true;
}


@Override public boolean drawImage(Image img,int dx1,int dy1,int dx2,int dy2,
      int sx1,int sy1,int xs2,int sy2,Color bg,ImageObserver o)
{
   fillRect(Math.min(dx1,dx2),Math.min(dy1,dy2),Math.abs(dx2-dx1),Math.abs(dy2-dy1));
   
   return true;
}


@Override public void drawImage(BufferedImage img,BufferedImageOp op,int x, int y)
{
   fillRect(x,y,img.getWidth(),img.getHeight());
}


@Override public boolean drawImage(Image img,AffineTransform xf,ImageObserver o)
{
   Point2D p1 = new Point2D.Double(0,0);
   Point2D p2 = new Point2D.Double(img.getWidth(o),img.getHeight(o));
   p1 = xf.transform(p1,p1);
   p2 = xf.transform(p2,p2);
   
   int x0 = (int) Math.min(p1.getX(),p2.getX());
   int y0 = (int) Math.min(p1.getY(),p2.getY());
   int w0 = (int) Math.abs(p2.getX() - p1.getX());
   int h0 = (int) Math.abs(p2.getY() - p1.getY());
   
   fillRect(x0,y0,w0,h0);
   
   return true;
}


@Override public void drawRenderableImage(RenderableImage img,AffineTransform xf)
{
   Point2D p1 = new Point2D.Double(0,0);
   Point2D p2 = new Point2D.Double(img.getWidth(),img.getHeight());
   p1 = xf.transform(p1,p1);
   p2 = xf.transform(p2,p2);
   
   int x0 = (int) Math.min(p1.getX(),p2.getX());
   int y0 = (int) Math.min(p1.getY(),p2.getY());
   int w0 = (int) Math.abs(p2.getX() - p1.getX());
   int h0 = (int) Math.abs(p2.getY() - p1.getY());
   
   fillRect(x0,y0,w0,h0);
}



@Override public void drawRenderedImage(RenderedImage img,AffineTransform xf)
{
   Point2D p1 = new Point2D.Double(0,0);
   Point2D p2 = new Point2D.Double(img.getWidth(),img.getHeight());
   if (xf != null) {
      p1 = xf.transform(p1,p1);
      p2 = xf.transform(p2,p2);
    }
   
   int x0 = (int) Math.min(p1.getX(),p2.getX());
   int y0 = (int) Math.min(p1.getY(),p2.getY());
   int w0 = (int) Math.abs(p2.getX() - p1.getX());
   int h0 = (int) Math.abs(p2.getY() - p1.getY());
   
   fillRect(x0,y0,w0,h0);
}


/********************************************************************************/
/*										*/
/*	String methods								*/
/*										*/
/********************************************************************************/

@Override public void drawString(String s,int x,int y)
{
   drawString(s,(float) x,(float) y);
}


@Override public void drawString(String s,float x,float y)
{
   if (s == null) return;
   
   Font f = getFont();
   FontRenderContext ctx = getFontRenderContext();
   Rectangle2D rc = f.getStringBounds(s,ctx);
   rc.setFrame(rc.getX() + x,rc.getY() + y,rc.getWidth(),rc.getHeight());
   
   fill(rc);
}


@Override public void drawString(AttributedCharacterIterator it,int x,int y)
{
   drawString(it,(float) x,(float) y);
}


@Override public void drawString(AttributedCharacterIterator it,float x,float y)
{
   if (it == null) return;
   
   Font f = getFont();
   FontRenderContext ctx = getFontRenderContext();
   Rectangle2D rc = f.getStringBounds(it,0,it.getEndIndex(),ctx);
   rc.setFrame(rc.getX() + x,rc.getY() + y,rc.getWidth(),rc.getHeight());
   
   fill(rc);
}



@Override public void drawGlyphVector(GlyphVector g,float x,float y)
{
   Rectangle2D rc = g.getLogicalBounds();
   rc.setFrame(rc.getX() + x,rc.getY() + y,rc.getWidth(),rc.getHeight());
   
   fill(rc);
}



/********************************************************************************/
/*										*/
/*	Clipping methods							*/
/*										*/
/********************************************************************************/

@Override public void clipRect(int x,int y,int w,int h)
{
   clip(new Rectangle(x,y,w,h));
}


@Override public void setClip(int x,int y,int w,int h)
{
   setClip(new Rectangle(x,y,w,h));
}



@Override public void setClip(Shape sh)
{
   user_clip = transformShape(sh);
}


@Override public void clip(Shape s)
{
   s = transformShape(s);
   if (user_clip != null) s = intersectShapes(user_clip,s);
   user_clip = s;
}


@Override public Shape getClip()
{
   if (user_clip == null) return null;
   
   return untransformShape(user_clip);
}


@Override public Rectangle getClipBounds()
{
   if (user_clip == null) return null;
   
   Rectangle r = getClip().getBounds();
   
   return r;
}



private Shape transformShape(Shape s)
{
   if (s == null) return null;
   if (user_transform == null || user_transform.isIdentity()) return s;
   
   return user_transform.createTransformedShape(s);
}



private Shape untransformShape(Shape s)
{
   if (s == null) return null;
   if (user_transform == null || user_transform.isIdentity()) return s;
   
   try {
      s = user_transform.createInverse().createTransformedShape(s);
    }
   catch (NoninvertibleTransformException e) { }
   
   return s;
}


private Shape intersectShapes(Shape s1,Shape s2)
{
   if (s1 instanceof Rectangle && s2 instanceof Rectangle) {
      return ((Rectangle) s1).intersection((Rectangle) s2);
    }
   if (s1 instanceof Rectangle2D) {
      return intersectRectShape((Rectangle2D) s1,s2);
    }
   if (s2 instanceof Rectangle2D) {
      return intersectRectShape((Rectangle2D) s2,s1);
    }
   return intersectByArea(s1,s2);
}


private Shape intersectRectShape(Rectangle2D r,Shape s)
{
   if (s instanceof Rectangle2D) {
      Rectangle2D r2 = (Rectangle2D) s;
      Rectangle2D out = new Rectangle2D.Float();
      double x1 = Math.max(r.getX(), r2.getX());
      double x2 = Math.min(r.getX()  + r.getWidth(), r2.getX() + r2.getWidth());
      double y1 = Math.max(r.getY(), r2.getY());
      double y2 = Math.min(r.getY()  + r.getHeight(), r2.getY() + r2.getHeight());
      if (((x2 - x1) < 0) || ((y2 - y1) < 0))
	 out.setFrameFromDiagonal(0, 0, 0, 0);
      else
	 out.setFrameFromDiagonal(x1, y1, x2, y2);
      return out;
    }
   if (r.contains(s.getBounds2D())) return s;
   
   return intersectByArea(r,s);
}


private Shape intersectByArea(Shape s1,Shape s2)
{
   Area a1 = new Area(s1);
   Area a2;
   if (s2 instanceof Area) a2 = (Area) s2;
   else a2 = new Area(s2);
   a1.intersect(a2);
   if (a1.isRectangular()) return a1.getBounds();
   return a1;
}




/********************************************************************************/
/*										*/
/*	Property methods							*/
/*										*/
/********************************************************************************/

@Override public Color getColor()
{
   return fg_color;
}


@Override public void setColor(Color c)
{
   fg_color = c;
   user_paint = c;
}


@Override public void setPaint(Paint p)
{
   if (p instanceof Color) setColor((Color) p);
   else user_paint = p;
}


@Override public Paint getPaint()
{
   return user_paint;
}


@Override public void setBackground(Color c)
{
   bg_color = c;
}

@Override public Color getBackground()
{
   return bg_color;
}


@Override public void setComposite(Composite c)
{
   user_composite = c;
}

@Override public Composite getComposite()
{
   return user_composite;
}



@Override public GraphicsConfiguration getDeviceConfiguration()
{
   return graphics_config;
}


@Override public void setPaintMode()
{
   setComposite(AlphaComposite.SrcOver);
}


@Override public void setXORMode(Color c)
{
   setComposite(AlphaComposite.Xor);
}


@Override public void addRenderingHints(Map<?,?> h)
{
   if (user_hints == null) user_hints = new RenderingHints(null);
   user_hints.putAll(h);
}


@Override public Object getRenderingHint(RenderingHints.Key k)
{
   if (user_hints != null) return user_hints.get(k);
   return null;
}


@Override public void setRenderingHint(RenderingHints.Key k,Object v)
{
   if (user_hints == null) user_hints = new RenderingHints(null);
   user_hints.put(k,v);
}


@Override public void setRenderingHints(Map<?,?> h)
{
   user_hints = null;
   addRenderingHints(h);
}


@Override public RenderingHints getRenderingHints()
{
   if (user_hints == null) return new RenderingHints(null);
   return (RenderingHints) user_hints.clone();
}



@Override public void setStroke(Stroke s)
{
   user_stroke = s;
}


@Override public Stroke getStroke()
{
   return user_stroke;
}




/********************************************************************************/
/*										*/
/*	Font methods								*/
/*										*/
/********************************************************************************/

@Override public Font getFont()
{
   return user_font;
}


@Override public void setFont(Font f)
{
   if (f != null) user_font = f;
}


@Override public FontMetrics getFontMetrics(Font f)
{
   return base_graphics.getFontMetrics(f);
}


@Override public FontRenderContext getFontRenderContext()
{
   return new FontRenderContext(user_transform,true,true);
}


/********************************************************************************/
/*										*/
/*	Transforms								*/
/*										*/
/********************************************************************************/

@Override public void translate(int x,int y)
{
   user_transform.translate(x,y);
}


@Override public void translate(double x,double y)
{
   user_transform.translate(x,y);
}


@Override public void rotate(double th)
{
   user_transform.rotate(th);
}



@Override public void rotate(double th,double x,double y)
{
   user_transform.rotate(th,x,y);
}


@Override public void scale(double sx,double sy)
{
   user_transform.scale(sx,sy);
}


@Override public void shear(double sx,double sy)
{
   user_transform.shear(sx,sy);
}


@Override public void transform(AffineTransform t)
{
   user_transform.concatenate(t);
}


@Override public void setTransform(AffineTransform t)
{
   user_transform.setTransform(t);
}



@Override public AffineTransform getTransform()
{
   return new AffineTransform(user_transform);
}



/********************************************************************************/
/*										*/
/*	Higher level primitives 						*/
/*										*/
/********************************************************************************/

@Override public boolean drawImage(Image img,int x,int y,Color bg,ImageObserver o)
{
   return drawImage(img,x,y,img.getWidth(o),img.getHeight(o),bg,o);
}


@Override public boolean drawImage(Image img,int x,int y,ImageObserver o)
{
   return drawImage(img,x,y,null,o);
}


@Override public boolean drawImage(Image img,int x,int y,int w,int h,ImageObserver o)
{
   return drawImage(img,x,y,w,h,null,o);
}


@Override public boolean drawImage(Image img,int dx1,int dy1,int dx2,int dy2,
      int sx1,int sy1,int sx2,int sy2,ImageObserver o)
{
   return drawImage(img,dx1,dy1,dx2,dy2,sx1,sy1,sx2,sy2,null,o);
}



/********************************************************************************/
/*										*/
/*	Miscellaneous methods							*/
/*										*/
/********************************************************************************/

@Override public boolean hit(Rectangle r,Shape s,boolean onstroke)
{
   if (onstroke) {
      s = user_stroke.createStrokedShape(s);
    }
   s = transformShape(s);
   
   return s.intersects(r);
}



/********************************************************************************/
/*										*/
/*	Reporting methods							*/
/*										*/
/********************************************************************************/

private void generateReport(Shape s)
{
   if (xml_output == null) return;
   
   xml_output.begin("DRAW");
   if (user_paint != null) {
      xml_output.begin("PAINT");
      xml_output.xmlText(user_paint.toString());
      xml_output.end();
    }
   if (user_stroke != null) {
      xml_output.begin("STROKE");
      xml_output.xmlText(user_stroke.toString());
      xml_output.end();
    }
   if (fg_color != null) {
      xml_output.begin("COLOR");
      xml_output.xmlText(fg_color.toString());
      xml_output.end();
    }
   
   xml_output.begin("SHAPE");
   Shape s1 = untransformShape(s);
   // output shape information here. Shape should be in top-level coordinates
   xml_output.text(s1.toString());
   xml_output.end("SHAPE");
   
   xml_output.end();
}




public String getReport()
{
   if (xml_output == null) return null;
   
   xml_output.end("SWING");
   
   String rslt = xml_output.toString();
   xml_output = null;
   
   return rslt;
}




/********************************************************************************/
/*                                                                              */
/*      Methods to invoke graphics methods                                      */
/*                                                                              */
/********************************************************************************/

CashewValue invokeMethodOn(CashewValue base,String method)
{
   return null;
}



}       // end of class CuminGraphics




/* end of CuminGraphics.java */


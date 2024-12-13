/********************************************************************************/
/*										*/
/*		CuminGraphicsEvaluator.java					*/
/*										*/
/*	Handle calls to our Graphics interface					*/
/*										*/
/********************************************************************************/
/*	Copyright 2011 Brown University -- Steven P. Reiss		      */
/*********************************************************************************
 *  Copyright 2011, Brown University, Providence, RI.				 *
 *										 *
 *			  All Rights Reserved					 *
 *										 *
 * This program and the accompanying materials are made available under the	 *
 * terms of the Eclipse Public License v1.0 which accompanies this distribution, *
 * and is available at								 *
 *	http://www.eclipse.org/legal/epl-v10.html				 *
 *										 *
 ********************************************************************************/

/* SVN: $Id$ */



package edu.brown.cs.seede.cumin;

import java.util.HashMap;
import java.util.Map;

import edu.brown.cs.ivy.jcomp.JcompType;
import edu.brown.cs.ivy.jcomp.JcompTyper;
import edu.brown.cs.ivy.xml.IvyXmlWriter;
import edu.brown.cs.seede.acorn.AcornLog;
import edu.brown.cs.seede.cashew.CashewClock;
import edu.brown.cs.seede.cashew.CashewConstants;
import edu.brown.cs.seede.cashew.CashewException;
import edu.brown.cs.seede.cashew.CashewValue;

class CuminGraphicsEvaluator extends CuminNativeEvaluator implements CuminConstants, CashewConstants
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

enum FieldType { FG, BG, PAINT, STROKE, COMPOSITE, HINTS, FONT, CLIP, TRANSFORM, };

enum CommandType { DRAW_ARC, FILL_ARC, DRAW_LINE, DRAW_OVAL, FILL_OVAL,
   DRAW_POLYGON, FILL_POLYGON, FILL_RECT, DRAW_POLYLINE, DRAW_ROUND_RECT,
   FILL_ROUND_RECT, DRAW, FILL, CLEAR_RECT, COPY_AREA, DRAW_IMAGE, DRAW_RENDERED_IMAGE,
   DRAW_RENDERABLE_IMAGE, DRAW_STRING, DRAW_GLPYH_VECTOR,
   DRAW_RECT, DRAW_3D_RECT, FILL_3D_RECT,
   TRANSFORM, ROTATE, SCALE, SHEAR, TRANSLATE, GET_TRANSFORM, SET_TRANSFORM,
   CONSTRAIN, CLIP_RECT, SET_CLIP, CLIP, CREATE,
};


private static Map<CashewValue,GraphicsOutput> output_map;


static {
   output_map = new HashMap<CashewValue,GraphicsOutput>();
}




/********************************************************************************/
/*										*/
/*	Static methods								*/
/*										*/
/********************************************************************************/

static void resetGraphics()
{
   output_map.clear();
}



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

CuminGraphicsEvaluator(CuminRunnerByteCode bc)
{
   super(bc);
}


/********************************************************************************/
/*										*/
/*	Entry point for getGraphics						*/
/*										*/
/********************************************************************************/

CuminRunStatus checkJFrame() throws CashewException
{
   CashewValue rslt = null;

   switch (getMethod().getName()) {
      case "getGraphics" :
	 CashewValue c = getValue(0);
	 AcornLog.logD("CUMIN","Start of poppy call: " + c);
	 String nm = "GET_GRAPHICS";
	 exec_runner.getCodeFactory().findClass("edu.brown.cs.seede.poppy.PoppyGraphics");
	 rslt = exec_runner.executeCall("edu.brown.cs.seede.poppy.PoppyGraphics.computeGraphics2",
	       c,CashewValue.stringValue(getTyper(),getTyper().STRING_TYPE,nm));
	 AcornLog.logD("CUMIN","Result of poppy call: " + rslt);
	 getSession().addPoppyGraphics(rslt);
	 break;
      default :
	 return null;
    }


   return CuminRunStatus.Factory.createReturn(rslt);
}



/********************************************************************************/
/*                                                                              */
/*      Entry point for getDefaultToolkit                                       */
/*                                                                              */
/********************************************************************************/

CuminRunStatus checkToolkit() throws CashewException
{
   CashewValue rslt = null;
   
   switch (getMethod().getName()) {
      case "getDefaultToolkit" :
         String exec = "java.awt.Toolkit.getDefaultToolkit()";
         rslt = exec_runner.getLookupContext().evaluate(exec);
         break;
      default :
         return null;
    }
   
   return CuminRunStatus.Factory.createReturn(rslt);
}


/********************************************************************************/
/*                                                                              */
/*      Entry point for Component                                               */
/*                                                                              */
/********************************************************************************/

CuminRunStatus checkComponent() throws CashewException
{
   CashewValue rslt = null;
   
   switch (getMethod().getName()) {
      case "updateCursorImmediately" :
         // skip this
         break;
      default :
         return null;
    }
   
   return CuminRunStatus.Factory.createReturn(rslt);
}



/********************************************************************************/
/*										*/
/*	Entry point for PoppyGraphics methods					*/
/*										*/
/********************************************************************************/

// CHECKSTYLE:OFF
CuminRunStatus checkPoppyGraphics() throws CashewException
// CHECKSTYLE:ON
{
   if (getMethod().isStatic() || getMethod().isConstructor()) return null;

   CashewValue rslt = null;

   CashewValue thisarg = getValue(0);
   GraphicsOutput graphics = output_map.get(thisarg);
   if (graphics == null) {
      AcornLog.logD("POPPY: create graphics");
      graphics = new GraphicsOutput(thisarg);
      output_map.put(thisarg,graphics);
    }

   switch (getMethod().getName()) {
      case "create" :
	 return null;
      case "dispose" :
	 output_map.remove(thisarg);
	 return null;
      case "initialize" :
	 return null;
      case "setupComplete" :
	 CashewValue ogr = getValue(1);
	 if (ogr.isNull(getSession(),getClock())) {
	    ogr = null;
	    handleCreate(graphics,null);
	  }
	 else {
	    GraphicsOutput gg = output_map.get(ogr);
	    if (gg != null) {
	       graphics.setNested(gg);
	     }
	    handleCreate(graphics,gg);
	  }
	 break;
      case "finalReport" :
	 return null;

      case "drawArc" :
	 createCommand(graphics,CommandType.DRAW_ARC);
	 break;
      case "fillArc" :
	 createCommand(graphics,CommandType.FILL_ARC);
	 break;
      case "drawLine" :
	 createCommand(graphics,CommandType.DRAW_LINE);
	 break;
      case "drawOval" :
	 createCommand(graphics,CommandType.DRAW_OVAL);
	 break;
      case "fillOval" :
	 createCommand(graphics,CommandType.FILL_OVAL);
	 break;
      case "drawPolygon" :
	 createCommand(graphics,CommandType.DRAW_POLYGON);
	 break;
      case "fillPolygon" :
	 createCommand(graphics,CommandType.FILL_POLYGON);
	 break;
      case "drawRect" :
	 createCommand(graphics,CommandType.DRAW_RECT);
	 break;
      case "fillRect" :
	 createCommand(graphics,CommandType.FILL_RECT);
	 break;
      case "draw3DRect" :
	 createCommand(graphics,CommandType.DRAW_3D_RECT);
	 break;
      case "fill3DRect" :
	 createCommand(graphics,CommandType.FILL_3D_RECT);
	 break;
      case "drawPolyline" :
	 createCommand(graphics,CommandType.DRAW_POLYLINE);
	 break;
      case "drawRoundRect" :
	 createCommand(graphics,CommandType.DRAW_ROUND_RECT);
	 break;
      case "fillRoundRect" :
	 createCommand(graphics,CommandType.FILL_ROUND_RECT);
	 break;
      case "draw" :
	 createCommand(graphics,CommandType.DRAW);
	 break;
      case "fill" :
	 createCommand(graphics,CommandType.FILL);
	 break;
      case "clearRect" :
	 createCommand(graphics,CommandType.CLEAR_RECT);
	 break;
      case "copyArea" :
	 createCommand(graphics,CommandType.COPY_AREA);
	 break;
      case "drawImage" :
	 rslt = CashewValue.booleanValue(getTyper(),true);
	 JcompType imgtyp = getTyper().findSystemType("java.awt.Image");
	 if (getDataType(1).isCompatibleWith(imgtyp)) {
	    // discard the ImageObserver argument
	    createCommand(graphics,CommandType.DRAW_IMAGE,1);
	  }
	 else
	    createCommand(graphics,CommandType.DRAW_IMAGE);
	 break;
      case "drawRenderableImage" :
	 createCommand(graphics,CommandType.DRAW_RENDERABLE_IMAGE);
	 break;
      case "drawRenderedImage" :
	 createCommand(graphics,CommandType.DRAW_RENDERED_IMAGE);
	 break;
      case "drawString" :
	 createCommand(graphics,CommandType.DRAW_STRING);
	 break;
      case "drawGlyphVector" :
	 createCommand(graphics,CommandType.DRAW_GLPYH_VECTOR);
	 break;

      case "transform" :
	 createCommand(graphics,CommandType.TRANSFORM);
	 return null;
      case "rotate" :
	 createCommand(graphics,CommandType.ROTATE);
	 return null;
      case "scale" :
	 createCommand(graphics,CommandType.SCALE);
	 return null;
      case "shear" :
	 createCommand(graphics,CommandType.SHEAR);
	 return null;
      case "setTransform" :
	 createCommand(graphics,CommandType.SET_TRANSFORM);
	 return null;
      case "getTransform" :
	 createCommand(graphics,CommandType.GET_TRANSFORM);
	 return null;
      case "translate" :
	 if (!graphics.checkSkipTranslate())
	    createCommand(graphics,CommandType.TRANSLATE);
	 return null;

      case "clipRect" :
	 createCommand(graphics,CommandType.CLIP_RECT);
	 return null;
      case "setClip" :
	 createCommand(graphics,CommandType.SET_CLIP);
	 return null;
      case "clip" :
	 createCommand(graphics,CommandType.CLIP);
	 return null;
      case "constrain" :
	 createCommand(graphics,CommandType.CONSTRAIN);
	 break;

      case "setColor" :
      case "setPaint" :
      case "setPaintMode" :
      case "setXORMode" :
      case "setBackground" :
      case "setComposite" :
      case "addRenderingHints" :
      case "setRenderingHint" :
      case "setStroke" :
      case "setFont" :
	 return null;

      case "getClip" :
      case "getClipBounds" :
      case "getColor" :
      case "getPaint" :
      case "getBackground" :
      case "getComposite" :
      case "getDeviceConfiguration" :
      case "getRenderingHint" :
      case "getRenderingHints" :
      case "getStroke" :
      case "getFont" :
      case "getFontMetrics" :
      case "getFontRenderContext" :
      case "hit" :
      case "doClip" :
	 return null;

      default :
	 AcornLog.logE("Unexpected method " + getMethod());
	 return null;

      case "getReport" :
	 JcompTyper typer = getTyper();
	 rslt = CashewValue.stringValue(typer,typer.STRING_TYPE,graphics.getResult());
	 output_map.remove(thisarg);
	 break;
    }

   return CuminRunStatus.Factory.createReturn(rslt);
}



/********************************************************************************/
/*										*/
/*	Handle graphics callback -- constrain to inner window			*/
/*										*/
/********************************************************************************/

CuminRunStatus checkGraphicsCallback() throws CashewException
{
   if (getMethod().isStatic() || getMethod().isConstructor()) return null;

   switch (getMethod().getName()) {
      case "constrainGraphics" :
	 CashewValue garg = getValue(1);
	 GraphicsOutput graphics = output_map.get(garg);
	 CashewValue rect = getValue(2);
	 constrainGraphics(graphics,rect);
	 graphics.skipTranslate();
	 return null;
      default :
	 return null;
    }
}


CuminRunStatus checkLoops() throws CashewException
{
   if (!getMethod().isNative()) return null;

   // native methods with void returns here can be ignored

   return CuminRunStatus.Factory.createReturn(null);
}



private void constrainGraphics(GraphicsOutput g,CashewValue rect) throws CashewException
{
   JcompTyper typer = getTyper();
   CashewValueSession sess = getSession();

   g.addFields();

   IvyXmlWriter xw = g.getCommandList();
   xw.begin("DRAW");
   xw.field("TYPE",CommandType.CONSTRAIN);
   xw.field("NUMARGS",4);
   xw.field("TIME",getClock().getTimeValue());
   outputArg(xw,rect.getFieldValue(sess,typer,getClock(),
         "java.awt.Rectangle.x",getContext()));
   outputArg(xw,rect.getFieldValue(sess,typer,getClock(),
         "java.awt.Rectangle.y",getContext()));
   outputArg(xw,rect.getFieldValue(sess,typer,getClock(),
         "java.awt.Rectangle.width",getContext()));
   outputArg(xw,rect.getFieldValue(sess,typer,getClock(),
         "java.awt.Rectangle.height",getContext()));
   xw.end("DRAW");
}

private void handleCreate(GraphicsOutput g,GraphicsOutput par) throws CashewException
{
   if (par != null) par.addFields();
   g.addFields();
}


private void outputArg(IvyXmlWriter xw,CashewValue cv) throws CashewException
{
   xw.begin("ARG");
   xw.field("TYPE","int");
   xw.field("VALUE",cv.getNumber(getSession(),getClock()).intValue());
   xw.end("ARG");
}




/********************************************************************************/
/*										*/
/*	Command Encoding routines						*/
/*										*/
/********************************************************************************/

private void createCommand(GraphicsOutput g,CommandType typ) throws CashewException
{
   createCommand(g,typ,0);
}


private void createCommand(GraphicsOutput g,CommandType typ,int ign) throws CashewException
{
   int act = getNumArgs() - ign;

   AcornLog.logD("Begin command " + typ);

   g.addFields();
   CashewValueSession sess = getSession();
   CashewClock cc = getClock();
   JcompType mrimage = getTyper().findSystemType("java.awt.image.MultiResolutionImage");

   IvyXmlWriter xw = g.getCommandList();
   xw.begin("DRAW");
   xw.field("TYPE",typ);
   xw.field("NUMARGS",act);
   xw.field("TIME",cc.getTimeValue());

   for (int i = 1; i < act; ++i) {
      xw.begin("ARG");
      CashewValue cv = getValue(i);
      JcompType dtyp = cv.getDataType(sess,cc,getTyper());
      xw.field("TYPE",dtyp.getName());
      if (dtyp.isPrimitiveType()) {
	 switch (dtyp.getName()) {
	    case "int" :
	    case "short" :
	    case "byte" :
	    case "char" :
	    case "long" :
	       xw.field("VALUE",cv.getNumber(sess,cc).longValue());
	       break;
	    case "float" :
	    case "double" :
	       xw.field("VALUE",cv.getNumber(sess,cc).doubleValue());
	       break;
	    case "boolean" :
	       xw.field("VALUE",cv.getBoolean(sess,cc));
	       break;
	  }
       }
      else if (cv.isNull(sess,cc)) {
	 xw.field("NULL",true);
       }
      else if (dtyp == getTyper().STRING_TYPE) {
	 xw.textElement("VALUE",cv.getString(getSession(),getTyper(),cc));
       }
      else if (dtyp.getName().equals("java.awt.geom.AffineTransform")) {
	 int trty = getIntField(cv,"java.awt.geom.AffineTransform.type");
	 int m00 = getIntField(cv,"java.awt.geom.AffineTransform.m00");
	 int m01 = getIntField(cv,"java.awt.geom.AffineTransform.m01");
	 int m02 = getIntField(cv,"java.awt.geom.AffineTransform.m02");
	 int m10 = getIntField(cv,"java.awt.geom.AffineTransform.m10");
	 int m11 = getIntField(cv,"java.awt.geom.AffineTransform.m11");
	 int m12 = getIntField(cv,"java.awt.geom.AffineTransform.m12");
	 xw.field("TRTYPE",trty);
	 xw.field("M00",m00);
	 xw.field("M01",m01);
	 xw.field("M02",m02);
	 xw.field("M10",m10);
	 xw.field("M11",m11);
	 xw.field("M12",m12);
       }
      else if (dtyp.getName().equals("java.awt.Rectangle")) {
	 xw.field("X",getIntField(cv,"java.awt.Rectangle.x"));
	 xw.field("Y",getIntField(cv,"java.awt.Rectangle.y"));
	 xw.field("WIDTH",getIntField(cv,"java.awt.Rectangle.width"));
	 xw.field("HEIGHT",getIntField(cv,"java.awt.Rectangle.height"));
       }
      else if (dtyp.getName().equals("sun.awt.image.ToolkitImage")) {
	 CashewValue cv1 = getOptionalField(cv,"sun.awt.image.ToolkitImage.src");
	 CashewValue cv2 = getOptionalField(cv1,"sun.awt.image.URLImageSource.url");
	 if (cv2 != null) {
	    xw.field("KIND","URL");
	    xw.field("PROTOCOL",getStringField(cv2,"java.net.URL.protocol"));
	    xw.field("HOST",getStringField(cv2,"java.net.URL.host"));
	    xw.field("PORT",getIntField(cv2,"java.net.URL.port"));
	    xw.field("FILE",getStringField(cv2,"java.net.URL.file"));
	  }
       }
      else if (dtyp.isCompatibleWith(mrimage)) {
         int basewidth = getIntField(cv,"sun.awt.image.MulteResolutionCachedImage.baseImageWidth");
         int baseht = getIntField(cv,"sun.awt.image.MulteResolutionCachedImage.baseImageHeight");
         xw.field("KIND","MULTI");
         xw.field("BASEWIDTH",basewidth);
         xw.field("BASEHT",baseht);
       }
      else {
	 xw.textElement("VALUE",cv.getString(getSession(),getTyper(),cc));
       }
      xw.end("ARG");
    }

   xw.end("DRAW");
}




/********************************************************************************/
/*										*/
/*	Field encoding routines 						*/
/*										*/
/********************************************************************************/

private String encodeField(CashewValue cv) throws CashewException
{
   JcompTyper typer = getTyper();
   CashewValueSession sess = getSession();

   if (cv == null || cv.isNull(sess,getClock())) return null;

   JcompType fldtyp = cv.getDataType(sess,getClock(),typer);
   switch (fldtyp.getName()) {
      case "java.awt.Color" :
      case "sun.swing.PrintColorUIResource" :
      case "javax.swing.plaf.ColorUIResource" :
	 int cval = getIntField(cv,"java.awt.Color.value");
	 return "<COLOR VALUE='#" + Integer.toHexString(cval) + "'/>";

      case "java.awt.Rectangle" :
	 int wd = getIntField(cv,"java.awt.Rectangle.width");
	 int ht = getIntField(cv,"java.awt.Rectangle.height");
	 int x = getIntField(cv,"java.awt.Rectangle.x");
	 int y = getIntField(cv,"java.awt.Rectangle.y");
	 return "<RECT X='" + x + "' Y='" + y + "' WIDTH='" + wd + "' HEIGHT='" + ht + "' />";

      case "java.awt.Font" :
      case "javax.swing.plaf.FontUIResource" :
      case "com.apple.laf.AquaFonts.DerivedUIResourceFont" :
	 String fnm = getStringField(cv,"java.awt.Font.name");
	 int fsz = getIntField(cv,"java.awt.Font.size");
	 int fstyle = getIntField(cv,"java.awt.Font.style");
	 return "<FONT NAME='" + fnm + "' SIZE='" + fsz + "' STYLE='" + fstyle + "' />";

      case "java.awt.RenderingHints" :
	 // should fix this
	 return "<HINTS />";

      case "java.awt.AlphaComposite" :
	 double alpha = getDoubleField(cv,"java.awt.AlphaComposite.extraAlpha");
	 int rule = getIntField(cv,"java.awt.AlphaComposite.rule");
	 return "<ALPHACOMP RULE='" + rule + "' ALPHA='" + alpha + "' />";

      case "java.awt.BasicStroke" :
	 return "<BASICSTROKE />";

      case "java.awt.geom.AffineTransform" :
	 int m00 = getIntField(cv,"java.awt.geom.AffineTransform.m00");
	 int m01 = getIntField(cv,"java.awt.geom.AffineTransform.m01");
	 int m02 = getIntField(cv,"java.awt.geom.AffineTransform.m02");
	 int m10 = getIntField(cv,"java.awt.geom.AffineTransform.m10");
	 int m11 = getIntField(cv,"java.awt.geom.AffineTransform.m11");
	 int m12 = getIntField(cv,"java.awt.geom.AffineTransform.m12");
	 return "<TRANSFORM M00='" + m00 + "' M01='" + m01 + "' M02='" + m02 + "' " +
		"M10='" + m10 + "' M11='" + m11 + "' M12='" + m12 + "' />";

      case "java.awt.GradientPaint" :
	  CashewValue col1 = cv.getFieldValue(sess,typer,getClock(),
                "java.awt.GradientPaint.color1",getContext());
	  int cval1 = getIntField(col1,"java.awt.Color.value");
	  CashewValue col2 = cv.getFieldValue(sess,typer,getClock(),
                "java.awt.GradientPaint.color2",getContext());
	  int cval2 = getIntField(col2,"java.awt.Color.value");
	  CashewValue pt1 = cv.getFieldValue(sess,typer,getClock(),
                "java.awt.GradientPaint.p1",getContext());
	  float f1x = getFloatField(pt1,"java.awt.geom.Point2D.Float,x");
	  float f1y = getFloatField(pt1,"java.awt.geom.Point2D.Float.y");
	  CashewValue pt2 = cv.getFieldValue(sess,typer,getClock(),
                "java.awt.GradientPaint.p2",getContext());
	  float f2x = getFloatField(pt2,"java.awt.geom.Point2D.Float,x");
	  float f2y = getFloatField(pt2,"java.awt.geom.Point2D.Float.y");
	  boolean cyc = getBooleanField(cv,"java.awt.GradientPaint.cyclic");
	  return "<GRADIENT C1='#" + Integer.toHexString(cval1) + "' C2='#" + Integer.toHexString(cval2) + "' " +
	       " X1='" + f1x + "' Y1='" + f1y + "' X2='" + f2x + "' Y2='" + f2y + "' CYC='" + cyc + "' />";

      default :
         AcornLog.logE("CUMIN","Unknown field type " + fldtyp);
	 break;
    }

   return "<VALUE TYPE='" + cv.getDataType(sess,getClock(),null) + "' >" +
	cv.getString(sess,typer,getClock()) + "</VALUE>";
}


private int getIntField(CashewValue cv,String name) throws CashewException
{
   CashewValue fval = cv.getFieldValue(getSession(),getTyper(),getClock(),
         name,getContext());
   if (fval == null) return 0;
   return fval.getNumber(getSession(),getClock()).intValue();
}


private CashewValue getOptionalField(CashewValue cv,String name) throws CashewException
{
   if (cv == null) return null;
   CashewValue fval = cv.getFieldValue(getSession(),getTyper(),getClock(),
         name,getContext(),false);
   return fval;
}


private double getDoubleField(CashewValue cv,String name) throws CashewException
{
   CashewValue fval = cv.getFieldValue(getSession(),getTyper(),getClock(),
         name,getContext());
   if (fval == null) return 0;
   return fval.getNumber(getSession(),getClock()).doubleValue();
}


private float getFloatField(CashewValue cv,String name) throws CashewException
{
   CashewValue fval = cv.getFieldValue(getSession(),getTyper(),getClock(),
         name,getContext());
   if (fval == null) return 0;
   return fval.getNumber(getSession(),getClock()).floatValue();
}


private boolean getBooleanField(CashewValue cv,String name) throws CashewException
{
   CashewValue fval = cv.getFieldValue(getSession(),getTyper(),getClock(),
         name,getContext());
   if (fval == null) return false;
   return fval.getBoolean(getSession(),getClock());
}


private String getStringField(CashewValue cv,String name) throws CashewException
{
   CashewValue fval = cv.getFieldValue(getSession(),getTyper(),getClock(),
         name,getContext());
   if (fval == null) return null;;
   return fval.getString(getSession(),getTyper(),getClock());
}



/********************************************************************************/
/*										*/
/*	GraphicsOutput == hold output for a window				*/
/*										*/
/********************************************************************************/

private class GraphicsOutput {

   private String	poppy_id;
   private CashewValue	poppy_graphics;
   private int		current_index;
   private String	current_fg;
   private String	current_bg;
   private String	current_paint;
   private String	current_font;
   private String	current_stroke;
   private String	current_composite;
   private String	current_hints;
   private String	current_clip;
   private String	current_transform;
   private GraphicsOutput parent_graphics;
   private IvyXmlWriter command_list;
   private boolean	skip_translate;

   GraphicsOutput(CashewValue cv) throws CashewException {
      poppy_graphics = cv;
      parent_graphics = this;
      poppy_id = cv.getFieldValue(getSession(),getTyper(),getClock(),
            "edu.brown.cs.seede.poppy.PoppyGraphics.poppy_id",getContext()).
            getString(getSession(),getTyper(),getClock());
      CashewValue dimv = cv.getFieldValue(getSession(),getTyper(),getClock(),
            "edu.brown.cs.seede.poppy.PoppyGraphics.poppy_width",getContext());
      int wid = dimv.getNumber(getSession(),getClock()).intValue();
      dimv = cv.getFieldValue(getSession(),getTyper(),getClock(),
            "edu.brown.cs.seede.poppy.PoppyGraphics.poppy_height",getContext());
      int ht = dimv.getNumber(getSession(),getClock()).intValue();
   
      current_index = 0;
      clearCurrents();
   
      command_list = new IvyXmlWriter();
      command_list.begin("GRAPHICS");
      command_list.field("WIDTH",wid);
      command_list.field("HEIGHT",ht);
      command_list.field("ID",poppy_id);
      skip_translate = false;
    }


   private void clearCurrents() {
      current_clip = null;
      current_transform = null;
      current_fg = null;
      current_bg = null;
      current_paint = null;
      current_font = null;
      current_stroke = null;
      current_composite = null;
      current_hints = null;
   }

   void setNested(GraphicsOutput go) {
      parent_graphics = go.parent_graphics;
      command_list = null;
    }

   String getResult() {
      IvyXmlWriter xw = parent_graphics.command_list;
      xw.end("GRAPHICS");
      return xw.toString();
    }

   IvyXmlWriter getCommandList()		{ return parent_graphics.command_list; }

   private void addFields() throws CashewException {
      boolean upd = updateIndex();
      current_transform = updateField(current_transform,"user_transform",FieldType.TRANSFORM);
      current_clip = updateField(current_clip,"user_clip",FieldType.CLIP);
      current_fg = updateField(current_fg,"fg_color",FieldType.FG);
      current_bg = updateField(current_bg,"bg_color",FieldType.BG);
      current_paint = updateField(current_paint,"user_paint",FieldType.PAINT);
      current_font = updateField(current_font,"user_font",FieldType.FONT);
      current_stroke = updateField(current_stroke,"user_stroke",FieldType.STROKE);
      current_hints = updateField(current_hints,"user_hints",FieldType.HINTS);
      current_composite = updateField(current_composite,"user_composite",FieldType.COMPOSITE);
      if (upd) {
         addInitializations();
       }
   }

   private void addInitializations() {
      IvyXmlWriter xw = getCommandList();
      if (current_transform != null) {
	 xw.begin("DRAW");
	 xw.field("TYPE",CommandType.TRANSFORM);
	 xw.field("NUMARGS",1);
	 xw.field("TIME",getClock().getTimeValue());
	 xw.begin("ARG");
	 xw.field("TYPE","java.awt.geom.AffineTransform");
	 xw.xmlText(current_transform);
	 xw.end("ARG");
	 xw.end("DRAW");
       }
      if (current_clip != null) {
	 xw.begin("DRAW");
	 xw.field("TYPE",CommandType.CLIP);
	 xw.field("NUMARGS",1);
	 xw.field("TIME",getClock().getTimeValue());
	 xw.begin("ARG");
	 xw.field("TYPE","java.awt.Rectangle");
	 xw.xmlText(current_clip);
	 xw.end("ARG");
	 xw.end("DRAW");
       }
   }

   private String updateField(String cur,String fld,FieldType typ) throws CashewException {
      String fld1 = "edu.brown.cs.seede.poppy.PoppyGraphics." + fld;
      CashewValue fval = poppy_graphics.getFieldValue(getSession(),getTyper(),getClock(),
            fld1,getContext());
      String nval = encodeField(fval);
      // AcornLog.logD("Update Field " + fld + " " + cur + " " + nval);
      if (nval == null && cur == null) return cur;
      else if (nval != null && nval.equals(cur)) return cur;
      IvyXmlWriter xw = getCommandList();
      xw.begin("FIELD");
      xw.field("TIME",getClock().getTimeValue());
      xw.field("TYPE",typ);
      xw.xmlText(nval);
      xw.end("FIELD");
      return nval;
    }

   private boolean updateIndex() throws CashewException
   {
      String fld1 = "edu.brown.cs.seede.poppy.PoppyGraphics.poppy_index";
      CashewValue fval = poppy_graphics.getFieldValue(getSession(),getTyper(),getClock(),
            fld1,getContext());
      String fld2 = "edu.brown.cs.seede.poppy.PoppyGraphics.parent_index";
      CashewValue fval2 = poppy_graphics.getFieldValue(getSession(),getTyper(),getClock(),
            fld2,getContext());
      int nidx = fval.getNumber(getSession(),getClock()).intValue();
      int pidx = fval2.getNumber(getSession(),getClock()).intValue();
      if (nidx == current_index) return false;
      AcornLog.logD("CUMIN","Update graphics index " + nidx);
      IvyXmlWriter xw = getCommandList();
      xw.begin("INDEX");
      xw.field("TIME",getClock().getTimeValue());
      xw.field("VALUE",nidx);
      xw.field("PARENT",pidx);
      xw.end("INDEX");
      boolean upd = (current_index == 0 && poppy_id.startsWith("MAIN_"));
      current_index = nidx;
      clearCurrents();
      return upd;
   }

   void skipTranslate() 			{ skip_translate = true; }
   boolean checkSkipTranslate() {
      boolean fg = skip_translate;
      skip_translate = false;
      return fg;
    }

}	// end of inner class GraphicsOutput





}	// end of class CuminGraphicsEvaluator




/* end of CuminGraphicsEvaluator.java */


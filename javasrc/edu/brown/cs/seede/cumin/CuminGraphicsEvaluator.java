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
import edu.brown.cs.ivy.xml.IvyXmlWriter;
import edu.brown.cs.seede.acorn.AcornLog;
import edu.brown.cs.seede.cashew.CashewConstants;
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
/*	Constructors								*/
/*										*/
/********************************************************************************/

CuminGraphicsEvaluator(CuminRunnerByteCode bc)
{
   super(bc);
}



/********************************************************************************/
/*										*/
/*	Entry point for PoppyGraphics methods					*/
/*										*/
/********************************************************************************/

void checkPoppyGraphics()
{
   if (getMethod().isStatic() || getMethod().isConstructor()) return;

   CashewValue rslt = null;

   CashewValue thisarg = getValue(0);
   GraphicsOutput graphics = output_map.get(thisarg);
   if (graphics == null) {
      graphics = new GraphicsOutput(thisarg);
      output_map.put(thisarg,graphics);
    }

   switch (getMethod().getName()) {
      case "create" :
	 return;
      case "dispose" :
	 output_map.remove(thisarg);
	 return;
      case "initialize" :
         return;
      case "setupComplete" :
	 CashewValue ogr = getValue(1);
	 GraphicsOutput gg = output_map.get(ogr);
	 if (gg != null) {
	    graphics.setNested(gg);
	  }
         handleCreate(graphics,gg);
	 break;
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
         return;
      case "rotate" :
         createCommand(graphics,CommandType.ROTATE);
         return;
      case "scale" :
         createCommand(graphics,CommandType.SCALE);
         return;
      case "shear" :
         createCommand(graphics,CommandType.SHEAR);
         return;
      case "setTransform" :
         createCommand(graphics,CommandType.SET_TRANSFORM);
         return;
      case "getTransform" :
         createCommand(graphics,CommandType.GET_TRANSFORM);
         return;
      case "translate" :
         if (!graphics.checkSkipTranslate())
            createCommand(graphics,CommandType.TRANSLATE);
         return;
         
      case "clipRect" :
         createCommand(graphics,CommandType.CLIP_RECT);
         return;
      case "setClip" :
         createCommand(graphics,CommandType.SET_CLIP);
         return;
      case "clip" :
         createCommand(graphics,CommandType.CLIP);
         return;
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
	 return;

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
	 return;

      default :
	 AcornLog.logE("Unexcepted method " + getMethod());
	 return;

      case "getReport" :
	 rslt = CashewValue.stringValue(graphics.getResult());
	 output_map.remove(thisarg);
	 break;
    }

   throw new CuminRunError(CuminRunError.Reason.RETURN,rslt);
}



/********************************************************************************/
/*                                                                              */
/*      Handle graphics callback -- constrain to inner window                   */
/*                                                                              */
/********************************************************************************/

void checkGraphicsCallback()
{
   if (getMethod().isStatic() || getMethod().isConstructor()) return;
   
   switch (getMethod().getName()) {
      case "constrainGraphics" :
         CashewValue garg = getValue(1);
         GraphicsOutput graphics = output_map.get(garg);
         CashewValue rect = getValue(2);
         constrainGraphics(graphics,rect);
         graphics.skipTranslate();
         return;
      default :
         return;
    }
}



private void constrainGraphics(GraphicsOutput g,CashewValue rect)
{
   g.addFields();
   
   IvyXmlWriter xw = g.getCommandList();
   xw.begin("DRAW");
   xw.field("TYPE",CommandType.CONSTRAIN);
   xw.field("NUMARGS",4);
   xw.field("TIME",getClock().getTimeValue());
   outputArg(xw,rect.getFieldValue(getClock(),"java.awt.Rectangle.x"));
   outputArg(xw,rect.getFieldValue(getClock(),"java.awt.Rectangle.y"));
   outputArg(xw,rect.getFieldValue(getClock(),"java.awt.Rectangle.width"));
   outputArg(xw,rect.getFieldValue(getClock(),"java.awt.Rectangle.height"));
   xw.end("DRAW");
}

private void handleCreate(GraphicsOutput g,GraphicsOutput par)
{
   par.addFields();
   g.addFields();
}


private void outputArg(IvyXmlWriter xw,CashewValue cv)
{
   xw.begin("ARG");
   xw.field("TYPE","int");
   xw.field("VALUE",cv.getNumber(getClock()).intValue());
   xw.end("ARG");
}




/********************************************************************************/
/*										*/
/*	Command Encoding routines						*/
/*										*/
/********************************************************************************/

private void createCommand(GraphicsOutput g,CommandType typ)
{
   g.addFields();

   IvyXmlWriter xw = g.getCommandList();
   xw.begin("DRAW");
   xw.field("TYPE",typ);
   xw.field("NUMARGS",getNumArgs());
   xw.field("TIME",getClock().getTimeValue());

   for (int i = 1; i < getNumArgs(); ++i) {
      xw.begin("ARG");
      CashewValue cv = getValue(i);
      JcompType dtyp = cv.getDataType(getClock());
      xw.field("TYPE",dtyp.getName());
      if (cv.getDataType(getClock()).isPrimitiveType()) {
	 switch (dtyp.getName()) {
	    case "int" :
	    case "short" :
	    case "byte" :
	    case "char" :
	    case "long" :
	       xw.field("VALUE",cv.getNumber(getClock()).longValue());
	       break;
	    case "float" :
	    case "double" :
	       xw.field("VALUE",cv.getNumber(getClock()).doubleValue());
	       break;
	    case "boolean" :
	       xw.field("VALUE",cv.getBoolean(getClock()));
	       break;
	  }
       }
      else if (cv.isNull(getClock())) {
	 xw.field("NULL",true);
       }
      else if (dtyp == STRING_TYPE) {
	 xw.textElement("VALUE",cv.getString(getClock()));
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
      else {
	 xw.textElement("VALUE",cv.getString(getClock()));
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

private String encodeField(CashewValue cv)
{
   if (cv == null || cv.isNull(getClock())) return null;

   switch (cv.getDataType(getClock()).getName()) {
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
      default :
	 break;
    }

   return "<VALUE TYPE='" + cv.getDataType(getClock()) + "' >" + cv.getString(getClock()) + "</VALUE>";
}


private int getIntField(CashewValue cv,String name)
{
   CashewValue fval = cv.getFieldValue(getClock(),name);
   if (fval == null) return 0;
   return fval.getNumber(getClock()).intValue();
}


private double getDoubleField(CashewValue cv,String name)
{
   CashewValue fval = cv.getFieldValue(getClock(),name);
   if (fval == null) return 0;
   return fval.getNumber(getClock()).doubleValue();
}


private String getStringField(CashewValue cv,String name)
{
   CashewValue fval = cv.getFieldValue(getClock(),name);
   if (fval == null) return null;;
   return fval.getString(getClock());
}



/********************************************************************************/
/*										*/
/*	GraphicsOutput == hold output for a window				*/
/*										*/
/********************************************************************************/

private class GraphicsOutput {

   private String	poppy_id;
   private CashewValue	poppy_graphics;
   private int          current_index;
   private String	current_fg;
   private String	current_bg;
   private String	current_paint;
   private String	current_font;
   private String	current_stroke;
   private String	current_composite;
   private String	current_hints;
   private String       current_clip;
   private String       current_transform;
   private GraphicsOutput parent_graphics;
   private IvyXmlWriter command_list;
   private boolean      skip_translate;

   GraphicsOutput(CashewValue cv) {
      poppy_graphics = cv;
      parent_graphics = this;
      poppy_id = cv.getFieldValue(getClock(),"edu.brown.cs.seede.poppy.PoppyGraphics.poppy_id").getString(getClock());
      CashewValue dimv = cv.getFieldValue(getClock(),"edu.brown.cs.seede.poppy.PoppyGraphics.poppy_width");
      int wid = dimv.getNumber(getClock()).intValue();
      dimv = cv.getFieldValue(getClock(),"edu.brown.cs.seede.poppy.PoppyGraphics.poppy_height");
      int ht = dimv.getNumber(getClock()).intValue();
      
      current_index = 0;
      clearCurrents();
      
      command_list = new IvyXmlWriter();
      command_list.begin("GRAPHICS");
      command_list.field("WIDTH",wid);
      command_list.field("HEIGHT",ht);
      command_list.field("ID",poppy_id);
      skip_translate = false;
    }
   
   
   private void clearCurrents() 
   {
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

   private void addFields() {
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
   
   
   private void addInitializations()
   {
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

   private String updateField(String cur,String fld,FieldType typ) {
      String fld1 = "edu.brown.cs.seede.poppy.PoppyGraphics." + fld;
      CashewValue fval = poppy_graphics.getFieldValue(getClock(),fld1);
      String nval = encodeField(fval);
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
   
   private boolean updateIndex()
   {
      String fld1 = "edu.brown.cs.seede.poppy.PoppyGraphics.poppy_index";
      CashewValue fval = poppy_graphics.getFieldValue(getClock(),fld1);
      String fld2 = "edu.brown.cs.seede.poppy.PoppyGraphics.parent_index";
      CashewValue fval2 = poppy_graphics.getFieldValue(getClock(),fld2);
      int nidx = fval.getNumber(getClock()).intValue();
      int pidx = fval2.getNumber(getClock()).intValue();
      if (nidx == current_index) return false;
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
   
   void skipTranslate()                         { skip_translate = true; }
   boolean checkSkipTranslate() {
      boolean fg = skip_translate;
      skip_translate = false;
      return fg;
    }

}	// end of inner class GraphicsOutput








}	// end of class CuminGraphicsEvaluator




/* end of CuminGraphicsEvaluator.java */


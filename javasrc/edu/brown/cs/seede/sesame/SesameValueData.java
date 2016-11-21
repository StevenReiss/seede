/********************************************************************************/
/*                                                                              */
/*              SesameValueData.java                                            */
/*                                                                              */
/*      Hold value returned from Bubbles                                        */
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



package edu.brown.cs.seede.sesame;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;

import edu.brown.cs.ivy.jcomp.JcompType;
import edu.brown.cs.ivy.jcomp.JcompTyper;
import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.seede.acorn.AcornLog;
import edu.brown.cs.seede.cashew.CashewConstants;
import edu.brown.cs.seede.cashew.CashewValue;

class SesameValueData implements SesameConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private SesameSessionLaunch sesame_session;
private ValueKind val_kind;
private String val_name;
private String val_type;
private String val_value;
private String decl_type;
private boolean has_values;
private boolean is_local;
private boolean is_static;
private int array_length;
private Map<String,SesameValueData> sub_values;
private String var_detail;
private CashewValue result_value;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

SesameValueData(SesameSessionLaunch sm,Element xml) 
{
   sesame_session = sm;
   val_name = IvyXml.getAttrString(xml,"NAME");
   initialize(xml);
}

SesameValueData(SesameValueData par,Element xml) 
{
   sesame_session = par.sesame_session;
   val_name = par.val_name + "?" + IvyXml.getAttrString(xml,"NAME");
   initialize(xml);
}



/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

ValueKind getKind()	        { return val_kind; }
String getName()		{ return val_name; }
String getType()		{ return val_type; }
String getValue()		{ return val_value; }
String getDeclaredType()	{ return decl_type; }
String getActualType()	        { return null; }
boolean hasContents()	        { return has_values; }
boolean isLocal()		{ return is_local; }
boolean isStatic()		{ return is_static; }
String getFrame()	        { return sesame_session.getFrameId(); }
String getThread()              { return sesame_session.getThreadId(); }
int getLength()		{ return array_length; }



CashewValue getCashewValue()
{
   JcompTyper typer = sesame_session.getProject().getTyper();
   String vtype = val_type;
   int idx = vtype.indexOf("<");
   if (idx >= 0) {
      vtype = val_type.substring(0,idx);
    }
   
   JcompType typ = typer.findType(val_type);
   if (typ == null) {
      String ityp = val_type.replace("$",".");
      typ = typer.findType(ityp);
    }
   if (typ == null) {
      typ = typer.findType(vtype);
    }
   
   if (result_value == null) {
      switch (val_kind) {
         case PRIMITIVE :
            if (typ.isBooleanType()) {
               result_value = CashewValue.booleanValue(val_value);
             }
            else if (typ.isNumericType()) {
               result_value = CashewValue.numericValue(typ,val_value);
             }
            break;
         case STRING :
            result_value = CashewValue.stringValue(val_value);
            break;
         case OBJECT :
            Map<String,Object> inits = new HashMap<String,Object>();
            typ.defineAll(typer);
            for (Map.Entry<String,JcompType> ent : typ.getFields().entrySet()) {
               String fnm = ent.getKey();
               if (sub_values != null && sub_values.get(fnm) != null) {
                  SesameValueData fsvd = sub_values.get(fnm);
                  fsvd = sesame_session.getUniqueValue(fsvd);
                  inits.put(fnm,fsvd.getCashewValue());
                }
               else {
                  DeferredLookup def = new DeferredLookup(fnm);
                  inits.put(fnm,def);
                }
             }
            result_value = CashewValue.objectValue(typ,inits);
            break;
         case ARRAY :
            break;
         case CLASS :
            break;
       }
      if (result_value == null) {
         AcornLog.logE("Unknown conversion to cashew value from bubbles");
       }
    }
   return result_value;
}


Collection<String> getVariables()
 {
   computeValues();
   if (sub_values == null) return Collections.emptyList();
   return new ArrayList<String>(sub_values.keySet());
}

SesameValueData getValue(String var)
{
   computeValues();
   if (sub_values == null) return null;
   return sub_values.get(var);
}


String getDetail() 
{
   if (var_detail == null) {
      CommandArgs args = new CommandArgs("FRAME",getFrame(),"THREAD",getThread());
      String varxml = "<VAR>" + IvyXml.xmlSanitize(val_name) + "</VAR>";
      Element xml = sesame_session.getControl().getXmlReply("VARDETAIL",sesame_session.getProject(),args,varxml,0);
      Element val = IvyXml.getChild(xml,"VALUE");
      var_detail = IvyXml.getTextElement(val,"DESCRIPTION");
      if (var_detail == null) var_detail = "<< UNKNOWN >>";
    }
   if (var_detail == "<< UNKNOWN >>") return null;
   
   return var_detail;
}



/********************************************************************************/
/*                                                                              */
/*      Setup methods                                                           */
/*                                                                              */
/********************************************************************************/

private void initialize(Element xml)
{
   val_kind = IvyXml.getAttrEnum(xml,"KIND",ValueKind.UNKNOWN);
   val_type = IvyXml.getAttrString(xml,"TYPE");
   val_value = IvyXml.getTextElement(xml,"DESCRIPTION");
   if (val_value == null) val_value = "";
   has_values = IvyXml.getAttrBool(xml,"HASVARS");
   is_local = IvyXml.getAttrBool(xml,"LOCAL");
   is_static = IvyXml.getAttrBool(xml,"STATIC");
   decl_type = IvyXml.getAttrString(xml,"DECLTYPE");
   array_length = IvyXml.getAttrInt(xml,"LENGTH",0);
   sub_values = null;
   var_detail = null;
   addValues(xml);
}


private void addValues(Element xml) 
{
   if (xml == null) return;
   for (Element e : IvyXml.children(xml,"VALUE")) {
      if (sub_values == null) sub_values = new HashMap<String,SesameValueData>();
      SesameValueData vd = new SesameValueData(this,e);
      vd = sesame_session.getUniqueValue(vd);
      sub_values.put(vd.getName(),vd);
    }
}

private synchronized void computeValues() 
{
   if (!has_values || sub_values != null) return;
   CommandArgs args = new CommandArgs("FRAME",getFrame(),"THREAD",getThread(),"DEPTH",1);
   String var = "<VAR>" + IvyXml.xmlSanitize(val_name) + "</VAR>";
   Element xml = sesame_session.getControl().getXmlReply("VARVAL",sesame_session.getProject(),args,var,0);
   if (IvyXml.isElement(xml,"RESULT")) {
      Element root = IvyXml.getChild(xml,"VALUE");
      addValues(root);
    }
}




/********************************************************************************/
/*                                                                              */
/*      Deferred value lookup                                                   */
/*                                                                              */
/********************************************************************************/

private class DeferredLookup implements CashewConstants.CashewDeferredValue {
   
   private String field_name;
   
   DeferredLookup(String name) {
      field_name = name;
    }
   
   @Override public CashewValue getValue() {
      computeValues();
      if (sub_values == null) return null;
      String fnm = field_name;
      int idx = fnm.lastIndexOf(".");
      if (idx >= 0) fnm = fnm.substring(idx+1);
      String lookup = val_name + "?" + fnm;
      SesameValueData svd = sub_values.get(lookup);
      svd = sesame_session.getUniqueValue(svd);
      if (svd == null) return null;
      return svd.getCashewValue();
    }
   
}       // end of inner class DeferredLookup




}       // end of class SesameValueData




/* end of SesameValueData.java */


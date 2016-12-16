/********************************************************************************/
/*										*/
/*		SesameValueData.java						*/
/*										*/
/*	Hold value returned from Bubbles					*/
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
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private SesameSessionLaunch sesame_session;
private ValueKind val_kind;
private String val_name;
private String val_expr;
private String val_type;
private String val_value;
private String val_thread;
private boolean has_values;
private boolean is_local;
private boolean is_static;
private int array_length;
private Map<String,SesameValueData> sub_values;
private String var_detail;
private CashewValue result_value;
private int hash_code;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

SesameValueData(SesameSessionLaunch sm,String thread,Element xml,String name)
{
   sesame_session = sm;
   val_thread = thread;
   if (name == null) val_name = IvyXml.getAttrString(xml,"NAME");
   else val_name = name;
   val_expr = null;
   initialize(xml,null);
}

SesameValueData(SesameValueData par,Element xml)
{
   sesame_session = par.sesame_session;
   val_thread = par.val_thread;
   if (par.val_expr != null) {
      val_expr = par.val_expr + "." + IvyXml.getAttrString(xml,"NAME");
    }
   val_name = par.val_name + "?" + IvyXml.getAttrString(xml,"NAME");

   initialize(xml,val_expr);
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

ValueKind getKind()		{ return val_kind; }

String getType()		{ return val_type; }
String getValue()		{ return val_value; }

String getActualType()		{ return null; }
boolean hasContents()		{ return has_values; }
boolean isLocal()		{ return is_local; }
boolean isStatic()		{ return is_static; }
String getFrame()		{ return sesame_session.getFrameId(val_thread); }
String getThread()		{ return val_thread; }
int getLength() 		{ return array_length; }



CashewValue getCashewValue()
{
   if (result_value != null) return result_value;

   JcompTyper typer = sesame_session.getProject().getTyper();

   if (val_kind == ValueKind.UNKNOWN && val_type == null) {
      return null;
    }

   if (val_type != null && val_type.equals("null")) {
      return CashewValue.nullValue();
    }
   if (val_type != null && val_type.equals("void")) return null;

   String vtype = val_type;
   if (vtype != null) {
      int idx = vtype.indexOf("<");
      int idx1 = vtype.lastIndexOf(">");
      if (idx >= 0) {
	 vtype = val_type.substring(0,idx);
	 if (idx1 > 0) vtype += val_type.substring(idx1+1);
       }
    }

   JcompType typ = typer.findType(val_type);
   if (typ == null && val_type != null) {
      String ityp = val_type.replace("$",".");
      typ = typer.findType(ityp);
    }
   if (typ == null && vtype != null) {
      typ = typer.findType(vtype);
    }
   if (typ == null && vtype != null) {
      String ityp = vtype.replace("$",".");
      typ = typer.findType(ityp);
    }
   if (typ == null) {
      typ = typer.findSystemType(val_type);
    }
   if (typ == null) {
      AcornLog.logE("TYPE " + val_type +  " " + vtype + " not found");
      return CashewValue.nullValue();
    }

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
	    String key = fnm;
	    int idx1 = fnm.lastIndexOf(".");
	    if (idx1 >= 0) key = fnm.substring(idx1+1);
	    key = getKey(key);
	    if (sub_values != null && sub_values.get(key) != null) {
	       SesameValueData fsvd = sub_values.get(key);
	       fsvd = sesame_session.getUniqueValue(fsvd);
	       inits.put(fnm,fsvd.getCashewValue());
	     }
	    else {
	       DeferredLookup def = new DeferredLookup(fnm);
	       inits.put(fnm,def);
	     }
	  }
	 if (hash_code == 0) {
	    inits.put(CashewConstants.HASH_CODE_FIELD,new DeferredLookup(CashewConstants.HASH_CODE_FIELD));
	  }
	 else {
	    CashewValue hvl = CashewValue.numericValue(CashewConstants.INT_TYPE,hash_code);
	    inits.put(CashewConstants.HASH_CODE_FIELD,hvl);
	  }
	 result_value = CashewValue.objectValue(typ,inits);
	 break;
      case ARRAY :
	 if (array_length <= 1024) computeValues();
	 Map<Integer,Object> ainits = new HashMap<Integer,Object>();
	 for (int i = 0; i < array_length; ++i) {
	    String key = "[" + i + "]";
	    key = getKey(key);
	    if (sub_values != null && sub_values.get(key) != null) {
	       SesameValueData fsvd = sub_values.get(key);
	       fsvd = sesame_session.getUniqueValue(fsvd);
	       ainits.put(i,fsvd.getCashewValue());
	     }
	    else {
	       DeferredLookup def = new DeferredLookup(key);
	       ainits.put(i,def);
	     }
	  }
	 result_value = CashewValue.arrayValue(typ,array_length,ainits);
	 // AcornLog.logD("BUILT ARRAY : " + result_value);
	 break;
      case CLASS :
	 int idx2 = val_value.lastIndexOf("(");
	 String tnm = val_value.substring(0,idx2);
	 if (tnm.startsWith("(")) {
	    idx2 = tnm.lastIndexOf(")");
	    tnm = tnm.substring(1,idx2).trim();
	  }
	 JcompType ctyp = typer.findType(tnm);
	 result_value = CashewValue.classValue(ctyp);
	 break;
      case UNKNOWN :
	 break;
    }

   if (result_value == null) {
      AcornLog.logE("Unknown conversion to cashew value from bubbles");
    }

   return result_value;
}


private String getKey(String fnm)
{
   return val_name + "?" + fnm;
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
/*										*/
/*	Setup methods								*/
/*										*/
/********************************************************************************/

private void initialize(Element xml,String expr)
{
   val_type = IvyXml.getAttrString(xml,"TYPE");
   if (val_type != null && val_type.equals("edu.brown.cs.seede.poppy.PoppyValue$Return")) {
      Element objxml = null;
      int refid = 0;
      int hashcode = 0;
      for (Element celt : IvyXml.children(xml,"VALUE")) {
	 switch (IvyXml.getAttrString(celt,"NAME")) {
	    case "for_object" :
	       objxml = celt;
	       break;
	    case "ref_id" :
	       refid = Integer.parseInt(IvyXml.getTextElement(celt,"DESCRIPTION"));
	       break;
	    case "hash_code" :
	       hashcode = Integer.parseInt(IvyXml.getTextElement(celt,"DESCRIPTION"));
	       break;
	  }
       }
      val_type = IvyXml.getAttrString(objxml,"TYPE");
      String nexpr = "edu.brown.cs.seede.poppy.PoppyValue.getValue(" + refid + ")";
      nexpr = "((" + val_type + ") " + nexpr + ")";
      initialize(objxml,nexpr);
      hash_code = hashcode;
      return;
    }

   val_kind = IvyXml.getAttrEnum(xml,"KIND",ValueKind.UNKNOWN);
   val_value = IvyXml.getTextElement(xml,"DESCRIPTION");
   if (val_value == null) val_value = "";
   has_values = IvyXml.getAttrBool(xml,"HASVARS");
   is_local = IvyXml.getAttrBool(xml,"LOCAL");
   is_static = IvyXml.getAttrBool(xml,"STATIC");
   array_length = IvyXml.getAttrInt(xml,"LENGTH",0);
   sub_values = null;
   var_detail = null;
   hash_code = 0;
   val_expr = expr;
   addValues(xml);
}


private void addValues(Element xml)
{
   if (xml == null) return;
   for (Element e : IvyXml.children(xml,"VALUE")) {
      if (sub_values == null) sub_values = new HashMap<String,SesameValueData>();
      SesameValueData vd = new SesameValueData(this,e);
      String nm = vd.val_name;
      vd = sesame_session.getUniqueValue(vd);
      sub_values.put(nm,vd);
      // AcornLog.logD("ADD VALUE " + nm + " = " + vd);
    }
}

private synchronized void computeValues()
{
   if (!has_values || sub_values != null) return;
   if (val_expr == null) {
      CommandArgs args = new CommandArgs("FRAME",getFrame(),"THREAD",getThread(),"DEPTH",2);
      String var = "<VAR>" + IvyXml.xmlSanitize(val_name) + "</VAR>";
      Element xml = sesame_session.getControl().getXmlReply("VARVAL",sesame_session.getProject(),args,var,0);
      if (IvyXml.isElement(xml,"RESULT")) {
	 Element root = IvyXml.getChild(xml,"VALUE");
	 addValues(root);
       }
    }
   else {
      SesameValueData svd = sesame_session.evaluateData(val_expr);
      sub_values = svd.sub_values;
    }
}




/********************************************************************************/
/*										*/
/*	Deferred value lookup							*/
/*										*/
/********************************************************************************/

private class DeferredLookup implements CashewConstants.CashewDeferredValue {

   private String field_name;

   DeferredLookup(String name) {
      field_name = name;
    }

   @Override public CashewValue getValue() {
      computeValues();
      if (field_name.equals(CashewConstants.HASH_CODE_FIELD)) {
	 if (sub_values == null) sub_values = new HashMap<String,SesameValueData>();
	 if (sub_values.get(field_name) == null) {
	    SesameValueData svd = null;
	    if (val_expr != null) {
	       svd = sesame_session.evaluateData("System.identityHashCode(" + val_expr + ")");
	     }
	    else {
	       CommandArgs args = new CommandArgs("FRAME",getFrame(),"THREAD",getThread(),"DEPTH",1);
	       String var = "<VAR>" + IvyXml.xmlSanitize(val_name) + "?@hashCode</VAR>";
	       Element xml = sesame_session.getControl().getXmlReply("VARVAL",sesame_session.getProject(),args,var,0);
	       if (IvyXml.isElement(xml,"RESULT")) {
		  svd = new SesameValueData(sesame_session,val_thread,xml,null);
		}
	     }
	    if (svd != null) sub_values.put(field_name,svd);
	  }
       }
      if (sub_values == null) return null;
      String fnm = field_name;
      int idx = fnm.lastIndexOf(".");
      if (idx >= 0) fnm = fnm.substring(idx+1);
      String lookup = getKey(fnm);
      SesameValueData svd = sub_values.get(lookup);
      svd = sesame_session.getUniqueValue(svd);
      if (svd == null) {
	 AcornLog.logE("Deferred Lookup of " + lookup + " not found");
	 return null;
       }
      CashewValue cvr = svd.getCashewValue();
      // AcornLog.logD("Deferred Lookup of " + lookup + " = " + cvr);
      return cvr;
    }

}	// end of inner class DeferredLookup



/********************************************************************************/
/*										*/
/*	Debugging methods							*/
/*										*/
/********************************************************************************/

@Override public String toString()
{
   StringBuffer buf = new StringBuffer();
   buf.append("<<");
   buf.append(val_kind);
   buf.append(":");
   buf.append(val_type);
   buf.append("@");
   buf.append(val_value);
   if (array_length > 0) buf.append("#" + array_length);
   buf.append(" ");
   buf.append(val_name);
   buf.append(">>");
   return buf.toString();
}




}	// end of class SesameValueData




/* end of SesameValueData.java */


/********************************************************************************/
/*										*/
/*		CashewValueString.java						*/
/*										*/
/*	Internal representation of string					*/
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



package edu.brown.cs.seede.cashew;

import java.util.HashMap;
import java.util.Map;

import edu.brown.cs.ivy.jcomp.JcompType;
import edu.brown.cs.ivy.jcomp.JcompTyper;
import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.ivy.xml.IvyXmlWriter;



public class CashewValueString extends CashewValue implements CashewConstants
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private String string_value;
private CashewValue value_field;
private CashewValue hash_field;
private CashewValue hash32_field;
private CashewValue coder_field;


/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

CashewValueString(JcompTyper typer,JcompType styp,String s)
{
   super(styp);
   setInitialValue(typer,s,-1);
  
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

@Override public String getString(CashewValueSession sess,
      JcompTyper typer,CashewClock cc,int lvl,boolean dbg)
{
   return string_value;
}



@Override public String getInternalRepresentation(CashewValueSession sess,CashewClock cc)
{
   if (string_value == null) return "null";
   String rslt = super.getInternalRepresentation(sess,cc);
   if (rslt != null) return rslt;

   StringBuffer buf = new StringBuffer();
   buf.append("\"");
   for (int i = 0; i < string_value.length(); ++i) {
      char c = string_value.charAt(i);
      switch (c) {
	 case '\\' :
	    buf.append("\\\\");
	    break;
	 case '\"' :
	    buf.append("\\");
	    break;
	 case '\n' :
	    buf.append("\\n");
	    break;
	 case '\t' :
	    buf.append("\\t");
	    break;
	 case '\b' :
	    buf.append("\\b");
	    break;
	 case '\f' :
	    buf.append("\\f");
	    break;
	 case '\r' :
	    buf.append("\\r");
	    break;
	 default :
	    if (c < 32 || c >= 128) {
	       buf.append("\\u");
	       buf.append(Integer.toHexString(c/16/16/16));
	       buf.append(Integer.toHexString((c/16/16)%16));
	       buf.append(Integer.toHexString((c/16)%16));
	       buf.append(Integer.toHexString(c%16));
	     }
	    else buf.append(c);
	    break;
       }
    }
   buf.append("\"");
   return buf.toString();
}




@Override public synchronized CashewValue getFieldValue(CashewValueSession sess,
      JcompTyper typer,CashewClock cc,String name,CashewContext ctx,boolean force)
{
   switch (name) {
      case "value" :
      case "java.lang.String.value" :
	 if (value_field == null) {
	    Map<Integer,Object> inits = new HashMap<Integer,Object>();
            byte [] bytes = string_value.getBytes();
	    for (int i = 0; i < bytes.length; ++i) {
	       inits.put(i,CashewValue.numericValue(typer,typer.BYTE_TYPE,bytes[i]));
	     }
	    value_field = CashewValue.arrayValue(typer,typer.BYTE_TYPE,bytes.length,inits);
	  }
	 return value_field;
      case "hash" :
      case "java.lang.String.hash" :
	 if (hash_field == null) {
	    hash_field = CashewValue.numericValue(typer,typer.INT_TYPE,string_value.hashCode());
	  }
	 return hash_field;
      case "hash32" :
      case "java.lang.String.hash32" :
	 if (hash32_field == null) {
	    hash32_field = CashewValue.numericValue(typer,typer.INT_TYPE,0);
	  }
	 return hash32_field;
      case "coder" :
      case "java.lang.String.coder" :
	 if (coder_field == null) {
	    coder_field = CashewValue.numericValue(typer,typer.BYTE_TYPE,0);
	  }
	 return coder_field;
      default :
	 if (force) {
	    throw new Error("Illegal string field access for " + name);
	  }
	 else return null;
    }
}



/********************************************************************************/
/*										*/
/*	Output methods								*/
/*										*/
/********************************************************************************/

@Override public CashewValue setFieldValue(CashewValueSession sess,
      JcompTyper typer,CashewClock cc,String name,CashewValue v)
	throws CashewException
{
   switch (name) {
      case "value" :
      case "java.lang.String.value" :
	 value_field = v;
	 int dim = v.getDimension(sess,cc);
	 char [] rslt = new char[dim];
	 for (int i = 0; i < dim; ++i) {
	    rslt[i] = v.getIndexValue(sess,cc,i).getChar(sess,cc);
	  }
	 string_value = new String(rslt);
	 break;
      case "hash" :
      case "java.lang.String.hash" :
	 hash_field = v;
	 break;
      case "hash32" :
      case "java.lang.String.hash32" :
	 hash32_field = v;
	 break;
      case "coder" :
      case "java.lang.String.coder" :
	 coder_field = v;
	 break;
      default :
	 throw new Error("Illegal string field access for " + name);

    }
   return v;
}



public void setInitialValue(JcompTyper typer,String s,int coder)
{
   string_value = s;
   value_field = null;
   hash_field = null;
   hash32_field = null;
   coder_field = null;
   if (coder < 0 && s != null && s.getBytes().length != s.length()) coder = 1;
   if (coder >= 0) {
      coder_field = CashewValue.numericValue(typer,typer.BYTE_TYPE,coder);
    }
}



@Override public void outputLocalXml(IvyXmlWriter xw,CashewOutputContext ctx,String name)
{
   int len = string_value.length();
   boolean usechars = false;
   for (int i = 0; i < len; ++i) {
      if (isBadChar(string_value.charAt(i))) {
         usechars = true;
         break;
       }
    }
   if (usechars) {
      xw.field("CHARS",true);
      xw.field("LENGTH",len);
      xw.text(IvyXml.encodeCharacters(string_value));
    }
   else {
      xw.cdata(string_value);
    }
}


private boolean isBadChar(char ch)
{
   if (ch >= 128) return true;
   if (ch >= 32) return false;
   if (ch == '\r' || ch == '\n' || ch == '\t') return false;
   return true;
}


/********************************************************************************/
/*                                                                              */
/*      Output methods                                                          */
/*                                                                              */
/********************************************************************************/

@Override public String toString()
{
   return "[[" + string_value + "]]";
}



}	// end of class CashewValueString




/* end of CashewValueString.java */


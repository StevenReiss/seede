/********************************************************************************/
/*                                                                              */
/*              CashewValue.java                                                */
/*                                                                              */
/*      Holder of a typed-value.  Values are immutable wrt a CashewClock.       */
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



package edu.brown.cs.seede.cashew;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;

import edu.brown.cs.ivy.jcomp.JcompType;
import edu.brown.cs.ivy.jcomp.JcompTyper;
import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.ivy.xml.IvyXmlWriter;

public abstract class CashewValue implements CashewConstants
{




/********************************************************************************/
/*                                                                              */
/*      Creation methods                                                        */
/*                                                                              */
/********************************************************************************/

public static CashewValue createValue(JcompTyper typer,Element xml) throws CashewException
{
   String tnm = IvyXml.getAttrString(xml,"TYPE");
   JcompType jtype = typer.findType(tnm);
   if (jtype == null) throw new CashewException("Unknown type");
   if (jtype == INT_TYPE || jtype == SHORT_TYPE || jtype == BYTE_TYPE || jtype == LONG_TYPE) {
      long v = IvyXml.getAttrLong(xml,"VALUE");
      return numericValue(jtype,v);
    }
   else if (jtype == BOOLEAN_TYPE) {
      boolean v = IvyXml.getAttrBool(xml,"VALUE");
      return booleanValue(v);
    }
   else if (jtype == CHAR_TYPE) {
      String s = IvyXml.getAttrString(xml,"VALUE");
      if (s.length() == 0) throw new CashewException("Empty character constants");
      char c = s.charAt(0);
      return characterValue(jtype,c);
    }
   else if (jtype == FLOAT_TYPE || jtype == DOUBLE_TYPE) {
      double dv = IvyXml.getAttrDouble(xml,"VALUE");
      return numericValue(jtype,dv);
    }
   else if (jtype == STRING_TYPE) {
      String s = IvyXml.getTextElement(xml,"VALUE");
      return stringValue(s);
    }
   else if (jtype == CLASS_TYPE) {
      String s = IvyXml.getTextElement(xml,"VALUE");
      if (s == null) return nullValue();
      JcompType vtyp = typer.findType(s);
      if (vtyp == null) throw new CashewException("Unknown type name " + s);
      return classValue(vtyp);
    }
   else if (jtype.getName().endsWith("[]")) {
      int dim = IvyXml.getAttrInt(xml,"DIM");
      Map<Integer,Object> inits = new HashMap<Integer,Object>();
      //TODO: set up contents of va
      CashewValueArray va = new CashewValueArray.ComputedValueArray(jtype,dim,inits);
      return va;
    }
   else if (jtype.isPrimitiveType()) {
      throw new CashewException("Illegal type for creation");
    }
   else {
      String val = IvyXml.getTextElement(xml,"VALUE");
      if (val != null && val.equals("null")) return nullValue();
      return objectValue(jtype);
    }
}



public static CashewValue createDefaultValue(JcompType type)
{
   if (type.isNumericType()) {
      return CashewValue.numericValue(type,0);
    }
   else if (type.isBooleanType()) {
      return CashewValue.booleanValue(false);
    }
   else if (type.getName().equals("char")) {
      return CashewValue.characterValue(type,(char) 0);
    }
   
   return CashewValue.nullValue();
}

public static CashewValue createReference(CashewValue base)
{
   CashewRef cr = new CashewRef(base);
   return cr;
}



/********************************************************************************/
/*                                                                              */
/*      Internal value finders                                                  */
/*                                                                              */
/********************************************************************************/

public static CashewValue nullValue()
{
   if (null_value == null) {
      null_value = new ValueNull(NULL_TYPE);
    }
   
   return null_value;
}


public static CashewValue numericValue(JcompType t,long v)
{
   ValueNumeric vn = null;
   
   if (t == INT_TYPE) {  
      Integer iv = (int) v;
      synchronized (int_values) {
         vn = int_values.get(iv);
         if (vn == null) {
            vn = new ValueNumeric(t,v);
            int_values.put(iv,vn);
          }
       }
    }
   else if (t == SHORT_TYPE) {
      Short sv = (short) v;
      synchronized (short_values) {
         vn = short_values.get(sv);
         if (vn == null) {
            vn = new ValueNumeric(t,v);
            short_values.put(sv,vn);
          }
       }
    }
   else if (t == BYTE_TYPE) {
      Byte sv = (byte) v;
      synchronized (byte_values) {
         vn = byte_values.get(sv);
         if (vn == null) {
            vn = new ValueNumeric(t,v);
            byte_values.put(sv,vn);
          }
       }
    }
   else if (t == LONG_TYPE) {
      synchronized (long_values) {
         vn = long_values.get(v);
         if (vn == null) {
            vn = new ValueNumeric(t,v);
            long_values.put(v,vn);
          }
       }
    }
   
   return vn;
}


public static CashewValue numericValue(JcompType t,double v)
{
   return new ValueNumeric(t,v);
}

public static CashewValue numericValue(JcompType t,String v)
{
   if (t == DOUBLE_TYPE || t == FLOAT_TYPE) {
      double dv = Double.parseDouble(v);
      return numericValue(t,dv);
    }
   else {
      long lv = Long.parseLong(v);
      return numericValue(t,lv);
    }
}


public static CashewValue booleanValue(boolean v)
{
   return (v ? true_value : false_value);
}

public static CashewValue booleanValue(String v)
{
   if (v == null || v.length() == 0) return booleanValue(false);
   if ("Tt1Yy".indexOf(v.charAt(0)) >= 0) return booleanValue(true);
   return booleanValue(false);
}

public static CashewValue characterValue(JcompType t,char v)
{
   synchronized (char_values) {
      ValueNumeric vn = char_values.get(v);
      if (vn == null) {
         vn = new ValueNumeric(t,v);
         char_values.put(v,vn);
       }
      return vn;
    }
}


public static CashewValue stringValue(String s)
{
   synchronized (string_values) {
      ValueString vs = string_values.get(s);
      if (vs == null) {
         vs = new ValueString(s);
         string_values.put(s,vs);
       }
      return vs;
    }
}


public static CashewValue classValue(JcompType vtyp)
{
   synchronized (class_values) {
      CashewValueObject cv = class_values.get(vtyp);
      if (cv == null) {
         cv = new CashewValueObject.ValueClass(vtyp);
         class_values.put(vtyp,cv);
       }
      return cv;
    }
}


public static CashewValue arrayValue(JcompType atyp,int dim)
{
   // might want to create multidimensional arrays here
   return new CashewValueArray.ComputedValueArray(atyp,dim,null);
}


public static CashewValue arrayValue(JcompType atyp,int dim,Map<Integer,Object> inits)
{
   return new CashewValueArray.ComputedValueArray(atyp,dim,inits);
}

public static CashewValue objectValue(JcompType otyp)
{
   return objectValue(otyp,null);
}


public static CashewValue objectValue(JcompType otyp,Map<String,Object> inits)
{
   CashewValueObject vo = new CashewValueObject.ComputedValueObject(otyp,inits);
   return vo;  
}




/********************************************************************************/
/*                                                                              */
/*      Private Static Sotrage for creation methods                             */
/*                                                                              */
/********************************************************************************/

private static ValueNull        null_value;
private static Map<Long,ValueNumeric> long_values;
private static Map<Byte,ValueNumeric> byte_values;
private static Map<Short,ValueNumeric> short_values;
private static Map<Integer,ValueNumeric> int_values;
private static Map<Character,ValueNumeric> char_values;
private static Map<String,ValueString> string_values;
private static Map<JcompType,CashewValueObject> class_values;
private static CashewValue      true_value;
private static CashewValue      false_value;



static {
   null_value = null;
   int_values = new HashMap<Integer,ValueNumeric>();
   long_values = new HashMap<Long,ValueNumeric>();
   short_values = new HashMap<Short,ValueNumeric>();
   byte_values = new HashMap<Byte,ValueNumeric>();
   char_values = new HashMap<Character,ValueNumeric>();
   string_values = new HashMap<String,ValueString>();
   class_values = new HashMap<JcompType,CashewValueObject>();
   true_value = new ValueNumeric(BOOLEAN_TYPE,1);
   false_value = new ValueNumeric(BOOLEAN_TYPE,0);
}



/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private JcompType       decl_type;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

protected CashewValue(JcompType jt)
{ 
   decl_type = jt;
}


protected CashewValue()
{
   decl_type = null; 
}

/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

public JcompType getDataType(CashewClock cc)    { return decl_type; }

protected JcompType getDataType()               
{ 
   if (decl_type == null) throw new Error("Illegal use of getDataType without clock");
   return decl_type;
}


public Number getNumber(CashewClock cc)
{
   throw new Error("Illegal value conversion");
}

public Boolean getBoolean(CashewClock cc)
{
   Number n = getNumber(cc);
   if (n == null) return null;
   
   return n.intValue() != 0;
}


public Character getChar(CashewClock cc)
{
   throw new Error("Illegal value conversion");
}

public String getString(CashewClock cc)
{
   throw new Error("Illegal value conversion");
}

public CashewValue getActualValue(CashewClock cc)
{
   return this;
}

public CashewValue getFieldValue(CashewClock cc,String name)
{
   throw new Error("Value is not an object");
}

public CashewValue setFieldValue(CashewClock cc,String name,CashewValue v)
{
   throw new Error("Value is not an object");
}


public CashewValue getIndexValue(CashewClock cc,int idx)
{
   throw new Error("Value is not an array");
}

public int getDimension(CashewClock cc)
{
   throw new Error("Value is not an array");
}

public CashewValue setIndexValue(CashewClock cc,int idx,CashewValue v)
{
   throw new Error("Value is not an array");
}


public CashewValue setValueAt(CashewClock cc,CashewValue cv)
{
   throw new Error("Not an l-value");
}



public Boolean isNull(CashewClock cc)           { return false; }

public Boolean isCategory2(CashewClock cc)      { return false; }



/********************************************************************************/
/*                                                                              */
/*      Output methods                                                          */
/*                                                                              */
/********************************************************************************/

public void outputXml(IvyXmlWriter xw)
{
   xw.begin("VALUE");
   xw.field("TYPE",getDataType());
   outputLocalXml(xw);
   xw.end("VALUE");
}


protected void outputLocalXml(IvyXmlWriter xw)  { }



/********************************************************************************/
/*                                                                              */
/*      Numeric values                                                          */
/*                                                                              */
/********************************************************************************/

private static class ValueNumeric extends CashewValue {
   
   private final Number num_value;
   
   ValueNumeric(JcompType jt,long v) {
      super(jt);
      num_value = fixValue(v);
    }
   ValueNumeric(JcompType jt,double v) {
      super(jt);
      num_value = fixValue(v);
    }
   ValueNumeric(JcompType jt,char c) {
      super(jt);
      num_value = fixValue((short) c);
    }
   
   @Override public Number getNumber(CashewClock cc) {
       return num_value;
    }
   
   @Override public Character getChar(CashewClock cc) {
      return (char) num_value.shortValue();
    }
   
   @Override public String getString(CashewClock cc) {
      return num_value.toString();
    }
   
   @Override public Boolean isCategory2(CashewClock cc) {
      if (num_value instanceof Double || num_value instanceof Long) return true;
      return false;
    }
   
   @Override public Boolean isNull(CashewClock cc) {
      throw new Error("Illegal value conversion");
    }
   
   private Number fixValue(Number v) {
       if (getDataType() == null) return v;
       switch (getDataType().getName()) {
          case "int" :
             v = v.intValue();
             break;
          case "long" :
             v = v.longValue();
             break;
          case "short" :
             v = v.shortValue();
             break;
          case "byte" :
             v = v.byteValue();
             break;
          case "char" :
             v = v.shortValue();
             break;
          case "float" :
             v = v.floatValue();
             break;
          case "double" :
             v = v.doubleValue();
             break;
          default :
             break;
        }
       return v;
    }
   
   @Override protected void outputLocalXml(IvyXmlWriter xw) {
      xw.text(num_value.toString());
    }

   @Override public String toString() {
      return "[[" + num_value + "]]";
    }
}       // end of inner class ValueNumber




/********************************************************************************/
/*                                                                              */
/*      String values                                                           */
/*                                                                              */
/********************************************************************************/

private static class ValueString extends CashewValue
{
   private final String string_value;
   
   ValueString(String s) {
      super(STRING_TYPE);
      string_value = s;
    }
   
   @Override public String getString(CashewClock cc)    { return string_value; }
   
   @Override protected void outputLocalXml(IvyXmlWriter xw) {
      xw.text(string_value.toString());
    }
   
   
}       // end of inner class ValueString








private static class ValueNull extends CashewValue
{
   ValueNull(JcompType jt) {
      super(jt);
    }
   
   @Override public Boolean isNull(CashewClock cc)      { return true; }
   
   @Override public CashewValue getFieldValue(CashewClock cc,String nm) {
      throw new Error("NullPointerException");
    }
   
   @Override public CashewValue setFieldValue(CashewClock cc,String nm,CashewValue v) {
      throw new Error("NullPointerException");
    }
   
   @Override public String getString(CashewClock cc) {
      return "null";
    }
   
   @Override protected void outputLocalXml(IvyXmlWriter xw) {
      xw.field("NULL",true);
    }
}       // end of inner class ValueNull



}       // end of class CashewValue




/* end of CashewValue.java */


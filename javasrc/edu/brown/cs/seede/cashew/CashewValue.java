/********************************************************************************/
/*										*/
/*		CashewValue.java						*/
/*										*/
/*	Holder of a typed-value.  Values are immutable wrt a CashewClock.	*/
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

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.w3c.dom.Element;

import edu.brown.cs.ivy.jcomp.JcompType;
import edu.brown.cs.ivy.jcomp.JcompTyper;
import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.ivy.xml.IvyXmlWriter;
import edu.brown.cs.seede.acorn.AcornLog;

public abstract class CashewValue implements CashewConstants
{




/********************************************************************************/
/*										*/
/*	Creation methods							*/
/*										*/
/********************************************************************************/

public static CashewValue createValue(CashewValueSession sess,JcompTyper typer,Element xml) throws CashewException
{
   String tnm = IvyXml.getAttrString(xml,"TYPE");
   JcompType jtype = typer.findType(tnm);
   if (jtype == null) throw new CashewException("Unknown type");
   if (jtype.isIntType() || jtype.isShortType() ||
	 jtype.isByteType() || jtype.isLongType()) {
      long v = IvyXml.getAttrLong(xml,"VALUE");
      return numericValue(typer,jtype,v);
    }
   else if (jtype.isBooleanType()) {
      boolean v = IvyXml.getAttrBool(xml,"VALUE");
      return booleanValue(typer,v);
    }
   else if (jtype.isCharType()) {
      String s = IvyXml.getAttrString(xml,"VALUE");
      if (s.length() == 0) throw new CashewException("Empty character constants");
      char c = s.charAt(0);
      return characterValue(jtype,c);
    }
   else if (jtype.isFloatingType()) {
      double dv = IvyXml.getAttrDouble(xml,"VALUE");
      return numericValue(jtype,dv);
    }
   else if (jtype.isStringType()) {
      String s = IvyXml.getTextElement(xml,"VALUE");
      return stringValue(typer,typer.STRING_TYPE,s);
    }
   else if (jtype.getName().equals("java.lang.Class")) {
      String s = IvyXml.getTextElement(xml,"VALUE");
      if (s == null) return nullValue(typer);
      JcompType vtyp = typer.findType(s);
      if (vtyp == null) throw new CashewException("Unknown type name " + s);
      return classValue(typer,vtyp);
    }
   else if (jtype.getName().equals("java.io.File")) {
      String path = IvyXml.getTextElement(xml,"VALUE");
      if (path == null) return nullValue(typer);
      return fileValue(typer,path);
    }
   else if (jtype.getName().endsWith("[]")) {
      int dim = IvyXml.getAttrInt(xml,"DIM");
      Map<Integer,Object> inits = new HashMap<Integer,Object>();
      //TODO: set up contents of va
      CashewValueArray va = new CashewValueArray(typer,jtype,dim,inits,true);
      return va;
    }
   else if (jtype.isPrimitiveType()) {
      throw new CashewException("Illegal type for creation");
    }
   else {
      String val = IvyXml.getTextElement(xml,"VALUE");
      if (val != null && val.equals("null")) return nullValue(typer);
      return objectValue(sess,null,typer,jtype);
    }
}



public static CashewValue createDefaultValue(JcompTyper typer,JcompType type)
{
   if (type.isNumericType()) {
      return CashewValue.numericValue(type,0);
    }
   else if (type.isBooleanType()) {
      return CashewValue.booleanValue(typer,false);
    }
   else if (type.getName().equals("char")) {
      return CashewValue.characterValue(type,(char) 0);
    }

   return CashewValue.nullValue(typer);
}

public static CashewValue createReference(CashewValue base,boolean caninit)
{
   if (base instanceof CashewRef) return base;
   
   CashewRef cr = new CashewRef(base,caninit);
   return cr;
}


public static CashewValue createReference(CashewDeferredValue cdv)
{
   CashewRef cr = new CashewRef(cdv);
   return cr;
}



/********************************************************************************/
/*										*/
/*	Internal value finders							*/
/*										*/
/********************************************************************************/

public static CashewValue nullValue(JcompTyper typer)
{
   if (null_value == null) {
      null_value = new ValueNull(typer.ANY_TYPE);
    }

   return null_value;
}


public static CashewValue numericValue(JcompTyper typer,JcompType t,long v)
{
   ValueNumeric vn = null;

   if (t.isIntType()) {
      Integer iv = (int) v;
      synchronized (int_values) {
	 vn = int_values.get(iv);
	 if (vn == null) {
	    vn = new ValueNumeric(t,v);
	    if (v > -1024 && v < 1024) int_values.put(iv,vn);
	  }
       }
    }
   else if (t.isShortType()) {
      Short sv = (short) v;
      synchronized (short_values) {
	 vn = short_values.get(sv);
	 if (vn == null) {
	    vn = new ValueNumeric(t,v);
	    if (v > -1024 && v < 1024) short_values.put(sv,vn);
	  }
       }
    }
   else if (t.isByteType()) {
      Byte sv = (byte) v;
      synchronized (byte_values) {
	 vn = byte_values.get(sv);
	 if (vn == null) {
	    vn = new ValueNumeric(t,v);
	    byte_values.put(sv,vn);
	  }
       }
    }
   else if (t.isLongType()) {
      synchronized (long_values) {
	 vn = long_values.get(v);
	 if (vn == null) {
	    vn = new ValueNumeric(t,v);
	    if (v > -1024 && v < 1024) long_values.put(v,vn);
	  }
       }
    }
   else if (t.isFloatingType()) {
      return numericValue(t,((double) v));
    }
   else if (t.isBooleanType()) {
      return booleanValue(typer,v != 0);
    }
   else if (t.isCharType()) {
      return characterValue(t,(char) v);
    }

   return vn;
}


public static CashewValue numericValue(JcompType t,double v)
{
   return new ValueNumeric(t,v);
}


public static CashewValue numericValue(JcompTyper typer,JcompType t,String v)
{
   if (t.isFloatingType()) {
      double dv = Double.parseDouble(v);
      return numericValue(t,dv);
    }
   else if (t.isCharType()) {
      char cv = 0;
      if (v.startsWith("&#x")) {
	 String hx = v.substring(3);
	 int idx = hx.indexOf(";");
	 if (idx > 0) hx = hx.substring(0,idx);
	 cv = (char)  Integer.parseInt(hx,16);
       }
      else if (v != null && v.length() > 0) cv = v.charAt(0);
      return characterValue(t,cv);
    }
   else {
      long lv = Long.parseLong(v);
      return numericValue(typer,t,lv);
    }
}


public static CashewValue booleanValue(JcompTyper typer,boolean v)
{
   if (v) {
      return new ValueNumeric(typer.BOOLEAN_TYPE,1);
    }
   else {
      return new ValueNumeric(typer.BOOLEAN_TYPE,0);
    }
}

public static CashewValue booleanValue(JcompTyper typer,String v)
{
   if (v == null || v.length() == 0) return booleanValue(typer,false);
   if ("Tt1Yy".indexOf(v.charAt(0)) >= 0) return booleanValue(typer,true);
   return booleanValue(typer,false);
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


public static CashewValue stringValue(JcompTyper typer,JcompType styp,String s)
{
   if (s == null) {
      CashewValueString vs = new CashewValueString(typer,styp,"");
      return vs;
    }

   synchronized (string_values) {
      CashewValueString vs = string_values.get(s);
      if (vs == null) {
	 vs = new CashewValueString(typer,styp,s);
	 string_values.put(s,vs);
       }
      return vs;
    }
}


public static CashewValue classValue(JcompTyper typer,JcompType vtyp)
{
   synchronized (class_values) {
      CashewValueObject cv = class_values.get(vtyp);
      if (cv == null) {
	 cv = new CashewValueClass(typer,vtyp);
	 class_values.put(vtyp,cv);
       }
      return cv;
    }
}



public static CashewValue fileValue(JcompTyper typer,String path)
{
   CashewValueFile cv = new CashewValueFile(typer,path);

   return cv;
}

public static CashewValue fileValue(JcompTyper typer)
{
   CashewValueFile cv = new CashewValueFile(typer,(File) null);
   return cv;
}


public static CashewValue arrayValue(JcompTyper typer,JcompType atyp,int dim)
{
   // might want to create multidimensional arrays here
   return new CashewValueArray(typer,atyp,dim,null,false);
}


public static CashewValue arrayValue(JcompTyper typer,JcompType atyp,int dim,
      Map<Integer,Object> inits)
{
   return new CashewValueArray(typer,atyp,dim,inits,true);
}


public static CashewValue arrayValue(JcompTyper typer,char [] arr)
{
   Map<Integer,Object> inits = new HashMap<Integer,Object>();
   for (int i = 0; i < arr.length; ++i) {
      inits.put(i,CashewValue.characterValue(typer.CHAR_TYPE,arr[i]));
    }

   JcompType jty = JcompType.createArrayType(typer.CHAR_TYPE);

   return arrayValue(typer,jty,arr.length,inits);
}


public static CashewValue arrayValue(JcompTyper typer,byte [] arr)
{
   Map<Integer,Object> inits = new HashMap<Integer,Object>();
   for (int i = 0; i < arr.length; ++i) {
      inits.put(i,CashewValue.numericValue(typer,typer.BYTE_TYPE,arr[i]));
    }

   JcompType jty = JcompType.createArrayType(typer.BYTE_TYPE);

   return arrayValue(typer,jty,arr.length,inits);
}


public static CashewValue arrayValue(JcompTyper typer,String [] arr)
{
   Map<Integer,Object> inits = new HashMap<Integer,Object>();
   for (int i = 0; i < arr.length; ++i) {
      inits.put(i,CashewValue.stringValue(typer,typer.STRING_TYPE,arr[i]));
    }

   JcompType jty = JcompType.createArrayType(typer.STRING_TYPE);

   return arrayValue(typer,jty,arr.length,inits);
}

public static CashewValue objectValue(CashewValueSession sess,CashewContext ctx,JcompTyper typer,JcompType otyp)
{
   return objectValue(sess,ctx,typer,otyp,null,false);
}


public static CashewValue objectValue(CashewValueSession sess,CashewContext ctx,
      JcompTyper typer,JcompType otyp,Map<String,Object> inits,boolean caninit)
{
   if (otyp.isParameterizedType()) otyp = otyp.getBaseType();

   if (otyp.getName().equals("java.io.File")) {
      CashewValueFile cf = new CashewValueFile(sess,ctx,typer,otyp,inits,caninit);
      return cf;
    }

   CashewValueObject vo = new CashewValueObject(typer,otyp,inits,caninit);
   return vo;
}



/********************************************************************************/
/*										*/
/*	Private Static Sotrage for creation methods				*/
/*										*/
/********************************************************************************/

private static ValueNull	null_value;
private static Map<Long,ValueNumeric> long_values;
private static Map<Byte,ValueNumeric> byte_values;
private static Map<Short,ValueNumeric> short_values;
private static Map<Integer,ValueNumeric> int_values;
private static Map<Character,ValueNumeric> char_values;
private static Map<String,CashewValueString> string_values;
private static Map<JcompType,CashewValueObject> class_values;



static {
   null_value = null;
   int_values = new HashMap<>();
   long_values = new HashMap<>();
   short_values = new HashMap<>();
   byte_values = new HashMap<>();
   char_values = new HashMap<>();
   string_values = new HashMap<>();
   class_values = new WeakHashMap<>();
   // true_value = new ValueNumeric(BOOLEAN_TYPE,1);
   // false_value = new ValueNumeric(BOOLEAN_TYPE,0);
}



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private JcompType	decl_type;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

protected CashewValue(JcompType jt)
{
   if (jt == null) AcornLog.logX("Creating a value without a type");

   decl_type = jt;
}


protected CashewValue() {
   decl_type = null;
}

/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

public JcompType getDataType(CashewValueSession sess,CashewClock cc,JcompTyper typer)	
{ 
   // null --> only the name is relevant
   
   if (typer == null) return decl_type;
   
   return decl_type.resetType(typer); 
}

protected JcompType getDataType(JcompTyper typer)
{
   if (decl_type == null) 
      AcornLog.logX("Illegal use of getDataType without clock");

   // null --> only the name is relevant
   
   if (typer == null) return decl_type;
   
   return decl_type.resetType(typer);
}



public Number getNumber(CashewValueSession sess,CashewClock cc) throws CashewException
{
   throw new CashewException("Illegal value conversion: " + this);
}


public Boolean getBoolean(CashewValueSession sess,CashewClock cc) throws CashewException
{
   Number n = getNumber(sess,cc);
   if (n == null) throw new CashewException("Bad boolean value");

   return n.intValue() != 0;
}


public Character getChar(CashewValueSession sess,CashewClock cc) throws CashewException
{
   throw new CashewException("Illegal value conversion");
}

public final String getString(CashewValueSession sess,
      JcompTyper typer,CashewClock cc) throws CashewException
{
   return getString(sess,typer,cc,4,false);
}


public String getDebugString(CashewValueSession sess,JcompTyper typer,CashewClock cc)
{
   try {
      return getString(sess,typer,cc,2,true);
    }
   catch (CashewException e) {
      return "<<< Illegal Value >>>";
    }
}

public String getString(CashewValueSession sess,
      JcompTyper typer,CashewClock cc,int lvl,boolean debug) throws CashewException
{
   throw new CashewException("Illegal value conversion");
}

public CashewValue getActualValue(CashewValueSession sess,CashewClock cc)
{
   if (decl_type == null) return null;

   return this;
}

public CashewValue getFieldValue(CashewValueSession sess,
      JcompTyper typer,CashewClock cc,String name,CashewContext ctx)
	throws CashewException
{
   return getFieldValue(sess,typer,cc,name,ctx,true);
}


public CashewValue getFieldValue(CashewValueSession sess,JcompTyper typer,
      CashewClock cc,String name,CashewContext ctx,boolean force) throws CashewException
{
   throw new CashewException("Value is not an object");
}

public CashewValue setFieldValue(CashewValueSession sess,JcompTyper typer,
      CashewClock cc,String name,CashewValue v) throws CashewException
{
   throw new CashewException("Value is not an object");
}


public CashewValue addFieldValue(CashewValueSession sess,JcompTyper typer,
      CashewClock cc,String name,CashewValue v)
{ 
   return this;
}


public CashewValue getIndexValue(CashewValueSession sess,CashewClock cc,int idx) throws CashewException
{
   throw new CashewException("Value is not an array");
}

public int getDimension(CashewValueSession sess,CashewClock cc) throws CashewException
{
   throw new CashewException("Value is not an array");
}

public CashewValue setIndexValue(CashewValueSession sess,CashewClock cc,int idx,CashewValue v) throws CashewException
{
   throw new CashewException("Value is not an array");
}


public CashewValue setValueAt(CashewValueSession sess,CashewClock cc,CashewValue cv) throws CashewException
{
   throw new CashewException("Not an l-value");
}



public boolean isNull(CashewValueSession sess,CashewClock cc)		
{
   return false;
}

public boolean isEmpty(CashewValueSession sess)         { return false; }


public boolean isCategory2(CashewValueSession sess,CashewClock cc)	
{ 
   return false;
}

public boolean isFunctionRef(CashewValueSession sess,CashewClock cc)	
{ 
   return false;
}




/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

public String getInternalRepresentation(CashewValueSession sess,CashewClock cc)
{
   return null;
}



public int getHashCode(CashewClock cc,CashewContext ctx)
{
   return 0;
}


public Map<Object,CashewValue> getBindings()	{ return null; }

CashewValue lookupVariableName(CashewValueSession sess,CashewContext ctx,
      JcompTyper typer,String name,long when) throws CashewException
{
   if (name == null || name.length() == 0) return this;

   CashewValue val = this;
   int idx = name.indexOf("?");
   String rest = null;
   if (idx > 0) {
      rest = name.substring(idx+1);
      name = name.substring(0,idx);
    }
   if (getDataType(typer).isArrayType()) {
      int index = Integer.parseInt(name);
      val = val.getIndexValue(sess,null,index);
    }
   else {
      val = val.getFieldValue(sess,typer,null,name,ctx);
    }
   if (rest != null) val = val.lookupVariableName(sess,ctx,typer,rest,when);

   return val;
}


boolean sameValue(CashewValueSession sess,CashewValue cv)
{
   return cv == this;
}



/********************************************************************************/
/*										*/
/*	Reset methods								*/
/*										*/
/********************************************************************************/

public void resetValues(CashewValueSession sess,Set<CashewValue> done)
{
   if (done.contains(this)) return;
   done.add(this);
   localResetValue(sess,done);
}


protected void localResetValue(CashewValueSession sess,Set<CashewValue> done)   { }



public void resetType(CashewValueSession sess,JcompTyper typer,Set<CashewValue> done)
{
   if (done.contains(this)) return;
   done.add(this);
   if (decl_type != null) {
      JcompType ntyp = decl_type.resetType(typer);
      if (ntyp != decl_type) {
	 decl_type = ntyp;
       }
    }
   localResetType(sess,typer,done);
}



protected void localResetType(CashewValueSession sess,JcompTyper typer,Set<CashewValue> done)		 { }



/********************************************************************************/
/*										*/
/*	Create a copy of the object from a given time				*/
/*										*/
/********************************************************************************/

void getChangeTimes(Set<Long> times,Set<CashewValue> done)		{ }




/********************************************************************************/
/*										*/
/*	Output methods								*/
/*										*/
/********************************************************************************/

public boolean checkChanged(CashewOutputContext outctx)
{
   return false;
}


public void checkToString(CashewValueSession sess,CashewOutputContext outctx)
{ }


public void checkToArray(CashewValueSession sess,CashewOutputContext outctx)
{ }


public String toString(CashewValueSession sess) 
{
   return toString();
}


public void outputXml(CashewOutputContext ctx,String name)
{
   IvyXmlWriter xw = ctx.getXmlWriter();
   xw.begin("VALUE");
   xw.field("TYPE",getDataType(ctx.getTyper()));
   outputLocalXml(xw,ctx,name);
   xw.end("VALUE");
}


protected void outputLocalXml(IvyXmlWriter xw,CashewOutputContext outctx,String name)  { }



/********************************************************************************/
/*										*/
/*	Numeric values								*/
/*										*/
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

   @Override public Number getNumber(CashewValueSession s,CashewClock cc) {
       return num_value;
    }

   @Override public Character getChar(CashewValueSession s,CashewClock cc) {
      return (char) num_value.shortValue();
    }

   @Override public String getString(CashewValueSession sess,
         JcompTyper typer,CashewClock cc,int idx,boolean dbg) {
      return num_value.toString();
    }

   @Override public boolean isCategory2(CashewValueSession s,CashewClock cc) {
      if (num_value instanceof Double || num_value instanceof Long) return true;
      return false;
    }

   @Override public String getInternalRepresentation(CashewValueSession s,CashewClock cc) {
      if (getDataType(null) == null) return num_value.toString();
      else if (getDataType(null).isBooleanType()) {
         if (num_value.intValue() != 0) return "true";
         else return "false";
       }
      return "((" + getDataType(null).getName() + ") " + num_value.toString() + ")";
    }
 
   private Number fixValue(Number v) {
       if (getDataType(null) == null) return v;
       switch (getDataType(null).getName()) {
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
             v = v.shortValue() & 0xffff;
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

   @Override protected void outputLocalXml(IvyXmlWriter xw,CashewOutputContext outctx,String name) {
      xw.text(num_value.toString());
    }

   @Override public String toString() {
      return "[[" + num_value + "]]";
    }
}	// end of inner class ValueNumber





private static class ValueNull extends CashewValue
{
   ValueNull(JcompType jt) {
      super(jt);
    }

   @Override public boolean isNull(CashewValueSession s,CashewClock cc)	{ 
      return true;
    }

   @Override public CashewValue getFieldValue(CashewValueSession sess,
         JcompTyper typer,CashewClock cc,String nm,CashewContext ctx,boolean force) {
      throw new NullPointerException();
    }

   @Override public CashewValue setFieldValue(CashewValueSession sess,
         JcompTyper typer,CashewClock cc,String nm,CashewValue v) {
      throw new NullPointerException();
    }

   @Override public String getString(CashewValueSession sess,
         JcompTyper typer,CashewClock cc,int idx,boolean dbg) {
      return "null";
    }

   @Override public String getInternalRepresentation(CashewValueSession sess,CashewClock cc) {
      return "null";
    }

   @Override protected void outputLocalXml(IvyXmlWriter xw,CashewOutputContext outctx,String name) {
      xw.field("NULL",true);
    }

}	// end of inner class ValueNull 













}	// end of class CashewValue




/* end of CashewValue.java */

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

public static CashewValue createValue(JcompTyper typer,Element xml) throws CashewException
{
   String tnm = IvyXml.getAttrString(xml,"TYPE");
   JcompType jtype = typer.findType(tnm);
   if (jtype == null) throw new CashewException("Unknown type");
   if (jtype.isIntType() || jtype.isShortType() ||
	 jtype.isByteType() || jtype.isLongType()) {
      long v = IvyXml.getAttrLong(xml,"VALUE");
      return numericValue(jtype,v);
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
      return stringValue(typer.STRING_TYPE,s);
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
      return objectValue(typer,jtype);
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
	    int_values.put(iv,vn);
	  }
       }
    }
   else if (t.isShortType()) {
      Short sv = (short) v;
      synchronized (short_values) {
	 vn = short_values.get(sv);
	 if (vn == null) {
	    vn = new ValueNumeric(t,v);
	    short_values.put(sv,vn);
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
	    long_values.put(v,vn);
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


public static CashewValue numericValue(JcompType t,String v)
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
      return numericValue(t,lv);
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


public static CashewValue stringValue(JcompType styp,String s)
{
   if (s == null) {
      CashewValueString vs = new CashewValueString(styp,"");
      return vs;
    }

   synchronized (string_values) {
      CashewValueString vs = string_values.get(s);
      if (vs == null) {
	 vs = new CashewValueString(styp,s);
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


public static CashewValue arrayValue(JcompTyper typer,JcompType atyp,int dim,Map<Integer,Object> inits)
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
      inits.put(i,CashewValue.numericValue(typer.BYTE_TYPE,arr[i]));
    }

   JcompType jty = JcompType.createArrayType(typer.BYTE_TYPE);

   return arrayValue(typer,jty,arr.length,inits);
}


public static CashewValue arrayValue(JcompTyper typer,String [] arr)
{
   Map<Integer,Object> inits = new HashMap<Integer,Object>();
   for (int i = 0; i < arr.length; ++i) {
      inits.put(i,CashewValue.numericValue(typer.STRING_TYPE,arr[i]));
    }

   JcompType jty = JcompType.createArrayType(typer.STRING_TYPE);

   return arrayValue(typer,jty,arr.length,inits);
}

public static CashewValue objectValue(JcompTyper typer,JcompType otyp)
{
   return objectValue(typer,otyp,null,false);
}


public static CashewValue objectValue(JcompTyper typer,JcompType otyp,Map<String,Object> inits,boolean caninit)
{
   if (otyp.isParameterizedType()) otyp = otyp.getBaseType();
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
   int_values = new HashMap<Integer,ValueNumeric>();
   long_values = new HashMap<Long,ValueNumeric>();
   short_values = new HashMap<Short,ValueNumeric>();
   byte_values = new HashMap<Byte,ValueNumeric>();
   char_values = new HashMap<Character,ValueNumeric>();
   string_values = new HashMap<String,CashewValueString>();
   class_values = new HashMap<JcompType,CashewValueObject>();
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


protected CashewValue()
{
   decl_type = null;
}

/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

public JcompType getDataType(CashewClock cc)	{ return decl_type; }

protected JcompType getDataType()
{
   if (decl_type == null) AcornLog.logX("Illegal use of getDataType without clock");

   return decl_type;
}



public Number getNumber(CashewClock cc) throws CashewException
{
   throw new CashewException("Illegal value conversion: " + this);
}


public Boolean getBoolean(CashewClock cc) throws CashewException
{
   Number n = getNumber(cc);
   if (n == null) throw new CashewException("Bad boolean value");

   return n.intValue() != 0;
}


public Character getChar(CashewClock cc) throws CashewException
{
   throw new CashewException("Illegal value conversion");
}

public final String getString(JcompTyper typer,CashewClock cc) throws CashewException
{
   return getString(typer,cc,4,false);
}


public String getDebugString(JcompTyper typer,CashewClock cc) 
{
   try {
      return getString(typer,cc,2,true);
    }
   catch (CashewException e) {
      return "<<< Illegal Value >>>";
    }
}

public String getString(JcompTyper typer,CashewClock cc,int lvl,boolean debug) throws CashewException
{
   throw new CashewException("Illegal value conversion");
}

public CashewValue getActualValue(CashewClock cc)
{
   if (decl_type == null) return null;

   return this;
}

public CashewValue getFieldValue(JcompTyper typer,CashewClock cc,String name)
        throws CashewException
{
   return getFieldValue(typer,cc,name,true);
}


public CashewValue getFieldValue(JcompTyper typer,CashewClock cc,String name,boolean force) throws CashewException
{
   throw new CashewException("Value is not an object");
}

public CashewValue setFieldValue(JcompTyper typer,CashewClock cc,String name,CashewValue v) throws CashewException
{
   throw new CashewException("Value is not an object");
}


public CashewValue getIndexValue(CashewClock cc,int idx) throws CashewException
{
   throw new CashewException("Value is not an array");
}

public int getDimension(CashewClock cc) throws CashewException
{
   throw new CashewException("Value is not an array");
}

public CashewValue setIndexValue(CashewClock cc,int idx,CashewValue v) throws CashewException
{
   throw new CashewException("Value is not an array");
}


public CashewValue setValueAt(CashewClock cc,CashewValue cv) throws CashewException
{
   throw new CashewException("Not an l-value");
}



public boolean isNull(CashewClock cc)		{ return false; }

public boolean isEmpty()			{ return false; }


public boolean isCategory2(CashewClock cc)	{ return false; }

public boolean isFunctionRef(CashewClock cc)	{ return false; }




/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

public String getInternalRepresentation(CashewClock cc)
{
   return null;
}



public int getHashCode(CashewClock cc,CashewContext ctx)
{
   return 0;
}


public Map<Object,CashewValue> getBindings()	{ return null; }

CashewValue lookupVariableName(JcompTyper typer,String name,long when) throws CashewException
{
   if (name == null || name.length() == 0) return this;

   CashewValue val = this;
   int idx = name.indexOf("?");
   String rest = null;
   if (idx > 0) {
      rest = name.substring(idx+1);
      name = name.substring(0,idx);
    }
   if (getDataType().isArrayType()) {
      int index = Integer.parseInt(name);
      val = val.getIndexValue(null,index);
    }
   else {
      val = val.getFieldValue(typer,null,name);
    }
   if (rest != null) val = val.lookupVariableName(typer,rest,when);

   return val;
}


boolean sameValue(CashewValue cv)
{
   return cv == this;
}



/********************************************************************************/
/*										*/
/*	Reset methods								*/
/*										*/
/********************************************************************************/

public void resetValues(Set<CashewValue> done)
{
   if (done.contains(this)) return;
   done.add(this);
   localResetValue(done);
}


protected void localResetValue(Set<CashewValue> done)		{ }



public void resetType(JcompTyper typer,Set<CashewValue> done)
{
   if (done.contains(this)) return;
   done.add(this);
   if (decl_type != null) {
      JcompType ntyp = decl_type.resetType(typer);
      if (ntyp != decl_type) {
	 decl_type = ntyp;
       }
    }
   localResetType(typer,done);
}



protected void localResetType(JcompTyper typer,Set<CashewValue> done)		 { }



/********************************************************************************/
/*                                                                              */
/*      Create a copy of the object from a given time                           */
/*                                                                              */
/********************************************************************************/

void getChangeTimes(Set<Long> times,Set<CashewValue> done)              { }




/********************************************************************************/
/*										*/
/*	Output methods								*/
/*										*/
/********************************************************************************/

public boolean checkChanged(CashewOutputContext outctx)
{
   return false;
}


public void checkToString(CashewOutputContext outctx)
{ }



public void outputXml(CashewOutputContext ctx,String name)
{
   IvyXmlWriter xw = ctx.getXmlWriter();
   xw.begin("VALUE");
   xw.field("TYPE",getDataType());
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

   @Override public Number getNumber(CashewClock cc) {
       return num_value;
    }

   @Override public Character getChar(CashewClock cc) {
      return (char) num_value.shortValue();
    }

   @Override public String getString(JcompTyper typer,CashewClock cc,int idx,boolean dbg) {
      return num_value.toString();
    }

   @Override public boolean isCategory2(CashewClock cc) {
      if (num_value instanceof Double || num_value instanceof Long) return true;
      return false;
    }

   @Override public boolean isNull(CashewClock cc) {
      return false;
    }

   @Override public String getInternalRepresentation(CashewClock cc) {
      if (getDataType() == null) return num_value.toString();
      else if (getDataType().isBooleanType()) {
	 if (num_value.intValue() != 0) return "true";
	 else return "false";
       }
      return "((" + getDataType().getName() + ") " + num_value.toString() + ")";
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

   @Override public boolean isNull(CashewClock cc)	{ return true; }

   @Override public CashewValue getFieldValue(JcompTyper typer,CashewClock cc,String nm,boolean force) {
      throw new NullPointerException();
    }

   @Override public CashewValue setFieldValue(JcompTyper typer,CashewClock cc,String nm,CashewValue v) {
      throw new NullPointerException();
    }

   @Override public String getString(JcompTyper typer,CashewClock cc,int idx,boolean dbg) {
      return "null";
    }

   @Override public String getInternalRepresentation(CashewClock cc) {
      return "null";
    }

   @Override protected void outputLocalXml(IvyXmlWriter xw,CashewOutputContext outctx,String name) {
      xw.field("NULL",true);
    }

}	// end of inner class ValueNull




























}	// end of class CashewValue




/* end of CashewValue.java */

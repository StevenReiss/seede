/********************************************************************************/
/*                                                                              */
/*              CashewValue.java                                                */
/*                                                                              */
/*      Holder of a typed-value.  Values are immutable.                         */
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

public abstract class CashewValue implements CashewConstants
{




/********************************************************************************/
/*                                                                              */
/*      Creation methods                                                        */
/*                                                                              */
/********************************************************************************/

public static CashewValue createValue(JcompTyper typer,Element xml) 
{
   return null;
}


public static CashewValue createRemoteValue(JcompTyper typer,Element xml)
{
   return null;
}


private static CashewValue nullValue(JcompTyper typer)
{
   if (null_value == null) {
      JcompType t = typer.findType("*ANY*");
      null_value = new ValueNull(t);
    }
   
   return null_value;
}




/********************************************************************************/
/*                                                                              */
/*      Private Static Sotrage for creation methods                             */
/*                                                                              */
/********************************************************************************/

private static ValueNull        null_value;



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


/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

public JcompType getDataType()                  { return decl_type; }

abstract public CashewValueKind getKind();

public Number getNumber()
{
   throw new Error("Illegal value conversion");
}

public char getChar()
{
   throw new Error("Illegal value conversion");
}

public String getString()
{
   throw new Error("Illegal value conversion");
}

public CashewValue getFieldValue(String name)
{
   throw new Error("Value is not an object");
}

public CashewValue setFieldValue(String name,CashewValue v)
{
   throw new Error("Value is not an object");
}


public CashewValue getIndexValue(int idx)
{
   throw new Error("Value is not an array");
}

public CashewValue setIndexValue(int idx,CashewValue v)
{
   throw new Error("Value is not an array");
}


public boolean isNull()                         { return false; }

public boolean isCategory2()                    { return false; }



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
   
   @Override public CashewValueKind getKind()   { return CashewValueKind.PRIMITIVE; }
   
   @Override public Number getNumber() {
       return num_value;
    }
   
   @Override public char getChar() {
      return (char) num_value.shortValue();
    }
   
   @Override public String getString() {
      return num_value.toString();
    }
   
   @Override public boolean isCategory2() {
      if (num_value instanceof Double || num_value instanceof Long) return true;
      return false;
    }
   
   @Override public boolean isNull() {
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
   
}       // end of inner class ValueNumber




/********************************************************************************/
/*                                                                              */
/*      String values                                                           */
/*                                                                              */
/********************************************************************************/

private static class ValueString extends CashewValue
{
   private final String string_value;
   
   ValueString(JcompType t,String s) {
      super(t);
      string_value = s;
    }
   
   @Override public CashewValueKind getKind()   { return CashewValueKind.STRING; }
   
   @Override public String getString()          { return string_value; }
   
}       // end of inner class ValueString




/********************************************************************************/
/*                                                                              */
/*      Object values                                                           */
/*                                                                              */
/********************************************************************************/

private static class ValueObject extends CashewValue
{
   private final Map<String,CashewValue> field_values;
   
   ValueObject(JcompType jt) {
      super(jt);
      field_values = new HashMap<String,CashewValue>();
      // want to iterate over fields and set default values
    }
   
   private ValueObject(ValueObject base,String fld,CashewValue val) {
      super(base.getDataType());
      field_values = new HashMap<String,CashewValue>(base.field_values);
      field_values.put(fld,val);
    }
   
   @Override public CashewValueKind getKind()   { return CashewValueKind.OBJECT; }
   
   @Override public CashewValue getFieldValue(String nm) {
      CashewValue cv = field_values.get(nm);
      if (cv == null) {
          throw new Error("UndefinedField");
       }
      return cv;
    }
   
   @Override public CashewValue setFieldValue(String nm,CashewValue cv) {
      CashewValue ov = field_values.get(nm);
      if (ov == null) {
         throw new Error("UndefinedField");
       }
      return new ValueObject(this,nm,cv);
    }
   
   @Override public String getString() {
      StringBuffer buf = new StringBuffer();
      buf.append("{");
      int ctr = 0;
      for (Map.Entry<String,CashewValue> ent : field_values.entrySet()) {
         if (ctr++ == 0) buf.append(",");
         buf.append(ent.getKey());
         buf.append(":");
         buf.append(ent.getValue().getString());
       }
      buf.append("}");
      return buf.toString();
    }
   
}       // end of inner class ValueObject



private static class ValueNull extends CashewValue
{
   ValueNull(JcompType jt) {
      super(jt);
    }
   
   @Override public CashewValueKind getKind()   { return CashewValueKind.OBJECT; }   
   
   @Override public boolean isNull()            { return true; }
   
   @Override public CashewValue getFieldValue(String nm) {
      throw new Error("NullPointerException");
    }
   
   @Override public CashewValue setFieldValue(String nm,CashewValue v) {
      throw new Error("NullPointerException");
    }
   
   @Override public String toString() {
      return "null";
    }
   
}       // end of inner class ValueNull



private static class ValueClass extends CashewValue 
{
   private Class<?>     class_value;
   
   ValueClass(JcompType jt,Class<?> c) {
      super(jt);
      class_value = c;
    }
   
   @Override public CashewValueKind getKind()   { return CashewValueKind.CLASS; }
   
   @Override public String getString() {
      return class_value.toString();
    }
   
}       // end of inner class ValueClass




/********************************************************************************/
/*                                                                              */
/*      Array Values                                                            */
/*                                                                              */
/********************************************************************************/

private static class ValueArray extends CashewValue {
   
   private int dim_size;
   private CashewValue[] array_values;
   
   ValueArray(JcompType jt,int dim) {
      super(jt);
      dim_size = dim;
      array_values = new CashewValue[dim];
      // initialize array_values with default value for base type
    }
   
   private ValueArray(ValueArray base,int idx,CashewValue cv) {
      super(base.getDataType());
      dim_size = base.dim_size;
      array_values = new CashewValue[dim_size];
      System.arraycopy(base.array_values,0,array_values,0,dim_size);
      array_values[idx] = cv;
    }
  
   @Override public CashewValueKind getKind()   { return CashewValueKind.ARRAY; }   
   
   @Override public CashewValue getFieldValue(String nm) {
      if (nm == "length") {
         // return new ValueNumber(int_type,dim_size);
         return null;
       }
      throw new Error("Illegal Value Conversion");
    }
   
   @Override public CashewValue getIndexValue(int idx) {
      if (idx < 0 || idx >= dim_size) throw new Error("IndexOutOfBounds");
      return array_values[idx];
    }
   
   @Override public CashewValue setIndexValue(int idx,CashewValue v) {
      if (idx < 0 || idx >= dim_size) throw new Error("IndexOutOfBounds");
      return new ValueArray(this,idx,v);
    }
   
   @Override public String getString() {
      StringBuffer buf = new StringBuffer();
      buf.append("[");
      for (int i = 0; i < dim_size; ++i) {
         if (i != 0) buf.append(",");
         buf.append(array_values[i].toString());
       }
      buf.append("]");
      return buf.toString();
    }
   
}       // end of inner class ValueArray

   


}       // end of class CashewValue




/* end of CashewValue.java */


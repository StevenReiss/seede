/********************************************************************************/
/*										*/
/*		PoppyValue.java 						*/
/*										*/
/*	Provide named access to run time values 				*/
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



package edu.brown.cs.seede.poppy;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.nio.channels.FileChannel;


public class PoppyValue implements PoppyConstants
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private static Map<Integer,Return>	value_map;
private static Map<Object,Return>	id_map;
private static AtomicInteger		id_counter;


static {
   value_map = new HashMap<Integer,Return>();
   id_map = new HashMap<Object,Return>();
   id_counter = new AtomicInteger();
}


/********************************************************************************/
/*										*/
/*	Registration methods							*/
/*										*/
/********************************************************************************/

public static byte register(byte v)
{
   return v;
}


public static char register(char v)
{
   return v;
}


public static short register(short v)
{
   return v;
}


public static int register(int v)
{
   return v;
}


public static long register(long v)
{
   return v;
}


public static float register(float v)
{
   return v;
}


public static double register(double v)
{
   return v;
}


public static boolean register(boolean v)
{
   return v;
}



public static Object register(Object v)
{
   if (v == null) return v;
   Return r = id_map.get(v);
   if (r != null) return r;

   r = new Return(v);
   value_map.put(r.ref_id,r);
   id_map.put(v,r);

   return r;
}



public static void unregister(int id)
{
   if (id == 0) return;

   Return r = value_map.remove(id);
   if (r != null) id_map.remove(r.for_object);
}




/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

public static Object getValue(int id)
{
   if (id == 0) return null;
   return value_map.get(id);
}


/********************************************************************************/
/*										*/
/*	Field access methods							*/
/*										*/
/********************************************************************************/

public static Object getStaticFieldValue(String itm)
{
   int idx1 = itm.lastIndexOf(".");
   String fld = itm.substring(idx1+1);
   String cls = itm.substring(0,idx1);

   Class<?> c1 = null;
   for ( ; ; ) {
      try {
	 c1 = Class.forName(cls);
	 break;
       }
      catch (ClassNotFoundException e) { }
      int idx = cls.lastIndexOf(".");
      if (idx < 0) return null;
      cls = cls.substring(0,idx) + "$" + cls.substring(idx+1);
    }
   try {
      Field f1 = c1.getDeclaredField(fld);
      f1.setAccessible(true);
      return f1.get(null);
    }
   catch (Throwable t) { }
   return null;
}


public static boolean getStaticFieldValueBoolean(String itm)
{
   Boolean bv = (Boolean) getStaticFieldValue(itm);
   return bv.booleanValue();
}



public static int getStaticFieldValueInt(String itm)
{
   Number nv = (Number) getStaticFieldValue(itm);
   return nv.intValue();
}


public static long getStaticFieldValueLong(String itm)
{
   Number nv = (Number) getStaticFieldValue(itm);
   return nv.longValue();
}


public static short getStaticFieldValueShort(String itm)
{
   Number nv = (Number) getStaticFieldValue(itm);
   return nv.shortValue();
}


public static double getStaticFieldValueDouble(String itm)
{
   Number nv = (Number) getStaticFieldValue(itm);
   return nv.doubleValue();
}


public static float getStaticFieldValueFloat(String itm)
{
   Number nv = (Number) getStaticFieldValue(itm);
   return nv.floatValue();
}



/********************************************************************************/
/*                                                                              */
/*      File calls                                                              */
/*                                                                              */
/********************************************************************************/

public static String getFileData(FileInputStream fis)
{
   FileChannel fc = fis.getChannel();
   if (!fc.isOpen()) return "*";
   try {
      long pos = fc.position();
      return "@" + pos;
    }
   catch (IOException e) {
    }
   
   return null;
}



/********************************************************************************/
/*                                                                              */
/*      toString handling                                                       */
/*                                                                              */
/********************************************************************************/

public static String getToString(Object o)
{
   if (o == null) return "null";
   
   String rslt = o.toString();
   
   return rslt;
}




/********************************************************************************/
/*										*/
/*	Value to return with additional information				*/
/*										*/
/********************************************************************************/

public static class Return {

   public Object for_object;
   public int ref_id;
   public int hash_code;

   Return(Object o) {
      for_object = o;
      ref_id = id_counter.incrementAndGet();
      hash_code = System.identityHashCode(o);
    }

}	// end of inner class Return




/********************************************************************************/
/*                                                                              */
/*      Other helper routines                                                   */
/*                                                                              */
/********************************************************************************/

public static ClassLoader getClassLoaderUsingPoppy(String cls)
{
   try {
      Class<?> cl = Class.forName(cls);
      return cl.getClassLoader();
    }
   catch (ClassNotFoundException e) { }
   return null;
}


public static Object getNewInstance(String name)
{
   try {
      Class<?> c = Class.forName(name);
      return c.newInstance();
    }
   catch (Throwable t) {
      return null;
    }
}


/********************************************************************************/
/*                                                                              */
/*      Handle invoke dynamic                                                   */
/*                                                                              */
/********************************************************************************/

public static Object invokeLambdaMetaFactory(String clsnam,
      String name,String desc,String sam,String mts1,String mts2)
{
   ClassLoader loader = PoppyValue.class.getClassLoader();
   try {
      Class<?> cl = Class.forName(clsnam);
      loader = cl.getClassLoader();
    }
   catch (ClassNotFoundException e) { }
   
   try {
      MethodHandles.Lookup lookup = MethodHandles.lookup();
      MethodType mtype = MethodType.fromMethodDescriptorString(desc,loader);
      MethodType samtype = MethodType.fromMethodDescriptorString(sam,loader);
      int idx = mts1.indexOf("(");
      MethodType mt1 = MethodType.fromMethodDescriptorString(mts1.substring(idx),loader);
      String mn1 = mts1.substring(0,idx);
      idx = mn1.indexOf(".");
      String clsnm1 = mn1.substring(0,idx);
      String mth1 = mn1.substring(idx+1);
      Class<?> cls1 = Class.forName(clsnm1);
      MethodHandle mh = lookup.findStatic(cls1,mth1,mt1);
      MethodType mt2 = MethodType.fromMethodDescriptorString(mts2,loader);
      CallSite cs = LambdaMetafactory.metafactory(lookup,name,mtype,samtype,mh,mt2);
      Object rslt = cs.getTarget().invoke();
      return rslt;
    }
   catch (Throwable t) {
      System.err.println("PROBLEM WITH INVOKE META: " + t);
      return t.toString();
    }
}




}	// end of class PoppyValue




/* end of PoppyValue.java */


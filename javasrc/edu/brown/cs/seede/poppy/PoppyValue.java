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

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.channels.FileChannel;
import java.text.NumberFormat;


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

   Class<?> c1 = getClassByName(cls);
   if (c1 == null) return null;

   Throwable err = null;

   // handle special cases
   switch (fld) {
      case "ENUM$VALUES" :
	 return c1.getEnumConstants();
    }

   try {
      Field f1 = c1.getDeclaredField(fld);
      try {
	 f1.setAccessible(true);
       }
      catch (Throwable t) {
         err = t;
      }

      return f1.get(null);
    }
   catch (Throwable t) {
      if (err == null) err = t;
    }

   System.err.println("POPPY: Problem getting static field: " + c1.getModule().getName() +
			 "/" +  c1.getName() + " " + fld);
   System.err.println("POPPY: Error: " + err);
   System.err.println("POPPY: Add '" + c1.getModule().getName() + "/" + c1.getName() + "' to Bicex.props");

   return null;
}


private static Class<?> getClassByName(String cls)
{
   String itm = cls;
   Class<?> c1 = null;

   for ( ; ; ) {
      try {
	 c1 = Class.forName(cls);
	 break;
       }
      catch (ClassNotFoundException e) { }
//    System.err.println("POPPY: Can't find class " + cls);
      int idx = cls.lastIndexOf(".");
      if (idx < 0) {
	 System.err.println("POPPY: Problem getting class: " + itm);
	 return null;
       }
      cls = cls.substring(0,idx) + "$" + cls.substring(idx+1);
    }

   return c1;
}


public static boolean getStaticFieldValueBoolean(String itm)
{
   Boolean bv = (Boolean) getStaticFieldValue(itm);
   if (bv == null) return false;
   return bv.booleanValue();
}



public static int getStaticFieldValueInt(String itm)
{
   Number nv = (Number) getStaticFieldValue(itm);
   if (nv == null) return 0;
   return nv.intValue();
}


public static long getStaticFieldValueLong(String itm)
{
   Number nv = (Number) getStaticFieldValue(itm);
   if (nv == null) return 0;
   return nv.longValue();
}


public static short getStaticFieldValueShort(String itm)
{
   Number nv = (Number) getStaticFieldValue(itm);
   if (nv == null) return 0;
   return nv.shortValue();
}


public static double getStaticFieldValueDouble(String itm)
{
   Number nv = (Number) getStaticFieldValue(itm);
   if (nv == null) return 0;
   return nv.doubleValue();
}


public static float getStaticFieldValueFloat(String itm)
{
   Number nv = (Number) getStaticFieldValue(itm);
   if (nv == null) return 0;
   return nv.floatValue();
}



/********************************************************************************/
/*										*/
/*	File calls								*/
/*										*/
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
/*										*/
/*	toString handling							*/
/*										*/
/********************************************************************************/

public static String getToString(Object o)
{
   if (o == null) return "null";

   String rslt = o.toString();

   return rslt;
}


public static Object [] getToArray(Object o)
{
   if (o instanceof Collection) {
      Collection<?> c = (Collection<?>) o;
      return c.toArray();
    }
   else if (o instanceof Map) {
      Map<?,?> m = (Map<?,?>) o;
      int sz = m.size();
      Object [][] rslt = new Object[sz][2];
      int ct = 0;
      for (Map.Entry<?,?> ent : m.entrySet()) {
	 rslt[ct][0] = ent.getKey();
	 rslt[ct][1] = ent.getValue();
	 ++ct;
       }
      return rslt;
    }
   return null;
}



public static void setAccessible(String cls)
{
   Class<?> c1 = getClassByName(cls);
   if (c1 == null) return;
   for (Field f : c1.getDeclaredFields()) {
      try {
	 f.trySetAccessible();
       }
      catch (Throwable t) { }
    }
}


public static Object getInteger(int v)
{
   return Integer.valueOf(v);
}



public static Object getDefaultModule()
{
   return PoppyValue.class.getModule();
}


public static Object getClassModule(String nm)
{
   if (nm != null) {
      try {
	 Class<?> c = Class.forName(nm);
	 return c.getModule();
       }
      catch (Throwable t) { }
    }
   return getDefaultModule();
}



/********************************************************************************/
/*										*/
/*	Value to return with additional information				*/
/*										*/
/********************************************************************************/

//CHECKSTYLE:OFF        needed to get values back to seede
public static class Return {

   private Object for_object;
   private int ref_id;
   
   @SuppressWarnings("unused")
   private int hash_code;

   Return(Object o) {
      for_object = o;
      ref_id = id_counter.incrementAndGet();
      hash_code = System.identityHashCode(o);
    }

}	// end of inner class Return
//CHECKSTYLE:OFF



/********************************************************************************/
/*										*/
/*	Other helper routines							*/
/*										*/
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


public static NumberFormat getNumberFormatInstance(String typ,String slocale)
{
   NumberFormat rslt = null;
   Locale locale = null;
   if (slocale != null) locale = Locale.forLanguageTag(slocale);
   if (typ == null) typ = "*";
   switch (typ) {
      default :
      case "*" :
      case "Number" :
	 if (locale == null) rslt = NumberFormat.getInstance();
	 else rslt = NumberFormat.getInstance(locale);
	 break;
      case "Integer" :
	 if (locale == null) rslt = NumberFormat.getIntegerInstance();
	 else rslt = NumberFormat.getIntegerInstance(locale);
	 break;
      case "Percent" :
	 if (locale == null) rslt = NumberFormat.getPercentInstance();
	 else rslt = NumberFormat.getPercentInstance(locale);
	 break;
      case "Currency" :
	 if (locale == null) rslt = NumberFormat.getCurrencyInstance();
	 else rslt = NumberFormat.getCurrencyInstance(locale);
	 break;
    }

   return rslt;
}



public static Constructor<?> getConstructorUsingPoppy(String cls, String... args)
	throws NoSuchMethodException
{
   try {
      Class<?> cl = Class.forName(cls);
      Class<?> [] acl = new Class<?>[args.length];
      for (int i = 0; i < args.length; ++i) {
	 acl[i] = getClassForName(args[i]);
       }
      Constructor<?> rslt = cl.getConstructor(acl);
      return rslt;
    }
   catch (ClassNotFoundException e) { }

   return null;
}



public static Constructor<?> getDeclaredConstructorUsingPoppy(String cls, String... args)
	throws NoSuchMethodException
	{
   try {
      Class<?> cl = Class.forName(cls);
      Class<?> [] acl = new Class<?>[args.length];
      for (int i = 0; i < args.length; ++i) {
	 acl[i] = getClassForName(args[i]);
       }
      Constructor<?> rslt = cl.getDeclaredConstructor(acl);
      return rslt;
    }
   catch (ClassNotFoundException e) { }

   return null;
}




public static Method getMethodUsingPoppy(String cls,String name,boolean decl,String... args)
	throws NoSuchMethodException
{
   try {
//    Class<?> cl = Class.forName(cls);
      Class<?> cl = getClassByName(cls);
      Class<?> [] acl = new Class<?>[args.length];
      for (int i = 0; i < args.length; ++i) {
	 acl[i] = getClassForName(args[i]);
       }
//    System.err.println("POPPY: Get method " + name + " " + acl.length + " " + args.length);

      Method rslt = null;
      if (decl) rslt = cl.getDeclaredMethod(name,acl);
      else rslt = cl.getMethod(name,acl);
      return rslt;
    }
   catch (ClassNotFoundException e) {
       System.err.println("POPPY: Class " + cls + " not found");
    }

   return null;
}



private static Class<?> getClassForName(String name) throws ClassNotFoundException
{
   switch (name) {
      case "int" :
	 return int.class;
      case "short" :
	 return short.class;
      case "byte" :
	 return byte.class;
      case "char" :
	 return char.class;
      case "long" :
	 return long.class;
      case "float" :
	 return float.class;
      case "double" :
	 return double.class;
    }

   return Class.forName(name);
}



public static Constructor<?>[] getDeclaredConstructorsUsingPoppy(String cls,boolean pub)
{
   try {
      Class<?> cl = Class.forName(cls);
      Constructor<?>[] rslt = cl.getDeclaredConstructors();
      if (pub) {
	 int ct = 0;
	 for (int i = 0; i < rslt.length; ++i) {
	    if (Modifier.isPublic(rslt[i].getModifiers())) ++ct;
	  }
	 if (ct != rslt.length) {
	    Constructor<?>[] nrslt = new Constructor<?>[ct];
	    int nct = 0;
	    for (int i = 0; i < rslt.length; ++i) {
	       if (Modifier.isPublic(rslt[i].getModifiers())) {
		  nrslt[nct++] = rslt[i];
		}
	     }
	    rslt = nrslt;
	  }
       }
      return rslt;
    }
   catch (ClassNotFoundException e) { }

   return null;
}






public static Object getNewInstance(String name)
{
   try {
      Class<?> c = Class.forName(name);
      return c.getDeclaredConstructor().newInstance();
    }
   catch (Throwable t) {
      return null;
    }
}



public static Object getLambdaClassUsingPoppy(String name)
{
   try {
      Class<?> cl = Class.forName(name);
      System.err.println("FOUND " + cl + " " + cl.toGenericString());
      System.err.println("SUPER: " + cl.getSuperclass());
      System.err.println("FIELDS: " + cl.getFields());
      System.err.println("FLAGS: " + cl.isAnonymousClass() + " " + cl.isLocalClass() + " " + cl.isMemberClass() + " " +
	    cl.isSynthetic());
      String s = cl.getName() + ";" + cl.getSuperclass().getName();
      return s;
    }
   catch (Throwable t) {
      System.err.println("Problem with lambda class: " + t);
      t.printStackTrace();
      return null;
    }
}

/********************************************************************************/
/*										*/
/*	Handle patterns 							*/
/*										*/
/********************************************************************************/

public static Matcher getPatternMatcher(String pat,String v)
{
   Pattern p = Pattern.compile(pat);
   return p.matcher(v);
}


public static Matcher matchFinder(String pat,String v,int start,
      int from,int to,boolean fail,int anch)
{
   Pattern p = Pattern.compile(pat);
   Matcher m = p.matcher(v);
   m.region(from,to);
   if (fail) {
      m.reset();
    }
   else if (anch == 1) {
      m.matches();
    }
   else if (anch == 0) {
      m.lookingAt();
    }
   else {
      m.find(start);
    }
   return m;
}



/********************************************************************************/
/*										*/
/*	Handle invoke dynamic							*/
/*										*/
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


/********************************************************************************/
/*										*/
/*		CashewValueObject.java						*/
/*										*/
/*	Object Value representation						*/
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

import edu.brown.cs.ivy.jcomp.JcompScope;
import edu.brown.cs.ivy.jcomp.JcompSymbol;
import edu.brown.cs.ivy.jcomp.JcompType;
import edu.brown.cs.ivy.jcomp.JcompTyper;
import edu.brown.cs.ivy.xml.IvyXmlWriter;
import edu.brown.cs.seede.acorn.AcornLog;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

public class CashewValueObject extends CashewValue implements CashewConstants
{


/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private Map<String,CashewRef> field_values;
private Set<String> new_fields;
private int old_ref;
private Map<Long,String> string_values;
private Map<Long,CashewValue> array_values;

private static Map<String,CashewRef> static_values;
 
static {
   static_values = new ConcurrentHashMap<String,CashewRef>();
}
 

/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

CashewValueObject(JcompTyper typer,JcompType jt,Map<String,Object> inits,boolean caninit)
{
   super(jt);

   field_values = new HashMap<String,CashewRef>();
   new_fields = null;
   old_ref = 0;
   string_values = null;
   array_values = null;

   for (JcompType jt0 = jt; jt0 != null; jt0 = jt0.getSuperType()) {
      JcompScope tscp = jt0.getScope();
      if (tscp != null) {
	 for (JcompSymbol fsym : tscp.getDefinedFields()) {
	    String key = fsym.getFullName();
	    CashewValue cv = CashewValue.createDefaultValue(typer,fsym.getType());
	    CashewRef cr = null;
	    if (inits != null) {
	       Object ival = inits.get(key);
	       if (ival != null) {
		  if (ival instanceof CashewValue) cv = (CashewValue) ival;
		  else if (ival instanceof CashewDeferredValue) {
		     CashewDeferredValue dv = (CashewDeferredValue) ival;
		     cr = new CashewRef(dv);
		   }
		}
	       else if (!fsym.isStatic()) {
		  // first check if user has definition for the field and use it if so
		  if (new_fields == null) new_fields = new HashSet<String>();
		  new_fields.add(key);
		}
	     }
	    if (cr == null) cr = new CashewRef(cv,caninit);

	    if (fsym.isStatic()) {
	       if (!static_values.containsKey(key)) {
		  static_values.put(key,cr);
		  AcornLog.logD("Add static field " + key + " to " + getDataType(null).getName());
		}
	     }
	    else field_values.put(key,cr);
	  }
       }
    }

   if (inits != null) {
      Object hval = inits.get(HASH_CODE_FIELD);
      if (hval != null) {
	 CashewRef hv = null;
	 if (hval instanceof CashewRef)
	    hv = (CashewRef) hval;
	 if (hval instanceof CashewValue) {
	    hv = new CashewRef((CashewValue) hval,false);
	  }
	 if (hval instanceof CashewDeferredValue) {
	    CashewDeferredValue dv = (CashewDeferredValue) hval;
	    hv = new CashewRef(dv);
	  }
	 if (hv != null) field_values.put(HASH_CODE_FIELD,hv);
       }
    }
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

@Override public CashewValue getFieldValue(CashewValueSession sess,
      JcompTyper typer,CashewClock cc,String nm,CashewContext ctx,boolean force)
{
   CashewValue cv = findFieldForName(typer,nm,force);
   if (cv == null && force) {
      AcornLog.logE("CASHEW","Missing field " + nm);
    }
   return cv;
}

@Override public CashewValue setFieldValue(CashewValueSession sess,
      JcompTyper typer,CashewClock cc,String nm,CashewValue cv)
{
   CashewRef ov = findFieldForName(typer,nm,true);
   ov.setValueAt(sess,cc,cv);
   if (cc == null && new_fields != null) {
      new_fields.remove(nm);
      if (new_fields.isEmpty()) new_fields = null;
    }

   return this;
}


@Override public CashewValue addFieldValue(CashewValueSession sess,
      JcompTyper typer,CashewClock cc,String nm,CashewValue cv)
{
   CashewRef cr = field_values.get(nm);
   CashewRef cr1 = static_values.get(nm);
   if (cr != null || cr1 != null) {
      return setFieldValue(sess,typer,cc,nm,cv);
    }
   CashewRef tr = new CashewRef(cv,true);
   field_values.put(nm,tr);
   
   return this;
}



private CashewRef findFieldForName(JcompTyper typer,String nm,boolean force)
{
// AcornLog.logD("CASHEW","Lookup field " + nm);
   CashewRef ov = field_values.get(nm);
   if (ov == null) {
      ov = static_values.get(nm);
    }
   String anm = nm;
   while (ov == null && anm.contains("$")) {
      int idx = anm.indexOf("$");
      anm = anm.substring(0,idx) + "." + anm.substring(idx+1);
      ov = field_values.get(anm);
      if (ov == null) ov = static_values.get(anm);
    }
   
   if (ov == null && nm.equals(HASH_CODE_FIELD)) {
      CashewValue hashv = CashewValue.numericValue(typer,typer.INT_TYPE,hashCode());
      AcornLog.logD("CASHEW","Save our hash value " + hashv);
      ov = new CashewRef(hashv,false);
      field_values.put(HASH_CODE_FIELD,ov);
    }
   
   if (ov == null) AcornLog.logD("CASHEW","No field found for " + nm);
   
   if (ov == null) {
      // TODO: what if new field is static?
      JcompType jdt = getDataType(typer);
      Map<String,JcompType> flds = jdt.getFields();
      JcompType fty = flds.get(nm);
      if (fty != null) {
	 if (new_fields == null) new_fields = new HashSet<String>();
	 new_fields.add(nm);
	 CashewValue newv = CashewValue.createDefaultValue(typer,fty);
	 ov = new CashewRef(newv,true);
	 field_values.put(nm,ov);
         AcornLog.logD("CASHEW","Create new field value " + fty + " " + newv);
       }
    }

   if (ov == null && force) {
      throw new Error("UndefinedField: " + nm);
    }

   return ov;
}




@Override CashewValue lookupVariableName(CashewValueSession sess,
      CashewContext ctx,JcompTyper typer,String name,long when)
	throws CashewException
{
   String rest = null;
   String look = name;
   int idx = look.indexOf("?");
   if (idx > 0) {
      rest = look.substring(idx+1);
      look = look.substring(0,idx);
    }

   CashewRef ov = field_values.get(look);
   if (ov == null) {
      ov = static_values.get(look);
    }
   if (ov != null) return super.lookupVariableName(sess,ctx,typer,name,when);

   String match = "." + name;
   for (String fnm : field_values.keySet()) {
      if (fnm.endsWith(match)) {
	 if (rest != null) fnm = fnm + "?" + rest;
	 return super.lookupVariableName(sess,ctx,typer,fnm,when);
       }
    }
   for (String fnm : static_values.keySet()) {
      if (fnm.endsWith(match)) {
	 if (rest != null) fnm = fnm + "?" + rest;
	 return super.lookupVariableName(sess,ctx,typer,fnm,when);
       }
    }

   return null;
}



@Override public String getString(CashewValueSession sess,
      JcompTyper typer,CashewClock cc,int lvl,boolean dbg)
	throws CashewException
{
   StringBuffer buf = new StringBuffer();
   buf.append(getDataType(sess,cc,typer));
   if (lvl > 0 && field_values != null) {
      buf.append("{");
      int ctr = 0;
      for (String fldname : field_values.keySet()) {
	 if (ctr++ != 0) buf.append(",");
	 buf.append(fldname);
	 buf.append(":");
	 CashewValue cv = getFieldValue(sess,typer,cc,fldname,null);
	 buf.append(cv.getString(sess,typer,cc,lvl-1,dbg));
	 if ((ctr % 10) == 0 && Thread.currentThread().isInterrupted()) break;
       }
      buf.append("}");
    }
   return buf.toString();
}



/********************************************************************************/
/*										*/
/*	Cloning methods 							*/
/*										*/
/********************************************************************************/

public CashewValueObject cloneObject(CashewValueSession sess,JcompTyper typer,
      CashewClock cc,long when)
{
   CashewClock ncc = cc;
   if (when > 0) ncc = new CashewClock(when);

   Map<String,Object> inits = new HashMap<String,Object>();
   for (Map.Entry<String,CashewRef> ent : field_values.entrySet()) {
      String key = ent.getKey();
      if (key.startsWith("@")) continue;
      CashewValue cv = ent.getValue().getActualValue(sess,ncc);
      inits.put(key,cv);
    }
   return new CashewValueObject(typer,getDataType(typer),inits,false);
}


/********************************************************************************/
/*										*/
/*	Reset methods								*/
/*										*/
/********************************************************************************/

@Override protected void localResetValue(CashewValueSession sess,Set<CashewValue> done)
{
   for (CashewRef cr : field_values.values()) {
      cr.resetValues(sess,done);
    }
}


@Override protected void localResetType(CashewValueSession sess,JcompTyper typer,Set<CashewValue> done)
{
   //TODO: add any missing fields here

   for (CashewRef cr : field_values.values()) {
      cr.resetType(sess,typer,done);
    }
}



/********************************************************************************/
/*										*/
/*	Output methods								*/
/*										*/
/********************************************************************************/

@Override public boolean checkChanged(CashewOutputContext outctx)
{
   if (outctx.noteChecked(this)) return (old_ref == 0);

   boolean fg = (old_ref == 0);

   for (CashewRef cr : field_values.values()) {
      fg |= cr.checkChanged(outctx);
    }

   if (fg) old_ref = 0;

   return fg;
}



@Override public void checkToString(CashewValueSession sess,CashewOutputContext outctx)
{
   if (old_ref != 0) return;			// done before => don't recompute

   int rvl = outctx.noteValue(this);
   if (rvl >= 0) {
      return;
    }

   Set<Long> times = new TreeSet<>();
   Set<CashewValue> done = new HashSet<>();

   getChangeTimes(times,done);

   if (times.isEmpty()) {
      String s = outctx.getToString(sess,this);
      if (s != null) {
	 string_values = new HashMap<>();
	 string_values.put(0L,s);
       }
    }
   else {
      CashewClock cc = outctx.getClock();
      String last = null;
      for (Long t : times) {
	 CashewValue crv = cloneObject(sess,outctx.getTyper(),cc,t+1);
	 String s = outctx.getToString(sess,crv);
	 if (s == null && string_values == null) continue;
	 if (string_values == null) {
	    string_values = new HashMap<>();
	    if (t != 0) string_values.put(0L,null);
	  }
	 if (s != null && s.equals(last)) continue;
	 else if (s == null && last == null) continue;
	 string_values.put(t+1,s);
	 last = s;
       }
    }
}


@Override public void checkToArray(CashewValueSession sess,CashewOutputContext outctx)
{
   if (old_ref != 0) return;			// done before => don't recompute
   
   int rvl = outctx.noteValue(this);
   if (rvl >= 0) {
      return;
    }
   
   // For now, just do this at the end time, not anytime else.
   // Eventually we want to do it so we can update when it changes, but
   // that gets tricky
   
   AcornLog.logD("CASHEW","CHECKARRAY " + this);
   
   CashewValue cv = outctx.getToArray(sess,this);
   if (cv != null) {
      array_values = new HashMap<>();
      array_values.put(0L,cv);
    }
   
   for (Map.Entry<String,CashewRef> ent : field_values.entrySet()) {
      CashewRef cr = ent.getValue();
      String nm = ent.getKey();
      if (nm.startsWith("java.")) continue;
      if (nm.startsWith("@")) continue;
      AcornLog.logD("CASHEW","CHECKARRAY FIELD " + nm + " " + cr.isEmpty(sess));
      if (cr.isEmpty(sess)) continue;
      CashewValue cv1 = cr.getActualValue(sess,outctx.getClock());
      if (cv1 == null || cv1.isEmpty(sess)) continue;
      cv1.checkToArray(sess,outctx);
    }
}
   


void getChangeTimes(Set<Long> times,Set<CashewValue> done)
{
   if (!done.add(this)) return;

   for (CashewRef cr : field_values.values()) {
      cr.getChangeTimes(times,done);
    }
}


@Override public void outputLocalXml(IvyXmlWriter xw,CashewOutputContext outctx,String name)
{
   CashewValueSession sess = outctx.getSession();
   xw.field("OBJECT",true);
   JcompType ctyp = outctx.getTyper().findSystemType("java.awt.Component");
   JcompType otyp = getDataType(outctx.getTyper());
   if (otyp.isCompatibleWith(ctyp)) {
      xw.field("COMPONENT",true);
    }
   if (otyp.isEnumType()) {
      try {
         CashewValue evl = getFieldValue(outctx.getSession(),outctx.getTyper(),
               outctx.getClock(),"java.lang.Enum.name",null);
         String enm = evl.getString(outctx.getSession(),outctx.getTyper(),outctx.getClock());
         xw.field("ENUM",enm);
       }
      catch (CashewException e) { }
    }
   int rvl = outctx.noteValue(this);
   if (outctx.expand(name)) {
      for (Map.Entry<String,CashewRef> ent : field_values.entrySet()) {
	 CashewRef cr = ent.getValue();
	 cr.getDataType(null);
	 AcornLog.logD("CASHEW","EXPANDED " + ent.getKey() + " " + cr);
       }
      if (rvl > 0) rvl = -rvl;
      old_ref = 0;
    }
   else if (outctx.expandChild(name)) {
      if (rvl > 0) rvl = -rvl;
      old_ref = 0;
    }

   int oref = old_ref;
   old_ref = Math.abs(rvl);
   xw.field("ID",old_ref);
   if (rvl > 0) {
      xw.field("REF",true);
    }
   else if (oref != 0) {
      xw.field("OREF",oref);
    }
   else {
      for (Map.Entry<String,CashewRef> ent : field_values.entrySet()) {
	 if (!ent.getValue().isEmpty(sess)) {
	    xw.begin("FIELD");
	    xw.field("NAME",ent.getKey());
	    if (new_fields != null && new_fields.contains(ent.getKey()))
	       xw.field("NEWFIELD",true);
	    String nnm = name;
	    if (nnm != null) {
	       String fnm = ent.getKey();
	       int idx = fnm.lastIndexOf(".");
	       if (idx > 0) fnm = fnm.substring(idx+1);
	       nnm += "?" + fnm;
	     }
	    ent.getValue().outputXml(outctx,nnm);
	    xw.end("FIELD");
	  }
       }
      if (string_values != null) {
	 xw.begin("FIELD");
	 xw.field("NAME",TO_STRING_FIELD);
	 if (string_values.size() == 1) {
	    for (String s : string_values.values()) {
	       xw.cdata(s);
	     }
	  }
	 else {
	    for (Map.Entry<Long,String> ent : string_values.entrySet()) {
	       xw.begin("VALUE");
	       xw.field("TIME",ent.getKey());
	       xw.field("TYPE","java.lang.String");
	       if (ent.getValue() == null) {
		  xw.field("NO_TOSTRING",true);
		  xw.field("NULL",true);
		}
	       else xw.cdata(ent.getValue());
	       xw.end("VALUE");
	     }
	  }
	 xw.end("FIELD");
       }
      if (array_values != null) {
         AcornLog.logD("CASHEW","HAVE ARRAY VALUES " + array_values.size());
	 xw.begin("FIELD");
	 xw.field("NAME",TO_ARRAY_FIELD);
         for (Map.Entry<Long,CashewValue> ent : array_values.entrySet()) {
            if (ent.getValue() != null) {
               ent.getValue().outputXml(outctx,"toArray()");
             }
            else {
               xw.field("NO_TOARRAY",true);
               xw.field("NULL",true);
             }
          }
	 xw.end("FIELD");
       }
    }
}



public static void outputStatics(CashewOutputContext outctx)
{
   IvyXmlWriter xw = outctx.getXmlWriter();
   xw.begin("STATICS");
   for (Map.Entry<String,CashewRef> ent : static_values.entrySet()) {
      if (!ent.getValue().isEmpty(outctx.getSession())) {
	 xw.begin("STATIC");
	 xw.field("NAME",ent.getKey());
	 ent.getValue().outputXml(outctx,ent.getKey());
	 xw.end("STATIC");
       }
    }
   xw.end("STATICS");
}


@Override public String toString(CashewValueSession sess)
{
   return getDebugString(sess,null,null);
}


@Override public String toString()
{
   return getDebugString(null,null,null);
}




}	// end of class CashewValueObject




/* end of CashewValueObject.java */


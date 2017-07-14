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

private static Map<String,CashewRef> static_values;

static {
   static_values = new HashMap<String,CashewRef>();
}


/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

CashewValueObject(JcompType jt,Map<String,Object> inits,boolean caninit)
{
   super(jt);

   field_values = new HashMap<String,CashewRef>();
   new_fields = null;
   old_ref = 0;
   
   for (JcompType jt0 = jt; jt0 != null; jt0 = jt0.getSuperType()) {
      JcompScope tscp = jt0.getScope();
      for (JcompSymbol fsym : tscp.getDefinedFields()) {
	 String key = fsym.getFullName();
	 CashewValue cv = CashewValue.createDefaultValue(fsym.getType());
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
               AcornLog.logD("Add static field " + key + " to " + getDataType().getName());
             }
	  }
	 else field_values.put(key,cr);
       }
    }
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

@Override public CashewValue getFieldValue(CashewClock cc,String nm,boolean force)
{
   CashewValue cv = findFieldForName(nm,force);
   if (cv == null && force) {
      AcornLog.logE("Missing field " + nm);
    }
   return cv;
}

@Override public CashewValue setFieldValue(CashewClock cc,String nm,CashewValue cv)
{
   CashewRef ov = findFieldForName(nm,true);
   ov.setValueAt(cc,cv);
   if (cc == null && new_fields != null) {
      new_fields.remove(nm);
      if (new_fields.isEmpty()) new_fields = null;
    }

   return this;
}



private CashewRef findFieldForName(String nm,boolean force)
{
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
      CashewValue hashv = CashewValue.numericValue(INT_TYPE,hashCode());
      ov = new CashewRef(hashv,false);
      field_values.put(HASH_CODE_FIELD,ov);
    }
   
   if (ov == null) {
      // TODO: what if new field is static?
      JcompType jdt = getDataType();
      Map<String,JcompType> flds = jdt.getFields();
      JcompType fty = flds.get(nm);
      if (fty != null) {
         if (new_fields == null) new_fields = new HashSet<String>();
         new_fields.add(nm);
         CashewValue newv = CashewValue.createDefaultValue(fty);
         ov = new CashewRef(newv,true);
         field_values.put(nm,ov);
       }
    }
   
   if (ov == null && force) {
      throw new Error("UndefinedField: " + nm);
    }

   return ov;
}







@Override public String getString(CashewClock cc,int lvl,boolean dbg)
{
   StringBuffer buf = new StringBuffer();
   buf.append(getDataType(cc));
   if (lvl > 0 && field_values != null) {
      buf.append("{");
      int ctr = 0;
      for (String fldname : field_values.keySet()) {
	 if (ctr++ != 0) buf.append(",");
	 buf.append(fldname);
	 buf.append(":");
	 CashewValue cv = getFieldValue(cc,fldname);
	 buf.append(cv.getString(cc,lvl-1,dbg));
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

public CashewValueObject cloneObject(CashewClock cc)
{
   Map<String,Object> inits = new HashMap<String,Object>();
   for (Map.Entry<String,CashewRef> ent : field_values.entrySet()) {
      String key = ent.getKey();
      if (key.startsWith("@")) continue;
      CashewValue cv = ent.getValue().getActualValue(cc);
      inits.put(key,cv);
    }
   return new CashewValueObject(getDataType(),inits,false);
}




/********************************************************************************/
/*										*/
/*	Reset methods								*/
/*										*/
/********************************************************************************/

@Override protected void localResetValue(Set<CashewValue> done)
{
   for (CashewRef cr : field_values.values()) {
      cr.resetValues(done);
    }
}


@Override protected void localResetType(JcompTyper typer,Set<CashewValue> done)
{
   //TODO: add any missing fields here

   for (CashewRef cr : field_values.values()) {
      cr.resetType(typer,done);
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



@Override public void outputLocalXml(IvyXmlWriter xw,CashewOutputContext outctx)
{
   xw.field("OBJECT",true);
   if (getDataType().isCompatibleWith(COMPONENT_TYPE)) {
      xw.field("COMPONENT",true);
    }
   int rvl = outctx.noteValue(this);
   
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
      if (oref != 0) xw.field("OREF",oref);
      for (Map.Entry<String,CashewRef> ent : field_values.entrySet()) {
         if (!ent.getValue().isEmpty()) {
            xw.begin("FIELD");
            xw.field("NAME",ent.getKey());
            if (new_fields != null && new_fields.contains(ent.getKey()))
               xw.field("NEWFIELD",true);
            ent.getValue().outputXml(outctx);
            xw.end("FIELD");
          }
       }
    }
}



public static void outputStatics(IvyXmlWriter xw,CashewOutputContext outctx)
{
   xw.begin("STATICS");
   for (Map.Entry<String,CashewRef> ent : static_values.entrySet()) {
      if (!ent.getValue().isEmpty()) {
         xw.begin("STATIC");
         xw.field("NAME",ent.getKey());
         ent.getValue().outputXml(outctx);
         xw.end("STATIC");
       }
    }
   xw.end("STATICS");
}



@Override public String toString()
{
   return getDebugString(null);
}




}	// end of class CashewValueObject




/* end of CashewValueObject.java */


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
import edu.brown.cs.ivy.xml.IvyXmlWriter;

import java.util.HashMap;
import java.util.Map;

public class CashewValueObject extends CashewValue implements CashewConstants
{


/********************************************************************************/
/*										*/
/*	Private storage 							*/
/*										*/
/********************************************************************************/

private Map<String,CashewRef> field_values;

private static Map<String,CashewRef> static_values;

static {
   static_values = new HashMap<String,CashewRef>();
}


/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

CashewValueObject(JcompType jt,Map<String,Object> inits)
{
   super(jt);

   field_values = new HashMap<String,CashewRef>();
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
	  }
	 if (cr == null) cr = new CashewRef(cv);

	 if (fsym.isStatic()) {
	    if (!static_values.containsKey(key)) static_values.put(key,cr);
	  }
	 else field_values.put(key,cr);
       }
    }
    if (field_values.get(HASH_CODE_FIELD) == null) {
       CashewValue chvl = CashewValue.numericValue(INT_TYPE,hashCode());
       field_values.put(HASH_CODE_FIELD,new CashewRef(chvl));
     }
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

@Override public CashewValue getFieldValue(CashewClock cc,String nm)
{
   CashewValue cv = findFieldForName(nm);
   return cv;
}

@Override public CashewValue setFieldValue(CashewClock cc,String nm,CashewValue cv)
{
   CashewRef ov = findFieldForName(nm);
   ov.setValueAt(cc,cv);
   return this;
}



private CashewRef findFieldForName(String nm)
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
   
   if (ov == null) {
      throw new Error("UndefinedField: " + nm);
    }
   
   return ov;
}







@Override public String getString(CashewClock cc,int lvl,boolean dbg)
{
   StringBuffer buf = new StringBuffer();
   buf.append(getDataType(cc));
   if (lvl > 0) {
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
/*	Output methods								*/
/*										*/
/********************************************************************************/

@Override public void outputLocalXml(IvyXmlWriter xw,CashewOutputContext outctx)
{
   xw.field("OBJECT",true);
   int rvl = outctx.noteValue(this);
   xw.field("ID",Math.abs(rvl));
   if (rvl > 0) {
      xw.field("REF",true);
    }
   else {
      for (Map.Entry<String,CashewRef> ent : field_values.entrySet()) {
	 xw.begin("FIELD");
	 xw.field("NAME",ent.getKey());
	 ent.getValue().outputXml(outctx);
	 xw.end("FIELD");
       }
      for (Map.Entry<String,CashewRef> ent : static_values.entrySet()) {
	 if (!outctx.noteField(ent.getKey())) {
	    xw.begin("FIELD");
	    xw.field("NAME",ent.getKey());
	    ent.getValue().outputXml(outctx);
	    xw.end("FIELD");
	  }
       }
    }
}



@Override public String toString()
{
   return getDebugString(null);
}




/********************************************************************************/
/*										*/
/*     Class Object value							*/
/*										*/
/********************************************************************************/

static class ValueClass extends CashewValueObject
{
   private JcompType	 class_value;

   ValueClass(JcompType c) {
      super(CLASS_TYPE,null);
      class_value = c;
    }

   @Override public String getString(CashewClock cc,int idx,boolean dbg) {
      return class_value.toString();
    }

   @Override public void outputLocalXml(IvyXmlWriter xw,CashewOutputContext outctx) {
      xw.field("OBJECT",true);
      if (class_value == null) xw.field("CLASS","*UNKNOWN*");
      else xw.field("CLASS",class_value.toString());
    }

}	// end of inner class ValueClass





}	// end of class CashewValueObject




/* end of CashewValueObject.java */


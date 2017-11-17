/********************************************************************************/
/*										*/
/*		CashewRef.java							*/
/*										*/
/*	Holds a reference to a value						*/
/*										*/
/*	This holds a set of values that is time dependent.  It can be used	*/
/*	to represent variables or objects.  An object value (or array value)	*/
/*	contains a pointer to its reference and all access to it from other	*/
/*	variables or objects will point to the reference.  All access to	*/
/*	vvalues needs to be time-based when computing an expression.  The	*/
/*	result of the computation is a value however.				*/
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

import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import edu.brown.cs.ivy.jcomp.JcompType;
import edu.brown.cs.ivy.jcomp.JcompTyper;
import edu.brown.cs.ivy.xml.IvyXmlWriter;

class CashewRef extends CashewValue implements CashewConstants
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private SortedMap<Long,CashewValue>	value_map;
private long	last_update;
private CashewValue last_value;
private CashewDeferredValue deferred_value;
private boolean can_initialize;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

CashewRef(CashewValue v,boolean caninit)
{
   last_update = 0;
   last_value = v;
   can_initialize = caninit;
}

CashewRef(CashewDeferredValue deferred)
{
   last_update = -1;
   last_value = null;
   deferred_value = deferred;
   can_initialize = true;
}




/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

@Override public Number getNumber(CashewClock cc)
{
   CashewValue cv = getValueAt(cc);
   if (cv == null) return null;
   return cv.getNumber(cc);
}


@Override public Character getChar(CashewClock cc)
{
   CashewValue cv = getValueAt(cc);
   if (cv == null) return null;
   return cv.getChar(cc);
}



@Override public String getString(CashewClock cc,int lvl,boolean dbg)
{
   if (dbg && deferred_value != null) return "<???>";

   CashewValue cv = getValueAt(cc);
   if (cv == null) return null;
   return cv.getString(cc,lvl,dbg);
}



@Override public CashewValue getActualValue(CashewClock cc)
{
   return getValueAt(cc);
}


@Override public CashewValue setValueAt(CashewClock cc,CashewValue cv)
{
   if (cv == null) return this;
   cv = cv.getActualValue(cc);
   if (cv == null) return this;

   long tv = 0;
   if (cc != null) tv = cc.getTimeValue();

   if (last_update < 0 || (tv == 0 && last_update == 0)) {
      // first time -- just record value
      last_update = tv;
      last_value = cv;
      if (cc != null) cc.tick();
      return this;
    }

   if (value_map == null) {
      value_map = new TreeMap<Long,CashewValue>();
      if (last_value != null) value_map.put(last_update,last_value);
    }

   if (tv >= last_update) {
      last_update = tv;
      last_value = cv;
    }

   value_map.put(tv,cv);
   if (cc != null) cc.tick();

   return this;
}



@Override protected void localResetValue(Set<CashewValue> done)
{
   if (value_map != null) {
      long v0 = value_map.firstKey();
      last_update = v0;
      last_value = value_map.get(v0);
      value_map = null;
    }
   if (last_update > 0) {
      last_update = 0;
      last_value = null;
    }

   if (last_value != null) last_value.resetValues(done);
}


@Override protected void localResetType(JcompTyper typer,Set<CashewValue> done)
{
   if (last_value != null) last_value.resetType(typer,done);
   if (value_map != null) {
      for (CashewValue cv : value_map.values()) {
	 cv.resetType(typer,done);
       }
    }
}



@Override public CashewValue getFieldValue(CashewClock cc,String name,boolean force)
{
   CashewValue cv = getValueAt(cc);
   if (cv == null) return null;
   return cv.getFieldValue(cc,name,force);
}



@Override public CashewValue setFieldValue(CashewClock cc,String name,CashewValue v)
{
   CashewValue cv = getValueAt(cc);
   if (cv == null) return null;
   CashewValue ncv = cv.setFieldValue(cc,name,v);
   setValueAt(cc,ncv);
   return this;
}


@Override public CashewValue getIndexValue(CashewClock cc,int idx)
{
   CashewValue cv = getValueAt(cc);
   if (cv == null) return null;
   return cv.getIndexValue(cc,idx);
}

@Override public int getDimension(CashewClock cc)
{
   CashewValue cv = getValueAt(cc);
   if (cv == null) 
      throw new Error("Value is not an array");
   return cv.getDimension(cc);
}


@Override public CashewValue setIndexValue(CashewClock cc,int idx,CashewValue v)
{
   CashewValue cv = getValueAt(cc);
   if (cv == null) return null;
   CashewValue ncv = cv.setIndexValue(cc,idx,v);
   setValueAt(cc,ncv);
   return this;
}



@Override public boolean isNull(CashewClock cc)
{
   CashewValue cv = getValueAt(cc);
   if (cv == null) return true;
   return cv.isNull(cc);
}


@Override public boolean isEmpty()
{
   return value_map == null && last_value == null;
}




@Override public boolean isCategory2(CashewClock cc)
{
   CashewValue cv = getValueAt(cc);
   if (cv == null) return false;
   return cv.isCategory2(cc);
}


@Override public boolean isFunctionRef(CashewClock cc)
{
   CashewValue cv = getValueAt(cc);
   if (cv == null) return false;
   return cv.isFunctionRef(cc);
}


@Override public JcompType getDataType(CashewClock cc)
{
   CashewValue cv = getValueAt(cc);
   if (cv == null) return null;
   return cv.getDataType(cc);
}





@Override public String getInternalRepresentation(CashewClock cc)
{
   CashewValue cv = getValueAt(cc);
   if (cv == null) return null;
   return cv.getInternalRepresentation(cc);
}




/********************************************************************************/
/*										*/
/*	Internal access methods 						*/
/*										*/
/********************************************************************************/

private CashewValue getValueAt(CashewClock cc)
{
   long tv = 0;
   if (cc == null) {
      if (last_update >= 0) tv = last_update+1;
    }
   else tv = cc.getTimeValue();

   if (last_update >= 0) {
      if (tv > last_update || tv == 0) return last_value;
    }

   if (value_map == null) {
      if (deferred_value != null) {
	 CashewValue cv = deferred_value.getValue();
	 if (cv != null) {
	    last_update = 0;
	    last_value = cv;
	    deferred_value = null;
	    return cv;
	  }
       }
      return null;
    }

   SortedMap<Long,CashewValue> head = value_map.headMap(tv);
   if (head.isEmpty()) return null;

   return value_map.get(head.lastKey());
}


@Override boolean sameValue(CashewValue cv)
{
   if (value_map == null && last_value != null)
      return last_value.sameValue(cv);

   return false;
}




/********************************************************************************/
/*										*/
/*	Output Methods								*/
/*										*/
/********************************************************************************/

@Override public boolean checkChanged(CashewOutputContext outctx)
{
   if (value_map == null && last_value == null) return false;
   if (value_map == null && last_value != null) {
      boolean fg = last_value.checkChanged(outctx);
      if (last_update == 0) return fg;
      return true;
    }
   for (CashewValue cv : value_map.values()) {
      if (cv != null) cv.checkChanged(outctx);
    }
   return true;
}




@Override public void outputXml(CashewOutputContext outctx,String name)
{
   IvyXmlWriter xw = outctx.getXmlWriter();
   if (outctx.expand(name)) {
      if (deferred_value != null && value_map == null) {
         getValueAt(null);
       }
    }
   if (value_map == null) {
      if (last_value != null) {
	 xw.begin("VALUE");
	 if (can_initialize) xw.field("CANINIT",true);
	 xw.field("TYPE",last_value.getDataType());
	 last_value.outputLocalXml(xw,outctx,name);
	 xw.end("VALUE");
       }
    }
   else {
      for (Map.Entry<Long,CashewValue> ent : value_map.entrySet()) {
	 long when = ent.getKey();
	 xw.begin("VALUE");
	 xw.field("TIME",when);
	 if (can_initialize) xw.field("CANINIT",true);
	 xw.field("TYPE",ent.getValue().getDataType());
	 if (ent.getValue() != null) {
	    ent.getValue().outputLocalXml(xw,outctx,name);
	  }
	 xw.end("VALUE");
       }
    }
}



/********************************************************************************/
/*										*/
/*	Debugging methods							*/
/*										*/
/********************************************************************************/

@Override public String toString()
{
   if (value_map != null) {
      StringBuffer buf = new StringBuffer();
      buf.append("[");
      int idx = 0;
      for (Map.Entry<Long,CashewValue> ent : value_map.entrySet()) {
	 if (idx++ > 0) buf.append(",");
	 buf.append(ent.getValue());
	 buf.append("@");
	 buf.append(ent.getKey());
       }
      buf.append("]");
      return buf.toString();
    }
   else if (deferred_value != null) return "[***]";
   else return "[" + last_value + "]";
}



}	// end of class CashewRef




/* end of CashewRef.java */

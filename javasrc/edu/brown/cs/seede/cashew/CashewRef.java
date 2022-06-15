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
import java.util.concurrent.ConcurrentHashMap;

import edu.brown.cs.ivy.jcomp.JcompType;
import edu.brown.cs.ivy.jcomp.JcompTyper;
import edu.brown.cs.ivy.xml.IvyXmlWriter;
import edu.brown.cs.seede.acorn.AcornLog;

class CashewRef extends CashewValue implements CashewConstants
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private Map<CashewValueSession,SortedMap<Long,CashewValue>> value_maps;
private Map<CashewValueSession,Long> last_updates;
private Map<CashewValueSession,CashewValue> last_values;
private CashewValue initial_value;
private CashewDeferredValue deferred_value;
private boolean can_initialize;
private CashewValueSession last_session;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

CashewRef(CashewValue v,boolean caninit)
{
   initialize(caninit,v);
}

CashewRef(CashewDeferredValue deferred)
{
   initialize(true,deferred);
}


private void initialize(boolean init,Object cv)
{
   last_updates = null;
   last_values = null;
   can_initialize = init;
   value_maps = null;
   last_session = null;
   if (cv != null && cv instanceof CashewDeferredValue) {
      deferred_value = (CashewDeferredValue) cv;
      initial_value = null;
    }
   else if (cv != null && cv instanceof CashewValue) {
      deferred_value = null;
      initial_value = (CashewValue) cv;
    }
   else {
      AcornLog.logX("CASHEW","Creating empty reference");
    }
}


/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

@Override public Number getNumber(CashewValueSession sess,CashewClock cc) throws CashewException
{
   CashewValue cv = getValueAt(sess,cc);
   if (cv == null) return null;
   return cv.getNumber(sess,cc);
}


@Override public Character getChar(CashewValueSession sess,CashewClock cc) throws CashewException
{
   CashewValue cv = getValueAt(sess,cc);
   if (cv == null) return null;
   return cv.getChar(sess,cc);
}



@Override public String getString(CashewValueSession sess,
      JcompTyper typer,CashewClock cc,int lvl,boolean dbg)
        throws CashewException
{
   if (dbg && deferred_value != null) return "<DEFER>";

   CashewValue cv = getValueAt(sess,cc);
   if (cv == null) return null;
   return cv.getString(sess,typer,cc,lvl,dbg);
}



@Override public CashewValue getActualValue(CashewValueSession sess,CashewClock cc)
{
   return getValueAt(sess,cc);
}


@Override public CashewValue setValueAt(CashewValueSession sess,CashewClock cc,CashewValue cv)
{
   if (cv == null) return this;
   cv = cv.getActualValue(sess,cc);
   if (cv == null) return this;
   
   last_session = sess;

   long tv = 0;
   if (cc != null) tv = cc.getTimeValue();
   
   long upd = getLastUpdate(sess);
   if (upd < 0 || (tv == 0 && upd == 0)) {
      // first time -- just record value
      initial_value = cv;
      setLastUpdate(sess,tv,cv);
      if (cc != null) cc.tick();
      return this;
    }

   SortedMap<Long,CashewValue> map = createValueMap(sess);
   map.put(tv,cv);
   
   if (tv >= upd) {
      setLastUpdate(sess,tv,cv);
    }

   if (cc != null) cc.tick();

   return this;
}



@Override protected void localResetValue(CashewValueSession sess,Set<CashewValue> done)
{
   SortedMap<Long,CashewValue> map = getValueMap(sess);
   if (map != null) {
      long v0 = map.firstKey();
      setLastUpdate(sess,v0,map.get(v0));
      clearValueMap(sess);
    }
   CashewValue cv = getLastValue(sess);
   clearLastUpdate(sess);

   if (cv != null) cv.resetValues(sess,done);
}


@Override protected void localResetType(CashewValueSession sess,JcompTyper typer,Set<CashewValue> done)
{
   CashewValue lv = getLastValue(sess);
   if (lv != null) lv.resetType(sess,typer,done);
   SortedMap<Long,CashewValue> map = getValueMap(sess);
   if (map != null) {
      for (CashewValue cv : map.values()) {
	 cv.resetType(sess,typer,done);
       }
    }
}



@Override public CashewValue getFieldValue(CashewValueSession sess,
      JcompTyper typer,CashewClock cc,String name,boolean force)
        throws CashewException
{
   CashewValue cv = getValueAt(sess,cc);
   if (cv == null) return null;
   return cv.getFieldValue(sess,typer,cc,name,force);
}



@Override public CashewValue setFieldValue(CashewValueSession sess,JcompTyper typer,
      CashewClock cc,String name,CashewValue v)
        throws CashewException
{
   CashewValue cv = getValueAt(sess,cc);
   if (cv == null) return null;
   CashewValue ncv = cv.setFieldValue(sess,typer,cc,name,v);
   setValueAt(sess,cc,ncv);
   return this;
}



@Override public CashewValue getIndexValue(CashewValueSession sess,CashewClock cc,int idx)
        throws CashewException
{
   CashewValue cv = getValueAt(sess,cc);
   if (cv == null) return null;
   return cv.getIndexValue(sess,cc,idx);
}



@Override public int getDimension(CashewValueSession sess,CashewClock cc) throws CashewException
{
   CashewValue cv = getValueAt(sess,cc);
   if (cv == null) 
      throw new Error("Value is not an array");
   return cv.getDimension(sess,cc);
}


@Override public CashewValue setIndexValue(CashewValueSession sess,CashewClock cc,int idx,CashewValue v)
        throws CashewException
{
   CashewValue cv = getValueAt(sess,cc);
   if (cv == null) return null;
   CashewValue ncv = cv.setIndexValue(sess,cc,idx,v);
   setValueAt(sess,cc,ncv);
   return this;
}



@Override public boolean isNull(CashewValueSession sess,CashewClock cc)
{
   CashewValue cv = getValueAt(sess,cc);
   if (cv == null) return true;
   return cv.isNull(sess,cc);
}


@Override public boolean isEmpty(CashewValueSession sess)
{
   return getValueMap(sess) == null && getLastValue(sess) == null;
}



@Override public boolean isCategory2(CashewValueSession sess,CashewClock cc)
{
   CashewValue cv = getValueAt(sess,cc);
   if (cv == null) return false;
   return cv.isCategory2(sess,cc);
}


@Override public boolean isFunctionRef(CashewValueSession sess,CashewClock cc)
{
   CashewValue cv = getValueAt(sess,cc);
   if (cv == null) return false;
   return cv.isFunctionRef(sess,cc);
}


@Override public JcompType getDataType(CashewValueSession sess,CashewClock cc,JcompTyper typer)
{
   CashewValue cv = getValueAt(sess,cc);
   if (cv == null) return null;
   return cv.getDataType(sess,cc,typer);
}



@Override public String getInternalRepresentation(CashewValueSession sess,CashewClock cc)
{
   CashewValue cv = getValueAt(sess,cc);
   if (cv == null) return null;
   return cv.getInternalRepresentation(sess,cc);
}



/********************************************************************************/
/*										*/
/*	Internal access methods 						*/
/*										*/
/********************************************************************************/

private CashewValue getValueAt(CashewValueSession sess,CashewClock cc)
{
   long tv = 0;
   long upd = getLastUpdate(sess);
   if (cc == null) {
      if (upd >= 0) tv = upd+1;
    }
   else tv = cc.getTimeValue();
   last_session = sess;

   if (upd >= 0) {
      CashewValue lvl = getLastValue(sess);
      if (lvl == null) {
         AcornLog.logD("CASHEW","Update with no value " + upd + " " + tv);
       }
      
      else if (tv > upd || tv == 0) return lvl;
    }

   SortedMap<Long,CashewValue> map = getValueMap(sess);
   if (map == null) {
      if (deferred_value != null) {
	 CashewValue cv = deferred_value.getValue(sess);
         if (cv == null) cv = deferred_value.getValue(sess.getParent());
	 if (cv != null) {
            setLastUpdate(sess,0,cv);
	    deferred_value = null;
            initial_value = cv;
	    return cv;
	  }
         else AcornLog.logE("CASHEW","No value computed for deferred value");
       }
      return null;
    }

   SortedMap<Long,CashewValue> head = map.headMap(tv);
   if (head.isEmpty()) return null;

   return map.get(head.lastKey());
}



@Override boolean sameValue(CashewValueSession sess,CashewValue cv)
{
   if (getValueMap(sess) == null && getLastValue(sess) != null)
      return getLastValue(sess).sameValue(sess,cv);

   return false;
}
 


/********************************************************************************/
/*                                                                              */
/*      Handle multiple sessions                                                */
/*                                                                              */
/********************************************************************************/

private long getLastUpdate(CashewValueSession sess)
{
   Long lng = null;
   if (last_updates != null && sess != null) lng = last_updates.get(sess);
   else if (sess != null) {
      getLastUpdate(sess.getParent());
    }
   if (lng == null) {
      if (deferred_value != null) return -1;
      return 0;
    }
   
   return lng;
}
 

private CashewValue getLastValue(CashewValueSession sess)
{
   if (last_values == null || sess == null) return initial_value;
   CashewValue cv = last_values.get(sess);
   if (cv != null) return cv;
   if (sess.getParent() == null) return initial_value;
   return getLastValue(sess.getParent());
}


private void setLastUpdate(CashewValueSession sess,long v,CashewValue cv)
{
   if (last_updates == null || last_values == null) {
      synchronized (this) {
         if (last_updates == null) last_updates = new ConcurrentHashMap<>();
         if (last_values == null) last_values = new ConcurrentHashMap<>();
       }
    }
   last_updates.put(sess,v);
   last_values.put(sess,cv);
}

private void clearLastUpdate(CashewValueSession sess)
{
   if (last_updates != null) last_updates.remove(sess);
   if (last_values != null) last_values.remove(sess);
}


private SortedMap<Long,CashewValue> getValueMap(CashewValueSession sess)
{
   if (value_maps == null || sess == null) return null;
   SortedMap<Long,CashewValue> map = value_maps.get(sess);
   if (map == null) {
      return getValueMap(sess.getParent());
    }
   return map;
}


private SortedMap<Long,CashewValue> createValueMap(CashewValueSession sess)
{
   if (value_maps == null) {
      synchronized (this) {
         if (value_maps == null) value_maps = new ConcurrentHashMap<>();
       }
    }
   SortedMap<Long,CashewValue> nmap = new TreeMap<>();
   SortedMap<Long,CashewValue> map = value_maps.putIfAbsent(sess,nmap);
   if (map != null) nmap = map;
   else {
      CashewValue cv = getLastValue(sess);
      if (cv != null) nmap.put(getLastUpdate(sess),cv);
    }
   return nmap;
}


private void clearValueMap(CashewValueSession sess)
{
   if (value_maps == null) return;
   value_maps.remove(sess);
}



/********************************************************************************/
/*										*/
/*	Output Methods								*/
/*										*/
/********************************************************************************/

@Override public boolean checkChanged(CashewOutputContext outctx)
{
   CashewValueSession sess = outctx.getSession();
   if (getValueMap(sess) == null && getLastValue(sess) == null) return false;
   if (getValueMap(sess) == null && getLastValue(sess) != null) {
      boolean fg = getLastValue(sess).checkChanged(outctx);
      if (getLastUpdate(sess) == 0) return fg;
      return true;
    }
   for (CashewValue cv : getValueMap(sess).values()) {
      if (cv != null) cv.checkChanged(outctx);
    }
   return true;
}



@Override public void checkToString(CashewValueSession sess,CashewOutputContext outctx)
{
   if (getValueMap(sess) == null && getLastValue(sess) == null) return;
   if (getValueMap(sess) == null && getLastValue(sess) != null) {
      getLastValue(sess).checkToString(sess,outctx);
      return;
    }
   // here we need to compute all relevant times and then
   // do the checkToString at a particular time
   
   for (CashewValue cv : getValueMap(sess).values()) {
      if (cv != null) cv.checkToString(sess,outctx);
    }
}


@Override public void checkToArray(CashewValueSession sess,CashewOutputContext outctx)
{
   if (getValueMap(sess) == null && getLastValue(sess) == null) return;
   if (getValueMap(sess) == null && getLastValue(sess) != null) {
      getLastValue(sess).checkToArray(sess,outctx);
      return;
    }
   // here we need to compute all relevant times and then
   // do the checkToArray at a particular time
   
   for (CashewValue cv : getValueMap(sess).values()) {
      if (cv != null) cv.checkToArray(sess,outctx);
    }
}


void getChangeTimes(CashewValueSession sess,Set<Long> times,Set<CashewValue> done)
{
   if (!done.add(this)) return;
   
   if (getValueMap(sess) != null) times.addAll(getValueMap(sess).keySet());
   else if (getLastUpdate(sess) > 0) times.add(getLastUpdate(sess));
   
   if (getValueMap(sess) != null) {
      for (CashewValue cv : getValueMap(sess).values()) {
         cv.getChangeTimes(times,done);
       }
    }
   else if (getLastValue(sess) != null) {
      getLastValue(sess).getChangeTimes(times,done);
    }
}



@Override public void outputXml(CashewOutputContext outctx,String name)
{
   CashewValueSession sess = outctx.getSession();
   IvyXmlWriter xw = outctx.getXmlWriter();
   if (outctx.expand(name)) {
      if (deferred_value != null && getValueMap(sess) == null) {
         getValueAt(outctx.getSession(),null);
       }
    }
   if (getValueMap(sess) == null) {
      if (getLastValue(sess) != null) {
	 xw.begin("VALUE");
	 if (can_initialize) xw.field("CANINIT",true);
	 xw.field("TYPE",getLastValue(sess).getDataType(null));
	 getLastValue(sess).outputLocalXml(xw,outctx,name);
	 xw.end("VALUE");
       }
    }
   else {
      for (Map.Entry<Long,CashewValue> ent : getValueMap(sess).entrySet()) {
	 long when = ent.getKey();
	 xw.begin("VALUE");
	 xw.field("TIME",when);
	 if (can_initialize) xw.field("CANINIT",true);
	 xw.field("TYPE",ent.getValue().getDataType(null));
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
   return toString(last_session);
}


 
@Override public String toString(CashewValueSession sess)
{
   if (getValueMap(sess) != null) {
      StringBuffer buf = new StringBuffer();
      buf.append("[");
      int idx = 0;
      for (Map.Entry<Long,CashewValue> ent : getValueMap(sess).entrySet()) {
	 if (idx++ > 0) buf.append(",");
	 buf.append(ent.getValue());
	 buf.append("@");
	 buf.append(ent.getKey());
       }
      buf.append("]");
      return buf.toString();
    }
   else if (deferred_value != null) return "[***]";
   else return "[" + getLastValue(sess) + "]";
}



}	// end of class CashewRef




/* end of CashewRef.java */

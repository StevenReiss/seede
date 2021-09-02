/********************************************************************************/
/*										*/
/*		CashewValueArray.java						*/
/*										*/
/*	description of class							*/
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
import java.util.HashMap;
import java.util.Set;

import edu.brown.cs.ivy.jcomp.JcompType;
import edu.brown.cs.ivy.jcomp.JcompTyper;
import edu.brown.cs.ivy.xml.IvyXmlWriter;

public class CashewValueArray extends CashewValue implements CashewConstants
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private int dim_size;
private CashewRef [] array_values;
private int old_ref;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

CashewValueArray(JcompTyper typer,JcompType jt,int dim,
      Map<Integer,Object> inits,boolean caninit) 
{
   super(jt);
   dim_size = dim;
   array_values = new CashewRef[dim];
   old_ref = 0;
   for (int i = 0; i < dim; ++i) {
      CashewValue cv = CashewValue.createDefaultValue(typer,jt.getBaseType());
      if (inits != null) {
	 Object ival = inits.get(i);
	 if (ival instanceof CashewValue) cv = (CashewValue) ival;
	 else if (ival instanceof CashewDeferredValue) {
	    CashewDeferredValue dcv = (CashewDeferredValue) ival;
	    array_values[i] = new CashewRef(dcv);
	    continue;
	  }
       }
      array_values[i] = new CashewRef(cv,caninit);
    }
}


/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

@Override public CashewValue getFieldValue(CashewValueSession sess,
      JcompTyper typer,CashewClock cc,String nm,boolean force) 
        throws CashewException
{
   if (nm != null && nm.equals("length")) {
      return CashewValue.numericValue(typer,typer.INT_TYPE,dim_size);
    }
   return super.getFieldValue(sess,typer,cc,nm,force);
}

@Override public int getDimension(CashewValueSession s,CashewClock cc)
{
   return dim_size;
}

@Override public CashewValue getIndexValue(CashewValueSession sess,
      CashewClock cc,int idx) 
{
   if (idx < 0 || idx >= dim_size)
      throw new Error("IndexOutOfBounds");
   return array_values[idx];
}

@Override public CashewValue setIndexValue(CashewValueSession sess,CashewClock cc,int idx,CashewValue v) 
{
   if (idx < 0 || idx >= dim_size) throw new Error("IndexOutOfBounds");
   array_values[idx].setValueAt(sess,cc,v);
   return this;
}

@Override public String getString(CashewValueSession sess,
      JcompTyper typer,CashewClock cc,int lvl,boolean dbg)
        throws CashewException
{
   StringBuffer buf = new StringBuffer();
   buf.append("[");
   if (lvl > 0) {
      int sz = dim_size;
      if (dbg) sz = Math.min(sz,10);
      for (int i = 0; i < sz; ++i) {
	 if (i != 0) buf.append(",");
	 buf.append(getIndexValue(sess,cc,i).getString(sess,typer,cc,lvl,dbg));
       }
      if (sz < dim_size) buf.append("...");
    }
   else if (dim_size > 0) buf.append("...");
   buf.append("]");
   return buf.toString();
}


@Override public String getInternalRepresentation(CashewValueSession sess,CashewClock cc)
{
   String rslt = super.getInternalRepresentation(sess,cc);
   if (rslt != null) return rslt;

   StringBuffer buf = new StringBuffer();
   buf.append("new " + getDataType(sess,cc,null).getBaseType().getName() + "[" + dim_size + "|");
   buf.append("{");
   for (int i = 0; i < dim_size; ++i) {
      String r = getIndexValue(sess,cc,i).getInternalRepresentation(sess,cc);
      if (i > 0) buf.append(",");
      buf.append(r);
    }
   buf.append("}");

   return buf.toString();
}



/********************************************************************************/
/*										*/
/*	Reset methods								*/
/*										*/
/********************************************************************************/

@Override protected void localResetValue(Set<CashewValue> done)
{
   for (CashewRef cr : array_values) {
      cr.resetValues(done);
    }
}



@Override protected void localResetType(JcompTyper typer,Set<CashewValue> done)
{
   for (CashewRef cr : array_values) {
      cr.resetType(typer,done);
    }
}


/********************************************************************************/
/*										*/
/*	Cloning methods 							*/
/*										*/
/********************************************************************************/

public CashewValueArray cloneObject(CashewValueSession sess,JcompTyper typer,CashewClock cc,long when)
{
   CashewClock ncc = cc;
   if (when > 0) ncc = new CashewClock(when);
   
   Map<Integer,Object> inits = new HashMap<>();
   for (int i = 0; i < array_values.length; ++i) {
      if (array_values[i] != null) {
	 inits.put(i,array_values[i].getActualValue(sess,ncc));
       }
    }
   return new CashewValueArray(typer,getDataType(typer),dim_size,inits,false);
}



void getChangeTimes(Set<Long> times,Set<CashewValue> done) 
{
   if (!done.add(this) || dim_size == 0) return;
   
   for (int i = 0; i < array_values.length; ++i) {
      if (array_values[i] != null) {
         array_values[i].getChangeTimes(times,done);
       }
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

   for (CashewValue cv : array_values) {
      if (cv != null) {
	 fg |= cv.checkChanged(outctx);
       }
    }

   if (fg) old_ref = 0;

   return fg;
}



@Override public void outputLocalXml(IvyXmlWriter xw,CashewOutputContext outctx,String name) 
{
   xw.field("ARRAY",true);
   int rvl = outctx.noteValue(this);
   if (outctx.expand(name)) {
      for (int i = 0; i < array_values.length; ++i) {
         CashewRef cr = array_values[i];
         cr.getDataType(null);
       }
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
      if (oref != 0) xw.field("OREF",oref);
      xw.field("SIZE",dim_size);
      CashewValue dfltval = createDefaultValue(outctx.getTyper(),
            getDataType(outctx.getTyper()).getBaseType());
      int numdflt = 0;
      for (int i = 0; i < array_values.length; ++i) {
	 if (array_values[i].sameValue(dfltval)) {
	    ++numdflt;
	    continue;
	  }
	 xw.begin("ELEMENT");
	 xw.field("INDEX",i);
	 String nnm = name;
	 if (nnm != null) nnm += "?" + i;
	 array_values[i].outputXml(outctx,nnm);
	 xw.end("ELEMENT");
       }
      if (numdflt != 0) {
	 xw.begin("ELEMENT");
	 xw.field("DEFAULT",true);
	 dfltval.outputXml(outctx,null);
	 xw.end("ELEMENT");
       }
    }
}



}	// end of class CashewValueArray




/* end of CashewValueArray.java */


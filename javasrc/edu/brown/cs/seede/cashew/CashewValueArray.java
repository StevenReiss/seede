/********************************************************************************/
/*                                                                              */
/*              CashewValueArray.java                                           */
/*                                                                              */
/*      description of class                                                    */
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

import java.util.Map;

import edu.brown.cs.ivy.jcomp.JcompType;
import edu.brown.cs.ivy.xml.IvyXmlWriter;

public class CashewValueArray extends CashewValue implements CashewConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private int dim_size;
private CashewRef [] array_values;




/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

CashewValueArray(JcompType jt,int dim,Map<Integer,Object> inits) {
   super(jt);
   dim_size = dim;
   array_values = new CashewRef[dim];
   for (int i = 0; i < dim; ++i) {
      CashewValue cv = CashewValue.createDefaultValue(jt.getBaseType());
      if (inits != null) {
         Object ival = inits.get(i);
         if (ival instanceof CashewValue) cv = (CashewValue) ival;
         else if (ival instanceof CashewDeferredValue) {
            CashewDeferredValue dcv = (CashewDeferredValue) ival;
            array_values[i] = new CashewRef(dcv);
            continue;
          }
       }
      array_values[i] = new CashewRef(cv);
    }
}


/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

@Override public CashewValue getFieldValue(CashewClock cc,String nm) {
   if (nm == "length") {
      return CashewValue.numericValue(null,dim_size);
    }
   return super.getFieldValue(cc,nm);
}

@Override public int getDimension(CashewClock cc)
{
   return dim_size; 
}

@Override public CashewValue getIndexValue(CashewClock cc,int idx) {
   if (idx < 0 || idx >= dim_size) throw new Error("IndexOutOfBounds");
   return array_values[idx];
}

@Override public CashewValue setIndexValue(CashewClock cc,int idx,CashewValue v) {
   if (idx < 0 || idx >= dim_size) throw new Error("IndexOutOfBounds");
   array_values[idx].setValueAt(cc,v);
   return this;
}

@Override public String getString(CashewClock cc,int lvl,boolean dbg) {
   StringBuffer buf = new StringBuffer();
   buf.append("[");
   if (lvl > 0) {
      int sz = dim_size;
      if (dbg) sz = Math.min(sz,10);
      for (int i = 0; i < sz; ++i) {
         if (i != 0) buf.append(",");
         buf.append(getIndexValue(cc,i).getString(cc,lvl,dbg));
       }
      if (sz < dim_size) buf.append("...");
    }
   else if (dim_size > 0) buf.append("...");
   buf.append("]");
   return buf.toString();
}


@Override public String getInternalRepresentation(CashewClock cc) 
{
   String rslt = super.getInternalRepresentation(cc);
   if (rslt != null) return rslt;
   
   StringBuffer buf = new StringBuffer();
   buf.append("new " + getDataType(cc).getBaseType().getName() + "[" + dim_size + "|");
   buf.append("{");
   for (int i = 0; i < dim_size; ++i) {
      String r = getIndexValue(cc,i).getInternalRepresentation(cc);
      if (i > 0) buf.append(",");
      buf.append(r);
    }
   buf.append("}");
   
   return buf.toString();
}



/********************************************************************************/
/*                                                                              */
/*      Output methods                                                          */
/*                                                                              */
/********************************************************************************/

@Override public void outputLocalXml(IvyXmlWriter xw,CashewOutputContext outctx) {
   xw.field("ARRAY",true);
   int rvl = outctx.noteValue(this);
   xw.field("ID",Math.abs(rvl));
   if (rvl > 0) {
      xw.field("REF",true);
    }
   else {
      xw.field("SIZE",dim_size);
      for (int i = 0; i < array_values.length; ++i) {
         xw.begin("ELEMENT");
         xw.field("INDEX",i);
         array_values[i].outputXml(outctx);
         xw.end("ELEMENT");
       }
    }
}



}       // end of class CashewValueArray




/* end of CashewValueArray.java */


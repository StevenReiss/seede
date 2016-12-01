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

public abstract class CashewValueArray extends CashewValue implements CashewConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private int dim_size;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

CashewValueArray(JcompType jt,int dim) {
   super(jt);
   dim_size = dim;
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

protected int getSize()                         { return dim_size; }

@Override public int getDimension(CashewClock cc)
{
   return dim_size; 
}

@Override public abstract CashewValue getIndexValue(CashewClock cc,int idx);


@Override public String getString(CashewClock cc) {
   StringBuffer buf = new StringBuffer();
   buf.append("[");
   for (int i = 0; i < dim_size; ++i) {
      if (i != 0) buf.append(",");
      buf.append(getIndexValue(cc,i).toString());
    }
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
/*      Computed array value                                                    */
/*                                                                              */
/********************************************************************************/

static class ComputedValueArray extends CashewValueArray {
   
   private CashewRef[] array_values;

   ComputedValueArray(JcompType jt,int dim,Map<Integer,Object> inits) {
      super(jt,dim);
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
  
   @Override public CashewValue getIndexValue(CashewClock cc,int idx) {
      if (idx < 0 || idx >= getSize()) throw new Error("IndexOutOfBounds");
      return array_values[idx];
    }
   
   @Override public CashewValue setIndexValue(CashewClock cc,int idx,CashewValue v) {
      if (idx < 0 || idx >= getSize()) throw new Error("IndexOutOfBounds");
      array_values[idx].setValueAt(cc,v);
      return this;
    }
   
   @Override public void outputLocalXml(IvyXmlWriter xw) {
      xw.field("ARRAY",true);
      xw.field("SIZE",getSize());
      for (int i = 0; i < array_values.length; ++i) {
         xw.begin("ELEMENT");
         xw.field("INDEX",i);
         array_values[i].outputXml(xw);
         xw.end("ELEMENT");
       }
    }
   
}       // end of inner class ComputedValueArray



}       // end of class CashewValueArray




/* end of CashewValueArray.java */


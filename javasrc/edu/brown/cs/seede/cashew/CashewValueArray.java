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

import edu.brown.cs.ivy.jcomp.JcompType;

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



/********************************************************************************/
/*                                                                              */
/*      Computed array value                                                    */
/*                                                                              */
/********************************************************************************/

static class ComputedValueArray extends CashewValueArray {
   
   private CashewValue[] array_values;

   ComputedValueArray(JcompType jt,int dim) {
      super(jt,dim);
    }
  
   private ComputedValueArray(ComputedValueArray base,int idx,CashewValue cv) {
      super(base.getDataType(),base.getSize());
      array_values = new CashewValue[getSize()];
      System.arraycopy(base.array_values,0,array_values,0,getSize());
      array_values[idx] = cv;
    }
   
   @Override public CashewValue getIndexValue(CashewClock cc,int idx) {
      if (idx < 0 || idx >= getSize()) throw new Error("IndexOutOfBounds");
      return array_values[idx];
    }
   
   @Override CashewValue setIndexValue(CashewClock cc,int idx,CashewValue v) {
      if (idx < 0 || idx >= getSize()) throw new Error("IndexOutOfBounds");
      return new ComputedValueArray(this,idx,v);
    }
   
}       // end of inner class ComputedValueArray



}       // end of class CashewValueArray




/* end of CashewValueArray.java */


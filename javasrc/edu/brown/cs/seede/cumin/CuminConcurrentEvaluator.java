/********************************************************************************/
/*										*/
/*		CuminConcurrentEvaluator.java					*/
/*										*/
/*	Handle various concurrent classes and methods				*/
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



package edu.brown.cs.seede.cumin;

import edu.brown.cs.ivy.jcomp.JcompType;
import edu.brown.cs.seede.cashew.CashewException;
import edu.brown.cs.seede.cashew.CashewValue;

class CuminConcurrentEvaluator extends CuminNativeEvaluator
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

CuminConcurrentEvaluator(CuminRunnerByteCode bc)
{
   super(bc);
}



/********************************************************************************/
/*										*/
/*	Handle the various AtomicXXX classes					*/
/*										*/
/********************************************************************************/

CuminRunStatus checkAtomicIntMethods() throws CashewException 
{
   CashewValue rslt = null;

   CashewValue thisarg = getValue(0);
   String clsnm = thisarg.getDataType(getClock()).getName();
   String valfld = clsnm + ".value";

   synchronized (thisarg) {
      CashewValue value = thisarg.getFieldValue(getTyper(),getClock(),valfld);
      long lval = value.getNumber(getClock()).longValue();
      JcompType dtyp = getTyper().LONG_TYPE;
      int secondarg = 3;
      if (clsnm.contains("Integer")){
	 dtyp = getTyper().INT_TYPE;
	 secondarg = 2;
       }

      switch (getMethod().getName()) {
	 case "get" :
	    rslt = value.getActualValue(getClock());
	    break;
	 case "set" :
	 case "lazySet" :
	    thisarg.setFieldValue(getTyper(),getClock(),valfld,getValue(1));
	    break;
	 case "getAndSet" :
	    rslt = value.getActualValue(getClock());
	    thisarg.setFieldValue(getTyper(),getClock(),valfld,getValue(1));
	    break;
	 case "getAndAdd" :
	    rslt = value.getActualValue(getClock());
	    lval += getLong(1);
	    thisarg.setFieldValue(getTyper(),getClock(),valfld,CashewValue.numericValue(dtyp,lval));
	    break;
	 case "getAndDecrement" :
	    rslt = value.getActualValue(getClock());
	    lval -= 1;
	    thisarg.setFieldValue(getTyper(),getClock(),valfld,CashewValue.numericValue(dtyp,lval));
	    break;
	 case "getAndIncrement" :
	    rslt = value.getActualValue(getClock());
	    lval += 1;
	    thisarg.setFieldValue(getTyper(),getClock(),valfld,CashewValue.numericValue(dtyp,lval));
	    break;
	 case "decrementAndGet" :
	    lval -= 1;
	    rslt = CashewValue.numericValue(dtyp,lval);
	    thisarg.setFieldValue(getTyper(),getClock(),valfld,rslt);
	    break;
	 case "incrementAndGet" :
	    lval += 1;
	    rslt = CashewValue.numericValue(dtyp,lval);
	    thisarg.setFieldValue(getTyper(),getClock(),valfld,rslt);
	    break;
	 case "addAndGet" :
	    lval += getLong(1);
	    rslt = CashewValue.numericValue(dtyp,lval);
	    thisarg.setFieldValue(getTyper(),getClock(),valfld,rslt);
	    break;
	 case "weakCompareAndSet" :
	 case "compareAndSet" :
	    if (lval == getLong(1)) {
	       thisarg.setFieldValue(getTyper(),getClock(),valfld,getValue(secondarg));
	       rslt = CashewValue.booleanValue(getTyper(),true);
	     }
	    else {
	       rslt = CashewValue.booleanValue(getTyper(),false);
	     }
	    break;
	 default :
	    return null;
       }
    }

   return CuminRunStatus.Factory.createReturn(rslt);
}




CuminRunStatus checkAtomicBooleanMethods() throws CashewException
{
   CashewValue rslt = null;

   CashewValue thisarg = getValue(0);
   String clsnm = thisarg.getDataType(getClock()).getName();
   String valfld = clsnm + ".value";

   synchronized (thisarg) {
      CashewValue value = thisarg.getFieldValue(getTyper(),getClock(),valfld);
      boolean bval = value.getBoolean(getClock());

      switch (getMethod().getName()) {
	 case "get" :
	    rslt = value.getActualValue(getClock());
	    break;
	 case "set" :
	 case "lazySet" :
	    thisarg.setFieldValue(getTyper(),getClock(),valfld,getValue(1));
	    break;
	 case "getAndSet" :
	    rslt = value.getActualValue(getClock());
	    thisarg.setFieldValue(getTyper(),getClock(),valfld,getValue(1));
	    break;
	 case "weakCompareAndSet" :
	 case "compareAndSet" :
	    if (bval == getBoolean(1)) {
	       thisarg.setFieldValue(getTyper(),getClock(),valfld,getValue(2));
	       rslt = CashewValue.booleanValue(getTyper(),true);
	     }
	    else {
	       rslt = CashewValue.booleanValue(getTyper(),false);
	     }
	    break;
	 default :
	    return null;
       }
    }

   return CuminRunStatus.Factory.createReturn(rslt);
}



/********************************************************************************/
/*                                                                              */
/*      Handle concurrent hash maps                                             */
/*                                                                              */
/********************************************************************************/

CuminRunStatus checkConcurrentHashMapMethods() throws CuminRunException, CashewException
{
   CashewValue rslt = null;
   
   if (getMethod().isStatic()) {
      switch (getMethod().getName()) {
         case "tabAt" :
           CashewValue a1 = getArrayValue(0);
           long idx = getLong(1);
           rslt = a1.getIndexValue(getClock(),(int) idx);
           break;
         case "casTabAt" :
            a1 = getArrayValue(0);
            int idxv = getInt(1);
            CashewValue a2 = getValue(2);
            CashewValue a3 = getValue(3);
            CashewValue v1 = a1.getIndexValue(getClock(),idxv).getActualValue(getClock());
            if (v1 == a2) {
               a1.setIndexValue(getClock(),idxv,a3);
               rslt = CashewValue.booleanValue(getTyper(),true);
             }
            else {
               rslt = CashewValue.booleanValue(getTyper(),false);
             }
            break;
         default :
            return null;
       }
    }
   else {
      return null;
    }
   
   return CuminRunStatus.Factory.createReturn(rslt);
}




/********************************************************************************/
/*                                                                              */
/*      Handle sun.misc.Unsafe methods                                          */
/*                                                                              */
/********************************************************************************/

synchronized CuminRunStatus checkUnsafeMethods() throws CuminRunException, CashewException
{
   CashewValue rslt = null;
   
   switch (getMethod().getName()) {
      case "compareAndSwapInt" :
         CashewValue cv1 = getValue(1);
         long off = getLong(2);
         int v1 = getInt(4);
         CashewValue v2 = getValue(5);
         JcompType typ = cv1.getDataType(getClock());
         String fld = null;
         if (typ.getName().equals("java.util.concurrent.ConcurrentHashMap")) {
            if (off == 20) {
               fld = "java.util.concurrent.ConcurrentHashMap.sizeCtl";
             }
          }
         if (fld == null) return null;
         CashewValue oldv = cv1.getFieldValue(getTyper(),getClock(),fld);
         int oldint = oldv.getNumber(getClock()).intValue();
         if (oldint != v1) rslt = CashewValue.booleanValue(getTyper(),false);
         else {
            cv1.setFieldValue(getTyper(),getClock(),fld,v2);
            rslt = CashewValue.booleanValue(getTyper(),true);
          }
         break;
      case "compareAndSwapLong" :
         cv1 = getValue(1);
         off = getLong(2);
         long lv1 = getLong(4);
         v2 = getValue(6);
         typ = cv1.getDataType(getClock());
         fld = null;
         if (typ.getName().equals("java.util.concurrent.ConcurrentHashMap")) {
            if (off == 24) {
               fld = "java.util.concurrent.ConcurrentHashMap.baseCount";
             }
          }
         if (fld == null) return null;
         oldv = cv1.getFieldValue(getTyper(),getClock(),fld);
         long oldlong = oldv.getNumber(getClock()).longValue();
         if (oldlong != lv1) rslt = CashewValue.booleanValue(getTyper(),false);
         else {
            cv1.setFieldValue(getTyper(),getClock(),fld,v2);
            rslt = CashewValue.booleanValue(getTyper(),true);
          }
         break;
      default :
         return null;
    }
   
   return CuminRunStatus.Factory.createReturn(rslt);
}

}	// end of class CuminConcurrentEvaluator




/* end of CuminConcurrentEvaluator.java */


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
import edu.brown.cs.seede.acorn.AcornLog;
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

CuminRunStatus checkAtomicIntMethods()
{
   CashewValue rslt = null;

   CashewValue thisarg = getValue(0);
   String clsnm = thisarg.getDataType(getClock()).getName();
   String valfld = clsnm + ".value";

   synchronized (thisarg) {
      CashewValue value = thisarg.getFieldValue(getClock(),valfld);
      long lval = value.getNumber(getClock()).longValue();
      JcompType dtyp = LONG_TYPE;
      int secondarg = 3;
      if (clsnm.contains("Integer")){
	 dtyp = INT_TYPE;
	 secondarg = 2;
       }

      switch (getMethod().getName()) {
	 case "get" :
	    rslt = value.getActualValue(getClock());
	    break;
	 case "set" :
	 case "lazySet" :
	    thisarg.setFieldValue(getClock(),valfld,getValue(1));
	    break;
	 case "getAndSet" :
	    rslt = value.getActualValue(getClock());
	    thisarg.setFieldValue(getClock(),valfld,getValue(1));
	    break;
	 case "getAndAdd" :
	    rslt = value.getActualValue(getClock());
	    lval += getLong(1);
	    thisarg.setFieldValue(getClock(),valfld,CashewValue.numericValue(dtyp,lval));
	    break;
	 case "getAndDecrement" :
	    rslt = value.getActualValue(getClock());
	    lval -= 1;
	    thisarg.setFieldValue(getClock(),valfld,CashewValue.numericValue(dtyp,lval));
	    break;
	 case "getAndIncrement" :
	    rslt = value.getActualValue(getClock());
	    lval += 1;
	    thisarg.setFieldValue(getClock(),valfld,CashewValue.numericValue(dtyp,lval));
	    break;
	 case "decrementAndGet" :
	    lval -= 1;
	    rslt = CashewValue.numericValue(dtyp,lval);
	    thisarg.setFieldValue(getClock(),valfld,rslt);
	    break;
	 case "incrementAndGet" :
	    lval += 1;
	    rslt = CashewValue.numericValue(dtyp,lval);
	    thisarg.setFieldValue(getClock(),valfld,rslt);
	    break;
	 case "addAndGet" :
	    lval += getLong(1);
	    rslt = CashewValue.numericValue(dtyp,lval);
	    thisarg.setFieldValue(getClock(),valfld,rslt);
	    break;
	 case "weakCompareAndSet" :
	 case "compareAndSet" :
	    if (lval == getLong(1)) {
	       thisarg.setFieldValue(getClock(),valfld,getValue(secondarg));
	       rslt = CashewValue.booleanValue(true);
	     }
	    else {
	       rslt = CashewValue.booleanValue(false);
	     }
	    break;
	 default :
	    return null;
       }
    }

   return CuminRunStatus.Factory.createReturn(rslt);
}




CuminRunStatus checkAtomicBooleanMethods()
{
   CashewValue rslt = null;

   CashewValue thisarg = getValue(0);
   String clsnm = thisarg.getDataType(getClock()).getName();
   String valfld = clsnm + ".value";

   synchronized (thisarg) {
      CashewValue value = thisarg.getFieldValue(getClock(),valfld);
      boolean bval = value.getBoolean(getClock());

      switch (getMethod().getName()) {
	 case "get" :
	    rslt = value.getActualValue(getClock());
	    break;
	 case "set" :
	 case "lazySet" :
	    thisarg.setFieldValue(getClock(),valfld,getValue(1));
	    break;
	 case "getAndSet" :
	    rslt = value.getActualValue(getClock());
	    thisarg.setFieldValue(getClock(),valfld,getValue(1));
	    break;
	 case "weakCompareAndSet" :
	 case "compareAndSet" :
	    if (bval == getBoolean(1)) {
	       thisarg.setFieldValue(getClock(),valfld,getValue(2));
	       rslt = CashewValue.booleanValue(true);
	     }
	    else {
	       rslt = CashewValue.booleanValue(false);
	     }
	    break;
	 default :
	    return null;
       }
    }

   return CuminRunStatus.Factory.createReturn(rslt);
}




}	// end of class CuminConcurrentEvaluator




/* end of CuminConcurrentEvaluator.java */


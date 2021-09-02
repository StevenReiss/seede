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
import edu.brown.cs.ivy.jcomp.JcompTyper;
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
   String clsnm = thisarg.getDataType(getSession(),getClock(),null).getName();
   String valfld = clsnm + ".value";
   JcompTyper typer = getTyper();
   CashewValueSession sess = getSession();

   synchronized (thisarg) {
      CashewValue value = thisarg.getFieldValue(sess,typer,getClock(),valfld);
      long lval = value.getNumber(sess,getClock()).longValue();
      JcompType dtyp = typer.LONG_TYPE;
      int secondarg = 3;
      if (clsnm.contains("Integer")){
	 dtyp = typer.INT_TYPE;
	 secondarg = 2;
       }

      switch (getMethod().getName()) {
	 case "get" :
	    rslt = value.getActualValue(sess,getClock());
	    break;
	 case "set" :
	 case "lazySet" :
	    thisarg.setFieldValue(sess,typer,getClock(),valfld,getValue(1));
	    break;
	 case "getAndSet" :
	    rslt = value.getActualValue(sess,getClock());
	    thisarg.setFieldValue(sess,typer,getClock(),valfld,getValue(1));
	    break;
	 case "getAndAdd" :
	    rslt = value.getActualValue(sess,getClock());
	    lval += getLong(1);
	    thisarg.setFieldValue(sess,typer,getClock(),valfld,CashewValue.numericValue(typer,dtyp,lval));
	    break;
	 case "getAndDecrement" :
	    rslt = value.getActualValue(sess,getClock());
	    lval -= 1;
	    thisarg.setFieldValue(sess,typer,getClock(),valfld,CashewValue.numericValue(typer,dtyp,lval));
	    break;
	 case "getAndIncrement" :
	    rslt = value.getActualValue(sess,getClock());
	    lval += 1;
	    thisarg.setFieldValue(sess,typer,getClock(),valfld,CashewValue.numericValue(typer,dtyp,lval));
	    break;
	 case "decrementAndGet" :
	    lval -= 1;
	    rslt = CashewValue.numericValue(typer,dtyp,lval);
	    thisarg.setFieldValue(sess,typer,getClock(),valfld,rslt);
	    break;
	 case "incrementAndGet" :
	    lval += 1;
	    rslt = CashewValue.numericValue(typer,dtyp,lval);
	    thisarg.setFieldValue(sess,typer,getClock(),valfld,rslt);
	    break;
	 case "addAndGet" :
	    lval += getLong(1);
	    rslt = CashewValue.numericValue(typer,dtyp,lval);
	    thisarg.setFieldValue(sess,typer,getClock(),valfld,rslt);
	    break;
	 case "weakCompareAndSet" :
	 case "compareAndSet" :
	    if (lval == getLong(1)) {
	       thisarg.setFieldValue(sess,typer,getClock(),valfld,getValue(secondarg));
	       rslt = CashewValue.booleanValue(typer,true);
	     }
	    else {
	       rslt = CashewValue.booleanValue(typer,false);
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
   
   CashewValueSession sess = getSession();
   JcompTyper typer = getTyper();

   CashewValue thisarg = getValue(0);
   String clsnm = thisarg.getDataType(sess,getClock(),null).getName();
   String valfld = clsnm + ".value";

   synchronized (thisarg) {
      CashewValue value = thisarg.getFieldValue(sess,typer,getClock(),valfld);
      boolean bval = value.getBoolean(sess,getClock());

      switch (getMethod().getName()) {
	 case "get" :
	    rslt = value.getActualValue(sess,getClock());
	    break;
	 case "set" :
	 case "lazySet" :
	    thisarg.setFieldValue(sess,typer,getClock(),valfld,getValue(1));
	    break;
	 case "getAndSet" :
	    rslt = value.getActualValue(sess,getClock());
	    thisarg.setFieldValue(sess,typer,getClock(),valfld,getValue(1));
	    break;
	 case "weakCompareAndSet" :
	 case "compareAndSet" :
	    if (bval == getBoolean(1)) {
	       thisarg.setFieldValue(sess,typer,getClock(),valfld,getValue(2));
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
/*										*/
/*	Handle concurrent hash maps						*/
/*										*/
/********************************************************************************/

CuminRunStatus checkConcurrentHashMapMethods() throws CuminRunException, CashewException
{
   CashewValue rslt = null;
   CashewValueSession sess = getSession();

   if (getMethod().isStatic()) {
      switch (getMethod().getName()) {
	 case "tabAt" :
	   CashewValue a1 = getArrayValue(0);
	   long idx = getLong(1);
	   rslt = a1.getIndexValue(sess,getClock(),(int) idx);
	   break;
	 case "casTabAt" :
	    a1 = getArrayValue(0);
	    int idxv = getInt(1);
	    CashewValue a2 = getValue(2);
	    CashewValue a3 = getValue(3);
	    CashewValue v1 = a1.getIndexValue(sess,getClock(),idxv).getActualValue(sess,getClock());
	    if (v1 == a2) {
	       a1.setIndexValue(sess,getClock(),idxv,a3);
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
/*										*/
/*	Handle sun.misc.Unsafe methods						*/
/*										*/
/********************************************************************************/

synchronized CuminRunStatus checkUnsafeMethods() throws CuminRunException, CashewException
{
   CashewValue rslt = null;
   JcompTyper typer = getTyper();
   CashewValueSession sess = getSession();

   switch (getMethod().getName()) {
      case "compareAndSetInt" :
      case "compareAndSwapInt" :
	 CashewValue cv1 = getValue(1);
	 long off = getLong(2);
	 int v1 = getInt(4);
	 CashewValue v2 = getValue(5);
	 JcompType typ = cv1.getDataType(sess,getClock(),typer);
	 String fld = null;
	 if (typ.getName().equals("java.util.concurrent.ConcurrentHashMap")) {
	    if (off == 20) {
	       fld = "java.util.concurrent.ConcurrentHashMap.sizeCtl";
	     }
	  }
	 if (fld == null) return null;
	 CashewValue oldv = cv1.getFieldValue(getSession(),getTyper(),getClock(),fld);
	 int oldint = oldv.getNumber(sess,getClock()).intValue();
	 if (oldint != v1) rslt = CashewValue.booleanValue(getTyper(),false);
	 else {
	    cv1.setFieldValue(sess,typer,getClock(),fld,v2);
	    rslt = CashewValue.booleanValue(getTyper(),true);
	  }
	 break;
      case "compareAndSetLong" :
      case "compareAndSwapLong" :
	 cv1 = getValue(1);
	 off = getLong(2);
	 long lv1 = getLong(4);
	 v2 = getValue(6);
	 typ = cv1.getDataType(sess,getClock(),typer);
	 fld = null;
	 if (typ.getName().equals("java.util.concurrent.ConcurrentHashMap")) {
	    if (off == 24) {
	       fld = "java.util.concurrent.ConcurrentHashMap.baseCount";
	     }
	  }
	 if (fld == null) return null;
	 oldv = cv1.getFieldValue(sess,typer,getClock(),fld);
	 long oldlong = oldv.getNumber(sess,getClock()).longValue();
	 if (oldlong != lv1) rslt = CashewValue.booleanValue(getTyper(),false);
	 else {
	    cv1.setFieldValue(sess,typer,getClock(),fld,v2);
	    rslt = CashewValue.booleanValue(typer,true);
	  }
	 break;
      default :
	 return null;
    }

   return CuminRunStatus.Factory.createReturn(rslt);
}



synchronized CuminRunStatus checkSunToolkitMethods() throws CuminRunException, CashewException
{
   CashewValue rslt = null;
   
   switch (getMethod().getName()) {
      case "awtLock" :
      case "awtLockWait" :
         break;
      case "awtTryLock" :
         rslt = CashewValue.booleanValue(getTyper(),true);
         break;
      case "awtUnlock" :
         break;
      case "awtLockNotify" :
      case "awtLockNotifyAll" :
         break;
      case "isAWTLockHeldByCurrentThread" : 
         rslt = CashewValue.booleanValue(getTyper(),true);
         break;
      default :
         return null;
    }
   
   return CuminRunStatus.Factory.createReturn(rslt);
}

}	// end of class CuminConcurrentEvaluator




/* end of CuminConcurrentEvaluator.java */


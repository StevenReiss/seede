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

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import edu.brown.cs.ivy.jcomp.JcompType;
import edu.brown.cs.ivy.jcomp.JcompTyper;
import edu.brown.cs.seede.acorn.AcornLog;
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
      CashewValue value = thisarg.getFieldValue(sess,typer,getClock(),valfld,getContext());
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
      CashewValue value = thisarg.getFieldValue(sess,typer,getClock(),valfld,getContext());
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
	    CashewValue v1 = a1.getIndexValue(sess,
                  getClock(),idxv).getActualValue(sess,
                        getClock());
	    if (v1 == a2) {
	       a1.setIndexValue(sess,getClock(),idxv,a3);
	       rslt = CashewValue.booleanValue(getTyper(),true);
	     }
	    else {
	       rslt = CashewValue.booleanValue(getTyper(),false);
	     }
	    break;
         case "setTabAt" :
            a1 = getArrayValue(0);
	    idxv = getInt(1);
	    a2 = getValue(2);
            a1.setIndexValue(sess,getClock(),idxv,a2);
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
	 String fld = getFieldAtOffset(typ,(int) off);
	 if (fld == null) return null;
	 CashewValue oldv = cv1.getFieldValue(getSession(),getTyper(),getClock(),fld,getContext());
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
	 fld = getFieldAtOffset(typ,(int) off);
	 if (fld == null) return null;
	 oldv = cv1.getFieldValue(sess,typer,getClock(),fld,getContext());
	 long oldlong = oldv.getNumber(sess,getClock()).longValue();
	 if (oldlong != lv1) rslt = CashewValue.booleanValue(getTyper(),false);
	 else {
	    cv1.setFieldValue(sess,typer,getClock(),fld,v2);
	    rslt = CashewValue.booleanValue(typer,true);
	  }
	 break;
      case "fullFence" :
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


synchronized CuminRunStatus checkVarHandleMethods() throws CuminRunException, CashewException
{
   CashewValue rslt = null;
   JcompTyper typer = getTyper();
   CashewValueSession sess = getSession();
   
   switch (getMethod().getName()) {
      case "set" :
      case "setRelease" :
      case "setVolatile" :
         CashewValue tgtval = getValue(1);
         JcompType tgttyp = tgtval.getDataType(sess,getClock(),typer);
         if (tgttyp.isArrayType()) {
            int index = getInt(2);
            CashewValue srcval = getValue(3);
            JcompType srctyp = srcval.getDataType(sess,getClock(),typer);
            if (tgttyp.getBaseType().isByteType()) {
               if (srctyp.isLongType()) {
                  byte [] buf = new byte[8];
                  ByteBuffer bbuf = ByteBuffer.wrap(buf);
                  bbuf.putLong(getLong(3));
                  for (int i = 0; i < 8; ++i) {
                     CashewValue bval = CashewValue.numericValue(typer,
                           typer.BYTE_TYPE,buf[i]);
                     tgtval.setIndexValue(sess,getClock(),index+i,bval);
                   }
                }
               else if (srctyp.isIntType()) {
                  byte [] buf = new byte[4];
                  ByteBuffer bbuf = ByteBuffer.wrap(buf);
                  bbuf.putInt(getInt(3));
                  for (int i = 0; i < 4; ++i) {
                     CashewValue bval = CashewValue.numericValue(typer,
                           typer.BYTE_TYPE,buf[i]);
                     tgtval.setIndexValue(sess,getClock(),index+i,bval);
                   }
                }
               else {
                  AcornLog.logE("CUMIN","SETTING ARRAY " + tgttyp + " NOT IMPLEMENTED");
                  return null;      
                }
               break;
             }
          }
         CashewValue thisarg = getValue(0);
         CashewValue setval = getValue(2);
         String fld = getOffsetField(thisarg);
         CashewValue fldoffset = thisarg.getFieldValue(getSession(),
               getTyper(),getClock(),
               fld,getContext());
         int fldoff = fldoffset.getNumber(getSession(),getClock()).intValue();
         JcompType typ = tgtval.getDataType(getSession(),getClock(),getTyper());
         String js = getFieldAtOffset(typ,fldoff);
         if (js != null) {
            tgtval.setFieldValue(getSession(),getTyper(),
                  getClock(),js,setval);
          }
         break;
      case "compareAndSet" :
      case "weakCompareAndSet" :
      case "weakCompareAndSetAcquire" :
      case "weakCompareAndSetPlain" :
      case "weakCompareAndSetRelease" :
         thisarg = getValue(0);
         tgtval = getValue(1);
         setval = getValue(3);
         fld = getOffsetField(thisarg);
         fldoffset = thisarg.getFieldValue(getSession(),getTyper(),getClock(),
               fld,getContext());
         fldoff = fldoffset.getNumber(getSession(),getClock()).intValue();
         typ = tgtval.getDataType(getSession(),getClock(),getTyper());
         js = getFieldAtOffset(typ,fldoff);
         if (js != null) {
            tgtval.setFieldValue(getSession(),getTyper(),getClock(),js,setval);
          }
         rslt = CashewValue.booleanValue(getTyper(),true);
         break;
      case "get" :
      case "getAcquire" :
         thisarg = getValue(0);
         tgtval = getValue(1);
         fld = getOffsetField(thisarg);
         fldoffset = thisarg.getFieldValue(getSession(),getTyper(),getClock(),
               fld,getContext());
         fldoff = fldoffset.getNumber(getSession(),getClock()).intValue();
         typ = tgtval.getDataType(getSession(),getClock(),getTyper());
         js = getFieldAtOffset(typ,fldoff);
         if (js != null) {
            rslt = tgtval.getFieldValue(getSession(),getTyper(),getClock(),js,getContext());
          }
         break; 
      case "acquireFence" :
      case "releaseFence" :
      case "loadLoadFence" :
      case "storeStoreFence" :
      case "fullFence" :
         break;
         
      default :
         AcornLog.logE("Unknown VarHandle operation " + getMethod().getName());
         break;
    }
    
   return CuminRunStatus.Factory.createReturn(rslt);
}


private String getOffsetField(CashewValue thisarg)
{
   String fld = "java.lang.invoke.VarHandleObjects.FieldInstanceReadOnly.fieldOffset";
   JcompType jt = thisarg.getDataType(getSession(),getClock(),getTyper());
   for (String s : jt.getFields().keySet()) {
      if (s.endsWith(".fieldOffset")) {
         fld = s;
         break;
       }
    }
   return fld;
}



private static Map<String,Map<Integer,String>> field_offset_map;

static {
   field_offset_map = new HashMap<>();
   addFields("java.util.concurrent.ConcurrentHashMap",20,"sizeCtl",24,"baseCount",32,"transferIndex",36,"cellsBusy");
   addFields("java.util.concurrent.ConcurrentHashMap.CounterCell",144,"value");
   addFields("java.util.concurrent.ConcurrentLinkedQueue",12,"head",16,"tail");
   addFields("java.util.concurrent.ConcurrentLinkedQueue.Node",12,"item",16,"next");
   addFields("java.util.concurrent.CompletableFuture",12,"result",16,"stack");
   addFields("java.util.concurrent.CompletableFuture.Completion",16,"next");
   addFields("java.util.concurrent.ConcurrentLinkedDeque",12,"head",16,"tail");
   addFields("java.util.concurrent.ConcurrentLinkedDeque.Node",12,"prev",20,"next",16,"item");
   addFields("java.util.concurrent.ConcurrentSkipListMap",24,"head",28,"adder");
   addFields("java.util.concurrent.ConcurrentSkipListMap.Node",20,"next",16,"val");
   addFields("java.util.concurrent.ConcurrentSkipListMap.Index",20,"right");
   addFields("java.util.concurrent.CountedCompleter",16,"pending");
   addFields("java.util.concurrent.Exchanger",12,"bound",24,"slot");
   addFields("java.util.concurrent.Exchanger.Node",160,"match");
   addFields("java.util.concurrent.ForkJoinPool",192,"ctl",36,"mode");
   addFields("java.util.concurrent.ForkJoinPool.WorkQueue",12,"phase",32,"base",36,"top");
   addFields("java.util.concurrent.ForkJoinTask",12,"status");
   addFields("java.util.concurrent.FutureTask",12,"state",24,"runner",28,"waiters");
   addFields("java.util.concurrent.LinkedTransferQueue",16,"head",20,"tail",12,"sweepVotes");
   addFields("java.util.concurrent.LinkedTransferQueue.Node",16,"item",20,"next",24,"waiter");
   addFields("java.util.concurrent.Phaser",16,"state");
   addFields("java.util.concurrent.PriorityBlockingQueue",16,"allocationSpinLock");
   addFields("java.util.concurrent.SubmissionPublisher.BufferedSubscription",32,"ctl",92,"demand");
   addFields("java.util.concurrent.SynchronousQueue.TransferStack.Snode",12,"next",20,"match");
   addFields("java.util.concurrent.SynchronousQueue.TransferStack",12,"head");
   addFields("java.util.concurrent.SynchronousQueue.TransferQueue.Qnode",12,"next",20,"item");
   addFields("java.util.concurrent.SynchronousQueue.TransferQueue",28,"cleanMe");
   addFields("java.util.concurrent.Thread",232,"threadLocalRandomSeed",240,"threadLocalRandomProbe",
         244,"threadLocalRandomSecondardSeed",80,"threadLocals",84,"inheritableThreadLocals",
         76,"inheritedAccessControlContext");
   addFields("java.util.concurrent.locks.AbstractQueuedSynchronizer",16,"state",8,"tail",0,"head");
}


private static void addFields(String cls,Object... args)
{
   Map<Integer,String> fmap = new HashMap<>();
   field_offset_map.put(cls,fmap);
   for (int i = 0; i < args.length; i += 2) {
      Integer off = (Integer) args[i];
      String nm = (String) args[i+1];
      if (nm.contains(".")) fmap.put(off,nm);
      else fmap.put(off,cls + "." + nm);
    }
}



private String getFieldAtOffset(JcompType typ,int off)
{
   for (JcompType st = typ; st != null; st = st.getSuperType()) { 
      Map<Integer,String> flds = field_offset_map.get(st.getName());
      if (flds != null) {
         String fld = flds.get(off);
         if (fld != null) return fld;
       }
    }
   
   AcornLog.logE("CUMIN","Unknown field offset " + typ.getName() + " " + off);
   
   return null;
}



/********************************************************************************/
/*                                                                              */
/*      Blocker methods                                                         */
/*                                                                              */
/********************************************************************************/

CuminRunStatus checkBlockerMethods() throws CuminRunException
{
   CashewValue rslt = null;
   
   switch (getMethod().getName()) {
      case "begin" :
         rslt = CashewValue.booleanValue(getTyper(),true);
         break;
      case "end" :
         break;
      default :
         return null;
    }
   
   return CuminRunStatus.Factory.createReturn(rslt);
}


}	// end of class CuminConcurrentEvaluator




/* end of CuminConcurrentEvaluator.java */


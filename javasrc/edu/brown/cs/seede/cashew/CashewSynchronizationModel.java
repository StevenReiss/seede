/********************************************************************************/
/*                                                                              */
/*              CashewSynchronizationModel.java                                 */
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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CashewSynchronizationModel implements CashewConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private Map<CashewValue,SynchData> synch_locks;




/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

public CashewSynchronizationModel()
{
   synch_locks = new HashMap<>();
}



/********************************************************************************/
/*                                                                              */
/*      Setup methods                                                           */
/*                                                                              */
/********************************************************************************/

synchronized public void clear()
{
   synch_locks.clear();
   // might want to hold locks known by the debugger
}



synchronized public void reset()
{
   clear();
}


/********************************************************************************/
/*                                                                              */
/*      Syncrhonization methods                                                 */
/*                                                                              */
/********************************************************************************/

public void synchEnter(CashewValue thread,CashewValue object)
{
    SynchData sd = getLockData(object);  
    sd.lock(thread);
}


public void synchExit(CashewValue thread,CashewValue object)
{
   SynchData sd = getLockData(object);  
   sd.unlock();
}


public void synchWait(CashewValue thread,CashewValue object,long timeout) 
        throws InterruptedException
{
   SynchData sd = getLockData(object);  
   sd.await(thread,timeout);
}


public void synchNotify(CashewValue object,boolean all) 
{
   SynchData sd = getLockData(object);  
   sd.signal(all);
}


public boolean holdsLock(CashewValue thread,CashewValue object)
{
   SynchData sd = getLockData(object);
   return thread.equals(sd.getCurrentThread());
}



private synchronized SynchData getLockData(CashewValue obj)
{
   SynchData sd = synch_locks.get(obj);
   if (sd == null) {
      sd = new SynchData();
      synch_locks.put(obj,sd);
    }
   
   return sd;
}



/********************************************************************************/
/*                                                                              */
/*      Lock representation                                                     */
/*                                                                              */
/********************************************************************************/

private static class SynchData {

   private Lock         our_lock;
   private Condition    lock_cond;
   private CashewValue  current_thread;
   private int          current_count;
   
   SynchData() {
      our_lock = new ReentrantLock();
      lock_cond = our_lock.newCondition();
      current_thread = null;
      current_count = 0;
    }
   
   void lock(CashewValue thrd) {   
      our_lock.lock(); 
      current_thread = thrd;
      ++current_count;
    }
   
   void unlock() {
      if (current_count == 0) return;
      --current_count;
      if (current_count == 0) current_thread = null;
      our_lock.unlock(); 
    }
   
   void await(CashewValue thread,long time) throws InterruptedException {
      if (time == 0) lock_cond.await();
      else lock_cond.await(time,TimeUnit.MILLISECONDS);
      current_thread = thread;
    }
   
   void signal(boolean all) {
      if (all) lock_cond.signalAll(); 
      else lock_cond.signal();
    }
   
   CashewValue getCurrentThread()               { return current_thread; }
   
}       // end of inner class LockData




}       // end of class CashewSynchronizationModel




/* end of CashewSynchronizationModel.java */


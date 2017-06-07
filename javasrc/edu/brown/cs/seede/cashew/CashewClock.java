/********************************************************************************/
/*                                                                              */
/*              CashewClock.java                                                */
/*                                                                              */
/*      Execeution time representation                                          */
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

import java.util.concurrent.locks.ReentrantLock;

public class CashewClock implements CashewConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private long            time_count;
private long            max_time;
private ReentrantLock   freeze_lock;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

public CashewClock()
{
   time_count = 0;
   max_time = 0;
   freeze_lock = new ReentrantLock();
}



/********************************************************************************/
/*                                                                              */
/*      Action methods                                                          */
/*                                                                              */
/********************************************************************************/

public long tick()
{
   if (freeze_lock.isHeldByCurrentThread()) return time_count;
   
   ++time_count;
   if (time_count > max_time) max_time = time_count;
   return time_count;
}


public long getTimeValue()                      { return time_count; }




public synchronized void freezeTime()
{
   freeze_lock.lock();
}




public void unfreezeTime()
{
   freeze_lock.unlock();
}



}       // end of class CashewClock




/* end of CashewClock.java */


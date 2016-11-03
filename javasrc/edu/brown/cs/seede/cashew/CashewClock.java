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



public class CashewClock implements CashewConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private long            time_count;
private long            max_time;


/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

CashewClock()
{
   time_count = 0;
   max_time = 0;
}



/********************************************************************************/
/*                                                                              */
/*      Action methods                                                          */
/*                                                                              */
/********************************************************************************/

public long tick()
{
   ++time_count;
   if (time_count > max_time) max_time = time_count;
   return time_count;
}


public long getTimeValue()                      { return time_count; }

public void setTime(long v) throws CashewException
{
   if (v > max_time || v < 0) throw new CashewException("Invalid time");
   time_count = v;
}



}       // end of class CashewClock




/* end of CashewClock.java */


/********************************************************************************/
/*                                                                              */
/*              SesameThreadContext.java                                        */
/*                                                                              */
/*      Context information for thread-specific information                     */
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



package edu.brown.cs.seede.sesame;

import edu.brown.cs.seede.cashew.CashewContext;
import edu.brown.cs.seede.cashew.CashewValue;

class SesameThreadContext extends CashewContext implements SesameConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private String  thread_id;
private SesameSession for_session;




/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

SesameThreadContext(String tid,SesameSession sess,SesameContext gbl)
{
   super("THREAD_CONTEXT",gbl);
   
   thread_id = tid;
   for_session = sess;
}



/********************************************************************************/
/*                                                                              */
/*      Overridden methods                                                      */
/*                                                                              */
/********************************************************************************/

public CashewValue findStaticFieldReference(String name,String type)
{
   if (name.equals(CURRENT_THREAD_FIELD)) {
      CashewValue cv = findReference(name);
      if (cv != null) return cv;
      SesameValueData svd = for_session.evaluateData("java.lang.Thread.currentThread()",thread_id);
      if (svd != null) {
         cv = svd.getCashewValue();
         if (cv != null) {
            define(name,cv);
            return cv;
          }
       }
    }
   
   return super.findStaticFieldReference(name,type);
}



}       // end of class SesameThreadContext




/* end of SesameThreadContext.java */


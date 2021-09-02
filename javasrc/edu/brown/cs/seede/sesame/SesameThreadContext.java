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

import edu.brown.cs.ivy.jcomp.JcompTyper;
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
private String  thread_name;
private SesameSession for_session;




/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

SesameThreadContext(String tid,String tnm,SesameSession sess,SesameContext gbl)
{
   super("THREAD_CONTEXT",null,gbl);
   
   thread_id = tid;
   thread_name = tnm;
   for_session = sess;
}



/********************************************************************************/
/*                                                                              */
/*      Overridden methods                                                      */
/*                                                                              */
/********************************************************************************/

@Override public CashewValue evaluate(String expr,String tid)
{
   if (parent_context != null) {
      if (tid == null) tid = thread_id;
      return parent_context.evaluate(expr,tid);
    }
   
   return null;
}



public CashewValue findStaticFieldReference(JcompTyper typer,String name,String type)
{
   if (name.equals(CURRENT_THREAD_FIELD)) {
      CashewValue cv = findActualReference(name);
      if (cv != null) return cv;
      SesameValueData svd = for_session.evaluateData("java.lang.Thread.currentThread()",thread_id,false);
      if (svd != null) {
         SesameSessionLaunch ssl = (SesameSessionLaunch) for_session;
         cv = svd.getCashewValue(ssl);
         if (cv != null) {
            define(name,cv);
            return cv;
          }
       }
    }
   else if (name.equals(CURRENT_THREAD_NAME_FIELD)) {
      return CashewValue.stringValue(typer,typer.STRING_TYPE,thread_name);
    }
   
   return super.findStaticFieldReference(typer,name,type);
}


public String findNameForValue(CashewValue cv)
{
   return findNameForValue(cv,thread_id);
}



}       // end of class SesameThreadContext




/* end of SesameThreadContext.java */


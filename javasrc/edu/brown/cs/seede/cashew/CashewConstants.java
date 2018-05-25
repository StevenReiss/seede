/********************************************************************************/
/*                                                                              */
/*              CashewConstants.java                                            */
/*                                                                              */
/*      Constants for Seede Cache and Value manager                             */
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




public interface CashewConstants
{


/********************************************************************************/
/*                                                                              */
/*      Kinds of values                                                         */
/*                                                                              */
/********************************************************************************/

enum CashewValueKind {
   UNKNOWN,
   PRIMITIVE,
   STRING,
   CLASS,
   OBJECT,
   ARRAY
};



/********************************************************************************/
/*                                                                              */
/*      Special fields                                                          */
/*                                                                              */
/********************************************************************************/

String HASH_CODE_FIELD = "@hashCode";
String TO_STRING_FIELD = "@toString";
String CURRENT_THREAD_FIELD = "@currentThread";
String CURRENT_THREAD_NAME_FIELD = "@currentThreadName";





/********************************************************************************/
/*                                                                              */
/*      Deferred value interface                                                */
/*                                                                              */
/********************************************************************************/

interface CashewDeferredValue {
   
   CashewValue getValue();
   
}









}       // end of interface CashewConstants




/* end of CashewConstants.java */


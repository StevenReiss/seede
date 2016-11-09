/********************************************************************************/
/*                                                                              */
/*              CuminRunError.java                                              */
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



package edu.brown.cs.seede.cumin;

import edu.brown.cs.seede.cashew.CashewValue;

class CuminRunError extends Error
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

enum Reason { ERROR, EXCEPTION, BREAKPOINT, TIMEOUT, 
   STEP_END, BREAK, CONTINUE, RETURN };

private Reason          throw_reason;
private CashewValue     associated_value;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

CuminRunError(Reason r,String msg,Throwable cause,CashewValue v) 
{
   super(msg,cause);
   
   throw_reason = r;
   associated_value = v;
}

CuminRunError(Reason r)
{
   this(r,r.toString(),null,null);
}

CuminRunError(Reason r,String label)
{
   this(r,label,null,null);
}


CuminRunError(Throwable t)
{
   this(Reason.ERROR,t.getMessage(),t,null);
}
   

CuminRunError(Reason r,CashewValue v)
{
   this(r,r.toString(),null,null);
   // v can be an throwable value (r = EXCEPTION) or a return value (r = RETURN)
}


/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

Reason getReason()                      { return throw_reason; }

CashewValue getValue()                  { return associated_value; }


}       // end of class CuminRunError




/* end of CuminRunError.java */


/********************************************************************************/
/*										*/
/*		CuminRunError.java						*/
/*										*/
/*	description of class							*/
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

import edu.brown.cs.seede.cashew.CashewValue;

public class CuminRunException extends Exception implements CuminRunStatus
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private Reason		throw_reason;
private transient CashewValue associated_value;
private transient CuminRunner call_value;

private final static long serialVersionUID = 1;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

CuminRunException(Reason r,String msg,Throwable cause,CashewValue v)
{
   super(msg,cause);

   throw_reason = r;
   associated_value = v;
   call_value = null;
}


CuminRunException(Reason r) 
{
   this(r,r.toString(),null,null);
}


CuminRunException(Reason r,String label)
{
   this(r,label,null,null);
}


public CuminRunException(Throwable t)
{
   this(Reason.ERROR,t.getMessage(),t,null);
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

@Override public Reason getReason()		{ return throw_reason; }

@Override public CashewValue getValue()	{ return associated_value; }

@Override public CuminRunner getCallRunner()	{ return call_value; }



}	// end of class CuminRunError




/* end of CuminRunError.java */


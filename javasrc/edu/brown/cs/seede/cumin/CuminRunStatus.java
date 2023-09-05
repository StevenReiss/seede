/********************************************************************************/
/*										*/
/*		CuminRunStatus.java						*/
/*										*/
/*	General Status return from execution					*/
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
import edu.brown.cs.seede.cashew.CashewContext;
import edu.brown.cs.seede.cashew.CashewValue;
import edu.brown.cs.seede.cashew.CashewConstants.CashewValueSession;

public interface CuminRunStatus extends CuminConstants
{



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

Reason getReason();

CashewValue getValue();

CuminRunner getCallRunner();

String getMessage();

Throwable getCause();




/********************************************************************************/
/*										*/
/*	Factory class								*/
/*										*/
/********************************************************************************/

public class Factory {

   public static CuminRunStatus createReturn() {
      return createReturn(null);
    }

   public static CuminRunStatus createReturn(CashewValue cv) {
      return new CuminRunValue(Reason.RETURN,cv);
    }

   public static CuminRunStatus createCall(CuminRunner cr) {
      return new CuminRunValue(cr);
    }

   public static CuminRunStatus createHalt() {
      return new CuminRunValue(Reason.HALTED);
    }

   public static CuminRunStatus createWait() {
      return new CuminRunValue(Reason.WAIT);
    }

   public static CuminRunStatus createStopped() {
      return new CuminRunValue(Reason.STOPPED);
    }

   public static CuminRunStatus createException(CashewValueSession sess,CashewValue cv) {
      return new CuminRunValue(Reason.EXCEPTION,cv,cv.getDataType(sess,null,null).getName());
    }

   public static CuminRunStatus createBreak(String id) {
      return new CuminRunValue(Reason.BREAK,id);
    }

   public static CuminRunStatus createContinue(String id) {
      return new CuminRunValue(Reason.CONTINUE,id);
    }

   public static CuminRunStatus createTimeout(CashewValueSession sess,CashewContext ctx,JcompTyper typer) {
      JcompType etyp = typer.findSystemType("java.lang.Error");
      CashewValue cv = CashewValue.objectValue(sess,ctx,typer,etyp);
      return new CuminRunException(Reason.EXCEPTION,"SEEDE_TIMEOUT",null,cv);  
   // return new CuminRunValue(Reason.TIMEOUT);
    }


   public static CuminRunException createStackOverflow(CashewValueSession sess,CashewContext ctx,JcompTyper typer) {
   // return new CuminRunException(Reason.STACK_OVERFLOW);
      JcompType etyp = typer.findSystemType("java.lang.StackOverflowError");
      CashewValue cv = CashewValue.objectValue(sess,ctx,typer,etyp);
      return new CuminRunException(Reason.EXCEPTION,etyp.toString(),null,cv);
   }

   public static CuminRunException createCompilerError() {
      return new CuminRunException(Reason.COMPILER_ERROR,"Compiler error");
    }

   public static CuminRunException createCompilerError(String msg) {
      return new CuminRunException(Reason.COMPILER_ERROR,"Compiler error: " + msg);
    }

   public static CuminRunException createError(String txt) {
      return new CuminRunException(Reason.ERROR,txt);
    }

   public static CuminRunException createError(Throwable t) {
      return new CuminRunException(t);
    }


}	// end of inner class Factory



}	// end of interface CuminRunStatus




/* end of CuminRunStatus.java */




























































































































































































































































































































































































































































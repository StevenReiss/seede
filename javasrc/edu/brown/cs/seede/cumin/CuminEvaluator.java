/********************************************************************************/
/*										*/
/*		CuminEvaluator.java						*/
/*										*/
/*	Expression evaluation logic						*/
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import edu.brown.cs.ivy.jcode.JcodeClass;
import edu.brown.cs.ivy.jcode.JcodeMethod;
import edu.brown.cs.ivy.jcomp.JcompType;
import edu.brown.cs.ivy.jcomp.JcompTyper;
import edu.brown.cs.seede.acorn.AcornLog;
import edu.brown.cs.seede.cashew.CashewClock;
import edu.brown.cs.seede.cashew.CashewConstants;
import edu.brown.cs.seede.cashew.CashewException;
import edu.brown.cs.seede.cashew.CashewValue;

class CuminEvaluator implements CuminConstants, CashewConstants
{

/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private static Map<Object,Map<String,CashewValue>> cached_values;


static {
   cached_values = new WeakHashMap<>();
}


/********************************************************************************/
/*										*/
/*	Arithmetic Expressions							*/
/*										*/
/********************************************************************************/

static CashewValue evaluate(CuminRunner runner,JcompTyper typer,
      CashewClock cc,CuminOperator op,CashewValue v1,CashewValue v2)
	throws CuminRunException
{
   try {
      return evaluateUnchecked(runner,typer,cc,op,v1,v2);
    }
   catch (CuminRunException e) {
      throw e;
    }
   catch (Throwable t) {
      AcornLog.logE("Problem in evaluation",t);
      throw CuminRunStatus.Factory.createCompilerError();
    }
}





static CashewValue evaluateUnchecked(CuminRunner runner,JcompTyper typer,CashewClock cc,CuminOperator op,
      CashewValue v1,CashewValue v2)
	throws CuminRunException, CashewException
{
   CashewValue rslt = null;

   v1 = v1.getActualValue(cc);
   v2 = v2.getActualValue(cc);

   JcompType t1 = v1.getDataType(cc,typer);
   JcompType t2 = v2.getDataType(cc,typer);
   boolean isstr = t1.isStringType() || t2.isStringType();

   boolean dounbox = false;
   switch (op) {
      case EQL :
      case NEQ :
	 if (t1.isPrimitiveType() || t2.isPrimitiveType()) dounbox = true;
	 break;
      case ADD :
	 if (!isstr) dounbox = true;
	 break;
      default :
	 dounbox = true;
	 break;
    }

   if (dounbox) {
      if (t1.isClassType() || t2.isClassType()) {
	 v1 = unboxValue(typer,cc,v1);
	 v2 = unboxValue(typer,cc,v2);
	 t1 = v1.getDataType(cc,typer);
	 t2 = v2.getDataType(cc,typer);
       }
    }

   boolean isflt = t1.isFloatType() || t2.isFloatType();
   boolean isdbl = t1.isDoubleType() || t2.isDoubleType();
   boolean islng = t1.isLongType() || t2.isLongType();
   Boolean crslt = null;
   int irslt = 0;

   // AcornLog.logD("START EVAL " + op + " " + v1 + " " + v2 );

   switch (op) {
      case ADD :
	 if (isstr) {
	    // TODO: Need to call toString here to get accurate results
//	    String s0 = getStringValue(v1,typer,cc) + getStringValue(v2,typer,cc);
	    String s0 = computeString(runner,typer,cc,v1) + computeString(runner,typer,cc,v2);
	    rslt = CashewValue.stringValue(typer,typer.STRING_TYPE,s0);
	  }
	 else if (isdbl) {
	    double v0 = v1.getNumber(cc).doubleValue() + v2.getNumber(cc).doubleValue();
	    rslt = CashewValue.numericValue(typer.DOUBLE_TYPE,v0);
	  }
	 else if (isflt) {
	    double v0 = v1.getNumber(cc).doubleValue() + v2.getNumber(cc).doubleValue();
	    rslt = CashewValue.numericValue(typer.FLOAT_TYPE,v0);
	  }
	 else if (islng) {
	    long v0 = v1.getNumber(cc).longValue() + v2.getNumber(cc).longValue();
	    rslt = CashewValue.numericValue(typer,typer.LONG_TYPE,v0);
	  }
	 else {
	    int v0 = v1.getNumber(cc).intValue() + v2.getNumber(cc).intValue();
	    rslt = CashewValue.numericValue(typer,typer.INT_TYPE,v0);
	  }
	 break;
      case AND :
	 if (islng) {
	    long v0 = v1.getNumber(cc).longValue() & v2.getNumber(cc).longValue();
	    rslt = CashewValue.numericValue(typer,typer.LONG_TYPE,v0);
	  }
	 else {
	    int v0 = v1.getNumber(cc).intValue() & v2.getNumber(cc).intValue();
	    rslt = CashewValue.numericValue(typer,typer.INT_TYPE,v0);
	  }
	 break;
      case DIV :
	 if (isdbl) {
	    double vx = v2.getNumber(cc).doubleValue();
	    double v0 = v1.getNumber(cc).doubleValue() / vx;
	    rslt = CashewValue.numericValue(typer.DOUBLE_TYPE,v0);
	  }
	 else if (isflt) {
	    float vx = v2.getNumber(cc).floatValue();
	    float v0 = v1.getNumber(cc).floatValue() / vx;
	    rslt = CashewValue.numericValue(typer.FLOAT_TYPE,v0);
	  }
	 else if (islng) {
	    long vx = v2.getNumber(cc).longValue();
	    if (vx == 0) throwException(typer,"java.lang.ArithmeticException");
	    long v0 = v1.getNumber(cc).longValue() / vx;
	    rslt = CashewValue.numericValue(typer,typer.LONG_TYPE,v0);
	  }
	 else {
	    int vx = v2.getNumber(cc).intValue();
	    if (vx == 0) throwException(typer,"java.lang.ArithmeticException");
	    int v0 = v1.getNumber(cc).intValue() / vx;
	    rslt = CashewValue.numericValue(typer,typer.INT_TYPE,v0);
	  }
	 break;
      case EQL :
	 if (isdbl) {
	    crslt = v1.getNumber(cc).doubleValue() == v2.getNumber(cc).doubleValue();
	  }
	 else if (isflt) {
	    crslt = v1.getNumber(cc).floatValue() == v2.getNumber(cc).floatValue();
	  }
	 else if (islng) {
	    crslt = v1.getNumber(cc).longValue() == v2.getNumber(cc).longValue();
	  }
	 else if (t1.isPrimitiveType() && t2.isPrimitiveType()) {
	    crslt = v1.getNumber(cc).intValue() == v2.getNumber(cc).intValue();
	  }
	 else {
	    crslt = (v1 == v2);
	    AcornLog.logD("COMPARE RESULT " + crslt + " " + v1 + " " + v2 + " " + (v1==v2));
	  }
	 break;
      case GEQ :
	 if (isdbl) {
	    crslt = v1.getNumber(cc).doubleValue() >= v2.getNumber(cc).doubleValue();
	  }
	 else if (isflt) {
	    crslt = v1.getNumber(cc).floatValue() >= v2.getNumber(cc).floatValue();
	  }
	 else if (islng) {
	    crslt = v1.getNumber(cc).longValue() >= v2.getNumber(cc).longValue();
	  }
	 else if (t1.isPrimitiveType() && t2.isPrimitiveType()) {
	    crslt = v1.getNumber(cc).intValue() >= v2.getNumber(cc).intValue();
	  }
	 else {
	    throw CuminRunStatus.Factory.createCompilerError();
	  }
	 break;
      case GTR :
	 if (isdbl) {
	    crslt = v1.getNumber(cc).doubleValue() > v2.getNumber(cc).doubleValue();
	  }
	 else if (isflt) {
	    crslt = v1.getNumber(cc).floatValue() > v2.getNumber(cc).floatValue();
	  }
	 else if (islng) {
	    crslt = v1.getNumber(cc).longValue() > v2.getNumber(cc).longValue();
	  }
	 else if (t1.isPrimitiveType() && t2.isPrimitiveType()) {
	    crslt = v1.getNumber(cc).intValue() > v2.getNumber(cc).intValue();
	  }
	 else {
	    throw CuminRunStatus.Factory.createCompilerError();
	  }
	 break;
      case LEQ :
	 if (isdbl) {
	    crslt = v1.getNumber(cc).doubleValue() <= v2.getNumber(cc).doubleValue();
	  }
	 else if (isflt) {
	    crslt = v1.getNumber(cc).floatValue() <= v2.getNumber(cc).floatValue();
	  }
	 else if (islng) {
	    crslt = v1.getNumber(cc).longValue() <= v2.getNumber(cc).longValue();
	  }
	 else if (t1.isPrimitiveType() && t2.isPrimitiveType()) {
	    crslt = v1.getNumber(cc).intValue() <= v2.getNumber(cc).intValue();
	  }
	 else {
	    throw CuminRunStatus.Factory.createCompilerError();
	  }
	 break;
      case LSH :
	 if (islng) {
	    long v0 = v1.getNumber(cc).longValue() << v2.getNumber(cc).intValue();
	    rslt = CashewValue.numericValue(typer,typer.LONG_TYPE,v0);
	  }
	 else {
	    int v0 = v1.getNumber(cc).intValue() << v2.getNumber(cc).intValue();
	    rslt = CashewValue.numericValue(typer,typer.INT_TYPE,v0);
	  }
	 break;
      case LSS :
	 if (isdbl) {
	    crslt = v1.getNumber(cc).doubleValue() < v2.getNumber(cc).doubleValue();
	  }
	 else if (isflt) {
	    crslt = v1.getNumber(cc).floatValue() < v2.getNumber(cc).floatValue();
	  }
	 else if (islng) {
	    crslt = v1.getNumber(cc).longValue() < v2.getNumber(cc).longValue();
	  }
	 else if (t1.isPrimitiveType() && t2.isPrimitiveType()) {
	    crslt = v1.getNumber(cc).intValue() < v2.getNumber(cc).intValue();
	  }
	 else {
	    throw CuminRunStatus.Factory.createCompilerError();
	  }
	 break;
      case MOD :
	 if (isdbl) {
	    double vx = v2.getNumber(cc).doubleValue();
	    double v0 = v1.getNumber(cc).doubleValue() % vx;
	    rslt = CashewValue.numericValue(typer.DOUBLE_TYPE,v0);
	  }
	 else if (isflt) {
	    float vx = v2.getNumber(cc).floatValue();
	    float v0 = v1.getNumber(cc).floatValue() % vx;
	    rslt = CashewValue.numericValue(typer.FLOAT_TYPE,v0);
	  }
	 else if (islng) {
	    long vx = v2.getNumber(cc).longValue();
	    if (vx == 0) throwException(typer,"java.lang.ArithmeticException");
	    long v0 = v1.getNumber(cc).longValue() % vx;
	    rslt = CashewValue.numericValue(typer,typer.LONG_TYPE,v0);
	  }
	 else {
	    int vx = v2.getNumber(cc).intValue();
	    if (vx == 0) throwException(typer,"java.lang.ArithmeticException");
	    int v0 = v1.getNumber(cc).intValue() % vx;
	    rslt = CashewValue.numericValue(typer,typer.INT_TYPE,v0);
	  }
	 break;
      case MUL :
	 if (isdbl) {
	    double v0 = v1.getNumber(cc).doubleValue() * v2.getNumber(cc).doubleValue();
	    rslt = CashewValue.numericValue(typer.DOUBLE_TYPE,v0);
	  }
	 else if (isflt) {
	    double v0 = v1.getNumber(cc).doubleValue() * v2.getNumber(cc).doubleValue();
	    rslt = CashewValue.numericValue(typer.FLOAT_TYPE,v0);
	  }
	 else if (islng) {
	    long v0 = v1.getNumber(cc).longValue() * v2.getNumber(cc).longValue();
	    rslt = CashewValue.numericValue(typer,typer.LONG_TYPE,v0);
	  }
	 else {
	    int v0 = v1.getNumber(cc).intValue() * v2.getNumber(cc).intValue();
	    rslt = CashewValue.numericValue(typer,typer.INT_TYPE,v0);
	  }
	 break;
      case NEQ :
	 if (isdbl) {
	    crslt = v1.getNumber(cc).doubleValue() != v2.getNumber(cc).doubleValue();
	  }
	 else if (isflt) {
	    crslt = v1.getNumber(cc).floatValue() != v2.getNumber(cc).floatValue();
	  }
	 else if (islng) {
	    crslt = v1.getNumber(cc).longValue() != v2.getNumber(cc).longValue();
	  }
	 else if (t1.isPrimitiveType() && t2.isPrimitiveType()) {
	    crslt = v1.getNumber(cc).intValue() != v2.getNumber(cc).intValue();
	  }
	 else {
	    crslt = (v1 != v2);
	  }
	 break;
      case OR :
	 if (islng) {
	    long v0 = v1.getNumber(cc).longValue() | v2.getNumber(cc).longValue();
	    rslt = CashewValue.numericValue(typer,typer.LONG_TYPE,v0);
	  }
	 else {
	    int v0 = v1.getNumber(cc).intValue() | v2.getNumber(cc).intValue();
	    rslt = CashewValue.numericValue(typer,typer.INT_TYPE,v0);
	  }
	 break;
      case RSH :
	 if (islng) {
	    long v0 = v1.getNumber(cc).longValue() >> v2.getNumber(cc).intValue();
	    rslt = CashewValue.numericValue(typer,typer.LONG_TYPE,v0);
	  }
	 else {
	    int v0 = v1.getNumber(cc).intValue() >> v2.getNumber(cc).intValue();
	    rslt = CashewValue.numericValue(typer,typer.INT_TYPE,v0);
	  }
	 break;
      case RSHU :
	 if (islng) {
	    long v0 = v1.getNumber(cc).longValue() >>> v2.getNumber(cc).intValue();
	    rslt = CashewValue.numericValue(typer,typer.LONG_TYPE,v0);
	  }
	 else {
	    int v0 = v1.getNumber(cc).intValue() >>> v2.getNumber(cc).intValue();
	    rslt = CashewValue.numericValue(typer,typer.INT_TYPE,v0);
	  }
	 break;
      case SUB :
	 if (isdbl) {
	    double v0 = v1.getNumber(cc).doubleValue() - v2.getNumber(cc).doubleValue();
	    rslt = CashewValue.numericValue(typer.DOUBLE_TYPE,v0);
	  }
	 else if (isflt) {
	    double v0 = v1.getNumber(cc).doubleValue() - v2.getNumber(cc).doubleValue();
	    rslt = CashewValue.numericValue(typer.FLOAT_TYPE,v0);
	  }
	 else if (islng) {
	    long v0 = v1.getNumber(cc).longValue() - v2.getNumber(cc).longValue();
	    rslt = CashewValue.numericValue(typer,typer.LONG_TYPE,v0);
	  }
	 else {
	    int v0 = v1.getNumber(cc).intValue() - v2.getNumber(cc).intValue();
	    rslt = CashewValue.numericValue(typer,typer.INT_TYPE,v0);
	  }
	 break;
      case XOR :
	 if (islng) {
	    long v0 = v1.getNumber(cc).longValue() ^ v2.getNumber(cc).longValue();
	    rslt = CashewValue.numericValue(typer,typer.LONG_TYPE,v0);
	  }
	 else {
	    int v0 = v1.getNumber(cc).intValue() ^ v2.getNumber(cc).intValue();
	    rslt = CashewValue.numericValue(typer,typer.INT_TYPE,v0);
	  }
	 break;
      case SIG :
	 if (isdbl) {
	    if (v1.getNumber(cc).doubleValue() < v2.getNumber(cc).doubleValue()) irslt = -1;
	    else if (v1.getNumber(cc).doubleValue() > v2.getNumber(cc).doubleValue()) irslt = 1;
	    else irslt = 0;
	  }
	 else if (isflt) {
	    if (v1.getNumber(cc).floatValue() < v2.getNumber(cc).floatValue()) irslt = -1;
	    else if (v1.getNumber(cc).floatValue() > v2.getNumber(cc).floatValue()) irslt = 1;
	    else irslt = 0;
	  }
	 else if (islng) {
	    if (v1.getNumber(cc).longValue() < v2.getNumber(cc).longValue()) irslt = -1;
	    else if (v1.getNumber(cc).longValue() > v2.getNumber(cc).longValue()) irslt = 1;
	    else irslt = 0;
	  }
	 else {
	    if (v1.getNumber(cc).intValue() < v2.getNumber(cc).intValue()) irslt = -1;
	    else if (v1.getNumber(cc).intValue() > v2.getNumber(cc).intValue()) irslt = 1;
	    else irslt = 0;
	  }
	 return CashewValue.numericValue(typer,typer.INT_TYPE,irslt);
      default :
	 AcornLog.logE("Unknown operator " + op);
	 // illegal binary operator
	 break;
    }


   if (rslt == null) {
      if (crslt != null) {
	 rslt = CashewValue.booleanValue(typer,crslt.booleanValue());
       }
    }

   if (rslt == null) {
      AcornLog.logD("EVAL " + op + " " + v1 + " " + v2 + " " + rslt + " " + crslt);
      throw CuminRunStatus.Factory.createCompilerError();
    }

   AcornLog.logD("EVAL " + op + " " + v1 + " " + v2 + " " + rslt);

   return rslt;
}



static CashewValue evaluateAssign(CuminRunner cr,CuminOperator op,CashewValue v1,CashewValue v2,JcompType tgt)
	throws CuminRunException, CashewException
{
   CashewClock cc = cr.getClock();
   CashewValue rslt = null;
   JcompTyper typer = cr.getTyper();

   switch (op) {
      case ASG :
	 rslt = v2;
	 break;
      case ASG_ADD :
	 rslt = evaluate(cr,typer,cc,CuminOperator.ADD,v1,v2);
	 break;
      case ASG_AND :
	 rslt = evaluate(cr,typer,cc,CuminOperator.AND,v1,v2);
	 break;
      case ASG_DIV :
	 rslt = evaluate(cr,typer,cc,CuminOperator.DIV,v1,v2);
	 break;
      case ASG_LSH :
	 rslt = evaluate(cr,typer,cc,CuminOperator.LSH,v1,v2);
	 break;
      case ASG_MOD :
	 rslt = evaluate(cr,typer,cc,CuminOperator.MOD,v1,v2);
	 break;
      case ASG_MUL :
	 rslt = evaluate(cr,typer,cc,CuminOperator.MUL,v1,v2);
	 break;
      case ASG_OR :
	 rslt = evaluate(cr,typer,cc,CuminOperator.OR,v1,v2);
	 break;
      case ASG_RSH :
	 rslt = evaluate(cr,typer,cc,CuminOperator.RSH,v1,v2);
	 break;
      case ASG_RSHU :
	 rslt = evaluate(cr,typer,cc,CuminOperator.RSHU,v1,v2);
	 break;
      case ASG_SUB :
	 rslt = evaluate(cr,typer,cc,CuminOperator.SUB,v1,v2);
	 break;
      case ASG_XOR :
	 rslt = evaluate(cr,typer,cc,CuminOperator.XOR,v1,v2);
	 break;
      default :
	 // error
	 break;
    }

   //AcornLog.logD("ASSIGN " + op + " " + v1.getDebugString(cc) + " " + v2.getDebugString(cc) + " " +
		    // rslt.getDebugString(cc) + " " + tgt);

   assignValue(cr,v1,rslt,tgt);

   return rslt;
}



static void assignValue(CuminRunner cr,CashewValue vr,CashewValue cv,JcompType tgt)
	throws CuminRunException, CashewException
{
   CashewClock cc = cr.getClock();
   cv = castValue(cr,cv,tgt);
   cv = cv.getActualValue(cc);
   // AcornLog.logD("DOASSIGN " + vr + " " + vr.getDebugString(cc) + " " + cv.getDebugString(cc));
   vr.setValueAt(cc,cv);
}


static CashewValue castValue(CuminRunner cr,CashewValue cv,JcompType target)
	throws CuminRunException, CashewException
{
   CashewClock cc = cr.getClock();
   JcompTyper typer = cr.getTyper();

   JcompType styp = cv.getDataType(cc,cr.getTyper());
   if (styp == typer.ANY_TYPE) return cv;

   if (styp == target) return cv;

   if (target.isNumericType()) {
      cv = unboxValue(typer,cc,cv);
      styp = cv.getDataType(cc,cr.getTyper());
      if (styp.isFloatingType()) {
	 cv = CashewValue.numericValue(target,cv.getNumber(cc).doubleValue());
       }
      else {
	 cv = CashewValue.numericValue(typer,target,cv.getNumber(cc).longValue());
       }
    }
   else if (target.isBooleanType()) {
      if (styp.getName().equals("java.lang.Boolean")) {
	 cv = unboxValue(typer,cc,cv);
       }
    }
   else if (styp.isNumericType()) {
      switch (target.getName()) {
	 case "java.lang.Long" :
	    break;
	 case "java.lang.Integer" :
	    cv = castValue(cr,cv,typer.INT_TYPE);
	    break;
	 case "java.lang.Short" :
	    cv = castValue(cr,cv,typer.SHORT_TYPE);
	    break;
	 case "java.lang.Byte" :
	    cv = castValue(cr,cv,typer.BYTE_TYPE);
	    break;
	 case "java.lang.Character" :
	    cv = castValue(cr,cv,typer.CHAR_TYPE);
	    break;
	 case "java.lang.Float" :
	    cv = castValue(cr,cv,typer.FLOAT_TYPE);
	    break;
	 case "java.lang.Double" :
	    cv = castValue(cr,cv,typer.DOUBLE_TYPE);
	    break;
	 case "java.lang.Number" :
	 case "java.lang.Object" :
	 default :
	    break;
       }
      cv = boxValue(cr,cv);
    }
   else if (styp.isBooleanType()) {
      if (target.getName().equals("java.lang.Boolean")) {
	 cv = boxValue(cr,cv);
       }
    }
   else {
      // other special casts here
    }

   return cv;
}


static CashewValue unboxValue(JcompTyper typer,CashewClock cc,CashewValue cv)
	throws CuminRunException, CashewException
{
   JcompType styp = cv.getDataType(cc,typer);
   if (styp.isAnyType()) throwException(typer,"java.lang.NullPointerException");
   if (styp.isNumericType() || styp.isBooleanType()) return cv;

   switch (styp.getName()) {
      case "java.lang.Long" :
      case "java.lang.Integer" :
      case "java.lang.Short" :
      case "java.lang.Byte" :
      case "java.lang.Character" :
      case "java.lang.Float" :
      case "java.lang.Double" :
      case "java.lang.Boolean" :
	 cv = cv.getFieldValue(typer,cc,styp.getName() + ".value");
	 break;
    }

   return cv;
}



static CashewValue boxValue(CuminRunner cr,CashewValue cv)
	throws CuminRunException, CashewException
{
   CashewClock cc = cr.getClock();
   JcompTyper typer = cr.getTyper();
   JcompType typ = cv.getDataType(cc,typer);

   if (typ == typer.INT_TYPE) {
      cv = invokeEvalConverter(cr,"java.lang.Integer","valueOf","(I)Ljava/lang/Integer;",cv);
    }
   else if (typ == typer.SHORT_TYPE) {
      cv = invokeEvalConverter(cr,"java.lang.Short","valueOf","(IS)Ljava/lang/Short;",cv);
    }
   else if (typ == typer.BYTE_TYPE) {
      cv = invokeEvalConverter(cr,"java.lang.Byte","valueOf","(B)Ljava/lang/Byte;",cv);
    }
   else if (typ == typer.CHAR_TYPE) {
      cv = invokeEvalConverter(cr,"java.lang.Character","valueOf","(C)Ljava/lang/Character;",cv);
    }
   else if (typ == typer.LONG_TYPE) {
      cv = invokeEvalConverter(cr,"java.lang.Long","valueOf","(J)Ljava/lang/Long;",cv);
    }
   else if (typ == typer.FLOAT_TYPE) {
      cv = invokeEvalConverter(cr,"java.lang.Float","valueOf","(F)Ljava/lang/Float;",cv);
    }
   else if (typ == typer.DOUBLE_TYPE) {
      cv = invokeEvalConverter(cr,"java.lang.Double","valueOf","(D)Ljava/lang/Double;",cv);
    }
   else if (typ == typer.BOOLEAN_TYPE) {
      cv = invokeEvalConverter(cr,"java.lang.Boolean","valueOf","(Z)Ljava/lang/Boolean;",cv);
    }

   return cv;
}



private static CashewValue invokeConverter(CuminRunner runner,String cls,
      String mthd,String sgn,CashewValue arg) throws CuminRunException

{
   JcodeClass mcls = runner.getCodeFactory().findClass(cls);
   JcodeMethod mmthd = mcls.findMethod(mthd,sgn);
   List<CashewValue> args = new ArrayList<CashewValue>();
   args.add(arg);
   CuminRunner cr = runner.handleCall(runner.getClock(),mmthd,args,CallType.VIRTUAL);

   CuminRunStatus sts = null;
   try {
      sts = cr.interpret(EvalType.RUN);
    }
   catch (CuminRunException r) {
      if (r.getReason() == Reason.RETURN) {
	 sts = r;
       }
      else throw r;
    }

   if (sts != null && sts.getReason() == Reason.RETURN) {
      return sts.getValue();
    }

   return arg;
}



private static CashewValue invokeEvalConverter(CuminRunner runner,String cls,String mthd,String sgn,
      CashewValue arg)
	throws CuminRunException, CashewException
{
   String cast = arg.getDataType(runner.getClock(),runner.getTyper()).getName();
   String argv = arg.getString(runner.getTyper(),runner.getClock());
   String expr = cls + "." + mthd + "( (" + cast + ") " + argv + ")";

   Map<String,CashewValue> cache = null;
   Object sess = runner.getLookupContext().getSessionKey();
   CashewValue cv = null;
   if (sess != null) {
      synchronized (cached_values) {
	 cache = cached_values.get(sess);
	 if (cache == null) {
	    cache = new HashMap<>();
	    cached_values.put(sess,cache);
	  }
       }
      cv = cache.get(expr);
      if (cv != null) return cv;
    }

   cv = runner.getLookupContext().evaluate(expr);
   if (cv == null) {
      cv = invokeConverter(runner,cls,mthd,sgn,arg);
    }
   if (cv != null && cache != null) {
      cache.put(expr,cv);
    }

   return cv;
}



private static String computeString(CuminRunner runner,JcompTyper typer,CashewClock cc,CashewValue arg)
	throws CuminRunException, CashewException
{
   JcompType typ = arg.getDataType(cc,typer);
   if (typ.isCharType()) {
      char v = arg.getChar(cc);
      return String.valueOf(v);
    }
   else if (typ.isPrimitiveType() || typ.isStringType()) {
      return getStringValue(arg,typer,cc);
    }
   String sgn = "(Ljava/lang/Object;)Ljava/lang/String;";
   if (typ.isArrayType() && typ.getBaseType().isCharType()) {
      sgn = "([C)Ljava/lang/String;";
    }
   CashewValue cv = invokeConverter(runner,"java.lang.String","valueOf",sgn,arg);
   return getStringValue(cv,typer,cc);
}



static CashewValue evaluate(JcompTyper typer,CashewClock cc,CuminOperator op,CashewValue v1)
	throws CuminRunException, CashewException
{
   JcompType t1 = v1.getDataType(cc,typer);
   if (t1.isClassType()) {
      v1 = unboxValue(typer,cc,v1);
      t1 = v1.getDataType(cc,typer);
    }
   boolean isflt = t1.isFloatType();
   boolean isdbl = t1.isDoubleType();
   boolean islng = t1.isLongType();

   CashewValue rslt = null;
   switch (op) {
      case POSTINCR :
	 if (isflt) {
	    rslt = v1.getActualValue(cc);
	    float fnv = v1.getNumber(cc).floatValue() + 1;
	    CashewValue nv = CashewValue.numericValue(typer.FLOAT_TYPE,fnv);
	    v1.setValueAt(cc,nv);
	  }
	 else if (isdbl) {
	    rslt = v1.getActualValue(cc);
	    double fnv = v1.getNumber(cc).doubleValue() + 1;
	    CashewValue nv = CashewValue.numericValue(typer.DOUBLE_TYPE,fnv);
	    v1.setValueAt(cc,nv);
	  }
	 else if (islng) {
	    rslt = v1.getActualValue(cc);
	    long fnv = v1.getNumber(cc).longValue() + 1;
	    CashewValue nv = CashewValue.numericValue(typer,typer.LONG_TYPE,fnv);
	    v1.setValueAt(cc,nv);
	  }
	 else {
	    rslt = v1.getActualValue(cc);
	    int fnv = v1.getNumber(cc).intValue() + 1;
	    CashewValue nv = CashewValue.numericValue(typer,typer.INT_TYPE,fnv);
	    v1.setValueAt(cc,nv);
	  }
	 break;
      case POSTDECR :
	 if (isflt) {
	    rslt = v1.getActualValue(cc);
	    float fnv = v1.getNumber(cc).floatValue() - 1;
	    CashewValue nv = CashewValue.numericValue(typer.FLOAT_TYPE,fnv);
	    v1.setValueAt(cc,nv);
	  }
	 else if (isdbl) {
	    rslt = v1.getActualValue(cc);
	    double fnv = v1.getNumber(cc).doubleValue() - 1;
	    CashewValue nv = CashewValue.numericValue(typer.DOUBLE_TYPE,fnv);
	    v1.setValueAt(cc,nv);
	  }
	 else if (islng) {
	    rslt = v1.getActualValue(cc);
	    long fnv = v1.getNumber(cc).longValue() - 1;
	    CashewValue nv = CashewValue.numericValue(typer,typer.LONG_TYPE,fnv);
	    v1.setValueAt(cc,nv);
	  }
	 else {
	    rslt = v1.getActualValue(cc);
	    int fnv = v1.getNumber(cc).intValue() - 1;
	    CashewValue nv = CashewValue.numericValue(typer,typer.INT_TYPE,fnv);
	    v1.setValueAt(cc,nv);
	  }
	 break;
      case INCR :
	 if (isflt) {
	    float fnv = v1.getNumber(cc).floatValue() + 1;
	    rslt = CashewValue.numericValue(typer.FLOAT_TYPE,fnv);
	    v1.setValueAt(cc,rslt);
	  }
	 else if (isdbl) {
	    double fnv = v1.getNumber(cc).doubleValue() + 1;
	    rslt = CashewValue.numericValue(typer.DOUBLE_TYPE,fnv);
	    v1.setValueAt(cc,rslt);
	  }
	 else if (islng) {
	    long fnv = v1.getNumber(cc).longValue() + 1;
	    rslt = CashewValue.numericValue(typer,typer.LONG_TYPE,fnv);
	    v1.setValueAt(cc,rslt);
	  }
	 else {
	    int fnv = v1.getNumber(cc).intValue() + 1;
	    rslt = CashewValue.numericValue(typer,typer.INT_TYPE,fnv);
	    v1.setValueAt(cc,rslt);
	  }
	 break;
      case DECR :
	 if (isflt) {
	    float fnv = v1.getNumber(cc).floatValue() - 1;
	    rslt = CashewValue.numericValue(typer.FLOAT_TYPE,fnv);
	    v1.setValueAt(cc,rslt);
	  }
	 else if (isdbl) {
	    double fnv = v1.getNumber(cc).doubleValue() - 1;
	    rslt = CashewValue.numericValue(typer.DOUBLE_TYPE,fnv);
	    v1.setValueAt(cc,rslt);
	  }
	 else if (islng) {
	    long fnv = v1.getNumber(cc).longValue() - 1;
	    rslt = CashewValue.numericValue(typer,typer.LONG_TYPE,fnv);
	    v1.setValueAt(cc,rslt);
	  }
	 else {
	    int fnv = v1.getNumber(cc).intValue() - 1;
	    rslt = CashewValue.numericValue(typer,typer.INT_TYPE,fnv);
	    v1.setValueAt(cc,rslt);
	  }
	 break;
      case NEG :
	 if (isflt) {
	    float fnv = - v1.getNumber(cc).floatValue();
	    rslt = CashewValue.numericValue(typer.FLOAT_TYPE,fnv);
	  }
	 else if (isdbl) {
	    double fnv = -v1.getNumber(cc).doubleValue();
	    rslt = CashewValue.numericValue(typer.DOUBLE_TYPE,fnv);
	  }
	 else if (islng) {
	    long fnv = -v1.getNumber(cc).longValue();
	    rslt = CashewValue.numericValue(typer,typer.LONG_TYPE,fnv);
	  }
	 else {
	    int fnv = -v1.getNumber(cc).intValue();
	    rslt = CashewValue.numericValue(typer,typer.INT_TYPE,fnv);
	  }
	 break;
      case NOT :
	 boolean notv = !v1.getBoolean(cc);
	 rslt = CashewValue.booleanValue(typer,notv);
	 break;
      case COMP :
         if (islng) {
            long fnv = ~v1.getNumber(cc).longValue();
            rslt = CashewValue.numericValue(typer,typer.LONG_TYPE,fnv);
          }
         else {
	    int fnv = ~v1.getNumber(cc).intValue();
	    rslt = CashewValue.numericValue(typer,typer.INT_TYPE,fnv);
	  }
         break;
      case NOP :
	 rslt = v1.getActualValue(cc);
	 break;
      default :
	 throw CuminRunStatus.Factory.createCompilerError();
    }

   return rslt;
}



/********************************************************************************/
/*									        */
/*	Throw exception 							*/
/*										*/
/********************************************************************************/

static void throwException(JcompTyper typer,String exc) throws CuminRunException
{
   JcompType typ = typer.findSystemType(exc);
   throwException(typer,typ);
}


static void throwException(JcompTyper typer,JcompType typ) throws CuminRunException
{
   CashewValue cv = CashewValue.objectValue(typer,typ);
   throw new CuminRunException(Reason.EXCEPTION,typ.toString(),null,cv);
}



static CuminRunStatus returnException(JcompTyper typer,String exc)
{
   JcompType typ = typer.findSystemType(exc);
   return returnException(typer,typ);
}



static CuminRunStatus returnException(JcompTyper typer,JcompType typ)
{
   CashewValue cv = CashewValue.objectValue(typer,typ);
   return CuminRunStatus.Factory.createException(cv);
}



/********************************************************************************/
/*										*/
/*	Array helper methods							*/
/*										*/
/********************************************************************************/

static CashewValue buildArray(CuminRunner runner,int idx,int [] bnds,JcompType base)
	throws CashewException
{
   JcompType atyp = base;
   for (int i = idx; i < bnds.length; ++i) {
      atyp = runner.getTyper().findArrayType(atyp);
    }
   CashewValue cv = CashewValue.arrayValue(runner.getTyper(),atyp,bnds[idx]);
   if (idx+1 < bnds.length) {
      for (int i = 0; i < bnds[idx]; ++i) {
	 cv.setIndexValue(runner.getClock(),i,buildArray(runner,idx+1,bnds,base));
       }
    }

   return cv;
}


/********************************************************************************/
/*										*/
/*	String helper methods							*/
/*										*/
/********************************************************************************/

static String getStringValue(CashewValue cv,JcompTyper typer,CashewClock cc) throws CashewException
{
   if (!cv.getDataType(cc,null).isPrimitiveType() && cv.isNull(cc)) return "null";

   JcompType jt = cv.getDataType(cc,typer);
   if (jt.isEnumType()) {
      return cv.getFieldValue(typer,cc,"java.lang.Enum.name").getString(typer,cc);
    }
   String rslt = cv.getString(typer,cc,1,false);

   if (rslt.length() > 4096) {
      rslt = cv.getString(typer,cc,0,false);
    }
   return rslt;
}












}	// end of class CuminEvaluator




/* end of CuminEvaluator.java */










































































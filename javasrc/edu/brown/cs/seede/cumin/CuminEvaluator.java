/********************************************************************************/
/*                                                                              */
/*              CuminEvaluator.java                                             */
/*                                                                              */
/*      Expression evaluation logic                                             */
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

import java.util.ArrayList;
import java.util.List;

import edu.brown.cs.ivy.jcode.JcodeClass;
import edu.brown.cs.ivy.jcode.JcodeMethod;
import edu.brown.cs.ivy.jcomp.JcompType;
import edu.brown.cs.seede.acorn.AcornLog;
import edu.brown.cs.seede.cashew.CashewClock;
import edu.brown.cs.seede.cashew.CashewConstants;
import edu.brown.cs.seede.cashew.CashewValue;

class CuminEvaluator implements CuminConstants, CashewConstants 
{


/********************************************************************************/
/*                                                                              */
/*      Arithmetic Expressions                                                  */
/*                                                                              */
/********************************************************************************/

static CashewValue evaluate(CashewClock cc,CuminOperator op,CashewValue v1,CashewValue v2)
{
   CashewValue rslt = null;
   
   JcompType t1 = v1.getDataType(cc);
   JcompType t2 = v2.getDataType(cc);
   boolean isstr = t1 == STRING_TYPE || t2 == STRING_TYPE;
   boolean isflt = t1 == FLOAT_TYPE || t2 == FLOAT_TYPE;
   boolean isdbl = t1 == DOUBLE_TYPE || t2 == DOUBLE_TYPE;
   boolean islng = t1 == LONG_TYPE || t2 == LONG_TYPE;
   Boolean crslt = null;
   int irslt = 0;
   
   switch (op) {
      case ADD :
         if (isstr) {
            String s0 = v1.getString(cc) + v2.getString(cc);
            rslt = CashewValue.stringValue(s0);
          }
         else if (isdbl) {
            double v0 = v1.getNumber(cc).doubleValue() + v2.getNumber(cc).doubleValue();
            rslt = CashewValue.numericValue(DOUBLE_TYPE,v0);
          }
         else if (isflt) {
            double v0 = v1.getNumber(cc).doubleValue() + v2.getNumber(cc).doubleValue();
            rslt = CashewValue.numericValue(DOUBLE_TYPE,v0);
          }
         else if (islng) {
            long v0 = v1.getNumber(cc).longValue() + v2.getNumber(cc).longValue();
            rslt = CashewValue.numericValue(LONG_TYPE,v0);
          }
         else {
            int v0 = v1.getNumber(cc).intValue() + v2.getNumber(cc).intValue();
            rslt = CashewValue.numericValue(INT_TYPE,v0);
          }
         break;
      case AND :
         if (islng) {
            long v0 = v1.getNumber(cc).longValue() & v2.getNumber(cc).longValue();
            rslt = CashewValue.numericValue(LONG_TYPE,v0);
          }
         else {
            int v0 = v1.getNumber(cc).intValue() & v2.getNumber(cc).intValue();
            rslt = CashewValue.numericValue(INT_TYPE,v0);
          }
         break;  
      case DIV :
         //TODO: check if v2 = 0 and provide exception
         if (isdbl) {
            double v0 = v1.getNumber(cc).doubleValue() / v2.getNumber(cc).doubleValue();
            rslt = CashewValue.numericValue(DOUBLE_TYPE,v0);
          }
         else if (isflt) {
            double v0 = v1.getNumber(cc).doubleValue() / v2.getNumber(cc).doubleValue();
            rslt = CashewValue.numericValue(DOUBLE_TYPE,v0);
          }
         else if (islng) {
            long v0 = v1.getNumber(cc).longValue() / v2.getNumber(cc).longValue();
            rslt = CashewValue.numericValue(LONG_TYPE,v0);
          }
         else {
            int v0 = v1.getNumber(cc).intValue() / v2.getNumber(cc).intValue();
            rslt = CashewValue.numericValue(INT_TYPE,v0);
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
            // error
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
            // error
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
            // error
          }
         break;
      case LSH :
         if (islng) {
            long v0 = v1.getNumber(cc).longValue() << v2.getNumber(cc).intValue();
            rslt = CashewValue.numericValue(LONG_TYPE,v0);
          }
         else {
            int v0 = v1.getNumber(cc).intValue() << v2.getNumber(cc).intValue();
            rslt = CashewValue.numericValue(INT_TYPE,v0);
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
            // error
          }
         break;   
      case MOD :
         //TODO: check for v2 = 0 and provide exception
         if (islng) {
            long v0 = v1.getNumber(cc).longValue() % v2.getNumber(cc).longValue();
            rslt = CashewValue.numericValue(LONG_TYPE,v0);
          }
         else {
            int v0 = v1.getNumber(cc).intValue() % v2.getNumber(cc).intValue();
            rslt = CashewValue.numericValue(INT_TYPE,v0);
          }
         break; 
      case MUL :
         if (isdbl) {
            double v0 = v1.getNumber(cc).doubleValue() * v2.getNumber(cc).doubleValue();
            rslt = CashewValue.numericValue(DOUBLE_TYPE,v0);
          }
         else if (isflt) {
            double v0 = v1.getNumber(cc).doubleValue() * v2.getNumber(cc).doubleValue();
            rslt = CashewValue.numericValue(DOUBLE_TYPE,v0);
          }
         else if (islng) {
            long v0 = v1.getNumber(cc).longValue() * v2.getNumber(cc).longValue();
            rslt = CashewValue.numericValue(LONG_TYPE,v0);
          }
         else {
            int v0 = v1.getNumber(cc).intValue() * v2.getNumber(cc).intValue();
            rslt = CashewValue.numericValue(INT_TYPE,v0);
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
            rslt = CashewValue.numericValue(LONG_TYPE,v0);
          }
         else {
            int v0 = v1.getNumber(cc).intValue() | v2.getNumber(cc).intValue();
            rslt = CashewValue.numericValue(INT_TYPE,v0);
          }
         break; 
      case RSH : 
         if (islng) {
            long v0 = v1.getNumber(cc).longValue() >> v2.getNumber(cc).intValue();
            rslt = CashewValue.numericValue(LONG_TYPE,v0);
          }
         else {
            int v0 = v1.getNumber(cc).intValue() >> v2.getNumber(cc).intValue();
            rslt = CashewValue.numericValue(INT_TYPE,v0);
          }
         break;    
      case RSHU :
         if (islng) {
            long v0 = v1.getNumber(cc).longValue() >>> v2.getNumber(cc).intValue();
            rslt = CashewValue.numericValue(LONG_TYPE,v0);
          }
         else {
            int v0 = v1.getNumber(cc).intValue() >>> v2.getNumber(cc).intValue();
            rslt = CashewValue.numericValue(INT_TYPE,v0);
          }
         break; 
      case SUB :    
         if (isdbl) {
            double v0 = v1.getNumber(cc).doubleValue() - v2.getNumber(cc).doubleValue();
            rslt = CashewValue.numericValue(DOUBLE_TYPE,v0);
          }
         else if (isflt) {
            double v0 = v1.getNumber(cc).doubleValue() - v2.getNumber(cc).doubleValue();
            rslt = CashewValue.numericValue(DOUBLE_TYPE,v0);
          }
         else if (islng) {
            long v0 = v1.getNumber(cc).longValue() - v2.getNumber(cc).longValue();
            rslt = CashewValue.numericValue(LONG_TYPE,v0);
          }
         else {
            int v0 = v1.getNumber(cc).intValue() - v2.getNumber(cc).intValue();
            rslt = CashewValue.numericValue(INT_TYPE,v0);
          }
         break;
      case XOR :
         if (islng) {
            long v0 = v1.getNumber(cc).longValue() ^ v2.getNumber(cc).longValue();
            rslt = CashewValue.numericValue(LONG_TYPE,v0);
          }
         else {
            int v0 = v1.getNumber(cc).intValue() ^ v2.getNumber(cc).intValue();
            rslt = CashewValue.numericValue(INT_TYPE,v0);
          }
         break; 
      case SIG :
         if (isdbl) {
            if (v1.getNumber(cc).doubleValue() < v1.getNumber(cc).doubleValue()) irslt = -1;
            else if (v1.getNumber(cc).doubleValue() > v1.getNumber(cc).doubleValue()) irslt = 1;
            else irslt = 0;
          }
         else if (isflt) {
            if (v1.getNumber(cc).floatValue() < v1.getNumber(cc).floatValue()) irslt = -1;
            else if (v1.getNumber(cc).floatValue() > v1.getNumber(cc).floatValue()) irslt = 1;
            else irslt = 0;
          }
         else if (islng) {
            if (v1.getNumber(cc).longValue() < v1.getNumber(cc).longValue()) irslt = -1;
            else if (v1.getNumber(cc).longValue() > v1.getNumber(cc).longValue()) irslt = 1;
            else irslt = 0;
          }
         else {
            if (v1.getNumber(cc).intValue() < v1.getNumber(cc).intValue()) irslt = -1;
            else if (v1.getNumber(cc).intValue() > v1.getNumber(cc).intValue()) irslt = 1;
            else irslt = 0;
          }
         return CashewValue.numericValue(INT_TYPE,irslt);
      default :
         AcornLog.logE("Unknown operator " + op);
         // illegal binary operator
         break;
    }
   
   if (rslt == null) {
      if (crslt != null) {
         rslt = CashewValue.booleanValue(crslt.booleanValue());
       }
    }
   
   return rslt;
}



static CashewValue evaluateAssign(CuminRunner cr,CuminOperator op,CashewValue v1,CashewValue v2,JcompType tgt)
{
   CashewClock cc = cr.getClock();
   CashewValue rslt = null;
   
   switch (op) {
      case ASG :
         rslt = v2;
         break;
      case ASG_ADD :
         rslt = evaluate(cc,CuminOperator.ADD,v1,v2);
         break;
      case ASG_AND :
         rslt = evaluate(cc,CuminOperator.AND,v1,v2);
         break;
      case ASG_DIV :
         rslt = evaluate(cc,CuminOperator.DIV,v1,v2);
         break;
      case ASG_LSH :
         rslt = evaluate(cc,CuminOperator.LSH,v1,v2);
         break;
      case ASG_MOD :
         rslt = evaluate(cc,CuminOperator.MOD,v1,v2);
         break;
      case ASG_MUL :
         rslt = evaluate(cc,CuminOperator.MUL,v1,v2);
         break;
      case ASG_OR :
         rslt = evaluate(cc,CuminOperator.OR,v1,v2);
         break;
      case ASG_RSH :
         rslt = evaluate(cc,CuminOperator.RSH,v1,v2);
         break;
      case ASG_RSHU :
         rslt = evaluate(cc,CuminOperator.RSHU,v1,v2);
         break;
      case ASG_SUB :
         rslt = evaluate(cc,CuminOperator.SUB,v1,v2);
         break;
      case ASG_XOR :
         rslt = evaluate(cc,CuminOperator.XOR,v1,v2);
         break;
      default :
         // error
         break;
    }
   
   assignValue(cr,v1,rslt,tgt);
   
   return rslt;
}



static void assignValue(CuminRunner cr,CashewValue vr,CashewValue cv,JcompType tgt)
{
   CashewClock cc = cr.getClock();
   cv = castValue(cr,cv,tgt);
   cv = cv.getActualValue(cc);
   vr.setValueAt(cc,cv);
}


static CashewValue castValue(CuminRunner cr,CashewValue cv,JcompType target) 
{
   CashewClock cc = cr.getClock();
   JcompType styp = cv.getDataType(cc);
   
   if (styp == target) return cv;
   
   if (target.isNumericType()) {
      if (styp == LONG_TYPE || styp == SHORT_TYPE || styp == BYTE_TYPE ||
            styp == INT_TYPE || styp == CHAR_TYPE) {
         cv = CashewValue.numericValue(target,cv.getNumber(cc).longValue());
       }
      else if (styp == DOUBLE_TYPE || styp == FLOAT_TYPE) {
         cv = CashewValue.numericValue(target,cv.getNumber(cc).doubleValue());
       }
      else {
         switch (styp.getName()) {
            case "java.lang.Long" :
               cv = invokeConverter(cr,"java.lang.Number","longValue","()L",cv);
               break;
            case "java.lang.Integer" :
               cv = invokeConverter(cr,"java.lang.Number","intValue","()I",cv);
               break;
            case "java.lang.Short" :
               cv = invokeConverter(cr,"java.lang.Number","shortValue","()S",cv);
               break;
            case "java.lang.Byte" :
               cv = invokeConverter(cr,"java.lang.Number","byteValue","()B",cv);
               break;
            case "java.lang.Character" :
               
               break;
            case "java.lang.Float" :
               cv = invokeConverter(cr,"java.lang.Number","floatValue","()F",cv);
               break;
            case "java.lang.Double" :
               cv = invokeConverter(cr,"java.lang.Number","doubleValue","()D",cv);
               break;
          }
       }
    }
   else if (target.isBooleanType()) {
      if (styp.getName().equals("java.lang.Boolean")) {
         
       }
    }
   else if (styp.isNumericType()) {
      switch (target.getName()) {
         case "java.lang.Long" :
            break;
         case "java.lang.Integer" :
            cv = castValue(cr,cv,INT_TYPE);
            break;
         case "java.lang.Short" :
            cv = castValue(cr,cv,SHORT_TYPE);
            break;
         case "java.lang.Byte" :
            cv = castValue(cr,cv,BYTE_TYPE);
            break;
         case "java.lang.Character" :
            cv = castValue(cr,cv,CHAR_TYPE);
            break;
         case "java.lang.Float" :
            cv = castValue(cr,cv,FLOAT_TYPE);
            break;
         case "java.lang.Double" :
            cv = castValue(cr,cv,DOUBLE_TYPE);
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



static private CashewValue boxValue(CuminRunner cr,CashewValue cv)
{
   CashewClock cc = cr.getClock();
   JcompType typ = cv.getDataType(cc);
   
   if (typ == INT_TYPE) {
      cv = invokeEvalConverter(cr,"java.lang.Integer","valueOf","(I)Ljava/lang/Integer;",cv);
    }
   else if (typ == SHORT_TYPE) {
      cv = invokeEvalConverter(cr,"java.lang.Short","valueOf","(IS)Ljava/lang/Short;",cv);
    }
   else if (typ == BYTE_TYPE) {
      cv = invokeEvalConverter(cr,"java.lang.Byte","valueOf","(B)Ljava/lang/Byte;",cv);
    }
   else if (typ == CHAR_TYPE) {
      cv = invokeEvalConverter(cr,"java.lang.Character","valueOf","(C)Ljava/lang/Character;",cv);
    }
   else if (typ == FLOAT_TYPE) {
      cv = invokeEvalConverter(cr,"java.lang.Float","valueOf","(F)Ljava/lang/Float;",cv);
    }
   else if (typ == DOUBLE_TYPE) {
      cv = invokeEvalConverter(cr,"java.lang.Double","valueOf","(D)Ljava/lang/Double;",cv);
    }
   else if (typ == BOOLEAN_TYPE) {
      cv = invokeEvalConverter(cr,"java.lang.Boolean","valueOf","(Z)Ljava/lang/Boolean;",cv);
    }
   
   return cv;
}



private static CashewValue invokeConverter(CuminRunner runner,String cls,String mthd,String sgn,
      CashewValue arg)
{
   JcodeClass mcls = runner.getCodeFactory().findClass(cls);
   JcodeMethod mmthd = mcls.findMethod(mthd,sgn);
   List<CashewValue> args = new ArrayList<CashewValue>();
   args.add(arg);
   CuminRunner cr = runner.doCall(runner.getClock(),mmthd,args);
   try {
      cr.interpret(EvalType.RUN);
    }
   catch (CuminRunError r) {
      if (r.getReason() == CuminRunError.Reason.RETURN) {
         return r.getValue();
       }
      else throw r;
    }

   return arg;
}



private static CashewValue invokeEvalConverter(CuminRunner runner,String cls,String mthd,String sgn,
      CashewValue arg)
{
   String cast = arg.getDataType(runner.getClock()).getName();
   String argv = arg.getString(runner.getClock());
   String expr = cls + "." + mthd + "( (" + cast + ") " + argv + ")";
   CashewValue cv = runner.getLookupContext().evaluate(expr);
   if (cv != null) return cv;
   
   return invokeConverter(runner,cls,mthd,sgn,arg);
}



static CashewValue evaluate(CashewClock cc,CuminOperator op,CashewValue v1)
{
   JcompType t1 = v1.getDataType(cc);
   boolean isflt = t1 == FLOAT_TYPE;
   boolean isdbl = t1 == DOUBLE_TYPE;
   boolean islng = t1 == LONG_TYPE;
   
   CashewValue rslt = null;
   switch (op) {
      case POSTINCR :
         if (isflt) {
            rslt = v1.getActualValue(cc);
            float fnv = v1.getNumber(cc).floatValue() + 1;
            CashewValue nv = CashewValue.numericValue(FLOAT_TYPE,fnv);
            v1.setValueAt(cc,nv);
          }
         else if (isdbl) {
            rslt = v1.getActualValue(cc);
            double fnv = v1.getNumber(cc).doubleValue() + 1;
            CashewValue nv = CashewValue.numericValue(DOUBLE_TYPE,fnv);
            v1.setValueAt(cc,nv);
          }
         else if (islng) {
            rslt = v1.getActualValue(cc);
            long fnv = v1.getNumber(cc).longValue() + 1;
            CashewValue nv = CashewValue.numericValue(LONG_TYPE,fnv);
            v1.setValueAt(cc,nv);
          }
         else {
            rslt = v1.getActualValue(cc);
            int fnv = v1.getNumber(cc).intValue() + 1;
            CashewValue nv = CashewValue.numericValue(INT_TYPE,fnv);
            v1.setValueAt(cc,nv);
          }
         break;
      case POSTDECR :
         if (isflt) {
            rslt = v1.getActualValue(cc);
            float fnv = v1.getNumber(cc).floatValue() - 1;
            CashewValue nv = CashewValue.numericValue(FLOAT_TYPE,fnv);
            v1.setValueAt(cc,nv);
          }
         else if (isdbl) {
            rslt = v1.getActualValue(cc);
            double fnv = v1.getNumber(cc).doubleValue() - 1;
            CashewValue nv = CashewValue.numericValue(DOUBLE_TYPE,fnv);
            v1.setValueAt(cc,nv);
          }
         else if (islng) {
            rslt = v1.getActualValue(cc);
            long fnv = v1.getNumber(cc).longValue() - 1;
            CashewValue nv = CashewValue.numericValue(LONG_TYPE,fnv);
            v1.setValueAt(cc,nv);
          }
         else {
            rslt = v1.getActualValue(cc);
            int fnv = v1.getNumber(cc).intValue() - 1;
            CashewValue nv = CashewValue.numericValue(INT_TYPE,fnv);
            v1.setValueAt(cc,nv);
          }
         break;     
      case INCR :
         if (isflt) {
            float fnv = v1.getNumber(cc).floatValue() + 1;
            rslt = CashewValue.numericValue(FLOAT_TYPE,fnv);
            v1.setValueAt(cc,rslt);
          }
         else if (isdbl) {
            double fnv = v1.getNumber(cc).doubleValue() + 1;
            rslt = CashewValue.numericValue(DOUBLE_TYPE,fnv);
            v1.setValueAt(cc,rslt);
          }
         else if (islng) {
            long fnv = v1.getNumber(cc).longValue() + 1;
            rslt = CashewValue.numericValue(LONG_TYPE,fnv);
            v1.setValueAt(cc,rslt);
          }
         else {
            int fnv = v1.getNumber(cc).intValue() + 1;
            rslt = CashewValue.numericValue(INT_TYPE,fnv);
            v1.setValueAt(cc,rslt);
          }
         break;        
      case DECR :
         if (isflt) {
            float fnv = v1.getNumber(cc).floatValue() - 1;
            rslt = CashewValue.numericValue(FLOAT_TYPE,fnv);
            v1.setValueAt(cc,rslt);
          }
         else if (isdbl) {
            double fnv = v1.getNumber(cc).doubleValue() - 1;
            rslt = CashewValue.numericValue(DOUBLE_TYPE,fnv);
            v1.setValueAt(cc,rslt);
          }
         else if (islng) {
            long fnv = v1.getNumber(cc).longValue() - 1;
            rslt = CashewValue.numericValue(LONG_TYPE,fnv);
            v1.setValueAt(cc,rslt);
          }
         else {
            int fnv = v1.getNumber(cc).intValue() - 1;
            rslt = CashewValue.numericValue(INT_TYPE,fnv);
            v1.setValueAt(cc,rslt);
          }
         break; 
      case NEG :
         if (isflt) {
            float fnv = - v1.getNumber(cc).floatValue();
            rslt = CashewValue.numericValue(FLOAT_TYPE,fnv);
          }
         else if (isdbl) {
            double fnv = -v1.getNumber(cc).doubleValue();
            rslt = CashewValue.numericValue(DOUBLE_TYPE,fnv);
          }
         else if (islng) {
            long fnv = -v1.getNumber(cc).longValue();
            rslt = CashewValue.numericValue(LONG_TYPE,fnv);
          }
         else {
            int fnv = -v1.getNumber(cc).intValue();
            rslt = CashewValue.numericValue(INT_TYPE,fnv);
          }
         break;   
      case NOP :
         rslt = v1;
         break;
      default :
         // error -- illegal unary operator
         break;
    }
   
   return rslt;
}





}       // end of class CuminEvaluator




/* end of CuminEvaluator.java */


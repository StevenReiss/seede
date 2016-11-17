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



static CashewValue evaluateAssign(CashewClock cc,CuminOperator op,CashewValue v1,CashewValue v2,JcompType tgt)
{
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
   
   assignValue(cc,v1,rslt,tgt);
   
   return rslt;
}



static void assignValue(CashewClock cc,CashewValue vr,CashewValue cv,JcompType tgt)
{
   cv = castValue(cc,cv,tgt);
   cv = cv.getActualValue(cc);
   vr.setValueAt(cc,cv);
}


static CashewValue castValue(CashewClock cc,CashewValue cv,JcompType target) 
{
   // handle casting
   // handle boxing
   
   return cv;
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


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
import edu.brown.cs.seede.cashew.CashewClock;
import edu.brown.cs.seede.cashew.CashewValue;

class CuminEvaluator implements CuminConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private static JcompType        string_type;
private static JcompType        double_type;
private static JcompType        float_type;
private static JcompType        int_type;
private static JcompType        long_type;
private static JcompType        boolean_type;




/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/




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
   boolean isstr = t1 == string_type || t2 == string_type;
   boolean isflt = t1 == float_type || t2 == float_type;
   boolean isdbl = t1 == double_type || t2 == double_type;
   boolean islng = t1 == long_type || t2 == long_type;
   Boolean crslt = null;
   
   switch (op) {
      case ADD :
         if (isstr) {
            String s0 = v1.getString(cc) + v2.getString(cc);
            rslt = CashewValue.stringValue(string_type,s0);
          }
         else if (isdbl) {
            double v0 = v1.getNumber(cc).doubleValue() + v2.getNumber(cc).doubleValue();
            rslt = CashewValue.numericValue(double_type,v0);
          }
         else if (isflt) {
            double v0 = v1.getNumber(cc).doubleValue() + v2.getNumber(cc).doubleValue();
            rslt = CashewValue.numericValue(double_type,v0);
          }
         else if (islng) {
            long v0 = v1.getNumber(cc).longValue() + v2.getNumber(cc).longValue();
            rslt = CashewValue.numericValue(long_type,v0);
          }
         else {
            int v0 = v1.getNumber(cc).intValue() + v2.getNumber(cc).intValue();
            rslt = CashewValue.numericValue(int_type,v0);
          }
         break;
      case AND :
         if (islng) {
            long v0 = v1.getNumber(cc).longValue() & v2.getNumber(cc).longValue();
            rslt = CashewValue.numericValue(long_type,v0);
          }
         else {
            int v0 = v1.getNumber(cc).intValue() & v2.getNumber(cc).intValue();
            rslt = CashewValue.numericValue(int_type,v0);
          }
         break;  
      case DIV :
         if (isdbl) {
            double v0 = v1.getNumber(cc).doubleValue() / v2.getNumber(cc).doubleValue();
            rslt = CashewValue.numericValue(double_type,v0);
          }
         else if (isflt) {
            double v0 = v1.getNumber(cc).doubleValue() / v2.getNumber(cc).doubleValue();
            rslt = CashewValue.numericValue(double_type,v0);
          }
         else if (islng) {
            long v0 = v1.getNumber(cc).longValue() / v2.getNumber(cc).longValue();
            rslt = CashewValue.numericValue(long_type,v0);
          }
         else {
            int v0 = v1.getNumber(cc).intValue() / v2.getNumber(cc).intValue();
            rslt = CashewValue.numericValue(int_type,v0);
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
            rslt = CashewValue.numericValue(long_type,v0);
          }
         else {
            int v0 = v1.getNumber(cc).intValue() << v2.getNumber(cc).intValue();
            rslt = CashewValue.numericValue(int_type,v0);
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
         if (islng) {
            long v0 = v1.getNumber(cc).longValue() % v2.getNumber(cc).longValue();
            rslt = CashewValue.numericValue(long_type,v0);
          }
         else {
            int v0 = v1.getNumber(cc).intValue() % v2.getNumber(cc).intValue();
            rslt = CashewValue.numericValue(int_type,v0);
          }
         break; 
      case MUL :
         if (isdbl) {
            double v0 = v1.getNumber(cc).doubleValue() * v2.getNumber(cc).doubleValue();
            rslt = CashewValue.numericValue(double_type,v0);
          }
         else if (isflt) {
            double v0 = v1.getNumber(cc).doubleValue() * v2.getNumber(cc).doubleValue();
            rslt = CashewValue.numericValue(double_type,v0);
          }
         else if (islng) {
            long v0 = v1.getNumber(cc).longValue() * v2.getNumber(cc).longValue();
            rslt = CashewValue.numericValue(long_type,v0);
          }
         else {
            int v0 = v1.getNumber(cc).intValue() * v2.getNumber(cc).intValue();
            rslt = CashewValue.numericValue(int_type,v0);
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
            rslt = CashewValue.numericValue(long_type,v0);
          }
         else {
            int v0 = v1.getNumber(cc).intValue() | v2.getNumber(cc).intValue();
            rslt = CashewValue.numericValue(int_type,v0);
          }
         break; 
      case RSH : 
         if (islng) {
            long v0 = v1.getNumber(cc).longValue() >> v2.getNumber(cc).intValue();
            rslt = CashewValue.numericValue(long_type,v0);
          }
         else {
            int v0 = v1.getNumber(cc).intValue() >> v2.getNumber(cc).intValue();
            rslt = CashewValue.numericValue(int_type,v0);
          }
         break;    
      case RSHU :
         if (islng) {
            long v0 = v1.getNumber(cc).longValue() >>> v2.getNumber(cc).intValue();
            rslt = CashewValue.numericValue(long_type,v0);
          }
         else {
            int v0 = v1.getNumber(cc).intValue() >>> v2.getNumber(cc).intValue();
            rslt = CashewValue.numericValue(int_type,v0);
          }
         break; 
      case SUB :    
         if (isdbl) {
            double v0 = v1.getNumber(cc).doubleValue() - v2.getNumber(cc).doubleValue();
            rslt = CashewValue.numericValue(double_type,v0);
          }
         else if (isflt) {
            double v0 = v1.getNumber(cc).doubleValue() - v2.getNumber(cc).doubleValue();
            rslt = CashewValue.numericValue(double_type,v0);
          }
         else if (islng) {
            long v0 = v1.getNumber(cc).longValue() - v2.getNumber(cc).longValue();
            rslt = CashewValue.numericValue(long_type,v0);
          }
         else {
            int v0 = v1.getNumber(cc).intValue() - v2.getNumber(cc).intValue();
            rslt = CashewValue.numericValue(int_type,v0);
          }
         break;
      case XOR :
         if (islng) {
            long v0 = v1.getNumber(cc).longValue() ^ v2.getNumber(cc).longValue();
            rslt = CashewValue.numericValue(long_type,v0);
          }
         else {
            int v0 = v1.getNumber(cc).intValue() ^ v2.getNumber(cc).intValue();
            rslt = CashewValue.numericValue(int_type,v0);
          }
         break;  
      default :
         // illegal binary operator
         break;
    }
   
   if (rslt == null) {
      if (crslt != null) {
         rslt = CashewValue.numericValue(boolean_type,crslt.booleanValue() ? 1 : 0);
       }
    }
   
   return rslt;
}



static CashewValue evaluate(CashewClock cc,CuminOperator op,CashewValue v1)
{
   JcompType t1 = v1.getDataType(cc);
   boolean isflt = t1 == float_type;
   boolean isdbl = t1 == double_type;
   boolean islng = t1 == long_type;
   
   CashewValue rslt = null;
   switch (op) {
      case POSTINCR :
         if (isflt) {
            rslt = v1.getActualValue(cc);
            float fnv = v1.getNumber(cc).floatValue() + 1;
            CashewValue nv = CashewValue.numericValue(float_type,fnv);
            v1.setValueAt(cc,nv);
          }
         else if (isdbl) {
            rslt = v1.getActualValue(cc);
            double fnv = v1.getNumber(cc).doubleValue() + 1;
            CashewValue nv = CashewValue.numericValue(double_type,fnv);
            v1.setValueAt(cc,nv);
          }
         else if (islng) {
            rslt = v1.getActualValue(cc);
            long fnv = v1.getNumber(cc).longValue() + 1;
            CashewValue nv = CashewValue.numericValue(long_type,fnv);
            v1.setValueAt(cc,nv);
          }
         else {
            rslt = v1.getActualValue(cc);
            int fnv = v1.getNumber(cc).intValue() + 1;
            CashewValue nv = CashewValue.numericValue(int_type,fnv);
            v1.setValueAt(cc,nv);
          }
         break;
      case POSTDECR :
         if (isflt) {
            rslt = v1.getActualValue(cc);
            float fnv = v1.getNumber(cc).floatValue() - 1;
            CashewValue nv = CashewValue.numericValue(float_type,fnv);
            v1.setValueAt(cc,nv);
          }
         else if (isdbl) {
            rslt = v1.getActualValue(cc);
            double fnv = v1.getNumber(cc).doubleValue() - 1;
            CashewValue nv = CashewValue.numericValue(double_type,fnv);
            v1.setValueAt(cc,nv);
          }
         else if (islng) {
            rslt = v1.getActualValue(cc);
            long fnv = v1.getNumber(cc).longValue() - 1;
            CashewValue nv = CashewValue.numericValue(long_type,fnv);
            v1.setValueAt(cc,nv);
          }
         else {
            rslt = v1.getActualValue(cc);
            int fnv = v1.getNumber(cc).intValue() - 1;
            CashewValue nv = CashewValue.numericValue(int_type,fnv);
            v1.setValueAt(cc,nv);
          }
         break;     
      case INCR :
         if (isflt) {
            float fnv = v1.getNumber(cc).floatValue() + 1;
            rslt = CashewValue.numericValue(float_type,fnv);
            v1.setValueAt(cc,rslt);
          }
         else if (isdbl) {
            double fnv = v1.getNumber(cc).doubleValue() + 1;
            rslt = CashewValue.numericValue(double_type,fnv);
            v1.setValueAt(cc,rslt);
          }
         else if (islng) {
            long fnv = v1.getNumber(cc).longValue() + 1;
            rslt = CashewValue.numericValue(long_type,fnv);
            v1.setValueAt(cc,rslt);
          }
         else {
            int fnv = v1.getNumber(cc).intValue() + 1;
            rslt = CashewValue.numericValue(int_type,fnv);
            v1.setValueAt(cc,rslt);
          }
         break;        
      case DECR :
         if (isflt) {
            float fnv = v1.getNumber(cc).floatValue() - 1;
            rslt = CashewValue.numericValue(float_type,fnv);
            v1.setValueAt(cc,rslt);
          }
         else if (isdbl) {
            double fnv = v1.getNumber(cc).doubleValue() - 1;
            rslt = CashewValue.numericValue(double_type,fnv);
            v1.setValueAt(cc,rslt);
          }
         else if (islng) {
            long fnv = v1.getNumber(cc).longValue() - 1;
            rslt = CashewValue.numericValue(long_type,fnv);
            v1.setValueAt(cc,rslt);
          }
         else {
            int fnv = v1.getNumber(cc).intValue() - 1;
            rslt = CashewValue.numericValue(int_type,fnv);
            v1.setValueAt(cc,rslt);
          }
         break; 
      case NEG :
         if (isflt) {
            float fnv = - v1.getNumber(cc).floatValue();
            rslt = CashewValue.numericValue(float_type,fnv);
          }
         else if (isdbl) {
            double fnv = -v1.getNumber(cc).doubleValue();
            rslt = CashewValue.numericValue(double_type,fnv);
          }
         else if (islng) {
            long fnv = -v1.getNumber(cc).longValue();
            rslt = CashewValue.numericValue(long_type,fnv);
          }
         else {
            int fnv = -v1.getNumber(cc).intValue();
            rslt = CashewValue.numericValue(int_type,fnv);
          }
         break;   
      case NOP :
         rslt = v1;
         break;
    }
   
   return rslt;
}





}       // end of class CuminEvaluator




/* end of CuminEvaluator.java */


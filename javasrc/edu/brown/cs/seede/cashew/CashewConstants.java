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

import edu.brown.cs.ivy.jcomp.JcompControl;
import edu.brown.cs.ivy.jcomp.JcompType;



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
/*      Access to Jcomp                                                         */
/*                                                                              */
/********************************************************************************/

JcompControl JCOMP_BASE = new JcompControl();

JcompType INT_TYPE = JCOMP_BASE.getSystemType("int");
JcompType SHORT_TYPE = JCOMP_BASE.getSystemType("short");
JcompType CHAR_TYPE = JCOMP_BASE.getSystemType("char");
JcompType BYTE_TYPE = JCOMP_BASE.getSystemType("byte");
JcompType LONG_TYPE = JCOMP_BASE.getSystemType("long");
JcompType FLOAT_TYPE = JCOMP_BASE.getSystemType("float");
JcompType DOUBLE_TYPE = JCOMP_BASE.getSystemType("double");
JcompType BOOLEAN_TYPE = JCOMP_BASE.getSystemType("boolean");
JcompType VOID_TYPE = JCOMP_BASE.getSystemType("void");
JcompType NULL_TYPE = JCOMP_BASE.getSystemType("*ANY*");

JcompType STRING_TYPE = JCOMP_BASE.getSystemType("java.lang.String");
JcompType OBJECT_TYPE = JCOMP_BASE.getSystemType("java.lang.Object");
JcompType CLASS_TYPE = JCOMP_BASE.getSystemType("java.lang.Class");

JcompType INT_BOX_TYPE = JCOMP_BASE.getSystemType("java.lang.Integer");
JcompType SHORT_BOX_TYPE = JCOMP_BASE.getSystemType("java.lang.Short");
JcompType CHAR_BOX_TYPE = JCOMP_BASE.getSystemType("java.lang.Character");
JcompType BYTE_BOX_TYPE = JCOMP_BASE.getSystemType("java.lang.Byte");
JcompType LONG_BOX_TYPE = JCOMP_BASE.getSystemType("java.lang.Long");
JcompType FLOAT_BOX_TYPE = JCOMP_BASE.getSystemType("java.lang.Float");
JcompType DOUBLE_BOX_TYPE = JCOMP_BASE.getSystemType("java.lang.Double");
JcompType BOOLEAN_BOX_TYPE = JCOMP_BASE.getSystemType("java.lang.Boolean");





}       // end of interface CashewConstants




/* end of CashewConstants.java */


/********************************************************************************/
/*										*/
/*		SesameConstants.java						*/
/*										*/
/*	SEEDE Management Environment constant definitions			*/
/*										*/
/********************************************************************************/
/*	Copyright 2016 Brown University -- Steven P. Reiss		      */
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



package edu.brown.cs.seede.sesame;

import java.util.Random;



public interface SesameConstants {


String	  SOURCE_ID = "SEEDE_" + (new Random().nextInt(1000000));

String BOARD_MINT_NAME = "BUBBLES_" + System.getProperty("user.name").replace(" ","_");



/********************************************************************************/
/*                                                                              */
/*      Value definitions                                                       */
/*                                                                              */
/********************************************************************************/

enum ValueKind {
   UNKNOWN, PRIMITIVE, STRING, CLASS, OBJECT, ARRAY
}


String LOCAL_FILE_DIR_PREFIX = "SEEDE_LOCAL_FILE";
String LOCAL_FILE_NAME_PREFIX = "SEEDE_";








}	// end of interface SesameConstants




/* end of SesameConstants.java */

/********************************************************************************/
/*                                                                              */
/*              CuminRunner.java                                                */
/*                                                                              */
/*      Generic code runner (interpreter)                                       */
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

import edu.brown.cs.seede.cashew.CashewValue;

public abstract class CuminRunner implements CuminConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private List<CashewValue>        initial_parameters;


/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

protected CuminRunner()
{
   initial_parameters = new ArrayList<>();
}



/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

protected List<CashewValue> getInitialParameters()
{
   return initial_parameters;
}


}       // end of class CuminRunner




/* end of CuminRunner.java */


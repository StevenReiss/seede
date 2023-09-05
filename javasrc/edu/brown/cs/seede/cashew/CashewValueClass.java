/********************************************************************************/
/*                                                                              */
/*              CashewValueClass.java                                           */
/*                                                                              */
/*      Representation of java.lang.Class objects                               */
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

import edu.brown.cs.ivy.jcomp.JcompType;
import edu.brown.cs.ivy.jcomp.JcompTyper;
import edu.brown.cs.ivy.xml.IvyXmlWriter;
import edu.brown.cs.seede.acorn.AcornLog;

public class CashewValueClass extends CashewValueObject implements CashewConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private JcompType       class_value;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

CashewValueClass(JcompTyper typer,JcompType c) 
{
   super(typer,typer.CLASS_TYPE,null,false);
   class_value = c;
}




/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

public JcompType getJcompType()                         { return class_value; }



@Override public String getString(CashewValueSession sess,
      JcompTyper typer,CashewClock cc,int idx,boolean dbg) 
{
   return class_value.toString();
}


@Override public CashewValue getFieldValue(CashewValueSession sess,
      JcompTyper typer,CashewClock cc,String nm,CashewContext ctx,boolean force)
{
   switch (nm) {
      case "java.lang.Class.packageName" :
      case "packageName" :
         return CashewValue.stringValue(typer,typer.STRING_TYPE,class_value.getPackageName());
      case "java.lang.Class.name" :
      case "name" :
         return CashewValue.stringValue(typer,typer.STRING_TYPE,class_value.getName());
      case "@hashCode" :
         return CashewValue.numericValue(typer,typer.INT_TYPE,class_value.hashCode());
      case "java.lang.Class.module" :
      case "module" :
         String cnm = class_value.getName();
         String eval = "edu.brown.cs.seede.poppy.PoppyValue.getClassModule(\"" + cnm + "\")";
         return ctx.evaluate(eval);
         
      default :
         AcornLog.logE("CASHEW","Unknown Class field: " + nm);
         break;
    }
   
   return null;
}



/********************************************************************************/
/*                                                                              */
/*      Output methods                                                          */
/*                                                                              */
/********************************************************************************/

@Override public void outputLocalXml(IvyXmlWriter xw,CashewOutputContext outctx,String name)
{
   xw.field("OBJECT",true);
   if (class_value == null) xw.field("CLASS","*UNKNOWN*");
   else xw.field("CLASS",class_value.toString());
}


}       // end of class CashewValueClass




/* end of CashewValueClass.java */


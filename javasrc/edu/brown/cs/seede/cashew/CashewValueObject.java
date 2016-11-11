/********************************************************************************/
/*                                                                              */
/*              CashewValueObject.java                                          */
/*                                                                              */
/*      Object Value representation                                             */
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

import java.util.HashMap;
import java.util.Map;

abstract public class CashewValueObject extends CashewValue implements CashewConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private storage                                                         */
/*                                                                              */
/********************************************************************************/

private Map<String,JcompType>   object_fields;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

CashewValueObject(JcompType jt) 
{
   super(jt);
   
   object_fields = jt.getFields();
}



/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

protected Map<String,JcompType> getAllFields()  { return object_fields; }

@Override abstract public CashewValue getFieldValue(CashewClock cc,String nm); 
@Override public abstract CashewValue setFieldValue(CashewClock cc,String nm,CashewValue cv);

public void addField(String name,JcompType type) 
{
   object_fields.put(name,type);
}


@Override public String getString(CashewClock cc) {
   StringBuffer buf = new StringBuffer();
   buf.append("{");
   int ctr = 0;
   for (String fldname : object_fields.keySet()) {
      if (ctr++ == 0) buf.append(",");
      buf.append(fldname);
      buf.append(":");
      buf.append(getFieldValue(cc,fldname));
    }
   buf.append("}");
   return buf.toString();
}



/********************************************************************************/
/*                                                                              */
/*      Computed Object value                                                   */
/*                                                                              */
/********************************************************************************/

static class ComputedValueObject extends CashewValueObject {
   
   private final Map<String,CashewRef> field_values;
   
   ComputedValueObject(JcompType jt) {
      super(jt);
      field_values = new HashMap<String,CashewRef>();
      for (Map.Entry<String,JcompType> ent : getAllFields().entrySet()) {
         field_values.put(ent.getKey(),new CashewRef());
       }
    }
   
   @Override public CashewValue getFieldValue(CashewClock cc,String nm) {
      CashewValue cv = field_values.get(nm);
      if (cv == null) {
         throw new Error("UndefinedField");
       }
      return cv;
    }
   
   @Override public CashewValue setFieldValue(CashewClock cc,String nm,CashewValue cv) {
      CashewRef ov = field_values.get(nm);
      if (ov == null) {
         throw new Error("UndefinedField");
       }
      ov.setValueAt(cc,cv);
      return this;
    }
   
}       // end of inner class ComputedValueObject



/********************************************************************************/
/*                                                                              */
/*     Class Object value                                                       */
/*                                                                              */
/********************************************************************************/

static class ValueClass extends ComputedValueObject 
{
   private JcompType     class_value;

   ValueClass(JcompType c) {
      super(CLASS_TYPE);
      class_value = c;
    }

   @Override public String getString(CashewClock cc) {
      return class_value.toString();
    }

}       // end of inner class ValueClass





}       // end of class CashewValueObject




/* end of CashewValueObject.java */


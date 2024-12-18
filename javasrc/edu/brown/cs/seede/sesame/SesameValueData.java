/********************************************************************************/
/*										*/
/*		SesameValueData.java						*/
/*										*/
/*	Hold value returned from Bubbles					*/
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



package edu.brown.cs.seede.sesame;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Element;

import edu.brown.cs.ivy.jcomp.JcompType;
import edu.brown.cs.ivy.jcomp.JcompTyper;
import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.ivy.mint.MintConstants.CommandArgs;
import edu.brown.cs.seede.acorn.AcornLog;
import edu.brown.cs.seede.cashew.CashewConstants;
import edu.brown.cs.seede.cashew.CashewException;
import edu.brown.cs.seede.cashew.CashewValue;
import edu.brown.cs.seede.cashew.CashewConstants.CashewValueSession;

class SesameValueData implements SesameConstants
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private ValueKind val_kind; 
private String val_name;
private String val_expr;
private String val_type;
private String val_value;
private String val_thread;
private boolean has_values;
private boolean is_local;
private boolean is_static;
private int array_length;
private Map<String,SesameValueData> sub_values;
private CashewValue result_value;
private int hash_code;
private String decl_type;
private CashewValueSession value_session;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

SesameValueData(SesameSessionLaunch sess,String thread,Element xml,String name)
{
   val_thread = thread;
   if (name == null) val_name = IvyXml.getAttrString(xml,"NAME");
   else val_name = name;
   val_expr = null;
   initialize(sess,xml,null);
}

SesameValueData(SesameSessionLaunch sess,SesameValueData par,Element xml)
{
   val_thread = par.val_thread;
   String vnm = IvyXml.getAttrString(xml,"NAME");
   if (par.val_expr != null) {
      val_expr = par.val_expr + "." + vnm;
    }
   String cnm = IvyXml.getAttrString(xml,"DECLTYPE");
   if (cnm != null) {
      vnm = getFieldKey(vnm,cnm);
    }
   val_name = par.val_name + "?" + vnm;

   initialize(sess,xml,val_expr);
}


SesameValueData(CashewValue cv)
{
   val_thread = null;
   val_name = null;
   val_expr = null;
   initialize(null,null,null);
   result_value = cv;
}




/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

ValueKind getKind()		        { return val_kind; }

String getType()		        { return val_type; }
String getValue()		        { return val_value; }

String getActualType()		        { return null; }
boolean hasContents()		        { return has_values; }
boolean isLocal()		        { return is_local; }
boolean isStatic()		        { return is_static; }
String getFrame(SesameSessionLaunch s)	{ return s.getFrameId(val_thread); }
String getThread()		        { return val_thread; }
int getLength() 		        { return array_length; }


//CHECKSTYLE:OFF
CashewValue getCashewValue(SesameSessionLaunch sess)
//CHECKSTYLE:ON
{
   if (result_value != null) return result_value;
   
   if (sess == null) {
      AcornLog.logE("Value has unknown session " + val_kind + " " + val_type + " " + val_expr + " " +
            val_value + " " + val_thread + " " + is_local + " " + is_static + " " + hash_code);
    }
   
   JcompTyper typer = sess.getProject().getTyper();
   String addall = null;

   if (val_kind == ValueKind.UNKNOWN && val_type == null) {
      return null;
    }

   if (val_type != null && val_type.equals("null")) {
      return CashewValue.nullValue(typer);
    }
   if (val_type != null && val_type.equals("void")) return null;

   String vtype = val_type;
   if (vtype != null) {
      int idx = vtype.indexOf("<");
      int idx1 = vtype.lastIndexOf(">");
      if (idx >= 0) {
	 vtype = val_type.substring(0,idx);
	 if (idx1 > 0) vtype += val_type.substring(idx1+1);
       }
    }

   JcompType typ = typer.findType(val_type);
   if (typ == null && val_type != null) {
      String ityp = val_type.replace("$",".");
      typ = typer.findType(ityp);
    }
   if (typ == null && vtype != null) {
      typ = typer.findType(vtype);
    }
   if (typ == null && vtype != null) {
      String ityp = vtype.replace("$",".");
      typ = typer.findType(ityp);
    }
   if (typ == null) {
      typ = typer.findSystemType(val_type);
    }
   if (typ == null) {
      typ = typer.findSystemType(vtype);
    }
   int lamidx = val_type.indexOf("$$Lambda$");
   if (typ == null && lamidx > 0 && decl_type != null) {
      // this doesnt't work -- need to find actual type for the lambda
      String ltyp = val_type.substring(0,lamidx);
      ltyp = ltyp.replace("$",".");
      // need to find type of the lambda at this point
      switch (ltyp) {
         case "java.util.stream.FindOps.FindSink.OfInt" :
            ltyp = "java.util.Optional";
            break;
       }
      typ = typer.findType(ltyp);
      if  (typ == null) {
         String ityp = decl_type.replace("$",".");
         typ = typer.findType(ityp);
       }
    }
   if (typ == null && val_type.startsWith("java.lang.invoke.VarHandleObjects")) {
      String ltyp = "java.lang.invoke.VarHandle";
      typ = typer.findType(ltyp);
      addall = ltyp;
    }
   if (typ == null) {
      AcornLog.logE("TYPE " + val_type +  " " + vtype + " not found");
      return CashewValue.nullValue(typer);
    }

   switch (val_kind) {
      case PRIMITIVE :
	 if (typ.isBooleanType()) {
	    result_value = CashewValue.booleanValue(typer,val_value);
	  }
	 else if (typ.isNumericType()) {
	    result_value = CashewValue.numericValue(typer,typ,val_value);
	  }
	 break;
      case STRING :
	 result_value = CashewValue.stringValue(typer,typer.STRING_TYPE,val_value);
	 break;
      case OBJECT :
	 Map<String,Object> inits = new HashMap<>();
	 typ.defineAll(typer);
	 Map<String,SesameValueData> sets = new HashMap<>();
         Map<String,SesameValueData> other = new HashMap<>();
	 for (Map.Entry<String,JcompType> ent : typ.getFields().entrySet()) {
	    String fnm = ent.getKey();
	    String cnm = null;
	    String key = fnm;
	    int idx1 = fnm.lastIndexOf(".");
	    if (idx1 >= 0) {
	       cnm = fnm.substring(0,idx1);
	       key = fnm.substring(idx1+1);
	     }
	    key = getKey(key,cnm);
	    if (sub_values != null && sub_values.get(key) != null) {
	       SesameValueData fsvd = sub_values.get(key);
	       fsvd = sess.getUniqueValue(fsvd);
	       sets.put(fnm,fsvd);
	     }
	    else {
	       DeferredLookup def = new DeferredLookup(fnm);
	       inits.put(fnm,def);
	     }
	  }
         if (addall != null) {
           for (String k : sub_values.keySet()) {
              String k1 = k;
              SesameValueData fsvd = sub_values.get(k);
              int idx1 = k.lastIndexOf("?");
              if (idx1 >= 0) k1 = k.substring(idx1+1);
              int idx2 = k1.lastIndexOf(".");
              if (idx2 > 0) {
                 String cnm = k1.substring(0,idx2);
                 if (cnm.equals(addall)) continue;
               }
              fsvd = sess.getUniqueValue(fsvd);
              other.put(k1,fsvd);
            }
            
          }
	 if (hash_code == 0) {
	    inits.put(CashewConstants.HASH_CODE_FIELD,
                  new DeferredLookup(CashewConstants.HASH_CODE_FIELD));
	  }
	 else {
	    CashewValue hvl = CashewValue.numericValue(typer,typer.INT_TYPE,hash_code);
	    inits.put(CashewConstants.HASH_CODE_FIELD,hvl);
	  }
	 result_value = CashewValue.objectValue(sess,null,typer,
               typ,inits,true);
	
	 for (Map.Entry<String,SesameValueData> ent : sets.entrySet()) {
	    CashewValue cv = ent.getValue().getCashewValue(sess);
	    try {
	       result_value.setFieldValue(sess,typer,null,ent.getKey(),cv);
	     }
	    catch (CashewException e) {
	       AcornLog.logE("Unexpected error setting field value",e);
	     }
	  }
         for (Map.Entry<String,SesameValueData> ent : other.entrySet()) {
            CashewValue cv = ent.getValue().getCashewValue(sess);
            result_value.addFieldValue(sess,typer,null,ent.getKey(),cv);
          }
	 break;
      case ARRAY :
	 if (array_length <= 1024) computeValues(sess);
	 Map<Integer,Object> ainits = new HashMap<Integer,Object>();
	 for (int i = 0; i < array_length; ++i) {
	    String key = "[" + i + "]";
	    String fullkey = getKey(key,null);
	    if (sub_values != null && sub_values.get(fullkey) != null) {
	       SesameValueData fsvd = sub_values.get(fullkey);
	       fsvd = sess.getUniqueValue(fsvd);
	       ainits.put(i,fsvd.getCashewValue(sess));
	     }
	    else {
	       DeferredLookup def = new DeferredLookup(key);
	       ainits.put(i,def);
	     }
	  }
	 result_value = CashewValue.arrayValue(typer,typ,array_length,ainits);
	 // AcornLog.logD("BUILT ARRAY : " + result_value);
	 break;
      case CLASS :
	 int idx2 = val_value.lastIndexOf("(");
	 String tnm = val_value.substring(0,idx2).trim();
	 if (tnm.startsWith("(")) {
	    idx2 = tnm.lastIndexOf(")");
	    tnm = tnm.substring(1,idx2).trim();
	  }
	 JcompType ctyp = typer.findType(tnm);
	 if (ctyp == null) ctyp = typer.findSystemType(tnm);
	 if (ctyp == null) {
	    int idx = tnm.indexOf("<");
	    if (idx > 0) {
	       tnm = tnm.substring(0,idx);
	       ctyp = typer.findSystemType(tnm);
	     }
	  }
	 if (ctyp == null) {
	    AcornLog.logE("Can't find type " + tnm + " for " + val_value);
	  }
	 result_value = CashewValue.classValue(typer,ctyp);
	 break;
      case UNKNOWN :
	 break;
    }

   if (result_value == null) {
      AcornLog.logE("Unknown conversion to cashew value from bubbles");
    }

   return result_value;
}


private String getKey(String fnm,String cnm)
{
   if (fnm.equals(CashewConstants.HASH_CODE_FIELD)) return fnm;

   String knm = getFieldKey(fnm,cnm);

   return val_name + "?" + knm;
}



private String getFieldKey(String fnm,String cnm)
{
   if (fnm.equals(CashewConstants.HASH_CODE_FIELD)) return fnm;

   if (fnm.startsWith("[")) return fnm;

   if (cnm == null) {
      System.err.println("CHECK NULL HERE");
    }

   if (cnm != null) return cnm.replace("$",".") + "." + fnm;

   return fnm;
}




String findValue(CashewValue cv,int lvl)
{
   if (result_value == null) return null;
   if (result_value == cv) return "";
   if (lvl == 0 || sub_values == null) return null;

   for (Map.Entry<String,SesameValueData> ent : sub_values.entrySet()) {
      String r = ent.getValue().findValue(cv,lvl-1);
      if (r != null) {
	 if (array_length > 0) {
	    return "[" + ent.getKey() + "]";
	  }
	 else return "." + ent.getKey();
       }
    }

   return null;
}



/********************************************************************************/
/*										*/
/*	Setup methods								*/
/*										*/
/********************************************************************************/

private void initialize(SesameSessionLaunch sess,Element xml,String expr)
{
   val_type = IvyXml.getAttrString(xml,"TYPE");
   decl_type = IvyXml.getAttrString(xml,"DECLTYPE");
   if (val_type != null && val_type.equals("edu.brown.cs.seede.poppy.PoppyValue$Return")) {
      Element objxml = null;
      int refid = 0;
      int hashcode = 0;
      for (Element celt : IvyXml.children(xml,"VALUE")) {
	 switch (IvyXml.getAttrString(celt,"NAME")) {
	    case "for_object" :
	       objxml = celt;
	       break;
	    case "ref_id" :
	       refid = Integer.parseInt(IvyXml.getTextElement(celt,"DESCRIPTION"));
	       break;
	    case "hash_code" :
	       hashcode = Integer.parseInt(IvyXml.getTextElement(celt,"DESCRIPTION"));
	       break;
	  }
       }
      val_type = IvyXml.getAttrString(objxml,"TYPE");
      String nexpr = "edu.brown.cs.seede.poppy.PoppyValue.getValue(" + refid + ")";
      nexpr = "((" + val_type + ") " + nexpr + ")";
      initialize(sess,objxml,nexpr);
      hash_code = hashcode;
      return;
    }

   val_kind = IvyXml.getAttrEnum(xml,"KIND",ValueKind.UNKNOWN);
   val_value = IvyXml.getTextElement(xml,"DESCRIPTION");
   if (val_value == null) val_value = "";
   if (val_kind == ValueKind.STRING && IvyXml.getAttrBool(xml,"CHARS")) {
      val_value = IvyXml.decodeCharacters(val_value,IvyXml.getAttrInt(xml,"LENGTH"));
    }
   has_values = IvyXml.getAttrBool(xml,"HASVARS");
   is_local = IvyXml.getAttrBool(xml,"LOCAL");
   is_static = IvyXml.getAttrBool(xml,"STATIC");
   array_length = IvyXml.getAttrInt(xml,"LENGTH",0);
   sub_values = null;
   hash_code = 0;
   val_expr = expr;
   value_session = sess;
   addValues(sess,xml);
}


private void addValues(SesameSessionLaunch sess,Element xml)
{
   if (xml == null) return;
   for (Element e : IvyXml.children(xml,"VALUE")) {
      if (sub_values == null) sub_values = new HashMap<String,SesameValueData>();
      SesameValueData vd = new SesameValueData(sess,this,e);
      String nm = vd.val_name;
      vd = sess.getUniqueValue(vd);
      sub_values.put(nm,vd);
      AcornLog.logD("SESAME","ADD DEFERRED VALUE " + nm + " = " + vd);
    }
}

private synchronized void computeValues(SesameSessionLaunch sess)
{
   AcornLog.logD("SESAME","Compute deferred values " + has_values + " " +  val_expr + " " + val_name +
         " " + sub_values);
   if (!has_values || sub_values != null) return;
   if (val_expr == null) {
      CommandArgs args = new CommandArgs("FRAME",getFrame(sess),"THREAD",getThread(),"DEPTH",2,
					    "ARRAY",-1);
      String var = "<VAR>" + IvyXml.xmlSanitize(val_name) + "</VAR>";
      Element xml = sess.getControl().getXmlReply("VARVAL",sess.getProject(),args,var,0);
      if (IvyXml.isElement(xml,"RESULT")) {
	 Element root = IvyXml.getChild(xml,"VALUE");
	 addValues(sess,root);
       }
    }
   else {
      SesameValueData svd = sess.evaluateData(val_expr,null,true);
      sub_values = svd.sub_values;
    }
}



void resetType(JcompTyper typer,Set<CashewValue> done)
{
   if (result_value != null) result_value.resetType(value_session,typer,done);
}



/********************************************************************************/
/*										*/
/*	Deferred value lookup							*/
/*										*/
/********************************************************************************/

private class DeferredLookup implements CashewConstants.CashewDeferredValue {

   private String field_name;

   DeferredLookup(String name) {
      field_name = name;
    }

   @Override public CashewValue getValue(CashewValueSession sessobj) {
      AcornLog.logD("SESAME","Start deferred values for " + val_type + " " + val_name);
      SesameSessionLaunch sess = (SesameSessionLaunch) sessobj;
      computeValues(sess);
      if (field_name.equals(CashewConstants.HASH_CODE_FIELD)) {
         if (sub_values == null) sub_values = new HashMap<String,SesameValueData>();
         if (sub_values.get(field_name) == null) {
            SesameValueData svd = null;
            if (val_expr != null) {
               svd = sess.evaluateData("System.identityHashCode(" + val_expr + ")",null,true);
             }
            else {
               CommandArgs args = new CommandArgs("FRAME",getFrame(sess),"THREAD",getThread(),
        	     "DEPTH",1,"ARRAY",-1);
               String var = "<VAR>" + IvyXml.xmlSanitize(val_name) + "?@hashCode</VAR>";
               Element xml = sess.getControl().getXmlReply("VARVAL",sess.getProject(),args,var,0);
               if (IvyXml.isElement(xml,"RESULT")) {
                  svd = new SesameValueData(sess,val_thread,IvyXml.getChild(xml,"VALUE"),null);
                }
             }
            if (svd != null) sub_values.put(field_name,svd);
          }
       }
      
      if (sub_values == null) {
         AcornLog.logE("SESAME","No sub values defined for " + this);
         return null;
       }   
      String fnm = field_name;
      String cnm = null;
      int idx = fnm.lastIndexOf(".");
      if (idx >= 0) {
         cnm = fnm.substring(0,idx);
         fnm = fnm.substring(idx+1);
       }
      String lookup = getKey(fnm,cnm);
      SesameValueData svd = sub_values.get(lookup);
      if (sess != null) svd = sess.getUniqueValue(svd);
      if (svd == null) {
         AcornLog.logE("SESAME","Deferred Lookup of " + lookup + " not found");
         return null;
       }
      CashewValue cvr = svd.getCashewValue(sess);
      AcornLog.logD("SESAME","Deferred Lookup of " + lookup + " = " + cvr.toString(sessobj));
      return cvr;
    }

}	// end of inner class DeferredLookup



/********************************************************************************/
/*										*/
/*	Debugging methods							*/
/*										*/
/********************************************************************************/

@Override public String toString()
{
   StringBuffer buf = new StringBuffer();
   buf.append("<<");
   if (result_value != null && val_kind == ValueKind.UNKNOWN) {
      buf.append(result_value.toString());
    }
   else {
      buf.append(val_kind);
      buf.append(":");
      buf.append(val_type);
      buf.append("@");
      buf.append(val_value);
      if (array_length > 0) buf.append("#" + array_length);
      buf.append(" ");
      buf.append(val_name);
    }
   buf.append(">>");
   return buf.toString();
}




}	// end of class SesameValueData




/* end of SesameValueData.java */


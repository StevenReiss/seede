/********************************************************************************/
/*                                                                              */
/*              SesameSessionLaunch.java                                        */
/*                                                                              */
/*      Session based on a debugger launch                                      */
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



package edu.brown.cs.seede.sesame;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.w3c.dom.Element;

import edu.brown.cs.ivy.jcomp.JcompAst;
import edu.brown.cs.ivy.jcomp.JcompSymbol;
import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.seede.cashew.CashewValue;

class SesameSessionLaunch extends SesameSession
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private String          launch_id;
private String          thread_id;
private String          frame_id;
private String          class_name;
private String          method_name;
private SesameFile      source_file;
private int             line_number;
private Map<String,SesameValueData> value_map;

private static AtomicInteger eval_counter = new AtomicInteger();



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

SesameSessionLaunch(SesameMain sm,String sid,Element xml)
{
   super(sm,sid,xml);
   
   launch_id = IvyXml.getAttrString(xml,"LAUNCHID");
   thread_id = IvyXml.getAttrString(xml,"THREADID");
   value_map = new HashMap<String,SesameValueData>();
   
   loadInitialValues();
}


/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

String getFrameId()                     { return frame_id; }

String getThreadId()                    { return thread_id; }

@Override public List<CashewValue> getCallArgs()
{
   MethodDeclaration md = getCallMethod();
   List<CashewValue> args = new ArrayList<CashewValue>();
   JcompSymbol msym = JcompAst.getDefinition(md.getName());
   if (!msym.isStatic()) {
      SesameValueData svd = value_map.get("this");
      CashewValue cv = svd.getCashewValue();
      args.add(cv);
    }
   for (Object o : md.parameters()) {
      SingleVariableDeclaration svd = (SingleVariableDeclaration) o;
      JcompSymbol psym = JcompAst.getDefinition(svd.getName());
      SesameValueData val = value_map.get(psym.getName());
      args.add(val.getCashewValue());
    }
   
   return args;
}


@Override CashewValue lookupValue(String name,String type)
{
   CashewValue cv = super.lookupValue(name,type);
   if (cv != null) return null;
   
   cv = evaluate(name);
   if (cv != null) return cv;
   
   return cv;
}


@Override CashewValue evaluate(String expr)
{
   String eid = "E_" + eval_counter.incrementAndGet();
   CommandArgs args = new CommandArgs("THREAD",thread_id,
         "FRAME",frame_id,"BREAK",false,"EXPR",expr,
         "LEVEL",4,"REPLYID",eid);
   Element xml = getControl().getXmlReply("EVALUATE",getProject(),args,null,0);
   if (IvyXml.isElement(xml,"RESULT")) {
      Element root = getControl().waitForEvaluation(eid);
      Element v = IvyXml.getChild(root,"EVAL");
      Element v1 = IvyXml.getChild(v,"VALUE");
      SesameValueData svd = new SesameValueData(this,v1);
      return svd.getCashewValue();
    }
   return null;
}



/********************************************************************************/
/*                                                                              */
/*      Launch access methods                                                   */
/*                                                                              */
/********************************************************************************/

private void loadInitialValues()
{
   CommandArgs cargs = new CommandArgs("LAUNCH",launch_id,"THREAD",null,"COUNT",1);
   
   Element stack = sesame_control.getXmlReply("GETSTACKFRAMES",getProject(),cargs,
         null,0);
   stack = IvyXml.getChild(stack,"STACKFRAMES");
   
   for (Element telt : IvyXml.children(stack,"THREAD")) {
      String teid = IvyXml.getAttrString(telt,"ID");
      if (!teid.equals(thread_id)) continue;
      Element frm = IvyXml.getChild(telt,"STACKFRAME"); 
      frame_id = IvyXml.getAttrString(frm,"ID");
      class_name = IvyXml.getAttrString(frm,"RECEIVER");
      method_name = IvyXml.getAttrString(frm,"METHOD");
      String fnm = IvyXml.getAttrString(frm,"FILE");
      File sf = new File(fnm);
      source_file = sesame_control.getFileManager().openFile(sf);
      line_number = IvyXml.getAttrInt(frm,"LINENO");
      for (Element var : IvyXml.children(frm,"VALUE")) {
         String nm = IvyXml.getAttrString(var,"NAME");
         SesameValueData svd = new SesameValueData(this,var);
         svd = getUniqueValue(svd);
         value_map.put(nm,svd);
       }
    }
   
   SesameLocation loc = new SesameLocation(source_file,method_name,line_number);
   addLocation(loc);
}


SesameValueData getUniqueValue(SesameValueData svd)
{
   if (svd == null) return null;
   if (svd.getKind() == ValueKind.OBJECT) {
      String dnm = "MATCH " + svd.getValue();
      SesameValueData nsvd = value_map.get(dnm);
      if (nsvd != null) svd = nsvd;
      else value_map.put(dnm,svd);
    }
   return svd;
}
      

}       // end of class SesameSessionLaunch




/* end of SesameSessionLaunch.java */


/********************************************************************************/
/*										*/
/*		SesameExecRunner.java						*/
/*										*/
/*	Threead to run current execution					*/
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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.MethodDeclaration;

import edu.brown.cs.ivy.mint.MintDefaultReply;
import edu.brown.cs.ivy.xml.IvyXmlWriter;
import edu.brown.cs.seede.acorn.AcornLog;
import edu.brown.cs.seede.cashew.CashewContext;
import edu.brown.cs.seede.cashew.CashewOutputContext;
import edu.brown.cs.seede.cashew.CashewValue;
import edu.brown.cs.seede.cumin.CuminConstants;
import edu.brown.cs.seede.cumin.CuminRunError;
import edu.brown.cs.seede.cumin.CuminRunner;

class SesameExecRunner implements SesameConstants
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private SesameMonitor	for_monitor;
private SesameSession	for_session;
private List<CuminRunner> cumin_runners;
private List<String>    graphics_outputs;
private String		reply_id;
private Map<CuminRunner,RunnerThread> runner_threads;
private Map<CuminRunner,CuminRunError> run_status;
private MasterThread	master_thread;
private boolean 	is_continuous;
private boolean 	run_again;
private List<String>	swing_components;
private SesameContext	base_context;
private long		max_time;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

SesameExecRunner(SesameSession ss,String rid,SesameContext ctx,
      boolean contin,long maxtime,CuminRunner... rs)
{
   for_session = ss;
   for_monitor = ss.getControl().getMonitor();
   base_context = ctx;
   reply_id = rid;
   cumin_runners = new ArrayList<CuminRunner>();
   runner_threads = new HashMap<CuminRunner,RunnerThread>();
   run_status = new HashMap<CuminRunner,CuminRunError>();
   is_continuous = contin;
   swing_components = new ArrayList<>();
   max_time = maxtime;
   graphics_outputs = new ArrayList<>();

   for (CuminRunner r : rs) {
      r.setMaxTime(maxtime);
      cumin_runners.add(r);
    }
   master_thread = null;
   run_again = false;
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

SesameContext getBaseContext()			{ return base_context; }
boolean isContinuous()				{ return is_continuous; }


void addRunner(CuminRunner cr)
{
   addRunner(cr,false);
}


void addRunner(CuminRunner cr,boolean start)
{
   stopCurrentRun();
   cr.setMaxTime(max_time);
   cumin_runners.add(cr);
   if (is_continuous && start) startRunner();
}


void removeRunner(CuminRunner cr)
{
   stopCurrentRun();
   cumin_runners.remove(cr);
   if (is_continuous) startRunner();
}


void removeAllRunners()
{
   stopCurrentRun();
   cumin_runners.clear();
}


void addSwingComponent(String name)
{
   swing_components.add(name);
}



/********************************************************************************/
/*										*/
/*	Execution methods							*/
/*										*/
/********************************************************************************/

void startExecution()
{
   startRunner();
}


synchronized void stopExecution()
{
   stopRunner();
   while (master_thread != null) {
      try {
	 wait(1000);
       }
      catch (InterruptedException e) { }
    }
}



void restartExecution()
{
   stopCurrentRun();

   if (is_continuous) startRunner();
}


private synchronized void stopCurrentRun()
{
   if (master_thread == null) return;

   for (RunnerThread rt : runner_threads.values()) {
      rt.interrupt();
    }

   while (!runner_threads.isEmpty()) {
      try {
	 wait(1000);
       }
      catch (InterruptedException e) { }
    }
}


private synchronized void stopRunner()
{
   stopCurrentRun();
   if (master_thread != null) {
      is_continuous = false;
      run_again = false;
      master_thread.interrupt();
    }
}


private synchronized void startRunner()
{
   if (master_thread == null) {
      master_thread = new MasterThread();
      master_thread.start();
    }
   else {
      run_again = true;
      notifyAll();
    }
}



/********************************************************************************/
/*										*/
/*	Output methods								*/
/*										*/
/********************************************************************************/

private void report()
{
   boolean empty = false;
   for (CuminRunError sts : run_status.values()) {
      if (sts == null || sts.getReason() == CuminRunError.Reason.STOPPED) empty = true;
    }

   if (reply_id != null) {
      CommandArgs args = new CommandArgs();
      String cnts = null;
      if (reply_id != null) args.put("ID",reply_id);
      if (empty || run_status.isEmpty()) args.put("EMPTY",true);
      else {
	 IvyXmlWriter xw = new IvyXmlWriter();
	 xw.begin("CONTENTS");
         
	 for (Map.Entry<CuminRunner,CuminRunError> ent : run_status.entrySet()) {
	    CuminRunner cr = ent.getKey();
	    CuminRunError sts = ent.getValue();
	    outputResult(xw,cr,sts);
	  }
         
	 for_session.getIOModel().outputXml(xw);
         
         for (String s : graphics_outputs) {
            xw.xmlText(s);
          }
         
	 xw.end("CONTENTS");
	 cnts = xw.toString();
	 xw.close();
       }
      MintDefaultReply hdlr = new MintDefaultReply();
      for_monitor.sendCommand("EXEC",args,cnts,hdlr);
      String mrslt = hdlr.waitForString();
      AcornLog.logD("Message result: " + mrslt);
    }

   for (CuminRunner cr : run_status.keySet()) {
      cr.resetValues();
    }
   
   for_session.getIOModel().reset();
   
   run_status.clear();
}



private void outputResult(IvyXmlWriter xw,CuminRunner cr,CuminRunError sts)
{
   CashewOutputContext outctx = new CashewOutputContext(xw);

   xw.begin("RUNNER");
   SesameLocation loc = for_session.getLocation(cr);
   if (loc != null) {
      xw.field("METHOD",loc.getMethodName());
      xw.field("LINE",loc.getLineNumber());
      xw.field("THREAD",loc.getThread());
      xw.field("THREADNAME",loc.getThreadName());
    }

   xw.begin("RETURN");
   xw.field("REASON",sts.getReason());
   xw.field("MESSAGE",sts.getMessage());
   if (sts.getCause() != null) {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      sts.getCause().printStackTrace(pw);
      xw.textElement("STACK",sw.toString());
      pw.close();
    }
   if (sts.getValue() != null) {
      sts.getValue().outputXml(outctx);
    }
   xw.end("RETURN");
   CashewContext ctx = cr.getLookupContext();
   ctx.outputXml(outctx);
   xw.end("RUNNER");
}




/********************************************************************************/
/*										*/
/*	Running methods 							*/
/*										*/
/********************************************************************************/

private synchronized void setStatus(CuminRunner cr,CuminRunError sts)
{
      run_status.put(cr,sts);
}


private class MasterThread extends Thread {

   MasterThread() {
      super("SeedeExec_" + reply_id);
    }

   @Override public void run()
   {
      for (int i = 0; ; ++i ) {
         // first start all the threads
         run_status.clear();
         for_session.getIOModel().clear();
         List<RunnerThread> waits = new ArrayList<RunnerThread>();
         SesameProject proj = for_session.getProject();
         
         for_session.resetCache();
   
         if (i != 0 && reply_id != null) {
            CommandArgs args = new CommandArgs();
            if (reply_id != null) args.put("ID",reply_id);
            for_monitor.sendCommand("RESET",args,null,null);
          }
         
         graphics_outputs.clear();
         
         proj.compileProject();
         proj.executionLock();
         try {
            synchronized (this) {
               for (CuminRunner cr : cumin_runners) {
                  if (i != 0) {
                     MethodDeclaration mthd = for_session.getRunnerMethod(cr);
                     if (mthd == null) continue;
                     cr.reset(mthd);
                     cr.setMaxTime(max_time);
                   }
                  RunnerThread rt = new RunnerThread(SesameExecRunner.this,cr);
                  runner_threads.put(cr,rt);
                  waits.add(rt);
                  rt.start();
                }
             }
            
            // wait for all to exit
            while (!waits.isEmpty()) {
               for (Iterator<RunnerThread> it = waits.iterator(); it.hasNext(); ) {
                  RunnerThread rt = it.next();
                  try {
                     rt.join();
                     synchronized (this) {
                        runner_threads.remove(rt.getRunner());
                        if (runner_threads.isEmpty()) {
                           notifyAll();
                         }
                      }
                     it.remove();
                   }
                  catch (InterruptedException e) { }
                }
             }
            
            for (CuminRunner cr : cumin_runners) {
               addSwingGraphics(cr);
             }
            runSwingThreads();
          }
         finally {
            proj.executionUnlock();
          }
         
         report();
         synchronized (this) {
            if (!is_continuous) break;
            while (!run_again) {
               if (isInterrupted()) break;
               if (!is_continuous) break;
               try {
                  wait(5000);
                }
               catch (InterruptedException e) { }
             }
            run_again = false;
            if (!is_continuous) break;
          }
       }
   
      master_thread = null;
   }

   private void runSwingThreads() {
      if (swing_components == null || swing_components.isEmpty()) return;
      for (String cvname : swing_components) {
         for (CuminRunner cr : cumin_runners) {
            String cvn = cr.findReferencedVariableName(cvname);
            CashewValue cv = cr.findReferencedVariableValue(cvname);
            if (cvn != null) {
               CashewValue rv = null;
               cr.ensureLoaded("edu.brown.cs.seede.poppy.PoppyGraphics");
               String expr =  "edu.brown.cs.seede.poppy.PoppyGraphics.computeGraphics(";
               expr += cvn + "," + "\"" + cvname + "\"" + ")";
               CashewValue cv1 = cr.getLookupContext().evaluate(expr);
               rv = cr.executeCall("edu.brown.cs.seede.poppy.PoppyGraphics.computeDrawingG",cv,cv1);
               if (rv != null) {
                  String rpt = rv.getString(cr.getClock());
                  AcornLog.logI("SWING REPORT: " + rpt);
                  if (rpt != null) graphics_outputs.add(rpt);
                }
               break;
             }
          }
       }
    }
      
   
   private void addSwingGraphics(CuminRunner cr)
   {
      for (CashewValue cv : cr.getCallArgs()) {
         if (cv.getDataType(cr.getClock()).getName().equals("edu.brown.cs.seede.poppy.PoppyGraphics")) {
            CashewValue rv = cr.executeCall("edu.brown.cs.seede.poppy.PoppyGraphics.finalReport",cv);
            if (rv != null) {
               String rslt = rv.getString(cr.getClock());
               if (rslt != null) graphics_outputs.add(rslt);
             }
          }
       }
   }
         

}	// end of inner class MasterRunner




/********************************************************************************/
/*										*/
/*	Thread to run a single runner						*/
/*										*/
/********************************************************************************/

private static class RunnerThread extends Thread {

   private SesameExecRunner exec_runner;
   private CuminRunner cumin_runner;

   RunnerThread(SesameExecRunner er,CuminRunner cr) {
      super("Runner_" + er.reply_id + "_" + cr);
      exec_runner = er;
      cumin_runner = cr;
    }

   CuminRunner getRunner()			{ return cumin_runner; }

   @Override public void run() {
      CuminRunError sts = null;
      try {
         cumin_runner.interpret(CuminConstants.EvalType.RUN);
       }
      catch (CuminRunError r) {
         sts = r;
       }
      catch (Throwable t) {
         sts = new CuminRunError(t);
       }
   
      exec_runner.setStatus(cumin_runner,sts);
    }

}	// end of inner class RunnerThread




}	// end of class SesameExecRunner




/* end of SesameExecRunner.java */


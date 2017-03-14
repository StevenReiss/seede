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
import edu.brown.cs.seede.cumin.CuminGraphics;
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
private String		reply_id;
private Map<CuminRunner,RunnerThread> runner_threads;
private Map<CuminRunner,CuminRunError> run_status;
private MasterThread	master_thread;
private boolean 	is_continuous;
private boolean 	run_again;
private List<CashewValue> swing_components;
private SesameContext   base_context;





/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

SesameExecRunner(SesameSession ss,String rid,SesameContext ctx,
      boolean contin,CuminRunner... rs)
{
   for_session = ss;
   for_monitor = ss.getControl().getMonitor();
   base_context = ctx;
   reply_id = rid;
   cumin_runners = new ArrayList<CuminRunner>();
   runner_threads = new HashMap<CuminRunner,RunnerThread>();
   run_status = new HashMap<CuminRunner,CuminRunError>();
   is_continuous = contin;
   swing_components = new ArrayList<CashewValue>();

   for (CuminRunner r : rs) {
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

SesameContext getBaseContext()                  { return base_context; }
boolean isContinuous()                          { return is_continuous; }


void addRunner(CuminRunner cr)
{
   addRunner(cr,false);
}


void addRunner(CuminRunner cr,boolean start)
{
   stopCurrentRun();
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



/********************************************************************************/
/*                                                                              */
/*      Execution methods                                                       */
/*                                                                              */
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
	 xw.end("CONTENTS");
	 cnts = xw.toString();
	 xw.close();
       }
      MintDefaultReply hdlr = new MintDefaultReply();
      for_monitor.sendCommand("EXEC",args,cnts,hdlr);
      String mrslt = hdlr.waitForString();
      AcornLog.logD("Message result: " + mrslt);
    }

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
         synchronized (this) {
            for (CuminRunner cr : cumin_runners) {
               if (i != 0) {
        	  MethodDeclaration mthd = for_session.getRunnerMethod(cr);
        	  if (mthd == null) continue;
        	  cr.reset(mthd);
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
         runSwingThreads();
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
      for (CashewValue cv : swing_components) {
	 CuminGraphics cgr = new CuminGraphics(cv,null);
	 // invoke cv.paint(cgr)
	 String rpt = cgr.getReport();
	 System.err.println("SWING REPORT: " + rpt);
	 // add report to output
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


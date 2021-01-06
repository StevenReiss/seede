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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.core.dom.MethodDeclaration;

import edu.brown.cs.ivy.xml.IvyXmlWriter;
import edu.brown.cs.ivy.mint.MintConstants.CommandArgs;
import edu.brown.cs.seede.acorn.AcornLog;
import edu.brown.cs.seede.cashew.CashewContext;
import edu.brown.cs.seede.cashew.CashewException;
import edu.brown.cs.seede.cashew.CashewOutputContext;
import edu.brown.cs.seede.cashew.CashewValue;
import edu.brown.cs.seede.cashew.CashewValueObject;
import edu.brown.cs.seede.cumin.CuminConstants;
import edu.brown.cs.seede.cumin.CuminRunException;
import edu.brown.cs.seede.cumin.CuminRunStatus;
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
private List<String>	graphics_outputs;
private String		reply_id;
private Map<CuminRunner,RunnerThread> runner_threads;
private Map<CuminRunner,CuminRunStatus> run_status;
private MasterThread	master_thread;
private StopperThread	stopper_thread;
private boolean 	is_continuous;
private boolean 	run_again;
private List<String>	swing_components;
private SesameContext	base_context;
private long		max_time;
private int		max_depth;

private AtomicInteger	report_counter = new AtomicInteger(1);


enum RunState { INIT, RUNNING, SWING, STOPPED, WAITING, EXIT };
enum StopState { RUN, STOP_DESIRED, EXIT_DESIRED };




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

SesameExecRunner(SesameSession ss,String rid,SesameContext ctx,
      boolean contin,long maxtime,int maxdepth,CuminRunner... rs)
{
   for_session = ss;
   for_monitor = ss.getControl().getMonitor();
   base_context = ctx;
   reply_id = rid;
   cumin_runners = new ArrayList<CuminRunner>();
   runner_threads = new HashMap<CuminRunner,RunnerThread>();
   run_status = new ConcurrentHashMap<>();
   is_continuous = contin;
   swing_components = new ArrayList<>();
   max_time = maxtime;
   max_depth = maxdepth;
   graphics_outputs = new ArrayList<>();

   for (CuminRunner r : rs) {
      r.setMaxTime(max_time);
      r.setMaxDepth(max_depth);
      cumin_runners.add(r);
    }
   master_thread = null;
   run_again = false;
   stopper_thread = null;
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
   stopCurrentRunAndWait();
   cr.setMaxTime(max_time);
   cr.setMaxDepth(max_depth);
   cumin_runners.add(cr);
   if (is_continuous && start) startRunner();
}


void removeRunner(CuminRunner cr)
{
   stopCurrentRunAndWait();
   cumin_runners.remove(cr);
   if (is_continuous) startRunner();
}


void removeAllRunners()
{
   stopCurrentRunAndWait();
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


void stopExecution()
{
   MasterThread mth = master_thread;
   if (mth != null) mth.stopMaster();
}



void restartExecution()
{
   stopCurrentRun();
}


private void stopCurrentRun()
{
   synchronized (this) {
      if (master_thread == null) return;
      if (stopper_thread == null) {
	 stopper_thread = new StopperThread();
	 stopper_thread.start();
       }
    }

   stopper_thread.initiateStop();
}



private void stopCurrentRunAndWait()
{
   MasterThread mth = master_thread;

   if (mth == null) return;
   mth.stopCurrentRun();

   AcornLog.logD("MASTER: Current run ended");

   if (is_continuous) startRunner();
}





private void startRunner()
{
   AcornLog.logD("MASTER: Start runner");

   MasterThread mth = master_thread;
   if (mth == null) {
      synchronized (this) {
	 if (master_thread == null) {
	    master_thread = new MasterThread();
	    master_thread.start();
	  }
	 if (stopper_thread == null) {
	    stopper_thread = new StopperThread();
	    stopper_thread.start();
	  }
       }
    }
   else {
      mth.restart();
    }
}



/********************************************************************************/
/*										*/
/*	Output methods								*/
/*										*/
/********************************************************************************/

private void report(long time)
{
   boolean empty = false;
   boolean error = false;
   boolean complete = true;
   for (CuminRunStatus sts : run_status.values()) {
      if (sts == null || sts.getReason() == CuminConstants.Reason.STOPPED) empty = true;
      else {
	 switch (sts.getReason()) {
	    case ERROR :
	    case COMPILER_ERROR :
	       error = true;
	       break;
	    case RETURN :
	       break;
	    default :
	       complete = false;
	       break;
	  }
       }
    }

   AcornLog.logD("MASTER: Start report " + reply_id + " " + empty + " " + error);

   if (reply_id != null) {
      CommandArgs args = new CommandArgs();
      String cnts = null;
      if (reply_id != null) args.put("ID",reply_id);
      args.put("INDEX",report_counter.incrementAndGet());
      if (empty || run_status.isEmpty()) args.put("EMPTY",true);
      else {
	 IvyXmlWriter xw = new IvyXmlWriter();
	 xw.begin("CONTENTS");
	 xw.field("EXECTIME",time);
	 xw.field("PROJECT",for_session.getProject().getName());
	 xw.field("ERROR",error);
	 xw.field("COMPLETE",complete);
	 xw.field("TRACE",AcornLog.isTracing());
	 xw.field("LOGLEVEL",AcornLog.getLogLevel());

	 boolean firsttime = true;
	 for (Map.Entry<CuminRunner,CuminRunStatus> ent : run_status.entrySet()) {
	    CuminRunner cr = ent.getKey();
	    CuminRunStatus sts = ent.getValue();
	    if (firsttime) {
	       xw.field("TICKS",cr.getClock().getTimeValue());
	     }
	    outputResult(xw,cr,sts,firsttime);
	    firsttime = false;
	  }

	 for_session.getIOModel().outputXml(xw);

	 for (String s : graphics_outputs) {
	    xw.xmlText(s);
	  }

	 xw.end("CONTENTS");
	 cnts = xw.toString();
	 xw.close();
       }
      for_monitor.sendCommand("EXEC",args,cnts,null);
    }

   for (CuminRunner cr : run_status.keySet()) {
      cr.resetValues();
    }

   for_session.getIOModel().reset();

   run_status.clear();

   AcornLog.logD("MASTER: Done report");
}



private void outputResult(IvyXmlWriter xw,CuminRunner cr,CuminRunStatus sts,boolean stats)
{
   CashewOutputContext outctx = new CashewOutputContext(cr,xw,for_session.getExpandNames());
   CashewContext ctx = cr.getLookupContext();
   CashewValue rval = sts.getValue();

   if (for_session.getComputeToString()) {
      if (rval != null) rval.checkToString(outctx);
      ctx.checkToString(outctx);
      outctx.resetValues();
    }

   if (rval != null) rval.checkChanged(outctx);
   ctx.checkChanged(outctx);
   // ctx.checkToString(outctx);

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
   if (rval != null) {
      rval.outputXml(outctx,"*RETURN*");
    }
   xw.end("RETURN");

   ctx.outputXml(outctx);
   xw.end("RUNNER");

   if (stats) {
      CashewValueObject.outputStatics(xw,outctx);
    }
}



/********************************************************************************/
/*										*/
/*	Running methods 							*/
/*										*/
/********************************************************************************/

private void setStatus(CuminRunner cr,CuminRunStatus sts)
{
   run_status.put(cr,sts);
}



private class MasterThread extends Thread {

   private RunState	run_state;
   private StopState	stop_state;

   MasterThread() {
      super("SeedeExec_" + reply_id);
      run_state = RunState.INIT;
      stop_state = StopState.RUN;
    }

   @Override public void run() {
      for (int i = 0; ; ++i ) {
	 // first start all the threads
	 setupRun(i == 0);
	 run_state = RunState.INIT;

	 long starttime = System.currentTimeMillis();

	 try {
	    runOnce(i == 0);
	  }
	 catch (Throwable t) {
	    AcornLog.logE("MASTER: Problem running",t);
	  }

	 long time = System.currentTimeMillis() - starttime;
	 AcornLog.logI("MASTER: Execution time = " + time);
	 try {
	    report(time);
	  }
	 catch (Throwable t) {
	    AcornLog.logE("MASTER: Problem with reporting: " + t,t);
	  }

	 synchronized (this) {
	    if (!is_continuous) break;
	    while (!run_again) {
	       AcornLog.logD("MASTER: begin wait " + stop_state + " " + run_state + " " + is_continuous);
	       if (interrupted()) continue;
	       if (stop_state == StopState.EXIT_DESIRED) break;
	       stop_state = StopState.RUN;
	       run_state = RunState.WAITING;
	       notifyAll();
	       if (!is_continuous) break;
	       try {
		  wait(5000);
		}
	       catch (InterruptedException e) { }
	     }
	    run_again = false;
	    stop_state = StopState.RUN;
	    if (!is_continuous) break;
	  }
       }

      master_thread = null;
      run_state = RunState.EXIT;
      if (stopper_thread != null) stopper_thread.exit();
      AcornLog.logD("MASTER: exit");
    }

   synchronized void restart() {
      AcornLog.logD("MASTER: restart request");
      run_again = true;
      notifyAll();
    }

   synchronized void stopCurrentRun() {
      stop_state = StopState.STOP_DESIRED;

      AcornLog.logI("MASTER: Stop current run " + runner_threads.size());

      for (RunnerThread rt : runner_threads.values()) {
	 rt.interrupt();
       }

      interrupt();

      notifyAll();

      for ( ; ; ) {
	 if (run_state == RunState.INIT || run_state == RunState.RUNNING ||
	       run_state == RunState.SWING) {
	    try {
	       wait(1000);
	     }
	    catch (InterruptedException e) { }
	  }
	 else break;
       }
    }

   synchronized void stopMaster() {
      AcornLog.logI("MASTER: Stop master request");
      is_continuous = false;
      run_again = false;
      stop_state = StopState.EXIT_DESIRED;
      stopCurrentRun();
      while (run_state != RunState.EXIT && master_thread != null) {
	 try {
	    wait(5000);
	  }
	 catch (InterruptedException e) { }
       }
      AcornLog.logD("MASTER: threads stopped");
    }

   private void setupRun(boolean firsttime) {
      AcornLog.logD("MASTER: setup run");
      run_state = RunState.INIT;
      run_status.clear();
      for_session.getIOModel().clear();
      CuminRunner.resetGraphics();

      SesameProject proj = for_session.getProject();

      if (!firsttime && reply_id != null) {
	 CommandArgs args = new CommandArgs();
	 if (reply_id != null) args.put("ID",reply_id);
	 for_monitor.sendCommand("RESET",args,null,null);
       }

      graphics_outputs.clear();

      proj.compileProject();

      for_session.resetCache();
    }

   private void runOnce(boolean firsttime) {
      List<RunnerThread> waits = new ArrayList<RunnerThread>();
      SesameProject proj = for_session.getProject();
   
      proj.executionLock();
      run_state = RunState.RUNNING;
      AcornLog.logD("MASTER: Start running");
      try {
         synchronized (this) {
            if (!firsttime) {
               // this might not be needed any more
               // cumin_runners.clear();
               // SesameContext gblctx = getBaseContext();
               // for (SesameLocation loc : for_session.getActiveLocations()) {
               // CuminRunner cr = for_session.createRunner(loc,gblctx);
               // cr.setMaxTime(max_time);
               // cr.setMaxDepth(max_depth);
               // cumin_runners.add(cr);
               // }
             }
            for (CuminRunner cr : cumin_runners) {
               if (!firsttime) {
        	  MethodDeclaration mthd = for_session.getRunnerMethod(cr);
        	  if (mthd == null) continue;
        	  cr.reset(mthd);
        	  cr.setMaxTime(max_time);
        	  cr.setMaxDepth(max_depth);
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
   
         if (stop_state == StopState.RUN) {
            for (CuminRunner cr : cumin_runners) {
               addSwingGraphics(cr);
             }
            runSwingThreads();
          }
       }
      finally {
         proj.executionUnlock();
         run_state = RunState.STOPPED;
         AcornLog.logD("MASTER: Run finished");
       }
   }

   private void runSwingThreads() {
      if (swing_components == null || swing_components.isEmpty()) return;
      AcornLog.logD("MASTER: Start swing thread run");
      run_state = RunState.SWING;
      for (String cvname : swing_components) {
	 if (stop_state != StopState.RUN) return;
	 for (CuminRunner cr : cumin_runners) {
	    String cvn = cr.findReferencedVariableName(cvname);
	    try {
	       CashewValue cv = cr.findReferencedVariableValue(cr.getTyper(),cvname,cr.getClock().getTimeValue());
	       if (cvn != null) {
		  CashewValue rv = null;
		  cr.ensureLoaded("edu.brown.cs.seede.poppy.PoppyGraphics");
		  String expr =  "edu.brown.cs.seede.poppy.PoppyGraphics.computeGraphics(";
		  expr += cvn + "," + "\"" + cvname + "\"" + ")";
		  CashewValue cv1 = cr.getLookupContext().evaluate(expr);
		  rv = cr.executeCall("edu.brown.cs.seede.poppy.PoppyGraphics.computeDrawingG",cv,cv1);
		  if (rv != null) {
		     String rpt = rv.getString(cr.getTyper(),cr.getClock());
		     AcornLog.logI("SWING REPORT: " + rpt);
		     if (rpt != null) graphics_outputs.add(rpt);
		   }
		  break;
		}
	     }
	    catch (CashewException e) {
	       AcornLog.logE("Unexpected error starting swing thread",e);
	     }
	  }
       }
   }


   private void addSwingGraphics(CuminRunner cr) {
      for (CashewValue cv : cr.getCallArgs()) {
	 if (cv == null) return;
	 if (cv.getDataType(cr.getClock()).getName().equals("edu.brown.cs.seede.poppy.PoppyGraphics")) {
	    CashewValue rv = cr.executeCall("edu.brown.cs.seede.poppy.PoppyGraphics.finalReport",cv);
	    if (rv != null) {
	       try  {
		  String rslt = rv.getString(cr.getTyper(),cr.getClock());
		  if (rslt != null) graphics_outputs.add(rslt);
		}
	       catch (CashewException e) {
		  AcornLog.logE("Unexpected error getting graphics result",e);
		}
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
      CuminRunStatus sts = null;
      try {
	 sts = cumin_runner.interpret(CuminConstants.EvalType.RUN);
       }
      catch (CuminRunException r) {
	 sts = r;
       }
      catch (Throwable t) {
	 cumin_runner.getLookupContext().setEndTime(cumin_runner.getClock());
	 AcornLog.logE("Problem running thread",t);
	 sts = new CuminRunException(t);
       }

      exec_runner.setStatus(cumin_runner,sts);
    }

}	// end of inner class RunnerThread



/********************************************************************************/
/*										*/
/*	Thread to handle stopping runners					*/
/*										*/
/********************************************************************************/

private class StopperThread extends Thread {

   private int do_stop;

   StopperThread() {
      super("SeedExecStopper_" + reply_id);
      do_stop = 0;
    }

   synchronized void initiateStop() {
      AcornLog.logD("MASTER: stopper stop request " + do_stop);

      if (do_stop < 0) return;
      do_stop = 1;
      notifyAll();
    }

   synchronized void exit() {
      AcornLog.logD("MASTER: Stopper exit request");
      do_stop = -1;
      interrupt();
      notifyAll();
      stopper_thread = null;
    }

   @Override public void run() {
      while (do_stop >= 0) {
	 synchronized (this) {
	    while (do_stop == 0) {
	       try {
		  wait();
		}
	       catch (InterruptedException e) { }
	     }
	    if (do_stop > 0) do_stop = 0;
	  }
	 stopCurrentRunAndWait();
       }
      stopper_thread = null;

      AcornLog.logD("MASTER: Stopper thread exited");
    }

}	// end of inner class StopperThread


}	// end of class SesameExecRunner




/* end of SesameExecRunner.java */


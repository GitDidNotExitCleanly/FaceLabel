package com.facelabel;

import java.io.PrintWriter;
import java.io.StringWriter;

import android.os.Process;

public class MyExceptionHandler implements
        java.lang.Thread.UncaughtExceptionHandler {

    public void uncaughtException(Thread thread, Throwable exception) {

        StringWriter stackTrace = new StringWriter();
        exception.printStackTrace(new PrintWriter(stackTrace));
        System.err.println(stackTrace);
        Process.killProcess(Process.myPid());
        System.exit(0);
    }
}
package weaver.micro.devkit.test;

import weaver.micro.devkit.core.SystemAPI;

import java.io.PrintStream;

public class StackLogDemo {

    public static void main(String[] args) {
        PrintStream out = System.err;
        out.println(SystemAPI.getCompleteStackTraceInfo("No Exception Here~"));
    }

}

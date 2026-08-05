package com.dateconvertor_java;

import javafx.application.Application;

/**
 * Non-Application main so the Spring Boot fat jar can be launched on the
 * classpath (a class extending {@link javafx.application.Application} as the
 * main class triggers JavaFX's "runtime components are missing" guard).
 */
public class DesktopMain {

    static {
        // WebView's Prism GPU pipeline fails on machines without proper GPU
        // drivers (RTImage NPE). Software rendering is stable everywhere.
        System.setProperty("prism.order", "sw");
    }

    public static void main(String[] args) {
        Application.launch(DesktopApp.class, args);
    }
}

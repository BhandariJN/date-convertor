package com.dateconvertor_java;

import javafx.application.Application;

/**
 * Non-Application main so the Spring Boot fat jar can be launched on the
 * classpath (a class extending {@link javafx.application.Application} as the
 * main class triggers JavaFX's "runtime components are missing" guard).
 */
public class DesktopMain {

    public static void main(String[] args) {
        Application.launch(DesktopApp.class, args);
    }
}

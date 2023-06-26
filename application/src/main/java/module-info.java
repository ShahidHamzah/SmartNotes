module application {
    requires kotlin.stdlib;
    requires javafx.controls;
    requires kotlinx.coroutines.core.jvm;
    requires shared;
    requires javafx.web;
    requires java.prefs;
    requires kotlinx.serialization.json;
    requires java.net.http;
    exports net.codebot.application;

}
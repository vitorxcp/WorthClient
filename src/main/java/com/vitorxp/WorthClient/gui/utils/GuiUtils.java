package com.vitorxp.WorthClient.gui.utils;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.function.Consumer;

public class GuiUtils {

    public static void openLink(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) {
            System.err.println("Não foi possível abrir o link: " + url);
            e.printStackTrace();
        }
    }
}
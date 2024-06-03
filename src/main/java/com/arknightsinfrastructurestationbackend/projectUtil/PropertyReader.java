package com.arknightsinfrastructurestationbackend.projectUtil;

import java.util.ResourceBundle;

public class PropertyReader {
    public static String getAuthorFromProperties() {
        ResourceBundle bundle = ResourceBundle.getBundle("static.sendEmail");
        return bundle.getString("author");
    }
}

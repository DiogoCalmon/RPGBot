package org.dev;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.util.EnumSet;

public class Main {
    public static void main(String[] args) {
        JDA kit = JDABuilder.createDefault("", EnumSet.allOf(GatewayIntent.class)).build();

        kit.addEventListener(new Keys());
    }
}
package com.vitorxp.WorthClient.config;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

public class VoidLagFixConfig {

    public static boolean enabled;
    public static int activationHeight;
    public static int[] dimensionWhitelist;


    public static void syncConfig(File configFile) {
        Configuration config = new Configuration(configFile);

        try {
            config.load();

            String category = "General";

            enabled = config.getBoolean(
                    "enabled",
                    category,
                    true,
                    "Ative ou desative completamente a correção de lag do void. (Default: true)"
            );

            activationHeight = config.getInt(
                    "activationHeight",
                    category,
                    40,
                    0,
                    255,
                    "A correção só será ativada para blocos quebrados ACIMA desta altura Y. (Default: 40)"
            );

            dimensionWhitelist = config.get(
                    category,
                    "dimensionWhitelist",
                    new int[]{0, 1},
                    "Uma lista de IDs de dimensão onde a correção deve funcionar. (Ex: Skyblock no Overworld, use [0])"
            ).getIntList();

        } catch (Exception e) {
            System.err.println("Ocorreu um erro ao carregar a configuração do VoidLagFix!");
            e.printStackTrace();
        } finally {
            if (config.hasChanged()) {
                config.save();
            }
        }
    }
}

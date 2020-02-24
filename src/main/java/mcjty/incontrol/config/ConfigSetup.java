package mcjty.incontrol.config;

public class ConfigSetup {

    // @todo 1.15
//    public static File modConfigDir;
//    private static Configuration mainConfig;
//
//    public static void init(FMLPreInitializationEvent e) {
//        modConfigDir = e.getModConfigurationDirectory();
//        mainConfig = new Configuration(new File(modConfigDir.getPath() + File.separator + "incontrol", "main.cfg"));
//        readMainConfig();
//    }
//
//    private static void readMainConfig() {
//        Configuration cfg = mainConfig;
//        try {
//            cfg.load();
//            cfg.addCustomCategoryComment(GeneralConfiguration.CATEGORY_GENERAL, "General settings");
//
//            GeneralConfiguration.init(cfg);
//        } catch (Exception e1) {
//            FMLLog.log(Level.ERROR, e1, "Problem loading config file!");
//        } finally {
//            if (mainConfig.hasChanged()) {
//                mainConfig.save();
//            }
//        }
//    }
//
//    public static void postInit() {
//        if (mainConfig.hasChanged()) {
//            mainConfig.save();
//        }
//    }

}

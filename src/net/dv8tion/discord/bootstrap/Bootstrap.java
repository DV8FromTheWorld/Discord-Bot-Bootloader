package net.dv8tion.discord.bootstrap;

import java.io.File;
import java.io.IOException;

public class Bootstrap
{
    public static final String BOT_DOWNLOAD_URL = "https://drone.io/github.com/DV8FromTheWorld/Discord-Bot/files/target/Yui-LATEST.jar";
    public static final String VERSION = "1.0.0";
    public static final String OLD_VERSION_STORAGE = "./old/";
    public static final File BOT_JAR_FILE = new File("./Yui.jar");

    public static final int UPDATE_EXITCODE = 20;
    public static final int NEWLY_CREATED_CONFIG = 21;
    public static final int UNABLE_TO_CONNECT_TO_DISCORD = 22;
    public static final int BAD_USERNAME_PASS_COMBO = 23;
    public static final int NO_USERNAME_PASS_COMBO = 24;

    public static final String[]  START_BOT_COMMAND = new String[] {
        "java", "-Dfile.encoding=UTF-8", "-jar", BOT_JAR_FILE.getPath()
    };

    public static void main(String[] args) throws IOException, InterruptedException
    {        
        if (!BOT_JAR_FILE.exists())
            downloadBot();

        System.out.println("Starting the Bootstrap Launch loop");
        boolean exit = false;
        while (!exit)
        {
            ProcessBuilder builder = new ProcessBuilder();
            builder.environment().put("BootstrapVersion", VERSION);
            builder.command(START_BOT_COMMAND);
            builder.inheritIO();

            Process botProcess = builder.start();
            botProcess.waitFor();
            switch(botProcess.exitValue())
            {
                case UPDATE_EXITCODE:
                    updateBot();
                    break;
                case NEWLY_CREATED_CONFIG:
                    //TODO: More to work on.
                    System.out.println("The config was created for the first time. Please input Email and Password values.");
                    exit = true;
                    break;
                case UNABLE_TO_CONNECT_TO_DISCORD:
                    break;
                case BAD_USERNAME_PASS_COMBO:
                    break;
                case NO_USERNAME_PASS_COMBO:
                    break;
                default:
            }
        }
    }

    private static boolean updateBot()
    {
        throw new UnsupportedOperationException("Feature not yet implemented.");
    }

    private static boolean downloadBot() throws InterruptedException
    {
        for (int i = 0; i < 3 && !BOT_JAR_FILE.exists(); i++)
        {
            try
            {
                if (i == 0)
                {
                    System.out.println("Attempting to download the Bot, Please wait...");
                    Downloader.file(BOT_DOWNLOAD_URL, BOT_JAR_FILE.getPath());
                }
                else
                {
                    System.out.println("Failed to download the Bot, Will wait 5 second and try again.");
                    Thread.sleep(5000);
                    System.out.printf("Attempting to download bot, Attempt #%d, Please wait...", (i + 1));
                    Downloader.file(BOT_DOWNLOAD_URL, BOT_JAR_FILE.getPath());
                }
                if (BOT_JAR_FILE.exists())
                {
                    System.out.println("Successfully downloaded the Bot!");
                    return true;
                } 
            }
            catch (IOException e)
            {
                System.out.println("Encountered an IOError attempting to download the Bot.");
                e.printStackTrace();
            }
        }
        return false;
    }
}

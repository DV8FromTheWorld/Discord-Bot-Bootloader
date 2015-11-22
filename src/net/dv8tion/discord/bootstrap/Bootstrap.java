package net.dv8tion.discord.bootstrap;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class Bootstrap
{
    public static final String BOT_DOWNLOAD_URL = "https://drone.io/github.com/DV8FromTheWorld/Discord-Bot/files/target/Yui-LATEST.jar";
    public static final String VERSION = "1.0.0";
    public static final File BOT_JAR_FILE = new File("./Yui.jar");
    public static final File BOT_JAR_FILE_OLD = new File("OLD_" + BOT_JAR_FILE.getName());

    public static final int NORMAL_SHUTDOWN = 10;
    public static final int UPDATE_EXITCODE = 20;
    public static final int NEWLY_CREATED_CONFIG = 21;
    public static final int UNABLE_TO_CONNECT_TO_DISCORD = 22;
    public static final int BAD_USERNAME_PASS_COMBO = 23;
    public static final int NO_USERNAME_PASS_COMBO = 24;

    public static final int UNKNOWN_EXITCODE = 50;
    public static final int DOWNLOAD_FAILED = 51;
    public static final int UPDATED_FAILED = 52;

    public static final String[] START_BOT_COMMAND = new String[] {
        "java", "-Dfile.encoding=UTF-8", "-jar", BOT_JAR_FILE.getPath()
    };

    enum UpdateStatus
    {
        SUCCESSFUL("successful"), FAILED("failed"), NONE("");

        private String commandArg;
        UpdateStatus(String commandArg)
        {
            this.commandArg = commandArg;
        }

        @Override
        public String toString()
        {
            return commandArg;
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException
    {        
        if (!BOT_JAR_FILE.exists())
        {
            if (!downloadBot())
            {
                System.out.println("Could not download Bot. Check Internet Connection and File System.");
                System.exit(DOWNLOAD_FAILED);
            }
        }

        System.out.println("Starting the Bootstrap Launch loop");
        UpdateStatus updateStatus = UpdateStatus.NONE;
        while (true)
        {
            ProcessBuilder builder = new ProcessBuilder();
            builder.environment().put("BootstrapVersion", VERSION);
            builder.inheritIO();
            builder.command(START_BOT_COMMAND);
            if (!updateStatus.equals(UpdateStatus.NONE))
            {
                builder.command().add(updateStatus.toString());
                updateStatus = UpdateStatus.NONE;
            }

            Process botProcess = builder.start();
            botProcess.waitFor();
            switch(botProcess.exitValue())
            {
                case NORMAL_SHUTDOWN:
                    System.out.println("The Bot requested to shutdown and not relaunch.\nShutting down...");
                    System.exit(0);
                    break;
                case UPDATE_EXITCODE:
                    updateStatus = updateBot() ? UpdateStatus.SUCCESSFUL : UpdateStatus.FAILED;
                    break;
                case NEWLY_CREATED_CONFIG:
                    //TODO: More to work on.
                    System.out.println("The config was created for the first time. Please input Email and Password values.");
                    System.exit(NEWLY_CREATED_CONFIG);
                    break;
                case UNABLE_TO_CONNECT_TO_DISCORD:
                    System.exit(UNABLE_TO_CONNECT_TO_DISCORD);
                    break;
                case BAD_USERNAME_PASS_COMBO:
                    System.exit(BAD_USERNAME_PASS_COMBO);
                    break;
                case NO_USERNAME_PASS_COMBO:
                    System.exit(NO_USERNAME_PASS_COMBO);
                    break;
                default:
                    System.out.println("The Bot's Exit code was unrecognized. ExitCode: " + botProcess.exitValue());
                    System.out.println("Shutting down now.");
                    System.exit(UNKNOWN_EXITCODE);
            }
        }
    }

    private static boolean updateBot()
    {
        try
        {
            Files.move(
                    BOT_JAR_FILE.toPath(),
                    BOT_JAR_FILE_OLD.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
            if (!downloadBot())
            {
                Files.move(
                        BOT_JAR_FILE_OLD.toPath(),
                        BOT_JAR_FILE.toPath(),
                        StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Encountered an error while downloading the updated Bot. Reverting to old version.");
                return false;
            }
            System.out.println("Update Successful!");
            return true;
        }
        catch (IOException e)
        {
            System.out.println("Encountered problem when trying to move the Bot jar file.");
            e.printStackTrace();
            return false;
        }
    }

    private static boolean downloadBot()
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
                    System.out.printf("Attempting to download bot, Attempt #%d, Please wait...\n", (i + 1));
                    Downloader.file(BOT_DOWNLOAD_URL, BOT_JAR_FILE.getPath());
                }
                if (BOT_JAR_FILE.exists())
                {
                    System.out.println("Successfully downloaded the Bot!");
                    return true;
                } 
            }
            catch (IOException | InterruptedException e)
            {
                System.out.println("Encountered an IOError attempting to download the Bot.");
                e.printStackTrace();
            }
        }
        return false;
    }
}

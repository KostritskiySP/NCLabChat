package ServiceThreads;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Sergio on 29.01.2018.
 */
public class ServerManagementThread extends Thread {
    ServerManagementController controller;

    public void addServerManagementController(ServerManagementController smc) {
        this.controller = smc;
    }

    public void run() {
        BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            try {
                String command = keyboard.readLine();
                Pattern exitPattern = Pattern.compile("\\s*[Ee][Xx][Ii][Tt]\\s*");
                if (exitPattern.matcher(command).matches()){
                    controller.exit();
                    System.exit(0);
                }
                Pattern kickPattern = Pattern.compile("\\s*[Kk][Ii][Cc][Kk].*");
                if (kickPattern.matcher(command).matches()){
                    command = command.replaceAll("\\s*[Kk][Ii][Cc][Kk]\\s*","");
                    controller.kick(command);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}

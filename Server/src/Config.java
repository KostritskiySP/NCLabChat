import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class Config {
    private static final String PROPERTIES_FILE = "properties.properties";
    public static int PORT;
    public static String REGISTERED_USERS_FILE;
    public static String SERVERS_NET_FILE;
    public static String CHAT_HISTORY_FILE;

    static {
        Properties properties = new Properties();
        FileInputStream propertiesFile = null;

        try {
            propertiesFile = new FileInputStream(PROPERTIES_FILE);
            properties.load(propertiesFile);

            PORT             = Integer.parseInt(properties.getProperty("PORT"));
            REGISTERED_USERS_FILE = properties.getProperty("REGISTERED_USERS_FILE");
            SERVERS_NET_FILE =properties.getProperty("SERVERS_NET_FILE");
            CHAT_HISTORY_FILE    = properties.getProperty("CHAT_HISTORY_FILE");

        } catch (FileNotFoundException ex) {
            System.err.println("Properties config file not found");
        } catch (IOException ex) {
            System.err.println("Error while reading file");
        } finally {
            try {
                propertiesFile.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
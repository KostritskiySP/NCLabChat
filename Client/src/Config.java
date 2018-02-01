import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * Класс, получающий данные из конфигурационного файла
 */
public class Config {

    private static final String PROPERTIES_FILE = "properties.properties";
    public static int[] PORTARRAY;
    public static String HOST;

    static {
        Properties properties = new Properties();
        FileInputStream propertiesFile = null;

        try {
            HOST  = properties.getProperty("HOST");
            propertiesFile = new FileInputStream(PROPERTIES_FILE);
            properties.load(propertiesFile);
            String[] parts = properties.getProperty("PORT_ARRAY").split(";");
            PORTARRAY = new int[parts.length];
            for (int i = 0; i < parts.length; ++i)
            {
                PORTARRAY[i] = Integer.valueOf(parts[i]);
            }
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
package util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class DBPropertyUtil {

    public static String getPropertyString(String propertyFileName) throws IOException {
        Properties props = new Properties();
        FileInputStream fis = new FileInputStream(propertyFileName);
        props.load(fis);
        fis.close();

        String url = props.getProperty("db.url");
        String username = props.getProperty("db.username");
        String password = props.getProperty("db.password");

        return url + "?user=" + username + "&password=" + password;
    }

	public static Properties loadProperties() {
		// TODO Auto-generated method stub
		return null;
	}
}
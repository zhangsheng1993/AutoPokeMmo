package Utils;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

public class ReceourceManage {
	private final static String filePath = "application.properties";
	private  Properties prop;
	private static ReceourceManage instance;

	private ReceourceManage() {
		createProperties();
	};

	public static ReceourceManage getInstance() {

		if (null == instance) {
			instance=new ReceourceManage ();
		}

		return instance;
	}

	private   void createProperties() {
		if (null != prop) {
			return;
		}
		prop = new Properties();
		InputStream in = null;
		try {
			in = new BufferedInputStream(new FileInputStream(filePath));
			prop.load(new InputStreamReader(in, "utf-8"));
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					System.out.println(e.getMessage());
				}
			}
		}
	}

	public String getProperty(String key) {
		return prop.getProperty(key, key);
	}
}

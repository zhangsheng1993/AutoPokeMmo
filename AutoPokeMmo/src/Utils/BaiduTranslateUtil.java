package Utils;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.json.JSONObject;

public class BaiduTranslateUtil {
	private static final String TRANS_API_HOST = "http://api.fanyi.baidu.com/api/trans/vip/translate";

	private String appid;
	private String securityKey;
	private String targetLanguage;
	private static BaiduTranslateUtil instance;

	private BaiduTranslateUtil() {
		appid = ReceourceManage.getInstance().getProperty(
				"baiduTranslate.APP_ID");
		securityKey = ReceourceManage.getInstance().getProperty(
				"baiduTranslate.SECURITY_KEY");
		targetLanguage = ReceourceManage.getInstance().getProperty(
				"baiduTranslate.targetLanguage");
	};

	public static BaiduTranslateUtil getInstance() {

		if (null == instance) {
			instance = new BaiduTranslateUtil();
		}

		return instance;
	}

	private String getTransResult(String query) {
		Map<String, String> params = buildParams(query);
		return HttpGet.get(TRANS_API_HOST, params);
	}

	/**
	 * 百度翻译的对外接口，返回的是JSONObject
	 * 
	 * @param query
	 * @return
	 */
	public JSONObject getQueryResult(String query) {
		String transResult = getTransResult(query);
		JSONObject json = new JSONObject(transResult);
		String str1 = json.get("trans_result").toString();
		// 去掉[]
		str1 = str1.replaceAll("\\[|\\]", "");
		JSONObject json1 = new JSONObject(str1);
		return json1;
	}

	private Map<String, String> buildParams(String query) {
	Map<String, String> params = new HashMap<String, String>();
		params.put("q", query);
		params.put("from", "auto");
		params.put("to", targetLanguage);
		params.put("appid", appid);

		// 随机数
		String salt = String.valueOf(System.currentTimeMillis());
		params.put("salt", salt);

		// 签名
		String src = appid + query + salt + securityKey; // 加密前的原文
		params.put("sign", MD5.md5(src));

		return params;
	}

	

	static class HttpGet {
		protected static final int SOCKET_TIMEOUT = 10000; // 10S
		protected static final String GET = "GET";

		public static String get(String host, Map<String, String> params) {
			try {
				// 设置SSLContext
				SSLContext sslcontext = SSLContext.getInstance("TLS");
				sslcontext.init(null,
						new TrustManager[] { myX509TrustManager }, null);

				String sendUrl = getUrlWithQueryString(host, params);

				// System.out.println("URL:" + sendUrl);

				URL uri = new URL(sendUrl); // 创建URL对象
				HttpURLConnection conn = (HttpURLConnection) uri
						.openConnection();
				if (conn instanceof HttpsURLConnection) {
					((HttpsURLConnection) conn).setSSLSocketFactory(sslcontext
							.getSocketFactory());
				}

				conn.setConnectTimeout(SOCKET_TIMEOUT); // 设置相应超时
				conn.setRequestMethod(GET);
				int statusCode = conn.getResponseCode();
				if (statusCode != HttpURLConnection.HTTP_OK) {
					System.out.println("Http错误码：" + statusCode);
				}

				// 读取服务器的数据
				InputStream is = conn.getInputStream();
				BufferedReader br = new BufferedReader(
						new InputStreamReader(is));
				StringBuilder builder = new StringBuilder();
				String line = null;
				while ((line = br.readLine()) != null) {
					builder.append(line);
				}

				String text = builder.toString();

				close(br); // 关闭数据流
				close(is); // 关闭数据流
				conn.disconnect(); // 断开连接

				return text;
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (KeyManagementException e) {
				e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}

			return null;
		}

		public static String getUrlWithQueryString(String url,
				Map<String, String> params) {
			if (params == null) {
				return url;
			}

			StringBuilder builder = new StringBuilder(url);
			if (url.contains("?")) {
				builder.append("&");
			} else {
				builder.append("?");
			}

			int i = 0;
			for (String key : params.keySet()) {
				String value = params.get(key);
				if (value == null) { // 过滤空的key
					continue;
				}

				if (i != 0) {
					builder.append('&');
				}

				builder.append(key);
				builder.append('=');
				builder.append(encode(value));

				i++;
			}

			return builder.toString();
		}

		protected static void close(Closeable closeable) {
			if (closeable != null) {
				try {
					closeable.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		/**
		 * 对输入的字符串进行URL编码, 即转换为%20这种形式
		 * 
		 * @param input
		 *            原文
		 * @return URL编码. 如果编码失败, 则返回原文
		 */
		public static String encode(String input) {
			if (input == null) {
				return "";
			}

			try {
				return URLEncoder.encode(input, "utf-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

			return input;
		}

		private static TrustManager myX509TrustManager = new X509TrustManager() {

			@Override
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			@Override
			public void checkServerTrusted(X509Certificate[] chain,
					String authType) throws CertificateException {
			}

			@Override
			public void checkClientTrusted(X509Certificate[] chain,
					String authType) throws CertificateException {
			}
		};
	}
}
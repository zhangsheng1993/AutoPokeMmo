package Utils;
import com.baidu.aip.ocr.AipOcr;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * 参考https://blog.csdn.net/fzq_javaee/article/details/80490398的例子写的
 */
public class WordDiscernUtil {
	private static AipOcr client;
	private static	WordDiscernUtil instance;
	private WordDiscernUtil() {
	};

	public static WordDiscernUtil getInstance() {

		if (null == instance) {
			createAipOcr();
		}

		return instance;
	}

	private synchronized static void createAipOcr() {
		// 设置APPID/AK/SK 需自行去百度申请哦
		String APP_ID = "11698324";
		String API_KEY = "YdwAeKtqayHo9Q58fHiuzU2M";
		String SECRET_KEY = "RQbi6aTv7oMrcoiU4ETomjIoSNSanad0";
		// 初始化一个AipOcr
		client = new AipOcr(APP_ID, API_KEY, SECRET_KEY);
		// 可选：设置网络连接参数
		client.setConnectionTimeoutInMillis(2000);
		client.setSocketTimeoutInMillis(60000);
		
		instance=new WordDiscernUtil();
	}

	// 通用文字识别返回一个包含全部文字的List
	public JSONArray getImgWordsAndLocationsFromPath(String path)
			throws JSONException {
		HashMap<String, String> options = new HashMap<String, String>();
		options.put("vertexes_location", "true");
		JSONObject jsonObject = client.general(path, options);
		JSONArray resultList = jsonObject.getJSONArray("words_result");
		return resultList;
	}

	// 返回一个包含全部文字的List高精度方法
	public JSONArray basicAccurateGeneral(byte[] image) throws JSONException {
		HashMap<String, String> options = new HashMap<String, String>();
		options.put("vertexes_location", "true");
		JSONObject jsonObject = client.basicAccurateGeneral(image, options);
		JSONArray resultList = jsonObject.getJSONArray("words_result");
		return resultList;
	}

}
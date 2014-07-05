package my.app.getroute.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

public class JsonParser {
	private static final String STATUS_KEY = "status";

	public static void parse(JSONObject jObject,
			List<List<HashMap<String, String>>> routes,
			List<String> instructionList) throws JSONException {
		if (routes != null && instructionList != null) {
			// = new ArrayList<List<HashMap<String, String>>>();
			JSONArray jRoutes = null;
			JSONArray jLegs = null;
			JSONArray jSteps = null;
			if (jObject.has(STATUS_KEY)
					&& jObject.getString(STATUS_KEY).equalsIgnoreCase("ok")) {

				jRoutes = jObject.getJSONArray("routes");
				/** Traversing all routes */
				for (int i = 0; i < jRoutes.length(); i++) {
					jLegs = ((JSONObject) jRoutes.get(i)).getJSONArray("legs");
					List<HashMap<String, String>> path = new ArrayList<HashMap<String, String>>();

					/** Traversing all legs */
					for (int j = 0; j < jLegs.length(); j++) {
						jSteps = ((JSONObject) jLegs.get(j))
								.getJSONArray("steps");

						/** Traversing all steps */
						for (int k = 0; k < jSteps.length(); k++) {
							String polyline = "";
							polyline = (String) ((JSONObject) ((JSONObject) jSteps
									.get(k)).get("polyline")).get("points");
							String inst = (String) ((JSONObject) jSteps.get(k))
									.getString("html_instructions");
							instructionList.add(inst);
							List<LatLng> list = decodePoly(polyline);

							/** Traversing all points */
							for (int l = 0; l < list.size(); l++) {
								HashMap<String, String> hm = new HashMap<String, String>();
								hm.put("lat", Double.toString(((LatLng) list
										.get(l)).latitude));
								hm.put("lng", Double.toString(((LatLng) list
										.get(l)).longitude));
								path.add(hm);
							}
						}
						routes.add(path);
					}
				}

			}

		}
	}

	/**
	 * Method Courtesy : jeffreysambells.com/2010/05/27
	 * /decoding-polylines-from-google-maps-direction-api-with-java
	 * */
	private static List<LatLng> decodePoly(String encoded) {

		List<LatLng> poly = new ArrayList<LatLng>();
		int index = 0, len = encoded.length();
		int lat = 0, lng = 0;

		while (index < len) {
			int b, shift = 0, result = 0;
			do {
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lat += dlat;

			shift = 0;
			result = 0;
			do {
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lng += dlng;

			LatLng p = new LatLng((((double) lat / 1E5)),
					(((double) lng / 1E5)));
			poly.add(p);
		}
		return poly;
	}

	public static JSONObject getGeoCode(String string, boolean docaching)
			throws IOException, JSONException {
		Log.i("Tag doGet", "making HTTP GET req");
		Log.i("Url :", string);

		HttpGet httpGet = new HttpGet(
				"http://maps.google.com/maps/api/geocode/json?address="
						+ URLEncoder.encode(string, "UTF-8") + "&sensor=false");
		HttpClient client = new DefaultHttpClient();
		HttpResponse response;
		StringBuilder stringBuilder = new StringBuilder();

		try {
			response = client.execute(httpGet);
			HttpEntity entity = response.getEntity();
			InputStream stream = entity.getContent();
			int b;
			while ((b = stream.read()) != -1) {
				stringBuilder.append((char) b);
			}
		} catch (ClientProtocolException e) {
		} catch (IOException e) {
		}

		JSONObject jsonObject = new JSONObject();
		jsonObject = new JSONObject(stringBuilder.toString());

		// Log.i("Tag jsonArray: ", sb.toString());
		return jsonObject;
	}

	private static final String GEOMETRY_KEY = "geometry";
	private static final String RESULT_KEY = "results";
	private static final String LOCATION_KEY = "location";
	private static final String LATITUDE_KEY = "lat";
	private static final String LONGITUDE_KEY = "lng";
	private static final String ADDRESS_KEY = "formatted_address";

	public static Location getLocationFromJson(JSONObject jObj)
			throws JSONException {
		JSONObject jObj1 = null, jObj2 = null, jObj3 = null;
		Location mLocation = null;

		// if (jArray.length() > 0) {
		// jObj1 = jObj;
		if (jObj.has(STATUS_KEY)
				&& jObj.getString(STATUS_KEY).equalsIgnoreCase("ok")) {
			JSONArray jArray = jObj.getJSONArray(RESULT_KEY);
			if (jArray.length() > 0) {
				jObj1 = jArray.getJSONObject(0);
				if (jObj1.has(GEOMETRY_KEY)) {
					String address = null;
					if (jObj1.has(ADDRESS_KEY))
						address = jObj1.getString(ADDRESS_KEY);
					jObj2 = jObj1.getJSONObject(GEOMETRY_KEY);
					if (jObj2.has(LOCATION_KEY)) {
						jObj3 = jObj2.getJSONObject(LOCATION_KEY);
						mLocation = new Location(jObj3.getDouble(LATITUDE_KEY),
								jObj3.getDouble(LONGITUDE_KEY), address);
						return mLocation;
					}
				}
			}
		}
		return mLocation;
	}

	private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
	private static final String API_KEY = "AIzaSyC1CHljfuBrSeTgXmm_NuJ1HZC6moNRRPE";
	private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
	private static final String OUT_JSON = "/json";

	public static ArrayList<String> autocomplete(String input) {
		ArrayList<String> resultList = null;

		HttpURLConnection conn = null;
		StringBuilder jsonResults = new StringBuilder();
		try {
			StringBuilder sb = new StringBuilder(PLACES_API_BASE
					+ TYPE_AUTOCOMPLETE + OUT_JSON);
			sb.append("?sensor=false&key=" + API_KEY);
			sb.append("&input=" + URLEncoder.encode(input, "utf8"));

			URL url = new URL(sb.toString());
			conn = (HttpURLConnection) url.openConnection();
			InputStreamReader in = new InputStreamReader(conn.getInputStream());

			// Load the results into a StringBuilder
			int read;
			char[] buff = new char[1024];
			while ((read = in.read(buff)) != -1) {
				jsonResults.append(buff, 0, read);
			}
		} catch (MalformedURLException e) {
			Log.e("placeAPI", "Error processing Places API URL", e);
			return resultList;
		} catch (IOException e) {
			Log.e("placeAPI", "Error connecting to Places API", e);
			return resultList;
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}

		try {
			// Create a JSON object hierarchy from the results
			JSONObject jsonObj = new JSONObject(jsonResults.toString());
			JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

			// Extract the Place descriptions from the results
			resultList = new ArrayList<String>(predsJsonArray.length());
			for (int i = 0; i < predsJsonArray.length(); i++) {
				resultList.add(predsJsonArray.getJSONObject(i).getString(
						"description"));
			}
		} catch (JSONException e) {
			Log.e("placeAPI", "Cannot process JSON results", e);
		}

		return resultList;
	}
}

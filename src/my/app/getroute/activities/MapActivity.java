package my.app.getroute.activities;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import my.app.getroute.adapters.InstructionAdapter;
import my.app.getroute.utils.HttpConnection;
import my.app.getroute.utils.JsonParser;
import my.app.getroute.utils.Location;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.my.getroute.R;

public class MapActivity extends FragmentActivity {

	public final static String RESULT = "result";
	public final static String INPUT = "input";

	List<List<HashMap<String, String>>> mRoutes = new ArrayList<List<HashMap<String, String>>>();
	List<String> mInstructionList = new ArrayList<String>();
	// Google Map
	private GoogleMap googleMap;

	private Marker mCurrentMarker;
	private CameraPosition cameraPosition;
	private Location mCurrentLocation;

	private ListView mDirectionLists;
	private String mSrc = "Delhi";
	private String mDest = "Kanpur";
	ProgressDialog mProgressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_activity);
		hideStatusBar();
		mSrc = getIntent().getStringExtra("origin");
		mDest = getIntent().getStringExtra("dest");
		try {
			// Loading map
			initilizeMap();
			initView();
			mProgressDialog = ProgressDialog.show(MapActivity.this, "Loading",
					"Please wait ..");
			if (mSrc != null && mDest != null) {

				if (mSrc.contains(",") && mDest.contains(",")) {
					mOriginLocation = getLocationFrmString(mSrc);
					mDestLocation = getLocationFrmString(mDest);
					traceRoute(mOriginLocation, mDestLocation);
				} else if (!mSrc.contains(",") && !mDest.contains(",")) {
					getGeocodeAndTraceRoute(mSrc, mDest);
				} else {
					Toast.makeText(this,
							"Please provide proper source and destination",
							Toast.LENGTH_LONG).show();

				}

			} else {
				Toast.makeText(this, "Please provide source and destination",
						Toast.LENGTH_LONG).show();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		initilizeMap();
	}

	/**
	 * function to load map If map is not created it will create it for you
	 * */
	private void initilizeMap() {
		if (googleMap == null) {
			FragmentManager fragmentManager = getSupportFragmentManager();
			SupportMapFragment mapFragment = (SupportMapFragment) fragmentManager
					.findFragmentById(R.id.map);
			googleMap = mapFragment.getMap();
			// check if map is created successfully or not
			if (googleMap == null) {
				Toast.makeText(getApplicationContext(),
						"Sorry! unable to create maps", Toast.LENGTH_SHORT)
						.show();
			}
		}

		// Changing map type
		googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		// Showing / hiding your current location
		googleMap.setMyLocationEnabled(true);

		// Enable / Disable zooming controls
		googleMap.getUiSettings().setZoomControlsEnabled(true);

		// Enable / Disable my location button
		googleMap.getUiSettings().setMyLocationButtonEnabled(false);

		// Enable / Disable Compass icon
		googleMap.getUiSettings().setCompassEnabled(true);

		// Enable / Disable Rotate gesture
		googleMap.getUiSettings().setRotateGesturesEnabled(true);

		// Enable / Disable zooming functionality
		googleMap.getUiSettings().setZoomGesturesEnabled(true);

	}

	private void hideStatusBar() {
		WindowManager.LayoutParams attrs = this.getWindow().getAttributes();
		attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
		this.getWindow().setAttributes(attrs);
	}

	private void showStatusBar() {
		WindowManager.LayoutParams attrs = this.getWindow().getAttributes();
		attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
		this.getWindow().setAttributes(attrs);
	}

	String place;

	private void setListener() {

		googleMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {

			@Override
			public void onInfoWindowClick(Marker arg0) {
				arg0.hideInfoWindow();
			}
		});

	}

	private void initView() {
		mDirectionLists = (ListView) findViewById(R.id.lvDirections);
		setListener();
	}

	private Location getLocationFrmString(String string) {
		Location myLocation = null;
		if (string != null && string.contains(",")) {
			int index = string.indexOf(",");
			String lat = string.substring(0, index);
			String lng = string.substring(index + 1);
			String address = string;
			myLocation = new Location(Double.valueOf(lat), Double.valueOf(lng),
					address);
			return myLocation;
		}
		return myLocation;
	}

	private void getGeocodeAndTraceRoute(String src, String dest) {
		new RouteMapper(mSrc, mDest).execute();

	}

	private void traceRoute(Location src, Location dest) {
		String url = null;
		try {
			url = getMapsApiDirectionsUrl(src, dest);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		if (url != null) {
			MapTask downloadTask = new MapTask();
			downloadTask.execute(url);
		} else {
			Toast.makeText(MapActivity.this, "Sorry not able trace route ..",
					Toast.LENGTH_LONG).show();
		}

	}

	private Location mOriginLocation;
	private Location mDestLocation;

	private class RouteMapper extends AsyncTask<Void, Void, Void> {
		String src, dest;

		public RouteMapper(String orign, String dest) {
			this.src = orign;
			this.dest = dest;
		}

		@Override
		protected Void doInBackground(Void... params) {
			try {
				initRouteMapping(src, dest);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			if (mOriginLocation != null && mDestLocation != null) {
				String url = null;
				try {
					url = getMapsApiDirectionsUrl(mOriginLocation,
							mDestLocation);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				if (url != null) {
					MapTask downloadTask = new MapTask();
					downloadTask.execute(url);
				} else {
					Toast.makeText(MapActivity.this,
							"Sorry not able trace route ..", Toast.LENGTH_LONG)
							.show();
				}

			} else {
				Toast.makeText(MapActivity.this,
						"Sorry not able trace route ..", Toast.LENGTH_LONG)
						.show();
			}
		}

	}

	private void initRouteMapping(String origin, String dest)
			throws InterruptedException {
		final CountDownLatch mLatch = new CountDownLatch(2);
		Thread originThread = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					JSONObject jObj = JsonParser.getGeoCode(mSrc, false);
					mOriginLocation = JsonParser.getLocationFromJson(jObj);
					mLatch.countDown();
				} catch (JSONException e) {

					e.printStackTrace();
				} catch (IOException e) {

					e.printStackTrace();
				}

			}
		});

		Thread destThread = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					JSONObject jObj = JsonParser.getGeoCode(mDest, false);
					mDestLocation = JsonParser.getLocationFromJson(jObj);
					mLatch.countDown();
				} catch (JSONException e) {

					e.printStackTrace();
				} catch (IOException e) {

					e.printStackTrace();
				}

			}
		});
		originThread.start();
		destThread.start();
		mLatch.await();
	}

	private void placeMarker(Location mLocation, String place) {
		Location location = mLocation;
		if (location != null) {

			double newLat = location.getLatitude();
			double newLng = location.getLongitude();
			String newAddress = location.getAddress();
			LatLng mCoords = new LatLng(newLat, newLng);
			MarkerOptions marker = new MarkerOptions().position(mCoords);

			marker.title(place);
			if (newAddress != null)
				marker.snippet(newAddress);

			marker.icon(BitmapDescriptorFactory
					.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

			googleMap.addMarker(marker);
		}
	}

	private class MapTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... url) {
			String data = "";
			try {
				HttpConnection http = new HttpConnection();
				data = http.readUrl(url[0]);
			} catch (Exception e) {
				Log.d("Background Task", e.toString());
			}
			return data;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			new ParserTask().execute(result);
		}
	}

	private class ParserTask extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... jsonData) {

			JSONObject jObject;

			try {
				jObject = new JSONObject(jsonData[0]);
				JsonParser.parse(jObject, mRoutes, mInstructionList);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			ArrayList<LatLng> points = null;
			PolylineOptions polyLineOptions = null;

			// traversing through routes
			for (int i = 0; i < mRoutes.size(); i++) {
				points = new ArrayList<LatLng>();
				polyLineOptions = new PolylineOptions();
				List<HashMap<String, String>> path = mRoutes.get(i);

				for (int j = 0; j < path.size(); j++) {
					HashMap<String, String> point = path.get(j);

					double lat = Double.parseDouble(point.get("lat"));
					double lng = Double.parseDouble(point.get("lng"));
					LatLng position = new LatLng(lat, lng);

					points.add(position);
				}

				polyLineOptions.addAll(points);
				polyLineOptions.width(2);
				polyLineOptions.color(Color.RED);
			}
			placeMarker(mOriginLocation, MapActivity.this.mSrc);
			placeMarker(mDestLocation, MapActivity.this.mDest);

			googleMap.addPolyline(polyLineOptions);
			moveCamera(mOriginLocation);
			if (mInstructionList.size() > 0) {
				mDirectionLists.setAdapter(new InstructionAdapter(
						mInstructionList, MapActivity.this));
			}
			if (mProgressDialog != null) {
				mProgressDialog.dismiss();
			}
		}
	}

	private String getMapsApiDirectionsUrl(Location source, Location dest)
			throws UnsupportedEncodingException {
		String output = "json";
		String url = "https://maps.googleapis.com/maps/api/directions/"
				+ output + "?" + "origin="
				+ URLEncoder.encode(source.toString(), "UTF-8")
				+ "&destination=" + URLEncoder.encode(dest.toString(), "UTF-8");
		return url;
	}

	private String getMapsApiDirectionsUrl(String source, String dest)
			throws UnsupportedEncodingException {
		String output = "json";
		String url = "https://maps.googleapis.com/maps/api/directions/"
				+ output + "?" + "origin="
				+ URLEncoder.encode(source.toString(), "UTF-8")
				+ "&destination=" + URLEncoder.encode(dest.toString(), "UTF-8");
		return url;
	}

	private Location getMidLocation(Location loc1, Location loc2) {
		return new Location((loc1.getLatitude() + loc2.getLatitude()) / 2,
				(loc1.getLongitude() + loc2.getLongitude()) / 2, null);
	}

	private void moveCamera(Location location) {

		LatLng mCoords = new LatLng(location.getLatitude(),
				location.getLongitude());

		cameraPosition = new CameraPosition.Builder().target(mCoords).zoom(6)
				.build();

		googleMap.animateCamera(CameraUpdateFactory
				.newCameraPosition(cameraPosition));

	}
}

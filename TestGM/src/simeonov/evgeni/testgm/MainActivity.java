package simeonov.evgeni.testgm;

import java.io.Console;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONObject;

import simeonov.evgeni.model.POISQLDBHelper;
import simeonov.evgeni.model.PointOfInterest;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends Activity {
	
	public static final int STATUS_ACTIVE = 1;
	public static final int STATUS_PAUSE = 0;
	
	// Development Settings
	static final Boolean is_debug_mode = true;
	private Float delta = Float.valueOf("0");
	private Float step = Float.valueOf("0");
	private String sOut = new String("");

	// UI elements
	private GoogleMap map;
	private TextView txtOutput;
	private Marker current_position;

	// Application data and Settings
	private ArrayList<PointOfInterest> local_pois = new ArrayList<PointOfInterest>();
	private POISQLDBHelper oData;
	private long[] vibratePattern = new long[] { 0, 1000, 1000 };
	private int distance_threshold = 10;
	private int currentStatus = MainActivity.STATUS_ACTIVE;
	private Integer launchedNotifications = 0;
	private String poi_service_url = new String(
			"http://helios.imedia-dev.com/devspace/evgeni.simeonov/btbpicks/GMTest/GMTest.php?act=Export");

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		currentStatus = MainActivity.STATUS_ACTIVE;
		map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
				.getMap();
		map.setMyLocationEnabled(true);
		map.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {

			LatLng myPastLocation = null;
			LatLng myLocation = null;
			String currAddress = null;

			@Override
			public void onMyLocationChange(Location arg0) {
				
				currAddress =  getCompleteAddressString(arg0.getLatitude(), arg0
						.getLongitude());

				myLocation = new LatLng(arg0.getLatitude() + delta, arg0
						.getLongitude() + delta);
				makeUseOfNewLocation(new Float(myLocation.latitude), new Float(
						myLocation.longitude));
				txtOutput = (TextView) findViewById(R.id.outputText);
				txtOutput.setText("Current Location: "
						+ "\n"
						+ String.format("Lat: %.4f", myLocation.latitude)
						+ "\n"
						+ String.format("Lng: %.4f", myLocation.longitude)
						+ "\n"
						+ "Your current address is: " + currAddress
						+ sOut);

				if (current_position != null) {
					current_position.remove();
				}
				current_position = map.addMarker(new MarkerOptions()
						.position(myLocation)
						.title("You are here")
						.icon(BitmapDescriptorFactory
								.fromResource(R.drawable.ic_launcher)));

				if (myPastLocation == null) {
					map.moveCamera(CameraUpdateFactory.newLatLngZoom(
							new LatLng(myLocation.latitude,
									myLocation.longitude), 15));
					map.animateCamera(CameraUpdateFactory.zoomTo(15), 2000,
							null);
				}
				myPastLocation = myLocation;
			}
		});

		new POILoader().execute();
		
		LocationManager locationManager = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);

		LocationListener locationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				makeUseOfNewLocation(new Float(location.getLatitude() + delta),
						new Float(location.getLongitude() + delta));
			}

			public void onStatusChanged(String provider, int status,
					Bundle extras) {
			}

			public void onProviderEnabled(String provider) {
			}

			public void onProviderDisabled(String provider) {
			}
		};

		locationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
	}

	private void makeUseOfNewLocation(Float latitude, Float longitude) {
		
		Iterator<PointOfInterest> iter = local_pois.iterator();
		while (iter.hasNext()) {
			PointOfInterest poi = iter.next();
			Float distance = getDistance(new LatLng(latitude, longitude),
					poi.getPosition());
			if (distance <= distance_threshold) {
				createNotification("You are close to "+poi.getTitle());
				launchedNotifications += 1;
			}
		}
		
		delta += step;
	}
	
	private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i < returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();
                Log.w("My Current loction address", "" + strReturnedAddress.toString());
            } else {
                Log.w("My Current loction address", "No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.w("My Current loction address", "Canont get Address!");
        }
        return strAdd;
    }

	@Override
	protected void onPause() {
		super.onPause();
		currentStatus = MainActivity.STATUS_PAUSE;
		launchedNotifications = 0;
	}

	@Override
	protected void onResume() {
		super.onResume();
		currentStatus = MainActivity.STATUS_ACTIVE;
	}

	public float getDistance(LatLng myLocation, LatLng poi) {
		
		double earthRadius = 3958.75;
		double latDiff = Math.toRadians(poi.latitude - myLocation.latitude);
		double lngDiff = Math.toRadians(poi.longitude - myLocation.longitude);
		double a = Math.sin(latDiff / 2) * Math.sin(latDiff / 2)
				+ Math.cos(Math.toRadians(myLocation.latitude))
				* Math.cos(Math.toRadians(poi.latitude))
				* Math.sin(lngDiff / 2) * Math.sin(lngDiff / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double distance = earthRadius * c;

		int meterConversion = 1609;

		return new Float(distance * meterConversion).floatValue();
	}

	public void createNotification(String message) {
		
		if(this.launchedNotifications == 0 && currentStatus == MainActivity.STATUS_PAUSE){
			Uri soundUri = Uri.parse("android.resource://"
			        + getPackageName() + "/" + R.raw.jingle);
			
			NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
					this).setContentTitle("TestGM").setVibrate(vibratePattern)
					.setSmallIcon(R.drawable.ic_launcher).setContentText(message)
					.setSound(soundUri);
			Intent resultIntent = new Intent(this, MainActivity.class);

			TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
			stackBuilder.addParentStack(MainActivity.class);
			stackBuilder.addNextIntent(resultIntent);
			PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
					PendingIntent.FLAG_UPDATE_CURRENT);
			mBuilder.setContentIntent(resultPendingIntent);
			NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

			mNotificationManager.notify(1, mBuilder.build());
		}		

	}

	public static void log(String str) {
		if (is_debug_mode) {
			Log.e("TestGM", str);
		}

	}

	public static void deployPOIsToMap(GoogleMap map,
			ArrayList<PointOfInterest> pois) {
		
		Iterator<PointOfInterest> iter = pois.iterator();
		while (iter.hasNext()) {
			PointOfInterest poi = iter.next();
			map.addMarker(new MarkerOptions()
					.position(poi.getPosition())
					.title(poi.getTitle())
					.snippet(poi.getDetails())
					.icon(BitmapDescriptorFactory
							.fromResource(R.drawable.poi_marker)));
		}
		
	}

	public class POILoader extends AsyncTask<Void, Void, Void> {
		ProgressDialog pDialog;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			// Showing progress dialog
			// pDialog = new ProgressDialog(MainActivity.this);
			// pDialog.setMessage("Please wait...");
			// pDialog.setCancelable(false);
			// pDialog.show();

		}

		@Override
		protected Void doInBackground(Void... arg0) {

			oData = new POISQLDBHelper(MainActivity.this);
			final SQLiteDatabase db = oData.getReadableDatabase();

			try {
				// Make a service request and fetch new POIs
				ServiceHandler serviceHandler = new ServiceHandler();
				String jSONData = serviceHandler.makeServiceCall(
						poi_service_url, ServiceHandler.GET);

				if (jSONData != null) {

					// Update local database with new POIs
					JSONObject jsonObj = new JSONObject(jSONData);
					JSONArray JSONpois = jsonObj.getJSONArray("pois");
					for (int i = 0; i < JSONpois.length(); i++) {
						JSONObject p = JSONpois.getJSONObject(i);
						PointOfInterest poi = new PointOfInterest(
								p.getInt("id"), p.getString("title"), "",
								new LatLng(p.getDouble("lat"),
										p.getDouble("lng")));
						local_pois.add(poi);
						Cursor oLoop = db
								.rawQuery(
										"SELECT id, title, details, longitude, latitude FROM points_of_interest WHERE id = "
												+ poi.getId(), null);
						if (oLoop == null) {
							db.execSQL("INSERT INTO points_of_interest (title, longitude, latitude) values ('"
									+ poi.getTitle()
									+ "', "
									+ poi.getPosition().longitude
									+ ", "
									+ poi.getPosition().latitude + ")");
						} else {
							db.rawQuery(
									"UPDATE points_of_interest SET title = '"
											+ poi.getTitle()
											+ "', longitude = "
											+ poi.getPosition().longitude
											+ ", latitude = "
											+ poi.getPosition().latitude
											+ " WHERE id = " + poi.getId(),
									null);
						}

					}

					db.close();
					oData.close();
				}
			} catch (Exception e) {
				log(e.toString());
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			// Load Local POIs in APP's memory
			oData = new POISQLDBHelper(MainActivity.this);
			final SQLiteDatabase db = oData.getReadableDatabase();

			Cursor oLoop = db
					.rawQuery(
							"select title, details, longitude, latitude from points_of_interest",
							null);

			while (oLoop.moveToNext()) {
				local_pois.add(new PointOfInterest(oLoop.getString(0), oLoop
						.getString(1), new LatLng(
						new Float(oLoop.getString(2)), new Float(oLoop
								.getString(3)))));
			}

			oLoop.deactivate();

			// Deploy all POIs on map
			deployPOIsToMap(map, local_pois);

			// Dismiss the progress dialog
			// if (pDialog.isShowing())
			// pDialog.dismiss();

		}

	}

}
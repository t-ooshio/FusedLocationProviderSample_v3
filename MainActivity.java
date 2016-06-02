package com.sio.fusedlocationprovidersample_v3;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
//GooglePlayServiceSDK6.5で廃止
 import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
//import com.google.android.gms.common.api.LocationClient.ConnectionCallbacks;
//import com.google.android.gms.common.api.LocationClient.OnConnectionFailedListener;
//import com.google.android.gms.common.api.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.SharedPreferences;

public class MainActivity extends Activity implements OnClickListener {

	// FusedLocationProvider 用の Client
	private LocationClient _locationClient;
	private TextView _textResult;
	private Boolean _isStarted = false;

	private SharedPreferences _Pref;                // 設定データ本体用インスタンス
	private SharedPreferences.Editor _PrefEditor;    // 設定データ書き換え用インスタンス

	private Button btn_locate;
	private EditText edit_intervaltext;
	private EditText edit_Distancetext;
	private CheckBox ch_Log;
	private String Filename;
	private String sPath;
	private String FName;
	Calendar Startcalendar = null;                    // 測位開始時刻
	Calendar Endcalendar = null;                    // 測位完了時刻
	public static int _defm_interval = 10000;    // 測位間隔時間初期値(msec）
	public static int _defm_distance = 0;            // 測位距離初期値(m)
	private int _interval = 10000;                    // 測位間隔時間設定値(msec）
	private int _distance = 0;                            // 測位距離設定値(m)
	private int _log = 1;                                    // ログ取得


	// 以前と変わらない LocationListener
	private LocationListener _locationListener = null;
	/**
	 * ATTENTION: This was auto-generated to implement the App Indexing API.
	 * See https://g.co/AppIndexing/AndroidStudio for more information.
	 */
	private LocationClient client;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		_Pref = getSharedPreferences(getString(R.string.PREF_KEY),
				ListActivity.MODE_PRIVATE);
		// パッケージ名を取得してディレクトリパス生成
		sPath = "/sdcard/" + "Fusedlocationprovidersample_v2";

		edit_intervaltext = (EditText) findViewById(R.id.edit_IntervalTime);
		edit_Distancetext = (EditText) findViewById(R.id.edit_IntervalDistance);
		ch_Log = (CheckBox) findViewById(R.id.ch_Log);
		edit_intervaltext.setText(String.valueOf(_Pref.getInt(
				getString(R.string.m_interval), _defm_interval)));
		edit_Distancetext.setText(String.valueOf(_Pref.getInt(
				getString(R.string.m_distance), _defm_distance)));
		if (_Pref.getInt(getString(R.string.m_log), 1) == 1)
			ch_Log.setChecked(true);
		else
			ch_Log.setChecked(false);


		File file = new File(sPath);
		try {
			if (!file.exists()) {
				// パッケージ名のディレクトリが存在しない場合新規作成
				file.mkdir();
			}
		} catch (SecurityException ex) {
		}

		_locationListener = new LocationListener() {

			@Override
			public void onLocationChanged(final Location location) {
				MainActivity.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						String text = _textResult.getText().toString();
						SimpleDateFormat DF = new SimpleDateFormat("HH:mm:ss.SSS", Locale.JAPAN);
						String date = DF.format(location.getTime());
						text = date + " - "
								+ location.getLatitude() + "/" +
								+location.getLongitude() + "/" +
								+location.getAccuracy() +
								"\n" + text;


						if (_log == 1) {
							FName = sPath + "/" + Filename + ".txt";

							File files = new File(FName);
							files.getParentFile().mkdir();

							Endcalendar = Calendar.getInstance();

							long diffTime1 = Endcalendar.getTimeInMillis() - Startcalendar.getTimeInMillis();

							// ミリ秒
							long msecond = diffTime1 % 1000;
							// 秒
							long second = (diffTime1 / 1000) % 60;
							// 分
							long minute = (diffTime1 / 1000) / 60;

							String IntervalTime = String.valueOf(minute * 60 + second) + "." + String.valueOf(msecond);


							String writeString = location.getLongitude() + ","
									+ location.getLatitude() + ","
									+ location.getAccuracy() + " , "
									+ IntervalTime + "\n\r";


							FileOutputStream fos = null;
							try {
								fos = new FileOutputStream(files, true);
							} catch (FileNotFoundException e) {
								e.printStackTrace();
							}
							OutputStreamWriter osw = null;
							try {
								osw = new OutputStreamWriter(fos, "UTF-8");
							} catch (UnsupportedEncodingException e) {
								e.printStackTrace();
							}
							BufferedWriter bw = new BufferedWriter(osw);

							try {
								bw.write(writeString);
								bw.flush();
								bw.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						_textResult.setText(text);
					}
				});
			}
		};

		_textResult = (TextView) findViewById(R.id.text_result);

		btn_locate = (Button) findViewById(R.id.btn_Location);
		btn_locate.setOnClickListener(this);
		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.
		client = new LocationClient.Builder(this).addApi(AppIndex.API).build();
	}

	@Override
	protected void onDestroy() {
		stopLocate();

		_Pref = getSharedPreferences(getString(R.string.PREF_KEY),
				ListActivity.MODE_PRIVATE);
		_PrefEditor = _Pref.edit();

		_PrefEditor.putInt(getString(R.string.m_interval), Integer
				.parseInt(edit_intervaltext.getText().toString()));
		_PrefEditor.putInt(getString(R.string.m_distance), Integer
				.parseInt(edit_Distancetext.getText().toString()));
		if (ch_Log.isChecked())
			_PrefEditor.putInt(getString(R.string.m_log), 1);
		else
			_PrefEditor.putInt(getString(R.string.m_log), 0);

		_PrefEditor.commit();

		super.onDestroy();
	}

	private void startLocate() {
		_locationClient = new LocationClient(this, new ConnectionCallbacks() {

			@Override
			public void onConnected(Bundle bundle) {


				_textResult.setText("");
				Startcalendar = Calendar.getInstance();
				Filename = "";
				Filename = String.valueOf(Startcalendar.get(Calendar.YEAR))
						+ String.valueOf(Startcalendar.get(Calendar.MONTH))
						+ String.valueOf(Startcalendar.get(Calendar.DAY_OF_MONTH))
						+ String.valueOf(Startcalendar.get(Calendar.HOUR_OF_DAY))
						+ String.valueOf(Startcalendar.get(Calendar.MINUTE))
						+ String.valueOf(Startcalendar.get(Calendar.SECOND))
						+ String.valueOf(Startcalendar.get(Calendar.MILLISECOND));

				_interval = Integer.parseInt(edit_intervaltext.getText().toString());
				_distance = Integer.parseInt(edit_Distancetext.getText().toString());
				if (ch_Log.isChecked())
					_log = 1;
				else
					_log = 0;

				// 2. 位置の取得開始！
				LocationRequest request = LocationRequest.create()
						.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
								//.setInterval(_interval*1000)
						.setInterval(_interval)
						.setSmallestDisplacement(_distance);
				//_locationClient.requestLocationUpdates(request, _locationListener);

			}

			@Override
			public void onDisconnected() {
				_locationClient = null;
			}

		}, new OnConnectionFailedListener() {
			@Override
			public void onConnectionFailed(ConnectionResult result) {
			}
		});

		// 1. 位置取得サービスに接続！
		_locationClient.connect();
	}

	private void stopLocate() {
		if (_locationClient == null || !_locationClient.isConnected()) {
			return;
		}

		_locationClient.removeLocationUpdates(_locationListener);
		_locationClient.disconnect();
//		 ConnectionCallbacks.onDisconnected が呼ばれるまで待った方がいい気がする
	}

	@Override
	public void onClick(View v) {

		if (v.equals(btn_locate)) {
			if (!_isStarted) {
				startLocate();
				btn_locate.setText("測位中");
			} else {
				stopLocate();
				btn_locate.setText("停止中");
			}
			_isStarted = !_isStarted;
		}

	}

	@Override
	public void onStart() {
		super.onStart();

		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.
		client.connect();
		Action viewAction = Action.newAction(
				Action.TYPE_VIEW, // TODO: choose an action type.
				"Main Page", // TODO: Define a title for the content shown.
				// TODO: If you have web page content that matches this app activity's content,
				// make sure this auto-generated web page URL is correct.
				// Otherwise, set the URL to null.
				Uri.parse("http://host/path"),

					// TODO: Make sure this auto-generated app deep link URI is correct.
				Uri.parse("android-app://com.sio.fusedlocationprovidersample_v3/http/host/path")
		);
		AppIndex.AppIndexApi.start(client, viewAction);
	}

	@Override
	public void onStop() {
		super.onStop();

		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.
		Action viewAction = Action.newAction(
				Action.TYPE_VIEW, // TODO: choose an action type.
				"Main Page", // TODO: Define a title for the content shown.
				// TODO: If you have web page content that matches this app activity's content,
				// make sure this auto-generated web page URL is correct.
				// Otherwise, set the URL to null.
				Uri.parse("http://host/path"),
				// TODO: Make sure this auto-generated app deep link URI is correct.
				Uri.parse("android-app://com.sio.fusedlocationprovidersample_v3/http/host/path")
		);
		AppIndex.AppIndexApi.end(client, viewAction);
		client.disconnect();
	}
}


/*
 * Copyright (c) 2015, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


 /////////////////////////////////////////////////////////////////////////////
//
//  V2.9S, 24 Mar 2020
//	Added UpdateStatusDisplayBlock() so we would update on more messages from the app
//
/////////////////////////////////////////////////////////////////////////////

package com.mushin.muconnect;




import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.DownloadManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.database.Cursor;


import android.graphics.Color;

import android.os.Environment;
import java.io.File;
import java.text.SimpleDateFormat;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileInputStream;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.BarGraphSeries;

import com.jjoe64.graphview.ValueDependentColor;

public class MainActivity extends Activity implements RadioGroup.OnCheckedChangeListener {
	private static final int REQUEST_SELECT_DEVICE = 1;
	private static final int REQUEST_ENABLE_BT = 2;
	private static final int REQUEST_FILE_SELECT = 3;
	private static final int REQUEST_ACTIVITY = 4;
	private static final int SELECT_INIT_FILE_REQ = 2;
	
	private static final int UART_PROFILE_READY = 10;
	public static final String TAG = "muShin Display";
	private static final int UART_PROFILE_CONNECTED = 20;
	private static final int UART_PROFILE_DISCONNECTED = 21;
	private static final int STATE_OFF = 10;

	private static int HRVinputRecordNum=1;

	private static int  s_num_program 	= 0;						//which program is running 0-sleep 1-Nap 2-Relax
	private static int  s_num_steps		= 0;							//Current program Step 
	private static int  s_type_block		= 0;							//Which type of block is running 0 =7.83hz, 1:3hz, 2=1hz
	private static int  s_num_blocks		= 0;							//how many times to run a block
	private static int  s_segment		= 0;								//how many times to run a block

	private static int Connection_Number = -1;

	private static int NumRecsForThisSessionID = 0;

	TextView mRemoteRssiVal;
	RadioGroup mRg;
	private int mState = UART_PROFILE_DISCONNECTED;
	public static UartService mService = null;
	private BluetoothDevice mDevice = null;
	private BluetoothAdapter mBtAdapter = null;
	
	//private ListView messageListView;

	// private ArrayAdapter<String> listAdapter;

	private com.mushin.muconnect.Configuration deviceConfiguration = new com.mushin.muconnect.Configuration();

	public com.mushin.muconnect.Configuration getDeviceConfiguration() {
		return deviceConfiguration;
	}

	public void setDeviceConfiguration(com.mushin.muconnect.Configuration newConfiguration) {

		ArrayList<String> updateCfgCommands = com.mushin.muconnect.Configuration.getConfigurationUpdateCommands(this.deviceConfiguration, newConfiguration);

		// TODO: send updated configuration to device

		this.deviceConfiguration = newConfiguration;
	}

	private enum ConnectionState {
		DISCONNECTED,
		CONNECTED,
		RECONNECTING,
		READY
	}

	private ConnectionState connectionState = ConnectionState.DISCONNECTED;

	private Button btnConnectDisconnect;
	private Button btnRequestData;
	private Button btnActivities;
	private Button btnConfig;
	
	// private Button btnSend;
	
	// private EditText edtMessage;

	public static FileWriter mDataFileWriter = null;
	public static File mDataFile = null;

	public static String FirstFileName;
	public static File FirstDataFile =null;

	private LineGraphSeries<DataPoint> mLineSeriesHR;
	private LineGraphSeries<DataPoint> mLineSeriesHRV;
	private BarGraphSeries<DataPoint> mBarSeriesSQ;

	private static final int HRV_X_MAX = 10;
	private static final int HR_X_MAX = 10;

	private static boolean bDataHeader = false;
	private static boolean bLogHeader = false;
	private boolean bGotDeviceName = false;
	private boolean bEraseAlready=false;
	private String EraseThisFIle;
	

	private static final int SQ_GRAPH_MULTIPLIER = 1;
		
	private GraphView HRgraph; 
	private GraphView HRVgraph;
	private static int mHRSampleNumber = 0;
	private static int mHRVSampleNumber = 0;

	public static int DeviceSessionNumber = -1;
	public static int DeviceMode = -1;
	public static int DeviceStatus = -1;

	public static int CSV_Filetype = 0;

	private boolean BLE_started = false;
	private boolean FirstSendData = true;
	public static boolean DoingSendData = false;
	public static boolean DoingSendLog = false;
	private String currentDateTimeString, firstDateTimeString;
	public static String DeviceConnectionName = "NONAME";
	public static String DeviceErrorInfo = "NOINFO";
	private String DeviceFirmwareVersion = "";
	private String VCFWFirmwareVersion = "";
	private String ComboFirmwareVersion = "";
	private String DeviceStageVersion = "";
	private String DeviceModeString = "";
	private String DeviceMaskStatusString = "";
	private String SessionNumberString = "";
	private int iFirstRRi;

	public static boolean FirstCreated=true;

	public static boolean ScheduleCreateDL=false;
	public static boolean ScheduleCreateLG=false;

	private TextView HRValue;

	private static final String LOG_TAG = "MUSHIN_BLE";

	public static String CurrentFileName="NULL";

	public static String OddMessage = "";

	private static final  int PACKET_HRVDATA = 1;		// HRV Data
	private static final  int PACKET_CPS = 2;		// Current Program Status
	private static final  int PACKET_FILENUM = 3;		// FDS File Number
	private static final  int PACKET_DEVICE_NAME = 4;		// Unique Mask Name
	private static final  int PACKET_FIRMWARE_VERSION = 5;		// Version
	private static final  int PACKET_BATTERY_STATUS = 6;
	private static final  int PACKET_STOPPING = 7;		// Battery Status
	private static final  int PACKET_VCFW_VERSION = 8;		// VC FW eresion
	private static final  int PACKET_MASK_STAGE = 9;		// Mask session stage
	private static final  int PACKET_SESSION_ID = 20	;	// Current session id
	private static final  int PACKET_PACKET_HRVEND = 21	;	// Signifies end of current session HRV data
	private static final  int PACKET_HRVDONE = 22;		// Signifies end of ALL session HRV data
	private static final  int PACKET_MASK_MODE = 23;	// mode is HRV or standard
	private static final  int PACKET_LIGHT_SOUND_LEVELS = 24;	// Light and Sound Levels

	private static final  int PACKET_MASK_STATUS = 25;	// Light and Sound Levels
	private static final  int PACKET_ERROR_INFO = 26;	// Error information

	private static final  int PACKET_EVENT_ONE = 27;		// First half of event/error packet
	private static final  int PACKET_EVENT_TWO = 28;		// Second half of event/error packet

	private static final int PACKET_SEND_VCFW_SIZE		= 10;	// VC FW Update
	private static final int PACKET_VCFW_ACK = 11	;	// ACK of FW update packet
	private static final int PACKET_VCFW_NACK = 12;	// NACK of FW update packet


	private static final int 	STORED_MODE_CHANGEABLE_MASK		= (1 << 0);
	private static final int 	STORED_REAL_OR_SHAM_MASK			= (1 << 1);
	private static final int 	STORED_APP_OR_NOAPP_MASK			= (1 << 2);
	private static final int 	STORED_STDBLE_OR_SECUREBLE_MASK	= (1 << 3);

	private static final int 	VALENCELL_ERROR	=		1;
	private static final int 	I2C_ERROR			=		2;
	private static final int 	VS1000_ERROR		=		3;
	private static final int 	BATTERY_ERROR		=		4;
	private static final int 	SPI_ERROR			=		5;
	private static final int 	FLASH_ERROR		=		6;
	private static final int 	TEST_ERROR			=		7;

	
	private static final int CF_ID_TYPE=0;
	private static final int CF_DL_TYPE=1;
	private static final int CF_LG_TYPE=2;
	private static final int CF_UK_TYPE=3;


	private String BatteryStg = "";
	private String StopCode="";
	private String LightSoundLevelStg="";

	private int GaugeLevel = 0;
	private int BatteryLow = 0;
	private int ChargerConnected = 0;
	private int Voltage	 = 0;

	private int SoundLevel;
	private int LightLevel;

	private String mFilePath;
	private Uri mFileStreamUri;
	private String mInitFilePath;
	private Uri mInitFileStreamUri;
	private int mFileType;
	private int mFileTypeTmp; // This value is being used when user is selecting a file not to overwrite the old value (in case he/she will cancel selecting file)

	private byte[] BytesVCFW;
	private int BytesVCFWLen, BytesVCFWSent=0;

	public static String dataFileName;

	private int iHrData = 0;
	private int[] iHrvData;
	private int iSignalQuality = 0;
	private boolean bAllocatedHrvData = false;

	private int errorEventCode  = 0;
	private int errorTimeSincePwrOn  = 0;
	private int errorData  = 0;
	private int errorSessionID  = 0;
	private int errorLineNum  = 0;
	private String errorFuncName = "";

	private int NoOfRRI		= 0;
	private int RRITime		= 0;

    private static final int REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION = 201;
    private String permissions = Manifest.permission.WRITE_EXTERNAL_STORAGE;

    /** Called when the user taps the Send button */
	public void sendMessage(View view) {
		// Do something in response to button
	}

	public class SendDataParameters {
	    public int INVALID_SESSION_ID = -1;
	    private int fromSession;
	    private int toSession;
	    private int currentSession = INVALID_SESSION_ID;

        SendDataParameters(int fromSession, int toSession) {
            this.fromSession = fromSession;
            this.toSession = toSession;
            this.currentSession = fromSession;
        }

        public void finishCurrentSession() {
            currentSession++;
        }
    }

    private SendDataParameters sendDataParameters = null;

    public void initiateSendData(int fromSession, int toSession) {
		mService.requestHighConnectionPriority(true);

        this.sendDataParameters = new SendDataParameters(fromSession, toSession);
	    SendSendData();
    }

    public void finalizeSendData() {
        this.sendDataParameters = null;
    }

    // request data either until End-Of-Transmission response
	public void SendSendData()
	{
        String message = "SendData";

        if (sendDataParameters != null) {

            message = message + " " + String.valueOf(sendDataParameters.fromSession) + " " + String.valueOf(sendDataParameters.toSession);
        }

        byte[] value;
        try
        {
            // send data to service
            value = message.getBytes("UTF-8");
            showMessage(getApplicationContext(),"Requesting Data From Device");
            Log.e(LOG_TAG,"Requesting Data From Device");

            if(mService != null) mService.writeRXCharacteristic(value);

            CloseDataFile(getApplicationContext());

            ScheduleCreateDL = true;

            Log.e(LOG_TAG, "*** DOING ScheduleCreateDL ");

            DoingSendData = true;
        }
        catch (UnsupportedEncodingException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
	}

	////////////////////////////////////////////////////////////

	public class SendLogParameters {
	    public int INVALID_SESSION_ID = -1;
	    private int fromSession;
	    private int toSession;
	    private int currentSession = INVALID_SESSION_ID;

        SendLogParameters(int fromSession, int toSession) {
            this.fromSession = fromSession;
            this.toSession = toSession;
            this.currentSession = fromSession;
        }

        public void finishCurrentSession() {
            currentSession++;
        }
    }

    private SendLogParameters sendLogParameters = null;

    public void initiateSendLog(int fromSession, int toSession) {
		mService.requestHighConnectionPriority(true);

        this.sendLogParameters = new SendLogParameters(fromSession, toSession);
	    SendSendLog();
    }

    public void finalizeSendLog() {
        this.sendLogParameters = null;
    }


public void InitGraphs()
{

	// Create HR graph and set axis dimensions
	HRgraph = (GraphView) findViewById(R.id.HRgraph);

	HRgraph.getViewport().setXAxisBoundsManual(true);
	HRgraph.getViewport().setMinX(0);
	HRgraph.getViewport().setMaxX(HR_X_MAX);
	HRgraph.getViewport().setYAxisBoundsManual(true);
	HRgraph.getViewport().setMinY(0);
	HRgraph.getViewport().setMaxY(180);

	mLineSeriesHR = new LineGraphSeries<>();
	HRgraph.addSeries(mLineSeriesHR);

	// Create SQ Bar graph and set axis dimensions

	// Create HRV graph and set axis dimensions

	HRVgraph = (GraphView) findViewById(R.id.HRVgraph);

	HRVgraph.getViewport().setXAxisBoundsManual(true);
	HRVgraph.getViewport().setMinX(0);
	HRVgraph.getViewport().setMaxX(HRV_X_MAX);

	HRVgraph.getViewport().setYAxisBoundsManual(true);
	HRVgraph.getViewport().setMinY(0);
       HRVgraph.getViewport().setMaxY(150);

	mLineSeriesHRV = new LineGraphSeries<>();
	mBarSeriesSQ = new BarGraphSeries<>();

	//HRVgraph.addSeries(mLineSeriesHRV);

        HRVgraph.addSeries(mBarSeriesSQ);
        HRVgraph.getGridLabelRenderer().setVerticalLabelsColor(Color.rgb(255,255,255));

	// Set some display characteristics for the SQ bar graph

        mBarSeriesSQ.setDrawValuesOnTop(true);
        mBarSeriesSQ.setValuesOnTopSize(20);
        mBarSeriesSQ.setValuesOnTopColor(Color.DKGRAY);
        mBarSeriesSQ.setSpacing(50);

	// Set the quality axis up to 150 so the bar graph doesn't dominate the graph
	
	HRVgraph.getSecondScale().setMinY(0);
	HRVgraph.getSecondScale().setMaxY(2000);
	HRVgraph.getSecondScale().addSeries(mLineSeriesHRV);
	
	// Erase the quality Y axis numbers, they're not necessary
       // HRVgraph.getGridLabelRenderer().setVerticalLabelsSecondScaleColor(Color.rgb(255,255,255));

	mHRSampleNumber = 0;
	mHRVSampleNumber = 0;
}

    // request data either until End-Of-Transmission response
	public void SendSendLog()
	{
        String message = "SendLog";

        if (sendLogParameters != null) {

            message = message + " " + String.valueOf(sendLogParameters.fromSession) + " " + String.valueOf(sendLogParameters.toSession);
        }

        byte[] value;
        try
        {
            // send data to service
            value = message.getBytes("UTF-8");
            showMessage(getApplicationContext(),"Requesting Log From Device");
            Log.e(LOG_TAG,"Requesting Log From Device");

            if(mService != null) mService.writeRXCharacteristic(value);
			else showMessage(getApplicationContext(),"Requesting Log but Service Null");

            CloseDataFile(getApplicationContext());

            ScheduleCreateLG = true;

            Log.e(LOG_TAG, "*** DOING ScheduleCreateLG ");

            DoingSendLog = true;
        }
        catch (UnsupportedEncodingException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
	}



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) {
            showMessage(getApplicationContext(),"Bluetooth is not available");
            finish();
            return;
        }
		
	// messageListView = (ListView) findViewById(R.id.listMessage);
	// listAdapter = new ArrayAdapter<String>(this, R.layout.message_detail);
	
	// messageListView.setAdapter(listAdapter);
	
	// messageListView.setDivider(null);
	
	btnConnectDisconnect = (Button) findViewById(R.id.btn_select);
	//btnRequestData = (Button) findViewById(R.id.xmodem_vcfw_button);
	btnActivities = (Button) findViewById(R.id.activities_button);
	btnConfig = (Button) findViewById(R.id.config_button);
	
	// btnSend = (Button) findViewById(R.id.sendButton);
	// edtMessage = (EditText) findViewById(R.id.sendText);
	service_init();

	InitGraphs();

	/*

	// Create HR graph and set axis dimensions
	HRgraph = (GraphView) findViewById(R.id.HRgraph);

	HRgraph.getViewport().setXAxisBoundsManual(true);
	HRgraph.getViewport().setMinX(0);
	HRgraph.getViewport().setMaxX(HR_X_MAX);
	HRgraph.getViewport().setYAxisBoundsManual(true);
	HRgraph.getViewport().setMinY(0);
	HRgraph.getViewport().setMaxY(180);

	mLineSeriesHR = new LineGraphSeries<>();
	HRgraph.addSeries(mLineSeriesHR);

	// Create SQ Bar graph and set axis dimensions

	// Create HRV graph and set axis dimensions

	HRVgraph = (GraphView) findViewById(R.id.HRVgraph);

	HRVgraph.getViewport().setXAxisBoundsManual(true);
	HRVgraph.getViewport().setMinX(0);
	HRVgraph.getViewport().setMaxX(HRV_X_MAX);

	HRVgraph.getViewport().setYAxisBoundsManual(true);
	HRVgraph.getViewport().setMinY(0);
       HRVgraph.getViewport().setMaxY(150);

	mLineSeriesHRV = new LineGraphSeries<>();
	mBarSeriesSQ = new BarGraphSeries<>();

	//HRVgraph.addSeries(mLineSeriesHRV);

        HRVgraph.addSeries(mBarSeriesSQ);
        HRVgraph.getGridLabelRenderer().setVerticalLabelsColor(Color.rgb(255,255,255));

	// Set some display characteristics for the SQ bar graph

        mBarSeriesSQ.setDrawValuesOnTop(true);
        mBarSeriesSQ.setValuesOnTopSize(20);
        mBarSeriesSQ.setValuesOnTopColor(Color.DKGRAY);
        mBarSeriesSQ.setSpacing(50);

	// Set the quality axis up to 150 so the bar graph doesn't dominate the graph
	
	HRVgraph.getSecondScale().setMinY(0);
	HRVgraph.getSecondScale().setMaxY(2000);
	HRVgraph.getSecondScale().addSeries(mLineSeriesHRV);
	
	// Erase the quality Y axis numbers, they're not necessary
       // HRVgraph.getGridLabelRenderer().setVerticalLabelsSecondScaleColor(Color.rgb(255,255,255));

	mHRSampleNumber = 0;
	mHRVSampleNumber = 0;

	*/

	//HRVgraph.getSecondScale().addSeries(mBarSeriesSQ);

	// Set the quality axis up to 150 so the bar graph doesn't dominate the graph
        //HRVgraph.getSecondScale().setMinY(0);
		
        //HRVgraph.getSecondScale().setMaxY(150);
;
	// Erase the quality Y axis numbers, they're not necessary
       // HRVgraph.getGridLabelRenderer().setVerticalLabelsSecondScaleColor(Color.rgb(255,255,255));
	// styling
	
	mBarSeriesSQ.setValueDependentColor(new ValueDependentColor<DataPoint>() {
	@Override
	public int get(DataPoint data) {

        int redval, blueval, greenval;

        redval = 0;
        blueval = 0;
        greenval = 0;

        if(data.getY() >= 95*SQ_GRAPH_MULTIPLIER)
        {
                greenval = 255;
            //mBarSeriesSQ.setValuesOnTopColor(Color.GREEN);
        }
        else
        {
                redval = 255;
            //mBarSeriesSQ.setValuesOnTopColor(Color.RED);
        }
	    return Color.argb(180, redval, greenval, blueval);
	}
	}
	);

    btnActivities.setOnClickListener(new View.OnClickListener()
	{
		@Override
		public void onClick(View v) 
		{
			//Connect button pressed, open DeviceListActivity class, with popup windows that scan for devices

//			Intent newIntent = new Intent(MainActivity.this, ButtonsActivity.class);
//			startActivityForResult(newIntent, REQUEST_ACTIVITY);
            DialogFragment newFragment = new ButtonsActivity();
            newFragment.show(getFragmentManager(), "ButtonsActivity");
        }
	}
	);

		btnConfig.setOnClickListener(new View.OnClickListener()	{
				@Override
				public void onClick(View v) {
					DialogFragment newFragment = new ConfigActivity();
					newFragment.show(getFragmentManager(), "ConfigActivity");
				}
			}
		);


		// Handle Disconnect & Connect button
        btnConnectDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mBtAdapter.isEnabled()) {
                    Log.i(TAG, "onClick - BT not enabled yet");
                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                } else {
                    if (connectionState == ConnectionState.DISCONNECTED) {

                //Connect button pressed, open DeviceListActivity class, with popup windows that scan for devices

                Intent newIntent = new Intent(MainActivity.this, com.mushin.muconnect.DeviceListActivity.class);
                //Intent newIntent = new Intent(MainActivity.this, DeviceListActivity.class);
                startActivityForResult(newIntent, REQUEST_SELECT_DEVICE);
                bGotDeviceName = false;
                DeviceConnectionName ="NODEVICENAME";
                    } else {
                        //Disconnect button pressed
                        if (mDevice != null) {
                            mService.disconnect();

                        }
                    }
                }
            }
        });

        checkWriteExternalStoragePermissions();
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.settings, menu);

		MenuItem itemPasscode = menu.findItem(R.id.useGeneratedPasscode);
		if (itemPasscode != null) {
			itemPasscode.setChecked(Utils.getPreference(this, "useGeneratedPasscode", false));
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId()){
			case R.id.useGeneratedPasscode:
				item.setChecked(!item.isChecked());
				Utils.setPreference(this, "useGeneratedPasscode", item.isChecked());
				if (mService != null) {
					mService.setEnableGeneratedPasscode(item.isChecked());
				}
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private void checkWriteExternalStoragePermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            // do nothing
        } else {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), permissions) != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(permissions)) {
                    // TODO: show explanation why location is needed
                } else {
                    requestPermissions(new String[]{permissions}, REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION);
                }
            } else {
                Log.d(TAG, "Write to external storage permissions has been granted already");
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        boolean permissionsGranted = false;

        switch (requestCode){
            case REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION:
                permissionsGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (permissionsGranted ) {
            Log.d(TAG, "Write to external storage permissions has been granted");
        } else {
            Log.d(TAG, "Can't write to external storage, permissions not granted");
        }
    }



    public static void createDataFile(Context context, int Type, int SessionNumber)
{

        File dlDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File dataDir = new File(dlDir, "muShinData");
 
        if (dataDir.exists()) {
            Log.e(LOG_TAG, "muShinData directory already exists");
        } else {
            dataDir.mkdirs();
            Log.e(LOG_TAG, "muShinData directory created");
        }

	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
	String currentDateandTimeString = sdf.format(new Date());
	String deviceModeStg;

        boolean appendToDataFile = false;

	CSV_Filetype = Type;

	if(Type == CF_ID_TYPE)		// Immediate Data
	{
		dataFileName = "muShin_ID_";
		
	}
	else if(Type == CF_DL_TYPE)	// Download from data area
	{
		dataFileName = "muShin_DL_";
	}
	else if(Type == CF_LG_TYPE)	// Download from error/entry log
	{
		dataFileName = "muShin_LG_";
	}
	else					// Unknown
	{
		dataFileName = "muShin_UK_";
	}

	if(DeviceMode == 1)
		deviceModeStg = "_HRV";
	else
		deviceModeStg = "_STD";

	bDataHeader = false;

	dataFileName = dataFileName + DeviceConnectionName;
	dataFileName = dataFileName + deviceModeStg;
	dataFileName = dataFileName + "_"+String.format("%05d", SessionNumber).trim();
	dataFileName = dataFileName + "_"+currentDateandTimeString+".csv";

	 Log.e(LOG_TAG, "!! dataFileName " + dataFileName);

	CurrentFileName = dataFileName;
        mDataFile = new File(dataDir, dataFileName);

	if(FirstCreated)
	{
		FirstFileName = CurrentFileName;
		FirstDataFile = mDataFile;
		FirstCreated = false;
	}

        // If file does not exists, then create it
        if (!mDataFile.exists()) {
            try{
                if(!mDataFile.createNewFile()) {
                    Log.e(LOG_TAG, "Unable to create data file "+ dataFileName);
                    mDataFileWriter = null;
                    return;
                }
                else {
                    Log.e(LOG_TAG, "Data file " + dataFileName + " created");
                    appendToDataFile = false;

			showMessage(context,"Creating "+ dataFileName);
                }
            }
            catch(IOException e) {
				Log.e(LOG_TAG, "Data file IOException");
                e.printStackTrace();
                mDataFileWriter = null;
                return;
            }
        }
        else {
            Log.e(LOG_TAG, "Data file "+ dataFileName +" already exists");
            appendToDataFile = true;
        }

        try {
            mDataFileWriter = new FileWriter(mDataFile.getAbsoluteFile(), appendToDataFile);
        }
        catch(IOException e){
            e.printStackTrace();
            mDataFileWriter = null;
        }

        return;
     }

	public static void CloseDataFile(Context context )
	{
		Log.e(LOG_TAG, "Closing CSV file");

		if (mDataFileWriter != null)
		{
			try {
				showMessage(context, "Closing " + CurrentFileName);
				Log.e(LOG_TAG, "Closing " + CurrentFileName);
				mDataFileWriter.flush();
				mDataFileWriter.close();

				addFileToDownloadManager(mDataFile, CurrentFileName, "muShin CSV file", "text/plain", context);

				mDataFileWriter = null;
			} 
			catch (Exception e) 
			{
				Log.e(TAG, e.toString());
			}
		}
	}
	
    //UART service connected/disconnected
    private ServiceConnection mServiceConnection = new ServiceConnection() 
    {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) 
		{
			mService = ((UartService.LocalBinder) rawBinder).getService();
			Log.d(TAG, "onServiceConnected mService= " + mService);
			if (!mService.initialize()) {
				Log.e(TAG, "Unable to initialize Bluetooth");
				finish();
			} else {
				boolean useGeneratedPasscode = Utils.getPreference(getApplicationContext(), "useGeneratedPasscode", false);
				mService.setEnableGeneratedPasscode(useGeneratedPasscode);
			}
		}

        public void onServiceDisconnected(ComponentName classname) 
	{
        		mService = null;
        }
    };

    private Handler mHandler = new Handler() 
{
        @Override
        
        //Handler events that received from UART service 
        public void handleMessage(Message msg) {
  
        }
    };

	private void UpdateInfoDisplayBlock()
	{
		String s_Connection_Number;

		s_Connection_Number = ", CN: " +String.format("%d", Connection_Number);
		
		SessionNumberString = "Session Number " +String.format("%d", DeviceSessionNumber);
		ComboFirmwareVersion = "muShin "+ DeviceFirmwareVersion + ":" + DeviceMaskStatusString +"\r\n" +"VCFW " + VCFWFirmwareVersion  + " " + SessionNumberString
			+ "\r\n" + DeviceModeString
			+ "\r\n" + DeviceErrorInfo + s_Connection_Number;
		((TextView) findViewById(R.id.hrv_diff_box)).setText(ComboFirmwareVersion);
		Log.e(LOG_TAG, "Version "+ComboFirmwareVersion);

		String s_num_program_string;

		if(s_num_program == 0) s_num_program_string ="Standard Mode";
		else if(s_num_program == 1) s_num_program_string ="Sham Mode";
		else if(s_num_program == 2) s_num_program_string ="HW Test Mode";
		else  s_num_program_string = String.format("Unknown Mode %d  ", s_num_program);

		String s_aggregate_string;

		s_aggregate_string = String.format("Steps left: %d, ", s_num_steps);
		if(s_type_block == 0) s_aggregate_string = s_aggregate_string + String.format("Freq: 7.83 Hz,  ", s_num_steps);
		else if(s_type_block == 1) s_aggregate_string = s_aggregate_string + String.format("Freq: 3 Hz,  ", s_num_steps);
		else if(s_type_block == 2) s_aggregate_string = s_aggregate_string + String.format("Freq: 1 Hz,  ", s_num_steps);
		else s_aggregate_string = s_aggregate_string + "Freq: UNK, ";

		s_aggregate_string = s_aggregate_string + String.format("Blocks left: %d,  ", s_num_blocks);
		s_aggregate_string = s_aggregate_string + String.format("Segment: %d ", s_segment);

		String s_whole_string;

		s_whole_string = s_num_program_string +"\r\n"+ s_aggregate_string;

		((TextView) findViewById(R.id.pgm_stats)).setText(s_whole_string);
		Log.e(LOG_TAG, "Version "+s_whole_string);
		
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private void AddRecordToOutputFile()
	{

		String TimeStampedData, HRVstg;
		String DiffItem;
		int iRRi;
		int nIdx;	
		
		Log.e(LOG_TAG, "AddRecordToOutputFile");

		if(ScheduleCreateDL)
		{
			Log.e(LOG_TAG, "*** DOING createDataFile of DL ");
			createDataFile(getApplicationContext(), CF_DL_TYPE, DeviceSessionNumber);
			ScheduleCreateDL =  false;
		}

		int iHrInEar = iSignalQuality & 0x01;
		iSignalQuality >>= 1;
		
		currentDateTimeString = DateFormat.getTimeInstance().format(new Date());

		// Let's get this written before doing graphical update
		if((mDataFileWriter != null) && bGotDeviceName)
		{

			// Create CSV string for output
			TimeStampedData = DeviceConnectionName + "," + currentDateTimeString + ","
			+ String.valueOf(DeviceSessionNumber) + ","
			+ String.format(" %d", RRITime)  
			+ String.format(" ,%d", iHrData)  
			+ String.format(" ,%d", NoOfRRI) 
			+ String.format(" ,%d", iSignalQuality) 
			+ String.format(" ,%d", iHrvData[0])
			+ String.format(" ,%d", iHrvData[1])
			+ String.format(" ,%d", iHrvData[2])
			+ String.format(" ,%d", iHrvData[3])
			+ String.format(" ,%d", iHrvData[4])
			+ String.format(" ,%d", s_num_program)
			+ String.format(" ,%d", s_type_block)
			+ String.format(" ,%d", s_num_steps)
			+ String.format(" ,%d", s_num_blocks)
			+ String.format(" ,%d", s_segment)
			+ String.format(" ,%d", GaugeLevel)
			+ String.format(" ,%d\n",Voltage );


			if(!bDataHeader)
			{
				String HeaderData;
				HeaderData = "MaskName,DateTime,SessionID,RRITime,iHrData,NoOfRRI,iSignalQuality,iHrvData[0],iHrvData[1],iHrvData[2],iHrvData[3],iHrvData[4],num_program,type_block,num_steps,num_blocks,segment, gauge, voltage\n";

				bDataHeader = true;
				
				try 
				{
					mDataFileWriter.append(HeaderData);
				}
				catch (Exception e) {
					Log.e(TAG, e.toString());
				}
			}

			try 
			{
				mDataFileWriter.append(TimeStampedData);
			}
			catch (Exception e) {
				Log.e(TAG, e.toString());
			}

			Log.e(LOG_TAG, "Output record is "+String.format("%d, ", mHRSampleNumber) +TimeStampedData);
			mHRSampleNumber++;
		}
		else 
		{
			if(!bGotDeviceName)
				Log.e(LOG_TAG,  "!bGotDeviceName");
			if(mDataFileWriter == null)
				Log.e(LOG_TAG,  "mDataFileWriter == null");
		}


		// We can clean this up when we know what Richard wants
		iRRi = iHrvData[0];

		UpdateStatusDisplayBlock();

		((TextView) findViewById(R.id.right_circle)).setText(String.format("%d", iRRi));

		if(iRRi - iFirstRRi > 0) DiffItem = "+"; else DiffItem = "";
		DiffItem = DiffItem + String.format(" %d", iRRi - iFirstRRi );
		((TextView) findViewById(R.id.diff_circle)).setText(DiffItem);

		currentDateTimeString = DateFormat.getTimeInstance().format(new Date());

		if(!BLE_started)
		{
			firstDateTimeString = currentDateTimeString;

			BLE_started = true;

			iFirstRRi = iRRi;

			HRVstg = "HRV"+ "-Original\r\n"+ firstDateTimeString;
			((TextView) findViewById(R.id.hrv_left_box)).setText(HRVstg);

			((TextView) findViewById(R.id.left_circle)).setText(String.format("%d", iRRi));

		}

		HRVstg = "HRV"+ "-Current\r\n"+ currentDateTimeString;
		((TextView) findViewById(R.id.hrv_right_box)).setText(HRVstg);


		// Scale SQ up to match HRV for graphing

		iSignalQuality = iSignalQuality * SQ_GRAPH_MULTIPLIER;

		// Update graphs:  HRV also has quality measure

		mLineSeriesHR.appendData(new DataPoint(mHRSampleNumber, iHrData), true, mHRSampleNumber+1);

		// Fix glitch in graphing
		// if(iHrvData <= 250) iHrvData = 250;

		for(nIdx=0; nIdx<5; nIdx++)
			if(iHrvData[nIdx] != 0)
			{
				mBarSeriesSQ.appendData(new DataPoint(mHRVSampleNumber, iSignalQuality), true, HRV_X_MAX);
				mLineSeriesHRV.appendData(new DataPoint(mHRVSampleNumber, iHrvData[nIdx]), true, HRV_X_MAX);

				mHRVSampleNumber++;
			}	

	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private void AddRecordToLogFile()
	{

		String TimeStampedData, HRVstg;
		String DiffItem;
		int iRRi;
		int nIdx;	
		
		Log.e(LOG_TAG, "AddRecordToLogFile");

		if(ScheduleCreateLG)
		{
			Log.e(LOG_TAG, "*** DOING createDataFile of LG ");
			createDataFile(getApplicationContext(), CF_LG_TYPE, DeviceSessionNumber);
			ScheduleCreateLG =  false;
		}

		currentDateTimeString = DateFormat.getTimeInstance().format(new Date());

		if((mDataFileWriter != null) && bGotDeviceName)
		{

			// Create CSV string for output
			TimeStampedData = DeviceConnectionName + "," + currentDateTimeString + ","
			+ String.format(" %d", errorSessionID) + ","
			+ String.format(" %d", errorEventCode) + ","
			+ String.format(" %d", errorTimeSincePwrOn)  + ","
			+ String.format(" %d", errorData) + ","
			+ String.format(" %d", errorLineNum) + ","
			+ errorFuncName + "\n";

			if(!bLogHeader)
			{
				String HeaderData;
				HeaderData = "MaskName,DateTime,SessionID,ECode,Time,Data,LineNo,Function\n";

				bLogHeader = true;
				
				try 
				{
					mDataFileWriter.append(HeaderData);
				}
				catch (Exception e) {
					Log.e(TAG, e.toString());
				}
			}

			try 
			{
				mDataFileWriter.append(TimeStampedData);
			}
			catch (Exception e) {
				Log.e(TAG, e.toString());
			}

			Log.e(LOG_TAG, "Output record is "+String.format("%d, ", mHRSampleNumber) +TimeStampedData);
			mHRSampleNumber++;
		}
		else 
		{
			if(!bGotDeviceName)
				Log.e(LOG_TAG,  "!bGotDeviceName");
			if(mDataFileWriter == null)
				Log.e(LOG_TAG,  "mDataFileWriter == null");
		}
	
	}
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private void UpdateStatusDisplayBlock() {

		String HRitem;

		if(!bAllocatedHrvData)
		{
			iHrvData = new int[5];
			bAllocatedHrvData = true;
			 iHrvData[0] = 0;
		}
		
		HRitem = DeviceConnectionName + ": HRV = " + String.format(" %d", iHrvData[0]) + "   " + "HR = "
				+ String.format(" %d", iHrData) + "   " + "Q = " + String.format(" %d%%", iSignalQuality) + "\r\n"
				+ LightSoundLevelStg + BatteryStg;

		((TextView) findViewById(R.id.HRtext)).setText(HRitem);
	}

	private void ClearInfoDisplayBlocks() {
		// firmware version
		((TextView) findViewById(R.id.hrv_diff_box)).setText("");

		// Steps/Segments block
		((TextView) findViewById(R.id.pgm_stats)).setText("");

		// HRV block
		((TextView) findViewById(R.id.HRtext)).setText("");
	}

    private final BroadcastReceiver UARTStatusChangeReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

	final Intent mIntent = intent;



	currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
			
           //*********************//
            if (action.equals(UartService.ACTION_GATT_CONNECTED)) {
            	 runOnUiThread(new Runnable() {
                     public void run() {
						 if (connectionState != ConnectionState.CONNECTED && connectionState != ConnectionState.READY) {
							 Log.e("ConnectionState", "CONNECTED");
							 connectionState = ConnectionState.CONNECTED;

						 }
							 String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
							 Log.d(TAG, "UART_CONNECT_MSG");
                             btnConnectDisconnect.setText("Disconnect");
                             // edtMessage.setEnabled(true);
                             // btnSend.setEnabled(true);
                             
                             //((TextView) findViewById(R.id.deviceName)).setText(mDevice.getName()+ " - ready");
							 
                             // listAdapter.add("["+currentDateTimeString+"] Connected to: "+ mDevice.getName());
                        	 //	messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                        	 
                             mState = UART_PROFILE_CONNECTED;

                     }
            	 });
            }

            //*********************//
            if (action.equals(UartService.ACTION_GATT_DISCONNECTED)) {
            	 runOnUiThread(new Runnable() {
                     public void run() {
						 ClearInfoDisplayBlocks();
						 Log.e("ConnectionState", "DISCONNECTED");
						 connectionState = ConnectionState.DISCONNECTED;
						 String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                             Log.d(TAG, "UART_DISCONNECT_MSG");
                             btnConnectDisconnect.setText("Connect");
                             // edtMessage.setEnabled(false);
                             // btnSend.setEnabled(false);
                             //((TextView) findViewById(R.id.deviceName)).setText("Not Connected");
							 
                            // listAdapter.add("["+currentDateTimeString+"] Disconnected to: "+ mDevice.getName());
							 
                             mState = UART_PROFILE_DISCONNECTED;
                             mService.close();

				if(CSV_Filetype != 1)
				{
					// Don't close a SendData or SendLog file
					// until we get an HRVDONE or LOGDONE
					CloseDataFile(getApplicationContext());
				}

                            //setUiState();
                         
                     }
                 });
            }

			//*********************//
			if (action.equals(UartService.ACTION_GATT_READY)) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (connectionState != ConnectionState.READY) {
							Log.e("ConnectionState", "READY");
							connectionState = ConnectionState.READY;
							Connection_Number++;
							UpdateInfoDisplayBlock();
						}
					}
				});

				showMessage(context, "Ready to communicate with the Mask");

				// Garbage collector will destroy last ones

				//InitGraphs();
				
				 //***********  JLB 25 May 2020 ***//

				 if(ButtonsActivity.AutoSessionOn)
				{		 					
					OddMessage = "StartSession";
					showMessage(getApplicationContext(), "Doing Auto Session Start in 3 Seconds...");


					
					try 
					{
						Thread.sleep(3000);
					} 
					catch (InterruptedException e) 
					{
						e.printStackTrace();
					}

					ButtonsActivity.writeMessage(OddMessage);
				}
							
			}

			//*********************//
			if (action.equals(UartService.ACTION_GATT_RECONNECTING)) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Log.e("ConnectionState", "RECONNECTING");
						connectionState = ConnectionState.RECONNECTING;
						btnConnectDisconnect.setText("Connection lost. Tap to stop reconnection");
						ClearInfoDisplayBlocks();
					}
				});
				showMessage(context, "Connection lost, trying to reconnect ...");
			}

			//*********************//
            if (action.equals(UartService.ACTION_DATA_AVAILABLE)) {
              
                 final byte[] txValue = intent.getByteArrayExtra(UartService.EXTRA_DATA);

				 Log.e(LOG_TAG, "["+currentDateTimeString+"]"+"*** ACTION_DATA_AVAILABLE");
				 
                 runOnUiThread(new Runnable() {
                     public void run() {
                         try 
			{
						 	
                         	String text = new String(txValue, "UTF-8");
                         	//String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
				String HRVitem, HRitem, DiffItem;
				String HexData;
				int PacketType;
				int nJdx;

				SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd_HH:mm:ss");
				String currentDateTimeString = sdf.format(new Date());

				HexData = "";
				for(nJdx=0; nJdx < txValue.length; nJdx++)
				{
					HexData = HexData + String.format(" %02x", (txValue[nJdx] & 0xff));
				}

				Log.e(LOG_TAG, String.format("[%d]", HRVinputRecordNum) + "Input record is "+HexData);

				HRVinputRecordNum++;

				PacketType = txValue[0] & 0xff;

				Log.e(LOG_TAG, "PacketType is "+String.format("%d, ", PacketType));

				if(PacketType == PACKET_HRVDONE)
				{
					Log.e(LOG_TAG, "Got PACKET_HRVDONE, Closing file");
					CloseDataFile(getApplicationContext());
					bLogHeader = false;
					bDataHeader = false;
	               		 finalizeSendData();
				}
				else if(PacketType == PACKET_PACKET_HRVEND)
				{
					Log.e(LOG_TAG, "Got PACKET_HRVEND, Doing SendData to Firmware");

					if(NumRecsForThisSessionID == 0)
					{

						iHrData 			= 999;
						iSignalQuality 		= 0;
						NoOfRRI		= 0;
						RRITime		= 99999;


						if(!bAllocatedHrvData)
						{
							iHrvData = new int[5];
							bAllocatedHrvData = true;
						}

						iHrvData[0] 		= 0;
						iHrvData[1] 		= 0;
						iHrvData[2]		= 0;
						iHrvData[3] 		= 0;
						iHrvData[4] 		= 0;


						AddRecordToOutputFile();
					}

					if (sendDataParameters != null) 
					{
                        sendDataParameters.finishCurrentSession();
                    }

				}
				else if(PacketType == PACKET_SESSION_ID)
				{

					Log.e(LOG_TAG, "&&& Got PACKET_SESSION_ID");
					
					NumRecsForThisSessionID = 0;
					
					DeviceSessionNumber = ( txValue[4] & 0xff) << 24 | (txValue[3] & 0xff) << 16 | ( txValue[2] & 0xff) << 8 | (txValue[1] & 0xff);

					if( bGotDeviceName &&   (mDataFileWriter == null))
					{ 
						if(!DoingSendData && !DoingSendLog) createDataFile(getApplicationContext(), CF_ID_TYPE, DeviceSessionNumber);
					}
 

					Log.e(LOG_TAG, "[A]Device Name is "+ DeviceConnectionName);

					Log.e(LOG_TAG, "[A]DeviceSessionNumber is "+String.format("%d, ", DeviceSessionNumber));

					UpdateInfoDisplayBlock();
				 
				}
				else if(PacketType == PACKET_MASK_MODE)
				{

					Log.e(LOG_TAG, "&&& Got PACKET_MASK_MODE");
					
					DeviceMode = ( txValue[2] & 0xff) << 8 | (txValue[1] & 0xff);

					DeviceModeString = "";

					if((DeviceMode &  STORED_MODE_CHANGEABLE_MASK) == STORED_MODE_CHANGEABLE_MASK)
						DeviceModeString = DeviceModeString + "Changeable";
					else 
						DeviceModeString = DeviceModeString + "UnChangeable";

					if((DeviceMode &  STORED_REAL_OR_SHAM_MASK) == STORED_REAL_OR_SHAM_MASK)
						DeviceModeString = DeviceModeString + "," + "Real";
					else 
						DeviceModeString = DeviceModeString + "," + "Sham";

					if((DeviceMode &  STORED_APP_OR_NOAPP_MASK) == STORED_APP_OR_NOAPP_MASK)
						DeviceModeString = DeviceModeString + "," + "App";
					else 
					{
						String message = "SCToken";
						DeviceModeString = DeviceModeString + "," + "NoApp";
						ButtonsActivity.writeMessage(message);
					}
					
					if((DeviceMode &  STORED_STDBLE_OR_SECUREBLE_MASK) == STORED_STDBLE_OR_SECUREBLE_MASK)
						DeviceModeString = DeviceModeString + "," + "StdBLE";
					else 
						DeviceModeString = DeviceModeString + "," + "SecBLE";

					UpdateInfoDisplayBlock();
				 
				}
				else if(PacketType == PACKET_MASK_STATUS)
				{

					Log.e(LOG_TAG, "&&& Got PACKET_MASK_STATUS");
					
					DeviceStatus = ( txValue[4] & 0xff) << 24 | (txValue[3] & 0xff) << 16 | ( txValue[2] & 0xff) << 8 | (txValue[1] & 0xff);

					if(DeviceStatus == 0)
						DeviceMaskStatusString = "Status:PASS";
					else if(DeviceStatus == VALENCELL_ERROR)
						DeviceMaskStatusString = "Error:VALENCELL";
					else if(DeviceStatus == I2C_ERROR)
						DeviceMaskStatusString = "Error:I2C";
					else if(DeviceStatus == VS1000_ERROR)
						DeviceMaskStatusString = "Error:VS1000";
					else if(DeviceStatus == BATTERY_ERROR)
						DeviceMaskStatusString = "Error:BATTERY";
					else if(DeviceStatus == SPI_ERROR)
						DeviceMaskStatusString = "Error:SPI";
					else if(DeviceStatus == FLASH_ERROR)
						DeviceMaskStatusString = "Error:FLASH";
					else if(DeviceStatus == TEST_ERROR)
						DeviceMaskStatusString = "Error:TEST";

					UpdateInfoDisplayBlock();
				 
				}
				else if(PacketType == PACKET_SEND_VCFW_SIZE)
				{
					byte[] VCFW_Filelen = new byte[4];

					Log.e(LOG_TAG, "&&& Got PACKET_SEND_VCFW_SIZE");

					VCFW_Filelen[3] = (byte) ((BytesVCFWLen & 0xFF000000) >> 24);
					VCFW_Filelen[2] = (byte) ((BytesVCFWLen & 0x00FF0000) >> 16);
					VCFW_Filelen[1] = (byte) ((BytesVCFWLen & 0x0000FF00) >>  8);
					VCFW_Filelen[0] = (byte) (BytesVCFWLen & 0x000000FF);

					String FileLenStg =  String.format(" %d", BytesVCFWLen) ;

					showMessage(getApplicationContext(), "PACKET_SEND_VCFW_SIZE: VC FW File Len = " + FileLenStg);

					showMessage(getApplicationContext(),"Sending VC FW Len");

					if(mService != null) mService.writeRXCharacteristic(VCFW_Filelen);
				}
				else if(PacketType == PACKET_VCFW_ACK)
				{
					// We're going to send the file 16 bytes at a time, with checksum
					byte[] VCFW_Packet = new byte[17];
					int idx, offset, len, left;
					byte Xor = 0;
					int Percent;
					String Msg;

					Log.e(LOG_TAG, "&&& Got PACKET_VCFW_ACK");

					left = BytesVCFWLen - BytesVCFWSent;

					offset = BytesVCFWSent;

					if(left >= 16) len =16;
					else len = left;
					
					for(idx=0; idx<len; idx++)
					{
						VCFW_Packet[idx] = BytesVCFW[idx+offset];
						Xor ^= BytesVCFW[idx+offset];
					}

					BytesVCFWSent += len;

					// Always put checksum in last byte
					VCFW_Packet[16] = Xor;

					if(mService != null) mService.writeRXCharacteristic(VCFW_Packet);

					Percent = (100*BytesVCFWSent)/BytesVCFWLen;

					Msg = "VCFW Update:  Sent "+String.format("%d ", BytesVCFWSent)+" of "+String.format("%d", BytesVCFWLen)+" bytes, "+
						String.format("%d ", Percent)+"% Complete";

					((TextView) findViewById(R.id.HRtext)).setText(Msg);
					
				}
				if(PacketType == PACKET_CPS)
				{

				Log.e(LOG_TAG, "&&& Got PACKET_CPS");

					// In C, the structure is
					//
					// typedef struct
					// {
					//	uint8_t num_program;						//which program is running 0-sleep 1-Nap 2-Relax
					//	uint8_t num_steps;							//Current program Step 
					//	uint8_t type_block;							//Which type of block is running 0 =7.83hz, 1:3hz, 2=1hz
					//	uint8_t num_blocks;							//how many times to run a block
					//	uint8_t segment;								//how many times to run a block
				//} ProgramStatus_t;


					s_num_program		=   ( txValue[1] & 0xff);
					s_type_block		=   ( txValue[2] & 0xff);
					s_num_steps		=   ( txValue[3] & 0xff);
					s_num_blocks		=   ( txValue[4] & 0xff);
					s_segment			=   ( txValue[5] & 0xff);
				
				}
				// Let's do this later
				
				else if(PacketType == PACKET_STOPPING)
				{
				int StopCodeNum 		= ( txValue[1] & 0xff);
				StopCode = "SC="+String.format("%d",StopCodeNum);

				Log.e(LOG_TAG, "&&& Got PACKET_STOPPING");
				Log.e(LOG_TAG, StopCode);
				
				//	
				//	HRitem = "HRV = " + String.format(" %d", iHrvData[0]) + "   " + "HR = " 
				//		+ String.format(" %d", iHrData) + "   " + "Q = " + String.format(" %d%%", iSignalQuality) + "\r\n" +BatteryStg+" "+StopCode;
				//	((TextView) findViewById(R.id.HRtext)).setText(HRitem);
				//
				}
				else if(PacketType == PACKET_FIRMWARE_VERSION)
				{
					int nIdx;		

					Log.e(LOG_TAG, "&&& Got PACKET_FIRMWARE_VERSION");
					
					DeviceFirmwareVersion = "";
					for(nIdx=1; nIdx < txValue.length; nIdx++)					
						{						
						if(txValue[nIdx] == 0x00) break;						
						DeviceFirmwareVersion = DeviceFirmwareVersion + String.format("%c", txValue[nIdx]);
						}

					UpdateInfoDisplayBlock();

				}
				else if(PacketType == PACKET_VCFW_VERSION)
				{
					int nIdx;	

					Log.e(LOG_TAG, "&&& Got PACKET_VCFW_VERSION");
					
					VCFWFirmwareVersion = "";					
					for(nIdx=1; nIdx < txValue.length; nIdx++)					
						{						
						if(txValue[nIdx] == 0x00) break;						
						VCFWFirmwareVersion = VCFWFirmwareVersion + String.format("%c", txValue[nIdx]);					
						}

					UpdateInfoDisplayBlock();

				}

				else if(PacketType == PACKET_MASK_STAGE)
				{
					int nIdx;	

					Log.e(LOG_TAG, "&&& Got PACKET_MASK_STAGE");

		
					 s_num_program 	= ( txValue[1] & 0xff);						//which program is running 0-sleep 1-Nap 2-Relax
					 s_num_steps		= ( txValue[2] & 0xff);						//Current program Step 
					 s_type_block		= ( txValue[3] & 0xff);						//Which type of block is running 0 =7.83hz, 1:3hz, 2=1hz
					 s_num_blocks	= ( txValue[4] & 0xff);						//how many times to run a block
					 s_segment		= ( txValue[5] & 0xff);						//how many times to run a block

					UpdateInfoDisplayBlock();

				}
					
				else if(PacketType == PACKET_BATTERY_STATUS)
				{
					GaugeLevel 		= ( txValue[1] & 0xff);
					BatteryLow 		= ( txValue[2] & 0x01);
					ChargerConnected	= ( txValue[2] & 0x02) >> 1;
					Voltage			= ( txValue[4] & 0xff) << 8 | (txValue[3] & 0xff);

					Log.e(LOG_TAG, "&&& Got PACKET_BATTERY_STATUS");


					BatteryStg = "Battery: "+ String.format("%3d", GaugeLevel) + "%" + " " + String.format("%4d",Voltage )+ "mV";
					if(BatteryLow > 0) BatteryStg = BatteryStg + " LO"; else BatteryStg = BatteryStg + " OK";
					if(ChargerConnected > 0) BatteryStg = BatteryStg + " C"; else BatteryStg = BatteryStg + " U";

					Log.e(LOG_TAG, "Battery String is "+BatteryStg);

					UpdateStatusDisplayBlock();
			
				}
				else if(PacketType == PACKET_LIGHT_SOUND_LEVELS)
				{
					LightLevel 		= ( txValue[1] & 0xff);
					SoundLevel 		= ( txValue[2] & 0xff);

					if(SoundLevel >127) SoundLevel = -(256 - SoundLevel);

					Log.e(LOG_TAG, "&&& Got PACKET_LIGHT_SOUND_LEVELS");


					LightSoundLevelStg = "Lights: "+ String.format("%2d", LightLevel) + " " + "Sound: "+ String.format("%2d ", SoundLevel);
		
					Log.e(LOG_TAG, "LightSoundLevelStg is "+LightSoundLevelStg);

					UpdateStatusDisplayBlock();
			
				}
				else if(PacketType == PACKET_DEVICE_NAME)
				{
					int nIdx;

					Log.e(LOG_TAG, "&&& Got PACKET_DEVICE_NAME");

					DeviceConnectionName = "";
					for(nIdx=1; nIdx < txValue.length; nIdx++)
					{
						if(txValue[nIdx] == 0x00) break;
						DeviceConnectionName = DeviceConnectionName + String.format("%c", txValue[nIdx]);
					}
					
					bGotDeviceName = true;

					Log.e(LOG_TAG, "[B] Device Mask Name is "+ DeviceConnectionName);

					Log.e(LOG_TAG, "[B] DeviceSessionNumber is "+String.format("%d, ", DeviceSessionNumber));

					if((DeviceSessionNumber != -1) &&  (mDataFileWriter ==null))
					{ 
						if(!DoingSendData && !DoingSendLog) createDataFile(getApplicationContext(), CF_ID_TYPE, DeviceSessionNumber);
					}

					UpdateInfoDisplayBlock();
						
				}

				else if(PacketType == PACKET_ERROR_INFO)
				{
					int nIdx;

					Log.e(LOG_TAG, "&&& Got PACKET_ERROR_INFO");

					DeviceErrorInfo = "";
					for(nIdx=1; nIdx < txValue.length; nIdx++)
					{
						if(txValue[nIdx] == 0x00) break;
						DeviceErrorInfo = DeviceErrorInfo + String.format("%c", txValue[nIdx]);
					}
					
					bGotDeviceName = true;

					Log.e(LOG_TAG, "[B] PACKET_ERROR_INFO is "+ DeviceErrorInfo);

					UpdateInfoDisplayBlock();
				}

				else if(PacketType == PACKET_EVENT_ONE)
				{
					Log.e(LOG_TAG, "&&& Got PACKET_EVENT_ONE");


					// Extract Values
					// In C, the structure is
					//
					//	typedef struct __attribute__((packed))
					//	{
					//		uint32_t  errorEventCode;
					//		uint32_t  timeSincePwrOn;					// Number of msec since powerup
					//		uint32_t  data;								// Data to be recorded with the error.    
					//		uint16_t  sessionID;	
					//		uint16_t  lineNum;
					//		uint8_t   funcName[ERR_MAX_FILE_NAME_SIZE]; 
					//	} EventErrorLogEntry;

					errorEventCode 		= ( txValue[ 4] & 0xff) << 24 | (txValue[ 3] & 0xff) << 16 | ( txValue[ 2] & 0xff) << 8 | (txValue[1] & 0xff);
					errorTimeSincePwrOn 	= ( txValue[ 8] & 0xff) << 24 | (txValue[ 7] & 0xff) << 16 | ( txValue[ 6] & 0xff) << 8 | (txValue[5] & 0xff);
					errorData 			= ( txValue[12] & 0xff) << 24 | (txValue[11] & 0xff) << 16 | ( txValue[10] & 0xff) << 8 | (txValue[9] & 0xff);
					errorSessionID 		= ( txValue[14] & 0xff) << 8 | (txValue[13] & 0xff);
					errorLineNum 			= ( txValue[16] & 0xff) << 8 | (txValue[15] & 0xff);
	
	                      }

				else if(PacketType == PACKET_EVENT_TWO)
				{

					int nIdx;

					Log.e(LOG_TAG, "&&& Got PACKET_EVENT_TWO");


					// Extract Values
					// In C, the structure is
					//
					//	typedef struct __attribute__((packed))
					//	{
					//		uint32_t  errorEventCode;
					//		uint32_t  timeSincePwrOn;					// Number of msec since powerup
					//		uint32_t  data;								// Data to be recorded with the error.    
					//		uint32_t  sessionID;	
					//		uint32_t  lineNum;
					//		uint8_t   funcName[ERR_MAX_FILE_NAME_SIZE]; 
					//	} EventErrorLogEntry;

					errorFuncName = "";
					for(nIdx=1; nIdx < txValue.length; nIdx++)
					{
						if(txValue[nIdx] == 0x00) break;
						errorFuncName = errorFuncName + String.format("%c", txValue[nIdx]);
					}

					AddRecordToLogFile();

	                      }
				
				else if(PacketType == PACKET_HRVDATA)
				{
					Log.e(LOG_TAG, "&&& Got PACKET_HRVDATA");

					NumRecsForThisSessionID++;

					// Extract Values
					// In C, the structure is
					//
					//typedef struct
					//	{
					//		uint16_t hr_data;		//heartrate
					//		uint8_t sig_qual;		//heartrate signal quality
					//		uint8_t   NoOfRRI;
					//		uint16_t  RRITime;
					//		uint16_t  RRIInterval[5];
					//	} BLE_packet_t;

					iHrData 		= ( txValue[2] & 0xff) << 8 | (txValue[1] & 0xff);
					iSignalQuality 	= ( txValue[3] & 0xff);
					NoOfRRI		= ( txValue[4] & 0xff);
					RRITime		= ( txValue[6] & 0xff) << 8 | (txValue[5] & 0xff);


					if(!bAllocatedHrvData)
					{
						iHrvData = new int[5];
						bAllocatedHrvData = true;
					}

					iHrvData[0] 		= ( txValue[ 8] & 0xff) << 8 | (txValue[7] & 0xff);
					iHrvData[1] 		= ( txValue[10] & 0xff) << 8 | (txValue[9] & 0xff);
					iHrvData[2]		= ( txValue[12] & 0xff) << 8 | (txValue[11] & 0xff);
					iHrvData[3] 		= ( txValue[14] & 0xff) << 8 | (txValue[13] & 0xff);
					iHrvData[4] 		= ( txValue[16] & 0xff) << 8 | (txValue[15] & 0xff);


					AddRecordToOutputFile();

	                         }
                        	
                         } catch (Exception e) {
                             Log.e(TAG, e.toString());
                         }
                     }
                 });
             }
           //*********************//
            if (action.equals(UartService.DEVICE_DOES_NOT_SUPPORT_UART)){
            	showMessage(getApplicationContext(),"Device doesn't support UART. Disconnecting");
            	mService.disconnect();
            }
            
            
        }
    };

    private void service_init() {
        Intent bindIntent = new Intent(this, UartService.class);
        bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
  
        LocalBroadcastManager.getInstance(this).registerReceiver(UARTStatusChangeReceiver, makeGattUpdateIntentFilter());
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UartService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_READY);
        intentFilter.addAction(UartService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(UartService.DEVICE_DOES_NOT_SUPPORT_UART);
		intentFilter.addAction(UartService.ACTION_GATT_RECONNECTING);
        return intentFilter;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroy() {
    	 super.onDestroy();
        Log.d(TAG, "onDestroy()");
		
	CloseDataFile(getApplicationContext());

        try {
        	LocalBroadcastManager.getInstance(this).unregisterReceiver(UARTStatusChangeReceiver);
        } catch (Exception ignore) {
            Log.e(TAG, ignore.toString());
        } 
        unbindService(mServiceConnection);
        mService.stopSelf();
        mService= null;
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        if (!mBtAdapter.isEnabled()) {
            Log.i(TAG, "onResume - BT not enabled yet");
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
 
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

	///////////////////////////////////////////////

public static String getPath(Context context, Uri uri) {

 if ("content".equalsIgnoreCase(uri.getScheme())) {
        return getDataColumn(context, uri, null, null);
    }
    // File
    else if ("file".equalsIgnoreCase(uri.getScheme())) {
        return uri.getPath();
    }
    return null;
}

public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
    Cursor cursor = null;
    final String column = "_data";
    final String[] projection = { column };
    try {
        cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
        if (cursor != null && cursor.moveToFirst()) {
            final int index = cursor.getColumnIndexOrThrow(column);
            return cursor.getString(index);
        }
    } finally {
        if (cursor != null)
            cursor.close();
    }
    return null;
}


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

	//super.onActivityResult(requestCode, resultCode, data);
	String filename;
	
        switch (requestCode) {

	case REQUEST_FILE_SELECT:
	{
		if (resultCode == Activity.RESULT_OK) 
		{
			//ToastIt( "REQUEST_FILE_SELECT OK ");
			//Log.d(TAG, "REQUEST_FILE_SELECT OK ");

			try 
			{
				Uri uri = data.getData();

				String path = getPath(this, uri);
				if (path == null) 
				{
					// filename = FilenameUtils.getName(uri.toString());
					// ToastIt( "path == null ");
				} 
				else 
				{
					File file = new File(path);
					filename = file.getName();

					showMessage(getApplicationContext(), "Firmware File  = " + path );
					
					BytesVCFWLen = (int) file.length();
					BytesVCFW = new byte[BytesVCFWLen];

					String FileLenStg =  String.format(" %d", BytesVCFWLen) ;

					showMessage(getApplicationContext(), "VC FW File Len = " + FileLenStg);

					FileInputStream fis = new FileInputStream(file);
					fis.read(BytesVCFW); //read file into bytes[]
				}
			}	
			catch (Exception e) 
			{
				e.printStackTrace();
			}

			String message = "SendVCFW";
			byte[] value;
			try 
			{
				// send data to service
				value = message.getBytes("UTF-8");
				showMessage(getApplicationContext(),"Sending VC FW Update");

				 if(mService != null) mService.writeRXCharacteristic(value);
				 else showMessage(getApplicationContext(),"Please Connect To Update VC Firmware");
			} 
		        catch (IOException e) {
		            Log.e(TAG, "Can not read file: " + e.toString());
		        }
		
		} 
		else 
		{
			showMessage(getApplicationContext(),"REQUEST_FILE_SELECT Failed ");
		}
	}
	break;
	
        case REQUEST_SELECT_DEVICE:
        	//When the DeviceListActivity return, with the selected device address
        		{

					Log.d(TAG, "************ REQUEST_SELECT_DEVICE");
        		}
			
            if (resultCode == Activity.RESULT_OK && data != null) {
                String deviceAddress = data.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
                mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);
               
                Log.d(TAG, "... onActivityResultdevice.address==" + mDevice + "mserviceValue" + mService);
               // ((TextView) findViewById(R.id.deviceName)).setText(mDevice.getName()+ " - connecting");
                mService.connect(deviceAddress);
            }
            break;

	case REQUEST_ACTIVITY:
		Log.d(TAG, "*** Case REQUEST_ACTIVITY hit");
		break;
		
        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                showMessage(getApplicationContext(),"Bluetooth has turned on ");

            } else {
                // User did not enable Bluetooth or an error occurred
                Log.d(TAG, "BT not enabled");
                showMessage(getApplicationContext(),"Problem in BT Turning ON ");
                finish();
            }
            break;
        default:
            Log.e(TAG, "wrong request code");
            break;
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
       
    }

    
    private static void showMessage(Context context,String msg) 
	{
	Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
	}

    @Override
    public void onBackPressed() {
        if (mState == UART_PROFILE_CONNECTED) {
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
            showMessage(getApplicationContext(),"muConnect is running in background.\n             Disconnect to exit");
        }
        else {
            new AlertDialog.Builder(this)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle(R.string.popup_title)
            .setMessage(R.string.popup_message)
            .setPositiveButton(R.string.popup_yes, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
   	                finish();
                }
            })
            .setNegativeButton(R.string.popup_no, null)
            .show();
        }
    }

    public static boolean isFileInDownloadManager(File file, Context context) {
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterByStatus(DownloadManager.STATUS_SUCCESSFUL);
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        Cursor cursor = downloadManager.query(query);
        if (cursor.moveToFirst()) {
            String fileUri = Uri.fromFile(file).toString();
            do {
                String testUri = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                if (testUri.equals(fileUri)) {
                    return true;
                }
            } while (cursor.moveToNext());
        }

        return false;
    }

    public static void addFileToDownloadManager(File file, String title, String description, String mimeType, Context context) {
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        if (downloadManager != null) {
            if (file.exists() && !isFileInDownloadManager(file, context)) {
                downloadManager.addCompletedDownload(
                        title,
                        description,
                        true,
                        mimeType,
                        file.getAbsolutePath(),
                        file.length(),
                        false);
            }
        }
    }
}

package jp.ksksue.app.terminal;

import jp.ksksue.driver.serial.FTDriver;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class AndroidSerialTerminal extends Activity {
	
	private static final int MENU_ID_SETTING = 0;
	private static final int REQUEST_PREFERENCE = 0;

	// Defines of Display Settings
	private static final int DISP_CHAR	= 0;
	private static final int DISP_DEC	= 1;
	private static final int DISP_HEX	= 2;
	
	// Linefeed Code Settings
	private static final int LINEFEED_CODE_CR		= 0;
	private static final int LINEFEED_CODE_CRLF	= 1;
	private static final int LINEFEED_CODE_LF		= 2;
	
	FTDriver mSerial;

	private TextView mTvSerial;
	private String mText;
	private boolean mStop=false;
	private boolean mStopped=true;
	
	String TAG = "AndroidSerialTerminal";
    
    Handler mHandler = new Handler();

    private Button btWrite;
    private EditText etWrite;
    
    private int mDisplayType=DISP_CHAR;
    private int mLinefeedCode=LINEFEED_CODE_CRLF;
    private int mBaudrate=FTDriver.BAUD9600;

    private static final String ACTION_USB_PERMISSION =
        "jp.ksksue.app.terminal.USB_PERMISSION";
    
    // Linefeed
    private final static String BR = System.getProperty("line.separator");
    
	// debug settings
	final boolean SHOW_LOGCAT = false;
	final boolean USE_WRITE_BUTTON_FOR_DEBUG = false;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mTvSerial = (TextView) findViewById(R.id.tvSerial);
        btWrite = (Button) findViewById(R.id.btWrite);
        etWrite = (EditText) findViewById(R.id.etWrite);
        
        // get service
        mSerial = new FTDriver((UsbManager)getSystemService(Context.USB_SERVICE));
          
        // listen for new devices
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(mUsbReceiver, filter);
        
        // load default baud rate
        mBaudrate = loadDefaultBaudrate();
        
        // for requesting permission
        // setPermissionIntent() before begin()
        PendingIntent permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        mSerial.setPermissionIntent(permissionIntent);
        
        if(mSerial.begin(mBaudrate)) {
        	mainloop();
        }
        
        // ---------------------------------------------------------------------------------------
       // Write Button
        // ---------------------------------------------------------------------------------------
		if (!USE_WRITE_BUTTON_FOR_DEBUG) {
			btWrite.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					String strWrite = etWrite.getText().toString();
					mSerial.write(strWrite.getBytes(), strWrite.length());
				}
			});
		} else {
			// Write test button for debug
			btWrite.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					String strWrite = "";
					for (int i = 0; i < 3000; ++i) {
						strWrite = strWrite + " " + Integer.toString(i);
					}
					mSerial.write(strWrite.getBytes(), strWrite.length());
				}
			});
		} // end of if(SHOW_WRITE_TEST_BUTTON)
    }
    
    // ---------------------------------------------------------------------------------------
    // Menu Button
    // ---------------------------------------------------------------------------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, MENU_ID_SETTING, Menu.NONE, "Setting");
        return super.onCreateOptionsMenu(menu);
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ID_SETTING :
			startActivityForResult(new Intent().setClassName(this.getPackageName(),
					AndroidSerialTerminalPrefActivity.class.getName()),REQUEST_PREFERENCE);
			return true;
		default :
			return false;
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    if (requestCode == REQUEST_PREFERENCE) {

	        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
	        
	        String res = pref.getString("display_list", Integer.toString(DISP_CHAR));
	        mDisplayType = Integer.valueOf(res);
	        
	        res = pref.getString("linefeedcode_list", Integer.toString(LINEFEED_CODE_CRLF));
	        mLinefeedCode = Integer.valueOf(res);
	        //TODO: get values of settings(data bits and parity, stop bits, flow control, break)
	        
	        // reset baudrate
	        res = pref.getString("baudrate_list", Integer.toString(FTDriver.BAUD9600));
	        if(mBaudrate != Integer.valueOf(res)) {
	        	mBaudrate = Integer.valueOf(res);
	        	mSerial.setBaudrate(mBaudrate, 0);
	        }
	    }
	}
    // ---------------------------------------------------------------------------------------
	
    @Override
    public void onDestroy() {
		mSerial.end();
		mStop=true;
       unregisterReceiver(mUsbReceiver);
		super.onDestroy();
    }
        
	private void mainloop() {
		new Thread(mLoop).start();
	}
	
	private Runnable mLoop = new Runnable() {
		@Override
		public void run() {
			int i;
			int len;
			byte[] rbuf = new byte[4096];
						
			for(;;){//this is the main loop for transferring
				
				//////////////////////////////////////////////////////////
				// Read and Display to Terminal
				//////////////////////////////////////////////////////////
				len = mSerial.read(rbuf);

				// TODO: UI:Show last line
				if(len > 0) {
					if(SHOW_LOGCAT) { Log.i(TAG,"Read  Length : "+len); }
					mText = (String) mTvSerial.getText();
					for(i=0;i<len;++i) {
						if(SHOW_LOGCAT) { Log.i(TAG,"Read  Data["+i+"] : "+rbuf[i]); }
						// TODO: change the output type from UI
						switch(mDisplayType) {
						case DISP_CHAR : 
							// "\r":CR(0x0D) "\n":LF(0x0A)
							if((mLinefeedCode==LINEFEED_CODE_CRLF) && (rbuf[i] == 0x0D) && (rbuf[i+1] == 0x0A)) {
								mText = mText + BR;
								++i;
							} else	if ((mLinefeedCode == LINEFEED_CODE_CR) && (rbuf[i] == 0x0D)) {
								mText = mText + BR;
							} else if ((mLinefeedCode == LINEFEED_CODE_LF) && (rbuf[i] == 0x0A)) {
								mText = mText + BR;
							} else {
								mText = mText + "" +(char)rbuf[i];
							}
							break;
						case DISP_DEC :
							if((mLinefeedCode==LINEFEED_CODE_CRLF) && (rbuf[i] == 0x0D) && (rbuf[i+1] == 0x0A)) {
								mText = mText + " " + Byte.toString(rbuf[i]) + BR;
								++i;
							} else if ((mLinefeedCode == LINEFEED_CODE_CR) && (rbuf[i] == 0x0D)) {
								mText = mText + " " + Byte.toString(rbuf[i]) + BR;
							} else if ((mLinefeedCode == LINEFEED_CODE_LF) && (rbuf[i] == 0x0A)) {
								mText = mText + " " + Byte.toString(rbuf[i]) + BR;
							} else {
								mText = mText + " " + Byte.toString(rbuf[i]);
							}							
							break;
						case DISP_HEX :
							if((mLinefeedCode==LINEFEED_CODE_CRLF) && (rbuf[i] == 0x0D) && (rbuf[i+1] == 0x0A)) {
								mText = mText + " " + Integer.toHexString((int) rbuf[i]) + BR;
								++i;
							} else	if ((mLinefeedCode == LINEFEED_CODE_CR) && (rbuf[i] == 0x0D)) {
								// TODO: output 2 length character (now not "0D", it's only "D".)
								mText = mText + " " + Integer.toHexString((int) rbuf[i]) + BR;
							} else if ((mLinefeedCode == LINEFEED_CODE_LF) && (rbuf[i] == 0x0A)) {
								mText = mText + " " + Integer.toHexString((int) rbuf[i]) + BR;
							} else {
								mText = mText + " "
										+ Integer.toHexString((int) rbuf[i]);
							}							
							break;
						}
					}

					mHandler.post(new Runnable() {
						public void run() {
							mTvSerial.setText(mText);
						}
					});
				}
				
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				if(mStop) {
					mStopped = true;
					return;
				}
			}
		}
	};
	
	// Load default baud rate
	int loadDefaultBaudrate() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String res = pref.getString("baudrate_list", Integer.toString(FTDriver.BAUD9600));
        return Integer.valueOf(res);
	}
	
    // BroadcastReceiver when insert/remove the device USB plug into/from a USB port  
    BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
    		String action = intent.getAction();
    		if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
    			mSerial.usbAttached(intent);
    			mBaudrate = loadDefaultBaudrate();
				mSerial.begin(mBaudrate);
    			mainloop();
				
    		} else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
    			mSerial.usbDetached(intent);
    			mSerial.end();
    			mStop=true;
    		}
        }
    };
}
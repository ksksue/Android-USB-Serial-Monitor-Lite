package jp.ksksue.app.terminal;

import jp.ksksue.driver.serial.FTDriver;
import android.app.Activity;
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

	final int SERIAL_BAUDRATE = FTDriver.BAUD115200;
	
	final boolean SHOW_LOGCAT = false;
	
	private static final int MENU_ID_SETTING = 0;
	private static final int REQUEST_PREFERENCE = 0;

	// Defines of Display Settings
	private static final int DISP_CHAR	= 0;
	private static final int DISP_DEC	= 1;
	private static final int DISP_HEX	= 2;
	
	FTDriver mSerial;

	private TextView mTvSerial;
	private String mText;
	private boolean mStop=false;
	private boolean mStopped=true;
		
	String TAG = "FTSampleTerminal";
    
    Handler mHandler = new Handler();

    private Button btWrite;
    private EditText etWrite;
    
    private int mDisplayType=DISP_CHAR;
    
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
        
        if(mSerial.begin(SERIAL_BAUDRATE)) {
        	mainloop();
        }
        
        // ---------------------------------------------------------------------------------------
       // Write Button
        // ---------------------------------------------------------------------------------------
        btWrite.setOnClickListener(new View.OnClickListener() {
    		@Override
    		public void onClick(View v) {
    			String strWrite = etWrite.getText().toString();
    			mSerial.write(strWrite.getBytes(),strWrite.length());
    		}
        });
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
						case 0 : 
							// "\r":CR(0x0D) "\n":LF(0x0A)
							if (rbuf[i] == 0x0D) {
								mText = mText + "\r";
							} else if (rbuf[i] == 0x0A) {
								mText = mText + "\n";
							} else {
								mText = mText + "" +(char)rbuf[i];
							}
							break;
						case 1 :
							if (rbuf[i] == 0x0D) {
								mText = mText + " " + Byte.toString(rbuf[i]) + "\r";
							} else if (rbuf[i] == 0x0A) {
								mText = mText + " " + Byte.toString(rbuf[i]) + "\n";
							} else {
								mText = mText + " " + Byte.toString(rbuf[i]);
							}							
							break;
						case 2 :
							if (rbuf[i] == 0x0D) {
								// TODO: output 2 length character (now not "0D", it's only "D".)
								mText = mText + " " + Integer.toHexString((int) rbuf[i]) + "\r";
							} else if (rbuf[i] == 0x0A) {
								mText = mText + " " + Integer.toHexString((int) rbuf[i]) + "\n";
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
	
    // BroadcastReceiver when insert/remove the device USB plug into/from a USB port  
    BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
    		String action = intent.getAction();
    		if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
    			mSerial.usbAttached(intent);
				mSerial.begin(SERIAL_BAUDRATE);
    			mainloop();
				
    		} else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
    			mSerial.usbDetached(intent);
    			mSerial.end();
    			mStop=true;
    		}
        }
    };
}
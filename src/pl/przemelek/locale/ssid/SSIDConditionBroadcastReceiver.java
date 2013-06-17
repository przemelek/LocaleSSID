/*
 * Created on 13-04-2012
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package pl.przemelek.locale.ssid;

import java.util.HashSet;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;

public class SSIDConditionBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
        String canSee = intent.getBundleExtra("com.twofortyfouram.locale.intent.extra.BUNDLE").getString("canSee");
        String cannotSee = intent.getBundleExtra("com.twofortyfouram.locale.intent.extra.BUNDLE").getString("cannotSee");		
        
        canSee = (canSee!=null)?canSee:"";
        cannotSee = (cannotSee!=null)?cannotSee:"";
            
        boolean result = true;
        
        
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        HashSet<String> ssids = new HashSet<String>();
        
        if (wifiManager!=null && wifiManager.getScanResults()!=null) {
	        for (ScanResult res:wifiManager.getScanResults()) {
			   ssids.add(res.SSID);
	        }
        }

        if (canSee.length()>0) {
	        for (String s:canSee.split("\\$")) {
	        	if (!ssids.contains(s)) {
	        		result = false;
	        		break;
	        	}
	        }
        }

        if (cannotSee.length()>0) {
	        for (String s:cannotSee.split("\\$")) {
	        	if (ssids.contains(s)) {
	        		result = false;
	        		break;
	        	}
	        }
        }
	        
        if (result) {
        	setResultCode(16);
        } else {
        	setResultCode(17);
        }
	}

}

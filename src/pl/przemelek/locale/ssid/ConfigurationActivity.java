/*
 * Created on 13-04-2012
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package pl.przemelek.locale.ssid;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class ConfigurationActivity extends Activity {
	
	private static class SSIDCondition {
		private boolean canSee;
		private String ssid;
		public SSIDCondition(boolean canSee, String ssid) {
			this.canSee=canSee;
			this.ssid=ssid;
		}
	}

	private final List<SSIDCondition> conditions = new ArrayList<ConfigurationActivity.SSIDCondition>();	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        String canSee = "";                
        String cannotSee = "";

        Bundle bundle = null;
        
        if (savedInstanceState!=null) {
        	bundle = savedInstanceState;
        } else if (getIntent().hasExtra("com.twofortyfouram.locale.intent.extra.BUNDLE")) {
        	bundle = getIntent().getBundleExtra("com.twofortyfouram.locale.intent.extra.BUNDLE");
        }
        
        if (bundle!=null) {
	        cannotSee = bundle.getString("cannotSee");
	        canSee = bundle.getString("canSee");        	
        }
        
        canSee=(canSee!=null)?canSee:"";
        cannotSee=(cannotSee!=null)?cannotSee:"";
        
        for (String ssid:canSee.split("\\$")) {
        	if (ssid.length()>0) {
        		conditions.add(new SSIDCondition(true, ssid));
        	}
        }
        for (String ssid:cannotSee.split("\\$")) {
        	if (ssid.length()>0) {
        		conditions.add(new SSIDCondition(false, ssid));	
        	}
        }
        
        final ListView listView = (ListView)findViewById(R.id.listView1);
        
        listView.setAdapter(new ArrayAdapter<SSIDCondition>(this, android.R.layout.simple_list_item_1) {
        	@Override
        	public View getView(int position, View convertView, ViewGroup parent) {
        		TextView view = (TextView)convertView;        		
        		if (view==null) {
        			view = new TextView(ConfigurationActivity.this);
        		}
        		SSIDCondition condition = conditions.get(position);
        		String visible = (condition.canSee)?"visible":"invisible";
        		view.setText(condition.ssid+" "+visible);
        		view.setTag(condition);
        		view.setHeight(35);
        		return view;
        	}
        });
        
        registerForContextMenu(listView);
        
        final ArrayAdapter<SSIDCondition> s = (ArrayAdapter<ConfigurationActivity.SSIDCondition>)listView.getAdapter();
        
        refreshListView(s);
        
        ((Button)findViewById(R.id.addButton)).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
		        WifiManager wifiManager = (WifiManager) ConfigurationActivity.this.getSystemService(Context.WIFI_SERVICE);

		        final List<String> ssids = new ArrayList<String>();
		        ssids.add("Type SSID of WiFi network for condition...");
		        if (wifiManager!=null && wifiManager.getScanResults()!=null) {
			        for (ScanResult res:wifiManager.getScanResults()) {
					   ssids.add(res.SSID);
			        }
		        }
		        AlertDialog.Builder builder = new AlertDialog.Builder(ConfigurationActivity.this);
		        String[] items = ssids.toArray(new String[0]);		        
		        builder.setItems(items, new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int which) {
		        	
	        			AlertDialog.Builder b = new AlertDialog.Builder(ConfigurationActivity.this);
	        			final EditText input = new EditText(ConfigurationActivity.this);
	        			LinearLayout view = new LinearLayout(ConfigurationActivity.this);
	        			view.setOrientation(LinearLayout.VERTICAL);
	        			view.addView(input);
	        			final CheckBox cb = new CheckBox(ConfigurationActivity.this);
	        			cb.setText("is visible");
	        			cb.setWidth(200);
	        			if (which!=0) {
	        				input.setText(ssids.get(which));
	        				cb.setChecked(true);
	        			}	        			
	        			view.addView(cb);
	        			b.setView(view);
	        			
	        			b.setPositiveButton("OK", new DialogInterface.OnClickListener() {								
							public void onClick(DialogInterface dialog, int which) {
								String ssid = input.getText().toString();
								if (ssid.length()>0) {
									final SSIDCondition condition = new SSIDCondition(cb.isChecked(), ssid);
									runOnUiThread(new Runnable() {
										public void run() {
											conditions.add(condition);
											refreshListView(s);
										}
									});
								}
							}
						});
	        			b.setNegativeButton("Cancel", null);
	        			b.show();
		        	}	
		        });
		        
		        builder.show();
				
			}
		});
        
        ((Button)findViewById(R.id.saveButton)).setOnClickListener(new View.OnClickListener() {			
			public void onClick(View v) {
				
				final Intent result = new Intent();
				final Bundle bundle = new Bundle();
				
				saveConditionsToBundle(bundle);
				
				result.putExtra("com.twofortyfouram.locale.intent.extra.BLURB", "Can see: ["+bundle.getString("canSee")+"] and cannot see ["+bundle.getString("cannotSee")+"]");
				result.putExtra("com.twofortyfouram.locale.intent.extra.BUNDLE", bundle);
				setResult(RESULT_OK, result);
				finish();
			}

		});
        
        ((Button)findViewById(R.id.cancelButton)).setOnClickListener(new View.OnClickListener() {			
			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				finish();
			}
		});   
	}

	private void refreshListView(final ArrayAdapter<SSIDCondition> s) {
		s.clear();
		for (SSIDCondition condition:conditions) {
        	s.add(condition);
        }
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) { 
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add("Delete condition");
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {		
		if ("Delete condition".equals(item.getTitle())) {
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
			conditions.remove((int)info.id);
			ListView listView = (ListView)findViewById(R.id.listView1);
			final ArrayAdapter<SSIDCondition> s = (ArrayAdapter<ConfigurationActivity.SSIDCondition>)listView.getAdapter();
			refreshListView(s);
		}
		return super.onMenuItemSelected(featureId, item);
	}
	
	private void saveConditionsToBundle(final Bundle bundle) {
		String canSee = "";
		String cannotSee = "";
		
		for (SSIDCondition condition:conditions) {
			if (condition.canSee) {
				canSee+=condition.ssid+"$";
			} else {
				cannotSee+=condition.ssid+"$";
			}
		}
		
		if (canSee.length()>1) canSee=canSee.substring(0,canSee.length()-1);
		if (cannotSee.length()>1) cannotSee=cannotSee.substring(0,cannotSee.length()-1);

		final String canSeeStr = canSee;
		bundle.putString("canSee", canSeeStr);
		final String cannotSeeStr = cannotSee;
		bundle.putString("cannotSee", cannotSeeStr);
	}
	
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		saveConditionsToBundle(outState);		
	}
}

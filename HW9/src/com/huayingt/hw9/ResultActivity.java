package com.huayingt.hw9;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher.ViewFactory;

import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.widget.WebDialog;

@SuppressLint("NewApi")
public class ResultActivity extends Activity {

	private ZillowData zillow = null;
	private ImageTask imageTask;
	private Bitmap[] bitmaps = new Bitmap[5]; 

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		zillow = new ZillowData();
		setContentView(R.layout.activity_result);
		imageTask = new ImageTask();
		imageTask.execute();

	}

	private void setupTab() {
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.addTab(actionBar.newTab().setText("BASIC INFO")
				.setTabListener(new TabListener(new ResultTable())));
		actionBar.addTab(actionBar.newTab().setText("HISTORICAL ZESTIMATES")
				.setTabListener(new TabListener(new ResultChart())));
	}

	public class TabListener implements ActionBar.TabListener {
		Fragment fragment;

		public TabListener(Fragment fragment) {
			this.fragment = fragment;
		}

		@Override
		public void onTabReselected(Tab tab, FragmentTransaction ft) {
		}

		@Override
		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			ft.replace(R.id.result_container, fragment);
		}

		@Override
		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
			ft.remove(fragment);
		}

	}

	@SuppressLint("UseSparseArrays")
	private void setReultTable() {

		Map<Integer, String> tableBind = new HashMap<Integer, String>();

		tableBind.put(R.id.propVal, nformat(zillow.useCode));
		tableBind.put(R.id.yrVal, nformat(zillow.yearBuilt));
		tableBind.put(R.id.lotsizeVal, nformat(zillow.lotSizeSqFt));
		tableBind.put(R.id.finish_areaVal, nformat(zillow.finishedSqFt));
		tableBind.put(R.id.bathVal, nformat(zillow.bathrooms));
		tableBind.put(R.id.bedVal, nformat(zillow.bedrooms));
		tableBind.put(R.id.taxyrVal, nformat(zillow.taxAssessmentYear));
		tableBind.put(R.id.taxVal, mformat(zillow.taxAssessment));
		tableBind.put(R.id.lastsoldpriceVal, mformat(zillow.lastSoldPrice));
		tableBind.put(R.id.lastsolddateVal, nformat(zillow.lastSoldDate));
		tableBind.put(R.id.zestimateVal, mformat(zillow.estimateAmount));
		tableBind.put(R.id.overall30Val, mformat(zillow.estimateValueChange));
		tableBind.put(R.id.zestimateDate,
				"Zestimate ® Property Estimate as of "
						+ zillow.estimateLastUpdate);
		tableBind.put(
				R.id.proprangeVal,
				rformat(zillow.estimateValuationRangeLow,
						zillow.esitmateValuationRangeHigh));
		tableBind.put(R.id.rentzestimateVal, mformat(zillow.resitmateAmount));
		tableBind.put(R.id.rent30Val, mformat(zillow.resitmateValueChange));
		tableBind.put(R.id.rentzestimateDate,
				"Rent Zestimate ® Rent Valuation as of "
						+ zillow.resitmateLastUpdate);
		tableBind.put(
				R.id.rentrangeVal,
				rformat(zillow.restimateValuationRangeLow,
						zillow.restimateValuationRangeHigh));

		Iterator<Integer> iterator = tableBind.keySet().iterator();
		while (iterator.hasNext()) {
			Integer key = (Integer) iterator.next();
			((TextView) findViewById(key)).setText((CharSequence) tableBind
					.get(key));
		}

		TextView link = (TextView) findViewById(R.id.linkVal);
		link.setText(Html.fromHtml("<a href='" + zillow.homedetails + "'>"
				+ zillow.addr + "</a>"));
		link.setMovementMethod(LinkMovementMethod.getInstance());

		
		if(zillow.restimateValueChangeSign.equals("+")){
			((TextView)findViewById(R.id.rent30Val)).setCompoundDrawablesWithIntrinsicBounds(new BitmapDrawable(getResources(),bitmaps[4]),null,null,null);
			//((ImageView)findViewById(R.id.rent30ValArrow)).setImageDrawable(new BitmapDrawable(getResources(),bitmaps[4]));
		}else if(zillow.restimateValueChangeSign.equals("-")){
			((TextView)findViewById(R.id.rent30Val)).setCompoundDrawablesWithIntrinsicBounds(new BitmapDrawable(getResources(),bitmaps[3]),null,null,null);
			//((ImageView)findViewById(R.id.rent30ValArrow)).setImageDrawable(new BitmapDrawable(getResources(),bitmaps[3]));
		}
		
		if(zillow.estimateValueChangeSign.equals("+")){
			((TextView)findViewById(R.id.overall30Val)).setCompoundDrawablesWithIntrinsicBounds(new BitmapDrawable(getResources(),bitmaps[4]),null,null,null);
			//((ImageView)findViewById(R.id.overall30ValArrow)).setImageDrawable(new BitmapDrawable(getResources(),bitmaps[4]));
		}else if(zillow.estimateValueChangeSign.equals("-")){
			((TextView)findViewById(R.id.overall30Val)).setCompoundDrawablesWithIntrinsicBounds(new BitmapDrawable(getResources(),bitmaps[3]),null,null,null);
			//((ImageView)findViewById(R.id.overall30ValArrow)).setImageDrawable(new BitmapDrawable(getResources(),bitmaps[3]));
		}
		
	}

	private void setFooter() {
		TextView footer = (TextView) findViewById(R.id.footerText);
		footer.setText(Html
				.fromHtml("© Zillow, Inc., 2006-2014.<br> Use is subject to <a href='http://www.zillow.com/corp/Terms.htm'>Terms of Use</a><br><a href='http://www.zillow.com/wikipages/What-is-a-Zestimate/'>What\'s a Zestimate?</a>"));
		footer.setMovementMethod(LinkMovementMethod.getInstance());

	}

	private String nformat(String s) {
		return (s.equals("")) ? "N/A" : s;
	}

	private String mformat(String s) {
		return (s.equals("")) ? "N/A" : "$" + s;
	}

	private String rformat(String s1, String s2) {
		if (s1.equals("") && s2.equals("")) {
			return "N/A";
		}
		return mformat(s1) + "-" + mformat(s2);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Session.getActiveSession().onActivityResult(this, requestCode,resultCode, data);
	}
	private void openFacebookSession(){
		Session.openActiveSession(this, true, new Session.StatusCallback() {
			@Override
			public void call(Session session, SessionState state, Exception exception) {
				if (session.isOpened()) {
					  Log.e("seesionKey",session.getAccessToken()); 
					  publishFeedDialog();
				}
				
			}
		    });
	}
	private void finishFeedDialog(Bundle values,FacebookException error) {
		if (error == null) {
            final String postId = values.getString("post_id");
            if (postId != null) {
                Toast.makeText(this,
                    "Posted story, id: "+postId,
                    Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this.getApplicationContext(), 
                    "Publish cancelled", 
                    Toast.LENGTH_SHORT).show();
            }
        } else if (error instanceof FacebookOperationCanceledException) {
            Toast.makeText(this.getApplicationContext(), 
                "Publish cancelled", 
                Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this.getApplicationContext(), 
                "Error posting story", 
                Toast.LENGTH_SHORT).show();
        }
	}
	private void publishFeedDialog() {
	    Bundle params = new Bundle();
	    params.putString("name", zillow.addr);
	    params.putString("caption", "Property information from Zillow.com");
	    params.putString("description", "Last Sold Price: "+ zillow.lastSoldPrice+
	    		", 30 Days Overall Change: "+zillow.estimateValueChangeSign+zillow.estimateValueChange);
	    params.putString("link", zillow.homedetails);
	    params.putString("picture", zillow.year1);

	    WebDialog feedDialog = (
	            new WebDialog.FeedDialogBuilder(this,Session.getActiveSession(),params)).setOnCompleteListener(new WebDialog.OnCompleteListener(){
					@Override
					public void onComplete(Bundle values,FacebookException error) {
						finishFeedDialog(values,error);
					}
	            }).build();
	    feedDialog.show();
	}
	private void shareAlert(){
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
		alertDialog.setTitle("Post to Facebook");
		alertDialog.setPositiveButton("Post Property Details",
		        new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialog, int which) {		            
			    		
			    		openFacebookSession();
		            }
		        });
		alertDialog.setNegativeButton("Cancel",
		        new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialog, int which) {
		                dialog.cancel();
		                cancelPost();
		            }
		        });
		alertDialog.show();
	}
	private void cancelPost(){
		Toast.makeText(getApplicationContext(), 
                "Publish cancelled", 
                Toast.LENGTH_SHORT).show();
	}
	public class ResultTable extends Fragment {
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View view = inflater.inflate(R.layout.result_table, container,
					false);		
			
			Button connect=(Button)view.findViewById(R.id.authButton);
		    connect.setOnClickListener(new OnClickListener() {	
		    	@Override
		    	public void onClick(View arg0) {
		    		if (Session.getActiveSession() != null) {
		    		    Session.getActiveSession().closeAndClearTokenInformation();
		    		}
		    		Session.setActiveSession(null);
		    		shareAlert();
		    		
		    	}
		        });
			return view;
		}
		
		@Override
		public void onViewCreated(View view, Bundle savedInstanceState) {
			super.onViewCreated(view, savedInstanceState);
			setReultTable();
			setFooter();
		}
	}

	public class ResultChart extends Fragment {

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View reusltChart = inflater.inflate(R.layout.result_chart,
					container, false);

			return reusltChart;
		}
	
		@Override
		public void onViewCreated(View view, Bundle savedInstanceState) {
			super.onViewCreated(view, savedInstanceState);

			imageTask.showResultChart();
			setFooter();

		}
	}

	private class ImageTask extends AsyncTask<String, Void, String> {
		ImageSwitcher imgSwitcher;
		TextSwitcher titleSwitcher;
		TextView addr;
		
		String[] titles = { "1 year", "5 years", "10 years" };
		int pos = 0;

		@Override
		protected String doInBackground(String... urls) {
			bitmaps[0] = downloadImage(zillow.year1);
			bitmaps[1] = downloadImage(zillow.year5);
			bitmaps[2] = downloadImage(zillow.year10);
			bitmaps[3] = downloadImage(zillow.imgn);
			bitmaps[4] = downloadImage(zillow.imgp);
			
			return null;
		}

		@Override
		protected void onPostExecute(String s) {

			/* setupTab until got images */
			setupTab();
		}

		private void showResultChart() {
			imgSwitcher = (ImageSwitcher) findViewById(R.id.imageSwitcher);
			titleSwitcher = (TextSwitcher) findViewById(R.id.chartTitle);
			addr = (TextView) findViewById(R.id.chartAddr);
			setImageSwitcher();
			setAddr();
			setTitleSwitcher();
			setBtn();
		}

		private void setImageSwitcher() {
			imgSwitcher.setFactory(new ViewFactory() {
				@Override
				public View makeView() {
					ImageView view = new ImageView(getApplicationContext());
					view.setScaleType(ImageView.ScaleType.FIT_START);
					view.setLayoutParams(new ImageSwitcher.LayoutParams(
							LayoutParams.MATCH_PARENT,
							LayoutParams.MATCH_PARENT));
					return view;
				}
			});
			imgSwitcher.setImageDrawable(new BitmapDrawable(bitmaps[0]));
		}

		private void setTitleSwitcher() {
			titleSwitcher.setFactory(new ViewFactory() {
				@Override
				public View makeView() {
					TextView view = new TextView(getApplicationContext());
					view.setGravity(Gravity.CENTER_HORIZONTAL);
					view.setTextSize(18);
					view.setTextColor(Color.BLACK);
					return view;
				}
			});
			titleSwitcher.setText("Historical Zestimate for the past "
					+ titles[0]);
		}

		private void setAddr() {
			addr.setText(zillow.addr);
		}

		private void setBtn() {

			Button next = (Button) findViewById(R.id.nextBtn);
			Button prev = (Button) findViewById(R.id.prevBtn);

			next.setOnClickListener(new Button.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					imgSwitcher.setImageDrawable(new BitmapDrawable(
							bitmaps[(++pos) % 3]));
					titleSwitcher.setText("Historical Zestimate for the past "
							+ titles[(pos) % 3]);
				}
			});
			prev.setOnClickListener(new Button.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					pos = (--pos) % 3 < 0 ? pos + 3 : pos;
					imgSwitcher.setImageDrawable(new BitmapDrawable(
							bitmaps[(pos) % 3]));
					titleSwitcher.setText("Historical Zestimate for the past "
							+ titles[(pos) % 3]);
				}
			});
		}

		private Bitmap downloadImage(String urlStr) {
			try {
				URL url = new URL(urlStr);
				InputStream in = (InputStream) url.getContent();
				return BitmapFactory.decodeStream(in);
			} catch (MalformedURLException e) {
				Log.e("err", e.toString(), e);
			} catch (IOException e) {
				Log.e("err", e.toString(), e);
			}
			return null;
		}
	}

	private class ZillowData {
		private JSONObject zillow_json = null;
		private JSONObject result = null;
		private JSONObject chart = null;

		public String homedetails = null;
		public String street = null;
		public String city = null;
		public String state = null;
		public String zipcode = null;
		public String addr = null;
		public String latitude = null;
		public String longitude = null;
		public String useCode = null;
		public String lastSoldPrice = null;
		public String yearBuilt = null;
		public String lastSoldDate = null;
		public String lotSizeSqFt = null;
		public String estimateLastUpdate = null;
		public String estimateAmount = null;
		public String finishedSqFt = null;
		public String estimateValueChangeSign = null;
		public String imgn = null;
		public String imgp = null;
		public String estimateValueChange = null;
		public String bathrooms = null;
		public String estimateValuationRangeLow = null;
		public String esitmateValuationRangeHigh = null;
		public String bedrooms = null;
		public String resitmateLastUpdate = null;
		public String resitmateAmount = null;
		public String taxAssessmentYear = null;
		public String restimateValueChangeSign = null;
		public String resitmateValueChange = null;
		public String taxAssessment = null;
		public String restimateValuationRangeLow = null;
		public String restimateValuationRangeHigh = null;

		public String year1 = null;
		public String year5 = null;
		public String year10 = null;

		private ZillowData() {
			Intent intent = getIntent();
			try {
				zillow_json = new JSONObject(intent.getStringExtra(MainActivity.ZILLOW_JSON));
				parseResult();
				parseChart();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		private void parseResult() {
			try {
				result = zillow_json.getJSONObject("result");
				homedetails = result.getString("homedetails");
				street = result.getString("street");
				city = result.getString("city");
				state = result.getString("state");
				zipcode = result.getString("zipcode");
				addr = street +", " + city + ", " + state + "-" + zipcode;

				latitude = result.getString("latitude");
				longitude = result.getString("longitude");
				useCode = result.getString("useCode");
				lastSoldPrice = result.getString("lastSoldPrice");
				yearBuilt = result.getString("yearBuilt");
				lastSoldDate = result.getString("lastSoldDate");
				lotSizeSqFt = result.getString("lotSizeSqFt");
				estimateLastUpdate = result.getString("estimateLastUpdate");
				estimateAmount = result.getString("estimateAmount");
				finishedSqFt = result.getString("finishedSqFt");
				estimateValueChangeSign = result
						.getString("estimateValueChangeSign");
				imgn = result.getString("imgn");
				imgp = result.getString("imgp");
				estimateValueChange = result.getString("estimateValueChange");
				bathrooms = result.getString("bathrooms");
				estimateValuationRangeLow = result
						.getString("estimateValuationRangeLow");
				esitmateValuationRangeHigh = result
						.getString("estimateValuationRangeHigh");
				resitmateLastUpdate = result.getString("restimateLastUpdate");
				resitmateAmount = result.getString("restimateAmount");
				taxAssessmentYear = result.getString("taxAssessmentYear");
				restimateValueChangeSign = result
						.getString("restimateValueChangeSign");
				resitmateValueChange = result.getString("restimtaeValueChange");
				taxAssessment = result.getString("taxAssessment");
				restimateValuationRangeLow = result
						.getString("restimateValuationRangeLow");
				restimateValuationRangeHigh = result
						.getString("restimateValuationRangeHigh");
				bedrooms = result.getString("bedrooms");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		private void parseChart() {
			try {
				chart = zillow_json.getJSONObject("chart");
				year1 = chart.getJSONObject("1year").getString("url");
				year5 = chart.getJSONObject("5years").getString("url");
				year10 = chart.getJSONObject("10years").getString("url");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}

package com.huayingt.hw9;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity {
	public final static String AWS_URL = "http://huaying.elasticbeanstalk.com/";
	public final static String ZILLOW_JSON = "ZillowJson";
	private EditText street;
	private EditText city;
	private Spinner state;
	private TextView errCity;
	private TextView errAddr;
	private TextView errState;
	private TextView errNotFound;
	private EmptyDetect streetED;
	private EmptyDetect cityED;
	private EmptyDetect stateED;
	private String streetInput;
	private String cityInput;
	private String stateInput;
	private ImageView logo;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		street = (EditText)findViewById(R.id.editAddr);
		city = (EditText)findViewById(R.id.editCity);
		state = (Spinner)findViewById(R.id.spinnerState);
		errCity = (TextView)findViewById(R.id.errCity);
		errAddr = (TextView)findViewById(R.id.errAddr);
		errState = (TextView)findViewById(R.id.errState);
		streetED = new EmptyDetect(street,errAddr);
		cityED = new EmptyDetect(city,errCity);
		stateED = new EmptyDetect(state,errState);
		errNotFound = (TextView)findViewById(R.id.errMatchNotFound);
		logo = (ImageView)findViewById(R.id.logo);
		
		logo.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_VIEW);
				intent.addCategory(Intent.CATEGORY_BROWSABLE);
				intent.setData(Uri.parse("http://www.zillow.com"));
				startActivity(intent);
				
			}
			
		});
	}

	public void onClick(View view) {
		
		streetInput = street.getText().toString();
		cityInput = city.getText().toString();
		stateInput = state.getSelectedItem().toString();
		
		if(inputValidate()){
			new ZillowTask(streetInput,cityInput,stateInput).execute();
		}
	}
	private class EmptyDetect implements TextWatcher,AdapterView.OnItemSelectedListener{
		private EditText edit;
		private TextView err;
		private Spinner select;
		private boolean mIsSpinnerFirstCall=true;

		EmptyDetect(EditText edit,TextView err){
			this.edit = edit;
			this.err = err;
		}
		EmptyDetect(Spinner select,TextView err){
			this.select = select;
			this.err = err;
		}
		@Override
		public void afterTextChanged(Editable s) {}
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {}
		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			if(edit.getText().toString().equals("")){
				err.setVisibility(View.VISIBLE);
			}else{
				err.setVisibility(View.INVISIBLE);
			}
		}
		
		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			if(!mIsSpinnerFirstCall){
				if(state.getSelectedItem().toString().equals("Choose State")){
					err.setVisibility(View.VISIBLE);
				}else{
					err.setVisibility(View.INVISIBLE);
				}
			}
			mIsSpinnerFirstCall = false;
		}
		@Override
		public void onNothingSelected(AdapterView<?> arg0) {}	
	}
	private boolean inputValidate(){
		
		boolean check = true;

		errAddr.setVisibility(View.INVISIBLE);
		errCity.setVisibility(View.INVISIBLE);
		errState.setVisibility(View.INVISIBLE);
		
		if(streetInput.equals("")){
			errAddr.setVisibility(View.VISIBLE);
			check = false;
		}
		if(cityInput.equals("")){
			errCity.setVisibility(View.VISIBLE);
			check = false;
		}
		if(stateInput.equals("Choose State")){
			errState.setVisibility(View.VISIBLE);
			check = false;
		}

		if(!check){
			street.addTextChangedListener(streetED);
			city.addTextChangedListener(cityED);
			state.setOnItemSelectedListener(stateED);
		}else{
			street.removeTextChangedListener(streetED);
			city.removeTextChangedListener(cityED);
		}

		return check;
	}

	private class ZillowTask extends AsyncTask<String, Void, String> {
		private String streetInput;
		private String cityInput;
		private String stateInput;
		
		ZillowTask(String streetInput, String cityInput, String stateInput){
			this.streetInput = streetInput;
			this.cityInput = cityInput;
			this.stateInput = stateInput;
		}
		@Override
		protected String doInBackground(String... arg0) {
			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(AWS_URL);
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			//params.add(new BasicNameValuePair("streetInput", "2636 Menlo Ave"));
			//params.add(new BasicNameValuePair("cityInput", "Los Angeles"));
			//params.add(new BasicNameValuePair("stateInput", "CA"));
			params.add(new BasicNameValuePair("streetInput", streetInput));
			params.add(new BasicNameValuePair("cityInput", cityInput));
			params.add(new BasicNameValuePair("stateInput", stateInput));

			Log.e("e",streetInput);
			Log.e("e",cityInput);
			Log.e("e",stateInput);
			try {
				post.setEntity(new UrlEncodedFormEntity(params));
				HttpResponse response = client.execute(post);
				return EntityUtils.toString(response.getEntity());

			} catch (UnsupportedEncodingException e1) {
			} catch (ClientProtocolException e) {
			} catch (IOException e) {
			}

			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			
			Log.e("e",result);
			if(result.equals("0")){
				errNotFound.setVisibility(View.VISIBLE);
			}else{
				errNotFound.setVisibility(View.INVISIBLE);
			
				Intent intent = new Intent(getApplicationContext(),ResultActivity.class);
				intent.putExtra(ZILLOW_JSON, result);
				startActivity(intent);
			
			}
		}
	}
}

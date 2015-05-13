package com.cooweather.activity;

import java.util.ArrayList;
import java.util.List;

import com.cooweather.db.CoolWeatherDB;
import com.cooweather.model.City;
import com.cooweather.model.County;
import com.cooweather.model.Province;
import com.cooweather.util.HTTPUtil;
import com.cooweather.util.HttpCallbackListener;
import com.cooweather.util.Utility;
import com.example.cooweather.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ChooseAreaActivity extends Activity {
	public static final int LEVEL_PROVINCE = 0;
	public static final int LEVEL_CITY = 1;
	public static final int LEVEL_COUNTY = 2;
	
	private ProgressDialog progressDialog ;
	private TextView titleText;
	private ListView listview;
	private ArrayAdapter<String> adapter;
	private CoolWeatherDB coolWeaherDB;
	private List<String> dataList = new ArrayList<String>();
	//省级列表
	private List<Province> provinceList;
	//市级列表
	private List<City> cityList;
	//县级列表
	private List<County> countyList;
	//选中的省份
	private Province selectedProvince;
	//选中的市级
	private City selectedCity;
	//选中的县
	private County selectedCounty;
	//当前选中的级别
	private int currentLevel;
	private boolean isFromWeatherActivity;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		isFromWeatherActivity = getIntent().getBooleanExtra("from_weather_activity", false);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_area);
		
		if(prefs.getBoolean("city_selected", false) && !isFromWeatherActivity){
			Intent intent = new Intent(this,WeatherActivity.class);
			startActivity(intent);
			finish();
			return ;
		}
		listview = (ListView) findViewById(R.id.list_view);
		titleText = (TextView) findViewById(R.id.title_text);
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,dataList);
		listview.setAdapter(adapter);
		coolWeaherDB = CoolWeatherDB.getInstance(this);
		listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int index,
					long arg3) {
				// TODO Auto-generated method stub
				if(currentLevel == LEVEL_PROVINCE){
					selectedProvince = provinceList.get(index);
					queryCities();
				}else if(currentLevel == LEVEL_CITY){
					selectedCity = cityList.get(index);
					queryCounties();
				}else if(currentLevel == LEVEL_COUNTY){
					String county_code = countyList.get(index).getCountyCode();
					Intent intent = new Intent(ChooseAreaActivity.this,WeatherActivity.class);
					intent.putExtra("county_code", county_code);
					startActivity(intent);
					finish();
				}
			}
			
		});
		queryProvinces();
	}
	//	查询全国所有的省，优先从数据库查找，如果没有在去服务器查找
	private void queryProvinces() {
		// TODO Auto-generated method stub
		provinceList = coolWeaherDB.loadProvince();
		if(provinceList.size() > 0){
			dataList.clear();
			for(Province province : provinceList){
				dataList.add(province.getProvinceName());
			}
			adapter.notifyDataSetChanged();
			listview.setSelection(0);
			titleText.setText("中国");
			currentLevel = LEVEL_PROVINCE;
		}else{
			queryFromServer(null, "province");
		}
	}
	//查询全国所有的市，优先从数据库查找，如果数据库没有在服务器查找
	private void queryCities(){
		cityList = coolWeaherDB.loadcity(selectedProvince.getId());
		if(cityList.size() > 0){
			dataList.clear();
			for(City city : cityList){
				dataList.add(city.getCityName());
			}
			adapter.notifyDataSetChanged();
			listview.setSelection(0);
			titleText.setText(selectedProvince.getProvinceName());
			currentLevel = LEVEL_CITY;
		}else{
			queryFromServer(selectedProvince.getProvinceCode(), "city");
		}
	}
	//查询全国所有的县，优先从数据库查找，如果数据库没有在服务器查找
	private void queryCounties(){
		countyList = coolWeaherDB.loadcounty(selectedCity.getId());
		if(countyList.size() > 0 ){
			dataList.clear();
			for(County county :countyList){
				dataList.add(county.getCountyName());
			}
			adapter.notifyDataSetChanged();
			listview.setSelection(0);
			titleText.setText(selectedCity.getCityName());
			currentLevel = LEVEL_COUNTY;
		}else{
			queryFromServer(selectedCity.getCityCode(), "county");
		}
	}
	//根据传入的代号和类型从服务器上潮汛省市县数据
	private void queryFromServer(final String code,final String type){
		String address;
		if(!TextUtils.isEmpty(code)){
			address = "http://www.weather.com.cn/data/list3/city"+code+".xml";
		}else{
			address = "http://www.weather.com.cn/data/list3/city.xml";
		}
		showProgressDialog();
		HTTPUtil.sendHttpRequest(address, new HttpCallbackListener() {
			
			@Override
			public void onFinish(String response) {
				// TODO Auto-generated method stub
				boolean result = false;
				if("province".equals(type)){
					result = Utility.handleProvincesResponse(coolWeaherDB, response);
				}else if("city".equals(type)){
					result = Utility.handleCitiesResponse(coolWeaherDB, response, selectedProvince.getId());
				}else if("county".equals(type)){
					result = Utility.handleCountyResponse(coolWeaherDB, response, selectedCity.getId());
				}
				if(result){
					//通过runonuithread 回到主线程处理逻辑
					runOnUiThread(new  Runnable() {
						public void run() {
							closeProgressDialog();
							if("province".equals(type)){
								queryProvinces();
							}else if("city".equals(type)){
								queryCities();
							}else if("county".equals(type)){
								queryCounties();
							}
						}
					});
				}
			}
			
			@Override
			public void onError(Exception e) {
				// TODO Auto-generated method stub
				//通过runonuithread 回到主线程处理逻辑
				runOnUiThread(new  Runnable() {
					public void run() {
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, "加载失败", Toast.LENGTH_LONG).show();
					}
				});
			}
		});
	}
	//显示进度条
	private void showProgressDialog() {
		// TODO Auto-generated method stub
		if(progressDialog == null){
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("正在加载....");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}
	//关闭进度条
	private void closeProgressDialog(){
		if(progressDialog != null){
			progressDialog.dismiss();
		}
	}
	//捕捉back按键，根据当前的级别来判断，此时应该返回省列表，市列表，县列表
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		if(currentLevel == LEVEL_COUNTY){
			queryCities();
		}else if(currentLevel == LEVEL_CITY){
			queryProvinces();
		}else{
			if(isFromWeatherActivity){
				Intent intent = new Intent(ChooseAreaActivity.this, WeatherActivity.class);
				startActivity(intent);
			}
			finish();
		}
	}
}

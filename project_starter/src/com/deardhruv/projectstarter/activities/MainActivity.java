
package com.deardhruv.projectstarter.activities;

import java.util.ArrayList;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;

import com.deardhruv.projectstarter.ProjectStarterApplication;
import com.deardhruv.projectstarter.R;
import com.deardhruv.projectstarter.abstracts.AbstractActivity;
import com.deardhruv.projectstarter.adapters.ImageItemDetailAdapter;
import com.deardhruv.projectstarter.events.ApiErrorEvent;
import com.deardhruv.projectstarter.events.ApiErrorWithMessageEvent;
import com.deardhruv.projectstarter.network.ApiClient;
import com.deardhruv.projectstarter.response.model.ImageListResponse;
import com.deardhruv.projectstarter.response.model.ImageResult;
import com.deardhruv.projectstarter.utils.Dumper;
import com.deardhruv.projectstarter.utils.Logger;

import de.greenrobot.event.EventBus;

public class MainActivity extends AbstractActivity implements OnClickListener, OnItemClickListener {

	private static final String LOGTAG = "MainActivity";
	private static final Logger LOG = new Logger(LOGTAG);

	private static final String IMAGE_LIST_REQUEST_TAG = LOGTAG + ".imageListRequest";

	private EventBus mEventBus;
	private ApiClient mApiClient;

	private Button btnReload, btnUploadFile;
	private ProgressDialog pd;
	private ListView listPhotos;

	private ArrayList<String> mImageUrls;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setProgressBarIndeterminateVisibility(true);

		setContentView(R.layout.main_activity_layout);

		initUI();

		mEventBus = EventBus.getDefault();
		ProjectStarterApplication app = ((ProjectStarterApplication) getApplication());
		mApiClient = app.getApiClient();

	}

	private void initUI() {
		btnReload = (Button) findViewById(R.id.btnReload);
		btnUploadFile = (Button) findViewById(R.id.btnUploadFile);
		listPhotos = (ListView) findViewById(R.id.listPhotos);

		initListener();
	}

	private void initListener() {
		btnReload.setOnClickListener(this);
		btnUploadFile.setOnClickListener(this);
		listPhotos.setOnItemClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		return id == R.id.action_settings || super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mEventBus.register(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		mEventBus.unregister(this);
	}

	private void loadImages() {
		showProgressDialog();
		mApiClient.getImageList(IMAGE_LIST_REQUEST_TAG);
	}

	private void showProgressDialog() {
		btnReload.setEnabled(false);
		pd = ProgressDialog.show(MainActivity.this, "Please wait", "getting images...");
		if (!pd.isShowing()) {
			pd.show();
		}
		setProgressBarIndeterminateVisibility(true);
	}

	private void dismissProgressDialog() {
		btnReload.setEnabled(true);
		if (pd.isShowing()) {
			pd.dismiss();
		}
		setProgressBarIndeterminateVisibility(false);
	}

	// ============================================================================================
	// User Clicks and Actions
	// ============================================================================================

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btnReload:
				loadImages();
				break;

			case R.id.btnUploadFile:
				final Intent intent = new Intent(MainActivity.this, UploadFileActivity.class);
				startActivity(intent);
				break;

			default:
				LOG.i("default case");
				break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

		final Intent intent = new Intent(MainActivity.this, PictureViewerActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putStringArrayListExtra(PictureViewerActivity.EXTRA_IMAGE_URLS, mImageUrls);
		intent.putExtra(PictureViewerActivity.EXTRA_IMAGE_SELECTION, position);
		startActivity(intent);
	}

	// ============================================================================================
	// EventBus callbacks
	// ============================================================================================

	/**
	 * Response of Image list.
	 * 
	 * @param imageListResponse ImageListResponse
	 */
	public void onEventMainThread(ImageListResponse imageListResponse) {
		switch (imageListResponse.getRequestTag()) {
			case IMAGE_LIST_REQUEST_TAG:
				dismissProgressDialog();
				ImageItemDetailAdapter adapter = new ImageItemDetailAdapter(MainActivity.this,
						imageListResponse.getData().getImageResultList());
				listPhotos.setAdapter(adapter);

				mImageUrls = new ArrayList<>();
				for (ImageResult imageResult : imageListResponse.getData().getImageResultList()) {
					mImageUrls.add(imageResult.getImg());
				}

				Dumper.dump(imageListResponse);
				break;

			default:
				break;
		}
	}

	/**
	 * EventBus listener. An API call failed. No error message was returned.
	 *
	 * @param event ApiErrorEvent
	 */
	public void onEventMainThread(ApiErrorEvent event) {
		switch (event.getRequestTag()) {
			case IMAGE_LIST_REQUEST_TAG:
				dismissProgressDialog();
				showToast(getString(R.string.error_server_problem));
				break;

			default:
				break;
		}
	}

	/**
	 * EventBus listener. An API call failed. An error message was returned.
	 *
	 * @param event ApiErrorWithMessageEvent Contains the error message.
	 */
	public void onEventMainThread(ApiErrorWithMessageEvent event) {
		switch (event.getRequestTag()) {
			case IMAGE_LIST_REQUEST_TAG:
				dismissProgressDialog();
				showToast(event.getResultMsgUser());
				break;

			default:
				break;
		}
	}

}

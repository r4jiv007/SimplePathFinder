package my.app.getroute.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.my.getroute.R;

public class MainActivity extends Activity {

	private EditText etOrigin, etDest;
	private Button bLoad;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main_activity);
		showStatusBar();
		initView();
	}

	private void hideStatusBar() {
		WindowManager.LayoutParams attrs = this.getWindow().getAttributes();
		attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
		this.getWindow().setAttributes(attrs);
	}

	private void showStatusBar() {
		WindowManager.LayoutParams attrs = this.getWindow().getAttributes();
		attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
		this.getWindow().setAttributes(attrs);
	}

	private void initView() {
		etOrigin = (EditText) findViewById(R.id.etOrigin);
		etDest = (EditText) findViewById(R.id.etDest);
		bLoad = (Button) findViewById(R.id.bLoad);
		bLoad.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent in = new Intent(MainActivity.this, MapActivity.class);
				in.putExtra("origin", etOrigin.getText().toString());
				in.putExtra("dest", etDest.getText().toString());
				startActivity(in);
			}
		});
	}
}

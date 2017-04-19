package org.wordpress.android.ui;

import android.app.Activity;
import android.os.Bundle;

import org.wordpress.android.R;

public class PermissionsRequestActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permissions_request);
    }
}

package com.granita;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.granita.tasks.R;

/**
 * Created by Daniel on 14/06/2015.
 */
public class installicloud extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.installicloud);
    }

    public void installredirect(View v)
    {
        String packageName = "com.granita.icloudcalsync";
        Intent intent = this.getPackageManager().getLaunchIntentForPackage(packageName);
        if (intent != null)
        {
	        /* we found the activity now start the activity */
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        else
        {
	        /* bring user to the market or let them choose an app? */
            intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setData(Uri.parse("market://details?id=" + packageName));
            startActivity(intent);
        }
    }


}

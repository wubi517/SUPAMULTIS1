package com.it_tech613.tvmulti.ui;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.lang.reflect.Array;
import java.util.Collection;


public class ExternalVlcActivity extends AppCompatActivity {
    public static final String TAG						= "VLC.IntentTest";
    private String Video_url = "";
    private String Video_title = "";
    private int  REQUEST_CODE = 42;
    private static final int  RESULT_PLAYBACK_ERROR = 3;

    private static final String 	PACKAGE_NAME		= "org.videolan.vlc";
    private static final String 	PLAYBACK_ACTIVITY	= "org.videolan.vlc.gui.video.VideoPlayerActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Video_url = getIntent().getStringExtra("video_url");

        Log.e("Url",Video_url);
        Video_title = getIntent().getStringExtra("video_title");

       MXPackageInfo packageInfo = getMXPackageInfo();
        if( packageInfo == null ){
            Toast.makeText(this,"Vlc not installed",Toast.LENGTH_SHORT).show();
            return;
        }


        Uri uri = Uri.parse(Video_url);
        Intent vlcIntent = new Intent(Intent.ACTION_VIEW);
        vlcIntent.setPackage("org.videolan.vlc");
        vlcIntent.setDataAndTypeAndNormalize(uri, "video/*");
        vlcIntent.putExtra("title", Video_title);
        vlcIntent.putExtra("from_start", false);
        vlcIntent.putExtra("position", 90000l);
        vlcIntent.setComponent(new ComponentName("org.videolan.vlc", "org.videolan.vlc.gui.video.VideoPlayerActivity"));
//        vlcIntent.putExtra("subtitles_location", "/sdcard/Movies/Fifty-Fifty.srt");
        startActivityForResult(vlcIntent, REQUEST_CODE);
    }

    private static class MXPackageInfo
    {
        final String packageName;
        final String activityName;

        MXPackageInfo( String packageName, String activityName ) {
            this.packageName = packageName;
            this.activityName = activityName;
        }
    }

    private static final MXPackageInfo[] PACKAGES = {
            new MXPackageInfo(PACKAGE_NAME, PLAYBACK_ACTIVITY)
    };

    /**
     * @return null if any MX Player packages not exist.
     */
    private MXPackageInfo getMXPackageInfo()
    {
        for( MXPackageInfo pkg: PACKAGES )
        {
            try
            {
                ApplicationInfo info = getPackageManager().getApplicationInfo(pkg.packageName, 0);
                if( info.enabled )
                    return pkg;
                else
                    Log.v( TAG, "MX Player package `" + pkg.packageName + "` is disabled." );
            }
            catch(PackageManager.NameNotFoundException ex)
            {
//                Utils.toaster(this,"VLC player does not exist.");
//                Log.v( TAG, "MX Player package `" + pkg.packageName + "` does not exist." );
            }
        }

        return null;
    }

    @Override
    protected void onActivityResult( int requestCode, int resultCode, Intent data )
    {
        if( requestCode == REQUEST_CODE )
        {
            switch( resultCode )
            {
                case RESULT_OK:
                    Log.i( TAG, "Ok: " + data );
                    break;

                case RESULT_CANCELED:
                    Log.i( TAG, "Canceled: " + data );
                    break;

                case RESULT_PLAYBACK_ERROR:
                    Log.e( TAG, "Error occurred: " + data );
                    break;

                default:
                    Log.w( TAG, "Undefined result code (" + resultCode  + "): " + data );
                    break;
            }

            if( data != null )
                dumpParams(data);

            /*
             * (YOUR CODE HERE) Handle result.
             */

            finish();
        }
        else
            super.onActivityResult(requestCode, resultCode, data);
    }
    private static void dumpParams( Intent intent )
    {
        StringBuilder sb = new StringBuilder();
        Bundle extras = intent.getExtras();

        sb.setLength(0);
        sb.append("* dat=").append(intent.getData());
        Log.v(TAG, sb.toString());

        sb.setLength(0);
        sb.append("* typ=").append(intent.getType());
        Log.v(TAG, sb.toString());

        if( extras != null && extras.size() > 0 )
        {
            sb.setLength(0);
            sb.append("    << Extra >>\n");

            int i = 0;
            for( String key : extras.keySet() )
            {
                sb.append( ' ' ).append( ++i ).append( ") " ).append( key ).append( '=' );
                appendDetails( sb, extras.get( key ) );
                sb.append( '\n' );
            }

            Log.v(TAG, sb.toString());
        }
    }

    private static void appendDetails( StringBuilder sb, Object object )
    {
        if( object != null && object.getClass().isArray() )
        {
            sb.append('[');

            int length = Array.getLength(object);
            for( int i = 0; i < length; ++i )
            {
                if( i > 0 )
                    sb.append(", ");

                appendDetails(sb, Array.get(object, i));
            }

            sb.append(']');
        }
        else if( object instanceof Collection)
        {
            sb.append('[');

            boolean first = true;
            for( Object element : (Collection)object )
            {
                if( first )
                    first = false;
                else
                    sb.append(", ");

                appendDetails(sb, element);
            }

            sb.append(']');
        }
        else
            sb.append(object);
    }
}

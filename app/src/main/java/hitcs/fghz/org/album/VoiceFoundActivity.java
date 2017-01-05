package hitcs.fghz.org.album;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.GridView;

import java.util.List;
import java.util.Map;

import hitcs.fghz.org.album.adapter.HorizontalScrollViewAdapter;
import hitcs.fghz.org.album.adapter.PhotoAdapter;
import hitcs.fghz.org.album.utils.ImagesScaner;

public class VoiceFoundActivity extends AppCompatActivity {

    private String voiceResult;
    private Context mContext;
    private GridView voiceGrid;
    private List<Map> mDatas;
    private Photos photos;
    private FragmentManager fManager;
    private FragmentTransaction fTransaction;
    private HorizontalScrollViewAdapter mAdapter;

    private static android.support.v7.app.ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_found);
        mContext = VoiceFoundActivity.this;
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        initResult();
        actionBar.setTitle(voiceResult);
        mDatas = ImagesScaner.getAlbumPhotos(mContext, voiceResult);
        fManager = getFragmentManager();
        fTransaction = fManager.beginTransaction();
        photos = new Photos(voiceResult);
        fTransaction.add(R.id.voice_content,photos);
        fTransaction.commit();
    }

    private void initResult() {
        Intent intent = getIntent();
        try {
            voiceResult = intent.getStringExtra("result");
        } catch (Exception e) {
            Log.d("ERROR: ", "" + e);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
        }
        return super.onOptionsItemSelected(item);
    }
}

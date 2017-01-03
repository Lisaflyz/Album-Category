package hitcs.fghz.org.album;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.tensorflow.demo.Classifier;
import org.tensorflow.demo.TensorFlowImageClassifier;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import hitcs.fghz.org.album.adapter.PhotoTypeAdapter;

import static hitcs.fghz.org.album.utils.ImagesScaner.getInformation;


/**
 * when you want to see more information about one image, this activity will be used
 * show image you are seeing
 * use tf to deal image and show tf info (should save the info into db, but i didn't do it)
 * show other information about image
 * Created by me on 16-12-31.
 */

public class PhotoInfoActivity extends Activity {

    // which image you are seeing
    private String url;
    // which image you are seeing
    private int position_now;
    // save tf info
    private List<Classifier.Recognition> results;
    // image info
    private Map<String, String> image_info;

    PhotoInfoActivity() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ui界面最上边的动作栏
        ActionBar actionBar = getActionBar();
        // 用于显示相应的属性
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle("相关信息");
        // 生成布局
        setContentView(R.layout.photo_info);
        // get intent information
        getMessage();
        // try to init the tf
        initTensorflow();
        // deal image by tf
        dealImage();
        // set image
        ImageView iv = (ImageView) findViewById(R.id.photo_target);
        iv.setImageURI(Uri.fromFile(new File(url)));
        // when tf work done, use this class to update UI
        new UpdateListView().execute();
    }
    /**
     * 生成动作栏上的菜单项目
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_about, menu);
        return super.onCreateOptionsMenu(menu);
    }
    /**
     * 监听菜单栏目的动作，当按下不同的按钮执行相应的动作
     * @param item
     * @return
     */
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        switch (item.getItemId()) {
            case android.R.id.home:
                // 返回
                this.finish();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    // if use intent to start this activity, you should use this method to get args
    // the args will be used to confirm which image should be showed
    protected void getMessage() {
        Intent intent = getIntent();
        try {
            position_now = intent.getIntExtra("position", -1);
            url = intent.getStringExtra("url");
        } catch (Exception e) {
            Log.d("ERROR: ", "" + e);
        }
        Log.d("Info: ", "" + position_now + " " + url);
    }
    // try to init the tf,
    private void initTensorflow() {
        // tf is a static value
        // if wasn't init  before, do this
        if (Config.classifier == null) {
            Config.classifier = new TensorFlowImageClassifier();
            try {
                Config.classifier.initializeTensorFlow(
                        getAssets(), Config.MODEL_FILE, Config.LABEL_FILE, Config.NUM_CLASSES, Config.INPUT_SIZE, Config.IMAGE_MEAN, Config.IMAGE_STD,
                        Config.INPUT_NAME, Config.OUTPUT_NAME);
            } catch (final IOException e) {
                ;
            }
        }
    }
    // deal image
    private void dealImage() {
        // get bitmap
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_4444;
        final Bitmap bitmap = BitmapFactory.decodeFile(url, options);
        // use tf to deal it in another thread
        new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void run() {
                do_tensorflow(bitmap);
            }
        }).start();

    }
    // use tf to deal image
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void do_tensorflow(Bitmap bitmap) {
        // resize
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float scaleWidth = ((float) Config.INPUT_SIZE) / width;
        float scaleHeight = ((float) Config.INPUT_SIZE) / height;
        Matrix matrix = new Matrix();

        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap newbm = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        // get results
        results = Config.classifier.recognizeImage(newbm);
        Log.d("Result", String.valueOf(results));
    }
    // update UI in this class
    class UpdateListView extends AsyncTask<String, String, String>
    {
        @Override
        protected String doInBackground(String... params) {
            try {
                // wait until work is done

                while (results == null) {
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(String result) {
            // TODO Auto-generated method stub
            if (results != null) {
                // init the listview
                ListView lv = (ListView) findViewById(R.id.photo_type_list);
                PhotoTypeAdapter adapter = new PhotoTypeAdapter(PhotoInfoActivity.this, R.layout.type_item, results);
                lv.setAdapter(adapter);
            }
        }
    }
}


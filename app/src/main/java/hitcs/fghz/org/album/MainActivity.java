package hitcs.fghz.org.album;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.NotificationManager;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.tensorflow.demo.Classifier;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import hitcs.fghz.org.album.dao.MyDatabaseHelper;


/**
 * 主活动
 * 进入相册的界面
 * Created by dongchang @ 20161218
 */

/**
 * 全部布局介绍：
 *
 * activity_main.xml: 主布局，进入app后的第一个布局，除了动作栏和选项卡之外，中间是一个fregment
 *
 * album_item: 相册的单个元素的定义
 *
 * fg_ablums: 相册的fregment定义
 *
 * fg_detail: 照片细节，即查看一张具体的照片
 *
 * fg_memory： 回忆的fregment定义
 *
 * fg_photos：显示全部照片的fregment
 *
 * gallery_item :照片细节中的下面缩略图单个的定义
 *
 * memory_item: 回忆的单个元素的定义
 *
 * photo_item: 照片fregment中的单个元素的定义
 */

/**
 * res 文件夹
 * drawable：
 * menu：上面动作栏的显示内容，如搜索等
 * mipmap：图标资源，如返回删除分享图标
 *
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    //UI Object
    private TextView txt_photos;
    private TextView txt_memory;
    private TextView txt_albums;

    //Fragment Object
    private Memory memory;
    private Photos photos;
    private Albums albums;
    private FragmentManager fManager;
    private static final int PERMISSION_REQUEST_STORAGE = 200;

    // for camera to save image
    private Uri contentUri;
    private File newFile;

    // actionBar
    public static android.support.v7.app.ActionBar actionBar;
    // fragment
    private FragmentTransaction fTransaction;

    // tensorflow
    // use static value in Config.java

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 生成布局
        setContentView(R.layout.activity_main);
        // ui界面最上边的动作栏
        actionBar = getSupportActionBar();
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        // fragment
        fManager = getFragmentManager();
        // 绑定事件
        bindViews();
        //模拟一次点击，既进去后选择第一项
        txt_photos.performClick();

    }
    /**
     * ActionBar
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        // search item
        MenuItem searchItem = menu.findItem(R.id.action_search);
        return super.onCreateOptionsMenu(menu);
    }
    /**
     * 监听菜单栏目的动作，当按下不同的按钮执行相应的动作
     * @param item
     * @return
     */
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // 返回
                System.out.println("title");
                getFragmentManager().popBackStack();
                actionBar.setDisplayHomeAsUpEnabled(false);
                actionBar.setTitle(" 相册");
                break;
            case R.id.action_search:
                // 搜索
               System.out.println("search");
                break;
            case R.id.action_camera:
                // 拍照
                if (Build.VERSION.SDK_INT >= 23) {
                    ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.CAMERA},
                            PERMISSION_REQUEST_STORAGE);
                }
                startCamera();
                break;
                // 语音
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 打开相机获取图片
     */
    private void startCamera() {
        File imagePath = new File(Environment.getExternalStorageDirectory(), "images");
        if (!imagePath.exists()) imagePath.mkdirs();
        newFile = new File(imagePath, "default_image.jpg");
        //第二参数是在manifest.xml定义 provider的authorities属性
        contentUri = FileProvider.getUriForFile(this, "hitcs.fghz.org.album.fileprovider", newFile);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //兼容版本处理，因为 intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION) 只在5.0以上的版本有效
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            ClipData clip = ClipData.newUri(getContentResolver(), "A photo", contentUri);
            intent.setClipData(clip);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        } else {
            List<ResolveInfo> resInfoList =
                    getPackageManager()
                            .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo resolveInfo : resInfoList) {
                String packageName = resolveInfo.activityInfo.packageName;
                grantUriPermission(packageName, contentUri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri);
        startActivityForResult(intent, 1000);
    }
    // 接受拍照的结果
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            ContentResolver contentProvider = getContentResolver();
            ParcelFileDescriptor mInputPFD;
            try {
                //获取contentProvider图片
                mInputPFD = contentProvider.openFileDescriptor(contentUri, "r");
                final FileDescriptor fileDescriptor = mInputPFD.getFileDescriptor();
                // new thread to deal image by tensorflow
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        dealPics(fileDescriptor);
                    }
                }).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // deal image by tensorflow
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void dealPics(FileDescriptor fileDescriptor) {
        Bitmap bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        // resize bitmap
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float scaleWidth = ((float) Config.INPUT_SIZE) / width;
        float scaleHeight = ((float) Config.INPUT_SIZE) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap newbm = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        // get classifier information
        final List<Classifier.Recognition> results = Config.classifier.recognizeImage(newbm);
        for (final Classifier.Recognition result : results) {
            System.out.println("Result: " + result.getTitle());
        }
        // call function to save image
        String url = saveImage("", bitmap);
        Log.d("Detected = ", String.valueOf(results));
        // update db
        Config.dbHelper = new MyDatabaseHelper(this, "Album.db", null, Config.dbversion);
        SQLiteDatabase db = Config.dbHelper.getWritableDatabase();
        ContentValues values_ablum = new ContentValues();
        values_ablum.put("album_name", results.get(0).getTitle());
        values_ablum.put("show_image", url);
        db.insert("Album", null, values_ablum);
        ContentValues values = new ContentValues();
        values.put("album_name", results.get(0).getTitle());
        values.put("url", url);
        db.insert("AlbumPhotos", null, values);
        db.close();

        // show notification about tf information of image
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.a)
                        .setContentTitle("新的照片")
                        .setContentText(String.valueOf(results));
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(3, mBuilder.build());

    }
    // save image
    private String saveImage(String type, Bitmap bitmap) {
        FileOutputStream b = null;
        // save images to this location
        File file = new File(Config.location);
        // 创建文件夹 @ Config.location
        file.mkdirs();
        String str=null;
        Date date=null;
        // 获取当前时间，进一步转化为字符串
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        date =new Date();
        str=format.format(date);
        String fileName = Config.location + str + ".jpg";

        try {
            b = new FileOutputStream(fileName);
            // 把数据写入文件
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, b);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                b.flush();
                b.close();
                // reflash the fragment of Photos
                Config.workdone = false;
                photos.onReflash(fileName);
                Config.workdone = false;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return fileName;
    }

    //UI组件初始化与事件绑定
    private void bindViews() {
        // 定位textview
        txt_photos = (TextView) findViewById(R.id.all_photos);
        txt_memory = (TextView) findViewById(R.id.memory);
        txt_albums = (TextView) findViewById(R.id.all_albums);
        // 对其设置监听动作
        txt_photos.setOnClickListener(this);
        txt_memory.setOnClickListener(this);
        txt_albums.setOnClickListener(this);
    }

    //重置所有文本的选中状态为未点击状态
    private void setSelected(){
        txt_photos.setSelected(false);
        txt_memory.setSelected(false);
        txt_albums.setSelected(false);
    }

    //隐藏所有Fragment
    private void hideAllFragment(FragmentTransaction fragmentTransaction){
        if(photos != null)fragmentTransaction.hide(photos);
        if(memory != null)fragmentTransaction.hide(memory);
        if(albums != null)fragmentTransaction.hide(albums);
    }

    /**
     * 监听textview的按钮事件
     *
     * @param v
     */

    @Override
    public void onClick(View v) {

        fTransaction = fManager.beginTransaction();
        hideAllFragment(fTransaction);
        switch (v.getId()){
            // 照片

            case R.id.all_photos:
                // set ActionBar tile && set no click action
                actionBar.setDisplayHomeAsUpEnabled(false);
                actionBar.setTitle("照片");
                setSelected();
                txt_photos.setSelected(true);
                // 暂时使用弹出堆栈，以避免从相簿进入相册无法返回
                // 可以使用其他方法，这个方法不好，下面相同
                getFragmentManager().popBackStack();
                if(photos == null){
                    photos = new Photos();
                    fTransaction.add(R.id.ly_content,photos);
                }else{
                    fTransaction.show(photos);
                }

                break;
            // 回忆
            case R.id.memory:
                // 用于显示相应的属性
                // same as photos
                actionBar.setDisplayHomeAsUpEnabled(false);
                actionBar.setTitle("回忆");

                setSelected();
                txt_memory.setSelected(true);
                getFragmentManager().popBackStack();
                if(memory == null){
                    memory = new Memory();
                    fTransaction.add(R.id.ly_content, memory);
                }else{
                    fTransaction.show(memory);
                }
                break;
            // 相册
            case R.id.all_albums:
                // same as photos
                actionBar.setDisplayHomeAsUpEnabled(false);
                actionBar.setTitle("相册");

                setSelected();
                txt_albums.setSelected(true);
                getFragmentManager().popBackStack();
                if(albums == null){
                    albums = new Albums();
                    fTransaction.add(R.id.ly_content,albums);
                }else{
                    fTransaction.show(albums);
                }
                break;
        }
        fTransaction.commit();
    }
}

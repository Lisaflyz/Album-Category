package hitcs.fghz.org.album;

import android.os.Handler;

import org.tensorflow.demo.TensorFlowImageClassifier;

import hitcs.fghz.org.album.dao.MyDatabaseHelper;


/**
 * Created by me on 16-12-31.
 */

public class Config {
    // for tensorflow
    public static final int NUM_CLASSES = 14;
    public static final int INPUT_SIZE = 299;
    public static final int IMAGE_MEAN = 128;
    public static final float IMAGE_STD = 128;
    public static final String INPUT_NAME = "Mul:0";
    public static final String OUTPUT_NAME = "final_result:0";
    public static final String MODEL_FILE = "file:///android_asset/stripped_graph.pb";
    public static final String LABEL_FILE = "file:///android_asset/retrained_labels.txt";
    // once the 'tfpb' had been load, the appliction needn't to reload it again when another thread need tf
    public static TensorFlowImageClassifier classifier;
    // location to save image
    public static String location = "/sdcard/TfAlbum/";
    // database
    public static MyDatabaseHelper dbHelper;
    public static int dbversion = 5;
    // update db after take photo finish?
    public static boolean workdone = false;
    // init the application done
    public static boolean init = false;

    public static int tf_type_times = 14;
    public static String[] tf_type_name = {"动物",
            "建筑", "车辆", "花朵",
            "很多人", "二次元", "人物",
            "风景", "雪景", "含有文字",
            "物品", "教室", "工作地点"
    };
    public static int[] tf_type_image = {R.drawable.animal,
            R.drawable.building, R.drawable.car,R.drawable.flower,
            R.drawable.group_people, R.drawable.manga, R.drawable.people,
            R.drawable.scenery, R.drawable.snow,R.drawable.text,
            R.drawable.things,R.drawable.room,R.drawable.room, R.drawable.a
    };
}

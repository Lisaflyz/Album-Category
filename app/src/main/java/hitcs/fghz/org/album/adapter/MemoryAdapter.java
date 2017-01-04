package hitcs.fghz.org.album.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import java.util.List;

import hitcs.fghz.org.album.Config;
import hitcs.fghz.org.album.R;
import hitcs.fghz.org.album.entity.MemoryItem;

import static android.media.ThumbnailUtils.extractThumbnail;
import static hitcs.fghz.org.album.utils.ImagesScaner.getBitmap;

/**
 * 回忆栏目的适配器
 * Created by me on 16-12-21.
 */

public class MemoryAdapter extends ArrayAdapter<MemoryItem> {
    private int resourceId;
    boolean mBusy = false;
    private Handler myHandler = new Handler()
    {
        @Override
        //重写handleMessage方法,根据msg中what的值判断是否执行后续操作
        public void handleMessage(Message msg) {
            switch (msg.what)
            {
                case 0x24:
                    Log.d("change", "y");
                    notifyDataSetChanged();
                    break;
            }
        }
    };
    public MemoryAdapter(Context context, int textViewResourceId,
                         List<MemoryItem> objects) {
        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        try {
            final MemoryItem memory = getItem(position);

            // 保存当前信息
            AlbumAdapter.ViewHolder holder = null;
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(resourceId, null);
                holder = new AlbumAdapter.ViewHolder();
                holder.iv_thumbnail = (ImageView) convertView.findViewById(R.id.memory_photo);
                holder.thumbnail_url = memory.getImageId();
//                holder.albumName = (TextView) convertView.findViewById(R.id.album_name);
//                holder.albumName.setText(album.getName());
                convertView.setTag(holder);
            } else {
                holder = (AlbumAdapter.ViewHolder) convertView.getTag();
                if (!holder.thumbnail_url.equals(memory.getImageId())) {
                    holder.iv_thumbnail.setImageResource(R.drawable.loading);
                }
            }
            if (!isBusy()) {
                final String imgUrl = memory.getImageId();
                final Bitmap[] bmp = {(Bitmap) Config.mImageCache.get(imgUrl)};
                if (bmp[0] != null) {
                    ;
                } else {
                    try {
//                    holder.iv_thumbnail.setImageResource(R.drawable.b);

                        setBusy(true);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Looper.prepare();
                                loadThumBitmap(imgUrl);
                                bmp[0] = (Bitmap) Config.mImageCache.get(imgUrl);
                                Looper.loop();
                            }
                        }).start();
                        setBusy(false);

                    } catch (Exception e) {
                        Log.d("Error:", "" + e);
                    }
                }
                holder.iv_thumbnail.setImageBitmap(bmp[0]);

            }

        } catch (Exception e) {
            ;
        }
        return convertView;
    }
    //用来保存各个控件的引用
    static class ViewHolder {
        ImageView iv_thumbnail;
        String thumbnail_url;
//        TextView albumName;
    }
    private void loadThumBitmap(final String url) {
        Bitmap bitmap = getBitmap(getContext(),url);
        if (bitmap != null) {
            ;
        } else {
            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_4444;
                bitmap = BitmapFactory.decodeFile(url, options);
                bitmap = extractThumbnail(bitmap,180 , 180);
            } catch (Exception e) {
                Log.d("Error: " , " " + e);
            }
        }
        Config.mImageCache.put(url, bitmap);
        myHandler.sendEmptyMessage(0x24);
    }
    public boolean isBusy() {
        return mBusy;
    }

    public void setBusy(boolean busy) {
        this.mBusy = busy;
    }
}
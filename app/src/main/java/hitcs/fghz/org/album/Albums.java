package hitcs.fghz.org.album;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import org.tensorflow.demo.TensorFlowImageClassifier;

import java.util.ArrayList;
import java.util.List;

import hitcs.fghz.org.album.entity.AlbumItem;
import hitcs.fghz.org.album.adapter.AlbumAdapter;

/**
 * 相册的fregment 的具体动作
 * 请首先阅读photos.java
 *
 * Created by me on 16-12-19.
 */

public class Albums extends Fragment {
    private String content;
    private FragmentManager manager;
    private FragmentTransaction ft;


    public Albums() {
    }
    private String[] data = { "ALBUMS", "Banana", "Orange", "Watermelon",
            "Pear", "Grape", "Pineapple", "Strawberry", "Cherry", "Mango" };
    private List<AlbumItem> albumList = new ArrayList<AlbumItem>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fg_albums,container,false);
        initAlbums();
        GridView listView = (GridView) view.findViewById(R.id.album_list);
        manager = getFragmentManager();
        AlbumAdapter adapter = new AlbumAdapter(getActivity(), R.layout.album_item, albumList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                System.out.println(position+ " " +id);
                // 进入一个新的fregment，这个fregment就是photos.java
                // photos.java中显示具体相册的具体照片
                //创建新的photos对象，初始数值qq暂时没有用， 可以定义为相册id等，但是需要进一步修改
                Photos myJDEditFragment = new Photos();
                ft = manager.beginTransaction();
                ft.add(R.id.ly_content , myJDEditFragment);
                ft.setTransition(FragmentTransaction. TRANSIT_FRAGMENT_OPEN);
                ft.addToBackStack( null);
                ft.commit();
            }
        });
        return view;
    }


    private void initAlbums() {
        AlbumItem album;
        for (String s: data) {
             album = new AlbumItem(s, R.drawable.girl);
            albumList.add(album);
        }
        album = new AlbumItem("Add", R.drawable.add);
        albumList.add(album);

    }
}


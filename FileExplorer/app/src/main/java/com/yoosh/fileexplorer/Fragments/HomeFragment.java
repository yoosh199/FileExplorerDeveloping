package com.yoosh.fileexplorer.Fragments;

import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yoosh.fileexplorer.R;
import com.yoosh.fileexplorer.FileClickListener;
import com.yoosh.fileexplorer.FileOpener;

import com.yoosh.fileexplorer.RecyclerAdapter;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class HomeFragment extends Fragment implements FileClickListener {

    private final String TAG = "LOG";
    View view;
    View home_item_image,home_item_download,home_item_video,home_item_docs,home_item_music,home_item_apk,home_item_internalstorage,home_item_sdcard;
    RecyclerView recycle_recent;
    TextView available_storage,home_item_internalstorage_storage;
    File storage;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view =inflater.inflate(R.layout.home_fragment, container, false);
        initial();

        storage = new File(System.getenv("EXTERNAL_STORAGE"));
        setItemClickListenerVer1(home_item_internalstorage,storage);
        setItemClickListenerVer1(home_item_download,Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));

        setItemClickListenerVer2(home_item_image,"image");
        setItemClickListenerVer2(home_item_video, "video");
        setItemClickListenerVer2(home_item_docs, "docs");
        setItemClickListenerVer2(home_item_music, "music");

        home_item_apk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Toast.makeText(getContext(), "앱", Toast.LENGTH_SHORT).show();
            }
        });

        displayFiles();
        return view;

    }

    private void setItemClickListenerVer1(View layout, File storage) {
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InternalStorageFragment internalStorageFragment = new InternalStorageFragment();
                Bundle bundle = new Bundle();
                bundle.putString("path",storage.getAbsolutePath());
                internalStorageFragment.setArguments(bundle);
                getFragmentManager().beginTransaction().replace(R.id.frame_layout, internalStorageFragment).addToBackStack(null).commit();
            }
        });

    }

    private void initial() {
        home_item_image = view.findViewById(R.id.home_item_image);
        home_item_download = view.findViewById(R.id.home_item_download);
        home_item_video = view.findViewById(R.id.home_item_video);
        home_item_docs = view.findViewById(R.id.home_item_docs);
        home_item_music = view.findViewById(R.id.home_item_music);
        home_item_apk = view.findViewById(R.id.home_item_apk);
        home_item_internalstorage= view.findViewById(R.id.home_item_internalstorage);
        home_item_sdcard= view.findViewById(R.id.home_item_sdcard);
        available_storage = view.findViewById(R.id.available_storage);
        home_item_internalstorage_storage = view.findViewById(R.id.home_item_internalstorage_storage);
    }

    private void setItemClickListenerVer2(View cardView, String type) {
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //interanl 로 보내도 될듯

                GoToInternalStorageFragment(type);
            }
        });
    }

    private void GoToInternalStorageFragment(String type) {
        CategorizedFragment categorizedFragment = new CategorizedFragment();
        Bundle bundle = new Bundle();
        bundle.putString("type", type);
        categorizedFragment.setArguments(bundle);
        getFragmentManager().beginTransaction().replace(R.id.frame_layout, categorizedFragment).addToBackStack(null).commit();
    }

    private void displayFiles() {

        //외부저장소만 계산함
        // 내부저장소 까지도 계산해야함
        available_storage.setText(getCurrentRemainLocalMemory());
        home_item_internalstorage_storage.setText(getLocalTotalMemory());


        recycle_recent = view.findViewById(R.id.recycle_recent);
        recycle_recent.setLayoutManager(new GridLayoutManager(getContext(),1));
        ArrayList<File> fileList = new ArrayList<>();

         findRecentFiles(fileList);

        RecyclerAdapter adapter = new RecyclerAdapter(fileList,getContext(),this);
        recycle_recent.setAdapter(adapter);

    }

    private void findRecentFiles(ArrayList<File> fileList) {
        File recentFile = new File(getContext().getFilesDir(),"recentFile.txt");
        if(recentFile !=null){
            try {
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(getContext().openFileInput("recentFile.txt")));
                fileList.add(new File(br.readLine()));
                Log.d(TAG, "recentFile open: ");
                br.close();
            } catch (Exception e) {
                Log.d(TAG, "recentFile error: ");
                e.printStackTrace();
            }
        }
    }

    @Override
    public void fileClick(File file) {
        Log.d(TAG, "recent fileClick");
        FileOpener.openFile(getContext(),file);

    }

    @Override
    public void fileLongClick(File file) {
        Toast.makeText(getContext(), "미구현", Toast.LENGTH_SHORT).show();

    }

    public  String formatMemorySize(long memory) {
        String suffix = null;

        double size = 0;

        if(memory >= 1024){
            suffix = "KB";
            size = (double) (memory / 1024);

            if(size >= 1024) {
                suffix = "MB";
                size = (double) (size / 1024);

                if(size >= 1024) {
                    suffix = "GB";
                    size = (double) (size / 1024);
                }
            }
        }

        size = Math.round(size * 10d)/10d;

        StringBuilder resultBuffer = new StringBuilder(Double.toString(size));

        if(suffix != null){
            resultBuffer.append(suffix);
        }

        return resultBuffer.toString();
    }

    public  String getCurrentRemainLocalMemory() {

        StatFs stat = new StatFs(storage.getAbsolutePath());
        long blockSize = 0;
        long availableBlocks = 0;
        if(Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            blockSize = stat.getBlockSizeLong();
            availableBlocks = stat.getAvailableBlocksLong();
        } else {
            blockSize = stat.getBlockSize();
            availableBlocks = stat.getAvailableBlocks();
        }

        return formatMemorySize(availableBlocks * blockSize);
    }
    public String getLocalTotalMemory() {

        StatFs stat = new StatFs(storage.getAbsolutePath());
        long blockSize = 0;
        long totalBlocks = 0;
        if(Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            blockSize = stat.getBlockSizeLong();
            totalBlocks = stat.getBlockCountLong();
        } else {
            blockSize = stat.getBlockSize();
            totalBlocks = stat.getBlockCount();
        }
        return  formatMemorySize(totalBlocks * blockSize);
    }

}

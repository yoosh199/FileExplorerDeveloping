package com.example.fileexplorer.Fragments;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fileexplorer.FileClickListener;
import com.example.fileexplorer.FileOpener;
import com.example.fileexplorer.R;
import com.example.fileexplorer.RecyclerAdapter;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import soup.neumorphism.NeumorphCardView;

public class HomeFragment extends Fragment implements FileClickListener {

    private final String TAG = "LOG";
    View view;
    LinearLayout home_item_image,home_item_download,home_item_video,home_item_docs,home_item_music,home_item_apk,home_item_internalstorage,home_item_sdcard;
    RecyclerView recycle_recent;
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

        runtimePermission();
        return view;

    }

    private void setItemClickListenerVer1(LinearLayout layout, File storage) {
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

    private void runtimePermission() {
        Dexter.withContext(getContext()).withPermissions(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
        ).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                displayFiles();
            }
            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                permissionToken.continuePermissionRequest();

            }
        }).check();
    }

    private void displayFiles() {
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

}

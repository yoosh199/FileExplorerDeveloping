package com.example.fileexplorer.Fragments;

import android.Manifest;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import android.text.format.Formatter;
import android.widget.Toast;

import java.util.List;

public class CategorizedFragment extends Fragment implements FileClickListener {
    File storage;
    RecyclerView recyclerView;
    String argumentData;
    TextView path_internal;
    ImageView itemBack;
    String[] dialog_items = {"자세히","변경","삭제"};
    ArrayList<File> fileList;
    RecyclerAdapter recyclerAdapter;
    private final String TAG = "LOG";
    private String mediaType;
    View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.category_fragment, container, false);

        storage = new File(System.getenv("EXTERNAL_STORAGE"));
        mediaType = getArguments().getString("type");

        runtimePermission();
        return view;
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
        recyclerView = view.findViewById(R.id.recycler_category);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(),1));
        fileList=findFiles(storage);
        recyclerAdapter = new RecyclerAdapter(fileList,getContext(),this);
        recyclerView.setAdapter(recyclerAdapter);

    }

    private ArrayList<File> findFiles(File storage) {

        ArrayList<File> list = new ArrayList<>();

        File[] files = storage.listFiles();
        if(files == null){
            return list;
        }

        for(File file : files){
            if(!file.isHidden()){
                if(file.isDirectory()){
                    list.addAll(findFiles(file));
                }
                else{
                    switch (mediaType){
                        case "image":
                            if(file.getName().toLowerCase().endsWith(".jpeg") ||file.getName().toLowerCase().endsWith(".jpg") ||
                                    file.getName().toLowerCase().endsWith(".png")){
                                list.add(file);
                            }
                            break;
                        case "download":
                            if(file.getName().toLowerCase().endsWith(".jpeg") ||file.getName().toLowerCase().endsWith(".jpg") ||
                                    file.getName().toLowerCase().endsWith(".png") || file.getName().toLowerCase().endsWith(".mp3") ||
                                    file.getName().toLowerCase().endsWith(".wav") || file.getName().toLowerCase().endsWith(".mp4") ||
                                    file.getName().toLowerCase().endsWith(".pdf") || file.getName().toLowerCase().endsWith(".doc") ||
                                    file.getName().toLowerCase().endsWith(".apk")){
                                list.add(file);
                            }
                            break;

                        case "music":
                            if(file.getName().toLowerCase().endsWith(".mp3")||file.getName().toLowerCase().endsWith(".wav")){
                                list.add(file);
                            }
                            break;

                        case "video":
                            if(file.getName().toLowerCase().endsWith(".mp4")){
                                list.add(file);
                            }
                            break;
                        case "apk":
                            if(file.getName().toLowerCase().endsWith(".apk")){
                                list.add(file);
                            }
                            break;
                        case "docs":
                            if(file.getName().toLowerCase().endsWith(".pdf") || file.getName().toLowerCase().endsWith(".doc")){
                                list.add(file);
                            }
                            break;
                    }
                }
            }

        }

        return list;
    }

    @Override
    public void fileClick(File file) {
        FileOpener.openFile(getContext(),file);
    }

    @Override
    public void fileLongClick(File file) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setItems(dialog_items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (dialog_items[i]){
                    case "자세히":
                        AlertDialog.Builder detailDialog = new AlertDialog.Builder(getContext());
                        builder.setTitle("자세히");
                        final TextView details = new TextView(getContext());
                        Date lastModified = new Date(file.lastModified());
                        SimpleDateFormat format = new SimpleDateFormat("yyyy/mm/dd HH:mm:ss");
                        String formattedDate = format.format(lastModified);

                        details.setText("파일명: " + file.getName() + "\n"+
                                "크기: "+ Formatter.formatShortFileSize(getContext(),file.length())+"\n"+
                                "경로: "+file.getAbsolutePath()+"\n"+
                                "최근 수정: "+ formattedDate);

                        detailDialog.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });

                        detailDialog.show();
                        break;

                    case "변경":
                        AlertDialog.Builder renameDialog = new AlertDialog.Builder(getContext());
                        renameDialog.setTitle("파일명 변경");
                        final EditText renameText = new EditText(getContext());
                        renameDialog.setView(renameText);

                        renameDialog.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String new_name = renameText.getText().toString();
                                String extention = file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf('.'));
                                File current = file;
                                File destination = new File(file.getAbsolutePath().replace(file.getName(),new_name+extention));
                                if(current.renameTo(destination)){
                                    fileList.set(i,destination);
                                    recyclerAdapter.notifyItemChanged(i);
                                    Toast.makeText(getContext(), "변경!", Toast.LENGTH_SHORT).show();

                                }
                                else{
                                    Toast.makeText(getContext(), "변경불가", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                        renameDialog.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });

                        renameDialog.create();
                        renameDialog.show();
                        break;

                    case "삭제":
                        AlertDialog.Builder deleteDialog = new AlertDialog.Builder(getContext());
                        deleteDialog.setTitle("삭제");
                        deleteDialog.setMessage("이 파일을 삭제하시겠습니까?");

                        deleteDialog.setPositiveButton("예", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                file.delete();
                                fileList.remove(i);
                                recyclerAdapter.notifyDataSetChanged();
                                Toast.makeText(getContext(), "삭제!", Toast.LENGTH_SHORT).show();
                            }
                        });

                        deleteDialog.setNegativeButton("아니요", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });

                        deleteDialog.create();
                        deleteDialog.show();
                        break;
                }
            }
        });

        builder.create();
        builder.show();
    }
}

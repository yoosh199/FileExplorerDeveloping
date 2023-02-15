package com.yoosh.fileexplorer.Fragments;


import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.yoosh.fileexplorer.MainActivity;
import com.yoosh.fileexplorer.R;
import com.yoosh.fileexplorer.FileClickListener;
import com.yoosh.fileexplorer.FileOpener;

import com.yoosh.fileexplorer.RecyclerAdapter;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;

import android.text.format.Formatter;
import android.widget.Toast;

public class CategorizedFragment extends Fragment implements FileClickListener {
    File storage;
    RecyclerView recyclerView;
    String argumentData;
    TextView path_internal;
    ImageView itemBack;
    String[] dialog_items = {"자세히", "변경", "삭제"};
    ArrayList<File> fileList;
    RecyclerAdapter recyclerAdapter;
    private final String TAG = "LOG";
    private String mediaType;
    View view;
    private static Handler mHandler ;
    ProgressDialog progressDialog;



    //스레드
    class NewRunnable implements Runnable {
        public NewRunnable() {
        }

        @Override
        public void run() {
            fileList = findFiles(storage);

            mHandler.sendEmptyMessage(0) ;
        }


    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.category_fragment, container, false);
        System.out.println("category onCreateView");

        recyclerView = view.findViewById(R.id.recycler_category);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
        storage = new File(System.getenv("EXTERNAL_STORAGE"));
        mediaType = getArguments().getString("type");


        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        //위의 onCreateView로 view를 생성해야지 processDialog 실행가능
        //프래그먼트가 만들어지는 시점에 onCreateView,onViewCreated 실행됨
        super.onViewCreated(view, savedInstanceState);

        progressDialog=new ProgressDialog(getActivity());
        progressDialog.setTitle("파일을 불러오는중 입니다...");
        progressDialog.show();


        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {


                displayFiles();
                progressDialog.dismiss();

            }
        } ;


        NewRunnable runnable = new NewRunnable();
        Thread t = new Thread(runnable);
        t.start();

    }

    private void displayFiles() {

        if(fileList.size()==0){
            LayoutInflater.from(getActivity()).inflate(R.layout.not_found, (ViewGroup) view,true);
        }
        else{
            recyclerAdapter = new RecyclerAdapter(fileList, getContext(), this);
            recyclerView.setAdapter(recyclerAdapter);
        }

        MainActivity.progressDialog.dismiss();

    }

    private ArrayList<File> findFiles(File storage) {

        ArrayList<File> list = new ArrayList<>();

        File[] files = storage.listFiles();
        if (files == null) {
            return list;
        }
        for (File file : files) {
            if (!file.isHidden()) {
                if (file.isDirectory()) {
                    list.addAll(findFiles(file));
                } else if (file.length() > 0) {
                    switch (mediaType) {
                        case "image":
                            if (file.getName().toLowerCase().endsWith(".jpeg") || file.getName().toLowerCase().endsWith(".jpg") ||
                                    file.getName().toLowerCase().endsWith(".png")) {
                                list.add(file);
                            }
                            break;
                        case "download":
                            if (file.getName().toLowerCase().endsWith(".jpeg") || file.getName().toLowerCase().endsWith(".jpg") ||
                                    file.getName().toLowerCase().endsWith(".png") || file.getName().toLowerCase().endsWith(".mp3") ||
                                    file.getName().toLowerCase().endsWith(".wav") || file.getName().toLowerCase().endsWith(".mp4") ||
                                    file.getName().toLowerCase().endsWith(".pdf") || file.getName().toLowerCase().endsWith(".doc") ||
                                    file.getName().toLowerCase().endsWith(".apk") || file.getName().toLowerCase().endsWith(".txt")) {
                                list.add(file);
                            }
                            break;

                        case "music":
                            if (file.getName().toLowerCase().endsWith(".mp3") || file.getName().toLowerCase().endsWith(".wav")) {
                                list.add(file);
                            }
                            break;

                        case "video":
                            if (file.getName().toLowerCase().endsWith(".mp4")) {
                                list.add(file);
                            }
                            break;
                        case "apk":
                            if (file.getName().toLowerCase().endsWith(".apk")) {
                                list.add(file);
                            }
                            break;
                        case "docs":
                            if (file.getName().toLowerCase().endsWith(".pdf") || file.getName().toLowerCase().endsWith(".doc")
                                    || file.getName().toLowerCase().endsWith(".txt")) {
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
        FileOpener.openFile(getContext(), file);
    }

    @Override
    public void fileLongClick(File file) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        final int[] location = new int[1];
        builder.setItems(dialog_items, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.d(TAG, "builder: " + i);
                location[0] = i;
                switch (dialog_items[i]) {
                    case "자세히":
                        AlertDialog.Builder detailDialog = new AlertDialog.Builder(getContext());
                        detailDialog.setTitle("자세히");
                        final TextView details = new TextView(getContext());
                        detailDialog.setView(details);
                        Date lastModified = new Date(file.lastModified());
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/mm/dd HH:mm:ss");
                        String formattedDate = formatter.format(lastModified);

                        details.setText("파일명: " + file.getName() + "\n" +
                                "크기: " + Formatter.formatShortFileSize(getContext(), file.length()) + "\n" +
                                "경로: " + file.getAbsolutePath() + "\n" +
                                "최근 수정: " + formattedDate);

                        detailDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
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
                        final EditText name = new EditText(getContext());
                        renameDialog.setView(name);

                        renameDialog.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String new_name = name.getEditableText().toString();
                                String extention = file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf("."));
                                File current = new File(file.getAbsolutePath());

                                File destination = new File(file.getAbsolutePath().replace(file.getName(), new_name + extention));

                                //디렉토리내에 같은 이름의 파일이 있는지 확인
                                Log.d(TAG, "current.renameTo(destination) : " + current.renameTo(destination));
                                Log.d(TAG, "current: " + current);
                                Log.d(TAG, "destination) : " + destination);

                                if (current.renameTo(destination)) {
                                    fileList.set(location[0] - 1, destination);
                                    recyclerAdapter.notifyItemChanged(location[0]);
                                    Toast.makeText(getContext(), "변경!", Toast.LENGTH_SHORT).show();
                                } else {
                                    //중복
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
                                //i는 1부터 시작
                                Log.d(TAG, "deleteDialog: " + i);
                                file.delete();
                                fileList.remove(location[0] - 1);
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

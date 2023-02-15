package com.yoosh.fileexplorer.Fragments;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SearchResultFragment extends Fragment implements FileClickListener {

    View view;
    RecyclerView recyclerView;
    RecyclerAdapter recyclerAdapter;
    String fileName;
    String[] dialog_items = {"자세히","변경","삭제"};
    ArrayList<File> fileList;
    ProgressDialog progressDialog;
    Handler handler;


    class NewRunnable implements Runnable{

        @Override
        public void run() {
            fileList = findFiles(new File(System.getenv("EXTERNAL_STORAGE")));
            handler.sendEmptyMessage(0);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.search_fragment,container,false);

        recyclerView = view.findViewById(R.id.recycler_search);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(),1));


        fileName = getArguments().getString("files");
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressDialog=new ProgressDialog(getActivity());
        progressDialog.setTitle("파일을 불러오는중 입니다...");
        progressDialog.show();

        handler=new Handler(){

            @Override
            public void handleMessage(@NonNull Message msg) {
                displayFiles();
                progressDialog.dismiss();
            }
        };

        NewRunnable newRunnable = new NewRunnable();

        Thread t = new Thread(newRunnable);

        t.start();

    }

    private void displayFiles() {
        if(fileList.size()==0){
            LayoutInflater.from(getActivity()).inflate(R.layout.not_found, (ViewGroup) view,true);
        }
        else{
            recyclerAdapter = new RecyclerAdapter(fileList,getContext(),this);
            recyclerView.setAdapter(recyclerAdapter);
        }

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
                else if(file.length()>0&& file.getName().toLowerCase().contains(fileName)){
                    list.add(file);
                }
            }

        }
        return list;
    }
    @Override
    public void fileClick(File file) {
        if(file.isDirectory()){
            InternalStorageFragment internalStorageFragment = new InternalStorageFragment();
            Bundle bundle = new Bundle();
            bundle.putString("path", file.getAbsolutePath());
            internalStorageFragment.setArguments(bundle);
            getFragmentManager().beginTransaction().replace(R.id.frame_layout,internalStorageFragment).addToBackStack(null).commit();
        }
        else{
            //파일 열기
            FileOpener.openFile(getContext(),file);
        }
    }

    @Override
    public void fileLongClick(File file) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        final int[] location = new int[1];
        builder.setItems(dialog_items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                location[0] =i;
                switch (dialog_items[i]){
                    case "자세히":
                        AlertDialog.Builder detailDialog = new AlertDialog.Builder(getContext());
                        detailDialog.setTitle("자세히");
                        final TextView details = new TextView(getContext());
                        detailDialog.setView(details);
                        Date lastModified = new Date(file.lastModified());
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/mm/dd HH:mm:ss");
                        String formattedDate = formatter.format(lastModified);

                        details.setText("파일명: "+file.getName() + "\n"+
                                "크기: "+ Formatter.formatShortFileSize(getContext(),file.length())+"\n"+
                                "경로: "+ file.getAbsolutePath()+"\n"+
                                "최근 수정: "+ formattedDate);

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

                                File destination = new File(file.getAbsolutePath().replace(file.getName(), new_name+extention));


                                if(current.renameTo(destination)){
                                    fileList.set(location[0]-1,destination);
                                    recyclerAdapter.notifyItemChanged(location[0]);
                                    Toast.makeText(getContext(), "변경!", Toast.LENGTH_SHORT).show();
                                }
                                else{
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

                                file.delete();
                                fileList.remove(location[0]-1);
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

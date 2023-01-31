package com.yoosh.fileexplorer.Fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yoosh.fileexplorer.FileClickListener;
import com.yoosh.fileexplorer.FileOpener;
import com.yoosh.fileexplorer.R;
import com.yoosh.fileexplorer.RecyclerAdapter;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class InternalStorageFragment extends Fragment implements FileClickListener {

    File storage;
    RecyclerView recyclerView;
    String argumentData;
    TextView path_internal;
    ImageView itemBack;
    String[] dialog_items = {"자세히","변경","삭제"};
    ArrayList<File> fileList;
    RecyclerAdapter recyclerAdapter;
    private final String TAG = "LOG";
    View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //parents 에 child 붙이는 작업
        view = inflater.inflate(R.layout.internalstorage_fragment,container,false);


        //저장소 초기화
        storage = new File(System.getenv("EXTERNAL_STORAGE"));

        //storage 경로가 EXTERNAL_STORAGE 갈지 아니면 디렉토리타고 들어와서 그쪽으로 갈지
        verifyStorageIsDirectory();
        path_internal= (TextView) view.findViewById(R.id.path_internal);
        itemBack = (ImageView) view.findViewById(R.id.item_back);

        path_internal.setText(storage.getAbsolutePath());
        itemBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFragmentManager().popBackStack();
            }
        });


        displayFiles();

        return view;
    }

    private void verifyStorageIsDirectory() {
        try{
            argumentData  = getArguments().getString("path");
            storage=new File(argumentData);


        }
        catch (Exception e){
            Log.d("log", "오류발생");
            e.printStackTrace();
        }
    }

    private void displayFiles() {

        //디렉토리 클릭시 여기서 새로운 프래그먼트 띄워줘야한다 근데 접근은 RecyclerAdapter 에서 밖에 못함
        //리스너를 인터페이스로 하나 만들고 RecyclerAdapter 로 전달시켜줘야함
        recyclerView= (RecyclerView) view.findViewById(R.id.recycler_internal);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(),1));
        fileList = findFiles();
        recyclerAdapter = new RecyclerAdapter(fileList,getContext(),this);
        recyclerView.setAdapter(recyclerAdapter);

     

    }

    private ArrayList<File> findFiles() {

        ArrayList<File> arrayList = new ArrayList<>();

        if(storage.listFiles()==null){
            return arrayList;
        }
        File[] files = storage.listFiles();
        Log.d("size", "findFiles: "+files.length);
        for(File file : files){
            if(!file.isHidden()){
                if (file.isDirectory()) {
                    arrayList.add(file);
                }
                else{
                    if (file.getName().toLowerCase().endsWith(".jpeg") ||file.getName().toLowerCase().endsWith(".jpg") ||
                            file.getName().toLowerCase().endsWith(".png") || file.getName().toLowerCase().endsWith(".mp3") ||
                            file.getName().toLowerCase().endsWith(".wav") || file.getName().toLowerCase().endsWith(".mp4") ||
                            file.getName().toLowerCase().endsWith(".pdf") || file.getName().toLowerCase().endsWith(".doc") ||
                            file.getName().toLowerCase().endsWith(".apk")) {
                        arrayList.add(file);
                    }
                }
            }

        }
        Log.d("size", "arrayList: "+arrayList.size());
        return arrayList;
    }


    @Override
    public void fileClick(File file) {
        if(file.isDirectory()){
            //새로운 fragment 만들어서 교체
            replaceNewFragment(file);
        }
        else{
            //파일 열기
            FileOpener.openFile(getContext(),file);
        }
    }

    private void replaceNewFragment(File file) {
        InternalStorageFragment internalStorageFragment = new InternalStorageFragment();
        Bundle bundle = new Bundle();
        bundle.putString("path", file.getAbsolutePath());
        internalStorageFragment.setArguments(bundle);
        getFragmentManager().beginTransaction().replace(R.id.frame_layout,internalStorageFragment).addToBackStack(null).commit();
    }

    @Override
    public void fileLongClick(File file) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        final int[] location = new int[1];
        builder.setItems(dialog_items, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.d(TAG, "builder: "+i);
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

                                //디렉토리내에 같은 이름의 파일이 있는지 확인
                                Log.d(TAG, "current.renameTo(destination) : "+current.renameTo(destination));
                                Log.d(TAG, "current: "+current);
                                Log.d(TAG, "destination) : "+destination);

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
                                Log.d(TAG, "deleteDialog: "+i);
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

package com.yoosh.fileexplorer;

import android.content.Context;
import android.net.Uri;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

    private final ArrayList<File> fileList;
    private final Context context;
    private final FileClickListener listener;
    private final String TAG = "LOG";

    public RecyclerAdapter(ArrayList<File> fileList, Context context, FileClickListener listener) {
        this.fileList = fileList;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d("size", "onBindViewHolder: "+fileList.get(position).getName());

        holderSetFileImage(holder, position);
        holderSetFileTitle(holder, position);
        holderSetFileSize(holder, position);
        holderSetClickListener(holder, position);


    }

    private void holderSetClickListener(@NonNull ViewHolder holder, int position) {
        holder.item_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //0 files 폴더 클릭시 강종 해결해야함
                listener.fileClick(fileList.get(position));
            }
        });

        holder.item_container.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Log.d(TAG, "onLongClick");
                listener.fileLongClick(fileList.get(position));
                return true;
            }
        });
    }

    private void holderSetFileTitle(@NonNull ViewHolder holder, int position) {
        holder.item_text.setText(fileList.get(position).getName());
    }

    private void holderSetFileSize(@NonNull ViewHolder holder, int position) {
        int item=0;
        if(fileList.get(position).isDirectory()){
            if(fileList.get(position).listFiles()!=null){
                File[] files = fileList.get(position).listFiles();

                for(File singleFile : files){
                    if(!singleFile.isHidden()){
                        item++;
                    }
                }
            }
            holder.item_size.setText(String.valueOf(item)+" Files");
        }
        else{
            //파일 사이즈 출력
            holder.item_size.setText(Formatter.formatFileSize(context,  fileList.get(position).length()));

        }
    }

    private void holderSetFileImage(@NonNull ViewHolder holder, int position) {

        if(!fileList.get(position).getName().contains(".")){
            holder.item_image.setImageResource(R.drawable.ic_folder);
        }
        else{
            if(fileList.get(position).getName().toLowerCase().endsWith(".jpeg")||fileList.get(position).getName().toLowerCase().endsWith(".jpg")
                    ||fileList.get(position).getName().toLowerCase().endsWith(".png")){
//            holder.item_image.setImageResource(R.drawable.ic_image);
                //이미지 경우 사진 올려서 보여주기
                Uri uri = FileProvider.getUriForFile(context.getApplicationContext(), context.getApplicationContext().getPackageName() + ".provider", fileList.get(position));
                holder.item_image.setImageURI(uri);
            }
            else if(fileList.get(position).getName().toLowerCase().endsWith(".pdf")){
                holder.item_image.setImageResource(R.drawable.ic_pdf);

            }
            else if(fileList.get(position).getName().toLowerCase().endsWith(".doc")){
                holder.item_image.setImageResource(R.drawable.ic_docs);

            }
            else if(fileList.get(position).getName().toLowerCase().endsWith(".mp3")){
                holder.item_image.setImageResource(R.drawable.ic_music);

            }
            else if(fileList.get(position).getName().toLowerCase().endsWith(".wav")){
                holder.item_image.setImageResource(R.drawable.ic_music);
            }
            else if(fileList.get(position).getName().toLowerCase().endsWith(".mp4")){
                holder.item_image.setImageResource(R.drawable.ic_play);
            }
            else if(fileList.get(position).getName().toLowerCase().endsWith(".apk")){
                holder.item_image.setImageResource(R.drawable.ic_apk);
            }
            else if(fileList.get(position).getName().toLowerCase().endsWith(".txt")){
                holder.item_image.setImageResource(R.drawable.ic_text);
            }
            else{
                holder.item_image.setImageResource(R.drawable.ic_mark);
            }
        }



    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView item_text;
        private TextView item_size;
        private ImageView item_image;
        LinearLayout item_container;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View
            item_text = (TextView) view.findViewById(R.id.item_title);
            item_size = (TextView) view.findViewById(R.id.item_size);
            item_image = (ImageView) view.findViewById(R.id.item_img);
            item_container = (LinearLayout) view.findViewById(R.id.item_container);
        }

    }

}

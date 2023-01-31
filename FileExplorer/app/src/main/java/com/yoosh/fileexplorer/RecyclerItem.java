package com.yoosh.fileexplorer;

public class RecyclerItem {
    //recycler_item에 들어갈 값들
    String item_title,item_size;
    int item_img;

    public RecyclerItem(String item_title, String item_size, int item_img) {
        this.item_title = item_title;
        this.item_size = item_size;
        this.item_img = item_img;
    }
}

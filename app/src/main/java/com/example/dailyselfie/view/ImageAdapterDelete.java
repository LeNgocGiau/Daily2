package com.example.dailyselfie.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.dailyselfie.R;

import java.util.ArrayList;

public class ImageAdapterDelete extends BaseAdapter {

    private Context context;
    private ArrayList<Bitmap> images = new ArrayList<Bitmap>();
    private ArrayList<String> image_name = new ArrayList<String>();
    LayoutInflater inflater;

    public ImageAdapterDelete(Context context, ArrayList<Bitmap> images, ArrayList<String> image_name) {
        this.context = context;
        this.images = images;
        this.image_name = image_name;
    }

    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (inflater == null)
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_image_delete, null);
        }
        ImageView imageView = convertView.findViewById(R.id.image_delete);
        imageView.setImageBitmap(images.get(position));
        TextView textView = convertView.findViewById(R.id.image_name);
        textView.setText(image_name.get(position));
        return convertView;
    }
}
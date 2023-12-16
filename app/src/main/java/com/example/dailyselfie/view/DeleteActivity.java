package com.example.dailyselfie.view;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.dailyselfie.R;

import java.io.File;
import java.util.ArrayList;

public class DeleteActivity extends AppCompatActivity {
    ArrayList<Bitmap> listImage = new ArrayList<Bitmap>();
    ArrayList<String> nameImage = new ArrayList<String>();
    ListView listView;
    String[] iteams = {"Xóa", "Khôi phục"}  ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete);
        listView = findViewById(R.id.list_image_delete);
        loadImage();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                AlertDialog.Builder choice = new AlertDialog.Builder(DeleteActivity.this);
                choice.setTitle("Tùy chọn");
                choice.setItems(iteams, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int id) {
                        if(id == 0){
                            deleteImage(i);
                        }else{
                            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                            File dir_1 = new File(storageDir, "Deleted");
                            File dir_2 = new File(dir_1, nameImage.get(i));
                            File dest = new File(storageDir, nameImage.get(i));
                            dir_2.renameTo(dest);
                            loadImage();
                        }
                    }
                });
                choice.show();
            }

        });
    }

    //Lấy danh sách hình ảnh đã xóa
    private void loadImage() {
        listImage.clear();
        nameImage.clear();
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        String[] list = storageDir.list();
        File listFile = new File(storageDir, "Deleted");
        String[] list_2 = listFile.list();
        for (int i = 0; i < list_2.length; i++) {
            Bitmap bitmap = bitmapEdit.setImageFromFilePath(listFile + "/" + list_2[i]);
            listImage.add(bitmap);
            nameImage.add(list_2[i]);
        }
        ImageAdapterDelete imageAdapter = new ImageAdapterDelete(DeleteActivity.this, listImage, nameImage);
        listView.setAdapter(imageAdapter);
    }

    //Thông báo xóa
    private void deleteImage(int i) {
        AlertDialog.Builder alert = new AlertDialog.Builder(DeleteActivity.this);
        alert.setTitle("Xác nhận");
        alert.setMessage("Xóa vĩnh viễn ảnh này?");
        alert.setPositiveButton("Đồng ý", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int id) {
                File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                File dir_1 = new File(storageDir, "Deleted");
                File dir_2 = new File(dir_1, nameImage.get(i));
                //Toast.makeText(DeleteActivity.this, dir_2.getPath(), Toast.LENGTH_SHORT).show();
                if (dir_2.delete()) {
                    Toast.makeText(DeleteActivity.this, "Xóa thành công", Toast.LENGTH_SHORT).show();
                    loadImage();
                }
            }
        });
        alert.setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        alert.show();
    }
}
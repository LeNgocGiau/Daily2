package com.example.dailyselfie;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.Fade;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.dailyselfie.databinding.ActivityMainBinding;
import com.example.dailyselfie.service.AlarmReceiver;
import com.example.dailyselfie.view.DeleteActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements GalleryAdapter.ItemClickListener {

    private ActivityMainBinding binding;
    private MaterialToolbar toolbar;
    private RecyclerView recyclerView;
    private ShapeableImageView largeImage;
    private List<Bitmap> list;
    private File[] images;
    private SharedPreferences prefs;
    static final int SECOND = 1000;        // no. of ms in a second
    static final int MINUTE = SECOND * 60; // no. of ms in a minute
    static final int HOUR = MINUTE * 60;   // no. of ms in an hour
    static final int DAY = HOUR * 24;      // no. of ms in a day
    static final int WEEK = DAY * 7;       // no. of ms in a week

    //thiết lập thời gian cho thông báo nhắc nhở chụp ảnh selfie.
    @SuppressLint("SuspiciousIndentation")
    private void setTimeForNotification(boolean isToast) {
        //Hủy bỏ bất kỳ thông báo nhắc nhở cũ nào đang tồn tại
        AlarmReceiver.cancel(getApplicationContext());
        //Lấy giá trị giờ từ SharedPreferences key "hour". Giá trị mặc định là 9 nếu key không tồn tại.
        int hour = prefs.getInt("hour",9);
        //Lấy giá trị phút từ SharedPreferences key "minute". Giá trị mặc định là 0 nếu key không tồn tại.
        int minute = prefs.getInt("minute",0);
        //Tạo một đối tượng Calendar mới theo ngôn ngữ hệ thống.
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        //Thiết lập giờ cho calendar thành giá trị lấy từ SharedPreferences
        calendar.set(Calendar.HOUR_OF_DAY,hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND,0);
        long nextTime; //Khai báo một biến long tên nextTime để lưu thời gian thông báo tiếp theo
        //Kiểm tra xem thời gian thông báo đã qua hay chưa
        //Nếu thời gian đã qua, đặt nextTime bằng thời gian thông báo của ngày hôm sau
        if (calendar.getTimeInMillis() - Calendar.getInstance(Locale.getDefault()).getTimeInMillis() < 0) {
            //Toast.makeText(this, "Đã qua " + hour + " giờ",Toast.LENGTH_SHORT).show();

            nextTime = (calendar.getTimeInMillis() - Calendar.getInstance(Locale.getDefault()).getTimeInMillis()) + (1_000L * 3600 * 24);
        }

        else {
            //Toast.makeText(this, "Chưa tới " + hour + " giờ",Toast.LENGTH_SHORT).show();
            //Nếu thời gian thông báo chưa đến, đặt nextTime bằng thời gian thông báo còn lại tính đến thời điểm hiện tại.
            nextTime = (calendar.getTimeInMillis() - Calendar.getInstance(Locale.getDefault()).getTimeInMillis());
        }
        //Gọi hàm remindAfterTime trong class AlarmReceiver để thiết lập thông báo với thời gian nextTime
        AlarmReceiver.remindAfterTime(getApplicationContext(),nextTime);
//        Toast.makeText(this, "Còn "+((double)nextTime / 3600D / 1000D) + " giờ nữa sẽ đến thời gian tự sướng.",Toast.LENGTH_SHORT).show();
//        Toast.makeText(this, "Còn "+nextTime + " miliseconds nữa sẽ đến thời gian tự sướng.",Toast.LENGTH_SHORT).show();
        int hourR   = (int)((nextTime % DAY) / HOUR);
        int minuteR = (int)((nextTime % HOUR) / MINUTE);
        int secondR = (int)((nextTime % MINUTE) / SECOND);
        if (isToast) //Nếu tham số true, hiển thị toast thông báo thời gian còn lại cho đến thời gian thông báo
            //Hiển thị toast thông báo thời gian còn lại tính bằng giờ
        Toast.makeText(this, "Còn "+ (hourR > 0 ? hourR + " giờ " : "") + (minuteR > 0 ? minuteR + " phút " : "") + (secondR > 0 ? secondR + " giây " : "") + " nữa sẽ đến thời gian selfie.",Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Gọi phương thức onCreate() của lớp cha để thực thi các hành động khởi tạo mặc định của Activity
        super.onCreate(savedInstanceState);
        //Sử dụng ActivityMainBinding để inflate layout của Activity
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        //Khởi tạo đối tượng SharedPreferences để lưu trữ các giá trị preferences của ứng dụng
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Kiểm tra xem giá trị của key "firstTime" trong SharedPreferences có bằng false hay không
        if(!prefs.getBoolean("firstTime", false)) {
            // run your one time code
            setTimeForNotification(false); //Thiết lập thời gian cho thông báo
            SharedPreferences.Editor editor = prefs.edit();  //Tạo đối tượng Editor để chỉnh sửa giá trị trong SharedPreferences
            //Thiết lập giá trị của key "firstTime" thành true để đánh dấu ứng dụng đã được khởi động lần đầu tiên
            editor.putBoolean("firstTime", true);
            editor.apply(); //Lưu lại các thay đổi trong SharedPreferences
        }

        initViews(); //Gọi hàm initViews() để khởi tạo các View trong layout
        initListeners();// Gọi hàm initListeners() để thiết lập các listener cho các View
        initRecycler(); //ọi hàm initRecycler() để khởi tạo và cập nhật Recycler View

        setContentView(binding.getRoot()); //Thiết lập layout của Activity bằng view root của binding

    }

    private void initViews() { //khởi tạo các view trong layout của activity
        toolbar = binding.toolbar; //Khởi tạo biến toolbar bằng view toolbar trong layout được inflated
        recyclerView = binding.recycler; //Khởi tạo biến recyclerView bằng view recycler trong layout được inflated
        largeImage = binding.largeImage; //Khởi tạo biến largeImage bằng view largeImage trong layout được inflated
    }

    //thiết lập menu item camera và clock trong toolbar
    private void initListeners() {
        toolbar.setOnMenuItemClickListener(item -> { //Thiết lập listener cho toolbar để lắng nghe các sự kiện click vào menu item
            if (item.getItemId() == R.id.camera) { //Kiểm tra xem menu item được click có id là R.id.camera hay không
                Intent i = new Intent(MainActivity.this, CameraActivity.class); //Nếu đúng, tạo một intent để mở CameraActivity
                startActivity(i); //Start activity CameraActivity
            }
            if (item.getItemId() == R.id.clock) { //Kiểm tra xem menu item được click có id là R.id.clock hay không
                MaterialTimePicker materialTimePicker = new MaterialTimePicker.Builder() //Nếu đúng, tạo một MaterialTimePicker mới
                        .setTimeFormat(TimeFormat.CLOCK_12H) //Thiết lập format thời gian 12h.
                        //Thiết lập giờ mặc định dựa trên giá trị lưu trong SharedPreferences key "hour". Giá trị mặc định là 9 nếu key không tồn tại
                        .setHour(prefs.getInt("hour",9))
                        .setMinute(prefs.getInt("minute",0))
                        .setTitleText("Chọn thời gian thông báo mỗi ngày")  //Thiết lập tiêu đề cho MaterialTimePicker
                        .build(); //Xây dựng MaterialTimePicker
                //Thêm listener cho button "OK" của MaterialTimePicker
                materialTimePicker.addOnPositiveButtonClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Tạo đối tượng Editor để chỉnh sửa giá trị trong SharedPreferences
                        SharedPreferences.Editor editor = prefs.edit();
                        //Lưu giá trị giờ được chọn vào SharedPreferences key "hour"
                        editor.putInt("hour", materialTimePicker.getHour());
                        //Lưu giá trị giờ được chọn vào SharedPreferences key "hour"
                        editor.putInt("minute", materialTimePicker.getMinute());
                        editor.apply(); //Lưu lại các thay đổi trong SharedPreferences
                        setTimeForNotification(true); //Gọi hàm setTimeForNotification để thiết lập nhắc nhở dựa trên thời gian được chọn
                    }
                });
                materialTimePicker.show(getSupportFragmentManager(),"GICUNGDC"); //Hiển thị MaterialTimePicker
            }
            return true; //Trả về true để cho biết sự kiện click đã được xử lý
        });
    }

    @Override
    public void onBackPressed() {
        //Kiểm tra xem thuộc tính visibility của largeImage có bằng View.VISIBLE hay không
        if (largeImage.getVisibility() == View.VISIBLE)
            //Nếu ảnh lớn đang được hiển thị, gọi hàm hideLargeImage() để ẩn nó và hiển thị lại Recycler View
            hideLargeImage();
        else //Nếu ảnh lớn không được hiển thị, nghĩa là Recycler View đang hiển thị.
        //Gọi phương thức onBackPressed() của lớp cha để thực hiện hành động mặc định của Back button
            super.onBackPressed();
    }

    private void showLargeImage(Bitmap bitmap) {
        //Thiết lập icon của navigation button trong toolbar thành icon mũi tên trái để người dùng có thể quay trở lại
        toolbar.setNavigationIcon(getDrawable(R.drawable.ic_arrow_sm_left));
        //Thiết lập listener cho navigation button để xử lý sự kiện click
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideLargeImage();
            } //ẩn ảnh, hiển thị lại Recycler View
        });
        largeImage.setImageBitmap(bitmap); //Thiết lập hình ảnh cho largeImage bằng tham số bitmap được truyền vào
        //Tạo một đối tượng Transition mới có tên transition với kiểu CircularRevealTransition
        Transition transition = new CircularRevealTransition();
        //Thiết lập thời gian animation cho transition là 600 mili giây
        transition.setDuration(600);
        //Thêm largeImage vào danh sách các View sẽ được animation bởi transition
        transition.addTarget(largeImage);
        //sử dụng phương thức beginDelayedTransition của lớp TransitionManager để bắt đầu animation
        TransitionManager.beginDelayedTransition(binding.getRoot(), transition);
        //Thiết lập thuộc tính visibility của largeImage thành View.VISIBLE để hiển thị ảnh
        largeImage.setVisibility(View.VISIBLE);
        //Thiết lập thuộc tính visibility của recyclerView thành View.GONE để ẩn Recycler View
        recyclerView.setVisibility(View.GONE);
    }

//Tạo animation mờ dần cho Recycler View, Ẩn ảnh, Hiển thị lại Recycler View, Ẩn icon navigation button trong toolbar.
    private void  hideLargeImage() {
        //Tạo một đối tượng Transition mới có tên transition với kiểu Fade. Kiểu này thực hiện animation mờ dần.
        Transition transition = new Fade();
        //hiết lập thời gian animation cho transition là 600 mili giây
        transition.setDuration(600);
        //Thêm recyclerView vào danh sách các View sẽ được animation bởi transition
        transition.addTarget(recyclerView);
        //Sử dụng phương thức beginDelayedTransition của lớp TransitionManager để bắt đầu animation
        TransitionManager.beginDelayedTransition(binding.getRoot(), transition);
        //Thiết lập thuộc tính visibility của largeImage thành View.GONE để ẩn ảnh
        largeImage.setVisibility(View.GONE);
        //Thiết lập thuộc tính visibility của recyclerView thành View.VISIBLE để hiển thị lại Recycler View
        recyclerView.setVisibility(View.VISIBLE);
        //Thiết lập icon của navigation button trong toolbar thành null để ẩn icon này
        toolbar.setNavigationIcon(null);
    }

    private void initRecycler() {
        //Lấy đường dẫn thư mục lưu ảnh từ bộ nhớ bên ngoài
        File folder = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES).toURI());
        if (folder.exists()) {  //Kiểm tra xem thư mục có tồn tại không
            images = folder.listFiles(new FilenameFilter() { //Biến images lưu trữ danh sách các file ảnh được lọc
                @Override
                public boolean accept(File file, String s) {
                    //sử dụng một FilenameFilter để lọc các file có đuôi jpg, jpeg, png
                    return (s.endsWith(".jpg") || s.endsWith(".jpeg") || s.endsWith(".png"));
                }
            });
            list = new ArrayList<>();  //Tạo danh sách ảnh
            for (File image : images) {  //Duyệt qua từng file ảnh trong danh sách images
                //Đối với mỗi file, sử dụng BitmapFactory.decodeFile(image.getPath()) để giải mã file và tạo một đối tượng Bitmap
                //Thêm đối tượng Bitmap này vào danh sách list
                list.add(BitmapFactory.decodeFile(image.getPath()));
            }
            // tạo và Thiết lập layout manager cho Recycler View là GridLayoutManager với 3 cột
            recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
            //Tạo một adapter GalleryAdapter và truyền danh sách list vào
            GalleryAdapter galleryAdapter = new GalleryAdapter(this,list);
            //Thiết lập listener cho adapter để lắng nghe sự kiện click vào item
            galleryAdapter.setClickListener(this);
            //Gán adapter cho Recycler View
            recyclerView.setAdapter(galleryAdapter);
        }
        else //Nếu thư mục ảnh không tồn tại, hiển thị một toast thông báo cho người dùng
            Toast.makeText(this,"Không thể load thư mục ảnh selfie",Toast.LENGTH_LONG).show();
    }

// click vào một item trong Recycler View
    @Override
    public void onItemClick(View view, int position) {
        if (list != null) {
            showLargeImage(list.get(position)); //Hàm này sẽ hiển thị ảnh lớn tương ứng với item được click, lấy vị trí ảnh trong ds list
        }
    }

    //xử lý sự kiện nhấn giữ lâu (long click) vào một item trong Recycler View
    @Override
    public void onItemLongClick(View view, int position) {
        //Kiểm tra xem mảng "images" có null hay không
        if (images != null) {
            //Tạo một đối tượng MaterialAlertDialogBuilder để hiển thị một dialog xác nhận xóa ảnh.
            new MaterialAlertDialogBuilder(this, com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered)
                    .setTitle("Xóa ảnh ?")
                    .setMessage("Bạn có chắc muốn xóa ảnh này ?")
                    .setPositiveButton("Xóa", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (images[position].delete()) {  //Xóa file ảnh tương ứng với item được nhấn giữ
                                initRecycler(); //Cập nhật lại recycler view
                                //Hiển thị toast thông báo "Xóa thành công" nếu xóa file thành công.
                                Toast.makeText(MainActivity.this, "Xóa thành công", Toast.LENGTH_SHORT).show();
                            }

                            else  //Nếu xóa file thất bại, hiển thị toast thông báo "Đã có lỗi xảy ra"
                                Toast.makeText(MainActivity.this, "Đã có lỗi xảy ra", Toast.LENGTH_SHORT).show();
                            dialogInterface.dismiss(); //Đóng dialog sau khi người dùng click vào nút "Xóa" hoặc "Hủy".
                        }
                    })
                    //Khi người dùng click vào nút "Hủy", dialog sẽ đóng
                    .setNeutralButton("Hủy", (dialogInterface, i) -> dialogInterface.dismiss()).show();
        }
    }

    //Recycler View được cập nhật và ảnh được ẩn khi activity được tiếp tục
    @Override
    protected void onResume() {
        super.onResume();
        initRecycler();
        hideLargeImage();
    }
}
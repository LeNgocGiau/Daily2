package com.example.dailyselfie;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Image;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Toast;

import com.example.dailyselfie.databinding.ActivityCameraBinding;
import com.example.dailyselfie.service.AlarmReceiver;
import com.example.dailyselfie.service.RemindWorker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CameraActivity extends AppCompatActivity {
    //Khai báo biến cho request code yêu cầu quyền, danh sách quyền cần thiết
    private int REQUEST_CODE_PERMISSIONS = 1001;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE"};

    private Executor executor = Executors.newSingleThreadExecutor();
    private PreviewView mPreviewView;
    private FloatingActionButton mCapture,mBack,mToggle;

    private ActivityCameraBinding binding;

    private int lensFacing = CameraSelector.LENS_FACING_BACK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //gắn các view trong layout với biến binding
        binding = ActivityCameraBinding.inflate(getLayoutInflater());
        //đặt view gốc của layout làm nội dung View
        setContentView(binding.getRoot());
        //Lấy tham chiếu đến các view cần sử dụng để truy xuất view
        mPreviewView = binding.cameraPreview;
        mCapture = binding.capture;
        mBack = binding.back;
        mToggle = binding.toggleLens;
        //Kiểm tra quyền và khởi tạo camera
        if(allPermissionsGranted()){
            startCamera(); //khởi tạo camera nếu đủ quyền
        } else{
            //Ngược lại yêu cầu cấp quyền
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

//        mToggle.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                toggleFrontBackCamera();
//            }
//        });

        mBack.setOnClickListener(new View.OnClickListener() {
            //mBack khi click sẽ kết thúc Activity
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    //chuyển đổi giữa camera trước và camera sau trong ứng dụng Android
    private void toggleFrontBackCamera() {
        //lensFacing là biến lưu trạng thái hiện tại là camera trước hay sau
        lensFacing = CameraSelector.LENS_FACING_FRONT == lensFacing ? CameraSelector.LENS_FACING_BACK : CameraSelector.LENS_FACING_FRONT;
        if(allPermissionsGranted()){  //Kiểm tra xem ứng dụng đã được cấp tất cả các quyền cần thiết chưa
            startCamera(); //khởi động lại camera
        } else{  // nếu chưa cấp quyền sẽ hiển thị yêu cầu cấp quyền cho người dùng
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
    }

    //khởi động camera trong ứng dụng Android
    private void startCamera() {
        //Tạo một đối tượng ListenableFuture để lấy một instance của ProcessCameraProvider lớp cung cấp quyền truy cập vào camera của thiết bị
        final ListenableFuture<ProcessCameraProvider> cameraProviderListenableFuture = ProcessCameraProvider.getInstance(this);
        //Listener này sẽ được gọi khi instance của ProcessCameraProvider đã sẵn sàng
        cameraProviderListenableFuture.addListener(new Runnable() {
            //Ghi đè phương thức run() của interface Runnable
            @Override
            public void run() {
                try {
                    //Lấy instance của ProcessCameraProvider từ ListenableFuture
                    ProcessCameraProvider cameraProvider = cameraProviderListenableFuture.get();
                    //thiết lập preview của camera trên giao diện người dùng.
                    bindPreview(cameraProvider);
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }, ContextCompat.getMainExecutor(this));  //Chỉ định rằng listener sẽ được thực thi trên main thread của ứng dụng
    }

    //thay đổi hình ảnh của hai View (mCapture và mToggle) dựa trên trạng thái được truyền vào (state)
    private void confirmMode(boolean state) {
        if (state) { // chế độ xem trước
            //đặt hình ảnh của View mCapture thành R.drawable.ic_check(biểu tượng xác nhận)
            mCapture.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ic_check));
            //Đặt hình ảnh của View mToggle thành R.drawable.ic_x(biểu tượng thoát)
            mToggle.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ic_x));
        }
        else {
            //Set hình biểu tượng cho nút chụp là hình biểu tượng camera
            mCapture.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ic_camera));
            //Set hình biểu tượng cho nút chuyển chế độ là hình biểu tượng tải lại
            mToggle.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ic_refresh));
        }

    }

    //sử dụng CameraX để thiết lập preview và chụp ảnh
    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        //Tạo một object Preview mới để hiển thị khung hình của camera.
        Preview preview = new Preview.Builder()
                .build();
        //Tạo một object CameraSelector để chọn camera trước hoặc sau dựa trên biến lensFacing
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build();
        //Tạo một object ImageAnalysis để phân tích ảnh chụp
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .build();
        //Tạo một object ImageCapture.Builder để xây dựng cấu hình chụp ảnh
        ImageCapture.Builder builder = new ImageCapture.Builder();

        //Vendor-Extensions (The CameraX extensions dependency in build.gradle)
//        HdrImageCaptureExtender hdrImageCaptureExtender = HdrImageCaptureExtender.create(builder);
//
//        // Query if extension is available (optional).
//        if (hdrImageCaptureExtender.isExtensionAvailable(cameraSelector)) {
//            // Enable the extension if available.
//            hdrImageCaptureExtender.enableExtension(cameraSelector);
//        }

        //Xây dựng object imageCapture và đặt hướng chụp ảnh tương ứng với hướng màn hình
        final ImageCapture imageCapture = builder
                .setTargetRotation(this.getWindowManager().getDefaultDisplay().getRotation())
                .build();
        //Thiết lập preview của camera trên SurfaceProvider của view mPreviewView
        preview.setSurfaceProvider(mPreviewView.getSurfaceProvider());
        //Liên kết camera với lifecycle của activity, chọn camera, preview, phân tích ảnh và chụp ảnh
        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, preview, imageAnalysis, imageCapture);


        mToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            //Click event sẽ unbind tất cả camera và chuyển đổi camera trước/sau
            public void onClick(View view) {
                cameraProvider.unbindAll();
                toggleFrontBackCamera();
            }
        });

        //Click event sẽ chụp ảnh và hiển thị xác nhận
        mCapture.setOnClickListener(new View.OnClickListener() {
            public String getBatchDirectoryName() { //Hàm này lấy đường dẫn thư mục ảnh trong bộ nhớ ngoài thiết bị

                String app_folder_path = "";
                //Gán cho app_folder_path đường dẫn đến thư mục "images" trong thư mục "DailySelfie" nằm trong thư mục lưu trữ ngoài của thiết bị
                app_folder_path = Environment.getExternalStorageDirectory().toString() + "/DailySelfie/images";
                File dir = new File(app_folder_path); //Tạo một object File với đường dẫn vừa tạo
                if (!dir.exists() && !dir.mkdirs()) { //Kiểm tra xem thư mục dir có tồn tại hay không
                    Toast.makeText(CameraActivity.this, "Lỗi", Toast.LENGTH_SHORT).show();
                }

                return app_folder_path; //Trả về app_folder_path chứa đường dẫn đến thư mục ảnh của ứng dụng
            }
            @Override
            public void onClick(View v) {
                //Tạo một object SimpleDateFormat để định dạng tên file ảnh theo thời gian chụp
                SimpleDateFormat mDateFormat = new SimpleDateFormat("HH-mm-ss-dd-MM-yyyy",Locale.getDefault());
                //Tạo một object File với đường dẫn đến file ảnh
                File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), mDateFormat.format(new Date())+ ".jpg");
 
                //Tạo một object ImageCapture.OutputFileOptions để xác định cách lưu ảnh
                ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(file).build();
                cameraProvider.unbind(preview); //Ngắt kết nối preview camera trước khi chụp ảnh để chỉ tập trung xử lý ảnh chụp
                //Thực hiện chụp ảnh với imageCapture và một callback để xử lý kết quả
                imageCapture.takePicture(executor, new ImageCapture.OnImageCapturedCallback() {
                    @Override
                    public void onCaptureSuccess(@NonNull ImageProxy image) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //hiển thị xác nhận chụp ảnh
                                confirmMode(true);
                                //Lấy ảnh đã chụp từ ImageProxy
                                @SuppressLint("UnsafeOptInUsageError") Image img = image.getImage();
                                mCapture.setOnClickListener(new View.OnClickListener() { //Thay đổi listener của mCapture để xử lý ảnh đã chụp
                                    @Override
                                    public void onClick(View view) {
                                        //Lấy buffer chứa dữ liệu ảnh từ Image
                                        ByteBuffer buffer = img.getPlanes()[0].getBuffer();
                                        //Tạo mảng byte để lưu dữ liệu ảnh
                                        byte[] bytes = new byte[buffer.remaining()];
                                        buffer.get(bytes); //Sao chép dữ liệu từ buffer sang mảng byte
                                        FileOutputStream outputStream = null; //Khai báo biến để lưu file output stream
                                        try {
                                            //Tạo một object FileOutputStream để ghi vào file đã tạo
                                            outputStream = new FileOutputStream(file);
                                            outputStream.write(bytes); //Ghi dữ liệu ảnh (đã lưu trong mảng byte) vào file
                                            //Kết thúc activity hiện tại
                                            finish();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        } finally {
                                            img.close(); //Đóng object Image
                                            if (outputStream != null) { //Nếu đã mở file output stream, đóng nó lại
                                                try {

                                                    outputStream.close(); //Đóng file output stream
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }
                                    }
                                });
                                mToggle.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        cameraProvider.unbindAll(); //Hủy liên kết camera cũ
                                        confirmMode(false); //Thay đổi giao diện
                                        startCamera(); //Khởi tạo lại camera mới
                                    }
                                });
                            }
                        });
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        super.onError(exception);
                    }
                });

//                mCapture.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        imageCapture.takePicture(outputFileOptions, executor, new ImageCapture.OnImageSavedCallback () {
//                            @Override
//                            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
//                                runOnUiThread(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        confirmMode(false);
//                                        startCamera();
//                                        Toast.makeText(Camera_Activity.this, "Image Saved successfully", Toast.LENGTH_SHORT).show();
//                                    }
//                                });
//
//                            }
//
//                            @Override
//                            public void onError(@NonNull ImageCaptureException error) {
//                                error.printStackTrace();
//                            }
//                        });
//                    }
//                });



            }
        });
    }


   //ứng dụng có đủ quyền để hoạt động hay chưa
    private boolean allPermissionsGranted(){
        //một vòng lặp for để duyệt qua từng quyền trong mảng REQUIRED_PERMISSIONS
        for(String permission : REQUIRED_PERMISSIONS){
            //mỗi lần lặp ktra xem quyền hiện tại có được cấp cho ứng dụng chưa
            if(ContextCompat.checkSelfPermission(this,permission) != PackageManager.PERMISSION_GRANTED){
                return false;//Nếu có quyền chưa được cấp thì trả về false
            }
        }//Nếu hết quyền đều được cấp thì trả về true
        return true;
    }


    //ứng dụng chỉ tiếp tục hoạt động khi người dùng đã cấp tất cả các quyền cần thiết
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //permissions là danh sách quyền đã yêu cầu, grantResults là kết quả cấp quyền của từng quyền, RequestCode dùng để nhận diện yêu cầu cấp quyền
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            //Kiểm tra xem liệu người dùng có cấp đủ các quyền cần thiết không bằng phương thức allPermissionsGranted()
            if (allPermissionsGranted()) {
                startCamera();//bắt đầu quá trình chụp ảnh
            } else {//Ngược lại nếu chưa đủ quyền sẽ hiển thị thông báo yêu cầu cấp quyền và kết thúc Activity
                Toast.makeText(this, "Bạn cần phải cấp quyền máy ảnh và lưu trữ.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}
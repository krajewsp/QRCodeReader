package pl.atrim.qrcode;

import java.io.IOException;
import java.util.List;
import java.util.Timer;  
import java.util.TimerTask;  
import com.google.zxing.BinaryBitmap;  
import com.google.zxing.MultiFormatReader;  
import com.google.zxing.Result;  
import com.google.zxing.android.PlanarYUVLuminanceSource;  
import com.google.zxing.common.HybridBinarizer;  
import android.app.Activity;  
import android.graphics.Bitmap;  
import android.graphics.PixelFormat;
import android.hardware.Camera;  
import android.os.Bundle;  
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;  
import android.view.View;  
import android.widget.EditText;
import android.widget.ImageView;  
import android.widget.TextView;  
public class QRCodeReaderActivity extends Activity {  
    /** Called when the activity is first created. */  
    private SurfaceView camView;  
    private MyCamera kamera;  
    private ImageView imgView;  
    private View centerView;  
    private TextView tekst;  
    private Timer mTimer;  
    private MyTimerTask mTimerTask;
    private EditText wynik; 
    int width;
    int height;
    int dstLeft, dstTop, dstWidth, dstHeight;  
    @Override  
    public void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);  
        setContentView(R.layout.main);  
        this.setTitle("Android");  
        imgView = (ImageView) this.findViewById(R.id.ImageView01);  
        centerView = (View) this.findViewById(R.id.centerView);  
        camView = (SurfaceView) this.findViewById(R.id.surface);  
        wynik = (EditText) this.findViewById(R.id.wynik);
        kamera = new MyCamera(camView.getHolder(), previewCallback);
        tekst=(TextView)this.findViewById(R.id.tekst);  
     
        mTimer = new Timer();  
        mTimerTask = new MyTimerTask();  
        mTimer.schedule(mTimerTask, 0, 1200);  
    }  
      
    class MyTimerTask extends TimerTask {  
        @Override  
        public void run() {  
            if (dstLeft == 0) {
                dstLeft = centerView.getLeft() * width  
                        / getWindowManager().getDefaultDisplay().getWidth();  
                dstTop = centerView.getTop() * height  
                        / getWindowManager().getDefaultDisplay().getHeight();  
                dstWidth = (centerView.getRight() - centerView.getLeft())* width  
                        / getWindowManager().getDefaultDisplay().getWidth();  
                dstHeight = (centerView.getBottom() - centerView.getTop())* height  
                        / getWindowManager().getDefaultDisplay().getHeight();  
            }  
            kamera.AutoFocusAndPreviewCallback();  
        }  
    }  
   
    private Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {  
        @Override  
        public void onPreviewFrame(byte[] data, Camera arg1) {  
              
            PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(data, width, height, dstLeft, dstTop, dstWidth, dstHeight, false);  
            
            Bitmap mBitmap = source.renderCroppedGreyscaleBitmap();  
            imgView.setImageBitmap(mBitmap);  
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));  
            MultiFormatReader reader = new MultiFormatReader();  
            try {  
                Result result = reader.decode(bitmap);    
                wynik.setText(result.getText());
            } catch (Exception e) {  
                tekst.setText("Skanujê");  
            }  
        }  
    };  
    public class MyCamera implements SurfaceHolder.Callback{  
        private SurfaceHolder holder = null;  
        private Camera mCamera;  
        private Camera.PreviewCallback previewCallback;  
          
        public MyCamera(SurfaceHolder holder,Camera.PreviewCallback previewCallback) {  
            this.holder = holder;    
            this.holder.addCallback(this);    
            this.holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);  
            this.previewCallback=previewCallback;  
        }  
          
        @Override  
        public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {  
            Camera.Parameters parameters = mCamera.getParameters();
            List<Camera.Size> list = parameters.getSupportedPreviewSizes ();
    		Camera.Size size = list.get(0);
    		width = size.width;
    		height = size.height;
    		parameters.setPreviewSize(width, height);  
            parameters.setPictureFormat(PixelFormat.JPEG);  
            mCamera.setParameters(parameters);    
            mCamera.startPreview();
            Log.e("Camera","surfaceChanged");  
        }  
        @Override  
        public void surfaceCreated(SurfaceHolder arg0) {  
            mCamera = Camera.open();    
            try {    
                mCamera.setPreviewDisplay(holder);   
                Log.e("Camera","surfaceCreated");  
            } catch (IOException e) {    
                mCamera.release();
                mCamera = null;    
            }  
              
        }  
        @Override  
        public void surfaceDestroyed(SurfaceHolder arg0) {  
            mCamera.setPreviewCallback(null);  
            mCamera.stopPreview();    
            mCamera = null;  
            Log.e("Camera","surfaceDestroyed");  
        }  
        
        public void AutoFocusAndPreviewCallback()  
        {  
            if(mCamera!=null)  
                mCamera.autoFocus(mAutoFocusCallBack);  
        }  
          
          
        private Camera.AutoFocusCallback mAutoFocusCallBack = new Camera.AutoFocusCallback() {    
                
            @Override    
            public void onAutoFocus(boolean success, Camera camera) {        
                if (success) {  
                    mCamera.setOneShotPreviewCallback(previewCallback);   
                }    
            }    
        };  
          
      
    }  
}  
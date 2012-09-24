package info.betterbeta.media;

import java.io.ByteArrayOutputStream;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Bitmap.CompressFormat;

public class ImageHelper {


	public static Bitmap resize(Bitmap bitmap){
		int width = bitmap.getWidth();
	    int height = bitmap.getHeight();
	    int newWidth;
	    int newHeight;
	    if (width > height){
		    newWidth = 480;
		    newHeight = 320;
	    }
	    else{
		    newWidth = 320;
		    newHeight = 480;
	    }
	    float scaleWidth = ((float) newWidth) / width;
	    float scaleHeight = ((float) newHeight) / height;
	    Matrix matrix = new Matrix();
	    matrix.postScale(scaleWidth, scaleHeight);
	    return  Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true); 
	}

	public static byte[] getBytes(Bitmap bitmap){
	    ByteArrayOutputStream output = new ByteArrayOutputStream();
	    bitmap.compress(CompressFormat.JPEG, 50, output);
	    return output.toByteArray(); 
	}
}

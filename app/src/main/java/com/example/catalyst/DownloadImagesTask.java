package com.example.catalyst;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import java.io.InputStream;
import java.net.URL;

class DownloadImagesTask extends AsyncTask<String,Void,Bitmap> {
    ImageView imv;
    Context ctx;
    public DownloadImagesTask(ImageView iv, Context c){
        ctx = c;
        imv = iv;
    }

    protected Bitmap doInBackground(String... urls){
        String url = urls[0];
        Bitmap icon = null;
        try {
            InputStream in = new URL(url).openStream();
            icon = BitmapFactory.decodeStream(in);
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return icon;
    }

    protected void onPostExecute(Bitmap result){
        RoundedBitmapDrawable rdb = RoundedBitmapDrawableFactory.create(ctx.getResources(),result);
        rdb.setCornerRadius(30);
        imv.setImageDrawable(rdb);
    }
}

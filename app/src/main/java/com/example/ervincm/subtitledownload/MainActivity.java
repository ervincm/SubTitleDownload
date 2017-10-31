package com.example.ervincm.subtitledownload;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /** 
                  * 动态获取权限，Android 6.0 新特性，一些保护权限，除了要在AndroidManifest中声明权限，还要使用如下代码动态获取 
                  */  
        if (Build.VERSION.SDK_INT >= 23) {  
            int REQUEST_CODE_CONTACT = 101;  
            String[] permissions = {
                Manifest.permission.WRITE_EXTERNAL_STORAGE};  
            //验证是否许可权限  
            for (String str : permissions) {  
                if (this.checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {  
                    //申请权限  
                    this.requestPermissions(permissions, REQUEST_CODE_CONTACT);  
                    return;  
                }  
            }  
        }

        System.out.println("Match number ");

             String regex = "(?i)S\\d+(?i)E\\d+";
             String INPUT = "Game.of.Thrones.s02E01";
                Pattern p = Pattern.compile(regex);
                Matcher m = p.matcher(INPUT); // 获取 matcher 对象
                int count = 0;

                while(m.find()) {
                    count++;
                    System.out.println("Match number "+count);
                    System.out.println("start(): "+m.start());
                    System.out.println("end(): "+m.end());
                }






        Thread thread=new Thread(new Runnable() {
            @Override
            public void run() {
                //search 字幕文件
                String name="http://api.assrt.net/v1/sub/search?token=wOu37LmDZYHpMypmS3xaeWnsqbVv7vhw&q="+"Game.of.Thrones.S02E01"+"&cnt=1&pos=0";



                String search=HttpUtil.sendHttpRequest(name);
                Log.e("ervincm1",search);
                int low=search.indexOf("\"id\"");
                if(low==-1){
                    return;
                }
                int high=search.indexOf(",",low);
                String videoID=null;
                try{
                    videoID=search.substring(low+5,high);
                    Log.e("1",videoID);
                  }catch (Exception e){
                    e.printStackTrace();
                }
                if(videoID!=null){
                    //选择选中的字幕文件
                    String url="http://api.assrt.net/v1/sub/detail?token=wOu37LmDZYHpMypmS3xaeWnsqbVv7vhw&id="+videoID;
                    String result=HttpUtil.sendHttpRequest(url);
                    Log.e("ervincm1",result);

                    //获得filelist内容
                    try {
                        JSONObject jsonObject=new JSONObject(result);
                        JSONObject jsonObjectSub=jsonObject.getJSONObject("sub");
                        JSONArray jsonArraySubs=jsonObjectSub.getJSONArray("subs");
                        JSONArray jsonArrayFilelist=jsonArraySubs.getJSONObject(0).getJSONArray("filelist");
                        for(int i=0;i<jsonArrayFilelist.length()-8;i++){
                            JSONObject jsonObjectDetials=jsonArrayFilelist.getJSONObject(i);
                            String fileUrl;
                            String subFileName;
                            fileUrl=jsonObjectDetials.getString("url");
                            subFileName=jsonObjectDetials.getString("f");
                            System.out.println(fileUrl);
                            System.out.println(subFileName);

                            if(fileUrl!=null){
                                InputStream in = null;

                                HttpURLConnection urlConnection;
                                FileOutputStream f = null;
                                //下载字幕文件
                                try {
                                    URL downloadUrl=new URL(fileUrl);
                                    System.out.println(downloadUrl.toString());
                                    urlConnection = (HttpURLConnection) downloadUrl.openConnection();
                                    urlConnection.setRequestProperty("charset","UTF-8");

                                    f = new FileOutputStream(new File("/storage/emulated/0/Android/data/org.videolan.vlc/files/"+subFileName));

                                    in = urlConnection.getInputStream();
                                    int length;
                                    byte[] buffer = new byte[1024];
                                    while ((length = in.read(buffer)) != -1) {
                                       f.write(buffer, 0, length);
                                    }

                                     /* 
                                     * 文件由ANSI转化为UTF-8
                                              * 需要用到流InputStreamReader和OutputStreamWriter
                                       * 这两个流有charset功能
                                       * */
                                      File srcFile = new File("/storage/emulated/0/Android/data/org.videolan.vlc/files/"+subFileName);
                                      File destFile = new File("/storage/emulated/0/Android/data/org.videolan.vlc/files/"+0+subFileName);
                                    try {
                                        InputStreamReader isr = new InputStreamReader(new FileInputStream(srcFile), "GBK"); //ANSI编码
                                        OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(destFile), "UTF-8"); //存为UTF-8

                                        int len = isr.read();
                                        while(-1 != len)
                                        {

                                            osw.write(len);
                                            len = isr.read();
                                        }
                                        //刷新缓冲区的数据，强制写入目标文件
                                        osw.flush();
                                        osw.close();
                                        isr.close();
                                    }
                                    catch (Exception e) {
                                        e.printStackTrace();
                                    }



                                    Log.e("videoPlayerActivity", "Download sub succeed!");


                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                            }

                        }
                        System.out.println(jsonArrayFilelist);

                    }catch (Exception e){
                        e.printStackTrace();
                    }

                    int low1=result.indexOf("\"url\"");
                    if(low1==-1){
                        return;
                    }
                    int high1=result.indexOf(",",low1);
                    String fileUrl=null;
                    try{
                        fileUrl=result.substring(low1+6,high1);
                        Log.e("1",fileUrl);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
//                    if(fileUrl!=null){
//                        InputStream in = null;
//
//                        HttpURLConnection urlConnection;
//                        FileOutputStream f = null;
//                        //下载字幕文件
//                        try {
//                            String url1=fileUrl.replace("\\","");
//                            System.out.println(url1.substring(1,url1.length()));
//
//                            URL downloadUrl=new URL(url1.substring(1,url1.length()));
//                            System.out.println(downloadUrl.toString());
//
//                            urlConnection = (HttpURLConnection) downloadUrl.openConnection();
//
//
//
//                            String str="//storage//emulated//0//Android//data//123.ass";
//
//                            f = new FileOutputStream(new File("/storage/emulated/0/Android/data/org.videolan.vlc/files/subs/123.ass"));
//                            in = urlConnection.getInputStream();
//                            int length;
//                            byte[] buffer = new byte[1024];
//                            while ((length = in.read(buffer)) != -1) {
//                                f.write(buffer, 0, length);
//                            }
//
//
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//
//                    }
                }
            }
        });
        thread.start();





    }


}

package com.buyhatke.appindexing;
import android.content.Context;
import android.net.Uri;
import android.support.v4.util.ArrayMap;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Map.*;
import java.util.Set;
import static java.net.URLDecoder.decode;
/**
 * Created by devdoot on 11/5/16.
 */
public class AppIndexing {
    private String mTitle;
    private String mDescription;
    private Uri mUri;
    private GoogleApiClient mClient;
    private Uri base;
    private ArrayMap<String,String>mQuerymap;
    private String SLASH = "/";

    public ArrayMap<String,String> getQueryMap(){
        return new ArrayMap<>(mQuerymap);
    }

    public  void setBaseUri(Context activity, String scheme, String host, String path){

        base= Uri.parse("android-app://"+activity.getPackageName()+"/"+scheme+"/"+host+"/"+path) ;
    }

    public  Uri setAppIndex(Context activity,  Uri data,GoogleApiClient client,  String description){
        String mdata=null;
        try {
            mdata = decode(data.toString(),"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        queryextractor(mdata);
        mClient = client;
        String result="";String temp;
        if(mQuerymap!=null){
            Set<Entry<String,String>> set=mQuerymap.entrySet();
            Iterator<Entry<String,String>> iterator=set.iterator();
            while(iterator.hasNext()){
                Entry e=iterator.next();
                temp=e.getKey()+"="+e.getValue()+"&";
                result+=temp;
            }
            result=result.substring(0,result.length()-1);

        }
        mUri= base.buildUpon().appendPath("?"+result).build();
        mTitle=mQuerymap.get("title");
        mDescription=description;
        return mUri;
    }

    public void startAppIndexing(){
        mClient.connect();
        AppIndex.AppIndexApi.start(mClient, getAction());
    }

    public void finishAppIndexing(){
        AppIndex.AppIndexApi.end(mClient,  getAction());
        mClient.disconnect();
    }

    private void queryextractor(String data){
        String Query=data.substring(data.lastIndexOf("?")+1);
        ArrayMap<String,String> query=new ArrayMap<>();
        String key,value;
        while(Query.length()>0){
            key=Query.substring(0,Query.indexOf("="));
            Query=Query.substring(Query.indexOf("=")+1);
            if(Query.contains("&")){
                value=Query.substring(0,Query.indexOf("&"));
                Query=Query.substring(Query.indexOf("&")+1);
            }
            else{
                value=Query;
                Query="";
            }
            query.put(key,value);
        }
        mQuerymap=new ArrayMap<>(query);
    }

    public Action getAction() {
        Thing object = new Thing.Builder()
                .setName(mTitle)
                .setDescription(this.mDescription)
                .setUrl(mUri)
                .build();

        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }
}

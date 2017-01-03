package edgar.com.preapp.adapter;

import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.List;

import edgar.com.preapp.R;

/**
 * Created by wushiquan on 2016/12/30.
 */


public class AppAdapter implements ListAdapter{

    List<HashMap<String,Object>> appInfos;
    Context mContext;

    public AppAdapter(Context mContext, List<HashMap<String,Object>> appInfos) {
        this.appInfos = appInfos;
        this.mContext = mContext;

    }
    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
    }

    @Override
    public int getCount() {
        return appInfos.size();
    }

    @Override
    public Object getItem(int position) {
        return appInfos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    /**
     * 自定义view
     */
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater mInflater = LayoutInflater.from(mContext);
        View infoView = mInflater.inflate(R.layout.item, null);
        ImageView mImageView = (ImageView) infoView.findViewById(R.id.icon);
        TextView mTextView = (TextView) infoView.findViewById(R.id.title);
        TextView indexTv = (TextView)infoView.findViewById(R.id.index);
        Button clostBtn = (Button)infoView.findViewById(R.id.close);


        String title = (String) appInfos.get(position).get("title");
        Drawable icon = (Drawable) appInfos.get(position).get("icon");
        Intent singleIntent = (Intent) appInfos.get(position).get("tag");
        final String packageName = (String) appInfos.get(position).get("packageName");

        infoView.setTag(singleIntent);
        mImageView.setImageDrawable(icon);
        mTextView.setText(title);
        indexTv.setText(String.valueOf(position+1));
        clostBtn.setOnClickListener(new OnClickListener() {
            //根据包名来清除应用进程
            @Override
            public void onClick(View v) {
                ActivityManager mAm;
                mAm = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
                mAm.killBackgroundProcesses(packageName);
                Toast.makeText(mContext, "已清除进程的包名为："+packageName, Toast.LENGTH_SHORT).show();
            }
        });

        //绑定点击事件，用来进行应用间的跳转
        infoView.setOnClickListener(new SingleAppClickListener());
        return infoView;
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    //点击应用的图标启动应用程序
    class SingleAppClickListener implements OnClickListener{
        @Override
        public void onClick(View v) {
            Intent intent = (Intent)v.getTag();
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
                try {
                    mContext.startActivity(intent);
                }
                catch (ActivityNotFoundException e) {
                    Log.w("Recent", "Unable to launch recent task", e);
                }
            }
        }
    }
}
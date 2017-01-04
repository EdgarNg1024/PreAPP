package edgar.com.preapp.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by wushiquan on 2017/1/3.
 */

public class FloatWindowService extends Service {

    /**
     * 用于在线程中创建或移除悬浮窗。
     */
    private Handler handler = new Handler();

    /**
     * 定时器，定时进行检测当前应该创建还是移除悬浮窗。
     */
    private Timer timer;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 开启定时器，每隔0.5秒刷新一次
        if (timer == null) {
            timer = new Timer();
            timer.scheduleAtFixedRate(new RefreshTask(), 0, 2000);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Service被终止的同时也停止定时器继续运行
        timer.cancel();
        timer = null;
    }

    class RefreshTask extends TimerTask {
        @Override
        public void run() {
            MyWindowManager.refresh(getApplication());
            if (MyWindowManager.isWindowShowing()) {
                //打开状态下,判断是否离开B
                if (MyWindowManager.isLeaveB()) {
                    //离开B则移除浮窗
                    MyWindowManager.removeSmallWindow(getApplicationContext());
                }else if (MyWindowManager.isA2B()){
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            MyWindowManager.updatePreAPPName(getApplicationContext());
                        }
                    });
                }
            } else {
                //关闭状态下,判断是否A跳到B
                if (MyWindowManager.isA2B()) {
                    //没有悬浮窗,则创建
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            MyWindowManager.createSmallWindow(getApplicationContext());
                        }
                    });
                }
            }
        }

    }


}
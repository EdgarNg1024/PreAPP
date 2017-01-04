package edgar.com.preapp.service;

import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edgar.com.preapp.R;

public class MyWindowManager {

    /**
     * 小悬浮窗View的实例
     */
    private static FloatWindowSmallView smallWindow;

    /**
     * 上一个APP的参数
     */
    private static HashMap<String, Object> preAPPInfo;

    /**
     * 小悬浮窗View的参数
     */
    private static LayoutParams smallWindowParams;

    /**
     * 用于控制在屏幕上添加或移除悬浮窗
     */
    private static WindowManager mWindowManager;

    /**
     * 用于获取手机可用内存
     */
    private static ActivityManager mActivityManager;

    private static int preAPP_now = -1; //当前的上一个APP
    private static int preAPP_cache = -1;//之前的上一个APP
    private static int APP_now = -1;//当前的APP
    private static int APP_cache = -1;//之前的APP
    private static int home = -1;//首页

//    private static boolean isReturned = false;//是否已经点击返回

    /**
     * 创建一个小悬浮窗。初始位置为屏幕的右部中间位置。
     *
     * @param context 必须为应用程序的Context.
     */
    public static void createSmallWindow(Context context) {
        WindowManager windowManager = getWindowManager(context);
        int screenWidth = windowManager.getDefaultDisplay().getWidth();
        int screenHeight = windowManager.getDefaultDisplay().getHeight();
        if (smallWindow == null) {
            smallWindow = new FloatWindowSmallView(context);
            if (smallWindowParams == null) {
                smallWindowParams = new LayoutParams();
                smallWindowParams.type = LayoutParams.TYPE_PHONE;
                smallWindowParams.format = PixelFormat.RGBA_8888;
                smallWindowParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | LayoutParams.FLAG_NOT_FOCUSABLE;
                smallWindowParams.gravity = Gravity.LEFT | Gravity.TOP;
                smallWindowParams.width = FloatWindowSmallView.viewWidth;
                smallWindowParams.height = FloatWindowSmallView.viewHeight;
                smallWindowParams.x = screenWidth;
                smallWindowParams.y = screenHeight / 2;
            }
            smallWindow.setParams(smallWindowParams);
            windowManager.addView(smallWindow, smallWindowParams);
            updatePreAPPName(context);
        }
    }

    /**
     * 返回上一个APP
     *
     * @param context
     */
    public static void returnToPreAPP(Context context) {
        if (preAPPInfo != null) {
            Intent intent = (Intent) preAPPInfo.get("tag");
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
                try {
                    context.startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Log.w("Recent", "Unable to launch recent task", e);
                }
            }
            removeSmallWindow(context);
        }
    }

    /**
     * 将小悬浮窗从屏幕上移除。
     *
     * @param context 必须为应用程序的Context.
     */
    public static void removeSmallWindow(Context context) {
        if (smallWindow != null) {
            WindowManager windowManager = getWindowManager(context);
            windowManager.removeView(smallWindow);
            preAPPInfo = null;
            smallWindow = null;
            preAPP_cache = preAPP_now;
            APP_cache = APP_now;
        }
    }


    /**
     * 是否从A APP 跳转到B APP
     */
    public static boolean isA2B() {
        if (preAPP_now != preAPP_cache && preAPP_now != home && preAPP_now != -1 ) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 是否从B APP 返回到A APP
     *
     * @return
     */
    public static boolean isBReturnToA() {
        return false;
    }

    /**
     * 更新小悬浮窗的TextView上的数据，显示内存使用的百分比。
     *
     * @param context 可传入应用程序上下文。
     */
    public static void updatePreAPPName(Context context) {
        if (smallWindow != null) {
            TextView percentView = (TextView) smallWindow.findViewById(R.id.percent);
            String showtxt = getPreAPPName(context);
            if (showtxt.length() == 0) {
                removeSmallWindow(context);
            } else {
                percentView.setText("« 返回\"" + showtxt + "\"");
                APP_cache = APP_now;
            }
        }
    }

    /**
     * 是否有悬浮窗(包括小悬浮窗和大悬浮窗)显示在屏幕上。
     *
     * @return 有悬浮窗显示在桌面上返回true，没有的话返回false。
     */
    public static boolean isWindowShowing() {
        return smallWindow != null;
    }


    /**
     * 如果WindowManager还未创建，则创建一个新的WindowManager返回。否则返回当前已创建的WindowManager。
     *
     * @param context 必须为应用程序的Context.
     * @return WindowManager的实例，用于控制在屏幕上添加或移除悬浮窗。
     */
    private static WindowManager getWindowManager(Context context) {
        if (mWindowManager == null) {
            mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        }
        return mWindowManager;
    }

    /**
     * 如果ActivityManager还未创建，则创建一个新的ActivityManager返回。否则返回当前已创建的ActivityManager。
     *
     * @param context 可传入应用程序上下文。
     * @return ActivityManager的实例，用于获取手机可用内存。
     */
    private static ActivityManager getActivityManager(Context context) {
        if (mActivityManager == null) {
            mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        }
        return mActivityManager;
    }

    /**
     * 获取上一个APP的名字,并返回
     *
     * @param context 可传入应用程序上下文。
     * @return 已使用内存的百分比，以字符串形式返回。
     */
    public static String getPreAPPName(Context context) {
        List<HashMap<String, Object>> appInfos = reloadButtons(context, 2);
        if (appInfos != null && appInfos.size() > 1) {
            preAPPInfo = appInfos.get(1);
            preAPP_now = (int) preAPPInfo.get("uid");
            APP_now = (int) appInfos.get(0).get("uid");
            return (String) appInfos.get(1).get("title");
        } else {
            return "";
        }

/*        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> rti = mActivityManager.getRunningTasks(2);
        if (rti != null && rti.size() > 1) {
            return rti.get(1).topActivity.getPackageName();
        } else {
            return "";
        }*/
    }


    /**
     * 核心方法，加载最近启动的应用程序 注意：这里我们取出的最近任务为 MAX_RECENT_TASKS +
     * 1个，因为有可能最近任务中包好Launcher2。 这样可以保证我们展示出来的 最近任务 为 MAX_RECENT_TASKS 个
     * 通过以下步骤，可以获得近期任务列表，并将其存放在了appInfos这个list中，接下来就是展示这个list的工作了。
     */
    public static List<HashMap<String, Object>> reloadButtons(Context context, int appNumber) {
        List<HashMap<String, Object>> appInfos = new ArrayList<>();
        int MAX_RECENT_TASKS = appNumber; // allow for some discards
        int repeatCount = appNumber;// 保证上面两个值相等,设定存放的程序个数

		/* 每次加载必须清空list中的内容 */
        appInfos.removeAll(appInfos);

        // 得到包管理器和activity管理器
        final PackageManager pm = context.getPackageManager();
        final ActivityManager am = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);

        // 从ActivityManager中取出用户最近launch过的 MAX_RECENT_TASKS + 1 个，以从早到晚的时间排序，
        // 注意这个 0x0002,它的值在launcher中是用ActivityManager.RECENT_IGNORE_UNAVAILABLE
        // 但是这是一个隐藏域，因此我把它的值直接拷贝到这里
        try {
            final List<ActivityManager.RecentTaskInfo> recentTasks = am
                    .getRecentTasks(MAX_RECENT_TASKS + 1, 0x0002);


            // 这个activity的信息是我们的launcher
            ActivityInfo homeInfo = new Intent(Intent.ACTION_MAIN).addCategory(
                    Intent.CATEGORY_HOME).resolveActivityInfo(pm, 0);
            int numTasks = recentTasks.size();
            for (int i = 0; i < numTasks && (i < MAX_RECENT_TASKS); i++) {
                HashMap<String, Object> singleAppInfo = new HashMap<String, Object>();// 当个启动过的应用程序的信息
                final ActivityManager.RecentTaskInfo info = recentTasks.get(i);

                Intent intent = new Intent(info.baseIntent);
                if (info.origActivity != null) {
                    intent.setComponent(info.origActivity);
                }
                /**
                 * 如果找到是launcher，直接continue，后面的appInfos.add操作就不会发生了
                 */
                if (homeInfo != null) {
                    if (homeInfo.packageName.equals(intent.getComponent()
                            .getPackageName())
                            && homeInfo.name.equals(intent.getComponent()
                            .getClassName())) {
                        MAX_RECENT_TASKS = MAX_RECENT_TASKS + 1;
                        continue;
                    }
                }
                // 设置intent的启动方式为 创建新task()【并不一定会创建】
                intent.setFlags((intent.getFlags() & ~Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                        | Intent.FLAG_ACTIVITY_NEW_TASK);
                // 获取指定应用程序activity的信息(按我的理解是：某一个应用程序的最后一个在前台出现过的activity。)
                final ResolveInfo resolveInfo = pm.resolveActivity(intent, 0);
                if (resolveInfo != null) {
                    final ActivityInfo activityInfo = resolveInfo.activityInfo;
                    final String title = activityInfo.loadLabel(pm).toString();
                    final int uid = activityInfo.applicationInfo.uid;
                    Drawable icon = activityInfo.loadIcon(pm);

                    if (title != null && title.length() > 0 && icon != null) {
                        singleAppInfo.put("title", title);
                        singleAppInfo.put("icon", icon);
                        singleAppInfo.put("tag", intent);
                        singleAppInfo.put("uid", uid);
                        singleAppInfo.put("packageName", activityInfo.packageName);
                        appInfos.add(singleAppInfo);
                    }
                }
            }
            return appInfos;
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("mmm", e.getMessage());
            return null;
        }
    }

    /**
     * 判断是否离开B
     *
     * @return true则已经离开B, false则还在B那溜达
     */
    public static boolean isLeaveB() {
        return preAPP_now == APP_cache || preAPP_now == -1;
    }

    /**
     * 给preAPP_now,app_now赋值
     *
     * @param context
     */
    public static void refresh(Context context) {
        //判断当前界面以及上一个界面是不是桌面
        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> rti = mActivityManager.getRunningTasks(10);
        List<String> gethomes = getHomes(context);
        if (gethomes.contains(rti.get(0).topActivity.getPackageName())) {
            //当前界面是桌面
            APP_now = -1;
        } else if (gethomes.contains(rti.get(1).topActivity.getPackageName())) {
            //上一个界面是桌面
            preAPP_now = -1;
        } else {
            getPreAPPName(context);
//            updatePreAPPName(context);
        }


    }


    /**
     * 获得属于桌面的应用的应用包名称
     *
     * @return 返回包含所有包名的字符串列表
     */
    private static List<String> getHomes(Context context) {
        List<String> names = new ArrayList<String>();
        PackageManager packageManager = context.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        List<ResolveInfo> resolveInfo = packageManager.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo ri : resolveInfo) {
            names.add(ri.activityInfo.packageName);
        }
        return names;
    }
}

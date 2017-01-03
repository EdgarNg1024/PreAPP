package edgar.com.preapp.ui;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edgar.com.preapp.R;
import edgar.com.preapp.adapter.AppAdapter;

public class APPList extends Activity {

    private ListView list;

    //用来存放每一个recentApplication的信息，我们这里存放应用程序名，应用程序图标和intent。
    private List<HashMap<String, Object>> appInfos = new ArrayList<HashMap<String, Object>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listview);
        list = (ListView) findViewById(R.id.listView);
        //设定显示的最大的任务数为20
        reloadButtons(this, appInfos, 20);

        list.setAdapter(new AppAdapter(this, appInfos));
    }

    /**
     * 核心方法，加载最近启动的应用程序 注意：这里我们取出的最近任务为 MAX_RECENT_TASKS +
     * 1个，因为有可能最近任务中包好Launcher2。 这样可以保证我们展示出来的 最近任务 为 MAX_RECENT_TASKS 个
     * 通过以下步骤，可以获得近期任务列表，并将其存放在了appInfos这个list中，接下来就是展示这个list的工作了。
     */
    public static void reloadButtons(Activity activity, List<HashMap<String, Object>> appInfos, int appNumber) {
        int MAX_RECENT_TASKS = appNumber; // allow for some discards
        int repeatCount = appNumber;// 保证上面两个值相等,设定存放的程序个数

		/* 每次加载必须清空list中的内容 */
        appInfos.removeAll(appInfos);

        // 得到包管理器和activity管理器
        final Context context = activity.getApplication();
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
                    Drawable icon = activityInfo.loadIcon(pm);

                    if (title != null && title.length() > 0 && icon != null) {
                        singleAppInfo.put("title", title);
                        singleAppInfo.put("icon", icon);
                        singleAppInfo.put("tag", intent);
                        singleAppInfo.put("packageName", activityInfo.packageName);
                        appInfos.add(singleAppInfo);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        MAX_RECENT_TASKS = repeatCount;
    }

}

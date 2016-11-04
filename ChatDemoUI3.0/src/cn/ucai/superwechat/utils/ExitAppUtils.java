package cn.ucai.superwechat.utils;

import android.app.Activity;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Administrator on 2016/11/4.
 * 活动链表，添加，删除，退出活动
 */
public class ExitAppUtils {
    List<Activity> mActivityList = new LinkedList<>();
    private static ExitAppUtils instance = new ExitAppUtils();

    private ExitAppUtils() {
    }

    public static ExitAppUtils getInstance() {
        return instance;
    }

    public void addActivity(Activity activity) {
        mActivityList.add(activity);
    }

    public void delActivity(Activity activity) {
        mActivityList.remove(activity);
    }

    public void exitActivity() {
        for (Activity activity : mActivityList) {
            activity.finish();
        }

    }
}

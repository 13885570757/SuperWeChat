package cn.ucai.superwechat.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.hyphenate.easeui.domain.User;

import cn.ucai.superwechat.I;
import cn.ucai.superwechat.R;
import cn.ucai.superwechat.ui.AddContactActivity;
import cn.ucai.superwechat.ui.AddFriendActivity;
import cn.ucai.superwechat.ui.FriendProfileActivity;
import cn.ucai.superwechat.ui.LoginActivity;
import cn.ucai.superwechat.ui.RegisterActivity;
import cn.ucai.superwechat.ui.SettingsActivity;
import cn.ucai.superwechat.ui.UserProfileActivity;

/**
 * Created by Administrator on 2016/11/1.
 */
public class MFGT {
    /**
     * 结束当前活动，添加动画
     * @param activity
     */
    public static void finish(Activity activity){
        activity.finish();
        activity.overridePendingTransition(R.anim.push_right_in,R.anim.push_right_out);
    }

    /**
     * 跳转方法
     * @param context
     * @param cls
     */
    public static void startActivity(Activity context, Class<?> cls) {
        Intent intent = new Intent();
        intent.setClass(context, cls);
        context.startActivity(intent);
        context.overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }
    public static void startActivity(Context context,Intent intent){
        context.startActivity(intent);
        ((Activity)context).overridePendingTransition(R.anim.push_left_in,R.anim.push_left_out);
    }

    /**
     * 跳转到登陆界面
     * @param context
     */
    public static void gotoLoginActivity (Activity context){
      startActivity(context,LoginActivity.class);
    }

    /**
     * 跳转到注册页面
     * @param context
     */
    public static void gotoRegisterActivity(Activity context){
        startActivity(context,RegisterActivity.class);
    }

    /**
     * 跳转到设置
     * @param context
     */
    public static void gotoSettingsActivity(Activity context){
        startActivity(context,SettingsActivity.class);
    }

    /**
     * 跳转到个人信息
     * @param context
     */
    public static void gotoUserProfileActivity(Activity context){
        startActivity(context,UserProfileActivity.class);
    }

    /**
     * 跳转到添加好友页面
     * @param context
     */
    public static void gotoAddFrient(Activity context) {
        startActivity(context, AddContactActivity.class);
    }

    /**
     * 跳转到好友详情
     * @param context
     * @param user
     */
    public static void gotoFriendProfile(Activity context, User user) {
        Intent intent = new Intent();
        intent.setClass(context,FriendProfileActivity.class);
        intent.putExtra(I.User.USER_NAME,user);
        startActivity(context, intent);
    }

    /**
     * 添加好友
     * @param context
     * @param username
     */
    public static void gotoAddFrientMsg(Activity context, String username) {
    Intent intent = new Intent();
        intent.setClass(context, AddFriendActivity.class);
        intent.putExtra(I.User.USER_NAME,username);
        startActivity(context,intent);
    }
}

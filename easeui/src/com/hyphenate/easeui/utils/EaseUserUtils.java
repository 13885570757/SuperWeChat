package com.hyphenate.easeui.utils;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.hyphenate.chat.EMClient;
import com.hyphenate.easeui.R;
import com.hyphenate.easeui.controller.EaseUI;
import com.hyphenate.easeui.controller.EaseUI.EaseUserProfileProvider;
import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.easeui.domain.User;

public class EaseUserUtils {
    private final static String TAG = EaseUserUtils.class.getSimpleName();

    static EaseUserProfileProvider userProvider;

    static {
        userProvider = EaseUI.getInstance().getUserProfileProvider();
    }

    /**
     * get EaseUser according username
     *
     * @param username
     * @return
     */
    public static EaseUser getUserInfo(String username) {
        if (userProvider != null)
            return userProvider.getUser(username);

        return null;
    }

    /**
     * 获取本地服务器用户信息
     *
     * @param username
     * @return
     */
    public static User getAppUserInfo(String username) {
        if (userProvider != null)
            return userProvider.getAppUser(username);

        return null;
    }

    public static User getCurrentAppUserInfo() {
        String username = EMClient.getInstance().getCurrentUser();
        if(userProvider!=null){
            return  userProvider.getAppUser(username);
        }
        return null;
    }

    /**
     * set user avatar
     *
     * @param username
     */
    public static void setUserAvatar(Context context, String username, ImageView imageView) {
        EaseUser user = getUserInfo(username);
        if (user != null && user.getAvatar() != null) {
            try {
                int avatarResId = Integer.parseInt(user.getAvatar());
                Glide.with(context).load(avatarResId).into(imageView);
            } catch (Exception e) {
                //use default avatar
                Glide.with(context).load(user.getAvatar()).diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.drawable.ease_default_avatar).into(imageView);
            }
        } else {
            Glide.with(context).load(R.drawable.ease_default_avatar).into(imageView);
        }
    }

    /**
     * set user's nickname
     */
    public static void setUserNick(String username, TextView textView) {
        if (textView != null) {
            EaseUser user = getUserInfo(username);
            if (user != null && user.getNick() != null) {
                textView.setText(user.getNick());
            } else {
                textView.setText(username);
            }
        }
    }

    /**
     * set user avatar
     *
     * @param username
     */
    public static void setAppUserAvatar(Context context, String username, ImageView imageView) {
        User user = getAppUserInfo(username);
        if (user != null && user.getAvatar() != null) {
            try {
                int avatarResId = Integer.parseInt(user.getAvatar());
                Glide.with(context).load(avatarResId).into(imageView);
            } catch (Exception e) {
                //use default avatar
                Glide.with(context).load(user.getAvatar()).diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.drawable.ease_default_avatar).into(imageView);
            }
        } else {
            Glide.with(context).load(R.drawable.ease_default_avatar).into(imageView);
        }
    }


    /**
     * set app user's nickname
     */
    public static void setAppUserNick(String username, TextView textView) {
        if (textView != null) {
            User user = getAppUserInfo(username);
            Log.e(TAG, "user=" + user);
            if (user != null && user.getMUserNick() != null) {
                textView.setText(user.getMUserNick());
            } else {
                textView.setText(username);
            }
        }
    }


    /**
     * 从本地服务器获取当前用户信息(头像)
     *
     * @param activity
     * @param imageView
     */
    public static void setCurrentAppUserAvatar(FragmentActivity activity, ImageView imageView) {
        String username = EMClient.getInstance().getCurrentUser();
        setAppUserAvatar(activity, username, imageView);
    }

    /**
     * 从本地服务器获取当前用户信息(昵称)
     *
     * @param textView
     */
    public static void setCurrentAppUserNick(TextView textView) {
        String username = EMClient.getInstance().getCurrentUser();
        setAppUserNick(username, textView);

    }

    /**
     * 环信服务器获取微信号
     *
     * @param textView
     */
    public static void setCurrentAppUserName(TextView textView) {
        String username = EMClient.getInstance().getCurrentUser();
        setAppUserName("微信号 ：", username, textView);
    }

    /**
     * 本地服务器获取微信号
     * @param username
     * @param textView
     */
    public static void setAppUserNameWithNo(String username, TextView textView) {
        setAppUserName("微信号 : ",username,textView);
    }

    /**
     * 环信服务器获取微信号
     * @param textView
     */
    public static void setCurrentAppUserNameWithNo(TextView textView) {
        String username = EMClient.getInstance().getCurrentUser();
        setAppUserName("",username,textView);
    }

    private static void setAppUserName(String suffix, String username, TextView textView) {
        textView.setText(suffix + username);
    }

}

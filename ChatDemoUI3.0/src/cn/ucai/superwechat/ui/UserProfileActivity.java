package cn.ucai.superwechat.ui;

import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.hyphenate.EMValueCallBack;
import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.easeui.domain.User;
import com.hyphenate.easeui.utils.EaseImageUtils;
import com.hyphenate.easeui.utils.EaseUserUtils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.ucai.superwechat.I;
import cn.ucai.superwechat.R;
import cn.ucai.superwechat.SuperWeChatHelper;
import cn.ucai.superwechat.bean.Result;
import cn.ucai.superwechat.net.NetDao;
import cn.ucai.superwechat.net.OkHttpUtils;
import cn.ucai.superwechat.utils.CommonUtils;
import cn.ucai.superwechat.utils.L;
import cn.ucai.superwechat.utils.MFGT;
import cn.ucai.superwechat.utils.ResultUtils;

public class UserProfileActivity extends BaseActivity implements OnClickListener {

    private static final String TAG = UserProfileActivity.class.getSimpleName();

    private static final int REQUESTCODE_PICK = 1;
    private static final int REQUESTCODE_CUTTING = 2;
    @Bind(R.id.img_back)
    ImageView imgBack;
    @Bind(R.id.txt_title)
    TextView txtTitle;
    @Bind(R.id.iv_userinfo_avatar)
    ImageView ivUserinfoAvatar;
    @Bind(R.id.tv_userinfo_nick)
    TextView tvUserinfoNick;
    @Bind(R.id.tv_userinfo_name)
    TextView tvUserinfoName;
    private ProgressDialog dialog;
    private RelativeLayout rlNickName;

    User user = null;


    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.em_activity_user_profile);
        ButterKnife.bind(this);
        initView();
        initListener();
        user = EaseUserUtils.getCurrentAppUserInfo();
        L.e(TAG, "===============onCreate：  " + user);
    }

    private void initView() {

        imgBack.setVisibility(View.VISIBLE);
        txtTitle.setVisibility(View.VISIBLE);
        // txtTitle.setText(getString(R.string.titlt_user_profile));
        txtTitle.setText("个人信息");
    }

    private void initListener() {
        EaseUserUtils.setCurrentAppUserAvatar(this, ivUserinfoAvatar);
        EaseUserUtils.setCurrentAppUserNick(tvUserinfoNick);
        EaseUserUtils.setCurrentAppUserName(tvUserinfoName);

    }


    public void asyncFetchUserInfo(String username) {
        SuperWeChatHelper.getInstance().getUserProfileManager().asyncGetUserInfo(username, new EMValueCallBack<EaseUser>() {
            @Override
            public void onSuccess(EaseUser user) {
                if (user != null) {
                    SuperWeChatHelper.getInstance().saveContact(user);
                    if (isFinishing()) {
                        return;
                    }
                    tvUserinfoNick.setText(user.getNick());
                    if (!TextUtils.isEmpty(user.getAvatar())) {
                        Glide.with(UserProfileActivity.this).load(user.getAvatar()).placeholder(R.drawable.em_default_avatar).into(ivUserinfoAvatar);
                    } else {
                        Glide.with(UserProfileActivity.this).load(R.drawable.em_default_avatar).into(ivUserinfoAvatar);
                    }
                }
            }

            @Override
            public void onError(int error, String errorMsg) {
            }
        });
    }

    /**
     * 更新头像
     */
    private void uploadHeadPhoto() {
        Builder builder = new Builder(this);
        builder.setTitle(R.string.dl_title_upload_photo);
        builder.setItems(new String[]{getString(R.string.dl_msg_take_photo), getString(R.string.dl_msg_local_upload)},
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        switch (which) {
                            case 0:
                                Toast.makeText(UserProfileActivity.this, getString(R.string.toast_no_support),
                                        Toast.LENGTH_SHORT).show();
                                break;
                            case 1:
                                Intent pickIntent = new Intent(Intent.ACTION_PICK, null);
                                pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                                startActivityForResult(pickIntent, REQUESTCODE_PICK);
                                break;
                            default:
                                break;
                        }
                    }
                });
        builder.create().show();
    }

    /**
     * 更新昵称
     *
     * @param nickName
     */
    private void updateRemoteNick(final String nickName) {
        dialog = ProgressDialog.show(this, getString(R.string.dl_update_nick), getString(R.string.dl_waiting));
        new Thread(new Runnable() {

            @Override
            public void run() {
                boolean updatenick = SuperWeChatHelper.getInstance().getUserProfileManager().updateCurrentUserNickName(nickName);
                if (UserProfileActivity.this.isFinishing()) {
                    return;
                }
                if (!updatenick) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(UserProfileActivity.this, getString(R.string.toast_updatenick_fail), Toast.LENGTH_SHORT)
                                    .show();
                            dialog.dismiss();
                        }
                    });
                } else {
                    updateAppNick(nickName);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialog.dismiss();
                            Toast.makeText(UserProfileActivity.this, getString(R.string.toast_updatenick_success), Toast.LENGTH_SHORT)
                                    .show();
                            tvUserinfoNick.setText(nickName);
                        }
                    });
                }
            }
        }).start();
    }

    /**
     * 更改本地服务器昵称
     *
     * @param nickName
     */
    private void updateAppNick(String nickName) {
        NetDao.updateUserNick(this, user.getMUserName(), nickName, new OkHttpUtils.OnCompleteListener<String>() {
            @Override
            public void onSuccess(String s) {
                if (s != null) {
                    Result result = ResultUtils.getResultFromJson(s, User.class);
                    if (result != null && result.isRetMsg()) {
                        User u = (User) result.getRetData();
                        updateLocatUser(u);
                    } else {
                        Toast.makeText(UserProfileActivity.this,
                                getString(R.string.toast_updatenick_fail),
                                Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                } else {
                    Toast.makeText(UserProfileActivity.this, getString(R.string.toast_updatenick_fail), Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            }

            @Override
            public void onError(String error) {

            }
        });
    }

    /**
     * 更新本地
     *读取本地保存的用户信息
     * @param u
     */
    private void updateLocatUser(User u) {
        user = u;
        SuperWeChatHelper.getInstance().saveAppContact(u);
        EaseUserUtils.setCurrentAppUserNick(tvUserinfoNick);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUESTCODE_PICK:
                if (data == null || data.getData() == null) {
                    return;
                }
                startPhotoZoom(data.getData());
                break;
            case REQUESTCODE_CUTTING:
                if (data != null) {
                    setPicToView(data);
                    updateAppUserAvatar(data);
                }
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 修改本地头像
     *
     * @param picData
     */
    private void updateAppUserAvatar(final Intent picData) {
        dialog = ProgressDialog.show(this, getString(R.string.dl_update_photo), getString(R.string.dl_waiting));
        dialog.show();
        File file = saveBitmapFile(picData);

        //网络请求
        NetDao.updateAvatar(this, user.getMUserName(), file, new OkHttpUtils.OnCompleteListener<String>() {
            @Override
            public void onSuccess(String s) {
                if (s != null) {
                    Result result = ResultUtils.getResultFromJson(s, User.class);
                    L.i("=========修改头像的结果：  " + result);
                    if (result.isRetMsg() && result != null) {
                        User u = (User) result.getRetData();
                        SuperWeChatHelper.getInstance().saveAppContact(u);//保存头像数据到数据库
                        setPicToView(picData);

                    } else {
                        dialog.dismiss();
                        CommonUtils.showLongToast(result != null ? result.getRetCode() : -1);
                        // CommonUtils.showShortToast(result!=null?R.string.toast_updatephoto_fail);
                    }

                } else {
                    dialog.dismiss();
                    CommonUtils.showShortToast(R.string.toast_updatephoto_fail);
                }
            }

            @Override
            public void onError(String error) {
                dialog.dismiss();
                CommonUtils.showShortToast(R.string.toast_updatephoto_fail);
            }
        });

    }


    /**
     * 好友的头像
     *
     * @param uri
     */
    public void startPhotoZoom(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", true);
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 300);
        intent.putExtra("outputY", 300);
        intent.putExtra("return-data", true);
        intent.putExtra("noFaceDetection", true);
        startActivityForResult(intent, REQUESTCODE_CUTTING);
    }

    /**
     * save the picture data
     *
     * @param picdata
     */
    private void setPicToView(Intent picdata) {
        Bundle extras = picdata.getExtras();
    if (extras != null) {
        Bitmap photo = extras.getParcelable("data");
        Drawable drawable = new BitmapDrawable(getResources(), photo);
        ivUserinfoAvatar.setImageDrawable(drawable);
        dialog.dismiss();
        Toast.makeText(UserProfileActivity.this, getString(R.string.toast_updatephoto_success),
                Toast.LENGTH_SHORT).show();
        //   uploadUserAvatar(Bitmap2Bytes(photo));
    }

}

    /**
     * 环信服务器更新用户头像
     * @param data
     */
    private void uploadUserAvatar(final byte[] data) {
        dialog = ProgressDialog.show(this, getString(R.string.dl_update_photo), getString(R.string.dl_waiting));
        new Thread(new Runnable() {

            @Override
            public void run() {
                final String avatarUrl = SuperWeChatHelper.getInstance().getUserProfileManager().uploadUserAvatar(data);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                        if (avatarUrl != null) {
                            Toast.makeText(UserProfileActivity.this, getString(R.string.toast_updatephoto_success),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(UserProfileActivity.this, getString(R.string.toast_updatephoto_fail),
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });

            }
        }).start();

        dialog.show();
    }


    public byte[] Bitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    @OnClick({R.id.img_back, R.id.layout_user_avatar, R.id.layout_userinfo_nick, R.id.layout_userinfo_name})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                MFGT.finish(this);
                break;
            case R.id.layout_user_avatar://修改头像
                uploadHeadPhoto();
                break;
            case R.id.layout_userinfo_nick:
                final EditText editText = new EditText(this);
                editText.setText(user.getMUserNick());
                new Builder(this).setTitle(R.string.setting_nickname).
                        setIcon(android.R.drawable.ic_dialog_info).setView(editText)
                        .setPositiveButton(R.string.dl_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String nickString = editText.toString();
                                if (TextUtils.isEmpty(nickString)) {
                                    Toast.makeText(UserProfileActivity.this, getString(R.string.toast_nick_not_isnull), Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                if (nickString.equals(user.getMUserNick())) {
                                    CommonUtils.showLongToast(getString(R.string.toast_nick_not_modify));
                               return;
                                }
                                updateRemoteNick(nickString);
                            }
                        }).setNegativeButton(R.string.dl_cancel, null).show();
                break;
            case R.id.layout_userinfo_name:
                CommonUtils.showLongToast(R.string.User_name_cannot_be_modify);
                break;
        }
    }

    private File saveBitmapFile(Intent picdata) {
        Bundle extraxs = picdata.getExtras();
        if (extraxs != null) {
            Bitmap bitmap = extraxs.getParcelable("data");
            String imagePath = EaseImageUtils.getImagePath(user.getMUserName() + I.AVATAR_SUFFIX_JPG);
            File file = new File(imagePath);//图片保存路径
            L.e("===========头像保存路径" + file.getAbsolutePath());
            try {
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
                bos.flush();
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return file;
        }
        return null;
    }
}

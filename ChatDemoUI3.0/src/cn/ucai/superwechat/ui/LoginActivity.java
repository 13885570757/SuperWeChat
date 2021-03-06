/**
 * Copyright (C) 2016 Hyphenate Inc. All rights reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.ucai.superwechat.ui;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.easeui.domain.User;
import com.hyphenate.easeui.utils.EaseCommonUtils;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.ucai.superwechat.R;
import cn.ucai.superwechat.SuperWeChatApplication;
import cn.ucai.superwechat.SuperWeChatHelper;
import cn.ucai.superwechat.bean.Result;
import cn.ucai.superwechat.db.SuperWeChatDBManager;
import cn.ucai.superwechat.db.UserDao;
import cn.ucai.superwechat.net.NetDao;
import cn.ucai.superwechat.net.OkHttpUtils;
import cn.ucai.superwechat.utils.L;
import cn.ucai.superwechat.utils.MD5;
import cn.ucai.superwechat.utils.MFGT;
import cn.ucai.superwechat.utils.ResultUtils;

/**
 * Login screen
 */
public class LoginActivity extends BaseActivity {
    private static final String TAG = "LoginActivity";
    public static final int REQUEST_CODE_SETNICK = 1;
    @Bind(R.id.img_back)
    ImageView imgBack;
    @Bind(R.id.txt_title)
    TextView txtTitle;
    @Bind(R.id.username)
    EditText usernameEditText;
    @Bind(R.id.password)
    EditText passwordEditText;
    @Bind(R.id.login_login_btn)
    Button loginLoginBtn;
    @Bind(R.id.forget_passwd)
    Button forgetPasswd;
    @Bind(R.id.login_register)
    Button loginRegister;

    private boolean progressShow;
    private boolean autoLogin = false;
    String currentPassword;
    String currentUsername;
    ProgressDialog pd;

    LoginActivity mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        // enter the main activity if already logged in
        //判断当前是否为有账号状态
        if (SuperWeChatHelper.getInstance().isLoggedIn()) {
            autoLogin = true;
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            return;
        }
        setContentView(R.layout.em_activity_login);
        ButterKnife.bind(this);
        mContext = this;
        setListener();
        initView();
    }

    // if user changed, clear the password
    //如果用户名变更，清空密码
    private void setListener() {
        usernameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                passwordEditText.setText(null);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        if (SuperWeChatHelper.getInstance().getCurrentUsernName() != null) {
            usernameEditText.setText(SuperWeChatHelper.getInstance().getCurrentUsernName());
        }
    }

    /**
     * 设置标题显示状态
     */
    private void initView() {
        if (SuperWeChatHelper.getInstance().getCurrentUsernName() != null) {
            usernameEditText.setText(SuperWeChatHelper.getInstance().getCurrentUsernName());
        }
        imgBack.setVisibility(View.VISIBLE);
        txtTitle.setVisibility(View.VISIBLE);
        txtTitle.setText(R.string.login);
    }

    /**
     * login方法
     */
    public void login() {
        if (!EaseCommonUtils.isNetWorkConnected(this)) {
            Toast.makeText(this, R.string.network_isnot_available, Toast.LENGTH_SHORT).show();
            return;
        }
        currentUsername = usernameEditText.getText().toString().trim();
        currentPassword = passwordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(currentUsername)) {
            Toast.makeText(this, R.string.User_name_cannot_be_empty, Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(currentPassword)) {
            Toast.makeText(this, R.string.Password_cannot_be_empty, Toast.LENGTH_SHORT).show();
            return;
        }

        progressShow = true;
        pd = new ProgressDialog(LoginActivity.this);
        pd.setCanceledOnTouchOutside(false);
        pd.setOnCancelListener(new OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                Log.d(TAG, "EMClient.getInstance().onCancel");
                progressShow = false;
            }
        });
        pd.setMessage(getString(R.string.Is_landing));
//        pd.show();

        loginEMServer();//登录方法


    }

    private void loginEMServer() {
        // After logout，the DemoDB may still be accessed due to async callback, so the DemoDB will be re-opened again.
        // close it before login to make sure DemoDB not overlap
        SuperWeChatDBManager.getInstance().closeDB();

        // reset current user name before login
        SuperWeChatHelper.getInstance().setCurrentUserName(currentUsername);

        final long start = System.currentTimeMillis();
        // call login method
        Log.d(TAG, "EMClient.getInstance().login");
        EMClient.getInstance().login(currentUsername, MD5.getMessageDigest(currentPassword), new EMCallBack() {

            @Override
            public void onSuccess() {
                Log.d(TAG, "login: onSuccess");
                //成功
                loginAppServer();//登陆本地服务器
            }

            @Override
            public void onProgress(int progress, String status) {
                Log.d(TAG, "login: onProgress");
            }

            @Override
            public void onError(final int code, final String message) {
                Log.d(TAG, "login: onError: " + code);
                if (!progressShow) {
                    return;
                }
                runOnUiThread(new Runnable() {
                    public void run() {
                        pd.dismiss();
                        Toast.makeText(getApplicationContext(), getString(R.string.Login_failed) + message,
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void loginSuccess() {
        // ** manually load all local groups and conversation
        EMClient.getInstance().groupManager().loadAllGroups();
        EMClient.getInstance().chatManager().loadAllConversations();

        // update current user's display name for APNs
        boolean updatenick = EMClient.getInstance().updateCurrentUserNick(
                SuperWeChatApplication.currentUserNick.trim());
        if (!updatenick) {
            Log.e("LoginActivity", "update current user nick fail");
        }

        if (!LoginActivity.this.isFinishing() && pd.isShowing()) {
            pd.dismiss();
        }
        // get user's info (this should be get from App's server or 3rd party service)
        SuperWeChatHelper.getInstance().getUserProfileManager().asyncGetCurrentUserInfo();

        Intent intent = new Intent(LoginActivity.this,
                MainActivity.class);
        startActivity(intent);

        finish();
    }

    /**
     * 登录本地服务器
     */
    private void loginAppServer() {
        NetDao.login(mContext, currentUsername, currentPassword, new OkHttpUtils.OnCompleteListener<String>() {
            @Override
            public void onSuccess(String s) {
                L.i("========loginAppserver" + s);
                if (s!=null&&s!=""){
                    Result result = ResultUtils.getResultFromJson(s, User.class);
                    if (result!=null&&result.isRetMsg()){
                        //保存到数据库
                        User user = (User) result.getRetData();
                        if (user!=null) {
                            UserDao dao = new UserDao(mContext);
                            dao.saveUser(user);
                            //保存当前用户到内存
                            SuperWeChatHelper.getInstance().getCurrentUsernName();
                        }
                    }else {
                        pd.dismiss();
                        //本地服务器失败
                    }
                }
                loginSuccess();
            }

            @Override
            public void onError(String error) {
                L.i("========loginAppserver" + error);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (autoLogin) {
            return;
        }
        //返回刚注册的账号显示在登录账号框中
        if (SuperWeChatHelper.getInstance().getCurrentUsernName()!=null){
            usernameEditText.setText(SuperWeChatHelper.getInstance().getCurrentUsernName());
        }
    }

    @OnClick({R.id.img_back, R.id.login_login_btn, R.id.forget_passwd, R.id.login_register})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_back://返回
                MFGT.finish(this);
                break;
            case R.id.login_login_btn:
                login();
                break;
            case R.id.forget_passwd:
                MFGT.gotoRegisterActivity(this);
                break;
            case R.id.login_register:
                MFGT.gotoRegisterActivity(this);
                break;
        }
    }
}

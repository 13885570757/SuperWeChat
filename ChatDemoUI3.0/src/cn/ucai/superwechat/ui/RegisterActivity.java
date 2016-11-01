/**
 * Copyright (C) 2016 Hyphenate Inc. All rights reserved.
 * <p>
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
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.hyphenate.EMError;
import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.HyphenateException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.ucai.superwechat.I;
import cn.ucai.superwechat.R;
import cn.ucai.superwechat.SuperWeChatHelper;
import cn.ucai.superwechat.bean.Result;
import cn.ucai.superwechat.net.NetDao;
import cn.ucai.superwechat.net.OkHttpUtils;
import cn.ucai.superwechat.utils.L;
import cn.ucai.superwechat.utils.MFGT;

/**
 * register screen
 */
public class RegisterActivity extends BaseActivity {
    @Bind(R.id.reback_btn)
    Button rebackBtn;
    @Bind(R.id.et_username)
    EditText etUsername;
    @Bind(R.id.et_usernick)
    EditText etUsernick;
    @Bind(R.id.et_password)
    EditText etPassword;
    @Bind(R.id.et_repassword)
    EditText etRepassword;
    @Bind(R.id.register)
    Button register;

    String username;
    String usernick;
    String password;
    String repassword;

    RegisterActivity mContext;
    ProgressDialog pd = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.em_activity_register);
        ButterKnife.bind(this);
        mContext = this;
        initview();
    }

    /**
     * 自定义布局Style
     */
    private void initview() {

    }


    /**
     * 判断注册输入值
     */
    public void register() {
        username = etUsername.getText().toString().trim();
        usernick = etUsernick.getText().toString().trim();
        password = etPassword.getText().toString().trim();
        repassword = etRepassword.getText().toString().trim();
        if (TextUtils.isEmpty(username)) {//账号不能为空
            Toast.makeText(this, getResources().getString(R.string.User_name_cannot_be_empty), Toast.LENGTH_SHORT).show();
            etUsername.requestFocus();
            return;

        } else if (!username.matches("[a-zA-Z]\\w{5,15}")) {//账号格式
            Toast.makeText(RegisterActivity.this, "账号格式错误", Toast.LENGTH_SHORT).show();

        } else if (TextUtils.isEmpty(password)) {//密码不能为空
            Toast.makeText(this, getResources().getString(R.string.Password_cannot_be_empty), Toast.LENGTH_SHORT).show();
            etPassword.requestFocus();
            return;
        } else if (TextUtils.isEmpty(repassword)) {//再次输入密码不能为空
            Toast.makeText(this, getResources().getString(R.string.Confirm_password_cannot_be_empty), Toast.LENGTH_SHORT).show();
            etRepassword.requestFocus();
            return;
        } else if (!password.equals(repassword)) {//两次密码不一致
            Toast.makeText(this, getResources().getString(R.string.Two_input_password), Toast.LENGTH_SHORT).show();
            return;
        }


        if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)) {
            pd.setMessage(getResources().getString(R.string.Is_the_registered));
            pd.show();
            registerAppServer();//超级微信服务器


        }
    }

    /**
     * 在服务器上注册
     */
    private void registerAppServer() {
        NetDao.register(mContext, username, usernick, password, new OkHttpUtils.OnCompleteListener<Result>() {
            @Override
            public void onSuccess(Result result) {
                if (result == null) {
                   pd.dismiss();
                    Toast.makeText(RegisterActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
                }else {
                    if (result.isRetMsg()){
                        registerEMServer();//环信服务器上注册
                    }
                    else {
                        if (result.getRetCode()== I.MSG_REGISTER_USERNAME_EXISTS){//存在该用户
                            cn.ucai.superwechat.utils.CommonUtils.showLongToast(result.getRetCode());
                            pd.dismiss();
                        }else {//取消注册
                            unregisterAppServer();
                        }
                    }
                }
            }

            @Override
            public void onError(String error) {
                pd.dismiss();
                L.e("=============注册失败信息" + error);
                Toast.makeText(RegisterActivity.this, "注册失败", Toast.LENGTH_SHORT).show();

            }
        });

    }

    /**
     * 取消注册
     */
    private void unregisterAppServer() {
        NetDao.unregister(mContext, username, new OkHttpUtils.OnCompleteListener<Result>() {
            @Override
            public void onSuccess(Result result) {
                pd.dismiss();
            }

            @Override
            public void onError(String error) {
            pd.dismiss();
            }
        });
    }

    /**
     * 环信服务器上注册
     */
    private void registerEMServer() {

        new Thread(new Runnable() {
            public void run() {
                try {
                    // call method in SDK
                    EMClient.getInstance().createAccount(username, password);
                    runOnUiThread(new Runnable() {
                        public void run() {
                            if (!RegisterActivity.this.isFinishing())
                                pd.dismiss();
                            // save current user
                            SuperWeChatHelper.getInstance().setCurrentUserName(username);
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.Registered_successfully), Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
                } catch (final HyphenateException e) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            if (!RegisterActivity.this.isFinishing())
                                pd.dismiss();
                            int errorCode = e.getErrorCode();
                            if (errorCode == EMError.NETWORK_ERROR) {
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.network_anomalies), Toast.LENGTH_SHORT).show();
                            } else if (errorCode == EMError.USER_ALREADY_EXIST) {
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.User_already_exists), Toast.LENGTH_SHORT).show();
                            } else if (errorCode == EMError.USER_AUTHENTICATION_FAILED) {
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.registration_failed_without_permission), Toast.LENGTH_SHORT).show();
                            } else if (errorCode == EMError.USER_ILLEGAL_ARGUMENT) {
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.illegal_user_name), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.Registration_failed), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        }).start();

    }


    //返回键
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        MFGT.finish(this);
    }

    @OnClick({R.id.reback_btn, R.id.register})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.reback_btn://点击返回，结束当前活动
                MFGT.finish(this);
                break;
            case R.id.register://点击注册
                break;
        }
    }
}

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
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hyphenate.chat.EMClient;
import com.hyphenate.easeui.domain.User;
import com.hyphenate.easeui.widget.EaseAlertDialog;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.ucai.superwechat.R;
import cn.ucai.superwechat.SuperWeChatHelper;
import cn.ucai.superwechat.bean.Result;
import cn.ucai.superwechat.net.NetDao;
import cn.ucai.superwechat.net.OkHttpUtils;
import cn.ucai.superwechat.utils.CommonUtils;
import cn.ucai.superwechat.utils.L;
import cn.ucai.superwechat.utils.MFGT;
import cn.ucai.superwechat.utils.ResultUtils;

public class AddContactActivity extends BaseActivity {
    private final static String TAG = AddContactActivity.class.getSimpleName();
    @Bind(R.id.img_back)
    ImageView imgBack;
    @Bind(R.id.txt_title)
    TextView txtTitle;
    @Bind(R.id.txt_right)
    TextView txtRight;
    @Bind(R.id.et_username)
    EditText etUsername;

    private String toAddUsername;

    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.em_activity_add_contact);
        ButterKnife.bind(this);
        initView();


    }

    private void initView() {
        imgBack.setVisibility(View.VISIBLE);
        txtTitle.setVisibility(View.VISIBLE);
        txtRight.setVisibility(View.VISIBLE);
        txtTitle.setText(getString(R.string.menu_addfriend));
        txtRight.setText(getString(R.string.search));
    }

    public void searchContact() {
        final String name = etUsername.getText().toString().trim();

        toAddUsername = name;
        if (TextUtils.isEmpty(name)) {
            new EaseAlertDialog(this, R.string.Please_enter_a_username).show();
            return;
        }
        progressDialog = new ProgressDialog(this);
        String str = getResources().getString(R.string.addcontact_search);
        progressDialog.setMessage(str);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        searchAppUser();

    }

    /**
     * 查询本地服务器用户
     */
    private void searchAppUser() {
        NetDao.searchUser(this, toAddUsername, new OkHttpUtils.OnCompleteListener<String>() {
            @Override
            public void onSuccess(String s) {
                progressDialog.dismiss();
                if (s != null) {
                    Result result = ResultUtils.getResultFromJson(s, User.class);
                    L.e(TAG, "searchAppuser===result" + result);
                    if (result != null && result.isRetMsg()) {
                        User user = (User) result.getRetData();
                        if (user != null) {//跳转到好友详情
                            MFGT.gotoFriendProfile(AddContactActivity.this, user);
                        }
                    } else {
                        CommonUtils.showShortToast(R.string.msg_104);
                    }
                } else {
                    CommonUtils.showShortToast(R.string.msg_104);
                }
            }

            @Override
            public void onError(String error) {
                L.e(TAG, "searchAppUser===" + error);
                progressDialog.dismiss();
                CommonUtils.showShortToast(R.string.msg_104);
            }
        });
    }

    /**
     * 添加好友
     *
     * @param view
     */
    public void addContact(View view) {
        if (EMClient.getInstance().getCurrentUser().equals(etUsername.getText().toString())) {
            new EaseAlertDialog(this, R.string.not_add_myself).show();
            return;
        }
        if (SuperWeChatHelper.getInstance().getContactList().containsKey(etUsername.getText().toString())) {//let the user know the contact already in your contact list
            if (EMClient.getInstance().contactManager().getBlackListUsernames().contains(etUsername.getText().toString())) {
                new EaseAlertDialog(this, R.string.user_already_in_contactlist).show();
                return;
            }
            new EaseAlertDialog(this, R.string.This_user_is_already_your_friend).show();
            return;
        }

        new Thread(new Runnable() {
            public void run() {
                try {
                    //demo use a hardcode reason here, you need let user to input if you like
                    String s = getResources().getString(R.string.Add_a_friend);
                    EMClient.getInstance().contactManager().addContact(toAddUsername, s);
                    runOnUiThread(new Runnable() {
                        public void run() {
                            progressDialog.dismiss();
                            String s1 = getResources().getString(R.string.send_successful);
                            Toast.makeText(getApplicationContext(), s1, Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (final Exception e) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            progressDialog.dismiss();
                            String s2 = getResources().getString(R.string.Request_add_buddy_failure);
                            Toast.makeText(getApplicationContext(), s2 + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }).start();
    }

    @OnClick({R.id.img_back, R.id.img_right})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                MFGT.finish(this);
                break;
            case R.id.img_right:
                searchContact();
                break;
        }
    }
}

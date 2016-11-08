package cn.ucai.superwechat.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hyphenate.chat.EMClient;
import com.hyphenate.easeui.utils.EaseUserUtils;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.ucai.superwechat.I;
import cn.ucai.superwechat.R;
import cn.ucai.superwechat.utils.L;
import cn.ucai.superwechat.utils.MFGT;

public class AddFriendActivity extends Activity {

    @Bind(R.id.img_back)
    ImageView imgBack;
    @Bind(R.id.txt_title)
    TextView txtTitle;
    @Bind(R.id.btn_send)
    Button btnSend;
    @Bind(R.id.et_msg)
    EditText etMsg;

    private ProgressDialog progressDialog;
    String username;
    String msg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);
        ButterKnife.bind(this);
        username = getIntent().getStringExtra(I.User.USER_NAME);
        if (username==null){
            MFGT.finish(this);
            L.e("===========AddFriend中的用户："+ username);
        }
        initView();

    }

    private void initView() {
        imgBack.setVisibility(View.VISIBLE);
        txtTitle.setVisibility(View.VISIBLE);
        txtTitle.setText(getString(R.string.add_friend));
        btnSend.setVisibility(View.VISIBLE);
        msg = getString(R.string.addcontact_send_msg_prefix)
                + EaseUserUtils.getCurrentAppUserInfo().getMUserNick();
        etMsg.setText(msg);
    }

    @OnClick({R.id.img_back, R.id.btn_send})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                MFGT.finish(this);
                break;
            case R.id.btn_send:
                sendMsh();
                break;
        }
    }

    /**
     * 发送请求
     */
    private void sendMsh() {
        progressDialog = new ProgressDialog(this);
        String str = getResources().getString(R.string.addcontact_adding);
        progressDialog.setMessage(str);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    EMClient.getInstance().contactManager().addContact(username,msg);
                    String s = getResources().getString(R.string.Add_a_friend);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                            String s1 = getResources().getString(R.string.send_successful);
                            Toast.makeText(AddFriendActivity.this, s1, Toast.LENGTH_SHORT).show();
                            MFGT.finish(AddFriendActivity.this);
                        }
                    });

                } catch (Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                            String s2 = getResources().getString(R.string.Request_add_buddy_failure);
                            Toast.makeText(AddFriendActivity.this, s2, Toast.LENGTH_SHORT).show();
                            MFGT.finish(AddFriendActivity.this);
                        }
                    });
                }

            }
        }).start();
    }
}

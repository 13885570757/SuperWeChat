package cn.ucai.superwechat.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.ucai.superwechat.R;
import cn.ucai.superwechat.utils.MFGT;

public class WelcomeActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_welcom);
        ButterKnife.bind(this);
        super.onCreate(savedInstanceState);
    }


    @OnClick({R.id.main_login_btn, R.id.main_regist_btn})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.main_login_btn:
                MFGT.gotoLoginActivity(this);
                break;
            case R.id.main_regist_btn:
                MFGT.gotoRegisterActivity(this);
                break;
        }
    }
}

package cn.ucai.superwechat.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.hyphenate.easeui.domain.User;
import com.hyphenate.easeui.utils.EaseUserUtils;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.ucai.superwechat.I;
import cn.ucai.superwechat.R;
import cn.ucai.superwechat.utils.MFGT;

public class FriendProfileActivity extends BaseActivity {

    @Bind(R.id.img_back)
    ImageView imgBack;
    @Bind(R.id.txt_title)
    TextView txtTitle;
    @Bind(R.id.img_right)
    ImageView imgRight;
    @Bind(R.id.profile_image)
    ImageView profileImage;
    @Bind(R.id.tv_userinfo_nick)
    TextView tvUserinfoNick;
    @Bind(R.id.tv_userinfo_name)
    TextView tvUserinfoName;

    User user = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_profile);
        ButterKnife.bind(this);
        user = (User) getIntent().getSerializableExtra(I.User.USER_NAME);
        if (user==null){
            MFGT.finish(this);
            
        }
        ininView();

    }

    private void ininView() {
        imgBack.setVisibility(View.VISIBLE);
        txtTitle.setVisibility(View.VISIBLE);
        txtTitle.setText(getString(R.string.userinfo_txt_profile));
        setUserInfo();
    }

    /**
     * 获取用户信息
     */
    private void setUserInfo() {
        EaseUserUtils.setAppUserAvatar(this,user.getMUserName(),profileImage);
        EaseUserUtils.setAppUserNick(user.getMUserNick(),tvUserinfoNick);
        EaseUserUtils.setAppUserNameWithNo(user.getMUserNick(),tvUserinfoName);
    }

    @OnClick(R.id.img_back)
    public void onClick() {
        MFGT.finish(this);
    }
}

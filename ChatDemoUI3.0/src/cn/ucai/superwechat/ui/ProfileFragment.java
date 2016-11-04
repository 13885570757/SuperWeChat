package cn.ucai.superwechat.ui;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.easemob.redpacketui.utils.RedPacketUtil;
import com.hyphenate.easeui.utils.EaseUserUtils;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.ucai.superwechat.Constant;
import cn.ucai.superwechat.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {


    @Bind(R.id.iv_profile_avatar)
    ImageView ivProfileAvatar;
    @Bind(R.id.tv_profile_nickname)
    TextView tvProfileNickname;
    @Bind(R.id.tv_profile_username)
    TextView tvProfileUsername;
    @Bind(R.id.layout_profile_view)
    RelativeLayout layoutProfileView;
    @Bind(R.id.tv_profile_money)
    TextView tvProfileMoney;
    @Bind(R.id.tv_profile_setting)
    TextView tvProfileSetting;

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null && savedInstanceState.getBoolean("isConflict", false))
            return;

        setUserInfo();
    }

    /**
     * 设置用户头像昵称用户名
     */
    private void setUserInfo() {
        EaseUserUtils.setCurrentAppUserAvatar(getActivity(), ivProfileAvatar);
        EaseUserUtils.setCurrentAppUserNick(tvProfileNickname);
        EaseUserUtils.setCurrentAppUserName(tvProfileUsername);


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @OnClick({R.id.layout_profile_view, R.id.tv_profile_money, R.id.tv_profile_setting})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.layout_profile_view:
                break;
            case R.id.tv_profile_money: //跳转到钱包
                RedPacketUtil.startChangeActivity(getActivity());
                //结束
                break;
            case R.id.tv_profile_setting:
                break;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (((MainActivity) getActivity()).isConflict) {
            outState.putBoolean("isConflice", true);
        } else if (((MainActivity) getActivity()).getCurrentAccountRemoved()) {
            outState.getBoolean(Constant.ACCOUNT_REMOVED, true);
        }
    }
}

package cn.bmob.push2user.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;

import cn.bmob.push2user.R;
import cn.bmob.push2user.bean.User;
import cn.bmob.v3.BmobUser;

/**
 * Created on 17/8/24 12:51
 * @author zhangchaozhou
 */

public class FlashActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flash);
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                User user = BmobUser.getCurrentUser(User.class);
                if (user == null) {
                    startActivity(new Intent(mContext, LoginActivity.class));
                } else {
                    startActivity(new Intent(mContext, MainActivity.class));
                }
            }
        }, 1000);
    }
}

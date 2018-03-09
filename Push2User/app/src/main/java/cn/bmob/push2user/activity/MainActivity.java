package cn.bmob.push2user.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.orhanobut.logger.Logger;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.bmob.push2user.R;
import cn.bmob.push2user.bean.Installation;
import cn.bmob.push2user.bean.User;
import cn.bmob.v3.BmobInstallation;
import cn.bmob.v3.BmobInstallationManager;
import cn.bmob.v3.BmobPushManager;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.PushListener;
import rx.functions.Action1;

/**
 * @author zhangchaozhou
 */
public class MainActivity extends BaseActivity {

    @BindView(R.id.tv_role)
    TextView mTvRole;
    @BindView(R.id.tv_score)
    TextView mTvScore;
    @BindView(R.id.btn_push)
    Button mBtnPush;

    private User mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mCurrentUser = BmobUser.getCurrentUser(User.class);
        if (mCurrentUser.getRole() == 0) {
            mTvRole.setText("当前用户角色：老师");
            mBtnPush.setVisibility(View.VISIBLE);
            mTvScore.setVisibility(View.GONE);
        } else {
            mTvScore.setText("当前学生分数：" + mCurrentUser.getScore());
            mTvRole.setText("当前用户角色：学生");
            mTvScore.setVisibility(View.VISIBLE);
            mBtnPush.setVisibility(View.GONE);
        }
    }

    @OnClick({R.id.btn_push, R.id.btn_exit})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_push:


                BmobQuery<User> bmobQueryUser = new BmobQuery<>();
                bmobQueryUser.addWhereEqualTo("role",1);
                bmobQueryUser.addWhereLessThan("score",60);
                bmobQueryUser.findObjects(new FindListener<User>() {
                    @Override
                    public void done(List<User> list, BmobException e) {

                        if (e==null){
                            for (User user:list){
                                BmobPushManager bmobPushManager = new BmobPushManager();
                                BmobQuery<BmobInstallation> query = BmobInstallation.getQuery();
                                query.addWhereEqualTo("user", user);
                                bmobPushManager.setQuery(query);
                                bmobPushManager.pushMessage("努力加油", new PushListener() {
                                    @Override
                                    public void done(BmobException e) {
                                        if (e == null) {
                                            Logger.e("推送成功！");
                                        } else {
                                            Logger.e("异常：" + e.getMessage());
                                        }
                                    }
                                });
                            }
                        }else {
                            toastE(e.getMessage());
                        }

                    }
                });

                break;
            case R.id.btn_exit:
                modifyInstallationUser();
                break;

                default:
                    break;
        }
    }


    /**
     * 修改设备表的用户信息：先查询设备表中的数据，再修改数据中用户信息
     */
    private void modifyInstallationUser() {
        BmobQuery<Installation> bmobQuery = new BmobQuery<>();
        final String id = BmobInstallationManager.getInstallationId();
        bmobQuery.addWhereEqualTo("installationId", id);
        bmobQuery.findObjectsObservable(Installation.class)
                .subscribe(new Action1<List<Installation>>() {
                    @Override
                    public void call(List<Installation> installations) {

                        if (installations.size() > 0) {
                            Installation installation = installations.get(0);
                            User user = new User();
                            installation.setUser(user);
                            user.setObjectId("");
                            installation.updateObservable()
                                    .subscribe(new Action1<Void>() {
                                        @Override
                                        public void call(Void aVoid) {
                                            toastI("更新设备用户信息成功！");
                                            /**
                                             * TODO 更新成功之后再退出
                                             */
                                            BmobUser.logOut();
                                            startActivity(new Intent(mContext, LoginActivity.class));
                                            finish();
                                        }
                                    }, new Action1<Throwable>() {
                                        @Override
                                        public void call(Throwable throwable) {
                                            toastE("更新设备用户信息失败：" + throwable.getMessage());
                                        }
                                    });

                        } else {
                            toastE("后台不存在此设备Id的数据，请确认此设备Id是否正确！\n" + id);
                        }

                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        toastE("查询设备数据失败：" + throwable.getMessage());
                    }
                });
    }
}

package cn.bmob.push2user.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.bmob.push2user.R;
import cn.bmob.push2user.bean.Installation;
import cn.bmob.push2user.bean.User;
import cn.bmob.v3.BmobInstallationManager;
import cn.bmob.v3.BmobQuery;
import rx.functions.Action1;

/**
 * Created on 18/3/9 09:50
 *
 * @author zhangchaozhou
 */

public class RegisterActivity extends BaseActivity {
    @BindView(R.id.account)
    AutoCompleteTextView mAccount;
    @BindView(R.id.password)
    EditText mPassword;
    @BindView(R.id.rb_teacher)
    RadioButton mRbTeacher;
    @BindView(R.id.rb_student)
    RadioButton mRbStudent;
    @BindView(R.id.rg_role)
    RadioGroup mRgRole;


    private Integer mRole = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);


        mRgRole.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_teacher:
                        mRole = 0;
                        break;
                    case R.id.rb_student:
                        mRole = 1;
                        break;
                    default:
                        break;
                }
            }
        });
    }

    @OnClick({R.id.account_sign_up_button, R.id.account_login_button})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.account_sign_up_button:

                String account = mAccount.getText().toString().trim();
                if (TextUtils.isEmpty(account)) {
                    toastE("账号不能为空！");
                    return;
                }
                String password = mPassword.getText().toString().trim();
                if (TextUtils.isEmpty(password)) {
                    toastE("密码不能为空！");
                    return;
                }
                User user = new User();

                user.setUsername(account);
                user.setPassword(password);
                user.setRole(mRole);
                if (mRole == 1) {
                    //为了简单理解，学生的分数会在注册的时候自动随机生成，在实际项目中需要老师为学生打分。
                    Random random = new Random();
                    Integer score = random.nextInt(99);
                    user.setScore(score);
                }
                user.signUpObservable(User.class)
                        .subscribe(new Action1<User>() {
                            @Override
                            public void call(User user) {
                                modifyInstallationUser(user);
                                startActivity(new Intent(mContext, MainActivity.class));
                                toastI("注册成功！");
                            }
                        }, new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                toastE("注册异常：" + throwable.getMessage());
                            }
                        });
                break;
            case R.id.account_login_button:
                startActivity(new Intent(mContext, LoginActivity.class));
                finish();
                break;

            default:
                break;
        }
    }


    /**
     * 修改设备表的用户信息：先查询设备表中的数据，再修改数据中用户信息
     *
     * @param user
     */
    private void modifyInstallationUser(final User user) {
        BmobQuery<Installation> bmobQuery = new BmobQuery<>();
        final String id = BmobInstallationManager.getInstallationId();
        bmobQuery.addWhereEqualTo("installationId", id);
        bmobQuery.findObjectsObservable(Installation.class)
                .subscribe(new Action1<List<Installation>>() {
                    @Override
                    public void call(List<Installation> installations) {

                        if (installations.size() > 0) {
                            Installation installation = installations.get(0);
                            installation.setUser(user);
                            installation.updateObservable()
                                    .subscribe(new Action1<Void>() {
                                        @Override
                                        public void call(Void aVoid) {
                                            toastI("更新设备用户信息成功！");
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

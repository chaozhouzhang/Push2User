package cn.bmob.push2user.bean;

import cn.bmob.v3.BmobUser;

/**
 * Created on 17/8/24 13:03
 * @author zhangchaozhou
 */

public class User extends BmobUser {
    private Integer score;
    private Integer role;

    public Integer getScore() {
        return score;
    }

    public User setScore(Integer score) {
        this.score = score;
        return this;
    }

    public Integer getRole() {
        return role;
    }

    public User setRole(Integer role) {
        this.role = role;
        return this;
    }
}

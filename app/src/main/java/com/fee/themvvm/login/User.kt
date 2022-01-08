package com.fee.themvvm.login

import androidx.room.Entity

/**
 ******************(^_^)***********************<br>
 * User: fee(QQ/WeiXin:1176610771)<br>
 * Date: 2021/11/30<br>
 * Time: 22:48<br>
 * <P>DESC:
 * 登录 的 用户实体类
 * </p>
 * ******************(^_^)***********************
 */
@Entity(tableName = "users")
class User {
    var userName: String? = ""

}
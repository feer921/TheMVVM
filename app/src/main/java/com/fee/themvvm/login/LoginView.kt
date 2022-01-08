package com.fee.themvvm.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import common.mvx.v.BaseViewDelegate

/**
 ******************(^_^)***********************<br>
 * User: fee(QQ/WeiXin:1176610771)<br>
 * Date: 2021/11/30<br>
 * Time: 22:01<br>
 * <P>DESC:
 * 框架 demo 之: 登陆 视图, 根据本框架的 思路，登陆视图将可以 随着项目业务的变化，
 * 可以变更放之于任意的 Activity、Fragment、ViewGroup、PopupWindow
 *
 * </p>
 * ******************(^_^)***********************
 */
class LoginView(context: Context): BaseViewDelegate(context) {
    /**
     * 提供 的视图/布局资源 ID
     */
    override fun provideVLayoutRes(): Int {
        TODO("Not yet implemented")
    }

    /**
     * 在该方法回调里初始化 Views
     * @param isInitState 是否为初始化状态,eg.: [Activity]的[onCreate]生命周期方法回调时；
     * @param dataIntent 从其他地方 跳转/路由 过来时所携带的 [Intent]
     * @param extraData 从其他地方 跳转/路由 过来时所携带的 [Bundle]数据； eg.: [Fragment]的初始化
     */
    override fun initViews(isInitState: Boolean, dataIntent: Intent?, extraData: Bundle?) {
        TODO("Not yet implemented")
    }

}
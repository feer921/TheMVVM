package com.fee.themvvm

import android.content.Context
import android.content.Intent
import android.os.Bundle
import common.mvx.v.BaseViewDelegate

/**
 ******************(^_^)***********************<br>
 * User: fee(QQ/WeiXin:1176610771)<br>
 * Date: 2021/6/20<br>
 * Time: 14:59<br>
 * <P>DESC:
 * 主界面的视图层
 * </p>
 * ******************(^_^)***********************
 */
class MainView(context: Context) : BaseViewDelegate(context) {
    /**
     * 提供 的视图/布局资源 ID
     */
    override fun provideVLayoutRes(): Int {
        return R.layout.activity_main
    }

    /**
     * 在该方法回调里初始化 Views
     * @param isInitState 是否为初始化状态,eg.: [Activity]的[onCreate]生命周期方法回调时；
     * @param dataIntent 从其他地方 跳转/路由 过来时所携带的 [Intent]
     * @param extraData 从其他地方 跳转/路由 过来时所携带的 [Bundle]数据； eg.: [Fragment]的初始化
     */
    override fun initViews(isInitState: Boolean, dataIntent: Intent?, extraData: Bundle?) {
    }

}
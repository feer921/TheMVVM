package common.mvx.activity

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import common.mvx.v.IView
import common.mvx.annotations.InvokeStep

/**
 * @author fee
 * <P> DESC:
 * 可为 [MVC]或者 [MVP]或者[MVVM] 框架下的 基类 [Activity]
 * [MVC]框架场景时，[Activity]作为为 [C]层,操作[M] 与[V]
 * [MVP]框架场景时 [Activity]作为[P]层
 * [MVVM]框架场景时，结合[DataBinding]或者[ViewModel]来使用
 * </P>
 */
abstract class BaseViewDelegateActivity<V : IView> : BaseActivity() {

    protected val mViewModule: V? by lazy(mode = LazyThreadSafetyMode.NONE) { provideVModule()?.apply {
        extraLogInfo = theDebugInfo()
        i(null,"--> initViewModule() $extraLogInfo")
        attachLifecycleOwner(this@BaseViewDelegateActivity)
        attachViewModelStoreOwner(this@BaseViewDelegateActivity)
    } }

    /**
     * 获取当前Activity需要填充、展示的内容视图，如果各子类提供，则由基类来填充，如果不提供，各子类也可自行处理
     * @return 当前Activity需要展示的内容视图资源ID
     */
    @InvokeStep(1,desc = "onCreate()")
    override fun getProvideContentViewResID(): Int {
        return 0
    }

    @InvokeStep(2,desc = "onCreate()")
    override fun providedContentView(container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return mViewModule?.onCreateView(null, savedInstanceState)
    }

    /**
     * 如果子类在getProvideContentViewResID()方法提供了视图资源，那么子类的初始化视图可在此方法中完成
     * @step 3
     */
    override fun initViews() {
        if (LIFE_CIRCLE_DEBUG) {
            i(TAG, "--> initViews() mViewModule = $mViewModule ")
        }
        mViewModule?.initViews(true,intent,null)
    }

    /**
     * 子类所提供的 视图[V]层的 对象
     */
    protected abstract fun provideVModule(): V?

    @CallSuper
    override fun onResume() {
        super.onResume()
        mViewModule?.onResume()
    }

    override fun onPause() {
        super.onPause()
        mViewModule?.onPause()
    }
    @CallSuper
    override fun onStop() {
        super.onStop()
        mViewModule?.onStop()
    }

    @CallSuper
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mViewModule?.onSaveInstanceState(outState)
    }

    @CallSuper
    override fun finish() {
        mViewModule?.onHostFinish()
        super.finish()
    }

    @CallSuper
    override fun onDestroy() {
        super.onDestroy()
        mViewModule?.let {
//            it.attachLifecycleOwner(null) ????
//            it.attachViewModelStoreOwner(null)
            it.onDetach()
        }
//        mViewModule = null
    }

    @CallSuper
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mViewModule?.onActivityResult(requestCode, resultCode, data)
    }

    @CallSuper
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        mViewModule?.onConfigurationChanged(newConfig)
    }

    @CallSuper
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        mViewModule?.initViews(false,intent,null)
    }

    protected var isLetViewDelegateConsumeBackPressed = true

    override fun onBackPressed() {
        if (isLetViewDelegateConsumeBackPressed && mViewModule?.onConsumeBackPressed() == true) {
            return
        }
        super.onBackPressed()
    }
}
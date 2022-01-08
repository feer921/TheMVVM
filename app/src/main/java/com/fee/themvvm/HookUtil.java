package com.fee.themvvm;

import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Message;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

import androidx.annotation.NonNull;

/**
 * *****************(^_^)***********************<br>
 * User: fee(QQ/WeiXin:1176610771)<br>
 * Date: 2021/11/29<br>
 * Time: 17:29<br>
 * <P>DESC:
 * Hook AMS 的 Activity的启动
 * </p>
 * ******************(^_^)***********************
 */
public class HookUtil {
    private static final String INTENT_KEY_ORIGIN_INTENT = "intent.key.origin.intent";

    public static void hookAMS() {
        try {
            Field singletonField = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {// >= 8.0
                Class<?> amClass = Class.forName("android.app.ActivityManager");
                /**
                 *  @UnsupportedAppUsage
                 *     private static final Singleton<IActivityManager> IActivityManagerSingleton =
                 *             new Singleton<IActivityManager>() {
                 *                 @Override
                 *                 protected IActivityManager create() {
                 *                     final IBinder b = ServiceManager.getService(Context.ACTIVITY_SERVICE);
                 *                     final IActivityManager am = IActivityManager.Stub.asInterface(b);
                 *                     return am;
                 *                 }
                 *             };
                 */
                //拿到 ActivityManager 类中声明的 静态的 Singleton<IActivityManager> 字段/属性
                singletonField = amClass.getDeclaredField("IActivityManagerSingleton");
            }else {
                Class<?> activityManagerNativeClass = Class.forName("android.app" +
                        ".ActivityManagerNative");
                singletonField = activityManagerNativeClass.getDeclaredField("gDefault");
            }

            singletonField.setAccessible(true);
            // 获取 Singleton<IActivityManager> IActivityManagerSingleton 对象
            Object objSingleton = singletonField.get(null); //因为是静态的
            // 反射拿到 IActivityManager
            Class<?> iActivityManagerClass = Class.forName("android.app.IActivityManager");
            // 反射拿到 Singleton<IActivityManager> 中的 mInstance属性对象
            Class<?> singletonClass = Class.forName("android.util.Singleton");
            /**
             * 拿到  Singleton<T> 类里的  mInstance 属性
             */
            Field mInstanceField = singletonClass.getDeclaredField("mInstance");
            mInstanceField.setAccessible(true);
            // 这里 就拿到了 Singleton<IActivityManager> IActivityManagerSingleton 的 mInstance 对象：也即
            // IActivityManager 原系统内的实例
            Object objInstanceInSingleton = mInstanceField.get(objSingleton);
            Object originIActivityManagerObj = objInstanceInSingleton;
            Object iActivityManagerProxy  =
                    Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                    new Class[]{iActivityManagerClass}, new InvocationHandler() {
                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                            if ("startActivity".equals(method.getName())){
                                //修改 intent, 把插件 Activity的 Intent 修改为启动代理的 Activity
                                int intentParamIndex = -1;
                                for (int i = 0; i < args.length; i++) {
                                    if (args [i] instanceof Intent){ //找到 Intent 参数
                                        intentParamIndex = i;
                                        break;
                                    }
                                }
                                if (intentParamIndex > -1){
                                    // 源 起动目标 Activity的
                                    Intent originIntent = (Intent) args[intentParamIndex];
                                    Intent proxyActivityIntent = new Intent();
                                    proxyActivityIntent.setClassName("com.fee.themvvm", "com.fee" +
                                            ".themvvm.ProxyActivity");
                                    args[intentParamIndex] = proxyActivityIntent;// intent就修改成
                                    // 启动代理Activity的 Intent
                                    //并且把 源 Intent 携带
                                    proxyActivityIntent.putExtra(INTENT_KEY_ORIGIN_INTENT, originIntent);
                                }
                            }
                            return method.invoke(originIActivityManagerObj,args);
                        }
                    });
            // 使用 代理对象 替换 系统的  ActivityManager.getService() --> IActivityManager 对象
            /**
             * 即 要让 ActivityManager 里面的 @UnsupportedAppUsage
             *     public static IActivityManager getService() {
             *         return IActivityManagerSingleton.get();
             *     }
             *     要返回 我们生成的代理对象
             */
            // 把 objSingleton 对象里的 mInstance 对象修改成 我们生成的 代理对象
            mInstanceField.set(objSingleton,iActivityManagerProxy);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public static void hookHandlerInActivityThread(){
        try{ //
//            Accessing internal APIs via reflection is not supported and may not work on all devices or in the future
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Field fieldStaticActivityThread = activityThreadClass.getDeclaredField(
                    "sCurrentActivityThread");
            fieldStaticActivityThread.setAccessible(true);
            //这样就拿到 了  ActivityThread 对象
            Object activityThreadObj = fieldStaticActivityThread.get(null); //静态属性
            //拿到  ActivityThread 中声明的  mH Handler 属性
            Field fieldMHandler = activityThreadClass.getDeclaredField("mH");
            fieldMHandler.setAccessible(true);
            Object mHandlerObj = fieldMHandler.get(activityThreadObj);
            Handler mH = (Handler) mHandlerObj;
            Class<?> handlerClass = Class.forName("android.os.Handler");
            //
//            Reflective access to mCallback, which is not part of the public SDK and therefore likely to change in future Android releases
            Field fieldCallBack = handlerClass.getDeclaredField("mCallback");
            fieldCallBack.setAccessible(true);

            Handler.Callback msgInterceptCallback = new Handler.Callback() {
                @Override
                public boolean handleMessage(@NonNull Message msg) {
                    if (msg.what == 100) {//Android 6.0/ 9.0需要适配
                        Object msgObj = msg.obj; //ActivityClientRecord acr
                        if (msgObj != null) {
                            try {
                                Field fieldIntent = msgObj.getClass().getDeclaredField("intent");
                                fieldIntent.setAccessible(true);
                                Intent proxyIntent = (Intent) fieldIntent.get(msgObj);
                                Intent pluginIntent = proxyIntent.getParcelableExtra(INTENT_KEY_ORIGIN_INTENT);
                                if (pluginIntent != null) {
                                    fieldIntent.set(msgObj,pluginIntent); //将插件的 Activity的 Intent 替换
                                    // 代理Activity的 Intent
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                    } else if (msg.what == 159){//Android 9.0
                        Object objClientTransaction = msg.obj;// ClientTransaction
                        if (objClientTransaction == null) {
                            return false;
                        }
                        Class<?> transactionClass = objClientTransaction.getClass();
                        try {
                            Field fieldActivityCallbacks = transactionClass.getDeclaredField(
                                    "mActivityCallbacks");
                            fieldActivityCallbacks.setAccessible(true);
                            Object callBackListObj =
                                    fieldActivityCallbacks.get(objClientTransaction);
                            List callBackList = (List) callBackListObj;
                            for (int i = 0; i < callBackList.size(); i++) {
                                Object item = callBackList.get(i);
                                Class<?> itemClass = item.getClass();
                                if ("android.app.servertransaction.LaunchActivityItem".equals(itemClass.getName())) {
                                    Field mIntentField = itemClass.getDeclaredField("mIntent");
                                    mIntentField.setAccessible(true);
                                    Object mIntentObj = mIntentField.get(item);
                                    Intent proxyIntent = (Intent) mIntentObj;
                                    //获取插件的 Activity Intent
                                    Intent pluginIntent = proxyIntent.getParcelableExtra(INTENT_KEY_ORIGIN_INTENT);
                                    if (pluginIntent != null) {
                                        mIntentField.set(item, pluginIntent);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    return false; //return false: 不影响系统的原有逻辑
                }
            };
            fieldCallBack.set(mH,msgInterceptCallback);
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}

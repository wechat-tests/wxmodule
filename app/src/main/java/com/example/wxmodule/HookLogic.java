package com.example.wxmodule;

import android.content.ContentValues;

import java.util.HashMap;
import java.util.Map;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookLogic implements IXposedHookLoadPackage {

    private static Map<Long, SaveInsertParams> msgCacheMap = new HashMap<>();

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        XposedBridge.log("HookLogic-->" + " current package:" + lpparam.packageName);
        if("com.example.xposedtestapp".equals(lpparam.packageName)){
            try {
                XposedHelpers.findAndHookMethod("com.example.xposedtestapp.MainActivity", lpparam.classLoader, "getInfo", new XC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                        return "hookCoder，我来自中国!";
                    }
                });
            } catch (Throwable t) {
                XposedBridge.log(t);
            }
        }

        if("com.tencent.mm".equals(lpparam.packageName)){
            try {
                hookInsertMsg(lpparam);
                handleReCallMsg(lpparam);
//                msgHandle(lpparam);
//                insertMsgDAOListener(lpparam);
            } catch (Throwable t) {
                XposedBridge.log(t);
            }
        }
    }

    private void hookInsertMsg(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedBridge.log("HookLogic-->" + "微信插入消息监听中");
        Class<?> classDb = XposedHelpers.findClassIfExists("com.tencent.wcdb.database.SQLiteDatabase", lpparam.classLoader);
        if(classDb == null){
            XposedBridge.log("HookLogic-->" + "没有找到类：com.tencent.wcdb.database.SQLiteDatabase");
        }
        XposedHelpers.findAndHookMethod(classDb, "insertWithOnConflict", String.class, String.class, ContentValues.class, int.class, new XC_MethodHook() {

            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                try{
                    if(param.args[0].equals("message")){
                        XposedBridge.log("HookLogic-->" + "                                 ");
                        XposedBridge.log("HookLogic-->" + "                                 ");
                        XposedBridge.log("HookLogic-->" + "              插入消息            ");
                        XposedBridge.log("HookLogic-->" + "**---------------start-----------");
                        XposedBridge.log("HookLogic-->" + "pm.obj: " + param.thisObject.toString());
                        XposedBridge.log("HookLogic-->" + "pm1: " + param.args[0].toString());
                        XposedBridge.log("HookLogic-->" + "pm2: " + param.args[1].toString());
                        XposedBridge.log("HookLogic-->" + "contentValues:");
                        ContentValues contentValues = (ContentValues) param.args[2];
                        long msgId = 0;
                        String content = "";
                        for(String s : contentValues.keySet()){
                            XposedBridge.log("HookLogic-->" + s + " : " + contentValues.getAsString(s));
                            if(s.equals("content")){
                                contentValues.put(s, contentValues.getAsString(s));
                                content ="xander拦截到，此消息被对方撤回：" + contentValues.getAsString(s);
                            }
                            if(s.equals("msgId")){
                                msgId = contentValues.getAsLong(s);
                            }

                        }
                        XposedBridge.log("HookLogic-->" + "**------------end---------------");
                        XposedBridge.log("HookLogic-->" + "                                 ");

                        try{
                            SaveInsertParams saveInsertParams = new SaveInsertParams();
                            saveInsertParams.paramString1 = param.args[0].toString();
                            saveInsertParams.paramString2 = param.args[1].toString();
                            saveInsertParams.paramContentValues = contentValues;
                            saveInsertParams.content = content;
                            msgCacheMap.put(msgId, saveInsertParams);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }catch (Exception e){
                    XposedBridge.log("hook insertWithOnConflict（）方法异常");
                    e.printStackTrace();
                }
            }
        });
    }

    public void handleReCallMsg(final XC_LoadPackage.LoadPackageParam lpparam) {
       XposedBridge.log("HookLogic-->" + "撤回消息监听中...");
        String database = "com.tencent.wcdb.database.SQLiteDatabase";
        String updateWithOnConflict = "updateWithOnConflict";
        XposedHelpers.findAndHookMethod(database, lpparam.classLoader, updateWithOnConflict, String.class, ContentValues.class, String.class, String[].class, int.class, new XC_MethodHook() {

            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                try {

                    if (param.args[0].equals("message")) {//消息列表
                       XposedBridge.log("HookLogic-->" + "                                   ");
                       XposedBridge.log("HookLogic-->" + "           消息修改                  ");
                       XposedBridge.log("HookLogic-->" + "!!-------------start-------------!!");
                       try{
                           XposedBridge.log("HookLogic-->" + "insertClass:" + param.thisObject.toString());
                           XposedBridge.log("HookLogic-->" + "pm1:" + param.args[0].toString());
                       }catch (Exception e){
                           e.printStackTrace();
                       }

                       XposedBridge.log("HookLogic-->" + "contentValue:");
                        ContentValues contentValues = null;
                        try{
                            contentValues = (ContentValues) param.args[1];
                            long msgId = 0;
                            for (String s : contentValues.keySet()) {
                                if(s.equals("msgId")){
                                    msgId = contentValues.getAsLong(s);
                                }

                                XposedBridge.log("HookLogic-->" + "msgId:" + msgId);
                                if(s.equals("content")){
                                    XposedBridge.log("HookLogic-->" + "msgId:" + msgId + ", content:" + contentValues.getAsString(s) +
                                            "，msgCacheMapValue = " + (msgCacheMap.get(msgId) == null ? "" : msgCacheMap.get(msgId).content));
                                    if(contentValues.getAsString(s) != null
                                            && contentValues.getAsString(s).contains("撤回了一条消息")
                                            && msgCacheMap.get(msgId) != null){
                                        contentValues.put(s, msgCacheMap.get(msgId).content);
                                    }

                                }
                                XposedBridge.log("HookLogic-->" + s + ":" + contentValues.getAsString(s));
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }


                        XposedBridge.log("HookLogic-->" + "!!------------end--------------!!");
                        XposedBridge.log("HookLogic-->" + "                                 ");
                        XposedBridge.log("HookLogic-->" + "                                 ");

                        contentValues = ((ContentValues) param.args[1]);

//                        if (contentValues.getAsInteger("type") == 10000) {
//                            handleMessageRecall(contentValues, param.thisObject);//插入提示消息
//                            param.setResult(1);//return
//                        }
                    }
                } catch (Error | Exception e) {
                   XposedBridge.log("HookLogic--> Exception: handleReCallMsg " + e.getMessage());
                   e.printStackTrace();
                }
            }
        });

    }

    private void handleMessageRecall(ContentValues contentValues, Object storageInsertClazz) {
        long msgId = contentValues.getAsLong("msgId");
        SaveInsertParams msg = msgCacheMap.get(msgId);
        XposedBridge.log("HookLogic-->" + "msgId : " + msgId + ", msg : " + msg);
        if(msg != null){
            Class[] classes = new Class[3];
            classes[0] = String.class;
            classes[1] = String.class;
            classes[2] = ContentValues.class;

            Object[] values = new Object[3];
            values[0] = msg.paramString1;
            values[1] = msg.paramString2;
            values[2] = msg.paramContentValues;
            XposedHelpers.callMethod(storageInsertClazz, "insert", classes, values);
        }
    }
}

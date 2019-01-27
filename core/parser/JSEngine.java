package com.dyh.movienow.core.parser;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.dyh.movienow.bean.ChapterBean;
import com.dyh.movienow.bean.MovieInfoUse;
import com.dyh.movienow.bean.SearchResult;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.mozilla.javascript.ConsString;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 作者：By hdy
 * 日期：On 2018/12/10
 * 时间：At 12:04
 */
public class JSEngine {
    private Class clazz;
    private String allFunctions;
    private volatile static JSEngine engine;
    private String lastResCode = "";
    private MovieInfoUse movieInfoUse;
    private OnFindCallBack<String> movieFindCallBack;
    private OnFindCallBack<List<SearchResult>> searchFindCallBack;
    private OnFindCallBack<List<ChapterBean>> chapterCallBack;
    private volatile static LoadMode loadMode = LoadMode.MOVIE_FIND;

    private enum LoadMode {
        SEARCH,
        MOVIE_FIND,
        CHAPTER
    }

    private JSEngine() {
        this.clazz = JSEngine.class;
        allFunctions = String.format(getAllFunctions(), clazz.getName());//生成js语法
    }

    public static JSEngine getInstance() {
        if (engine == null) {
            synchronized (JSEngine.class) {
                if (engine == null) {
                    engine = new JSEngine();
                }
            }
        }
        return engine;
    }


    public synchronized void parseSearchRes(String res, MovieInfoUse movieInfoUse, OnFindCallBack<List<SearchResult>> searchJsCallBack) {
        loadMode = LoadMode.SEARCH;
        this.searchFindCallBack = searchJsCallBack;
        this.lastResCode = res;
        this.movieInfoUse = movieInfoUse;
        if (!movieInfoUse.getSearchFind().startsWith("js:")) {
            searchJsCallBack.showErr(movieInfoUse.getTitle() + "---搜索结果解析失败！请检查规则");
        } else {
            runScript(getDomScripts(res) + "\n" + movieInfoUse.getSearchFind().replaceFirst("js:", ""));
        }
    }

    public synchronized void parseChapter(String res, MovieInfoUse movieInfoUse, OnFindCallBack<List<ChapterBean>> chapterCallBack) {
        loadMode = LoadMode.SEARCH;
        this.chapterCallBack = chapterCallBack;
        this.lastResCode = res;
        this.movieInfoUse = movieInfoUse;
        if (!movieInfoUse.getChapterFind().startsWith("js:")) {
            chapterCallBack.showErr(movieInfoUse.getTitle() + "---搜索结果解析失败！请检查规则");
        } else {
            runScript(movieInfoUse.getChapterFind().replaceFirst("js:", ""));
        }
    }

    public synchronized void parseMovieFind(String res, MovieInfoUse movieInfoUse, OnFindCallBack<String> callBack) {
        loadMode = LoadMode.MOVIE_FIND;
        this.movieFindCallBack = callBack;
        this.movieInfoUse = movieInfoUse;
        this.lastResCode = res;
        if (!movieInfoUse.getMovieFind().startsWith("js:")) {
            movieFindCallBack.showErr(movieInfoUse.getTitle() + "---搜索结果解析失败！请检查规则");
        } else {
            runScript(getDomScripts(res) + "\n" + movieInfoUse.getMovieFind().replaceFirst("js:", ""));
        }
    }

    /**
     * 将源码直接暴露的js加载进去
     *
     * @param s 源码
     * @return
     */
    private String getDomScripts(String s) {
        if(TextUtils.isEmpty(s)){
            return "";
        }
        if (s.startsWith("[") || s.startsWith("{")) {
            return "";
        }
        Document doc = Jsoup.parse(s);
        if (doc == null) {
            return "";
        }
        Elements elements = doc.getElementsByTag("script");
        if (elements == null || elements.size() < 1) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Element element : elements) {
            if (sb.length() != 0)
                sb.append("\n");
            sb.append(wrapTryScript(element.html()));
        }
        return sb.toString();
    }

    /**
     * 避免出错
     *
     * @param script js
     * @return
     */
    private String wrapTryScript(String script) {
        return "try {" + script + "}catch(err){}";
    }


    /**
     * 供js获取相关信息
     *
     * @return 源码
     */
    @JSAnnotation(returnType = 1)
    public String getResCode() {
        return this.lastResCode;
    }

    /**
     * 供js获取相关信息
     *
     * @return 规则
     */
    @JSAnnotation(returnType = 2)
    public String getRule() {
        if (this.movieInfoUse == null) {
            return "null";
        }
        return JSON.toJSONString(this.movieInfoUse);
    }

    /**
     * 供js回调
     *
     * @param o 要回调的结果
     */
    @JSAnnotation
    public void setMovieFindResult(Object o) {
        Object res = argsNativeObjectAdjust(o);
        if (!(res instanceof String)) {
            if (this.movieFindCallBack != null) {
                this.movieFindCallBack.showErr(movieInfoUse.getTitle() + "---视频解析失败！请检查规则");
            }
            return;
        }
        try {
            if (this.movieFindCallBack != null) {
                this.movieFindCallBack.onSuccess((String) res);
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.movieFindCallBack.showErr(movieInfoUse.getTitle() + "---视频解析失败！请检查规则");
        }
    }

    /**
     * 供js回调
     *
     * @param o 要回调的结果
     */
    @JSAnnotation
    public void setSearchResult(Object o) {
        Object res = argsNativeObjectAdjust(o);
        if (!(res instanceof JSONObject)) {
            if (this.searchFindCallBack != null) {
                this.searchFindCallBack.showErr(movieInfoUse.getTitle() + "---搜索结果解析失败！请检查规则");
            }
            return;
        }
        try {
            JSONArray array = ((JSONObject) res).getJSONArray("data");
            List<SearchResult> results = new ArrayList<>();
            for (int i = 0; i < array.size(); i++) {
                try {
                    SearchResult searchResult = new SearchResult();
                    searchResult.setTitle(array.getJSONObject(i).getString("title"));
                    searchResult.setUrl(array.getJSONObject(i).getString("url"));
                    searchResult.setDesc(this.movieInfoUse.getTitle());
                    searchResult.setType("video");
                    results.add(searchResult);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            this.searchFindCallBack.onSuccess(results);
        } catch (Exception e) {
            e.printStackTrace();
            this.searchFindCallBack.showErr(movieInfoUse.getTitle() + "---搜索结果解析失败！请检查规则");
        }
    }

    /**
     * 供js回调
     *
     * @param o 要回调的结果
     */
    @JSAnnotation
    public void setChapterResult(Object o) {
        Object res = argsNativeObjectAdjust(o);
        if (!(res instanceof JSONObject)) {
            if (this.chapterCallBack != null) {
                this.chapterCallBack.showErr(movieInfoUse.getTitle() + "---解析失败！请检查规则：chapterCallBack is null");
            }
            return;
        }
        try {
            JSONArray array = ((JSONObject) res).getJSONArray("data");
            List<ChapterBean> resList = JSON.parseArray(array.toJSONString(), ChapterBean.class);
            this.chapterCallBack.onSuccess(resList);
        } catch (Exception e) {
            e.printStackTrace();
            this.chapterCallBack.showErr(movieInfoUse.getTitle() + "---解析失败！请检查规则：" + e.toString());
        }
    }

    /**
     * 供js回调
     *
     * @param o 要回调的结果
     */
    @JSAnnotation
    public void setError(Object o) {
        Object res = argsNativeObjectAdjust(o);
        if (res instanceof String) {
            if (loadMode == LoadMode.MOVIE_FIND && movieFindCallBack != null) {
                movieFindCallBack.showErr(movieInfoUse.getTitle() + "---解析失败！请检查规则：" + res);
            } else if (loadMode == LoadMode.SEARCH && searchFindCallBack != null) {
                searchFindCallBack.showErr(movieInfoUse.getTitle() + "---解析失败！请检查规则：" + res);
            } else if (loadMode == LoadMode.CHAPTER && chapterCallBack != null) {
                chapterCallBack.showErr(movieInfoUse.getTitle() + "---解析失败！请检查规则：" + res);
            }
        }
    }

    /**
     * 执行JS
     *
     * @param js js执行代码 eg: "var v1 = getValue('Ta');setValue(‘key’，v1);"
     */
    private synchronized void runScript(String js) {
        String runJSStr = allFunctions + "\n" + js;//运行js = allFunctions + js
        org.mozilla.javascript.Context rhino = org.mozilla.javascript.Context.enter();
        rhino.setOptimizationLevel(-1);
        try {
            Scriptable scope = rhino.initStandardObjects();

            ScriptableObject.putProperty(scope, "javaContext", org.mozilla.javascript.Context.javaToJS(this, scope));//配置属性 javaContext:当前类JSEngine的上下文
            ScriptableObject.putProperty(scope, "javaLoader", org.mozilla.javascript.Context.javaToJS(clazz.getClassLoader(), scope));//配置属性 javaLoader:当前类的JSEngine的类加载器

            rhino.evaluateString(scope, runJSStr, clazz.getSimpleName(), 1, null);
        } finally {
            org.mozilla.javascript.Context.exit();
        }
    }

    /**
     * 通过注解自动生成js方法语句
     */
    private String getAllFunctions() {
        String funcStr = " var ScriptAPI = java.lang.Class.forName(\"%s\", true, javaLoader);\n";
        Class cls = this.getClass();
        for (Method method : cls.getDeclaredMethods()) {
            JSAnnotation an = method.getAnnotation(JSAnnotation.class);
            if (an == null) continue;
            String functionName = method.getName();

            String paramsTypeString = "";//获取function的参数类型
            String paramsNameString = "";//获取function的参数名称
            String paramsNameInvokeString = "";
            Class[] parmTypeArray = method.getParameterTypes();
            if (parmTypeArray != null && parmTypeArray.length > 0) {
                String[] parmStrArray = new String[parmTypeArray.length];
                String[] parmNameArray = new String[parmTypeArray.length];
                for (int i = 0; i < parmTypeArray.length; i++) {
                    parmStrArray[i] = parmTypeArray[i].getName();
                    parmNameArray[i] = "param" + i;
                }
                paramsTypeString = String.format(",[%s]", TextUtils.join(",", parmStrArray));
                paramsNameString = TextUtils.join(",", parmNameArray);
                paramsNameInvokeString = "," + paramsNameString;
            }

            Class returnType = method.getReturnType();
            String returnStr = returnType.getSimpleName().equals("void") ? "" : "return";//是否有返回值

            String methodStr = String.format(" var method_%s = ScriptAPI.getMethod(\"%s\"%s);\n", functionName, functionName, paramsTypeString);
            String functionStr = "";
            if (an.returnType() == 1) {
                //返回字符串
                functionStr = String.format(
                        " function %s(%s){\n" +
                                "    var retStr = method_%s.invoke(javaContext%s);\n" +
                                "    return retStr;\n" +
                                " }\n", functionName, paramsNameString, functionName, paramsNameInvokeString);
            } else if (an.returnType() == 2) {
                //返回对象
                functionStr = String.format(
                        " function %s(%s){\n" +
                                "    var retStr = method_%s.invoke(javaContext%s);\n" +
                                "    var ret = {} ;\n" +
                                "    eval('ret='+retStr);\n" +
                                "    return ret;\n" +
                                " }\n", functionName, paramsNameString, functionName, paramsNameInvokeString);
            } else {
                //非返回对象
                functionStr = String.format(
                        " function %s(%s){\n" +
                                "    %s method_%s.invoke(javaContext%s);\n" +
                                " }\n", functionName, paramsNameString, returnStr, functionName, paramsNameInvokeString);
            }
            funcStr = funcStr + methodStr + functionStr;
        }
        return funcStr;
    }

    /**
     * 参数调整：
     * 存在问题：从js传入的JSON 对象，类型变为 NativeObject；而NativeObject 中的String类型可能被js转为
     * ConsString 类型；用 Gson.toJson(xxx) 处理带有ConsString 类型的数据会出现异常。其中的ConsString
     * 类型的数据转化出来并不是 String 类型，而是一个特殊对象。
     * 解决方案：遍历 NativeObject 对象，将其中的 ConsString 类型的数据转为 String 类型
     *
     * @param input
     * @return
     */
    private Object argsNativeObjectAdjust(Object input) {

        if (input instanceof NativeObject) {
            JSONObject bodyJson = new JSONObject();
            NativeObject nativeBody = (NativeObject) input;
            for (Object key : nativeBody.keySet()) {
                Object value = nativeBody.get(key);

                value = argsNativeObjectAdjust(value);
                try {
                    bodyJson.put((String) key, value);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return bodyJson;
        }

        if (input instanceof NativeArray) {
            JSONArray jsonArray = new JSONArray();
            NativeArray nativeArray = (NativeArray) input;
            for (int i = 0; i < nativeArray.size(); i++) {
                Object value = nativeArray.get(i);
                value = argsNativeObjectAdjust(value);
                jsonArray.add(value);
            }

            return jsonArray;
        }

        if (input instanceof ConsString) {
            return input.toString();
        }
        return input;
    }

    public interface OnFindCallBack<T> {
        void onSuccess(T data);

        void showErr(String msg);
    }

    /**
     * 注解
     */
    @Target(value = ElementType.METHOD)
    @Retention(value = RetentionPolicy.RUNTIME)
    public @interface JSAnnotation {
        int returnType() default 0;//是否返回对象，默认为false 不返回
    }

}

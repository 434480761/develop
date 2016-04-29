package nd.esp.service.lifecycle.utils;



import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nd.esp.service.lifecycle.utils.gson.ObjectUtils;

public class UrlParamParseUtil {
    /** 
     * 去掉url中的路径，留下请求参数部分 
     * @param strURL url地址 
     * @return url请求参数部分 
     */ 
     private static String TruncateUrlPage(String strURL) 
     { 
         String strAllParam=null; 
         String[] arrSplit=null; 
         strURL=strURL.trim(); 
         if(strURL.indexOf("?")>0 && strURL.indexOf("?")<strURL.length()-1) {
             strAllParam = strURL.substring(strURL.indexOf("?")+1);
         }
         return strAllParam; 
     } 
     /** 
      * 解析出url参数中的键值对 
      * 如 "index.jsp?Action=del&id=123"，解析出Action:del,id:123存入map中 
      * @param URL url地址 
      * @return url请求参数部分 
      */ 
     public static Map<String, String> URLRequest(String URL) 
     {
         Map<String, String> mapRequest = new HashMap<String, String>(); 
         String[] arrSplit=null; 
         String strUrlParam=TruncateUrlPage(URL); 
         if(strUrlParam==null) 
         { 
             return mapRequest; 
         } 
         //每个键值为一组 
         arrSplit=strUrlParam.split("[&]"); 
         for(String strSplit:arrSplit) 
         { 
             String[] arrSplitEqual=null; 
             arrSplitEqual= strSplit.split("[=]"); 
             //解析出键值 
             if(arrSplitEqual.length>1) 
             { 
                 List values = Arrays.asList(ArrayUtils.subarray(arrSplitEqual, 1, arrSplitEqual.length));
                 //正确解析 
                 mapRequest.put(arrSplitEqual[0], StringUtils.join(values, "=")); 
             } 
             else 
             { 
                 if(arrSplitEqual[0]!="") 
                 { 
                     //只有参数没有值，不加入 
                     mapRequest.put(arrSplitEqual[0], ""); 
                 } 
             } 
         } 
         return mapRequest; 
     }
     
     public static void main(String[] args) {
         String url = "http://esp-lifecycle.beta.web.sdp.101.com/v0.3/assets/packaging/callback?identifier=1ec651f2-11d5-41d0-a17f-4d26b259a516&target=default&icplayer=null&webp_first=false&status=0&err_msg=%e8%bf%9c%e7%a8%8b+REST+api+%22PATCH+http%3a//betacs.101.com/v0.1/dentries?session=a4b1cf5d-20df-4666-8b09-d0030563b470%22+%e8%bf%94%e5%9b%9e%e5%bc%82%e5%b8%b8%e4%bf%a1%e6%81%af%e3%80%82HTTP+STATUS%3a+400%2c+CODE%3a+CS/DENTRY_NOT_FOUND%2c+MESSAGE%3a+%5binfo=%27%e7%9b%ae%e5%bd%95%e9%a1%b9%e4%b8%8d%e5%ad%98%e5%9c%a8%3apath=/preproduction_content_module_mng/preproduction/com-nd-sdp-module/module-js-lib/highcharts/4.1.9%27%5d+%5buuid=edf5ec9d-4644-426d-879b-d3f18055c495%5d";
         Map<String,String> map = UrlParamParseUtil.URLRequest(url);
         System.out.println(ObjectUtils.toJson(map));
     }
}

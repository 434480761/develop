package nd.esp.service.lifecycle.support.web;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @title request上下文，目前仅支持request对象的存/读
 * @desc
 * @atuh lwx
 * @createtime on 2015年7月27日 上午5:01:08
 */
public class RequestContext {
    
    private ServletContext context;
    private HttpSession session;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private Map<String, Cookie> cookies;
     
    private final static ThreadLocal<RequestContext> contexts = new ThreadLocal<RequestContext>();
     
    /**
     * 初始化请求上下文
     * @param ctx
     * @param req
     * @param res
     */
    public static RequestContext begin(ServletContext ctx, HttpServletRequest req, HttpServletResponse res) {
        RequestContext rc = new RequestContext();
        rc.context = ctx;
        rc.request = req;
        rc.response = res;
        rc.session = req.getSession(false);
        rc.cookies = new HashMap<String, Cookie>();
        Cookie[] cookies = req.getCookies();
        if(cookies != null)
            for(Cookie ck : cookies) {
                rc.cookies.put(ck.getName(), ck);
            }
        contexts.set(rc);
        return rc;
    }
     
    /**
     * 获取当前请求的上下文
     * @return
     */
    public static RequestContext get(){
        return contexts.get();
    }
     
 
    public void end() {
        this.context = null;
        this.request = null;
        this.response = null;
        this.session = null;
        this.cookies = null;
        contexts.remove();
    }
 
    public ServletContext getContext() {
        return context;
    }
 
    public void setContext(ServletContext context) {
        this.context = context;
    }
 
    public HttpSession getSession() {
        return session;
    }
 
    public void setSession(HttpSession session) {
        this.session = session;
    }
 
    public HttpServletRequest getRequest() {
        return request;
    }
 
    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }
 
    public HttpServletResponse getResponse() {
        return response;
    }
 
    public void setResponse(HttpServletResponse response) {
        this.response = response;
    }
 
    public Map<String, Cookie> getCookies() {
        return cookies;
    }
 
    public void setCookies(Map<String, Cookie> cookies) {
        this.cookies = cookies;
    } 
     
}

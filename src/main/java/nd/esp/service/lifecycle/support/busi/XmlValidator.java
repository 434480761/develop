package nd.esp.service.lifecycle.support.busi;

import java.io.IOException;
import java.io.StringReader;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.junit.Assert;
import org.xml.sax.InputSource;

import nd.esp.service.lifecycle.app.LifeCircleApplicationInitializer;
import nd.esp.service.lifecycle.support.Constant;
import nd.esp.service.lifecycle.support.Constant.CSInstanceInfo;
import nd.esp.service.lifecycle.utils.HttpClientUtils;

public class XmlValidator {
	private final static Log LOG = LogFactory.getLog(XmlValidator.class);
	
	/**sdp-package.xml 相对路径头*/
	public final static String REF_PATH="${ref-path}";
	/**sdp-package.xml target节点名称*/
	public final static String PACKAGE_TARGET_NODE="target";
	/**sdp-package.xml 默认target节点名*/
    public final static String PACKAGE_DEFAULT_TARGET_NAME="default";
    /**sdp-package.xml 依赖package的targetName属性*/
    public final static String PACKAGE_TARGET_NAME_ATTRIBUTE="targetName";
    /**sdp-package.xml target节点name属性名称*/
    public final static String PACKAGE_NAME_ATTRIBUTE="name";
	/**sdp-package.xml add节点名称*/
	public final static String PACKAGE_ADD_NODE="add";
	/**sdp-package.xml group节点名称*/
	public final static String PACKAGE_GROUP_NODE="group";
	/**sdp-package.xml src属性名称*/
	public final static String PACKAGE_SRC_ATTRIBUTE="src";
	/**sdp-package.xml type属性名称*/
	public final static String PACKAGE_TYPE_ATTRIBUTE="type";
	
	/**sdp-package.xml type属性值 1.* 2.package*/
	public final static String PACKAGE_TYPE_VALUE[]=new String[]{"*","package"};
	
	
	public static boolean checkPackXmlFile(String path) {
		
		if(StringUtils.isEmpty(path)) {
			return false;
		}
		
		String xmlStr;
		try {
			String header = getRefpathHeader(path);
			if(!header.startsWith(REF_PATH)) {
				header = REF_PATH + header;
			}
			xmlStr = AccessPackXmlFile(path, header);
		} catch (Exception e) {
			LOG.error("下载sdp-package.xml失败：",e);
			return false;
		}
		
		Document doc;
		try {
			doc = buildDoc(xmlStr);
		} catch (JDOMException | IOException e) {
			LOG.error("解析sdp-package.xml失败：",e);
			return false;
		}

        // 获得根路径
        Element root = doc.getRootElement();
        List<Element> children = root.getChildren(PACKAGE_TARGET_NODE);
        if (children == null || children.size() == 0) {
            return false;
        }
        // 检索target节点
        boolean bHasDefaultTarget = false;
        for(Element oneTarget:children) {
            if(oneTarget.getAttributeValue(PACKAGE_NAME_ATTRIBUTE).equals(PACKAGE_DEFAULT_TARGET_NAME)) {
            	bHasDefaultTarget = true;
                break;
            }
        }
        
        for(Element oneTarget:children) {
	        // 非package类型的add节点， key为src，value为add节点Element
	        Map<String,Element> addNodesNormal = new HashMap<String,Element>();
	        // 类型为package的add节点， key为src，value为add节点Element
	        Map<String,Element> addNodesPack = new HashMap<String,Element>();
	        // 获取add节点
	        List<Element> addElementList=oneTarget.getChildren(PACKAGE_ADD_NODE);
	        // 遍历add 节点
	        for(Element add:addElementList){
	            if (null != add) {
	                String type = add.getAttributeValue(PACKAGE_TYPE_ATTRIBUTE);
	                String src = add.getAttributeValue(PACKAGE_SRC_ATTRIBUTE);
	                if(type!=null && type.equals(PACKAGE_TYPE_VALUE[1]) && !src.equals(PACKAGE_TYPE_VALUE[0])) {
	                    addNodesPack.put(src, add);
	                }
	                else {
	                    addNodesNormal.put(src, add);
	                }
	            }
	        }
	        
	        // 获取group节点
	        List<Element> groupElements = oneTarget.getChildren(PACKAGE_GROUP_NODE);
	        // 遍历各个group节点
	        for (Element group : groupElements) {
	            // 获取add节点
	            List<Element> addElements = group.getChildren(PACKAGE_ADD_NODE);
	            for (Element add : addElements) {
	                if(add.getAttributeValue(PACKAGE_TYPE_ATTRIBUTE).equals(PACKAGE_TYPE_VALUE[1])) {
	                    addNodesPack.put(add.getAttributeValue(PACKAGE_SRC_ATTRIBUTE), add);
	                }
	                else {
	                    addNodesNormal.put(add.getAttributeValue(PACKAGE_SRC_ATTRIBUTE), add);
	                }
	            }
	        }
	        
	        for(String src:addNodesNormal.keySet()) {
	            if (!src.equals(PACKAGE_TYPE_VALUE[0])) { // 不是* 
	                LOG.info("解析[" + path
	                        + "]目录下的sdp-package.xml中add节点的src不为*,而是[" + src
	                        + "]");
	                
	                if(StringUtils.isEmpty(src)) {
	                	LOG.error("解析[" + path
	                            + "]目录下的sdp-package.xml中有异常add节点的src为空");
	                    return false;
	                }
	                String subHeader = "";
	                String srcPath = "";
	                boolean isXmlRefPath = false;
	                if(src.startsWith("${ref-path")) {
	                    subHeader = getRefpathHeader(src);
	                    if(!Constant.CS_INSTANCE_MAP.containsKey(subHeader)){
	                        LOG.error("解析[" + path
	                                + "]目录下的sdp-package.xml中包含不能处理的实例:"+subHeader+";src="+src);
	                        return false;
	                    }
	                }
	                
	            }
	        }
	        
	        for(String src:addNodesPack.keySet()) {
			    LOG.info(path+"--package:" + src);
			    if(StringUtils.isEmpty(src) || !(src.contains("/") && src.lastIndexOf('/')>0)) {
			    	LOG.error("解析[" + path
	                        + "]目录下的sdp-package.xml中有异常add节点的src为:" + src);
			    	return false;
	            }
	            String subHeader = getRefpathHeader(src);
	            if(!Constant.CS_INSTANCE_MAP.containsKey(subHeader)){
	            	LOG.error("解析[" + path
	                        + "]目录下的sdp-package.xml中包含不能处理的实例:"+subHeader+";src="+src);
	            	return false;
	            }
	        }
        }
		
		return true;
	}
	
	public static String AccessPackXmlFile(String path,String header) throws Exception{
		
		path = replaceRefpath(path);
		
		CSInstanceInfo info = Constant.CS_INSTANCE_MAP.get(header);

        String url = Constant.CS_INSTANCE_MAP.get(header).getUrl()+"/"
                +SessionUtil.createSession("777", Constant.CS_INSTANCE_MAP.get(header).getUrl(),
                		Constant.CS_INSTANCE_MAP.get(header).getPath(),
                		Constant.CS_INSTANCE_MAP.get(header).getServiceId())
                +"/static"+path+"/sdp-package.xml";

        String xmlFile = null;
        try{
            xmlFile = HttpClientUtils.httpGet(url);
        } catch (Exception e) {
            LOG.info(path+"未取得打包信息xml:"+e.getMessage());
        }
        return xmlFile;
    }
	
	/**
	 * @desc 文件下载地址路径做decodeUrl处理并截取掉${ref-path} 及 ${ref-path-addon}
	 * @param downloadPath
	 *            文件下载地址路径
	 * @return 替换后的path
	 * @author qil
	 */
	private static String replaceRefpath(String downloadPath) {
		Assert.assertNotNull("downloadPath不能为空", downloadPath);
		String path = "";
        try {
            path = URLDecoder.decode(downloadPath, "UTF-8");
        } catch(Exception e) {
            LOG.error(e.getMessage());
        }
		if(path.contains(REF_PATH)) {
		    path = path.replace(REF_PATH, "");
		}
		
		return path;
	}
	
	/**
     * @desc 文件下载地址路径截取头部
     * @param downloadPath
     *            文件下载地址路径
     * @return 路径头部
     * @author qil
     */
    private static String getRefpathHeader(String downloadPath) {
        return downloadPath.substring(0, downloadPath.indexOf('/', downloadPath.indexOf('/')+1));
    }
	
	/**
	 * @desc 创建document对象
	 * @param xmlStr
	 * @return
	 * @throws JDOMException
	 * @throws IOException
	 * @author qil
	 */
	public static Document buildDoc(String xmlStr) throws JDOMException, IOException {
		Assert.assertNotNull("xml不能为空", xmlStr);
		StringReader read = new StringReader(xmlStr);
		// 创建新的输入源SAX 解析器将使用 InputSource 对象来确定如何读取 XML 输入
		InputSource source = new InputSource(read);
		// 创建一个新的SAXBuilder
		SAXBuilder sb = new SAXBuilder();
		// 通过输入源构造一个Document
		Document doc = sb.build(source);
		return doc;

	}
	
	public static void main(String[] args) {

		String str = "/edu/esp/coursewareobjects/c0901f33-2be5-4993-b845-342b6bdd155c.pkg";
		
		if(checkPackXmlFile(str)) {
			System.out.println("校验通过： "+str);
		}

	}
}

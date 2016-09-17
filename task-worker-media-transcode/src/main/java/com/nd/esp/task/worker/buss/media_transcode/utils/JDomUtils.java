package com.nd.esp.task.worker.buss.media_transcode.utils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URLDecoder;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.nd.esp.task.worker.buss.media_transcode.Constant;
import com.nd.esp.task.worker.buss.media_transcode.Constant.CSInstanceInfo;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.junit.Assert;
import org.springframework.stereotype.Component;
import org.xml.sax.InputSource;

/**
 * @title xml解析工具--jdom
 * @Desc TODO
 * @author liuwx
 * @version 1.0
 * @create 2015年4月23日 上午11:16:02
 */
@Component
public class JDomUtils {

	private final Log LOG = LogFactory.getLog(JDomUtils.class);
	
	public final String RULE_NODE="rule";
	public final String FILE_TYPE_NODE="fileType";
	public final String COMMAND_NODE="command";
	
	
	/**sdp-package.xml target节点名称*/
	public final String PACKAGE_TARGET_NODE = "target";
	
	/**sdp-package.xml 默认target节点名*/
    public final String PACKAGE_DEFAULT_TARGET_NAME="default";
    /**sdp-package.xml 依赖package的targetName属性*/
    public final String PACKAGE_TARGET_NAME_ATTRIBUTE="targetName";
    /**sdp-package.xml target节点name属性名称*/
    public final String PACKAGE_NAME_ATTRIBUTE="name";
	/**sdp-package.xml add节点名称*/
	public final String PACKAGE_ADD_NODE="add";
	/**sdp-package.xml group节点名称*/
	public final String PACKAGE_GROUP_NODE="group";
	/**sdp-package.xml src属性名称*/
	public final String PACKAGE_SRC_ATTRIBUTE="src";
	/**sdp-package.xml type属性名称*/
	public final String PACKAGE_TYPE_ATTRIBUTE="type";
	/**sdp-package.xml dest属性名称*/
    public final String ADDNODE_DEST_ATTRIBUTE="dest";
    /**sdp-package.xml dest属性名称*/
    public final String ADDNODE_UNZIP_ATTRIBUTE="unzip";
	
	/**sdp-package.xml type属性值 1.* 2.package*/
	public final String PACKAGE_TYPE_VALUE[]=new String[]{"*","package"};
	
	/**sdp-package.xml 相对路径头*/
	public final String REF_PATH="${ref-path}";
	
	/**sdp-package.xml addon相对路径头*/
    public final String REF_PATH_ADDON="${ref-path-addon}";
	
	/**sdp-package.xml 相对压缩包路径*/
    public final String PACK_REF_PATH="/_ref";
	
	

	public static void main(String[] args) {

		String str = "${ref-path}/esp/demo/1eab4d2b-e292-4695-b2bb-2d753f152afb.png";
		System.out.println(str.replace("${ref-path}", ""));

	}

	/**
	 * @desc 文件下载地址路径做decodeUrl处理并截取掉${ref-path} 及 ${ref-path-addon}
	 * @param downloadPath
	 *            文件下载地址路径
	 * @return 替换后的path
	 * @author liuwx
	 */
	private String replaceRefpath(String downloadPath) {
		Assert.assertNotNull("downloadPath不能为空", downloadPath);
		String path = "";
        try {
            path = URLDecoder.decode(downloadPath, "UTF-8");
        } catch(Exception e) {
            LOG.error(e.getMessage());
        }
		if(path.contains(REF_PATH)) {
		    return path.replace(REF_PATH, "");
		} else {
		    return path.replace(REF_PATH_ADDON, "");
		}
	}
	
	/**
     * @desc 文件下载地址路径截取头部
     * @param downloadPath
     *            文件下载地址路径
     * @return 路径头部
     * @author qil
     */
    private String getRefpathHeader(String downloadPath, String addonInstance) {
        Assert.assertNotNull("downloadPath不能为空", downloadPath);
        if(downloadPath.contains(REF_PATH_ADDON)) {
            return addonInstance;
        } else {
            return downloadPath.substring(0, downloadPath.indexOf('/', downloadPath.indexOf('/')+1));
        }
    }

    public void analyzeStrategy(String xmlPath, Map<String,String> args) throws Exception {
        Document doc = buildDoc(xmlPath);
        LOG.info(doc.getBaseURI());
        
        String srcFormat = args.get("sourceFormat");
        // 获得根路径
        Element root = doc.getRootElement();
        List<Element> children = root.getChildren(RULE_NODE);
        if (children == null || children.size() == 0) {
            throw new Exception("rule节点获取失败");
        }
        
        for(Element oneRule:children) {
            Element command = oneRule.getChild(COMMAND_NODE);
            if (children == null) {
                throw new Exception("command节点获取失败");
            }
        }
    }
    
    /**
     * @desc 生成打包路径清单
     * @param path
     *            文件打包的相对地址
     * @param xmlStr
     * @return
     * @author qil
     * @throws Exception
     */
//    public void generatorPackPath(Map<String, Map<String, String>> pathsGrpMap,
//            String path, String xmlStr, String target, boolean isInit, String rootPath, String header, 
//            Map<String,CSInstanceInfo> instanceMap, Set<String> unzipFiles, String addonInstance) throws Exception {
//        Assert.assertNotNull("path路径不能为空", path);
//        Assert.assertNotNull("rootPath路径不能为空", rootPath);
//        LOG.info("当前遍历路径path="+path);
//        LOG.info("根路径rootPath="+rootPath);
//        
//        if(isInit) {
//            xmlStr = AccessXmlFile(path, header, instanceMap, addonInstance);
//            String id = path.substring(path.lastIndexOf('/')+1)+System.currentTimeMillis();
//            String zipFileTempDir = FileUtils.getTempDirectoryPath().endsWith(File.separator)?
//                    FileUtils.getTempDirectoryPath() + "lifecircle" + File.separator + "temp" :
//                    FileUtils.getTempDirectoryPath() + File.separator + "lifecircle" + "temp";
//            String filePath = zipFileTempDir+"/xmlFile/"+id+".xml";
//            File file = new File(filePath);
//            FileUtils.writeStringToFile(file, xmlStr);
//            LOG.info(id+"取得打包信息xml,存于"+filePath);
//        }
//        if(StringUtils.isEmpty(xmlStr)){//如果sdp-package.xml不存在,则直接打包根目录下的所有文件
//            if(!StringUtils.isEmpty(target) && !target.equals(PackageUtil.TARGET_DEFALUT) && isInit) {
//                throw new Exception("目标target节点:"+target+"获取失败");
//            }
//            if(!pathsGrpMap.containsKey(header)) {
//                pathsGrpMap.put(header, new HashMap<String, String>());
//            }
//            if(isInit){
//                pathsGrpMap.get(header).put(path, "/");
//            }
//            else {
//                pathsGrpMap.get(header).put(path, PACK_REF_PATH+path);
//            }
//            return;
//        }
//        
//        Document doc =buildDoc(xmlStr);
//        LOG.info(doc.getBaseURI());
//        // 获得根路径
//        Element root = doc.getRootElement();
//        List<Element> children = root.getChildren(PACKAGE_TARGET_NODE);
//        if (children == null || children.size() == 0) {
//            throw new Exception("target节点获取失败");
//        }
//        // 检索target节点
//        Element destTarget = null;
//        if(StringUtils.isEmpty(target)) {
//            target = PACKAGE_DEFAULT_TARGET_NAME;
//        }
//        for(Element oneTarget:children) {
//            if(oneTarget.getAttributeValue(PACKAGE_NAME_ATTRIBUTE).equals(target)) {
//                destTarget = oneTarget;
//                break;
//            }
//        }
//        if (destTarget == null) {
//            if(isInit) {
//                throw new Exception("目标target节点:"+target+"获取失败");
//            } else {
//                LOG.error("子节点目标target节点:"+target+"获取失败");
//            }
//        }
//        // 非package类型的add节点， key为src，value为add节点Element
//        Map<String,Element> addNodesNormal = new HashMap<String,Element>();
//        // 类型为package的add节点， key为src，value为add节点Element
//        Map<String,Element> addNodesPack = new HashMap<String,Element>();
//        // 获取add节点
//        List<Element> addElementList=destTarget.getChildren(PACKAGE_ADD_NODE);
//        // 遍历add 节点
//        for(Element add:addElementList){
//            if (null != add) {
//                String type = add.getAttributeValue(PACKAGE_TYPE_ATTRIBUTE);
//                String src = add.getAttributeValue(PACKAGE_SRC_ATTRIBUTE);
//                if(type!=null && type.equals(PACKAGE_TYPE_VALUE[1]) && !src.equals(PACKAGE_TYPE_VALUE[0])) {
//                    addNodesPack.put(src, add);
//                }
//                else {
//                    addNodesNormal.put(src, add);
//                }
//            }
//        }
//        
//        // 获取group节点
//        List<Element> groupElements = destTarget.getChildren(PACKAGE_GROUP_NODE);
//        // 遍历各个group节点
//        for (Element group : groupElements) {
//            // 获取add节点
//            List<Element> addElements = group.getChildren(PACKAGE_ADD_NODE);
//            for (Element add : addElements) {
//                if(add.getAttributeValue(PACKAGE_TYPE_ATTRIBUTE).equals(PACKAGE_TYPE_VALUE[1])) {
//                    addNodesPack.put(add.getAttributeValue(PACKAGE_SRC_ATTRIBUTE), add);
//                }
//                else {
//                    addNodesNormal.put(add.getAttributeValue(PACKAGE_SRC_ATTRIBUTE), add);
//                }
//            }
//        }
//        
//        for(String src:addNodesNormal.keySet()) {
//            if (src.equals(PACKAGE_TYPE_VALUE[0])) {
//                if(!pathsGrpMap.containsKey(header)) {
//                    pathsGrpMap.put(header, new HashMap<String, String>());
//                }
//                String dest = addNodesNormal.get(src).getAttributeValue(ADDNODE_DEST_ATTRIBUTE);
//                if(StringUtils.isNotEmpty(dest)) {
//                    pathsGrpMap.get(header).put(path, dest);
//                } else if(path.equals(rootPath)){
//                    pathsGrpMap.get(header).put(path, "/");
//                } else{
//                    pathsGrpMap.get(header).put(path, PACK_REF_PATH+path);
//                }
//            } else {// 不是* 
//                LOG.info("解析[" + path
//                        + "]目录下的sdp-package.xml中add节点的src不为*,而是[" + src
//                        + "]");
//                
//                if(StringUtils.isEmpty(src)) {
//                    throw new Exception("解析[" + path
//                            + "]目录下的sdp-package.xml中有异常add节点的src为空");
//                }
//                String subHeader = "";
//                String srcPath = "";
//                boolean isXmlRefPath = false;
//                if(!src.startsWith("${ref-path")) { //相对xml文件路径
//                    subHeader = header;
//                    srcPath = path+"/"+src;
//                    isXmlRefPath = true;
//                } else {
//                    subHeader = getRefpathHeader(src, addonInstance);
//                    if(!instanceMap.containsKey(subHeader)){
//                        String errInfo = "解析[" + path
//                                + "]目录下的sdp-package.xml中包含不能处理的实例:"+subHeader+";src="+src;
//                        throw new Exception(errInfo);
//                    }
//                    srcPath = replaceRefpath(src);
//                }
//                // 直接加入到制定目录下
//                if(!pathsGrpMap.containsKey(subHeader)) {
//                    pathsGrpMap.put(subHeader, new HashMap<String, String>());
//                }
//                String dest = addNodesNormal.get(src).getAttributeValue(ADDNODE_DEST_ATTRIBUTE);
//                String unzip = addNodesNormal.get(src).getAttributeValue(ADDNODE_UNZIP_ATTRIBUTE);
//                if(StringUtils.isNotEmpty(dest)) {
//                    if(StringUtils.isNotEmpty(unzip) && unzip.equals("true")) {
//                        dest = (dest.endsWith("/") ? dest+srcPath.substring(srcPath.lastIndexOf("/")+1) 
//                                : dest+srcPath.substring(srcPath.lastIndexOf("/")));
//                    }
//                    pathsGrpMap.get(subHeader).put(srcPath, dest);
//                } else if(isXmlRefPath && isInit) {
//                    pathsGrpMap.get(subHeader).put(srcPath, "/");
//                } else {
//                    pathsGrpMap.get(subHeader).put(srcPath, 
//                            PACK_REF_PATH+srcPath.substring(0, srcPath.lastIndexOf("/")));
//                }
//                if(StringUtils.isNotEmpty(unzip) && unzip.equals("true")) {
//                    unzipFiles.add(pathsGrpMap.get(subHeader).get(srcPath));
//                }
//            }
//        }
//        
//        // 如果是package目录 则继续检索
//        ExecutorService executorService = Executors.newFixedThreadPool(15);
//        int index = 0;
//        List<Callable<DownloadXmlThread>> tasks = new ArrayList<Callable<DownloadXmlThread>>();
//        for(String src:addNodesPack.keySet()) {
//		    LOG.info(path+"--package:" + src);
//		    if(StringUtils.isEmpty(src) || !(src.contains("/") && src.lastIndexOf('/')>0)) {
//                throw new Exception("解析[" + path
//                        + "]目录下的sdp-package.xml中有异常add节点的src为:" + src);
//            }
//            String subHeader = getRefpathHeader(src, addonInstance);
//            if(!instanceMap.containsKey(subHeader)){
//                String errInfo = "解析[" + path
//                        + "]目录下的sdp-package.xml中包含不能处理的实例:"+subHeader+";src="+src;
//                throw new Exception(errInfo);
//            }
//            String srcPath = replaceRefpath(src);
//            DownloadXmlThread xmlThread=new DownloadXmlThread(srcPath, subHeader, 
//                    addNodesPack.get(src).getAttributeValue(PACKAGE_TARGET_NAME_ATTRIBUTE), instanceMap, addonInstance);
//            tasks.add(xmlThread);
//            ++index;
//        }
//        List<Future<DownloadXmlThread>> results = executorService.invokeAll(tasks);
//
//        index = 0;
//        for(Future<DownloadXmlThread> res:results) {
//            DownloadXmlThread thread = res.get();
//            String src = thread.getSrc();
//            String subHeader = thread.getHeader();
//            generatorPackPath(pathsGrpMap,src,thread.getStrXml(),thread.getTarget(),false,rootPath,
//                    subHeader,instanceMap,unzipFiles,addonInstance);
//            ++index;
//        }
//    }
    
    class DownloadXmlThread implements Callable<DownloadXmlThread> {

        private final String src;
        private final String header;
        private final String target;
        private final String addonInstance;
        private final Map<String,CSInstanceInfo> instanceMap;
        private String strXml = "";

        

        DownloadXmlThread(String src, String header, String target, Map<String,CSInstanceInfo> instanceMap, String addonInstance) {
            this.header = header;
            this.src = src;
            this.target = target;
            this.instanceMap = instanceMap;
            this.addonInstance = addonInstance;
        }

        public DownloadXmlThread call(){
            // todo 调用packageService重新获取
            try {
                setStrXml(AccessXmlFile(src,header,instanceMap,addonInstance));
                
            } catch (Exception e) {
                LOG.info(e.getMessage());
            }

            return this;
        }

        public String getStrXml() {
            return strXml;
        }

        public void setStrXml(String strXml) {
            this.strXml = strXml;
        }
        
        public String getSrc() {
            return src;
        }
        
        public String getHeader() {
            return header;
        }

        public String getTarget() {
            return target;
        }

    }

	public void test() throws Exception {
		SAXBuilder saxBuilder = new SAXBuilder();
		Document doc = saxBuilder.build(new File("sdp-package.xml"));
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("sdp-package.xml")
				.getFile());
	}

	/**
	 * @desc 创建document对象
	 * @param xmlStr
	 * @return
	 * @throws JDOMException
	 * @throws IOException
	 * @author liuwx
	 */
	public Document buildDoc(String xmlPath) throws JDOMException, IOException {
		Assert.assertNotNull("path不能为空", xmlPath);
		//FileReader
		FileReader read = new FileReader(xmlPath);
		// 创建新的输入源SAX 解析器将使用 InputSource 对象来确定如何读取 XML 输入
		InputSource source = new InputSource(read);
		// 创建一个新的SAXBuilder
		SAXBuilder sb = new SAXBuilder();
		// 通过输入源构造一个Document
		Document doc = sb.build(source);
		return doc;

	}
	
	/**
     * 取得打包信息xml文件
     *
     * @param path sdp-package.xml文件所在路径
     * @param isFullPath 是否是绝对路径
     *
     * @return sdp-package.xml文件内容
     */
    public String AccessXmlFile(String path,String header,
            Map<String,CSInstanceInfo> instanceMap, String addonInstance) throws Exception{
        if(header.equals(addonInstance)) {
            path = instanceMap.get(addonInstance).getPath()+path;
        }
        String url = instanceMap.get(header).getUrl()+"/"
                +SessionUtil.createSession("777", instanceMap.get(header).getUrl(),
                        instanceMap.get(header).getPath(),
                        instanceMap.get(header).getServiceId())
                +"/static"+path+"/sdp-package.xml";

        String xmlFile = null;
        try{
            xmlFile = HttpClientUtils.httpGet(url);
        } catch (Exception e) {
            LOG.info(path+"未取得打包信息xml:"+e.getMessage());
        }
        return xmlFile;
    }

}

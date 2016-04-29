package nd.esp.service.lifecycle.controller.v06;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import nd.esp.service.lifecycle.BaseControllerConfig;
import nd.esp.service.lifecycle.daos.teachingmaterial.v06.ChapterDao;
import nd.esp.service.lifecycle.impl.SimpleJunitTest4ResourceImpl;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.utils.gson.ObjectUtils;
import nd.esp.service.lifecycle.vos.chapters.v06.ChapterViewModel;
import nd.esp.service.lifecycle.vos.chapters.v06.ChapterViewModel4Move;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.model.Chapter;
import nd.esp.service.lifecycle.repository.sdk.ChapterRepository;

import com.nd.gaea.rest.o2o.JacksonCustomObjectMapper;
import com.nd.gaea.rest.testconfig.MockUtil;

public class TestPreorderTreeOperationForChapter extends BaseControllerConfig {

    private static final Logger logger = LoggerFactory.getLogger(TestPreorderTreeOperationForChapter.class);
    private static final JacksonCustomObjectMapper JSON_CUSTOM_OBJECT_MAPPER = new JacksonCustomObjectMapper();

    private String mid; // 教材uuid

    private static String ROOT_URL = "/v0.6/fortest/teachingmaterials";

    @Autowired
    private ChapterDao chapterDao;

    @Autowired
    private ChapterRepository chapterRepository;

    @SuppressWarnings("unchecked")
    @Override
    public void before() {
        String json = "";
        String uri = "";
        String resStr = "";
        Map<String, Object> m = null;
        uri = "/v0.6/teachingmaterials";
        json = getCreateTeachingMaterial();
        try {
            resStr = MockUtil.mockCreate(mockMvc, uri, json);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        m = ObjectUtils.fromJson(resStr, Map.class);
        Assert.assertNotNull("教材创建失败", m);
        Assert.assertEquals("教材创建失败", "人教版-初中一年级数学上册", m.get("title"));
        Assert.assertNotNull("教材创建失败", m.get("categories"));
        Assert.assertNotNull("教材创建失败", m.get("life_cycle"));
        Assert.assertNotNull("教材创建失败", m.get("education_info"));
        Assert.assertNotNull("教材创建失败", m.get("copyright"));
        mid = (String) m.get("identifier");
        logger.error("lsm test teachingmaterial uuid: " + mid);
        logger.info("教材创建验证通过");
    }

//    @Test
    @Override
    public void doTest() {

        int depth = 3;
        int width = 3;

        try {
            createTree(mid, depth, width);
            checkTree(mid);

            testDelete((int)(Math.pow(width, depth)));//random 删除n个
            testMove((int) (Math.pow(width, depth)));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * @author linsm
     * @param i
     * @throws Exception
     * @throws UnsupportedEncodingException
     * @since
     */
    private void testMove(int num) throws UnsupportedEncodingException, Exception {
        Chapter condition = new Chapter();
        condition.setTeachingMaterial(mid);
        condition.setEnable(null);
        List<Chapter> chapters = chapterRepository.getAllByExample(condition);
        Assert.assertNotNull("教材下保证存在章节", chapters);

        // 按照父结点的值来组织数据
        Map<String, List<Chapter>> helpValidTreeMap = new HashMap<String, List<Chapter>>();
        for (Chapter chapter : chapters) {
            if (helpValidTreeMap.containsKey(chapter.getParent())) {
                helpValidTreeMap.get(chapter.getParent()).add(chapter);
            } else {
                List<Chapter> innerChapters = new ArrayList<Chapter>();
                innerChapters.add(chapter);
                helpValidTreeMap.put(chapter.getParent(), innerChapters);
            }
        }
        int tryNum = 0;
        while (++tryNum <= num) {
            int randSourceIndex = (int) (Math.random() * chapters.size());
            int randTargetIndex = (int) (Math.random() * chapters.size());
            Chapter source = chapters.get(randSourceIndex);
            Chapter target = chapters.get(randTargetIndex);

            logger.info("move chapter from: title: " + source.getTitle()
                    + "       to:  title: " + target.getTitle());
            // source ->target direction: pre
            ChapterViewModel4Move chapterViewModel4Move = new ChapterViewModel4Move();
            chapterViewModel4Move.setTarget(target.getIdentifier());
            chapterViewModel4Move.setDirection("next");
            chapterViewModel4Move.setParent(mid);// 有 target 时 ：失效
            moveChapter(source.getIdentifier(), chapterViewModel4Move);

            checkTreeWithOutTitleOrdered(mid);//此时或者title 与left 不一致

            // 还原回去：两种情况，一种是source 在同一个子树下，存在右结点（同一层次），一种是不存在
            List<Chapter> sameParenWithSource = helpValidTreeMap.get(source.getParent());
            Assert.assertNotNull("至少有一个，必然不为空", sameParenWithSource);
            Chapter right = null;
            for (Chapter chapter : sameParenWithSource) {
                if (chapter.getLeft() > source.getLeft()) {
                    if (right != null) {
                        if (right.getLeft() > chapter.getLeft()) {// 更靠近source
                            right = chapter;
                        }
                    } else {
                        right = chapter;
                    }
                }
            }
            if (right != null) {
                // 说明存在直接右边结点
                ChapterViewModel4Move moveViewModel4Move = new ChapterViewModel4Move();
                moveViewModel4Move.setTarget(right.getIdentifier());
                moveViewModel4Move.setDirection("pre");
                moveViewModel4Move.setParent(mid);
                moveChapter(source.getIdentifier(), moveViewModel4Move);
            } else {
                // 父结点的最后
                ChapterViewModel4Move moveViewModel4Move2 = new ChapterViewModel4Move();
                moveViewModel4Move2.setTarget(null);
                moveViewModel4Move2.setDirection("pre");
                moveViewModel4Move2.setParent(source.getParent());
                moveChapter(source.getIdentifier(), moveViewModel4Move2);
            }

            checkTree(mid);

        }

    }

    /**
     * @author linsm
     * @param identifier
     * @param chapterViewModel4Move
     * @throws Exception
     * @throws UnsupportedEncodingException
     * @since
     */
    private void moveChapter(String identifier, ChapterViewModel4Move chapterViewModel4Move) throws UnsupportedEncodingException,
                                                                                            Exception {
        String url = ROOT_URL + "/" + mid + "/chapters/" + identifier + "/actions/move";
        String json = JSON_CUSTOM_OBJECT_MAPPER.writeValueAsString(chapterViewModel4Move);
        String result = MockUtil.mockPut(mockMvc, url, json);

    }

    /**
     * @author linsm
     * @param num
     * @throws EspStoreException
     * @since
     */
    private void testDelete(int num) throws EspStoreException {
        List<TreeNode> allChapters = getAllChapters();

        int tryNum = 0;
        while (++tryNum <= num) {
            int randIndex = (int) (Math.random() * allChapters.size());
            TreeNode node = allChapters.get(randIndex);
            logger.info("delete chapter, id: " + node.identifier + "  title: " + node.title);
            deleteChapter(node.identifier);
            checkTree(mid);
        }

    }

    /**
     * @author linsm
     * @param identifier
     * @since
     */
    private void deleteChapter(String identifier) {
        String url = ROOT_URL + "/" + mid + "/chapters/" + identifier;
        String json = "";
        try {
            MockUtil.mockDelete(mockMvc, url, json);
        } catch (Exception e) {
            logger.error("delete fail, id: " + identifier);
        }

    }

    /**
     * 取得教材下的所有章节
     * 
     * @author linsm
     * @return
     * @throws EspStoreException
     * @since
     */
    private List<TreeNode> getAllChapters() throws EspStoreException {
        Chapter condition = new Chapter();
        condition.setTeachingMaterial(mid);
        condition.setEnable(null);
        List<Chapter> chapters = chapterRepository.getAllByExample(condition);
        Assert.assertNotNull("教材下保证存在章节", chapters);

        List<TreeNode> chapterNodes = new ArrayList<TestPreorderTreeOperationForChapter.TreeNode>();
        for (Chapter chapter : chapters) {
            TreeNode treeNode = new TreeNode();
            treeNode.identifier = chapter.getIdentifier();
            treeNode.title = chapter.getTitle();
            chapterNodes.add(treeNode);
        }

        return chapterNodes;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void after() {
//        String uri = "";
//        String resStr = "";
//        Map<String, Object> m = null;
//
//        // delete测试
//        uri = "/v0.6/teachingmaterials/" + mid;
//        try {
//            resStr = MockUtil.mockDelete(mockMvc, uri, null);
//        } catch (Exception e) {
//            logger.error("delete teachingmaterial failed: " + mid);
//        }
//        m = ObjectUtils.fromJson(resStr, Map.class);
//        Assert.assertNotNull("教材删除失败", m);
//        Assert.assertNotNull("教材删除失败", m.get("process_code"));
//        Assert.assertEquals("教材删除失败", "LC/DELETE_RESOURCE_SUCCESS", (String) m.get("process_code"));
    }

    private void createTree(String root, int depth, int width) throws UnsupportedEncodingException, Exception {

        int level = 0;
        Set<TreeNode> levelTreeNodes = new HashSet<TreeNode>();
        TreeNode rootNode = new TreeNode();
        rootNode.identifier = root;
        rootNode.title = "lsm";
        levelTreeNodes.add(rootNode);
        while (++level <= depth) {
            Set<TreeNode> newLevelTreeNodes = new HashSet<TreeNode>();
            for (TreeNode node : levelTreeNodes) {
                newLevelTreeNodes.addAll(createDirectChildren(node, width));
            }
            levelTreeNodes = newLevelTreeNodes;
        }
    }

    /**
     * @author linsm
     * @param node
     * @param width
     * @return
     * @throws Exception
     * @throws UnsupportedEncodingException
     * @since
     */
    private Set<TreeNode> createDirectChildren(TreeNode node, int width) throws UnsupportedEncodingException, Exception {
        Set<TreeNode> children = new HashSet<TreeNode>();
        int orderNum = 0;
        ChapterViewModel chapterViewModel = new ChapterViewModel();
        chapterViewModel.setParent(node.identifier);
        while (++orderNum <= width) {
            chapterViewModel.setTitle(node.title + "." + orderNum);
            chapterViewModel.setDescription(chapterViewModel.getTitle());
            ChapterViewModel child = createChapter(chapterViewModel);
             checkTree(mid);
            TreeNode childNode = new TreeNode();
            childNode.identifier = child.getIdentifier();
            childNode.title = child.getTitle();
            children.add(childNode);
        }

        return children;
    }

    /**
     * @author linsm
     * @param chapterViewModel
     * @throws Exception
     * @throws UnsupportedEncodingException
     * @since
     */
    private ChapterViewModel createChapter(ChapterViewModel chapterViewModel) throws UnsupportedEncodingException,
                                                                             Exception {
        String url = ROOT_URL + "/" + mid + "/chapters";
        String json = JSON_CUSTOM_OBJECT_MAPPER.writeValueAsString(chapterViewModel);
        String result = MockUtil.mockCreate(mockMvc, url, json);
        chapterViewModel = JSON_CUSTOM_OBJECT_MAPPER.readValue(result, ChapterViewModel.class);
        return chapterViewModel;

    }
    
    private void checkTree(String root) throws EspStoreException {
        List<Chapter> chapters = checkTreeWithOutTitleOrdered(root);
        if(CollectionUtils.isNotEmpty(chapters)){
            //sort by title string desc
            Collections.sort(chapters, new Comparator<Chapter>() {

                @Override
                public int compare(Chapter o1, Chapter o2) {
//                    if(o1==null){
//                        return -1;
//                    }
//                    if(o2 == null){
//                        return 1;
//                    }
//                    if(o1.getTitle()==null){
//                        return -1;
//                    }
                    return o1.getTitle().compareTo(o2.getTitle());
                }
                
            });
            int index = 0;
            while (++index<chapters.size()) {
                Assert.assertTrue("left 的顺序必然与title （String asc)一致", chapters.get(index-1).getLeft()<chapters.get(index).getLeft());
            }
        }
    }

    private List<Chapter> checkTreeWithOutTitleOrdered(String root) throws EspStoreException {
        Chapter condition = new Chapter();
        condition.setTeachingMaterial(root);
        condition.setEnable(null);
        List<Chapter> chapters = chapterRepository.getAllByExample(condition);
        Assert.assertNotNull("教材下保证存在章节", chapters);
        int treeNodeNum = chapters.size();
        Assert.assertNotEquals(treeNodeNum, 0);

        // right>left (right -left)%2 = 1 old
        for (Chapter chapter : chapters) {
            Assert.assertTrue("right>left", chapter.getRight() > chapter.getLeft());
            Assert.assertTrue("(right -left)%2", (chapter.getRight() - chapter.getLeft()) % 2 == 1);
        }

        Map<String, Chapter> helpValidMap = new HashMap<String, Chapter>();
        for (Chapter chapter : chapters) {
            helpValidMap.put(chapter.getIdentifier(), chapter);
        }
        // have parent, parent.left<left<right<parent.right
        // not parent, top level 0<left<right<2*totalNUm+1; parent = = root
        int rootLeft = 0;
        int rootRight = 2 * treeNodeNum + 1;
        for (Chapter chapter : chapters) {
            Chapter parent = helpValidMap.get(chapter.getParent());
            if (parent != null) {
                Assert.assertTrue("parent.left<left", parent.getLeft() < chapter.getLeft());
                Assert.assertTrue("right<parent.right", chapter.getRight() < parent.getRight());
            } else {
                // mid top level
                Assert.assertEquals(mid, chapter.getParent());
                Assert.assertTrue("parent.left<left", rootLeft < chapter.getLeft());
                Assert.assertTrue("right<parent.right", chapter.getRight() < rootRight);
            }
        }
        return chapters;
    }

    /**
     * 拼装教材创建教材的入参报文
     * 
     * @return
     */
    private static String getCreateTeachingMaterial() {
        String json = "{" + "\"title\": \"人教版-初中一年级数学上册\"," + "\"description\": \""+SimpleJunitTest4ResourceImpl.DERAULT_DESCRIPTION+"\"," + "\"language\": \"zh_CN\","
                + "\"keywords\": [\"教材\",\"数学\"]," + "\"tags\": [\"教材\",\"数学\"]," + "\"preview\": {"
                + "   \"png\": \"{ref_path}/edu/esp/preview/123.png\"" + "}," + "\"ext_properties\":{"
                + "    \"isbn\": \"ISBN-10 4-88888-913-9\","
                + "    \"attachments\":[\"http://service.edu.nd.com.cn/library/assets/100013\"],"
                + "    \"criterion\": \"《XXX课标》\"" + "}," + "\"life_cycle\":{" + "    \"version\": \"v0.2\","
                + "    \"status\": \"INIT\"," + "    \"enable\":\"true\"," + "    \"creator\": \""+SimpleJunitTest4ResourceImpl.DERAULT_CREATOR+"\","
                + "    \"publisher\": \""+SimpleJunitTest4ResourceImpl.DERAULT_PUBLISHER+"\"," + "    \"provider\":\"NetDragon Inc.\","
                + "    \"provider_source\":\"\"" + "}," + "\"education_info\":{" + "    \"interactivity\":\"2\","
                + "    \"interactivity_level\":\"2\"," + "    \"end_user_type\":\"教师，管理者\","
                + "    \"semantic_density\":\"1\"," + "    \"context\":\"基础教育\"," + "    \"age_range\":\"7岁以上\","
                + "    \"difficulty\":\"easy\"," + "    \"learning_time\":\"P0Y0M0DT3H0M\","
                + "    \"description\":{\"zh_CN\":\"如何使用学习对象进行描述\"}," + "    \"language\":\"zh_CN\"" + "},"
                + "\"copyright\":{" + "    \"right\":\"zh\"," + "    \"description\":\"如何使用学习对象进行描述\","
                + "    \"author\":\"johnny\"" + "}," + "\"coverages\":[" + "    {"
                + "        \"target_type\":\"User\"," + "        \"target\":\"890399\","
                + "        \"target_title\":\"tom.king\"," + "        \"strategy\":\"OWNER\"" + "    }" + "], "
                + "\"categories\":{" + "    \"phase\":[" + "        {"
                + "        \"taxonpath\":\"K12/$ON030000/$ON030200/$SB0500/$E004000/" + UUID.randomUUID().toString()
                + "\"," + "        \"taxoncode\":\"$ON030000\"" + "        }" + "    ]" + "}"+ "}";

        return json;
    }

    static class TreeNode {
        public String identifier;
        public String title;

    }

}
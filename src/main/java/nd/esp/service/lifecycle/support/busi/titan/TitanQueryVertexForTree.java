package nd.esp.service.lifecycle.support.busi.titan;

import java.util.HashMap;
import java.util.Map;

/**
 * 章节递归点，要求遍历章节子树
 * 
 * @author linsm
 *
 */
public class TitanQueryVertexForTree extends TitanQueryVertex {

	private String treeEdgeLabel = TitanKeyWords.tree_has_chapter.toString();

	// private Integer maxDepth = 2;
	//
	// public void setMaxDepth(Integer maxDepth) {
	// this.maxDepth = maxDepth;
	// }

	public void setTreeEdgeLabel(String treeEdgeLabel) {
		this.treeEdgeLabel = treeEdgeLabel;
	}

	@Override
	public String generateScript(Map<String, Object> scriptParamMap) {
		// setVertexLabel(ResourceNdCode.chapters.toString());
		setTitanDirection(TitanDirection.no);
		StringBuffer scriptBuffer = new StringBuffer(
				super.generateScript(scriptParamMap));
		// .aggregate('x').repeat(out('knows').aggregate('x')).times(2).emit().select('x').unfold().dedup()
		// FIXME magic string ; has_chapter properties
		// FIXME emit 在后：当节点是叶子结点时会导致数据丢失
		String key = TitanUtils.generateKey(scriptParamMap, treeEdgeLabel);
		scriptParamMap.put(key, treeEdgeLabel);
		scriptBuffer
				.append(".aggregate('subtree').emit().repeat(out(")
				.append(key)
				.append(").aggregate('subtree')).times(2).select('subtree').unfold().dedup()");

		return scriptBuffer.toString();
	}

	/*********************************************** TEST ******************************/
	public static void main(String[] args) {
		Map<String, Object> scriptParamMap = new HashMap<String, Object>();
		TitanQueryVertexForTree titanQueryVertexForChapter = generateTestExampleForChapter();
		System.out.println(titanQueryVertexForChapter
				.generateScript(scriptParamMap));
		System.out.println(scriptParamMap);
	}

	private static TitanQueryVertexForTree generateTestExampleForChapter() {
		TitanQueryVertex titanQueryVertex = TitanQueryVertex
				.generateTestExample();
		TitanQueryVertexForTree titanQueryVertexForChapter = new TitanQueryVertexForTree();
		titanQueryVertexForChapter.setPropertiesMap(titanQueryVertex
				.getPropertiesMap());
		return titanQueryVertexForChapter;
	}

}

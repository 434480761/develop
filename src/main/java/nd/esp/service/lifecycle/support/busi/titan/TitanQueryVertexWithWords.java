package nd.esp.service.lifecycle.support.busi.titan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nd.esp.service.lifecycle.utils.StringUtils;

public class TitanQueryVertexWithWords extends TitanQueryVertex {

	private String stringWords;
	private String textWords;

	// FIXME 暂时放在一起
	public void setWords(String words) {
		String[] chunks = words.split("\\$");
		if (!"null".equals(chunks[0])) {
			stringWords = chunks[0].trim();
		}

		//System.out.println(stringWords);

		if (!"null".equals(chunks[1])) {
			textWords = chunks[1].trim();
		}

		//System.out.println(textWords);
	}

	@Override
	public String generateScript(Map<String, Object> scriptParamMap) {

		StringBuffer scriptBuffer = new StringBuffer(
				super.generateScript(scriptParamMap));
		if (StringUtils.isNotEmpty(stringWords)) {
			scriptBuffer.append(".").append(TitanKeyWords.or.toString())
					.append("(");
			List<Object> values = new ArrayList<Object>();
			values.add(stringWords);
			for (WordsCover wordsCover : WordsCover.values()) {

				// remove the "."
				scriptBuffer.append(Titan_OP.like.generateScipt(
						wordsCover.toString(), values, scriptParamMap)
						.substring(1));
				scriptBuffer.append(",");
			}
			scriptBuffer.deleteCharAt(scriptBuffer.length() - 1);
			scriptBuffer.append(")");
		}
		
		if (StringUtils.isNotEmpty(textWords)) {
			scriptBuffer.append(".").append(TitanKeyWords.or.toString())
					.append("(");
			List<Object> values = new ArrayList<Object>();
			values.add(textWords);
			for (WordsCover wordsCover : WordsCover.values()) {

				// remove the "."
				scriptBuffer.append(Titan_OP.fulltextlike.generateScipt(
						wordsCover.toString(), values, scriptParamMap)
						.substring(1));
				scriptBuffer.append(",");
			}
			scriptBuffer.deleteCharAt(scriptBuffer.length() - 1);
			scriptBuffer.append(")");
		}
		return scriptBuffer.toString();
	}

	public static enum WordsCover {
		title, description, keywords, tags, edu_description, cr_description
	}

	/*************************************** TEST **********************************/
	public static void main(String[] args) {

		testGenerateScript();

		// testWords();
	}

	static void testGenerateScript() {
		TitanQueryVertex titanQueryVertex = TitanQueryVertex
				.generateTestExample();
		TitanQueryVertexWithWords titanQueryVertexWithWords = new TitanQueryVertexWithWords();

		titanQueryVertexWithWords.setPropertiesMap(titanQueryVertex
				.getPropertiesMap());
		titanQueryVertexWithWords.setTitanDirection(titanQueryVertex
				.getTitanDirection());
		titanQueryVertexWithWords.setVertexLabel(titanQueryVertex
				.getVertexLabel());
		String stringWords = null;
		String fulltext = null;
		String words = stringWords + "$" + fulltext;
		System.out.println(words);
		titanQueryVertexWithWords.setWords(words);

		Map<String, Object> scriptParamMap = new HashMap<String, Object>();
		System.out.println(titanQueryVertexWithWords
				.generateScript(scriptParamMap));
		System.out.println(scriptParamMap);

	}

	static void testWords() {
		TitanQueryVertexWithWords testObjecTitanQueryVertexWithWords = new TitanQueryVertexWithWords();
		String stringWords = null;
		String fulltext = null;
		String words = stringWords + "$" + fulltext;
		System.out.println(words);
		testObjecTitanQueryVertexWithWords.setWords(words);

		stringWords = "stringWords";

		words = stringWords + "$" + fulltext;
		System.out.println(words);
		testObjecTitanQueryVertexWithWords.setWords(words);

		fulltext = "fulltext";

		words = stringWords + "$" + fulltext;
		System.out.println(words);
		testObjecTitanQueryVertexWithWords.setWords(words);
	}

}

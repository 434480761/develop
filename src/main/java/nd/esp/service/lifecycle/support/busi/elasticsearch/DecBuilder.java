package nd.esp.service.lifecycle.support.busi.elasticsearch;

import java.io.IOException;

import nd.esp.service.lifecycle.support.enums.ES_Field;
import nd.esp.service.lifecycle.support.enums.ElasticSearchReservedWords;

import org.elasticsearch.common.xcontent.XContentBuilder;

/**
 * 包装了builder,简化使用。
 * 
 * @author linsm
 *
 */
public class DecBuilder {
	private XContentBuilder builder;

	public DecBuilder(XContentBuilder builder) {
		this.builder = builder;

	}

	public DecBuilder startObject() throws IOException {
		builder.startObject();
		return this;
	}

	public DecBuilder startObject(ElasticSearchReservedWords name)
			throws IOException {
		builder.startObject(name.toString());
		return this;
	}

	// public DecBuilder startObject(MY_ES_Field name) throws IOException {
	// builder.startObject(name.toString());
	// return this;
	// }

	public DecBuilder startObject(ES_Field name) throws IOException {
		builder.startObject(name.toString());
		return this;
	}

	public DecBuilder endObject() throws IOException {
		builder.endObject();
		return this;
	}

	public DecBuilder setFieldDefaultProperties(ElasticSearchReservedWords type)
			throws IOException {
		builder.field(ElasticSearchReservedWords.TYPE.toString(),
				type.toString())
				.field(ElasticSearchReservedWords.STORE.toString(),
						ElasticSearchReservedWords.YES.toString())
				.field(ElasticSearchReservedWords.INDEX.toString(),
						ElasticSearchReservedWords.NOT_ANALYZED.toString());
		if (type == ElasticSearchReservedWords.STRING) {
			builder.field(ElasticSearchReservedWords.IGNORE_ABOVE.toString(),
					10000);// 设置term 的最大长度，10000个字符，（lucence term
							// 最大长度为3万多个bytes）
		}

		return this;
	}

	// public DecBuilder build(MY_ES_Field name, ElasticSearchReservedWords
	// type)
	// throws IOException {
	// builder.startObject(name.toString());
	// setFieldDefaultProperties(type);
	// builder.endObject();
	// return this;
	//
	// }

	// public DecBuilder build(MY_ES_Field name) throws IOException {
	// return build(name, ElasticSearchReservedWords.STRING);
	// }

	public DecBuilder build(ES_Field name, ElasticSearchReservedWords type)
			throws IOException {
		builder.startObject(name.toString());
		setFieldDefaultProperties(type);
		builder.endObject();
		return this;

	}

	public DecBuilder build(ES_Field name) throws IOException {
		return build(name, ElasticSearchReservedWords.STRING);
	}

	public XContentBuilder builder() {
		return builder;
	}

	public DecBuilder field(String fieldName, String fieldValue)
			throws IOException {
		builder.field(fieldName, fieldValue);
		return this;
	}

}

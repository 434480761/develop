package nd.esp.service.lifecycle.daos.elasticsearch;

import java.io.IOException;

import nd.esp.service.lifecycle.support.busi.elasticsearch.DecBuilder;
import nd.esp.service.lifecycle.support.enums.ES_Field;
import nd.esp.service.lifecycle.support.enums.ElasticSearchReservedWords;
import nd.esp.service.lifecycle.support.enums.ResourceNdCode;

import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.Requests;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * 索引配置
 * 
 * @author linsm
 *
 */
@Repository
public class EsIndexOperationImpl implements EsIndexOperation {

	@Autowired
	private Client client;

	/**
	 * 创建索引
	 * 
	 * @param indexName
	 *            索引名
	 * @author linsm
	 * @return
	 */
	@Override
	public Boolean createIndex(String indexName) {
		CreateIndexRequest request = new CreateIndexRequest(indexName);
		CreateIndexResponse createIndexResponse = client.admin().indices()
				.create(request).actionGet();
		if (createIndexResponse != null) {
			return createIndexResponse.isAcknowledged(); // bylsm meaning, what
															// is success
		}
		return false;
	}

	/**
	 * 删除索引
	 * 
	 * @param esIndexName
	 *            索引名
	 * @author linsm
	 * @return
	 */
	@Override
	public Boolean deleteIndex(String esIndexName) {
		DeleteIndexResponse response = client.admin().indices()
				.delete(Requests.deleteIndexRequest(esIndexName)).actionGet();
		if (response != null) {
			return response.isAcknowledged();
		}
		return false;
	}

	/**
	 * 添加或者更新mapping
	 * 
	 * @param indexName
	 *            索引名
	 * @param primaryCategory
	 *            资源类型
	 * @author linsm
	 * @return
	 */
	@Override
	public Boolean putMapping(String indexName, String primaryCategory) {
		XContentBuilder builder = buildMapping(primaryCategory);
		if (builder == null) {
			return false;
		}
		PutMappingRequest mapping = Requests.putMappingRequest(indexName)
				.type(primaryCategory).source(builder);
		PutMappingResponse putMappingResponse = client.admin().indices()
				.putMapping(mapping).actionGet();
		if (putMappingResponse != null) {
			return putMappingResponse.isAcknowledged();
		}
		return false;
	}

	/**
	 * index mapping
	 * 
	 * @author linsm
	 * @return
	 */
	private XContentBuilder buildMapping(String mappingType) {
		try {
			XContentBuilder builder = XContentFactory.jsonBuilder();
			DecBuilder builderImpl = new DecBuilder(builder);
			builderImpl
					.startObject()
					.field(ElasticSearchReservedWords.DYNAMIC.toString(),
							ElasticSearchReservedWords.STRICT.toString())
					.startObject(ElasticSearchReservedWords.PROPERTIES)
					.build(ES_Field.identifier)
					.build(ES_Field.title)
					.build(ES_Field.language)
					.build(ES_Field.description)
					.build(ES_Field.preview)
					.build(ES_Field.tags)
					.build(ES_Field.keywords)
					.build(ES_Field.custom_properties)
					.build(ES_Field.ndres_code)
					// LifeCycle
					.startObject(ES_Field.life_cycle)
					.startObject(ElasticSearchReservedWords.PROPERTIES)
					.build(ES_Field.create_time,
							ElasticSearchReservedWords.Long)
					.build(ES_Field.creator)
					.build(ES_Field.enable, ElasticSearchReservedWords.BOOLEAN)
					.build(ES_Field.last_update,
							ElasticSearchReservedWords.Long)
					.build(ES_Field.provider)
					.build(ES_Field.provider_source)
					.build(ES_Field.publisher)
					.build(ES_Field.status)
					.build(ES_Field.version)
					.endObject()
					.setFieldDefaultProperties(
							ElasticSearchReservedWords.NESTED)
					.endObject()
					// EducationInfo
					.startObject(ES_Field.education_info)
					.startObject(ElasticSearchReservedWords.PROPERTIES)
					.build(ES_Field.age_range)
					.build(ES_Field.context)
					.build(ES_Field.description)
					.build(ES_Field.difficulty)
					.build(ES_Field.end_user_type)
					.build(ES_Field.interactivity,
							ElasticSearchReservedWords.INTEGER)
					.build(ES_Field.interactivity_level,
							ElasticSearchReservedWords.INTEGER)
					.build(ES_Field.language)
					.build(ES_Field.learning_time)
					.build(ES_Field.semantic_density,
							ElasticSearchReservedWords.INTEGER)
					.endObject()
					.setFieldDefaultProperties(
							ElasticSearchReservedWords.NESTED)
					.endObject()
					// categories
					.startObject(ES_Field.category_list)
					.startObject(ElasticSearchReservedWords.PROPERTIES)
					.build(ES_Field.identifier)
					.build(ES_Field.taxonpath)
					.build(ES_Field.taxonname)
					.build(ES_Field.taxoncode)
					.build(ES_Field.taxoncode_id)
					.build(ES_Field.short_name)
					.build(ES_Field.category_code)
					.build(ES_Field.category_name)
					.endObject()
					.setFieldDefaultProperties(
							ElasticSearchReservedWords.NESTED)
					.endObject()
					// coverages
					.startObject(ES_Field.coverages)
					.startObject(ElasticSearchReservedWords.PROPERTIES)
					.build(ES_Field.strategy)
					.build(ES_Field.target)
					.build(ES_Field.target_title)
					.build(ES_Field.target_type)
					.endObject()
					.setFieldDefaultProperties(
							ElasticSearchReservedWords.NESTED)
					.endObject()
					// techinfo 8
					.startObject(ES_Field.tech_info_list)
					.startObject(ElasticSearchReservedWords.PROPERTIES)
					.build(ES_Field.format)
					.build(ES_Field.size, ElasticSearchReservedWords.Long)
					.build(ES_Field.location)
					.build(ES_Field.md5)
					.build(ES_Field.title)
					.build(ES_Field.entry)
					.build(ES_Field.secure_key)
					.build(ES_Field.printable, ElasticSearchReservedWords.BOOLEAN)
					/* add at 2016.03.30:18:43 */
					// requirement 7
					.startObject(ES_Field.requirements)
					.startObject(ElasticSearchReservedWords.PROPERTIES)
					.build(ES_Field.identifier)
					.build(ES_Field.type)
					.build(ES_Field.name)
					.build(ES_Field.min_version)
					.build(ES_Field.max_version)
					.build(ES_Field.installation)
					.build(ES_Field.installation_file)
					.build(ES_Field.value)
					.endObject()
					.setFieldDefaultProperties(
							ElasticSearchReservedWords.NESTED)
					.endObject()
					.endObject()
					.setFieldDefaultProperties(
							ElasticSearchReservedWords.NESTED)
					.endObject()
					// copyRight
					.startObject(ES_Field.copyright)
					.startObject(ElasticSearchReservedWords.PROPERTIES)
					.build(ES_Field.author)
					.build(ES_Field.description)
					.build(ES_Field.right)
					.endObject()
					.setFieldDefaultProperties(
							ElasticSearchReservedWords.NESTED).endObject();
			// primaryCategory
			// .build(ES_Field.primary_category).build(ES_Field.code);
			// extend
			extendMapping(builderImpl, mappingType);
			builderImpl.endObject().endObject();

			return builderImpl.builder();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 添加扩展属性（各种资源）
	 * 
	 * @param builder
	 * @param mappingType
	 * @author linsm
	 * @return
	 * @throws IOException
	 */
	private static DecBuilder extendMapping(DecBuilder builder,
			String mappingType) throws IOException {
		ResourceNdCode resourceNdCode = ResourceNdCode.fromString(mappingType);
		if (resourceNdCode == null) {
			return builder;
		}
		switch (resourceNdCode) {
		case teachingmaterials:
		case guidancebooks:
		case ebooks:
			builder.startObject(ES_Field.ext_properties)
					.startObject(ElasticSearchReservedWords.PROPERTIES)
					.build(ES_Field.isbn)
					.build(ES_Field.attachments)
					.build(ES_Field.criterion)
					.endObject()
					.setFieldDefaultProperties(
							ElasticSearchReservedWords.NESTED).endObject();
			break;
		case questions:
			builder.startObject(ES_Field.ext_properties)
					.startObject(ElasticSearchReservedWords.PROPERTIES)
					.build(ES_Field.discrimination,
							ElasticSearchReservedWords.FLOAT)
					.build(ES_Field.answer)
					.build(ES_Field.item_content)
					.build(ES_Field.criterion)
					.build(ES_Field.score, ElasticSearchReservedWords.FLOAT)
					.build(ES_Field.secrecy, ElasticSearchReservedWords.INTEGER)
					.build(ES_Field.modified_difficulty,
							ElasticSearchReservedWords.FLOAT)
					.build(ES_Field.ext_difficulty,
							ElasticSearchReservedWords.FLOAT)
					.build(ES_Field.modified_discrimination,
							ElasticSearchReservedWords.FLOAT)
					.build(ES_Field.used_time,
							ElasticSearchReservedWords.INTEGER)
					.build(ES_Field.exposal_date,
							ElasticSearchReservedWords.DATE)
					.build(ES_Field.auto_remark,
							ElasticSearchReservedWords.BOOLEAN)
					.endObject()
					.setFieldDefaultProperties(
							ElasticSearchReservedWords.NESTED).endObject();
			break;

		default:
			break;
		}
		return builder;

	}

}

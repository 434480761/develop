package nd.esp.service.lifecycle.support.enums;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nd.esp.service.lifecycle.educommon.vos.constant.PropOperationConstant;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.utils.StringUtils;

import org.apache.commons.collections4.CollectionUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

public enum ES_OP {
	eq {

		@Override
		public BoolQueryBuilder _buildQuery(ES_SearchField es_SearchField,
				List<String> values) {
			BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

			if (es_SearchField.getParent() == ES_SearchField.category_list) {
				// 直接内置：
				// category
				for (String value : values) {
					if (StringUtils.isNotEmpty(value) && value.contains("*")) {
						// like
						if (ES_SearchField.cg_taxoncode == es_SearchField
								&& value.contains(PropOperationConstant.OP_AND)) {
							// code and
							boolQueryBuilder.should(getCodeAndBuilder(value));
						} else {
							boolQueryBuilder
									.should(categoryBuilder(QueryBuilders
											.wildcardQuery(es_SearchField
													.getNameInEsWithNested(),
													value)));
						}

					} else {
						// eq
						if (ES_SearchField.cg_taxoncode == es_SearchField
								&& value.contains(PropOperationConstant.OP_AND)) {
							// code and
							boolQueryBuilder.should(getCodeAndBuilder(value));
						} else {
							boolQueryBuilder
									.should(categoryBuilder(QueryBuilders
											.termQuery(es_SearchField
													.getNameInEsWithNested(),
													value)));
						}

					}
				}

			}else if(es_SearchField==ES_SearchField.ti_printable){
				String value = values.get(0);
				if(value.contains("#")){
					List<String> list = Arrays.asList(value.split("#"));
					if(CollectionUtils.isNotEmpty(list) && list.size() >= 2){
						boolQueryBuilder.must(QueryBuilders.termQuery(
								es_SearchField
										.getNameInEsWithNested(),
										list.get(0)));
						boolQueryBuilder
						.must(QueryBuilders.termQuery(
								ES_SearchField.ti_title
										.getNameInEsWithNested(),
								list.get(1)));
					}
				}else{
					boolQueryBuilder.should(QueryBuilders.termQuery(
							es_SearchField.getNameInEsWithNested(), value));
				}
			} else {
				for (String value : values) {
					boolQueryBuilder.should(QueryBuilders.termQuery(
							es_SearchField.getNameInEsWithNested(), value));
				}
			}

			return boolQueryBuilder;
		}

		/*
		 * 处理categoryCode 包含and (已经内置)
		 */
		private QueryBuilder getCodeAndBuilder(String value) {
			// code and 关系
			String[] chunkCodes = value.split(PropOperationConstant.OP_AND);
			BoolQueryBuilder innerBoolQueryBuilder = QueryBuilders.boolQuery();
			if (chunkCodes != null) {

				for (String chunkCode : chunkCodes) {
					if (StringUtils.isNotEmpty(chunkCode)) {
						String realValue = chunkCode.trim();
						if (StringUtils.isNotEmpty(realValue)
								&& realValue.contains("*")) {
							innerBoolQueryBuilder
									.must(categoryBuilder(QueryBuilders.wildcardQuery(
											ES_SearchField.cg_taxoncode
													.getNameInEsWithNested(),
											realValue)));
						} else {
							innerBoolQueryBuilder
									.must(categoryBuilder(QueryBuilders.termQuery(
											ES_SearchField.cg_taxoncode
													.getNameInEsWithNested(),
											realValue)));
						}
					}

				}

			}
			return innerBoolQueryBuilder;
		}

	},
	ne {
		@Override
		public BoolQueryBuilder _buildQuery(ES_SearchField esSearchField,
				List<String> values) {
			BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
			if (ES_SearchField.coverages == esSearchField) {
				for (String value : values) {
					if (StringUtils.isEmpty(value)) {
						continue;
					}
					String[] chunks = value.split("/");
					if (chunks == null
							|| (chunks.length != 3 && chunks.length != 4)) {
						continue;
					}
					if (chunks.length == 3) {
						// targetType/target/strategy
						boolQueryBuilder.mustNot(buildCoverage(chunks));

					} else if (chunks.length == 4) {
						// targetType/target/strategy/status;
						boolQueryBuilder.must(QueryBuilders.boolQuery()
								.mustNot(buildCoverage(chunks))
								.must(buildStatus(chunks[3])));
					}
				}
			} else if (esSearchField.getParent() == ES_SearchField.category_list) {
				// 需要特殊处理内置；
				for (String value : values) {
					QueryBuilder queryBuilder = QueryBuilders.termQuery(
							esSearchField.getNameInEsWithNested(), value);
					boolQueryBuilder.mustNot(categoryBuilder(queryBuilder));
				}

			} else {
				for (String value : values) {
					QueryBuilder queryBuilder = QueryBuilders.termQuery(
							esSearchField.getNameInEsWithNested(), value);
					boolQueryBuilder.mustNot(queryBuilder);
				}
			}

			return boolQueryBuilder;
		}

	},
	like {
		@Override
		public BoolQueryBuilder _buildQuery(ES_SearchField esSearchField,
				List<String> values) {

			BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
			for (String value : values) {
				QueryBuilder wildcardQueryBuilder = QueryBuilders
						.wildcardQuery(esSearchField.getNameInEsWithNested(),
								changeToWildValue(value));
				boolQueryBuilder.should(wildcardQueryBuilder);
			}

			return boolQueryBuilder;
		}

		private String changeToWildValue(String value) {
			return "*" + value + "*";
		}
	},
	in {
		@Override
		public BoolQueryBuilder _buildQuery(ES_SearchField fieldName,
				List<String> values) {
			// 外 界保证，in,只有一个，且无eq
			BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
			if (ES_SearchField.coverages == fieldName) {
				for (String value : values) {
					if (StringUtils.isEmpty(value)) {
						continue;
					}
					String[] chunks = value.split("/");
					if (chunks == null
							|| (chunks.length != 3 && chunks.length != 4)) {
						continue;
					}
					if (chunks.length == 3) {
						// targetType/target/strategy
						boolQueryBuilder.should(buildCoverage(chunks));

					} else if (chunks.length == 4) {
						// targetType/target/strategy/status;
						boolQueryBuilder.should(QueryBuilders.boolQuery()
								.must(buildCoverage(chunks))
								.must(buildStatus(chunks[3])));
					}
				}
			} else {
				QueryBuilder wildcardQueryBuilder = QueryBuilders.termsQuery(
						fieldName.getNameInEsWithNested(), values);
				boolQueryBuilder.should(wildcardQueryBuilder);
			}

			return boolQueryBuilder;
		}
	},
	le {

		@Override
		public BoolQueryBuilder _buildQuery(ES_SearchField fieldName,
				List<String> values) {
			BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
			for (String value : values) {

				QueryBuilder queryBuilder;
				queryBuilder = QueryBuilders.rangeQuery(
						fieldName.getNameInEsWithNested()).lte(
						Timestamp.valueOf(value).getTime());
				boolQueryBuilder.should(queryBuilder);

			}
			return boolQueryBuilder;
		}

	},
	ge {

		@Override
		public BoolQueryBuilder _buildQuery(ES_SearchField fieldName,
				List<String> values) {
			BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
			for (String value : values) {
				QueryBuilder queryBuilder;
				queryBuilder = QueryBuilders.rangeQuery(
						fieldName.getNameInEsWithNested()).gte(
						Timestamp.valueOf(value).getTime());
				boolQueryBuilder.should(queryBuilder);
			}
			return boolQueryBuilder;
		}

	},
	lt {

		@Override
		public BoolQueryBuilder _buildQuery(ES_SearchField fieldName,
				List<String> values) {
			BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
			for (String value : values) {

				QueryBuilder queryBuilder;
				queryBuilder = QueryBuilders.rangeQuery(
						fieldName.getNameInEsWithNested()).lt(
						Timestamp.valueOf(value).getTime());
				boolQueryBuilder.should(queryBuilder);

			}
			return boolQueryBuilder;
		}

	},
	gt {

		@Override
		public BoolQueryBuilder _buildQuery(ES_SearchField fieldName,
				List<String> values) {
			BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
			for (String value : values) {
				QueryBuilder queryBuilder;
				queryBuilder = QueryBuilders.rangeQuery(
						fieldName.getNameInEsWithNested()).gt(
						Timestamp.valueOf(value).getTime());
				boolQueryBuilder.should(queryBuilder);
			}
			return boolQueryBuilder;
		}

	};

	private static final Logger LOG = LoggerFactory.getLogger(ES_OP.class);
	private static Map<String, ES_OP> map = new HashMap<String, ES_OP>();
	static {
		for (ES_OP es_OP : ES_OP.values()) {
			map.put(es_OP.toString(), es_OP);
		}
	}

	public static ES_OP fromString(String opString) {
		return map.get(opString);
	}

	public BoolQueryBuilder buildQuery(ES_SearchField es_SearchField,
			List<String> values) {
		// check
		if (es_SearchField == null || CollectionUtils.isEmpty(values)) {
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
					"LC/elasticsearch/invalidParam",
					"LC/elasticsearch/invalidParam");
		}
		return _buildQuery(es_SearchField, values);
	}

	/**
	 * 生成coverage块条件
	 * 
	 * @param chunks
	 * @return
	 */
	private static QueryBuilder buildCoverage(String[] chunks) {
		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
		if (!"*".equals(chunks[0])) {
			boolQueryBuilder.must(buildPropertyCoverage(
					ES_SearchField.cv_target_type, chunks[0]));
		}
		if (!"*".equals(chunks[1])) {
			boolQueryBuilder.must(buildPropertyCoverage(
					ES_SearchField.cv_target, chunks[1]));
		}
		if (!"*".equals(chunks[2])) {
			boolQueryBuilder.must(buildPropertyCoverage(
					ES_SearchField.cv_strategy, chunks[2]));
		}
		return QueryBuilders.nestedQuery(ES_Field.coverages.toString(),
				boolQueryBuilder);
	}

	/**
	 * coverage内属性：targetType,target,strategy
	 * 
	 * @param property
	 * @param value
	 * @return
	 */
	private static QueryBuilder buildPropertyCoverage(ES_SearchField property,
			String value) {
		if (StringUtils.isNotEmpty(value) && value.startsWith("!")) {
			// ne
			return QueryBuilders.boolQuery().mustNot(
					QueryBuilders.termQuery(property.getNameInEsWithNested(),
							value.substring(1)));
		} else {
			return QueryBuilders.termQuery(property.getNameInEsWithNested(),
					value);
		}
	}

	/**
	 * 在status外加一层nested
	 * 
	 * @param value
	 * @return
	 */
	private static QueryBuilder buildStatus(String value) {
		return ES_SearchField.life_cycle
				.build(QueryBuilders.boolQuery().must(
						QueryBuilders.termQuery(ES_SearchField.lc_status
								.getNameInEsWithNested(), value)));
	}

	// private static boolean isCodeWithAnd(String fieldName, String value) {
	// return ES_Field.taxoncode.toString().equals(fieldName)
	// && StringUtils.isNotEmpty(value)
	// && value.contains(PropOperationConstant.OP_AND);
	// }

	abstract public BoolQueryBuilder _buildQuery(ES_SearchField es_SearchField,
			List<String> values);

	/**
	 * 在category外加nested
	 * 
	 * @param objectQueryBuilder
	 * @return
	 */
	private static QueryBuilder categoryBuilder(QueryBuilder objectQueryBuilder) {
		return QueryBuilders.nestedQuery(ES_Field.category_list.toString(),
				objectQueryBuilder);
	}

	public static void main(String[] args) {
		System.out.println("Org/nd/*/ONLINE".split("/").length);
	}
}

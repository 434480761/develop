package nd.esp.service.lifecycle.support.enums;

import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

/**
 * es 中字段名
 * 
 * @author linsm
 *
 */
public enum ES_SearchField {
	root(null, null),
	// base 8
	identifier(ES_Field.identifier, root), title(ES_Field.title, root), language(
			ES_Field.language, root), description(ES_Field.description, root), preview(
			ES_Field.preview, root), tags(ES_Field.tags, root), keywords(
			ES_Field.keywords, root), custom_properties(
			ES_Field.custom_properties, root), ndres_code(ES_Field.ndres_code,
			root),m_identifier(ES_Field.m_identifier, root),

	// lifeCycle 9
	life_cycle(ES_Field.life_cycle, root) {
		@Override
		public QueryBuilder build(QueryBuilder objecdQueryBuilder) {
			return QueryBuilders.nestedQuery(life_cycle.toString(),
					objecdQueryBuilder);
		}
	},
	lc_version(ES_Field.version, life_cycle), lc_status(ES_Field.status,
			life_cycle), lc_enable(ES_Field.enable, life_cycle), lc_creator(
			ES_Field.creator, life_cycle), lc_publisher(ES_Field.publisher,
			life_cycle), lc_provider(ES_Field.provider, life_cycle), lc_provider_source(
			ES_Field.provider_source, life_cycle), lc_create_time(
			ES_Field.create_time, life_cycle), lc_last_update(
			ES_Field.last_update, life_cycle),
	// educationInfo 10
	education_info(ES_Field.education_info, root) {
		@Override
		public QueryBuilder build(QueryBuilder objecdQueryBuilder) {
			return QueryBuilders.nestedQuery(education_info.toString(),
					objecdQueryBuilder);
		}
	},
	edu_interactivity(ES_Field.interactivity, education_info), edu_interactivity_level(
			ES_Field.interactivity_level, education_info), edu_end_user_type(
			ES_Field.end_user_type, education_info), edu_semantic_density(
			ES_Field.semantic_density, education_info), edu_context(
			ES_Field.context, education_info), edu_age_range(
			ES_Field.age_range, education_info), edu_difficulty(
			ES_Field.difficulty, education_info), edu_learning_time(
			ES_Field.learning_time, education_info), edu_description(
			ES_Field.description, education_info), edu_language(
			ES_Field.language, education_info),

	// copyRight 3
	copyright(ES_Field.copyright, root) {
		@Override
		public QueryBuilder build(QueryBuilder objecdQueryBuilder) {
			return QueryBuilders.nestedQuery(copyright.toString(),
					objecdQueryBuilder);
		}
	},
	cr_right(ES_Field.right, copyright), cr_description(ES_Field.description,
			copyright), cr_author(ES_Field.author, copyright),
	/* primary_category, code, */

	// category 7  (category 为了实现 taxoncode and 操作，将nested放置在es_op中
	category_list(ES_Field.category_list, root), cg_category_code(
			ES_Field.category_code, category_list), cg_category_name(
			ES_Field.category_name, category_list), cg_short_name(
			ES_Field.short_name, category_list), cg_taxoncode(
			ES_Field.taxoncode, category_list), cg_taxoncode_id(
			ES_Field.taxoncode_id, category_list), cg_taxonname(
			ES_Field.taxonname, category_list), cg_taxonpath(
			ES_Field.taxonpath, category_list),

	// coverages 4
	coverages(ES_Field.coverages, root) {
		@Override
		public QueryBuilder build(QueryBuilder objecdQueryBuilder) {
			return QueryBuilders.nestedQuery(coverages.toString(),
					objecdQueryBuilder);
		}
	},
	cv_strategy(ES_Field.strategy, coverages), cv_target(ES_Field.target,
			coverages), cv_target_title(ES_Field.target_title, coverages), cv_target_type(
			ES_Field.target_type, coverages),

	// techinfo 8
	tech_info_list(ES_Field.tech_info_list, root) {

		@Override
		public QueryBuilder build(QueryBuilder objecdQueryBuilder) {
			return QueryBuilders.nestedQuery(tech_info_list.toString(),
					objecdQueryBuilder);
		}
	},
	ti_entry(ES_Field.entry, tech_info_list), ti_format(ES_Field.format,
			tech_info_list), ti_location(ES_Field.location, tech_info_list), ti_md5(
			ES_Field.md5, tech_info_list), ti_requirements(
			ES_Field.requirements, tech_info_list), ti_size(ES_Field.size,
			tech_info_list), ti_title(ES_Field.title, tech_info_list),ti_secure_key(ES_Field.secure_key, tech_info_list),
			ti_printable(ES_Field.printable, tech_info_list),

	// requirements 7
	ti_r_name(ES_Field.name, ti_requirements), ti_r_type(ES_Field.type,
			ti_requirements), ti_r_value(ES_Field.value, ti_requirements), ti_r_min_version(
			ES_Field.min_version, ti_requirements), ti_r_max_version(
			ES_Field.max_version, ti_requirements), ti_r_installation(
			ES_Field.installation, ti_requirements), ti_r_installation_file(
			ES_Field.installation_file, ti_requirements),

	// teachingmaterials, guidancebooks,ebooks
	// isbn, dbattachments, criterion,
	// // questions
	// discrimination, dbanswer, dbitem_content, dbcriterion, score, secrecy,
	// modified_difficulty, ext_difficulty, modified_discrimination, used_time,
	// exposal_date, auto_remark, ;
	;
	private ES_SearchField parent;
	private ES_Field esField;

	private ES_SearchField(ES_Field esField, ES_SearchField parent) {
		this.parent = parent;
		this.esField = esField;
	}

	private static Map<String, ES_SearchField> stringToEnumMap = new HashMap<String, ES_SearchField>();
	static {
		for (ES_SearchField es_SearchField : ES_SearchField.values()) {
			stringToEnumMap.put(es_SearchField.toString(), es_SearchField);
		}
	}

	public static ES_SearchField fromString(String name) {
		return stringToEnumMap.get(name);
	}

	@Override
	public String toString() {
		return super.toString();
	}

	public ES_SearchField getParent() {
		return parent;
	}

	public ES_Field getES_Field() {
		return this.esField;
	}

	public QueryBuilder build(QueryBuilder objecdQueryBuilder) {
		return objecdQueryBuilder;
	}

	private static final String SPLIT = ".";

	// nested . root will never be used
	public String getNameInEsWithNested() {
		if (parent == root) {
			return esField.toString();
		}
		return parent.getNameInEsWithNested() + SPLIT + esField.toString();
	}

	public static void main(String[] args) {
		System.err.println(ES_SearchField.title.getNameInEsWithNested());
		System.err.println(ES_SearchField.lc_create_time
				.getNameInEsWithNested());
		System.err.println(ES_SearchField.ti_r_installation
				.getNameInEsWithNested());
	}

}

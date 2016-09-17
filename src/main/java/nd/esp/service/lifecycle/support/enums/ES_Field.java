package nd.esp.service.lifecycle.support.enums;

/**
 * es 中字段名
 * 
 * @author linsm
 *
 */
public enum ES_Field {
	// base 8
	identifier, title, language, description, preview, tags, keywords, custom_properties,ndres_code,m_identifier,

	// lifeCycle 9
	life_cycle, version, status, enable, creator, publisher, provider, provider_source,provider_mode, create_time, last_update, education_info,
	// educationInfo 10
	interactivity, interactivity_level, end_user_type, semantic_density, context, age_range, difficulty, learning_time, /*
																														 * db_edu_description
																														 * ,
																														 * edu_language
																														 * ,
																														 */
	// copyRight 3
	copyright, right, /* cr_description, */author,has_right,right_start_date,right_end_date,
	/* primary_category, code, */

	// category 7
	category_list, category_code, category_name, short_name, taxoncode, taxoncode_id, taxonname, taxonpath,

	// coverages 4
	coverages, strategy, target, target_title, target_type,

	// techinfo 8
	tech_info_list, entry, format, location, md5, requirements, size, /* title, */ secure_key, printable,

	// requirements 7
	name, type, value, min_version, max_version, installation, installation_file,

	// teachingmaterials, guidancebooks,ebooks
	 isbn, attachments, criterion,
	 // questions
	 ext_properties,
	 discrimination, answer, item_content, /*criterion,*/ score, secrecy,
	 modified_difficulty, ext_difficulty, modified_discrimination, used_time,
	 exposal_date, auto_remark, ;

}

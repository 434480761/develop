package nd.esp.service.lifecycle.daos.titan;

import nd.esp.service.lifecycle.daos.titan.inter.TitanIndexOperation;
import org.springframework.stereotype.Repository;


@Repository
public class TitanIndexOperationImpl implements TitanIndexOperation {

	private static final String BACKING_INDEX = "search";


	/* (non-Javadoc)
	 * @see nd.esp.service.lifecycle.daos.titan.TitanIndexOperation#createSchema()
	 */
	@Override
	public boolean createSchema() {
		//FIXME
		// Create Schema
//		TitanManagement mgmt = graph.openManagement();
//		final PropertyKey identifier = mgmt
//				.makePropertyKey(ES_Field.identifier.toString())
//				.dataType(String.class).make();
//
//		final PropertyKey title = mgmt
//				.makePropertyKey(ES_Field.title.toString())
//				.dataType(String.class).make();
//
//		// category
//		final PropertyKey category = mgmt
//				.makePropertyKey(ES_Field.category_list.toString())
//				.dataType(String.class).cardinality(Cardinality.SET).make();
//		// lifecycle
//		final PropertyKey createTime = mgmt
//				.makePropertyKey(ES_Field.create_time.toString())
//				.dataType(Long.class).make();
//		final PropertyKey lastUpdate = mgmt
//				.makePropertyKey(ES_Field.last_update.toString())
//				.dataType(Long.class).make();
//
//		// relation
//		final PropertyKey relationType = mgmt.makePropertyKey("relation_type")
//				.dataType(String.class).make();
//
//		// coverage
//		final PropertyKey targetType = mgmt
//				.makePropertyKey(ES_Field.target_type.toString())
//				.dataType(String.class).make();
//		final PropertyKey target = mgmt
//				.makePropertyKey(ES_Field.target.toString())
//				.dataType(String.class).make();
//		final PropertyKey strategy = mgmt
//				.makePropertyKey(ES_Field.strategy.toString())
//				.dataType(String.class).make();
//
//		// index
//		mgmt.buildIndex(ES_Field.identifier.toString(), Vertex.class)
//				.addKey(identifier).unique().buildCompositeIndex();
//		mgmt.buildIndex(ES_Field.title.toString(), Vertex.class).addKey(title)
//				.buildCompositeIndex();
//		
//		mgmt.buildIndex(ES_Field.create_time.toString(), Vertex.class)
//				.addKey(createTime).buildMixedIndex(BACKING_INDEX);
//		mgmt.buildIndex(ES_Field.last_update.toString(), Vertex.class)
//				.addKey(lastUpdate).buildMixedIndex(BACKING_INDEX);
//		
//		mgmt.buildIndex(ES_Field.category_list.toString(), Vertex.class)
//		.addKey(category).unique().buildCompositeIndex();
//		
//		mgmt.buildIndex(ES_Field.coverages.toString(), Vertex.class).addKey(targetType)
//				.addKey(target).addKey(strategy).buildCompositeIndex();
//
//		// edge
//		mgmt.makeEdgeLabel("cover").multiplicity(Multiplicity.MULTI)
//				.make();
//		mgmt.makeEdgeLabel("relate").multiplicity(Multiplicity.MULTI)
//				.signature(relationType).make();
//
//		// vertex
//		mgmt.makeVertexLabel(ResourceNdCode.coursewares.toString()).make();
//		mgmt.makeVertexLabel(ResourceNdCode.questions.toString()).make();
//		mgmt.makeVertexLabel(ResourceNdCode.assets.toString()).make();
//		
//		mgmt.makeVertexLabel(ES_Field.coverages.toString()).make();
//		
//		mgmt.commit();

		return true;
	}

}

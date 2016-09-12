package nd.esp.service.lifecycle.repository.model;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;


@Entity
@Table(name = "icrs_resource")
@NamedQueries({
    @NamedQuery(name="queryAssetsByCategory",query="SELECT e FROM Asset e,ResourceCategory rc WHERE e.primaryCategory = 'assets' AND rc.primaryCategory = 'assets' AND e.enable = true AND e.identifier = rc.resource AND rc.taxoncode = :category ORDER BY e.dbcreateTime desc"),
    @NamedQuery(name="queryInsTypesByCategory",query="SELECT e FROM Asset e,ResourceCategory rc WHERE e.primaryCategory = 'assets' AND rc.primaryCategory = 'assets' AND e.enable = true AND e.identifier = rc.resource AND rc.taxoncode = :category AND e.title like :likeName ORDER BY e.dbcreateTime desc"),
    @NamedQuery(name="queryAssetsBySourceId",query="SELECT t FROM Asset e,Asset t,ResourceRelation rr,ResourceCategory rc WHERE e.primaryCategory = 'assets' AND t.primaryCategory = 'assets' AND rc.primaryCategory = 'assets' AND rr.resType = 'assets' AND rr.resourceTargetType = 'assets' AND e.enable = true AND t.enable = true AND e.identifier = rr.sourceUuid AND t.identifier = rr.target AND t.identifier = rc.resource AND e.identifier = :sourceId AND rc.taxoncode = :category ORDER BY t.dbcreateTime desc")
})
public class Icrs{

}

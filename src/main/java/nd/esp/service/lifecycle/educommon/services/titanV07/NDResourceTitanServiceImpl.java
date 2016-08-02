package nd.esp.service.lifecycle.educommon.services.titanV07;

import nd.esp.service.lifecycle.daos.titan.inter.TitanCommonRepository;
import nd.esp.service.lifecycle.educommon.models.ResourceModel;
import nd.esp.service.lifecycle.services.titan.TitanResultParse;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.enums.ES_SearchField;
import nd.esp.service.lifecycle.utils.StringUtils;
import nd.esp.service.lifecycle.utils.TitanScritpUtils;
import org.apache.tinkerpop.gremlin.driver.Result;
import org.apache.tinkerpop.gremlin.driver.ResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;

import java.util.*;

/**
 * Created by liuran on 2016/8/1.
 */
@Repository
public class NDResourceTitanServiceImpl implements NDResourceTitanService {
    private final static Logger LOG = LoggerFactory.getLogger(NDResourceTitanServiceImpl.class);

    @Autowired
    private TitanCommonRepository titanCommonRepository;

    @Override
    public ResourceModel getDetail(String resourceType, String uuid, List<String> includeList, Boolean isAll) {

        return getDetailOnlyOne(resourceType, uuid, includeList, isAll);
    }

    @Override
    public List<ResourceModel> batchDetail(String resourceType, Set<String> uuidSet, List<String> includeList) {
        List<ResourceModel> resourceModelList = new ArrayList<>();
        List<String> uuids = new ArrayList<>();
        uuids.addAll(uuidSet);
        Map<TitanScritpUtils.KeyWords, Object> resultScript = TitanScritpUtils.buildGetDetailScript(resourceType,uuids,includeList,false);
        String script = resultScript.get(TitanScritpUtils.KeyWords.script).toString();
        Map<String, Object> params = (Map<String, Object>) resultScript.get(TitanScritpUtils.KeyWords.params);
        System.out.println(script);
        ResultSet resultSet = null;
        try {
            resultSet = titanCommonRepository.executeScriptResultSet(script, params);
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage());
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "LC/TITAN", "submit script and has errors");
        }

        List<String> result = new ArrayList<String>();
        Iterator<Result> iterator = resultSet.iterator();
        while (iterator.hasNext()) {
            result.add(iterator.next().getString());
        }

        String mainResult = null;
        List<String> otherLines = new ArrayList<String>();
        String taxOnPath = null;
        for (String line : result) {
            if (line.contains(ES_SearchField.cg_taxonpath.toString())) {
                Map<String, String> map = TitanResultParse.toMap(line);
                taxOnPath = map.get(ES_SearchField.cg_taxonpath.toString());
            } else if (line.contains(ES_SearchField.lc_create_time.toString())) {
                System.out.println(line);
                mainResult = line;
            } else {
                otherLines.add(line);
            }
        }
        return null;
    }

    private ResourceModel getDetailOnlyOne(String resourceType, String uuid, List<String> includeList, Boolean isAll){
        List<String> uuids = new ArrayList<>();
        uuids.add(uuid);
        Map<TitanScritpUtils.KeyWords, Object> resultScript = TitanScritpUtils.buildGetDetailScript(resourceType,uuids,includeList,isAll);
        String script = resultScript.get(TitanScritpUtils.KeyWords.script).toString();
        Map<String, Object> params = (Map<String, Object>) resultScript.get(TitanScritpUtils.KeyWords.params);
        System.out.println(script);
        ResultSet resultSet = null;
        try {
            resultSet = titanCommonRepository.executeScriptResultSet(script, params);
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage());
            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "LC/TITAN", "submit script and has errors");
        }

        List<String> result = new ArrayList<String>();
        Iterator<Result> iterator = resultSet.iterator();
        while (iterator.hasNext()) {
            result.add(iterator.next().getString());
        }

        String mainResult = null;
        List<String> otherLines = new ArrayList<String>();
        String taxOnPath = null;
        for (String line : result) {
            if (line.contains(ES_SearchField.cg_taxonpath.toString())) {
                Map<String, String> map = TitanResultParse.toMap(line);
                taxOnPath = map.get(ES_SearchField.cg_taxonpath.toString());
            } else if (line.contains(ES_SearchField.lc_create_time.toString())) {
                System.out.println(line);
                mainResult = line;
            } else {
                otherLines.add(line);
            }
        }
        if(StringUtils.isEmpty(mainResult)){
            LOG.info("never created");

            LOG.error(LifeCircleErrorMessageMapper.ResourceNotFound.getMessage() + " resourceType:" + resourceType
                    + " uuid:" + uuid);

            throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.ResourceNotFound.getCode(),
                    LifeCircleErrorMessageMapper.ResourceNotFound.getMessage()
                            + " resourceType:" + resourceType + " uuid:" + uuid);
        }
        return TitanResultParse.parseResource(
                resourceType, mainResult, otherLines, taxOnPath);
    }
}

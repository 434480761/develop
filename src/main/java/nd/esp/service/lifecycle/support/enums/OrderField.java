package nd.esp.service.lifecycle.support.enums;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nd.esp.service.lifecycle.educommon.models.ResClassificationModel;
import nd.esp.service.lifecycle.educommon.models.ResTechInfoModel;
import nd.esp.service.lifecycle.educommon.models.ResourceModel;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.utils.CollectionUtils;

import org.springframework.http.HttpStatus;

/**
 * 用于实时查询排序
 * 
 * @author linsm
 */
public enum OrderField {
    lc_create_time {
        @Override
        int compare(ResourceModel firstModel, ResourceModel secondModel) {
            return firstModel.getLifeCycle().getCreateTime()
                    .compareTo(secondModel.getLifeCycle().getCreateTime());
        }

        @Override
        public void addInclude(List<String> includes) {
            if (!includes.contains("LC")) {
                includes.add("LC");
            }
        }
    },
    lc_last_update {
        @Override
        int compare(ResourceModel firstModel, ResourceModel secondModel) {
            return firstModel.getLifeCycle().getLastUpdate()
                    .compareTo(secondModel.getLifeCycle().getLastUpdate());
        }

        @Override
        public void addInclude(List<String> includes) {
            if (!includes.contains("LC")) {
                includes.add("LC");
            }
        }
    },
    lc_status {
        @Override
        int compare(ResourceModel firstModel, ResourceModel secondModel) {
            return firstModel.getLifeCycle().getStatus()
                    .compareTo(secondModel.getLifeCycle().getStatus());
        }

        @Override
        public void addInclude(List<String> includes) {
            if (!includes.contains("LC")) {
                includes.add("LC");
            }
        }
    },
    title {
        @Override
        int compare(ResourceModel firstModel, ResourceModel secondModel) {
            return firstModel.getTitle().compareTo(secondModel.getTitle());
        }
    },
    ti_size {
        @Override
        int compare(ResourceModel firstModel, ResourceModel secondModel) {
            long firstModelSize = 0L;
            for (ResTechInfoModel techInfo : firstModel.getTechInfoList()) {
                if ("href".equals(techInfo.getTitle())) {
                    firstModelSize = techInfo.getSize();
                    break;
                }
            }
            long secondeModelSize = 0L;
            for (ResTechInfoModel techInfo : secondModel.getTechInfoList()) {
                if ("href".equals(techInfo.getTitle())) {
                    secondeModelSize = techInfo.getSize();
                    break;
                }
            }
            return (int) (firstModelSize - secondeModelSize);
        }

        @Override
        public void addInclude(List<String> includes) {
            if (!includes.contains("TI")) {
                includes.add("TI");
            }
        }
    },
    cg_taxoncode {
        @Override
        int compare(ResourceModel firstModel, ResourceModel secondModel) {
            // taxoncode 以 "RL" 开头才排序，不以 "RL" 开头则默认设为空字符串("")
            String firstModelTaxoncode = "";
            for (ResClassificationModel category : firstModel.getCategoryList()) {
                if (category.getTaxoncode().startsWith("RL")) {
                    firstModelTaxoncode = category.getTaxoncode();
                }
            }
            String secondModelTaxoncode = "";
            for (ResClassificationModel category : secondModel.getCategoryList()) {
                if (category.getTaxoncode().startsWith("RL")) {
                    secondModelTaxoncode = category.getTaxoncode();
                }
            }
            return firstModelTaxoncode.compareTo(secondModelTaxoncode);
        }

        @Override
        public void addInclude(List<String> includes) {
            if (!includes.contains("CG")) {
                includes.add("CG");
            }
        }
    },
    sta_key_value {
        @Override
        int compare(ResourceModel firstModel, ResourceModel secondModel) {
        	// 请求参数：orderby=statisticals desc&statistics_type=votes&statistics_platform=all
            Double first = firstModel.getStatisticsNum();
            Double second = secondModel.getStatisticsNum();
            if (first != null && second == null) {
                return 1;
            }

            if (first == null && second != null) {
                return -1;
            }

            if (first == null && second == null) {
                return 0;
            }
            return first.compareTo(second);
        }
    },
    top {
        @Override
        int compare(ResourceModel firstModel, ResourceModel secondModel) {
            return compareStatisticsNum(firstModel, secondModel, "top");
        }
    },
    scores {
        @Override
        int compare(ResourceModel firstModel, ResourceModel secondModel) {
            return compareStatisticsNum(firstModel, secondModel, "scores");
        }
    },
    votes {
        @Override
        int compare(ResourceModel firstModel, ResourceModel secondModel) {
            return compareStatisticsNum(firstModel, secondModel, "votes");
        }
    },
    views {
        @Override
        int compare(ResourceModel firstModel, ResourceModel secondModel) {
            return compareStatisticsNum(firstModel, secondModel, "views");
        }
    },
    m_identifier {
        @Override
        int compare(ResourceModel firstModel, ResourceModel secondModel) {

            if (firstModel.getmIdentifier() != null && secondModel.getmIdentifier() == null) {
                return 1;
            }
            if (firstModel.getmIdentifier() == null && secondModel.getmIdentifier() != null) {
                return -1;
            }
            if (firstModel.getmIdentifier() != null && secondModel.getmIdentifier() != null) {
                return firstModel.getmIdentifier().compareTo(secondModel.getmIdentifier());
            }
            return 0;
        }
    },
    lc_version {
        @Override
        int compare(ResourceModel firstModel, ResourceModel secondModel) {
            return firstModel.getLifeCycle().getVersion().compareTo(secondModel.getLifeCycle().getVersion());
        }
        
        @Override
        public void addInclude(List<String> includes) {
            if (!includes.contains("LC")) {
                includes.add("LC");
            }
        }
    },
    ;

    abstract int compare(ResourceModel firstModel, ResourceModel secondModel);

    public void addInclude(List<String> includes) {
        // 默认什么都不做
    }

    public int compareStatisticsNum(ResourceModel firstModel, ResourceModel secondModel, String type) {
        Double first = 0D;
        Double second = 0D;
        if (CollectionUtils.isNotEmpty(firstModel.getStatisticsItems())) {
            Double value = firstModel.getStatisticsItems().get(type);
            if (value != null) {
                first = value;
            }
        }
        if (CollectionUtils.isNotEmpty(secondModel.getStatisticsItems())) {
            Double value = secondModel.getStatisticsItems().get(type);
            if (value != null ) {
                second = value;
            }
        }

		return first.compareTo(second);
    }
    
    /**
     * 对 ResourceModel进行排序
     * 
     * @param fields
     *            顺序字段,重要在前（与MySQL一致）
     * @param orders
     *            与fields一一对应，使用方保证（ASC，DESC）
     * @return
     */
    public static Comparator<ResourceModel> comparator(List<String> fields,
            List<String> orders) {

        final List<OrderField> orderFieldEnumList = OrderField
                .fromString(fields);
        final List<Order> orderEnumList = Order.fromString(orders);
        return new Comparator<ResourceModel>() {

            @Override
            public int compare(ResourceModel o1, ResourceModel o2) {
                int value = 0;
                for (int i = 0; i < orderFieldEnumList.size(); i++) {
                    value = orderFieldEnumList.get(i).compare(o1, o2);
                    if (value != 0) {
                        switch (orderEnumList.get(i)) {
                        case ASC:
                            return value;
                        case DESC:
                            return -value;
                        default:
                            throw new LifeCircleException(
                                    HttpStatus.INTERNAL_SERVER_ERROR,
                                    "LC/titan/search",
                                    "order direction is invalid");
                        }
                    }
                }
                return value;
            }
        };
    }

    private static Map<String, OrderField> map = new HashMap<String, OrderField>();
    static {
        for (OrderField orderField : OrderField.values()) {
            map.put(orderField.toString(), orderField);
        }
    }

    private static OrderField fromString(String orderField) {
        return map.get(orderField);
    }

    public static List<OrderField> fromString(List<String> orderFieldList) {
        List<OrderField> orderFieldsEnumList = new ArrayList<OrderField>();
        if (CollectionUtils.isNotEmpty(orderFieldList)) {
            for (String orderField : orderFieldList) {
                OrderField orderFieldEnum = fromString(orderField);
                if (orderFieldEnum == null) {
                    throw new LifeCircleException(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "LC/titan/search", orderField+" has not supported");
                }
                orderFieldsEnumList.add(orderFieldEnum);
            }
        }
        return orderFieldsEnumList;
    }

    static private enum Order {
        ASC, DESC, ;

        private static Map<String, Order> map = new HashMap<String, Order>();
        static {
            for (Order order : Order.values()) {
                map.put(order.toString(), order);
            }
        }

        static private Order fromString(String order) {
            return map.get(order);
        }

        public static List<Order> fromString(List<String> orderList) {
            List<Order> ordersEnumList = new ArrayList<Order>();
            if (CollectionUtils.isNotEmpty(orderList)) {
                for (String orderField : orderList) {
                    Order orderFieldEnum = fromString(orderField);
                    if (orderFieldEnum == null) {
                        throw new LifeCircleException(
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                "LC/titan/search", "order direction is invalid");
                    }
                    ordersEnumList.add(orderFieldEnum);
                }
            }
            return ordersEnumList;
        }
    }
}

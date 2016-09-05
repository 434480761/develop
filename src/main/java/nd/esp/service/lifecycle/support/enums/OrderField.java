package nd.esp.service.lifecycle.support.enums;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;

import nd.esp.service.lifecycle.educommon.models.ResourceModel;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.utils.CollectionUtils;

/**
 * 用于实时查询排序
 * 
 * @author linsm
 *
 */
public enum OrderField {
	lc_create_time {
		@Override
		int compare(ResourceModel firstModel, ResourceModel secondModel) {
			return firstModel.getLifeCycle().getCreateTime()
					.compareTo(secondModel.getLifeCycle().getCreateTime());
		}
	},
	lc_last_update {
		@Override
		int compare(ResourceModel firstModel, ResourceModel secondModel) {
			return firstModel.getLifeCycle().getLastUpdate()
					.compareTo(secondModel.getLifeCycle().getLastUpdate());
		}
	},
	title {
		@Override
		int compare(ResourceModel firstModel, ResourceModel secondModel) {
			return firstModel.getTitle().compareTo(secondModel.getTitle());
		}
	},
	;

	abstract int compare(ResourceModel firstModel, ResourceModel secondModel);

	/**
	 * 对 ResourceModel进行排序
	 * 
	 * @param fields
	 *            顺序字段,重要在前（与MySQL一致）
	 * @param orders
	 *            与fields一一对应，使用方保证（ASC，DESC）
	 * @return
	 */
	public Comparator<ResourceModel> comparator(List<String> fields,
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
							"LC/titan/search", "order field not support");
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

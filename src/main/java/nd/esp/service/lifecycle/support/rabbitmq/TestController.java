//package nd.esp.service.lifecycle.support.rabbitmq;
//
//import com.alibaba.fastjson.JSONObject;
//import com.lcmq.component.listener.OnMessageListener;
//import com.lcmq.component.model.MqMessage;
//
////@RestController
////@RequestMapping("/test")
//public class TestController {
//	
//	//@Autowired
//	private ConsumerExecutor consumerExecutor;
//	
//	//@Autowired
//	private ProducerExecutor producerExecutor;
//	
////	@RequestMapping(value = "/mq/p",method=RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
//	public void testSend() throws Exception {
//		
//		for(int i=0;i<1000;i++){
//			JSONObject object = new JSONObject();
//			object.put("lc-test" + i, "生产者" + i);
//			producerExecutor.startProducer(object);
//		}
//	}
//	
////	@RequestMapping(value = "/mq",method=RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
//    public void testMq(){
//		consumerExecutor.startConsumer(RabbitMqConstant.QUEUE_NAME, new OnMessageListener() {
//			
//			@Override
//			public JSONObject handleMessage(MqMessage mqMessage) throws Exception {
//				if(mqMessage != null && mqMessage.getObject() != null){
//					JSONObject jsonObject = mqMessage.getObject();
//					if(jsonObject != null && jsonObject.size() > 0){
//						for(String key : jsonObject.keySet()){
//							if(key.startsWith("lc-test")){
//								System.out.println(jsonObject.get(key));
//							}
//						}
//					}
//				}else{
//					System.out.println(mqMessage.toString());
//					System.out.println("未找到符合条件的消息");
//				}
//				
//				return null;
//			}
//		});
//    }
//}

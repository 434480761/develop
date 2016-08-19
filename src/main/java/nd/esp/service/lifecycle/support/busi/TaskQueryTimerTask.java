package nd.esp.service.lifecycle.support.busi;

import nd.esp.service.lifecycle.educommon.services.impl.CommonServiceHelper;
import nd.esp.service.lifecycle.services.task.v06.QueryTaskService;
import nd.esp.service.lifecycle.support.enums.SynVariable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class TaskQueryTimerTask {
    private final Logger LOG = LoggerFactory.getLogger(TaskQueryTimerTask.class);
    
    @Autowired
    private QueryTaskService queryTaskService;
    
    @Autowired
    private CommonServiceHelper commonServiceHelper;
    
    //cron="0/15 * *  * * ? "
    //@Scheduled(fixedDelay=10000)
    public void runQuery() {  
        if(commonServiceHelper.queryAndUpdateSynVariable(SynVariable.queryAsyncTask.getValue()) == 0){
            return;
        }
        
        queryTaskService.QueryAllRunningTaskStatus();
        
        commonServiceHelper.initSynVariable(SynVariable.queryAsyncTask.getValue());
    } 
    
        
    public static void main(String[] args) {
        //String url = "http://192.168.46.101:8080/task-server-webapp/concurrent/service/proxy/get-executions-result?executionIds=";
        
        String ids = "004f0044-d3ae-41cf-80cf-f74aed2c258e,0000e8e5-6f20-4c60-bab8-2781c9c6e5d5,002366a6-86c5-4e50-a6a8-33b8341773d9,fe929b02-5099-4b5a-9826-31b2945732f4,bedc9f01-4f4a-4323-8e1c-b93b8c9fbfb0,9c934f3b-53e6-442b-8c45-8a6aaf947e23,6086c0aa-b09a-42dc-b889-86efc64019a8,182de335-2cca-4b8e-925a-02dd79896e8e,8b058414-962f-4b5d-81e9-cdf0b4800744,00c3cb41-5830-4c01-86fd-f8d7b75a987c,138cc056-8a6c-43ce-90b0-b1aeed46159d,dc6b1826-3b2e-43f0-aa5d-707494a3404b,2d7eb659-7b02-453f-82e8-71f5588925e2,2d7eb659-7b02-453f-82e8-71f5588925e2,37581e11-d1a6-47f0-8bb6-dde7e7e11cc6,d69764de-8e03-4e13-847c-dfc145afab59,53c6706f-83b5-43de-936e-37cf8818c2c9,3f318f1d-bf72-41c1-93b1-60af893f51d0,3b82041a-722c-42bc-a13d-0910a4332b48,6423a2e9-1eb5-4734-a64b-b0218235f3bb,3f0c5dd3-4cf1-4bb0-8e4a-3fb2b0a68927,413076e4-3902-4102-85e5-e9440f60196f,d2e1f2c4-e198-4c4b-bbdf-7d92a9881332,ed2d09b6-d4e1-4953-82e6-b72630debecf,2452faca-e13a-479f-9e69-8d6db9d6efd2,0b8cb616-eefe-47ce-83b0-c92dda71bb19,5fa24afd-966c-4cee-b042-20e532af21d6,fc9aac60-1309-4f28-8e59-fcf1236d0d6f,2bcfe1d7-a354-4af3-9b2e-440f659c0a81,4eaaa269-44e4-4123-b34e-de58cbabff2e,0a42f957-b930-4127-a4a5-6e3e06767c63,7bec33de-3485-49ae-b98a-5c83abe8e405,4830684e-6b76-481a-bd3c-a25a635608e3,c4da96db-7114-44fa-8821-cf2c8d9295d1,1a8cf270-ba63-46ed-9113-fa749147732f,71547de5-bbe4-4cb7-9872-e37624e23fc9,cffe018c-1243-4c36-99af-c83e8ac690f4,bff34212-366d-4cd7-94c1-0e4caf009c9e,af138b98-fde5-4366-a754-c1426136d2dc,18f5c864-904c-42e4-86fb-39427c7f3cfa,093d013f-d29b-44c7-8ab0-a6f2bd51a537,8cc6cdf5-8f3c-494f-9f63-8b75b563a48e,a26b713d-80d2-47de-8625-e14041b02458,b2a02c9b-8fc9-4741-9237-b7ff19563c1c,feeb4a47-c639-428e-858b-d40e35fd02db,fbe80f9f-6683-4f2e-a69a-b4cbfee67525,e4dd6376-adeb-484c-ae43-0af41d3e0600,958b463c-22f8-4278-8034-7007392d88cb,215ffeda-2397-4c1c-a391-0651d2fddc0f,6e979113-d43d-41b0-82cd-a5f73f94e0ef,98abe2e8-8a7b-4672-b925-4634b979dbfb,f822773a-9b2c-4cae-b8a7-1ba98626d9c4,37688a02-fb09-43f0-8584-158cb0457047,59765b02-0099-4d5c-95b3-2af438cf0cf7,33b74196-2469-420f-ae8f-3c0dfa05bdba,00bb5740-e97e-4f92-b8de-d8cf0cb85dae,59e27864-6411-44f9-b2e8-e42204a88156,64c87432-cb99-47f6-8566-46e6db4bc956,2319df3f-188e-4c32-a443-aa4f55c782cf,eca73dc5-4291-4231-8815-bf8daf165db9,96ec2baf-3835-4517-89a5-2fba80839028,97a541c0-1b7e-4e2f-9458-27fffea1a756,c278a4cc-bedd-4f59-a039-e56fda80bc05,e1d58578-a211-4497-8a71-7feb160b155e,7f040f48-2ae7-40cc-8949-591223f77a3b,f57de8e5-994a-494f-acc7-c6d2dde4d79a,661dcf71-46fa-4d72-8244-cd6ff69a8586,717dbdcc-c4aa-49ea-a0a2-4b53e69ad156,4a2383dd-a7b6-43c0-ae2f-b70e690325b1,8d1eb9e6-e008-43f4-8df0-27b377dfabc3,3fea27b2-067d-402e-88d4-9a0d87e25139,da40936d-efcf-4442-a111-0c28c3ff015f,0598bab4-4538-4bc3-b5c4-9ffdbbc0a376,781606d1-a9ab-4305-90d4-968c24894ac1,d28cad9d-570e-408d-9833-bfd6ce12bb58,6c170bd3-7141-4118-8018-4ea3f155a7da,10d78fca-3d60-4217-8f97-27ceab98ab9d,b1f1a9a6-d65b-4946-9a31-a3bd474b91d2,cd0582f7-a861-4525-ad52-2df64c3fe6e3,a957c909-3d10-4554-9cc5-de92eac2540a,1cb247db-ce8c-4893-90c2-85a9272077b9,97cff953-34fd-436e-b2ed-9bf5569a3364,8b730521-088e-4352-b00d-073e60685244,d6af8ea5-04ad-4a69-b45f-fa5e86dcedbb,525abed2-f357-4d53-bb58-b17aec6cf650,38098236-fe92-42c9-9635-efe7844d0242,2d9df75b-0afe-4f7c-b17b-3ff52d30594d,b3abdab7-9549-4ea6-a828-4e950dca08fc,eb8bb267-3c94-42dd-ba07-a7091626f7cf,745e57e9-33ba-40ad-a440-adba6c1f6662,9aea63bf-7719-4ecd-aee3-9244a5ca6fb5,d87993a1-d94c-4269-b90b-2f3260af878e,00c099d4-4a1a-4c78-89a8-532375ff204f,8907f583-5cf5-4f85-86df-9f751e99b734,4414d95f-6d2d-4c1b-8225-2c6847238dbd,ff715675-c677-44ca-ba97-0d5785efae53,0000bf30-58af-4c95-a213-7d6be7b605a0,09647af8-bd04-4f4a-859d-1dd5ecf3c619,62c72765-db9b-4469-9680-b953f0aa402a,291c1a04-d3d8-4256-bd6b-45718ad1b6a6,ad8af150-bb1e-47eb-ae43-36e7db9c1de4,92101462-9042-4140-b746-f25a13fa92db,c9ac88d2-c4cc-48bc-afc7-6926c0a4e2c9,eec83f27-e6dc-4317-bee8-69ef72485ff0,f9ad3566-eaa6-493c-8a2b-f6ae9724048c,d5fc7dc8-75a0-4026-ac1f-68eb77b35efe,0db757b0-8751-43f4-b971-71fad0493890,8531ef34-d5e0-4bb4-8c00-a75792d488e5,6243d534-f1b2-466e-baa7-82069e42e86f,60c2b6db-f3df-4282-84b4-547ae1537bc1,4a1e3851-73bd-40b9-8bb8-1414c86e4e1c,cec852c3-7749-4a4a-a345-5f3227c648c4,ae78a418-8fd1-4d97-ab76-889a62bb2f47,0e3afb35-7f60-4bdc-ba48-1aa69cf09c92,af8593f6-0b16-4c32-806c-fbcc3a6ed589,48c5cfc4-43c4-4649-a3a0-8b0ad6859eb1,79d43856-9843-4655-a41d-cdfd99045899,b99fdc06-d27d-46c8-ba5b-d687c9e2978a,dc81d89b-7376-4ce3-99e5-f97e906955ee,abffe214-9937-4b3e-9153-ccd06b7dde51,adc267bb-4b44-4f56-93e0-ad39e7ca93cb,4035fd50-2d4e-4354-b92e-b8dcebc8af4c,526b16e5-b0cc-47bf-8d59-03ba4df88e95,015f692e-5c22-4946-8644-d89f42369996,73be7786-884a-4cce-939e-b14411f1b7cb,76f1d14c-cc22-4080-afc9-9426268b2235,25480a1d-9c8d-4b5e-a5fe-357e8ce71135,d3a20d3d-ef34-4468-bf85-f5c46e45303b,e30a91a0-0fa8-4c16-a7de-cfc0e551795f,3e9276d8-9159-433d-a420-a3206ea1a05f,5b10e4e2-7b03-47d2-9999-4026659f15ca,1a790a66-0f85-4e7d-a111-ae62559debeb,2f87ce5f-bee0-47a8-8a8f-30b6fb4316cd,05e414f7-b8b8-44f8-8fbf-b4e50efcb3c1,3eb215a6-2c1d-4d31-8816-94e2ca176077,a11ed115-3687-4e5c-8b5f-cf7a61296052,a8223e9d-09a0-42bc-9393-e692a972e896,2c506601-a0ea-4159-9bdb-851e1e554aad,57c66d4f-82c1-4770-971b-8dfe65ebf4c6,5b142e39-dbc7-423f-9c00-a12dbbf6399a,d5907951-623f-4dd5-81b7-5fc00bef9faf,d13c918f-532b-4035-8a73-600828a3664e,aa49b331-deef-432b-b037-e03ec96e1ac7,733791de-32c4-4d3a-9942-99a2de67b7f7,65f7b2e6-996a-4e00-98b5-0ba7e769ccaa,eadeb3d3-aa28-4d9d-a403-47415f79e204,a89f677d-62c2-4847-a711-38b1c4e0153e,003eedcf-b944-46f1-950a-64212de4975a,c1ad03af-aa75-4f88-a5fb-392beccb6783,271ec7f6-e04b-4039-b274-99ce3436d512,accbe87e-f85e-4026-bea0-137ab7965bc8,fd81c37e-5fba-4d71-8452-5b4fbd6bd440,3fc6c16c-8766-458f-ae2b-5bafa1769ce6,617a8342-90c4-4517-80fc-09a144f2d77e,97d70315-a97e-453b-b08b-1a1d165db18e,699ffd56-255d-4944-a897-063c70d1ee71,f608849e-5862-4d1a-a047-d7892936ad28,59e0183b-e2b9-49ce-ae1b-dca88e96f970,6744887c-0b42-4aa4-b6c2-848d7ffa216f,71026fda-8c1e-4821-af07-f3c1f1f54cf1,9744e7e5-a681-43ef-ba7e-97cce45a2fa0,ace09e53-0c61-4514-b77e-81fee49ed782,90f6c560-e32d-4d0b-a783-67ecef6b2e30,75f23008-a404-457c-bef9-e45a831df84c,6dc13caa-e9e1-402b-8165-0ee4c00f75fe,d01f7a9d-622e-4613-b3f5-8584378ef4e3,ed8869ae-1501-4f95-b557-49e771de54e9,8616e27a-f9b6-424b-a89b-97afc5459330,a6b997d8-5dc8-42c5-b059-062775bc5e49,0cef9a73-6878-4693-a303-16e2fb13851f,e188a5f2-e60e-47a8-9178-ed35750675b3,4d93f3d5-98cc-4b9c-85fc-9bbfac3c32a0,6bdfb694-5b4a-4e6a-94ca-da4fba35a1a6,5afdf9f5-6ca0-4a9b-b782-4138fd203c55,ea1aa72d-4f2a-440a-909f-c04f40ba4571,b4835b34-ce8e-44ca-996b-31bc5d1617ce,f86edb8a-df3c-442e-8255-320c9843c41e,fe280198-d468-465c-a52a-779ec317264a,7652a509-f3de-4b51-85e6-cd2e06dd747c,2dbde55f-4ead-4a8a-9f64-52219d15aeaf,1ef23abc-07a4-4b19-9590-7b2e8ea48dcf,49c45cd9-16b8-43bf-98a5-0669427badc5,e9cd37dc-ab8a-4be2-b8ac-fad9bf23fc49,bc72e98f-6993-4333-b5bf-7822acf2bf9e,0efa90f8-dcb2-4ee8-ad5b-5f8fc355d701,1db4533c-a83d-42d5-8793-9384f4fe355e,9d5b9477-f38a-4273-85d2-a7cccd73a779,1281871c-e439-4f7e-a4ca-61eac763fff7,c6274b62-7ecc-49de-a18b-68300ed96272,907f0dc6-c636-4858-8729-2704538b8b5d,f7d6d00f-97a1-4ec5-b566-971564d36715,49ef0104-5927-43fb-b5be-f455466b730d,eb9ecddf-b293-42a2-a316-7898b9464f73,4cfc750b-6e98-4553-94bf-19427821dba1,e957e6aa-47a0-4655-9204-ccbcf852d228,fc735b4b-101d-4d11-a3ee-8bd7d4f5d8fd,e7304666-3646-4399-bfb8-1bd54d0dc0d1,c37613a7-c606-4e0d-8c65-0c6582f2de36,b4df6046-edf8-4e16-90ed-37229ff9618d,a420718c-997d-4a54-824b-6bfdc04d28cd,1e39e972-b851-480b-8d5b-99fd3f5f3993,ce909e2d-22ff-435e-9dcf-03df6f68cd61,9e5a4c73-b706-497e-be2a-eff61caef383,a87b89c5-7da3-4557-9184-f545d832b07a,307bbe1d-f493-4280-a243-1ca2065f1d67,d8a9852b-88d9-40b6-8d4c-b410d0407eb3,d44a2843-8947-4dc4-a253-413653ec2d9e,9a652cc1-47dd-4380-9611-41dfb7ccb1e2,e229b977-f249-49fa-9bb2-d48ebc697053,48c69e38-e792-4bbe-8361-99ae6f8c22cd,5f0ecf63-29be-483b-a5e8-a7f7d5deff22,761e6d46-ecce-43ba-8328-45e4838e7951,545c9684-bffa-4cf3-8dbc-ec07deb5160f,79e789c3-b8c7-42ab-a7e2-4c9967ad2abe,3b2eeaa6-d8a8-4cd1-ab75-420d4c620370,8119fae7-aa17-46c7-bea8-c9598f8ec870,1b4b9dd1-d76e-4109-880b-2bcdd7e00275,ebd86302-1fc3-4fbf-a211-75118e073a3b,0e6540ed-4a81-46dd-8524-932eb04ae977,a3716ca6-6b12-40c4-8aa0-19e259d084a2,ec5260ea-f073-4cd0-8d58-955e629a00f3,15157bb8-934d-4746-9616-639980b6989d,3223427d-3e65-425e-8d98-cf45f285cac8,6b72c53b-0c1d-4464-b300-4b562ddefec0,040bc90c-407d-4a1e-9ffc-63089c5cfe60,9a0b6bf6-d870-4f71-8c89-82384fd7b2f9,d34c285a-67fa-4744-b8dd-c91f306f13e3,0f38e839-8645-4b65-8a88-8a4f093045d2,90d671b8-d269-46ae-b37d-cefe4c698124,db9787b4-29c7-41d6-8385-0ae2614f3b71,86a42882-38ee-4ec1-b83c-3d05e959935b,24c72155-c28e-466e-825e-c9d286e195d1,2881f7f6-ae06-44b1-8761-f43bc2c33dc9,64a336af-ddc7-4655-8aab-06f6b515c33d,2b9edc85-ca15-4d4b-94c5-fd1d538fc4e6,86b9aae1-8ced-4c61-bff4-be9daa142b32,ad4946f6-2dd2-471e-86df-952e790e2a0f,3c3e4433-b402-45a7-be25-6177a50c5174,7b8968be-d559-4956-8a60-0691a4fff54b,7ed65b73-b6bc-431a-a97e-c71ad1c6a95f,693356f3-e194-4df1-a21c-1e79ee40544a,038e60c7-5999-4077-a451-b20853fa21dc,c751c91b-a2cb-4410-8adc-a002ebb63e35,1c3d9a25-0159-4a8a-b782-adac15d6a524,a7701f03-1d98-4430-9f3a-3c827541335c,bb0008da-7a05-4cf7-be89-4aed4767fd45,b6a01fb0-efe9-491c-b035-22678d62889b,7da96e80-af0c-42ea-9cab-63d4bf6ba23e,404b3da2-75fa-40e9-82e1-5e129b33cc64,1f965cf8-19a9-4b9e-9b7f-1257ff3658df,e52717d8-b333-4e8d-9e09-8dbea815d442,b3cf51c3-4e12-46d8-aa2f-f6cb736bd4ed,ccfc8b76-3968-4200-b82a-141de592be4f,9cc19aff-1793-4a22-bc00-4295186d2b95,f0055d51-c86f-4c44-b811-167927c2bc8b,6ab1db9f-2810-4f52-8c1a-8652c595bc61,553e7d4b-3cab-48bc-bdcd-e0fb949d8f48,93b4f604-4bb2-4ce3-9c23-d6ba2ff7f5b0,f47a444d-d4ae-4929-ae42-0d0e3aca3bac,e2da4023-fef1-4635-a03d-483ef75eb2d7,30b6d7d2-5a34-4447-95a3-e11e6ec782ae,2054e349-dd86-484f-9e8c-f0f24ccf719e,8106da6d-f14a-4aca-989c-1b66b084c9ee,cb160d49-abc1-4563-9157-58a8464edfcd,3237d721-bad7-452f-a1a2-6dfb135a1a0f,c0df5af8-b9ca-4078-92eb-c2ff2031d2c1,649dba42-5e66-4b7e-8515-107d98347e5e,177f1ba0-637a-4525-b664-a36ed28c29a4,d026874e-bed5-4557-9d7c-62b04dafd0ab,63d18552-02ba-44a1-8bd7-5291ce28dd51,fb2edd28-f1ee-409c-87a9-e05c5ab692ff,74f06c95-dc93-4795-8c0f-b9299c309238,8e5d1a8b-a38b-4fe0-b8cb-9c69167136fc,3fbba6be-fc13-4120-9361-535822386722,c77ed290-7910-4ca8-be5a-faa7ff9a8f5a,2cc73f84-8536-4b60-b18a-5dbbb4ebf6b3,5107e53f-c851-4cc5-bde4-0814214d3c7a,3e09b39c-e3e8-4fef-9a74-9089bd9cb64f,c55475cc-d526-44e5-8d3b-daf1b3df6c58,9b858061-7b7c-4916-9fcd-5a815e2d3ca5,2e245684-42e9-49a2-a73e-c47cc2d9126b,70e9578d-7a7f-48cb-91ee-af6f28fbb1bb,2f442aec-eb34-421f-9093-16e6ef5e7b52,707516b9-2615-4357-903e-4f50281a9c89,95d7ad33-368d-4468-8fb7-7e5bc59f42c9,32ee354b-a7a5-49fd-9da1-c7b339f2535f,ed4efcbb-c46b-48bc-aa28-77528a7e4ad1,ff949d22-22f4-4f03-bec2-7bea43ac5576,e4b4d27b-fd19-4845-a5f5-274c9b0cc6fb,cf9cce45-bdf5-48b6-9168-72208146a24a,cee30cb4-b739-4303-8e48-76f5f37e0432,c7606d3e-c7e9-47cb-af6a-10b96aa55dcd,b72bb93b-c5f2-4145-a44c-e42f1bf3bc20,aee26ac4-721b-4c0b-ac46-351ccef06337,ade5886b-ace5-4da9-84c4-8db012095c62,a920e019-5345-4574-a9a1-9a69bcea1f97,a579686a-ecaa-4519-a08c-0f45689e44c6,9c84a76d-475e-4758-8a75-c9dc8b808d1a,9a57cb83-87de-4586-935d-9bb3031df17e,984800a5-f704-4d11-8c5b-b22b2bd97065,9803c3ee-2608-49e5-9991-a58391e15644,8a82fe2f-9772-48b1-a92c-251206ed8b5e,8055804a-1450-436c-96fa-290e4fc62d27,74bcb104-44b3-443d-8f35-0e34536eb8d7,6c3a9f43-43c8-4eaf-bea5-ff558c1fa753,6c024621-1461-42ed-9642-9db402afa883,5dfb6d21-97cb-4cce-871c-60f67ccb8868,5cdc2289-a582-4bcf-826e-9fa2118a7589,5b943a11-2d59-4a22-944e-bfcd44952b45,4de6e112-51eb-4239-af40-9bcde7e9f224,4aec6886-575c-43ec-8995-a112b00b535c,461303ba-839b-4795-b75c-f062467fb233,306fd7ee-35bc-4827-b6cd-bf498a67f480,297bc743-e560-4265-b0d0-228592060c5f,209d3ebe-28ce-4c6e-80f7-2ea73444683e,200d4279-ccd8-4d3c-b8e2-6471bfdeac75,18a2ee9f-18d1-4473-bc6e-5af2e2d2060d,17d7c63c-94d1-4f52-8fb5-3a501c79e455,07c0dcc5-902d-443a-966c-b8a119ad10bb,06afd7d0-8479-49f9-9a4c-fc4b26aff05e,0509a3f7-18a2-40c4-99a6-fe183d802ee0,0355d662-9b3b-4c25-a568-08d220ed21f0,9fa55363-a2c4-43dc-9202-ab601edb2a60,2048d0bd-7df9-427b-80e8-1d36b251dc87,8f179360-76aa-4b98-b0b9-d9cdd0bffe8b,a405fde6-bfe6-4564-8383-95b151e82997,69f5c7d3-73fc-4590-900b-46b9722a88ef,b7773fd3-d2bc-438f-b04f-bb430cb4e8ee,94526832-2f66-45d9-804b-2bcab58321af,092e227e-5816-4ca1-9647-17bd08c966a8,315651b3-5de7-4a11-8eb2-da2ec06946d2,a6e70918-5004-44bc-b6e2-c11d0fb46dbd,e5dab461-f303-4d31-86a3-4c5e1ff52590,7d51027a-b48f-44c3-9fc4-334cf8f4f5ef,9a28e551-a500-4e05-88d8-5bd349f697e7,3f9ad285-236c-4fe9-96ea-d153f8326a1e,8c74357e-ceba-49e4-bc05-673b56406f19,9ee4af8c-9f7f-4a92-8462-afd41e420f31,2780f2a0-2791-459d-a278-857a68343b92,5cf42030-8280-4cbe-b130-53500b78d24a,f06e9a01-eb3c-46fa-876b-8d296781b1c0,aad1b375-1c7e-48c0-8317-0f39401f4a32,7f582b5d-4d21-48f9-9f91-bd2271c49460,a5405031-a4a3-4e65-9546-8ff4a8625206,f0838b05-da14-445d-94f7-83e7da606416,e3c53d9e-b49a-4fe4-88a5-4e67443ec121,43ef460d-0f28-4d15-ab43-72c5d8cd9a6a,e8ce4814-ecd1-41ee-b816-228bea4091ce,54810c05-c1b8-4251-83ab-342f1ca54c8c,36a2f811-5f5e-408b-958a-43cb0f4b043d,1fa71b04-381a-48ef-9a3c-d2d7e1c96794,79a3a299-98d9-4914-94c1-8cb1dd9272db,2cbd7e42-3626-4dc5-ab55-4205d601b522,35a2d1af-0cca-4b97-a18b-e6f93e17bd4a,9167fedb-8ab7-47c9-947d-5b713de4565f,d19e359e-3900-4ddf-9636-79c36ffc3570,3037579a-3a7d-4dc9-a533-0f64f6e9d1f9,ddaac079-3967-4ab0-b725-b6ba1713d3a3,3c4d94b5-3150-4d25-9482-32e3d30685ce,eb0a14fb-1d1e-41c7-a91b-667eadd0bdbb,4909f1cf-72eb-4118-948e-f50fbb5b218b,59960387-ee8a-4681-acf1-968009c84500,718d2940-3f27-458b-9c39-36fd9fdc868a,8364da6b-9ba3-45b4-90f7-f7eaff6e40e2,9d22ba00-c77d-4c96-ae8a-992484198455,aa1787d7-f912-406d-ba17-8c528e560897,b4224cdf-5b2b-40e5-b8b1-2f14aaa43444,15f108d1-cb0d-4095-94d5-09812a4b1d90,d0a33811-f9ad-4045-8a40-f16cba241455,3bed1fe9-42ad-4675-8b5c-34d3b2564b25,eaa5e3d7-5ac7-46a6-8fab-32cd26e32100,5864a48f-a167-4ffb-a54c-fe27df658a16,a8b489e6-0c74-45d4-8fef-b549e985189b,05d0bb66-2eef-46eb-aebe-093b79f728c2,b40b4085-e7e6-4b4a-9d67-73607f237926,c18ccb5b-5ce3-4308-a9cb-925cf43492dc,227ba158-4478-4f8f-a26a-2de6da4d7ac8,d010f2f7-ee24-41da-90b4-4c3b2dbc7fff,db0ca84f-c25d-41fb-bd9b-9a351bd46ea6,ea248e02-297d-4f75-b32b-483f31719f4b,46a519fe-67e8-479a-a4b8-38faad61d721,f7385bd3-7472-4c25-b939-c9693acb8ffc,57364cc0-9b74-4a87-a8d1-e2c987c62fdf,6fd834f8-4096-43b3-b403-2823c8bce525,8f1e4869-0fe3-49bc-9246-8a27c8e2beef,9b496ee0-c69f-46e2-8b91-e83f2e4ecd06,04a91725-4aa5-447c-8487-74e3becd1e22,c06fd66d-1788-4f69-8368-8f73b42d64b8,dae160d2-7deb-4854-b3ad-66371395a0b9,38f72e84-d591-4b2a-94be-54964c3ba0a2,45522110-07d5-4ca4-a6e6-e569298ad9b3,8e10bc58-f3f4-46da-9fff-85a8a98ce58d,bf7d019c-6878-4062-9681-4cbeeabcdbd2,d883a6a9-9df0-44fb-b015-8f394b4081aa,37e6b27e-1c82-45f7-832f-13bf5556a317,435e9b7b-5cb7-4da7-8d48-3a1b940c7832,f3d42c20-9cad-4cf6-9a25-93b3e3f7a290,60e5132a-dbd6-477c-9799-95898125c557,7c23be7d-c219-4b0d-b3ce-86de5dae5055,a66fb119-b110-46b1-94ed-f2d6edca1d13,b0f02cc7-fb3d-4d4b-84de-27b572d68c8f,be478c61-59b4-4305-888d-c2948ea6bfdd,cdd517b3-ee52-4dfa-ac5b-bcf22ff1a181,d84b8b43-9fc3-40b1-b1fe-0ddbde7f43a6,370f8c7e-09dc-4065-bee9-ca961af1ec8b,42b043a6-b1a3-4a4e-864c-6e36447e032a,f0c9c7b8-acbb-462d-a823-e98b87122d30,8bcdb784-0317-4239-8d3b-f2f6dea15a43,a560fd65-acff-4382-8e02-c6f359a69253,0d457260-1b8f-4752-8d33-3d40446232c9,bb35b43b-27c5-48f6-bba0-3b8442a9dded,cbc01462-99c5-4be2-8c7a-12a5af528f5f,2bf6110a-60b3-4165-8d4c-d1429a97fcc3,d6d57df0-73a8-49e1-910b-cd15f5fa8d72,35d59206-3205-43f3-8701-66b5f1d53ceb,40cb10cb-18ce-4721-8df0-7a49b6d6f8c7,5d24eadf-bccc-48eb-bb2f-80d777a1dc51,6b4f000a-d857-4005-a160-82ac4edcaeef,003539c6-cb9b-49cc-bd58-aea7280ab6e5,0c73290a-b4fd-48e6-a217-b9a4d205c8db,b916900e-fda6-4f9f-b26a-fb34695e63c9,29ff54a9-5e55-40d7-aa81-e55fdf095957,34a58d5d-fbd8-4360-b7d9-e4e7a2808ddb,ee83bac3-cea6-4aa8-a5de-881a0b38b681,fda52dd1-37f1-4fe6-906d-9bb44b3eb5aa,5cd12a5d-3709-402f-94f4-59fb7060955f,6a5a9f1f-d706-4d61-8970-34e53897656f,896e81af-8a19-4128-b41d-ad4856850529,9769aeca-82f6-4e2c-9620-ad8f9186e4c9,a2d6f8f4-2252-4392-a47a-e2e26a89331d,0009bf95-5353-48fe-93f2-ff64fe39fc4b,0b8abb5d-e046-4e3f-a1db-56590431c8d6,b8866e4b-76c9-4291-9cf7-d246e976ae69,88daf644-86f6-4276-ae26-c24cd74e23cc,ad49fea3-e55e-4d03-b54c-ed152bd15665,0a9c8a7b-a43e-4435-89d5-fcdd01b0ce07,b770b8ee-650c-45e8-83d8-cc2bc556dae7,19ff745c-581c-4e28-bf77-6f431e53c17f,28721fba-9a25-4ce4-96a7-77c9d3d6bbbd,31c65e63-e7b8-4692-8d4a-f30365ef1287,e1b0acaf-a7d4-4831-abe7-375c36a11088,86b76d57-c951-4b68-a3ba-10c089a73c4f,943679b1-60d9-48c8-87fb-09fe9f73d5d5,9ed6ff5b-a06b-442a-a6c8-9124a9004270,094a0e8c-9125-4b44-960b-2879d9d4e7cc,b6d1a988-c333-4f92-b9fa-a39948da5705,d2afb1da-668b-4dd5-a415-259f3ebf625b,e09d9a18-23dc-4f99-8aaa-304edc3185fc,eb4e107e-2b85-4e22-8b2b-d163f6871f4d,fb02160a-6408-4771-8cc6-25cf2f7e3754,71fd193b-7ba9-4a87-890b-c3ec6f7d1372,93a1e3de-a612-495a-a34c-9a0d349612dc,9e15ab12-abde-4d7d-a3cd-087790c3c91e,aac064a1-55a0-46a4-8667-ab9ca5e8d9a9,08f42a66-37d8-4932-a7b4-bc8e977e1d03,16f5f30a-2dfd-48ce-aac8-51eac261eb91,c4b130ad-2314-4eb9-9a78-8d234580ae2e,24b90071-dd00-493a-8ec1-a8b1b57c43d7,dcd002a5-29e6-4325-99cd-51b81ef03cd8,eae5198b-16a5-4c35-803e-dce42b089053,64ba0c17-d741-4678-b8a6-55b6036e51c4,b4213630-f9bc-4355-8e0d-cb1383ba47cf,15ccc9a0-5964-43d4-9582-97bfada846a8,2383308d-7c88-4b2d-9cb8-774b637eca6d,dc030506-97ca-4963-9b74-ab6ba8928d02,3bba09fe-3800-41c4-aaa9-32d7372ad5c4,ea360582-b6cd-47ec-850c-91e4f3dda07c,481210f6-67d6-4ef9-a4d4-8c81abe8c034,64927fb7-8ecb-436d-b63d-1e13abdf4d6c,7f628582-1413-4922-b2c4-9b2c7eaba1bf,8ff68ff0-3406-466a-a2e1-5ba4c7adaf28,b3fd1a6d-b96d-48ca-92a7-d5dddd4cd35f,12cda88a-c40d-4485-81f5-47782e81d15e,c0f6960e-d0df-4b6c-9c85-9ebeba6cbe0f,21fc884f-29ff-43f0-a6c5-83d53f4e7ccf,dafd518c-0f83-44c8-995a-333f6b1d399f,3acc8097-0a59-4e62-9742-438712362574,466954e2-5fb5-469a-b21a-729921d4c190,55cc8494-c6e7-48d4-8a37-45ec44ec0d00,63568c0d-c669-4abf-9f87-59182665eb71,6f80eb5c-29fa-4923-8eb5-7cb61c44134f,2cf7d9e7-4584-4f21-94e8-d7644cb06493,e9c7d927-a30b-41c0-ba26-29232fdcff96,548e7d88-446e-425e-b020-228be34e8201,621fe607-936b-4fc0-aa1b-0d0808a5e8f5,6e5c3353-cdf6-4ff1-aada-2de6737f68c1,7d5a8044-e37a-43b0-bb16-3e1fbf0d316d,8d776e12-45a4-44a1-97cc-afed8e8bf90c,a6c96436-100e-46a9-9dd9-f74cc3872692,031f2b28-42e4-4c46-8db3-4c37d2ea514b,e81a2efc-1c94-43a3-9d53-369dbf2691b8,43514cc9-cb10-41aa-b6fb-7437fea25ab3,f3295d53-0d5c-466b-a905-a00e5d371dbf,541d4d94-7441-4920-8c65-c2a7ee15b56b,8cc27a3a-9e48-4757-9d94-913ebaa809c5,a6315c3c-1f15-4e68-9736-356ad757d4e8,b0aa1208-0953-487e-9404-7dc7f9f0c084,0d862bb7-f085-4b68-ae87-5ff20d542d70,bd16c52c-bad4-4702-8c9f-0d9bab23bbce,1f29ae01-f66f-4c67-9912-ba1b0b7043dd,daf11f92-ebb2-4717-abf5-0a5c1dd2e6c1,3a33786b-86c9-4fc7-92dc-b87c842f4ab9,e9e35e2b-7a69-43a7-a814-394b70d9e066,f59c58f3-3f7a-4b40-a5f3-9849373d9b3d,55a024b7-38be-44d4-9593-3f7069084d79,6f015a7f-a944-4eab-9059-e61eea6b6a6c,7eb79fc3-c940-435b-95ad-9a4f000477cf,9ac71a5c-e217-4d65-8b4c-4730a8b295b6,a72d3ce4-d550-428c-9289-9a70695969b1,b304a51e-7a37-4126-a061-f3aaffd02d6e,be80c6a6-13c6-489e-8c2b-d158f0418c2e,37fcf5ad-bbfc-4aab-b927-455504934c98,f42588fd-6e9c-4c15-8232-59c4ebfaca72,026a841c-cdf4-4c16-8876-d0eca8182a10,b1c57971-3aae-4b2f-ba54-866d869b0c4e,0df60de4-86cc-461b-848c-d0d1a93a833b,2bff7bc5-b2f0-4f61-bebb-ef140c9261a5,d86ed06a-5961-4050-a349-5d6735d4c5e1,e7b64bd5-5a89-43a8-8f34-c51693fad977,42c1e3a0-9ba8-4989-8e46-cd08a6b131c0,f1a953c1-7da5-49dd-8f24-7beb3a7167c7,988b757f-8a02-4d96-a840-4f0df55345b4,0d544c4a-0de8-4542-9578-e26ec3b1d2ed,cb134097-9fcb-4edd-98d2-416cbdabfc38,d7b835fb-abe3-4bb6-811b-35206fdb42c0,e5adff4f-fcf0-4c25-a987-2c95f72ec62f,517f5481-1d78-424a-8630-62ecfdf2968f,6b6dc920-ba89-489f-83ea-3d715b98da25,a4eb203c-b51f-477a-953f-87ab4c849e24,aed322af-b135-4bba-9ca9-7185beaa3ec8,0ceee477-9f58-4295-80c7-c4eb0b6a2d14,b8a59d9b-7d60-42b3-b694-b123a3590164,1b4e4834-d3a2-483b-86ff-31dfc704e214,2932a18b-632c-409a-a7dc-e057507789f8,34e62b27-9371-466e-b13d-18e7b60f5484,4ededf13-a88a-4997-91d9-6d3247d28b12,6a619a4f-c5af-48c6-a16b-10f1bc4875e1,76d3c9c5-a773-4d40-9cdf-5a70c538fcea,89a999b0-f0a7-4723-a12d-54e563cb602f,000c6b0e-6977-495e-a299-9bc4ccbd30d2,ae1527fd-2d69-44ad-85c7-4b4cd80a0626,c653b89a-46eb-4131-92bf-32358afe18f6,d49ce7ba-3300-4a7c-b86e-2df05f7f473e,338b7e7b-2352-47ec-98ea-a3c143368efe,fcf16ec5-82ff-408a-98a6-5998b12320f4,5be23e77-dff0-4d9e-a514-68c7bc0876b2,88dfa361-6a11-44c5-9de8-e5f9c31f87db,a1549af4-30a8-47f2-8b8c-d6b0d0193ced,0b4e8b81-c14b-48b5-ace7-e800ed84a27f,b7296a96-0426-4c98-9eb7-6046e6c878a1,c5ab40f7-33f0-4269-9254-8ab6bd8211e8,3f6033af-760a-4cdd-83c7-30479ee05a60,6850406a-e161-47bd-90a9-7ff9d94248c5,b59a2cc3-cde9-4639-8202-7d1e1189eef1,c4cf0dce-c7b0-47ca-bec9-e03ab303fb6d,e1647f64-a814-4822-b418-8fdbc3ff005f,fb0b6f04-9774-4a59-8149-e295fa97bf17,5a939413-2956-4172-9dc0-e674b76ed347,9e2755dd-79b3-4ce1-bcf6-5fd5adf37525,b4224cdf-5b2b-40e5-b8b1-2f14aaa43444,15f108d1-cb0d-4095-94d5-09812a4b1d90,d19e359e-3900-4ddf-9636-79c36ffc3570,3037579a-3a7d-4dc9-a533-0f64f6e9d1f9,ddaac079-3967-4ab0-b725-b6ba1713d3a3,3c4d94b5-3150-4d25-9482-32e3d30685ce,eb0a14fb-1d1e-41c7-a91b-667eadd0bdbb,4909f1cf-72eb-4118-948e-f50fbb5b218b,59960387-ee8a-4681-acf1-968009c84500,718d2940-3f27-458b-9c39-36fd9fdc868a,8364da6b-9ba3-45b4-90f7-f7eaff6e40e2,9d22ba00-c77d-4c96-ae8a-992484198455,aa1787d7-f912-406d-ba17-8c528e560897,b40b4085-e7e6-4b4a-9d67-73607f237926,c18ccb5b-5ce3-4308-a9cb-925cf43492dc,227ba158-4478-4f8f-a26a-2de6da4d7ac8,d0a33811-f9ad-4045-8a40-f16cba241455,3bed1fe9-42ad-4675-8b5c-34d3b2564b25,eaa5e3d7-5ac7-46a6-8fab-32cd26e32100,5864a48f-a167-4ffb-a54c-fe27df658a16,a8b489e6-0c74-45d4-8fef-b549e985189b,05d0bb66-2eef-46eb-aebe-093b79f728c2,c06fd66d-1788-4f69-8368-8f73b42d64b8,d010f2f7-ee24-41da-90b4-4c3b2dbc7fff,db0ca84f-c25d-41fb-bd9b-9a351bd46ea6,ea248e02-297d-4f75-b32b-483f31719f4b,46a519fe-67e8-479a-a4b8-38faad61d721,f7385bd3-7472-4c25-b939-c9693acb8ffc,57364cc0-9b74-4a87-a8d1-e2c987c62fdf,6fd834f8-4096-43b3-b403-2823c8bce525,8f1e4869-0fe3-49bc-9246-8a27c8e2beef,9b496ee0-c69f-46e2-8b91-e83f2e4ecd06,04a91725-4aa5-447c-8487-74e3becd1e22,bf7d019c-6878-4062-9681-4cbeeabcdbd2,dae160d2-7deb-4854-b3ad-66371395a0b9,38f72e84-d591-4b2a-94be-54964c3ba0a2,45522110-07d5-4ca4-a6e6-e569298ad9b3,8e10bc58-f3f4-46da-9fff-85a8a98ce58d,be478c61-59b4-4305-888d-c2948ea6bfdd,cdd517b3-ee52-4dfa-ac5b-bcf22ff1a181,d883a6a9-9df0-44fb-b015-8f394b4081aa,37e6b27e-1c82-45f7-832f-13bf5556a317,435e9b7b-5cb7-4da7-8d48-3a1b940c7832,f3d42c20-9cad-4cf6-9a25-93b3e3f7a290,60e5132a-dbd6-477c-9799-95898125c557,7c23be7d-c219-4b0d-b3ce-86de5dae5055,a66fb119-b110-46b1-94ed-f2d6edca1d13,b0f02cc7-fb3d-4d4b-84de-27b572d68c8f,bb35b43b-27c5-48f6-bba0-3b8442a9dded,cbc01462-99c5-4be2-8c7a-12a5af528f5f,2bf6110a-60b3-4165-8d4c-d1429a97fcc3,d84b8b43-9fc3-40b1-b1fe-0ddbde7f43a6,370f8c7e-09dc-4065-bee9-ca961af1ec8b,42b043a6-b1a3-4a4e-864c-6e36447e032a,f0c9c7b8-acbb-462d-a823-e98b87122d30,8bcdb784-0317-4239-8d3b-f2f6dea15a43,a560fd65-acff-4382-8e02-c6f359a69253,0d457260-1b8f-4752-8d33-3d40446232c9";
        String [] idArr = ids.split(",");
        String url = "http://esp-lifecycle.web.sdp.101.com/v0.6/questions/";
        RestTemplate rest = new RestTemplate();
        for(String id : idArr) {
            ResponseEntity<String> response=null;
            try {
                response = rest.postForEntity(url+id+"/archive", null, String.class);
                System.out.println(response);
            } catch (RestClientException e) {
                e.printStackTrace();
            } 
        }
    }
}

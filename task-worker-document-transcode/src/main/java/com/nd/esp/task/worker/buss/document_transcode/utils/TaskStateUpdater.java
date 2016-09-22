/**
 * Copyright 2015-2099 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nd.esp.task.worker.buss.document_transcode.utils;

import java.net.URI;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 * @since 1.0.0
 * @since JDK 1.7
 * @version %version%
 * @author 990618
 * @see
 * @category task-worker-transcoding
 * @serial exclude
 */
public class TaskStateUpdater {
    // NEW, QUEUE, CONVERTING, UPLOADING, CANCELED, TERMINATED, FAILED;
    public static final String NEW_STATE = "NEW";
    public static final String QUEUE_STATE = "QUEUE";
    public static final String CONVERTING_STATE = "CONVERTING";
    public static final String UPLOADING_STATE = "UPLOADING";
    public static final String CANCELED_STATE = "CANCELED";
    public static final String TERMINATED_STATE = "TERMINATED";
    public static final String FAILED_STATE = "FAILED";
    public static final String ID_REQUEST_PARAMETER = "id";
    public static final String STATE_REQUEST_PARAMETER = "state";
    private String location;
    private RestTemplate template = new RestTemplate();

    public void update(long id, String state) {

        MultiValueMap<String, String> mvm = new LinkedMultiValueMap<String, String>();
        mvm.add(ID_REQUEST_PARAMETER, id + "");
        mvm.add(STATE_REQUEST_PARAMETER, state);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<MultiValueMap<String, String>>(mvm, headers);
        ResponseEntity<String> result = template.postForEntity(URI.create(this.location), entity, String.class);
    }

    public void setLocation(String location) {
        this.location = location;
    }

}

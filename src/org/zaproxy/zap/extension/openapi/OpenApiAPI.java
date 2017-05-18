/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2017 The ZAP Development Team
 *  
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */
package org.zaproxy.zap.extension.openapi;

import java.io.File;
import java.util.List;

import org.apache.commons.httpclient.URI;
import org.zaproxy.zap.extension.api.ApiAction;
import org.zaproxy.zap.extension.api.ApiException;
import org.zaproxy.zap.extension.api.ApiImplementor;
import org.zaproxy.zap.extension.api.ApiResponse;
import org.zaproxy.zap.extension.api.ApiResponseElement;
import org.zaproxy.zap.extension.api.ApiResponseList;

import net.sf.json.JSONObject;

public class OpenApiAPI extends ApiImplementor {

    private static final String PREFIX = "openapi";
    private static final String ACTION_IMPORT_FILE = "importFile";
    private static final String ACTION_IMPORT_URL = "importUrl";
    private static final String PARAM_URL = "url";
    private static final String PARAM_FILE = "file";
    private ExtensionOpenApi extension = null;

    public OpenApiAPI(ExtensionOpenApi ext) {
        extension = ext;
        this.addApiAction(new ApiAction(ACTION_IMPORT_FILE, new String[] { PARAM_FILE }));
        this.addApiAction(new ApiAction(ACTION_IMPORT_URL, new String[] { PARAM_URL }));
    }

    @Override
    public String getPrefix() {
        return PREFIX;
    }

    @Override
    public ApiResponse handleApiAction(String name, JSONObject params) throws ApiException {
        if (ACTION_IMPORT_FILE.equals(name)) {
            File file = new File(params.getString(PARAM_FILE));
            if (!file.exists() || !file.canRead()) {
                throw new ApiException(ApiException.Type.DOES_NOT_EXIST, file.getAbsolutePath());
            }

            extension.importOpenApiDefinition(file);

            return ApiResponseElement.OK;

        } else if (ACTION_IMPORT_URL.equals(name)) {

            try {
                List<String> errors = extension.importOpenApiDefinition(new URI(params.getString(PARAM_URL), false), false);
                
                ApiResponseList result = new ApiResponseList(name);
                for (String error : errors) {
                    result.addItem(new ApiResponseElement("warning", error));
                }

                return result;
            } catch (Exception e) {
                throw new ApiException(ApiException.Type.ILLEGAL_PARAMETER, PARAM_URL);
            }

        } else {
            throw new ApiException(ApiException.Type.BAD_ACTION);
        }
    }

}
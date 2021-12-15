package com.webarity.autocom;

import javax.faces.component.behavior.ClientBehaviorBase;

public class AutoCom extends ClientBehaviorBase {

    private final static String DEFAULT_DELIMITER = " "; 
    private final static String DEFAULT_CALLBACK_FUNC = "WebarityAutoCom.defaultCallbackFunc"; 
    private final static Integer DEFAULT_MAX_RESULTS = 50; 

    private Object source;
    private String delimiter;
    private String callbackFunc;
    private Integer maxResults;

    public Object getSource() {
        return source;
    }
    public String getCallbackFunc() {
        if (callbackFunc == null) return DEFAULT_CALLBACK_FUNC;
        if (callbackFunc.isEmpty()) return DEFAULT_CALLBACK_FUNC;
        return callbackFunc;
    }
    public void setCallbackFunc(String callbackFunc) { this.callbackFunc = callbackFunc; }

    public void setSource(Object source) {
        this.source = source;
    }

    public Integer getMaxResults() {
        if (maxResults == null || maxResults < 0) return DEFAULT_MAX_RESULTS;
        return maxResults;
    }
    public void setMaxResults(Integer maxResults) { this.maxResults = maxResults; }

    public String getDelimiter() {
        if (delimiter == null || delimiter.isEmpty()) return DEFAULT_DELIMITER;
        return delimiter;
    }
    public void setDelimiter(String delimiter) { this.delimiter = delimiter; }

    @Override
    public String getRendererType() {
        return "AutoComRenderer";
    }
}
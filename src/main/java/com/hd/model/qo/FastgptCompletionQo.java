package com.hd.model.qo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

public class FastgptCompletionQo {

    private List<Message> messages;

    private String responseChatItemId = UUID.randomUUID().toString();

    private String appId;

    private String chatId;

    private boolean detail;

    private boolean stream;

    private boolean retainDatasetCite;

    private Map<String, Object> variables;

    public FastgptCompletionQo() {
    }

    public FastgptCompletionQo(String appId) {
        this.messages = new ArrayList<>();
        this.appId = appId;
        if (StringUtils.isEmpty(chatId)) {
            this.chatId = UUID.randomUUID().toString();
        } else  {
            this.chatId = chatId;
        }
        this.detail = false;
        this.stream = false;
        this.retainDatasetCite = false;
        this.variables = new HashMap<>();
        // 格式化日期
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss EEEE", Locale.ENGLISH);
        variables.put("cTime", sdf.format(new Date()));
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public String getResponseChatItemId() {
        return responseChatItemId;
    }

    public void setResponseChatItemId(String responseChatItemId) {
        this.responseChatItemId = responseChatItemId;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public boolean isDetail() {
        return detail;
    }

    public void setDetail(boolean detail) {
        this.detail = detail;
    }

    public boolean isStream() {
        return stream;
    }

    public void setStream(boolean stream) {
        this.stream = stream;
    }

    public boolean isRetainDatasetCite() {
        return retainDatasetCite;
    }

    public void setRetainDatasetCite(boolean retainDatasetCite) {
        this.retainDatasetCite = retainDatasetCite;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }

    public void addMessage(String role, String content) {
        this.messages.add(new Message(role, content));
    }

    public void addVariable(String key, Object value) {
        this.variables.put(key, value);
    }

    private class Message {

        private String content;

        private String dataId;

        private boolean hideInUI;

        private String role;

        public Message(){
        }

        public Message(String role, String content){
            this.role = role;
            this.content = content;
            this.dataId = UUID.randomUUID().toString();
            this.hideInUI = false;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getDataId() {
            return dataId;
        }

        public void setDataId(String dataId) {
            this.dataId = dataId;
        }

        public boolean isHideInUI() {
            return hideInUI;
        }

        public void setHideInUI(boolean hideInUI) {
            this.hideInUI = hideInUI;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }
}
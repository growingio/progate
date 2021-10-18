package io.growing.gateway.config;

/**
 * @Description: 认证配置
 * @Author: zhuhongbin
 * @Date 2021/10/18 11:15 上午
 **/
public class OAuth2Config {
    private String tokenCheckUrl;

    public void setTokenCheckUrl(String tokenCheckUrl) {
        this.tokenCheckUrl = tokenCheckUrl;
    }

    public String getTokenCheckUrl() {
        return tokenCheckUrl;
    }
}

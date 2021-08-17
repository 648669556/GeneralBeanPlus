package com.zhiyi.generalbeanplus.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Arrays;
import java.util.List;

@ConfigurationProperties("general-bean")
public class GeneralBeanProperties {
    private List<String> needPass = Arrays.asList("limit","start","createdAt","updatedAt");

    public List<String> getNeedPass() {
        return needPass;
    }

    public void setNeedPass(List<String> needPass) {
        this.needPass = needPass;
    }
}

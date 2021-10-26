package com.zhiyi.generalbeanplus.autoconfigure;

import com.zhiyi.generalbeanplus.GeneralBeanService;
import com.zhiyi.generalbeanplus.mapper.BeanDaoHandler;
import com.zhiyi.generalbeanplus.support.MapBuilder;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;

/**
 * @author chenjunhong
 */
@Configuration
@Import(MybatisAutoConfiguration.class)
@AutoConfigureAfter({MybatisAutoConfiguration.class})
@MapperScan("com.zhiyi.generalbeanplus.mapper")
@EnableConfigurationProperties(GeneralBeanProperties.class)
public class GeneralBeanAutoConfiguration {

    @Autowired
    GeneralBeanProperties generalBeanProperties;

    @Resource
    BeanDaoHandler beanDaoHandler;

    @Bean
    @ConditionalOnMissingBean
    public GeneralBeanService generalBeanService() {
        beforePrepare();
        return new GeneralBeanService(beanDaoHandler);
    }


    public void beforePrepare() {
        MapBuilder.setNeedPass(generalBeanProperties.getNeedPass());
    }
}

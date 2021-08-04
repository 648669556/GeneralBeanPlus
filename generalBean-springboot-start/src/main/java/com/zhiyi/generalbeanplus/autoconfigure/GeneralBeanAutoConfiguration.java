package com.zhiyi.generalbeanplus.autoconfigure;

import com.zhiyi.generalbeanplus.GeneralBeanService;
import com.zhiyi.generalbeanplus.mapper.BeanDaoHandler;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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
public class GeneralBeanAutoConfiguration {

    @Resource
    BeanDaoHandler beanDaoHandler;

    @Bean
    @ConditionalOnMissingBean
    public GeneralBeanService generalBeanService() {
        return new GeneralBeanService(beanDaoHandler);
    }

}

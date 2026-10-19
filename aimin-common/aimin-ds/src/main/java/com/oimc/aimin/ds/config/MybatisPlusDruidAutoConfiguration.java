package com.oimc.aimin.ds.config;


import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.oimc.aimin.ds.dict.service.DictServiceImpl;
import com.oimc.aimin.ds.translate.DictTranslateInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;



@AutoConfiguration
@Import({
        MyMetaObjectHandler.class,
        DictTranslateInterceptor.class,
        DictServiceImpl.class
})
@MapperScan("com.oimc.aimin.**.mapper")
public class MybatisPlusDruidAutoConfiguration {

    /**
     * 添加分页插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL)); // 如果配置多个插件, 切记分页最后添加
        return interceptor;
    }

}

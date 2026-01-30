package club.bugmakers.boy.config;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;

import java.io.IOException;
import java.util.Properties;

/**
 * YAML 属性源工厂类
 * 用于支持 @PropertySource 注解读取 YAML 文件
 */
public class YamlPropertySourceFactory implements PropertySourceFactory {

    /**
     * 创建属性源
     * @param name 属性源名称
     * @param resource 资源
     * @return 属性源
     * @throws IOException IO异常
     */
    @Override
    public PropertySource<?> createPropertySource(String name, EncodedResource resource) throws IOException {
        try {
            YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
            factory.setResources(resource.getResource());
            factory.afterPropertiesSet();
            Properties properties = factory.getObject();
            return new PropertiesPropertySource(name != null ? name : resource.getResource().getFilename(), properties);
        } catch (Exception e) {
            // 文件不存在或解析失败时，返回空的属性源
            return new PropertiesPropertySource(name != null ? name : "empty", new Properties());
        }
    }
}

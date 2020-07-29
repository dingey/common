package com.github.dingey.common;

import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Set;

public class CommonRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware, EnvironmentAware {
    private ResourceLoader resourceLoader;

    @Override
    public void setEnvironment(Environment environment) {
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        CommonClassPathScanner scanner = new CommonClassPathScanner(registry);
        if (resourceLoader != null) {
            scanner.setResourceLoader(resourceLoader);
        }
        scanner.doScan(this.getClass().getPackage().getName());
    }

    class CommonClassPathScanner extends ClassPathBeanDefinitionScanner {

        CommonClassPathScanner(BeanDefinitionRegistry registry) {
            super(registry);
            addExcludeFilter((metadataReader, metadataReaderFactory) -> metadataReader.getClassMetadata().getClassName().equals("com.github.dingey.common.CommonAutoConfiguration"));
        }

        @Override
        protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
            return super.doScan(basePackages);
        }
    }
}

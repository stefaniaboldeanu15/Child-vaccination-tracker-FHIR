package org.prt.prtvaccinationtracking_fhir.auth.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class LegacyRelatedPersonControllerExclusionPostProcessor implements BeanDefinitionRegistryPostProcessor {

    private static final String BROKEN_CONTROLLER_PACKAGE =
            "org.prt.prtvaccinationtracking_fhir.controller.relatedPerson.";

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        List<String> beanNames = new ArrayList<>();
        for (String beanName : registry.getBeanDefinitionNames()) {
            BeanDefinition definition = registry.getBeanDefinition(beanName);
            String beanClassName = definition.getBeanClassName();
            if (beanClassName != null && beanClassName.startsWith(BROKEN_CONTROLLER_PACKAGE)) {
                beanNames.add(beanName);
            }
        }

        for (String beanName : beanNames) {
            registry.removeBeanDefinition(beanName);
        }
    }

    @Override
    public void postProcessBeanFactory(
            org.springframework.beans.factory.config.ConfigurableListableBeanFactory beanFactory
    ) throws BeansException {
    }
}

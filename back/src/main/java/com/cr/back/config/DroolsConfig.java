package com.cr.back.config;

import org.drools.template.DataProviderCompiler;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.Message;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.Results;
import org.kie.api.io.Resource;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Configuration
public class DroolsConfig {
    private static final String KMODULE_PATH = "src/main/resources/META-INF/kmodule.xml";
    private static final String RULES_GLOB = "classpath*:rules/*.drl";
    private static final String TEMPLATE_DRT = "classpath:templates/deck-validation-template.drt";
    private static final String TEMPLATE_CSV = "classpath:templates/data/deck-validation.csv";
    private static final String GENERATED_TEMPLATE_PATH = "src/main/resources/generated/templates/deck-validation-template.drl";

    @Bean
    public KieContainer kieContainer(ResourceLoader resourceLoader) throws IOException {
        KieServices kieServices = KieServices.Factory.get();
        ReleaseId releaseId = kieServices.newReleaseId("com.cr", "back-generated-rules", "1.0.0");
        var kieFileSystem = kieServices.newKieFileSystem();
        kieFileSystem.generateAndWritePomXML(releaseId);

        writeClasspathResource(kieServices, kieFileSystem, resourceLoader.getResource("classpath:META-INF/kmodule.xml"), KMODULE_PATH);

        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(resourceLoader);
        org.springframework.core.io.Resource[] ruleResources = resolver.getResources(RULES_GLOB);
        Arrays.sort(ruleResources, Comparator.comparing(org.springframework.core.io.Resource::getFilename, Comparator.nullsLast(String::compareTo)));
        for (org.springframework.core.io.Resource ruleResource : ruleResources) {
            String filename = ruleResource.getFilename();
            if (filename == null) {
                continue;
            }
            writeClasspathResource(kieServices, kieFileSystem, ruleResource, "src/main/resources/rules/" + filename);
        }

        String generatedTemplateDrl = generateDeckValidationRules(resourceLoader);
        kieFileSystem.write(
                GENERATED_TEMPLATE_PATH,
                kieServices.getResources()
                        .newByteArrayResource(generatedTemplateDrl.getBytes(StandardCharsets.UTF_8))
                        .setSourcePath(GENERATED_TEMPLATE_PATH)
        );

        KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem).buildAll();
        Results results = kieBuilder.getResults();
        if (results.hasMessages(Message.Level.ERROR)) {
            throw new IllegalStateException("Failed to build Drools rulebase: " + results.getMessages(Message.Level.ERROR));
        }

        return kieServices.newKieContainer(releaseId);
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public KieSession deckRecommendationSession(KieContainer kieContainer) {
        return kieContainer.newKieSession("deckRecommendationSession");
    }

    private void writeClasspathResource(
            KieServices kieServices,
            org.kie.api.builder.KieFileSystem kieFileSystem,
            org.springframework.core.io.Resource resource,
            String targetPath
    ) throws IOException {
        try (InputStream inputStream = resource.getInputStream()) {
            byte[] bytes = inputStream.readAllBytes();
            Resource kieResource = kieServices.getResources()
                    .newByteArrayResource(bytes)
                    .setSourcePath(targetPath);
            kieFileSystem.write(targetPath, kieResource);
        }
    }

    private String generateDeckValidationRules(ResourceLoader resourceLoader) throws IOException {
        List<String[]> rows = readTemplateRows(resourceLoader.getResource(TEMPLATE_CSV));
        try (InputStream templateStream = resourceLoader.getResource(TEMPLATE_DRT).getInputStream()) {
            return new DataProviderCompiler()
                    .compile(new org.drools.template.objects.ArrayDataProvider(rows.toArray(String[][]::new)), templateStream);
        }
    }

    private List<String[]> readTemplateRows(org.springframework.core.io.Resource csvResource) throws IOException {
        try (InputStream inputStream = csvResource.getInputStream()) {
            List<String> lines = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8)
                    .lines()
                    .map(String::trim)
                    .filter(line -> !line.isEmpty())
                    .toList();
            if (lines.size() <= 1) {
                return List.of();
            }
            return lines.subList(1, lines.size()).stream()
                    .map(line -> line.split("\\s*,\\s*"))
                    .toList();
        }
    }
}

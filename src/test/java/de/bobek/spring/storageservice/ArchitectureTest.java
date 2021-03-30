package de.bobek.spring.storageservice;

import com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.Architectures;

@AnalyzeClasses(
        packages = "de.bobek.spring.storageservice",
        importOptions = DoNotIncludeTests.class
)
public class ArchitectureTest {

    @ArchTest
    static final ArchRule architecture = Architectures.layeredArchitecture()
            .layer("Top").definedBy("de.bobek.spring.storageservice")
            .layer("Configuration").definedBy("de.bobek.spring.storageservice.configuration")
            .layer("Common").definedBy("de.bobek.spring.storageservice.common")
            .layer("Module-API").definedBy("de.bobek.spring.storageservice.module.*.api..")
            .layer("Module-Internal").definedBy("de.bobek.spring.storageservice.module.*.internal..")
            .layer("Module-Web").definedBy("de.bobek.spring.storageservice.module.*.web..")
            .whereLayer("Top").mayNotBeAccessedByAnyLayer()
            .whereLayer("Configuration").mayNotBeAccessedByAnyLayer()
            .whereLayer("Common").mayOnlyBeAccessedByLayers("Module-Internal")
            .whereLayer("Module-API").mayOnlyBeAccessedByLayers("Module-Web", "Module-Internal")
            .whereLayer("Module-Internal").mayNotBeAccessedByAnyLayer()
            .whereLayer("Module-Web").mayNotBeAccessedByAnyLayer();
}

package pl.com.tt.flex.server;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class ArchTest {

    void servicesAndRepositoriesShouldNotDependOnWebLayer() {

        JavaClasses importedClasses = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("pl.com.tt.flex.server");

        noClasses()
            .that()
                .resideInAnyPackage("pl.com.tt.flex.server.service..")
            .or()
                .resideInAnyPackage("pl.com.tt.flex.server.repository..")
            .should().dependOnClassesThat()
                .resideInAnyPackage("..pl.com.tt.flex.server.web..")
        .because("Services and repositories should not depend on web layer")
        .check(importedClasses);
    }
}

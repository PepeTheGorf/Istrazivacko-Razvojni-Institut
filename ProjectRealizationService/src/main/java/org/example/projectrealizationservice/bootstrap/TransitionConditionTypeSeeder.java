package org.example.projectrealizationservice.bootstrap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.projectrealizationservice.model.TransitionConditionType;
import org.example.projectrealizationservice.repository.TransitionConditionTypeRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransitionConditionTypeSeeder implements ApplicationRunner {

    private final TransitionConditionTypeRepository transitionConditionTypeRepository;

    private record SeedType(String name, String description) {}

    private static final List<SeedType> SEED_TYPES = List.of(
            new SeedType(
                    "Zadatak dodeljen korisniku",
                    "Član tima koji pokušava prelaz mora imati dodeljen ovaj zadatak"
            ),
            new SeedType(
                    "Svi kriterijumi prihvatanja ispunjeni",
                    "Svi kriterijumi prihvatanja (stavke sa liste) koje je menadžer definisao moraju biti označeni pre prelaza"
            ),
            new SeedType(
                    "Svi podzadaci završeni",
                    "Svi podzadaci (i pod-podzadaci do 4. nivoa) ovog zadatka moraju biti u finalnoj fazi pre nego što roditeljski zadatak može preći"
            ),
            new SeedType(
                    "Nema otvorenih problema",
                    "Ne sme postojati nijedan nerešen prijavljen problem na ovom zadatku pre prelaska u sledeću fazu"
            ),
            new SeedType(
                    "Zadatak u roku",
                    "Zadatak ne sme prekoračiti definisani rok da bi se normalno prešlo u sledeću fazu"
            )
    );

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (SeedType seedType : SEED_TYPES) {
            if (transitionConditionTypeRepository.existsByName(seedType.name())) {
                continue;
            }

            TransitionConditionType entity = TransitionConditionType.builder()
                    .name(seedType.name())
                    .description(seedType.description())
                    .build();

            try {
                transitionConditionTypeRepository.save(entity);
                log.info("Seeded transition condition type '{}'.", seedType.name());
            } catch (DataIntegrityViolationException ex) {
                log.info("Transition condition type '{}' already exists, skipping.", seedType.name());
            }
        }
    }
}

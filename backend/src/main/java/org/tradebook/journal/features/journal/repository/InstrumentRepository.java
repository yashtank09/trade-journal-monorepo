package org.tradebook.journal.features.journal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.tradebook.journal.features.journal.entity.Instrument;

import java.util.Optional;

@Repository
public interface InstrumentRepository extends JpaRepository<Instrument, Integer> {
    Optional<Instrument> findBySymbolAndExchange(String symbol, String exchange);
}

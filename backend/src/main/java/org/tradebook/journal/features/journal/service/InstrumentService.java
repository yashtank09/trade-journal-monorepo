package org.tradebook.journal.features.journal.service;

import org.tradebook.journal.features.journal.entity.Instrument;
import java.util.List;

public interface InstrumentService {
    Instrument save(Instrument instrument);
    List<Instrument> findAll();
}

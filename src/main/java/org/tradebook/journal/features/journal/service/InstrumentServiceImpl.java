package org.tradebook.journal.features.journal.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.tradebook.journal.features.journal.entity.Instrument;
import org.tradebook.journal.features.journal.repository.InstrumentRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InstrumentServiceImpl implements InstrumentService {

    private final InstrumentRepository instrumentRepository;

    @Override
    public Instrument save(Instrument instrument) {
        return instrumentRepository.save(instrument);
    }

    @Override
    public List<Instrument> findAll() {
        return instrumentRepository.findAll();
    }
}

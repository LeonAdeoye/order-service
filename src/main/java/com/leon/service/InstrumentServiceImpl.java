package com.leon.service;

import com.leon.model.Instrument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.beans.factory.InitializingBean;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class InstrumentServiceImpl implements InstrumentService, InitializingBean
{
    private static final Logger logger = LoggerFactory.getLogger(InstrumentServiceImpl.class);
    @Autowired
    private InstrumentRepository instrumentRepository;
    private List<Instrument> instruments = new ArrayList<>();

    @Override
    public List<Instrument> getAll()
    {
        return instruments;
    }

    @Override
    public Optional<Instrument> getByInstrumentCode(String instrumentCode)
    {
        return instruments.stream()
                .filter(instrument -> instrument.getInstrumentCode().equals(instrumentCode))
                .findFirst();
    }

    @Override
    public void afterPropertiesSet()
    {
        instruments.addAll(instrumentRepository.findAll());
        logger.info("Loaded instrument service with {} instruments(s).", instruments.size());
    }

    @Override
    public String getInstrumentSector(String instrumentCode)
    {
        if (instrumentCode == null || instrumentCode.isBlank()) {
            return "Unknown";
        }
        
        return getByInstrumentCode(instrumentCode)
                .map(Instrument::getSector)
                .filter(sector -> sector != null && !sector.isBlank())
                .orElse("Unknown");
    }

    @Override
    public String getInstrumentCountry(String instrumentCode)
    {
        if (instrumentCode == null || instrumentCode.isBlank()) {
            return "Unknown";
        }
        
        return getByInstrumentCode(instrumentCode)
                .map(Instrument::getCountry)
                .filter(country -> country != null && !country.isBlank())
                .orElse("Unknown");
    }

    @Override
    public void reconfigure()
    {
        instruments.clear();
        afterPropertiesSet();
    }
}

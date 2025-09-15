package com.leon.service;

import com.leon.model.Instrument;
import java.util.List;
import java.util.Optional;

public interface InstrumentService
{
    List<Instrument> getAll();
    Optional<Instrument> getByInstrumentCode(String instrumentCode);
    String getInstrumentSector(String instrumentCode);
    String getInstrumentCountry(String instrumentCode);
    void reconfigure();
}

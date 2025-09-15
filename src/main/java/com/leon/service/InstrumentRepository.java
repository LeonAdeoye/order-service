package com.leon.service;

import com.leon.model.Instrument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface InstrumentRepository extends MongoRepository<Instrument, String>
{
    Optional<Instrument> findByInstrumentCode(String instrumentCode);
}

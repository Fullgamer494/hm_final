package com.hugin_munin.service;

import com.hugin_munin.model.CausaBaja;
import com.hugin_munin.repository.CausaBajaRepository;

import java.sql.SQLException;
import java.util.List;

public class CausaBajaService {
    private final CausaBajaRepository bajaRepo;

    public CausaBajaService(CausaBajaRepository bajaRepo) {
        this.bajaRepo = bajaRepo;
    }

    //Get all
    public List<CausaBaja> getAll() throws SQLException {
        return bajaRepo.getAllCausaBaja();
    }

    //GetById
    public List <CausaBaja> getById(int id) throws SQLException {
        return bajaRepo.getByIdCausaBaja(id);
    }

    //Update
    public boolean update (int id, String causaBaja) throws SQLException {
        return bajaRepo.updateCausaBaja(id,causaBaja);
    }

}
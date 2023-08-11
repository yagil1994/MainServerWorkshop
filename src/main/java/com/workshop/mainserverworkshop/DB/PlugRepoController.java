package com.workshop.mainserverworkshop.DB;

import java.util.ArrayList;
import java.util.List;

public class PlugRepoController {
    private final PlugRepository plugRepository;

    public PlugRepoController(PlugRepository plugRepository) {
        this.plugRepository = plugRepository;
    }

    public void SavePlugToDB(PlugSave plugSave){
        RemovePlugFromDB(plugSave);
        plugRepository.save(plugSave);
    }

    public void RemovePlugFromDB(PlugSave plugSave){
        if(plugRepository.existsById(plugSave.getPlugTitle())){
            plugRepository.delete(plugSave);
        }
    }

    public List<PlugSave> GetAllPlugsFromDB(){
        List<PlugSave> plugSaveList;
        if(plugRepository.count() == 0){
            plugSaveList = new ArrayList<>();
        }
        else {
            plugSaveList = plugRepository.findAll();
        }

        return plugSaveList;
    }
}

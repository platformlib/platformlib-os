package com.platformlib.os.core.bean;

import com.platformlib.os.api.dto.IdNamePair;

public class IdNamePairBean implements IdNamePair {
    private final int id;
    private final String name;

    public IdNamePairBean(final int id, final  String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }
}

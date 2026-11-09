package com.hd.model.po;

import java.io.Serializable;

import lombok.Data;

@Data
public abstract class BasePo<T extends Serializable> {

    public abstract T getId();
}

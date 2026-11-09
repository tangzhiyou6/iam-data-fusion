package com.hd.model.vo;

import java.io.Serializable;
import java.util.List;

public abstract class DataFusionBaseVo<T, D> implements Serializable {

    public abstract List<T> buildPo(D sourceDataId, String scene);
}

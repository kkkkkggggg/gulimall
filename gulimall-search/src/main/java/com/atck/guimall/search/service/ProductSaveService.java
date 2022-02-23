package com.atck.guimall.search.service;

import com.atck.common.to.es.SkuEsModel;

import java.io.IOException;
import java.util.List;

public interface ProductSaveService
{
    boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException;
}

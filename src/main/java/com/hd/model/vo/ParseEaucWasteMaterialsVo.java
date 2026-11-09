package com.hd.model.vo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hd.model.po.EaucWasteMaterialsPo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

/**
 * 电能e拍-废料物资解析VO
 * 用于接收和映射LLM或接口返回的废料物资JSON结构
 */
@Slf4j
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParseEaucWasteMaterialsVo extends DataFusionBaseVo<EaucWasteMaterialsPo, String> {

    /**
     * 文档完整名称
     */
    @JsonProperty("file_name")
    private String fileName;

    /**
     * 申报单位全称
     */
    @JsonProperty("declare_company")
    private String declareCompany;

    /**
     * 物料列表
     */
    @JsonProperty("materials")
    //private List<Material> materials;
    private List<List<String>> materials;

    @Override
    public List<EaucWasteMaterialsPo> buildPo(String sourceDataId, String scene) {
        String batchNo = UUID.randomUUID().toString();
        if (!CollectionUtils.isEmpty(materials)) {
            List<EaucWasteMaterialsPo> result = new ArrayList<>();
            /*for (Material material : materials) {
                EaucWasteMaterialsPo po = this.buildCommonPo(sourceDataId, batchNo);
                po.setMaterialName(material.getMaterialName());
                po.setQuantity(material.getQuantity());
                po.setUnit(material.getUnit());
                po.setSpecification(material.getSpecification());
                po.setEstimatedWeight(material.getEstimatedWeight());
                result.add(po);
            }*/
            for (List<String> material : materials) {
                if (CollectionUtils.isEmpty(material) || material.size() < 5) {
                    log.error("电能e拍-废料物资解析，material数据有缺失，material：{}", null == material ? "NULL" : material);
                }
                EaucWasteMaterialsPo po = this.buildCommonPo(sourceDataId, batchNo);
                po.setMaterialName(material.get(0));
                po.setQuantity(material.get(1));
                po.setUnit(material.get(2));
                po.setSpecification(material.get(3));
                po.setEstimatedWeight(material.get(4));
                result.add(po);
            }
            return result;
        } else {
            return Collections.singletonList(this.buildCommonPo(sourceDataId, batchNo));
        }
    }

    private EaucWasteMaterialsPo buildCommonPo(String sourceDataId, String batchNo){
        EaucWasteMaterialsPo po = new EaucWasteMaterialsPo();
        po.setSourceId(sourceDataId);
        po.setBatchNo(batchNo);
        po.setFileName(this.fileName);
        po.setDeclareCompany(this.declareCompany);
        return po;
    }

    /**
     * 物料内部类
     * 对应JSON中的 materials 数组元素
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Material {
        /**
         * 物料名称
         */
        @JsonProperty("material_name")
        private String materialName;

        /**
         * 数量
         */
        @JsonProperty("quantity")
        private String quantity;

        /**
         * 计量单位
         */
        @JsonProperty("unit")
        private String unit;

        /**
         * 物料规格
         */
        @JsonProperty("specification")
        private String specification;

        /**
         * 预估重量(吨)
         */
        @JsonProperty("estimated_weight")
        private String estimatedWeight;
    }
}
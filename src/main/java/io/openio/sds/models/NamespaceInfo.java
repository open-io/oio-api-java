package io.openio.sds.models;

import java.util.Map;

import com.google.gson.annotations.SerializedName;

/**
 * 
 * @author Christopher Dedeurwaerder
 *
 */
public class NamespaceInfo {

    private String ns;
    private Long chunksize;
    private Map<String, String> options;
    @SerializedName("storage_policy")
    private Map<String, String> storagePolicies;
    @SerializedName("storage_class")
    private Map<String, String> storageClasses;
    @SerializedName("data_security")
    private Map<String, String> dataSecurities;
    @SerializedName("data_treatments")
    private Map<String, String> dataTreatments;

    public NamespaceInfo() {
    }

    public String ns() {
        return ns;
    }

    public NamespaceInfo ns(String ns) {
        this.ns = ns;
        return this;
    }

    public Long chunksize() {
        return chunksize;
    }

    public NamespaceInfo chunksize(Long chunksize) {
        this.chunksize = chunksize;
        return this;
    }

    public Map<String, String> options() {
        return options;
    }

    public NamespaceInfo options(Map<String, String> options) {
        this.options = options;
        return this;
    }

    public Map<String, String> storagePolicies() {
        return storagePolicies;
    }

    public NamespaceInfo storagePolicies(Map<String, String> storagePolicies) {
        this.storagePolicies = storagePolicies;
        return this;
    }

    public Map<String, String> storageClasses() {
        return storageClasses;
    }

    public NamespaceInfo storageClasses(Map<String, String> storageClasses) {
        this.storageClasses = storageClasses;
        return this;
    }

    public Map<String, String> dataSecurities() {
        return dataSecurities;
    }

    public NamespaceInfo dataSecurities(Map<String, String> dataSecurities) {
        this.dataSecurities = dataSecurities;
        return this;
    }

    public Map<String, String> dataTreatments() {
        return dataTreatments;
    }

    public NamespaceInfo dataTreatments(Map<String, String> dataTreatments) {
        this.dataTreatments = dataTreatments;
        return this;
    }

}

package io.openio.sds.models;

import io.openio.sds.common.MoreObjects;

public class ContainerInfo {

    private String name;
    private String account;
    private Long ctime;
    private Long init;
    private Long usage;
    private Long version;
    private String id;
    private String ns;
    private String type;
    private String user;
    private String schemavers;
    private String versionMainAdmin;
    private String versionMainAliases;
    private String versionMainChunks;
    private String versionMainContents;
    private String versionMainProperties;

    public ContainerInfo(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    public ContainerInfo name(String name) {
        this.name = name;
        return this;
    }

    public String account() {
        return account;
    }

    public ContainerInfo account(String account) {
        this.account = account;
        return this;
    }

    public Long ctime() {
        return ctime;
    }

    public ContainerInfo ctime(Long ctime) {
        this.ctime = ctime;
        return this;
    }

    public Long init() {
        return init;
    }

    public ContainerInfo init(Long init) {
        this.init = init;
        return this;
    }

    public Long usage() {
        return usage;
    }

    public ContainerInfo usage(Long usage) {
        this.usage = usage;
        return this;
    }

    public Long version() {
        return version;
    }

    public ContainerInfo version(Long version) {
        this.version = version;
        return this;
    }

    public String id() {
        return id;
    }

    public ContainerInfo id(String id) {
        this.id = id;
        return this;
    }

    public String ns() {
        return ns;
    }

    public ContainerInfo ns(String ns) {
        this.ns = ns;
        return this;
    }

    public String type() {
        return type;
    }

    public ContainerInfo type(String type) {
        this.type = type;
        return this;
    }

    public String user() {
        return user;
    }

    public ContainerInfo user(String user) {
        this.user = user;
        return this;
    }

    public String schemavers() {
        return schemavers;
    }

    public ContainerInfo schemavers(String schemavers) {
        this.schemavers = schemavers;
        return this;
    }

    public String versionMainAdmin() {
        return versionMainAdmin;
    }

    public ContainerInfo versionMainAdmin(String versionMainAdmin) {
        this.versionMainAdmin = versionMainAdmin;
        return this;
    }

    public String versionMainAliases() {
        return versionMainAliases;
    }

    public ContainerInfo versionMainAliases(String versionMainAliases) {
        this.versionMainAliases = versionMainAliases;
        return this;
    }

    public String versionMainChunks() {
        return versionMainChunks;
    }

    public ContainerInfo versionMainChunks(String versionMainChunks) {
        this.versionMainChunks = versionMainChunks;
        return this;
    }

    public String versionMainContents() {
        return versionMainContents;
    }

    public ContainerInfo versionMainContents(String versionMainContents) {
        this.versionMainContents = versionMainContents;
        return this;
    }

    public String versionMainProperties() {
        return versionMainProperties;
    }

    public ContainerInfo versionMainProperties(
            String versionMainProperties) {
        this.versionMainProperties = versionMainProperties;
        return this;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .omitNullValues()
                .add("name", name)
                .add("account", account)
                .add("m2-ctime", ctime)
                .add("m2-init", init)
                .add("m2-usage", usage)
                .add("id", id)
                .add("ns", ns)
                .add("type", type)
                .add("user-name", user)
                .toString();
    }
}
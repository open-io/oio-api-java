package io.openio.sds.models;

public class ObjectDeletionOptions {

    private Long version;
    private boolean simulateVersioning = false;
    private boolean deleteMarker = false;

    public Long version() {
        return version;
    }

    public ObjectDeletionOptions version(Long version) {
        this.version = version;
        return this;
    }

    public boolean simulateVersioning() {
        return simulateVersioning;
    }

    public ObjectDeletionOptions simulateVersioning(boolean simulateVersioning) {
        this.simulateVersioning = simulateVersioning;
        return this;
    }

    public boolean deleteMarker() {
        return deleteMarker;
    }

    public ObjectDeletionOptions deleteMarker(boolean deleteMarker) {
        this.deleteMarker = deleteMarker;
        return this;
    }

}

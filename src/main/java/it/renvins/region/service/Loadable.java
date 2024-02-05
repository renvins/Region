package it.renvins.region.service;

public interface Loadable {

    void load();
    default void unload() {}
}

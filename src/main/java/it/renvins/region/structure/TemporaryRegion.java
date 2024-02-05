package it.renvins.region.structure;

import lombok.Getter;
import lombok.Setter;

@Setter @Getter
public class TemporaryRegion implements IRegion {

    private LazyLocation loc1;
    private LazyLocation loc2;

    public Region toRegion(String name) {
        if (loc1 == null || loc2 == null) {
            return null;
        }
        return new Region(name, loc1, loc2);
    }
}
